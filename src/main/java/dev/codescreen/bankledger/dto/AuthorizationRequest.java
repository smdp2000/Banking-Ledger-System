package dev.codescreen.bankledger.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AuthorizationRequest {

    @NotBlank(message = "User ID cannot be empty")
    private String userId;
    @NotBlank(message = "Message ID cannot be empty")
    private String messageId;
    @NotNull(message = "Transaction amount is required")
    @Valid
    private Amount transactionAmount;

    // Default constructor for serialization/deserialization
    public AuthorizationRequest() {
    }

    // Parameterized constructor for manual instantiation
    public AuthorizationRequest(String userId, String messageId, Amount transactionAmount) {
        this.userId = userId;
        this.messageId = messageId;
        this.transactionAmount = transactionAmount;
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

    // Getter for transactionAmount
    public Amount getTransactionAmount() {
        return transactionAmount;
    }

    // Setter for transactionAmount
    public void setTransactionAmount(Amount transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
}
