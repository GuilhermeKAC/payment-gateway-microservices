package com.paymentgateway.apigateway.service;

import com.paymentgateway.apigateway.model.MerchantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantCacheService {

    private static final String CACHE_PREFIX = "cache:merchant:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final ReactiveRedisTemplate<String, MerchantContext> merchantRedisTemplate;

    public Mono<MerchantContext> findByApiKey(String apiKey) {
        String key = CACHE_PREFIX + apiKey;

        return merchantRedisTemplate.opsForValue().get(key)
                .doOnNext(merchant -> log.debug("Cache HIT for api_key: {}...", apiKey.substring(0, 8)))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Cache MISS for api_key: {}...", apiKey.substring(0, 8));
                    return mockMerchant(apiKey);
                }));
    }

    public Mono<Void> save(String apiKey, MerchantContext merchant) {
        String key = CACHE_PREFIX + apiKey;
        return merchantRedisTemplate.opsForValue()
                .set(key, merchant, CACHE_TTL)
                .then();
    }

    // Temporário — substitído pelo Merchant Service real na Fase 2
    private Mono<MerchantContext> mockMerchant(String apiKey) {
        MerchantContext merchant = MerchantContext.builder()
                .merchantId("merchant-001")
                .apiKey(apiKey)
                .status("ACTIVE")
                .build();

        return save(apiKey, merchant).thenReturn(merchant);
    }
}