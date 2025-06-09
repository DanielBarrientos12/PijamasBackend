package com.neosoft.pijamasbakend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neosoft.pijamasbakend.entities.Cliente;
import com.neosoft.pijamasbakend.entities.Factura;
import com.neosoft.pijamasbakend.models.CheckoutRequest;
import com.neosoft.pijamasbakend.models.CheckoutResponse;
import com.neosoft.pijamasbakend.services.ClienteService;
import com.neosoft.pijamasbakend.services.FacturaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;
    private final ClienteService clienteService;
    private final ObjectMapper mapper;
    @Value("${wompi.integrity-key}")
    private String integrityKey;

    @GetMapping
    public ResponseEntity<List<Factura>> getAll() {
        List<Factura> facturas = facturaService.obtenerFacturas();
        if (facturas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> getById(@PathVariable Integer id) {
        try {
            Factura factura = facturaService.obtenerFactura(id);
            return ResponseEntity.ok(factura);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Factura>> listarPorCliente(@PathVariable Integer clienteId) {

        List<Factura> facturas = facturaService.listarFacturasPorCliente(clienteId);
        if (facturas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(facturas);
    }

    @PostMapping("/checkout")
    public CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth.getPrincipal() instanceof UserDetails ud)
                ? ud.getUsername()
                : auth.getPrincipal().toString();
        Cliente cliente = clienteService.findByEmail(email);

        CheckoutResponse resp = facturaService.checkout(request, cliente);

        log.info("Checkout OK. Factura {} – Cliente {}", resp.referencia(), email);
        return resp;
    }

    @PostMapping("/webhook/wompi")
    public ResponseEntity<Void> webhookWompi(@RequestBody String rawBody, @RequestHeader("X-Integrity-Signature") String signature) {

        // 1. Calcular firma esperada
        String expected = DigestUtils.sha256Hex(rawBody + integrityKey);

        if (!expected.equalsIgnoreCase(signature)) {
            log.warn("Webhook rechazado: firma inválida");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 2. Parsear JSON
        JsonNode root;
        try {
            root = mapper.readTree(rawBody);
        } catch (Exception ex) {
            log.error("Payload webhook no es JSON: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        }

        JsonNode data = root.path("data");
        if (data.isMissingNode()) return ResponseEntity.ok().build();  // evento desconocido

        String reference = data.path("reference").asText(null);
        String status = data.path("status").asText(null);
        String authCode = data.path("authorization_code").asText(null);

        if (reference == null || status == null) {
            log.warn("Webhook sin reference o status");
            return ResponseEntity.ok().build();
        }

        facturaService.actualizarDesdeWebhook(reference, status, authCode);
        log.info("Webhook OK. Factura {} → {}", reference, status);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync-pendientes")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void syncPendientes() {
        facturaService.sincronizarFacturasPendientes();
        log.info("Sincronización manual desencadenada");
    }
}
