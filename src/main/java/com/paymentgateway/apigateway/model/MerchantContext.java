package com.paymentgateway.apigateway.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantContext {
    private String merchantId;
    private String apiKey;
    private String status;
}