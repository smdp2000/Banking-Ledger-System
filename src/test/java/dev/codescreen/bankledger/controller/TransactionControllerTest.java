package dev.codescreen.bankledger.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.codescreen.bankledger.dto.AuthorizationRequest;
import dev.codescreen.bankledger.dto.LoadRequest;
import dev.codescreen.bankledger.dto.Amount;
import dev.codescreen.bankledger.enums.DebitCredit;
import dev.codescreen.bankledger.enums.ResponseCode;
import dev.codescreen.bankledger.event.AuthorizationEvent;
import dev.codescreen.bankledger.event.Event;
import dev.codescreen.bankledger.event.LoadEvent;
import dev.codescreen.bankledger.store.EventStore;
import dev.codescreen.bankledger.util.CurrencyConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventStore eventStore;

    @MockBean
    private CurrencyConverter currencyConverter;

    @BeforeEach
    void setUp() {
        when(currencyConverter.convert(anyString(), anyString(), any(BigDecimal.class))).thenReturn(BigDecimal.ONE);
        when(eventStore.getLatestSnapshotBalance(anyString())).thenReturn(BigDecimal.ZERO);
    }



    @Test
    public void testUnitHandleLoad() throws Exception { // since we are unit testing all other services needs to be mocked
        // Prepare the request object
        LoadRequest request = new LoadRequest("user1", "message1",new Amount("100", "USD", DebitCredit.CREDIT));
        when(eventStore.getLatestSnapshotBalance(anyString())).thenReturn(BigDecimal.ZERO);
        List<Event> mockEvents = Arrays.asList(
                new LoadEvent("user1", new BigDecimal("100.00"), "USD")
        );
        when(eventStore.getEventsSinceLastSnapshot("user1")).thenReturn(mockEvents);
        when(currencyConverter.convert(anyString(), anyString(), any(BigDecimal.class)))
                .thenAnswer(invocation -> invocation.getArgument(2, BigDecimal.class));

        mockMvc.perform(put("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance.amount").value("100.00"));

        // Verify that the correct methods were called on the mock objects
        verify(eventStore, atLeastOnce()).getEventsSinceLastSnapshot("user1");
        verify(currencyConverter, atLeastOnce()).convert(anyString(), anyString(), any(BigDecimal.class));
        verify(eventStore).addEvent(anyString(), any());
    }

    @Test
    public void testUnitHandleAuthorization() throws Exception { // since we are unit testing all other services needs to be mocked
        when(eventStore.getLatestSnapshotBalance("user1")).thenReturn(new BigDecimal("100.00"));
        //since we already it to store the new authorization request, it will be in events after last snapshot authorization
        List<Event> mockEvents = Arrays.asList(
                new AuthorizationEvent("user1", new BigDecimal("50"), "USD", ResponseCode.APPROVED, DebitCredit.DEBIT)
        );
        when(eventStore.getEventsSinceLastSnapshot("user1")).thenReturn(mockEvents);
        when(currencyConverter.convert(anyString(), anyString(), any(BigDecimal.class)))
                .thenAnswer(invocation -> invocation.getArgument(2, BigDecimal.class));

        // Prepare the request object
        AuthorizationRequest request = new AuthorizationRequest("user1", "message1", new Amount("50", "USD", DebitCredit.DEBIT));

        // Perform the authorization request and expect certain outcomes
        mockMvc.perform(put("/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance.amount").value("50.00")); // Check the resulting balance after the authorization

        // Verify that the correct methods were called on the mock objects
        verify(eventStore).addEvent(anyString(), any(AuthorizationEvent.class));
        verify(eventStore, atLeastOnce()).getEventsSinceLastSnapshot("user1");
        verify(currencyConverter, atLeastOnce()).convert(anyString(), anyString(), any(BigDecimal.class));// Called twice if used in updateSnapshot as well
    }


    // Helper to convert objects to JSON string for request bodies
    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON string", e);
        }
    }
}
