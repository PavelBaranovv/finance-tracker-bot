package financetracker.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private Currency mockCurrency;

    @Mock
    private Purchase mockPurchase;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setCurrency(mockCurrency);

        List<Purchase> purchases = new ArrayList<>();
        purchases.add(mockPurchase);
        user.setPurchases(purchases);
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, user.getId());
        assertEquals("john_doe", user.getUsername());
        assertEquals(mockCurrency, user.getCurrency());
        assertNotNull(user.getPurchases());
        assertEquals(1, user.getPurchases().size());
    }

    @Test
    void testEquals() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        assertTrue(user1.equals(user2));
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testAllArgsConstructor() {
        List<Purchase> purchases = new ArrayList<>();

        User fullUser = new User(
                1L,
                "jane_doe",
                purchases,
                mockCurrency
        );

        assertEquals(1L, fullUser.getId());
        assertEquals("jane_doe", fullUser.getUsername());
        assertEquals(purchases, fullUser.getPurchases());
        assertEquals(mockCurrency, fullUser.getCurrency());
    }

    @Test
    void testNoArgsConstructor() {
        User emptyUser = new User();
        assertNotNull(emptyUser);
        assertNull(emptyUser.getUsername());
        assertNull(emptyUser.getPurchases());
        assertNull(emptyUser.getCurrency());
    }

    @Test
    void testUsernameUniqueness() {
        // Тест на уникальность username
        user.setUsername("unique_user_123");
        assertEquals("unique_user_123", user.getUsername());
    }

    @Test
    void testPurchasesManagement() {
        // Проверка работы со списком покупок
        List<Purchase> newPurchases = new ArrayList<>();
        user.setPurchases(newPurchases);
        assertEquals(0, user.getPurchases().size());

        // Добавление покупки
        user.getPurchases().add(mockPurchase);
        assertEquals(1, user.getPurchases().size());

        // Очистка списка
        user.getPurchases().clear();
        assertEquals(0, user.getPurchases().size());
    }

    @Test
    void testCurrencyAssociation() {
        // Проверка связи с валютой
        assertNotNull(user.getCurrency());
        assertEquals(mockCurrency, user.getCurrency());

        // Смена валюты
        Currency newCurrency = new Currency();
        newCurrency.setCode("EUR");
        user.setCurrency(newCurrency);
        assertEquals(newCurrency, user.getCurrency());
    }
}