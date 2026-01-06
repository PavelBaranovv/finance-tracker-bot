package financetracker.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PurchaseTest {

    @Mock
    private Currency mockCurrency;

    @Mock
    private User mockUser;

    @Mock
    private ExchangeRate mockExchangeRate;

    private Purchase purchase;

    @BeforeEach
    void setUp() {
        purchase = new Purchase();
        purchase.setId(1L);
        purchase.setName("Laptop");
        purchase.setPrice(new BigDecimal("99999.99"));
        purchase.setAmount(1L);
        purchase.setCurrency(mockCurrency);
        purchase.setUser(mockUser);
        purchase.setExchangeRate(mockExchangeRate);
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, purchase.getId());
        assertEquals("Laptop", purchase.getName());
        assertEquals(new BigDecimal("99999.99"), purchase.getPrice());
        assertEquals(1L, purchase.getAmount());
        assertEquals(mockCurrency, purchase.getCurrency());
        assertEquals(mockUser, purchase.getUser());
        assertEquals(mockExchangeRate, purchase.getExchangeRate());
    }

    @Test
    void testEquals() {
        Purchase purchase1 = new Purchase();
        purchase1.setId(1L);

        Purchase purchase2 = new Purchase();
        purchase2.setId(1L);

        assertTrue(purchase1.equals(purchase2));
        assertEquals(purchase1.hashCode(), purchase2.hashCode());
    }

    @Test
    void testAllArgsConstructor() {
        Purchase fullPurchase = new Purchase(
                1L,
                "Smartphone",
                new BigDecimal("49999.99"),
                1L,
                mockCurrency,
                mockUser,
                mockExchangeRate
        );

        assertEquals(1L, fullPurchase.getId());
        assertEquals("Smartphone", fullPurchase.getName());
        assertEquals(new BigDecimal("49999.99"), fullPurchase.getPrice());
        assertEquals(1L, fullPurchase.getAmount());
    }

    @Test
    void testNoArgsConstructor() {
        Purchase emptyPurchase = new Purchase();
        assertNotNull(emptyPurchase);
        assertNull(emptyPurchase.getName());
        assertNull(emptyPurchase.getPrice());
        assertNull(emptyPurchase.getAmount());
        assertNull(emptyPurchase.getCurrency());
        assertNull(emptyPurchase.getUser());
        assertNull(emptyPurchase.getExchangeRate());
    }

    @Test
    void testTotalPriceCalculation() {
        purchase.setPrice(new BigDecimal("100.00"));
        purchase.setAmount(3L);

        BigDecimal total = purchase.getPrice().multiply(BigDecimal.valueOf(purchase.getAmount()));
        assertEquals(new BigDecimal("300.00"), total);
    }

    @Test
    void testNameLengthConstraints() {
        // Проверяем, что длинные имена обрабатываются
        String longName = "A".repeat(100);
        purchase.setName(longName);
        assertEquals(longName, purchase.getName());

        // Проверяем граничное значение
        String boundaryName = "B".repeat(100);
        purchase.setName(boundaryName);
        assertEquals(boundaryName, purchase.getName());
    }
}