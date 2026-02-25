package com.helper.payment.service;

import com.helper.payment.entity.PlatformConfig;
import com.helper.payment.repository.PlatformConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformConfigService {

    private final PlatformConfigRepository configRepo;

    @Value("${app.payment.default-commission-rate:0.02}")
    private String defaultCommissionRate;

    @Value("${app.payment.default-gst-rate:0.18}")
    private String defaultGstRate;

    @Value("${app.payment.default-cancellation-fee-rate:0.10}")
    private String defaultCancellationRate;

    public BigDecimal getCommissionRate() {
        return getRate(PlatformConfig.COMMISSION_RATE, defaultCommissionRate);
    }

    public BigDecimal getGstRate() {
        return getRate(PlatformConfig.GST_RATE, defaultGstRate);
    }

    public BigDecimal getCancellationFeeRate() {
        return getRate(PlatformConfig.CANCELLATION_FEE_RATE, defaultCancellationRate);
    }

    public String getConfig(String key) {
        return configRepo.findById(key).map(PlatformConfig::getConfigValue).orElse(null);
    }

    public List<PlatformConfig> getAllConfigs() {
        return configRepo.findAll();
    }

    public PlatformConfig updateConfig(String key, String value, UUID adminId) {
        PlatformConfig config = configRepo.findById(key)
                .orElse(PlatformConfig.builder().configKey(key).build());
        config.setConfigValue(value);
        config.setUpdatedBy(adminId);
        config = configRepo.save(config);
        log.info("Config updated: {} = {} by admin: {}", key, value, adminId);
        return config;
    }

    private BigDecimal getRate(String key, String defaultValue) {
        return configRepo.findById(key)
                .map(c -> new BigDecimal(c.getConfigValue()))
                .orElse(new BigDecimal(defaultValue));
    }
}
