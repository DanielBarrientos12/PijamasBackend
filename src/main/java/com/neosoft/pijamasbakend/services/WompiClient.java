package com.neosoft.pijamasbakend.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.neosoft.pijamasbakend.entities.Factura;
import com.neosoft.pijamasbakend.models.CheckoutRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WompiClient {

    @Value("${wompi.base-url}")
    String base;
    @Value("${wompi.private-key}")
    String prv;
    @Value("${wompi.integrity-key}")
    String integ;

    WebClient cli = WebClient.builder()
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + prv)
            .build();

    /* =======   Modelos internos   ======= */
    record TxRequest(String acceptance_token, String accept_personal_auth,
                     Long amount_in_cents, String currency, String customer_email,
                     String reference, Map<String, Object> payment_method,
                     String payment_method_type, String signature,
                     Long payment_source_id, Boolean recurrent) {
    }

    record TxData(String id, String status,
                  @JsonProperty("redirect_url") String redirectUrl,
                  @JsonProperty("payment_method") Map<String, Object> pm) {
    }

    record TxResp(TxData data) {
    }

    /* =======   Crear transacción   ======= */
    TxResp createTx(Factura f, CheckoutRequest r, Long cents) {
        String plain = cents + "COP" + f.getReferencia();
        String sig = DigestUtils.sha256Hex(integ + plain);

        TxRequest body = new TxRequest(
                r.acceptanceToken(), r.acceptPersonalAuth(),
                cents, "COP", f.getCliente().getEmail(),
                f.getReferencia(), r.metodoPagoDetail(),
                r.metodoPago(), sig,
                (Long) r.metodoPagoDetail().get("payment_source_id"), // null si no viene
                (Boolean) r.metodoPagoDetail().getOrDefault("recurrent", false)
        );

        return cli.post().uri(base + "/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(TxResp.class)
                .block();
    }

    /* =======   Consulta por ID   ======= */
    TxResp getTx(String id) {
        return cli.get().uri(base + "/transactions/" + id)
                .retrieve().bodyToMono(TxResp.class).block();
    }
}
