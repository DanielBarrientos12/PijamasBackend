package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.models.*;
import com.neosoft.pijamasbakend.services.WompiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wompi")
@RequiredArgsConstructor
public class PaymentController {

    private final WompiClient wompi;

    @PostMapping("/tokens/cards")
    public CardTokenResponse tokenizarTarjeta(@RequestBody CardTokenRequest body) {
        return wompi.createCardToken(body);
    }

    @PostMapping("/tokens/nequi")
    public NequiTokenStatus tokenizarNequi(@RequestBody NequiTokenRequest body) {
        return wompi.createNequiToken(body);
    }

    @GetMapping("/tokens/nequi/{id}")
    public NequiTokenStatus estadoNequi(@PathVariable String id) {
        return wompi.getNequiTokenStatus(id);
    }

    @PostMapping("/payment-sources")
    public PaymentSource crearFuente(@RequestBody PaymentSourceRequest body) {
        return wompi.createPaymentSource(body);
    }

    @GetMapping("/payment-sources/{id}")
    public PaymentSource obtenerFuente(@PathVariable Integer id) {
        return wompi.getPaymentSource(id);
    }
}
