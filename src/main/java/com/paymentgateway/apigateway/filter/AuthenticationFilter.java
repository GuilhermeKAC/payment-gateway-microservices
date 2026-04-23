package com.paymentgateway.apigateway.filter;

import com.paymentgateway.apigateway.service.MerchantCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String MERCHANT_ID_HEADER = "X-Merchant-Id";

    private final MerchantCacheService merchantCacheService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Request rejected: missing X-API-Key header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return merchantCacheService.findByApiKey(apiKey)
                .flatMap(merchant -> {
                    if (!"ACTIVE".equals(merchant.getStatus())) {
                        log.warn("Request rejected: merchant {} is {}", merchant.getMerchantId(), merchant.getStatus());
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    log.info("Request authorized for merchant: {}", merchant.getMerchantId());

                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(r -> r.header(MERCHANT_ID_HEADER, merchant.getMerchantId()))
                            .build();

                    return chain.filter(mutatedExchange);
                });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}