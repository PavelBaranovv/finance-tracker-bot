package financetracker.repository;

import financetracker.entity.Currency;
import financetracker.entity.CurrencyRate;
import financetracker.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    Optional<CurrencyRate> findByExchangeRateAndCurrency(ExchangeRate exchangeRate, Currency currency);
}


