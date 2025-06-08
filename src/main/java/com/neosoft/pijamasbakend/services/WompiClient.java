package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.models.*;
import com.neosoft.pijamasbakend.utils.WompiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class WompiClient {

    private final WebClient pubCli;
    private final WebClient prvCli;
    private final String integrityKey;

    public WompiClient(
            @Value("${wompi.base-url}")
            String baseUrl,
            @Value("${wompi.public-key}")
            String pubKey,
            @Value("${wompi.private-key}")
            String prvKey,
            @Value("${wompi.integrity-key}")
            String integrityKey
    ) {
        this.pubCli = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        "Bearer " + pubKey)
                .build();
        this.prvCli = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        "Bearer " + prvKey)
                .build();

        this.integrityKey = integrityKey;
    }

    public TransactionResponse createTransaction(TransactionRequest body, Long paymentSourceId) {

        WebClient cli = (paymentSourceId == null) ? pubCli : prvCli;

        return cli.post().uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(TransactionResponse.class)
                .block();
    }

    public TransactionResponse getTransaction(String id) {
        return pubCli.get()
                .uri("/transactions/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(TransactionResponse.class)
                .block();
    }

    public CardTokenResponse createCardToken(CardTokenRequest body) {
        return pubCli.post().uri("/tokens/cards")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(CardTokenResponse.class)
                .block();
    }

    public NequiTokenStatus createNequiToken(NequiTokenRequest body) {
        return pubCli.post().uri("/tokens/nequi")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(NequiTokenStatus.class)
                .block();
    }

    public NequiTokenStatus getNequiTokenStatus(String tokenId) {
        return pubCli.get().uri("/tokens/nequi/{id}", tokenId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(NequiTokenStatus.class)
                .block();
    }

    public PaymentSource createPaymentSource(PaymentSourceRequest body) {
        return prvCli.post().uri("/payment_sources")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(PaymentSource.class)
                .block();
    }

    public PaymentSource getPaymentSource(Integer id) {
        return prvCli.get().uri("/payment_sources/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(PaymentSource.class)
                .block();
    }

    private void require(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            if (!map.containsKey(k) || map.get(k) == null) {
                throw new IllegalArgumentException(
                        "Falta el campo obligatorio «" + k + "» en payment_method");
            }
        }
    }

    private Mono<? extends Throwable> mapError(ClientResponse resp) {
        return resp.bodyToMono(String.class)
                .flatMap(body -> Mono.error(
                        new WompiException(resp.statusCode(), body)));
    }

    public String buildSignature(long amountInCents, String reference) {
        String plain = amountInCents + "COP" + reference + integrityKey;
        return DigestUtils.sha256Hex(plain);
    }

    public Map<String, Object> buildPaymentMethod(Map<String, Object> raw) {
        String type = (String) raw.get("type");
        if (type == null) throw new IllegalArgumentException("type es obligatorio");

        switch (type.toUpperCase()) {
            case "CARD" -> {
                if (!raw.containsKey("payment_source_id"))
                    require(raw, "token");
                require(raw, "installments");
            }
            case "NEQUI", "DAVIPLATA" -> require(raw, "phone_number");
            case "PSE" -> require(raw, "user_type", "user_legal_id_type",
                    "user_legal_id", "financial_institution_code");
            default -> throw new IllegalArgumentException("Método de pago desconocido: " + type);
        }
        raw.remove("payment_source_id");
        return raw;
    }

}
