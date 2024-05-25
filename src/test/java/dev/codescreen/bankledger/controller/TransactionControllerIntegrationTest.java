package dev.codescreen.bankledger.controller;

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

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc

public class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurrencyConverter currencyConverter;  // Declare the mock bean


    @Test
    public void testLoadEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user1\", \"messageId\": \"message1\", \"transactionAmount\": {\"amount\": \"200\", \"currency\": \"USD\", \"debitOrCredit\": \"CREDIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("200.00"));
    }

    @Test
    public void testAuthorizationEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\": \"user2\", \"messageId\": \"message2\", \"transactionAmount\": {\"amount\": \"100\", \"currency\": \"USD\", \"debitOrCredit\": \"CREDIT\"}}"));

        mockMvc.perform(MockMvcRequestBuilders.put("/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user2\", \"messageId\": \"message3\", \"transactionAmount\": {\"amount\": \"50\", \"currency\": \"USD\", \"debitOrCredit\": \"DEBIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("50.00"));
    }

    @Test
    public void testLoadAndAuthorizeApproved() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user3\", \"messageId\": \"message4\", \"transactionAmount\": {\"amount\": \"300\", \"currency\": \"USD\", \"debitOrCredit\": \"CREDIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.put("/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user3\", \"messageId\": \"message5\", \"transactionAmount\": {\"amount\": \"100\", \"currency\": \"USD\", \"debitOrCredit\": \"DEBIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("200.00"));
    }

    @Test
    public void testLoadAndAuthorizeDeclined() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user4\", \"messageId\": \"message6\", \"transactionAmount\": {\"amount\": \"150\", \"currency\": \"USD\", \"debitOrCredit\": \"CREDIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.put("/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user4\", \"messageId\": \"message7\", \"transactionAmount\": {\"amount\": \"200\", \"currency\": \"USD\", \"debitOrCredit\": \"DEBIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("150.00")); // Balance remains unchanged due to decline
    }

    @Test
    public void testLoadAuthorizeLoad() throws Exception {
        // First load operation
        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user8\", \"messageId\": \"message8\", \"transactionAmount\": {\"amount\": \"500\", \"currency\": \"USD\", \"debitOrCredit\": \"CREDIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("500.00"));

        // Authorization that deducts from the balance
        mockMvc.perform(MockMvcRequestBuilders.put("/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user8\", \"messageId\": \"message9\", \"transactionAmount\": {\"amount\": \"200\", \"currency\": \"USD\", \"debitOrCredit\": \"DEBIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("300.00"));

        // Second load operation after the authorization
        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user8\", \"messageId\": \"message10\", \"transactionAmount\": {\"amount\": \"150\", \"currency\": \"USD\", \"debitOrCredit\": \"CREDIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("450.00")); // Expecting new balance to be 300 (previous balance) + 150 (new load)
    }

    @Test
    public void testLoadAuthorizeDifferentUsersDifferentBalances() throws Exception {
        // Load operation for user5
        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user5\", \"messageId\": \"message11\", \"transactionAmount\": {\"amount\": \"300\", \"currency\": \"USD\", \"debitOrCredit\": \"CREDIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("300.00"));

        // Load operation for user6
        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user6\", \"messageId\": \"message12\", \"transactionAmount\": {\"amount\": \"400\", \"currency\": \"USD\", \"debitOrCredit\": \"CREDIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("400.00"));

        // Authorization for user5 deducting from their balance
        mockMvc.perform(MockMvcRequestBuilders.put("/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user5\", \"messageId\": \"message13\", \"transactionAmount\": {\"amount\": \"100\", \"currency\": \"USD\", \"debitOrCredit\": \"DEBIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("200.00")); // User5's balance should now be 200

        // Authorization for user6 deducting a different amount from their balance
        mockMvc.perform(MockMvcRequestBuilders.put("/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user6\", \"messageId\": \"message14\", \"transactionAmount\": {\"amount\": \"150\", \"currency\": \"USD\", \"debitOrCredit\": \"DEBIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("250.00")); // User6's balance should now be 250
    }

    @Test
    public void testLoadWithNegativeAmount() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user1\", \"messageId\": \"message17\", \"transactionAmount\": {\"amount\": \"-100\", \"currency\": \"USD\", \"debitOrCredit\": \"CREDIT\"}}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testLoadWithMissingFields() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user1\", \"messageId\": \"message19\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }



}