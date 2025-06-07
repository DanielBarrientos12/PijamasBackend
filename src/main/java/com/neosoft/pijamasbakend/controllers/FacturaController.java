package com.neosoft.pijamasbakend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neosoft.pijamasbakend.entities.Cliente;
import com.neosoft.pijamasbakend.enums.EstadoFactura;
import com.neosoft.pijamasbakend.models.CheckoutRequest;
import com.neosoft.pijamasbakend.models.CheckoutResponse;
import com.neosoft.pijamasbakend.repositories.ClienteRepository;
import com.neosoft.pijamasbakend.repositories.FacturaRepository;
import com.neosoft.pijamasbakend.services.FacturaService;
import jakarta.validation.Valid;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;
    @Autowired
    private ClienteRepository clienteRepo;
    @Autowired
    private FacturaRepository facturaRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<CheckoutResponse> crearFactura(@Valid @RequestBody CheckoutRequest req) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Cliente cliente = clienteRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no existe"));

        CheckoutResponse resp = facturaService.checkout(req, cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /* ====== Webhook de Wompi (público) ====== */
    @Value("${wompi.events-secret}")
    private String wompiEventsSecret;

    @PostMapping("/wompi-webhook")
    public ResponseEntity<Void> wompiWebhook(@RequestHeader("X-Event-Signature") String signature, @RequestBody String rawBody) throws IOException {

        // Validar firma
        String local = DigestUtils.sha512Hex(rawBody + wompiEventsSecret);
        if (!local.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Parsear JSON
        JsonNode root = mapper.readTree(rawBody);
        JsonNode data = root.path("data");
        String ref = data.path("reference").asText();
        String status = data.path("status").asText();

        String authCode = null;
        JsonNode extra = data.path("payment_method").path("extra");
        if (extra.has("external_identifier")) {
            authCode = extra.get("external_identifier").asText(null);
        }

        facturaService.actualizarDesdeWebhook(ref, status, authCode);
        return ResponseEntity.ok().build();
    }

    /* ====== Ver factura propia ====== */
    @GetMapping("/{id}")
    public ResponseEntity<?> verFactura(@PathVariable Integer id) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        var factura = facturaRepo.findById(id)
                .filter(f -> f.getCliente().getEmail().equals(email))
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(factura);
    }

    /* ====== Listado (sólo admin) ====== */
    @GetMapping
    public ResponseEntity<?> listarTodas(@RequestParam(name = "estado", required = false) EstadoFactura estado) {
        var lista = (estado == null) ? facturaRepo.findAll() : facturaRepo.findByEstado(estado);
        return ResponseEntity.ok(lista);
    }
}

