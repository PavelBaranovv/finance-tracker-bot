package financetracker.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CurrencyRateTest {

    @Mock
    private Currency mockCurrency;

    @Mock
    private ExchangeRate mockExchangeRate;

    private CurrencyRate currencyRate;

    @BeforeEach
    void setUp() {
        currencyRate = new CurrencyRate();
        currencyRate.setId(1L);
        currencyRate.setCurrency(mockCurrency);
        currencyRate.setNominal(new BigDecimal("1"));
        currencyRate.setValue(new BigDecimal("75.50"));
        currencyRate.setExchangeRate(mockExchangeRate);
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, currencyRate.getId());
        assertEquals(mockCurrency, currencyRate.getCurrency());
        assertEquals(new BigDecimal("1"), currencyRate.getNominal());
        assertEquals(new BigDecimal("75.50"), currencyRate.getValue());
        assertEquals(mockExchangeRate, currencyRate.getExchangeRate());
    }

    @Test
    void testEquals() {
        CurrencyRate rate1 = new CurrencyRate();
        rate1.setId(1L);

        CurrencyRate rate2 = new CurrencyRate();
        rate2.setId(1L);

        assertTrue(rate1.equals(rate2));
        assertEquals(rate1.hashCode(), rate2.hashCode());
    }

    @Test
    void testAllArgsConstructor() {
        CurrencyRate rate = new CurrencyRate(
                1L,
                mockCurrency,
                new BigDecimal("100"),
                new BigDecimal("85.30"),
                mockExchangeRate
        );

        assertEquals(1L, rate.getId());
        assertEquals(new BigDecimal("100"), rate.getNominal());
        assertEquals(new BigDecimal("85.30"), rate.getValue());
    }

    @Test
    void testNoArgsConstructor() {
        CurrencyRate emptyRate = new CurrencyRate();
        assertNotNull(emptyRate);
        assertNull(emptyRate.getCurrency());
        assertNull(emptyRate.getNominal());
        assertNull(emptyRate.getValue());
        assertNull(emptyRate.getExchangeRate());
    }

    @Test
    void testBigDecimalOperations() {
        BigDecimal nominal = new BigDecimal("10");
        BigDecimal value = new BigDecimal("750.25");

        currencyRate.setNominal(nominal);
        currencyRate.setValue(value);

        assertEquals(nominal, currencyRate.getNominal());
        assertEquals(value, currencyRate.getValue());

        // Проверка вычислений
        BigDecimal rateForOne = value.divide(nominal);
        assertEquals(new BigDecimal("75.025"), rateForOne);
    }
}