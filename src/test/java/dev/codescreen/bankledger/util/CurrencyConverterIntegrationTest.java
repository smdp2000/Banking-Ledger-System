package dev.codescreen.bankledger.util;

import dev.codescreen.bankledger.util.CurrencyConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CurrencyConverterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyConverter currencyConverter;


    @Test
    public void testLoadInOneCurrencyAuthorizationInDifferentCurrency() throws Exception {
        when(currencyConverter.convert(anyString(), anyString(), any(BigDecimal.class))).thenAnswer(i -> {
            String sourceCurrency = i.getArgument(0);
            String targetCurrency = i.getArgument(1);
            BigDecimal amount = i.getArgument(2);
            if (sourceCurrency.equals("USD") && targetCurrency.equals("EUR"))
                return amount.multiply(BigDecimal.valueOf(0.9)).setScale(2, RoundingMode.HALF_EVEN); // Fixed rate for the test
            else if(sourceCurrency.equals("EUR") && targetCurrency.equals("USD"))
                return amount.divide(BigDecimal.valueOf(0.9), BigDecimal.ROUND_HALF_UP); // Convert and round
            return amount; // Default fallback
        });

        // Act: Load in USD
        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user7\", \"messageId\": \"message15\", \"transactionAmount\": {\"amount\": \"500\", \"currency\": \"USD\", \"debitOrCredit\": \"CREDIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        // Act & Assert: Authorize in EUR with mocked conversion rate
        mockMvc.perform(MockMvcRequestBuilders.put("/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user7\", \"messageId\": \"message16\", \"transactionAmount\": {\"amount\": \"90\", \"currency\": \"EUR\", \"debitOrCredit\": \"DEBIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("360.00")); // Expected after converting 100 EUR to 90 USD
    }
}
