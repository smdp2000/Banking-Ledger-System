package dev.codescreen.bankledger.event;
import dev.codescreen.bankledger.enums.DebitCredit;
import dev.codescreen.bankledger.enums.ResponseCode;


import java.math.BigDecimal;

public class AuthorizationEvent extends Event{

    private ResponseCode responseCode;

    public AuthorizationEvent(String userId, BigDecimal amount, String currency, ResponseCode responseCode, DebitCredit type) {
        super(userId, amount, currency, DebitCredit.DEBIT);
        this.responseCode = responseCode;
    }

//    @Override
//    public BigDecimal getEffectOnBalance(String baseCurrency) {
//        // If the authorization is approved, it has a negative effect on the balance; otherwise, it has no effect.
//        return responseCode == ResponseCode.APPROVED ? getAmount().negate() : BigDecimal.ZERO;
//    }



    public ResponseCode getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(ResponseCode responseCode){
        this.responseCode = responseCode;

    }
}
