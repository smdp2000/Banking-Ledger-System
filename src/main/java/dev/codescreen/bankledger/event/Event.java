package dev.codescreen.bankledger.event;
import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.codescreen.bankledger.enums.DebitCredit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
public abstract class Event {

    private final String userId;
    private final BigDecimal amount;
    private final String currency;
    private final LocalDateTime timestamp;
    private final DebitCredit type;

    public Event(String userId, BigDecimal amount, String currency, DebitCredit type) {
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.type = type;
        this.timestamp = LocalDateTime.now(); // Assume the event's timestamp is set at the time of its creation
    }

    // Abstract method to get the effect of the event on balance
//    @JsonIgnore //To avoid serializing effect on balance
//    public abstract BigDecimal getEffectOnBalance();

    // Getters
    public String getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getCurrency() {
        return currency;
    }

    public DebitCredit getType() {
        return type;
    }

}
