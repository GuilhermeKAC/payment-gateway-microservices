package com.paymentgateway.apigateway.filter;

import com.paymentgateway.apigateway.util.HmacUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SignatureFilter implements GlobalFilter, Ordered {

    private static final String SIGNATURE_HEADER = "X-Signature";
    private static final String MERCHANT_SECRET = "secret-temporario-fase2";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();

        // Só valida assinatura em requisições que modificam estado
        if (!requiresSignature(method)) {
            return chain.filter(exchange);
        }

        String signature = exchange.getRequest().getHeaders().getFirst(SIGNATURE_HEADER);

        if (signature == null || signature.isBlank()) {
            log.warn("Request rejected: missing X-Signature header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    String body = dataBuffer.toString(StandardCharsets.UTF_8);
                    DataBufferUtils.release(dataBuffer);

                    if (!HmacUtil.verify(body, MERCHANT_SECRET, signature)) {
                        log.warn("Request rejected: invalid signature");
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    log.info("Signature verified successfully");
                    return chain.filter(exchange);
                });
    }

    private boolean requiresSignature(HttpMethod method) {
        return method == HttpMethod.POST
                || method == HttpMethod.PUT
                || method == HttpMethod.DELETE;
    }

    @Override
    public int getOrder() {
        return -90; // executa depois do AuthenticationFilter (-100)
    }
}