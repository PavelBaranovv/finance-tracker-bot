package financetracker.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateTest {

    @Mock
    private CurrencyRate mockCurrencyRate;

    @Mock
    private Purchase mockPurchase;

    private ExchangeRate exchangeRate;

    @BeforeEach
    void setUp() {
        exchangeRate = new ExchangeRate();
        exchangeRate.setId(1L);
        exchangeRate.setDate(LocalDate.of(2024, 1, 15));

        List<CurrencyRate> currencyRates = new ArrayList<>();
        currencyRates.add(mockCurrencyRate);
        exchangeRate.setCurrencyRates(currencyRates);

        List<Purchase> purchases = new ArrayList<>();
        purchases.add(mockPurchase);
        exchangeRate.setPurchases(purchases);
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, exchangeRate.getId());
        assertEquals(LocalDate.of(2024, 1, 15), exchangeRate.getDate());
        assertNotNull(exchangeRate.getCurrencyRates());
        assertEquals(1, exchangeRate.getCurrencyRates().size());
        assertNotNull(exchangeRate.getPurchases());
        assertEquals(1, exchangeRate.getPurchases().size());
    }

    @Test
    void testAllArgsConstructor() {
        List<CurrencyRate> currencyRates = new ArrayList<>();
        List<Purchase> purchases = new ArrayList<>();

        ExchangeRate rate = new ExchangeRate(
                1L,
                LocalDate.of(2024, 1, 20),
                currencyRates,
                purchases
        );

        assertEquals(1L, rate.getId());
        assertEquals(LocalDate.of(2024, 1, 20), rate.getDate());
        assertEquals(currencyRates, rate.getCurrencyRates());
        assertEquals(purchases, rate.getPurchases());
    }

    @Test
    void testNoArgsConstructor() {
        ExchangeRate emptyRate = new ExchangeRate();
        assertNotNull(emptyRate);
        assertNull(emptyRate.getDate());
        assertNull(emptyRate.getCurrencyRates());
        assertNull(emptyRate.getPurchases());
    }

    @Test
    void testDateOperations() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        exchangeRate.setDate(yesterday);

        assertEquals(yesterday, exchangeRate.getDate());
        assertTrue(exchangeRate.getDate().isBefore(LocalDate.now()));
    }

    @Test
    void testListsManagement() {
        // Проверка добавления в списки
        List<CurrencyRate> currencyRates = new ArrayList<>();
        exchangeRate.setCurrencyRates(currencyRates);
        assertEquals(0, exchangeRate.getCurrencyRates().size());

        // Проверка null безопасность
        exchangeRate.setCurrencyRates(null);
        assertNull(exchangeRate.getCurrencyRates());
    }
}