package dev.codescreen.bankledger.event;
import dev.codescreen.bankledger.enums.DebitCredit;

import java.math.BigDecimal;

public class LoadEvent extends Event{

    public LoadEvent(String userId, BigDecimal amount, String currency) {
        super(userId, amount, currency, DebitCredit.CREDIT);
    }

//    @Override
//    public BigDecimal getEffectOnBalance() {
//        // LoadEvent has a positive effect on the balance
//        return getAmount();
//    }


}
