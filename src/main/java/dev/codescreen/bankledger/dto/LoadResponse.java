package dev.codescreen.bankledger.dto;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"messageId", "userId", "balance"}) // To maintain ResponseBody Order
public class LoadResponse {

    private String userId;
    private String messageId;
    private Amount balance;

    // No-argument constructor for frameworks
    public LoadResponse() {
    }

    // All-argument constructor for manual instantiation
    public LoadResponse(String messageId, String userId, Amount balance) {
        this.messageId = messageId;
        this.userId = userId;
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

    // Getter for balance
    public Amount getBalance() {
        return balance;
    }

    // Setter for balance
    public void setBalance(Amount balance) {
        this.balance = balance;
    }

}
