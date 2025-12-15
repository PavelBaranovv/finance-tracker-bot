package financetracker.bot;

import financetracker.entity.Currency;
import financetracker.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AddPurchaseState {

    public enum Step {
        WAITING_FOR_CURRENCY,
        WAITING_FOR_PRICE,
        WAITING_FOR_AMOUNT,
        WAITING_FOR_NAME
    }

    private Step step;
    private Currency currency;
    private BigDecimal price;
    private Long amount;
    private String name;
    private User user;
}


