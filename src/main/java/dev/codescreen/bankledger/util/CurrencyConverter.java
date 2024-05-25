package dev.codescreen.bankledger.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.math.RoundingMode;

@Component
public class CurrencyConverter {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyConverter.class);
    private Map<String, BigDecimal> conversionRates = new ConcurrentHashMap<>();
    private RestTemplate restTemplate = new RestTemplate();
    @Value("${api.exchangeRate.key}")
    String API_KEY;
    @Scheduled(fixedRate = 86400000) // Update every 24 hours
    public void fetchConversionRates() {

        String url = "https://v6.exchangerate-api.com/v6/"+API_KEY+"/latest/USD"; // Example API
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                Map<String, Number> rates = (Map<String, Number>) responseBody.get("conversion_rates");
                conversionRates.clear();
                rates.forEach((currency, rate) -> conversionRates.put(currency, BigDecimal.valueOf(rate.doubleValue())));
            }
        } catch (Exception e) {
            logger.error("Failed to fetch conversion rates, defaulting to rate of 1. Error: {}", e.getMessage());
            conversionRates.clear();
        }
    }

    public BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal amount) {
        BigDecimal fromRate = conversionRates.getOrDefault(fromCurrency, BigDecimal.ONE);
        BigDecimal toRate = conversionRates.getOrDefault(toCurrency, BigDecimal.ONE);
        BigDecimal rate = toRate.divide(fromRate, 10, RoundingMode.HALF_UP);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
    }
}
