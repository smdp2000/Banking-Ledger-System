package dev.codescreen.bankledger.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dev.codescreen.bankledger.enums.ResponseCode;

@JsonPropertyOrder({"messageId", "userId", "responseCode", "balance"}) // To maintain ResponseBody Order
public class AuthorizationResponse {
    private String userId;
    private String messageId;
    private ResponseCode responseCode;
    private Amount balance;

    // No-argument constructor for frameworks
    public AuthorizationResponse() {
    }

    // All-argument constructor for manual instantiation
    public AuthorizationResponse(String userId, String messageId, ResponseCode responseCode, Amount balance) {
        this.messageId = messageId;
        this.userId = userId;
        this.responseCode = responseCode;
        this.balance = balance;
    }

    // Getter for userId
    public String getUserId() {
        return userId;
    }

    // Setter for userId
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getter for messageId
    public String getMessageId() {
        return messageId;
    }

    // Setter for messageId
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    // Getter for responseCode
    public ResponseCode getResponseCode() {
        return responseCode;
    }

    // Setter for responseCode
    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    // Getter for balance
    public Amount getBalance() {
        return balance;
    }

    // Setter for balance
    public void setBalance(Amount balance) {
        this.balance = balance;
    }
}
