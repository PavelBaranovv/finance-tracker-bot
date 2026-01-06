package financetracker.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CurrencyTest {

    private Currency currency;

    @BeforeEach
    void setUp() {
        currency = new Currency();
        currency.setId(1L);
        currency.setCode("USD");
        currency.setName("US Dollar");
        currency.setSymbol("$");
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, currency.getId());
        assertEquals("USD", currency.getCode());
        assertEquals("US Dollar", currency.getName());
        assertEquals("$", currency.getSymbol());
    }

    @Test
    void testEquals_SameId() {
        Currency currency1 = new Currency();
        currency1.setId(1L);

        Currency currency2 = new Currency();
        currency2.setId(1L);

        assertTrue(currency1.equals(currency2));
        assertEquals(currency1.hashCode(), currency2.hashCode());
    }

    @Test
    void testEquals_DifferentId() {
        Currency currency1 = new Currency();
        currency1.setId(1L);

        Currency currency2 = new Currency();
        currency2.setId(2L);

        assertFalse(currency1.equals(currency2));
    }

    @Test
    void testAllArgsConstructor() {
        Currency fullCurrency = new Currency(1L, "EUR", "Euro", "€");

        assertEquals(1L, fullCurrency.getId());
        assertEquals("EUR", fullCurrency.getCode());
        assertEquals("Euro", fullCurrency.getName());
        assertEquals("€", fullCurrency.getSymbol());
    }

    @Test
    void testNoArgsConstructor() {
        Currency emptyCurrency = new Currency();
        assertNotNull(emptyCurrency);
        assertNull(emptyCurrency.getCode());
        assertNull(emptyCurrency.getName());
        assertNull(emptyCurrency.getSymbol());
    }


}