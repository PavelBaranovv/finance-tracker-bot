package financetracker.service.statistic;

import financetracker.entity.Currency;
import financetracker.entity.CurrencyRate;
import financetracker.entity.Purchase;
import financetracker.entity.User;
import financetracker.repository.CurrencyRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticCalculator {

    private final CurrencyRateRepository currencyRateRepository;

    public Map<Currency, BigDecimal> aggregateByCurrency(List<Purchase> purchases) {
        Map<Currency, BigDecimal> result = new LinkedHashMap<>();

        for (Purchase purchase : purchases) {
            if (purchase.getCurrency() == null) continue;

            BigDecimal amount = purchase.getPrice()
                    .multiply(BigDecimal.valueOf(purchase.getAmount()));

            result.merge(purchase.getCurrency(), amount, BigDecimal::add);
        }

        return result;
    }

    public BigDecimal calculateTotalInUserCurrency(List<Purchase> purchases, User user) {
        return purchases.stream()
                .map(p -> convertToUserCurrency(p, user))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal convertToUserCurrency(Purchase purchase, User user) {
        if (purchase.getCurrency() == null || user.getCurrency() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal baseAmount = purchase.getPrice()
                .multiply(BigDecimal.valueOf(purchase.getAmount()));

        if (purchase.getCurrency().equals(user.getCurrency())) {
            return baseAmount;
        }

        var exchangeRate = purchase.getExchangeRate();
        if (exchangeRate == null) {
            return BigDecimal.ZERO;
        }

        CurrencyRate fromRate = currencyRateRepository
                .findByExchangeRateAndCurrency(exchangeRate, purchase.getCurrency())
                .orElse(null);

        CurrencyRate userRate = currencyRateRepository
                .findByExchangeRateAndCurrency(exchangeRate, user.getCurrency())
                .orElse(null);

        if (fromRate == null || userRate == null) {
            log.warn("Missing currency rate for conversion");
            return BigDecimal.ZERO;
        }

        BigDecimal amountInRub = baseAmount
                .multiply(fromRate.getValue())
                .divide(fromRate.getNominal(), 8, RoundingMode.HALF_UP);

        BigDecimal rubPerUnitUser = userRate.getValue()
                .divide(userRate.getNominal(), 8, RoundingMode.HALF_UP);

        return amountInRub
                .divide(rubPerUnitUser, 8, RoundingMode.HALF_UP);
    }
}


