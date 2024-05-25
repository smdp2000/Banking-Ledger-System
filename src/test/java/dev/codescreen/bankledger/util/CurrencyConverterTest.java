import dev.codescreen.bankledger.util.CurrencyConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CurrencyConverterTest.Config.class)
class CurrencyConverterTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CurrencyConverter currencyConverter;

    @Value("${api.exchangeRate.key}")
    private String apiKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Map<String, Number> rates = new HashMap<>();
        rates.put("USD", 1.0);
        rates.put("EUR", 0.9);
        rates.put("GBP", 0.8);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("conversion_rates", rates);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // Manually fetch rates to populate conversionRates
        currencyConverter.fetchConversionRates();
    }

    @Test
    void testConvertUSDToEUR() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal expected = new BigDecimal("90.00").setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal result = currencyConverter.convert("USD", "EUR", amount);
        assertEquals(expected, result, "Conversion from USD to EUR should be correct");
    }

    @Test
    void testConvertGBPToUSD() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal expected = new BigDecimal("125.00").setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal result = currencyConverter.convert("GBP", "USD", amount);
        assertEquals(expected, result, "Conversion from GBP to USD should be correct");
    }

    @Test
    void testConvertUSDtoUSD() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal expected = new BigDecimal("100.00").setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal result = currencyConverter.convert("USD", "USD", amount);
        assertEquals(expected, result, "Conversion from USD to USD should be the same amount");
    }

    @Test
    void testConvertNonExistingCurrency() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal expected = new BigDecimal("100.00").setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal result = currencyConverter.convert("XXX", "USD", amount);
        assertEquals(expected, result, "Conversion with non-existing currency should return the same amount");
    }

    @Test
    void testAPIConnection() {
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        ResponseEntity<Map> response = restTemplate.getForEntity("https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/USD", Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "The API connection should be OK");
    }

    @org.springframework.context.annotation.Configuration
    static class Config {
        @Bean
        public CurrencyConverter currencyConverter() {
            return new CurrencyConverter();
        }

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
}
