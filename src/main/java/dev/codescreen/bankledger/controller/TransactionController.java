package dev.codescreen.bankledger.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import dev.codescreen.bankledger.dto.*;
import dev.codescreen.bankledger.dto.Error;
import dev.codescreen.bankledger.enums.DebitCredit;
import dev.codescreen.bankledger.enums.ResponseCode;
import dev.codescreen.bankledger.event.AuthorizationEvent;
import dev.codescreen.bankledger.event.Event;
import dev.codescreen.bankledger.event.LoadEvent;
import dev.codescreen.bankledger.store.EventStore;
import dev.codescreen.bankledger.util.CurrencyConverter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController

public class TransactionController {

    private final EventStore eventStore;
    private final CurrencyConverter currencyConverter;
    private final String BASE_CURRENCY = "USD";

    @Autowired
    public TransactionController(EventStore eventStore, CurrencyConverter currencyConverter) {
        this.eventStore = eventStore;
        this.currencyConverter = currencyConverter;
    }


    @GetMapping("/ping")
    public ResponseEntity<?> pingServer() {
        try {
            Ping response = new Ping();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            Error error = new Error(e.getMessage(), String.valueOf(status.value()));
            return new ResponseEntity<>(error, status);
        }
    }

    @PutMapping("/load")
    public ResponseEntity<?> handleLoad(@Valid @RequestBody LoadRequest request) {
        if (request.getTransactionAmount().getDebitOrCredit() != DebitCredit.CREDIT) {
            return ResponseEntity.badRequest().body(new Error("Load transactions must be of type CREDIT", String.valueOf(HttpStatus.BAD_REQUEST.value())));
        }

        BigDecimal amount = new BigDecimal(request.getTransactionAmount().getAmount());
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.badRequest().body(new Error("Amount cannot be negative", String.valueOf(status.value())));
        }
        // add event to store
        eventStore.addEvent(request.getUserId(), new LoadEvent(request.getUserId(), amount, request.getTransactionAmount().getCurrency()));
        // update latest transaction
        updateSnapshot(request.getUserId(), request.getTransactionAmount().getCurrency());
        // calculate updated balance
        BigDecimal balance = calculateBalance(request.getUserId(), request.getTransactionAmount().getCurrency());

        Amount balanceAmount = new Amount(balance.toString(), request.getTransactionAmount().getCurrency(), DebitCredit.CREDIT);
        LoadResponse response = new LoadResponse(UUID.randomUUID().toString(),request.getUserId(), balanceAmount);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/authorization")
    public ResponseEntity<?> handleAuthorization(@Valid @RequestBody AuthorizationRequest request) {
        if (request.getTransactionAmount().getDebitOrCredit() != DebitCredit.DEBIT) {
            return ResponseEntity.badRequest().body(new Error("Authorization transactions must be of type DEBIT", String.valueOf(HttpStatus.BAD_REQUEST.value())));
        }

        BigDecimal amount = new BigDecimal(request.getTransactionAmount().getAmount());
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.badRequest().body(new Error("Amount cannot be negative", String.valueOf(status.value())));
        }

        BigDecimal balance = calculateBalance(request.getUserId(), request.getTransactionAmount().getCurrency());
        ResponseCode responseCode = balance.compareTo(amount) >= 0 ? ResponseCode.APPROVED : ResponseCode.DECLINED;

        // add event to store
        eventStore.addEvent(request.getUserId(), new AuthorizationEvent(request.getUserId(), amount, request.getTransactionAmount().getCurrency(),responseCode, DebitCredit.DEBIT));
        // update latest transaction
        updateSnapshot(request.getUserId(), request.getTransactionAmount().getCurrency());
        // calculate updated balance
        balance = calculateBalance(request.getUserId(), request.getTransactionAmount().getCurrency());
        Amount balanceAmount = new Amount(balance.toString(), request.getTransactionAmount().getCurrency(), DebitCredit.DEBIT);
        AuthorizationResponse response = new AuthorizationResponse(request.getUserId(), UUID.randomUUID().toString(), responseCode, balanceAmount);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/events/{userId}")
    public ResponseEntity<?> getEventsForUser(@PathVariable String userId) {
        List<Event> events = eventStore.getEventsOfUser(userId);
        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(events);
    }

    private BigDecimal calculateBalance(String userId, String responseCurrency) {
        BigDecimal lastSnapshotBalance = eventStore.getLatestSnapshotBalance(userId); // get last snapshot
        BigDecimal balanceSinceSnapshot = eventStore.getEventsSinceLastSnapshot(userId).stream()
                .map(event -> {
                    BigDecimal eventEffect = BigDecimal.ZERO;
                    if (event instanceof LoadEvent) {
                        eventEffect = currencyConverter.convert(event.getCurrency(), BASE_CURRENCY, event.getAmount());
                    } else if (event instanceof AuthorizationEvent) {
                        // Only consider the amount for approved Authorization events
                        AuthorizationEvent authEvent = (AuthorizationEvent) event;
                        if (authEvent.getResponseCode() == ResponseCode.APPROVED) {
                            eventEffect = currencyConverter.convert(event.getCurrency(), BASE_CURRENCY, event.getAmount().negate());
                        }
                    }
                    return eventEffect;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add); // get balance since last snapshot
        BigDecimal currentBalanceInBase = lastSnapshotBalance.add(balanceSinceSnapshot);
        return currencyConverter.convert(BASE_CURRENCY, responseCurrency, currentBalanceInBase);
    }

    private void updateSnapshot(String userId, String currency) {
        BigDecimal currentBalance = currencyConverter.convert(currency, BASE_CURRENCY, calculateBalance(userId, currency));
        LocalDateTime now = LocalDateTime.now(); // Current date and time
        eventStore.updateSnapshot(userId, currentBalance, now);
    }


}
