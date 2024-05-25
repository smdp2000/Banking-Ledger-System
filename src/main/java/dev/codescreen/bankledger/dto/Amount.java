package dev.codescreen.bankledger.dto;

import dev.codescreen.bankledger.enums.DebitCredit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public class Amount {

    @Pattern(regexp = "^[0-9]*\\.?[0-9]+$", message = "Amount must be a positive number or zero")
    private String amount;
    @NotNull(message = "Currency must be specified")
    private String currency;
    @NotNull(message = "Debit or Credit type is required")
    private DebitCredit debitOrCredit;

    // Default constructor for serialization/deserialization
    public Amount() {
    }

    // Parameterized constructor for manual instantiation
    public Amount(String amount, String currency, DebitCredit debitOrCredit) {
        this.amount = amount;
        this.currency = currency;
        this.debitOrCredit = debitOrCredit;
    }

    // Getter for amount
    public String getAmount() {
        return amount;
    }

    // Setter for amount
    public void setAmount(String amount) {
        this.amount = amount;
    }

    // Getter for currency
    public String getCurrency() {
        return currency;
    }

    // Setter for currency
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    // Getter for debitOrCredit
    public DebitCredit getDebitOrCredit() {
        return debitOrCredit;
    }

    // Setter for debitOrCredit
    public void setDebitOrCredit(DebitCredit debitOrCredit) {
        this.debitOrCredit = debitOrCredit;
    }
}
