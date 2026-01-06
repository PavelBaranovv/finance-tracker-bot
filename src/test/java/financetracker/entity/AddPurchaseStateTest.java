package financetracker.entity;

import financetracker.enums.AddPurchaseStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddPurchaseStateTest {

    @Mock
    private User mockUser;

    @Mock
    private Currency mockCurrency;

    private AddPurchaseState addPurchaseState;

    @BeforeEach
    void setUp() {
        addPurchaseState = new AddPurchaseState();
        addPurchaseState.setId(1L);
        addPurchaseState.setStep(AddPurchaseStep.WAITING_FOR_NAME);
        addPurchaseState.setCurrency(mockCurrency);
        addPurchaseState.setPrice(new BigDecimal("100.50"));
        addPurchaseState.setAmount(2L);
        addPurchaseState.setName("Test Purchase");
        addPurchaseState.setUser(mockUser);
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, addPurchaseState.getId());
        assertEquals(AddPurchaseStep.WAITING_FOR_NAME, addPurchaseState.getStep());
        assertEquals(mockCurrency, addPurchaseState.getCurrency());
        assertEquals(new BigDecimal("100.50"), addPurchaseState.getPrice());
        assertEquals(2L, addPurchaseState.getAmount());
        assertEquals("Test Purchase", addPurchaseState.getName());
        assertEquals(mockUser, addPurchaseState.getUser());
    }

    @Test
    void testEquals_SameId() {
        AddPurchaseState state1 = new AddPurchaseState();
        state1.setId(1L);

        AddPurchaseState state2 = new AddPurchaseState();
        state2.setId(1L);

        assertTrue(state1.equals(state2));
        assertEquals(state1.hashCode(), state2.hashCode());
    }

    @Test
    void testEquals_DifferentId() {
        AddPurchaseState state1 = new AddPurchaseState();
        state1.setId(1L);

        AddPurchaseState state2 = new AddPurchaseState();
        state2.setId(2L);

        assertFalse(state1.equals(state2));
    }

    @Test
    void testEquals_Null() {
        assertFalse(addPurchaseState.equals(null));
    }

    @Test
    void testEquals_SameObject() {
        assertTrue(addPurchaseState.equals(addPurchaseState));
    }

    @Test
    void testEquals_DifferentClass() {
        assertFalse(addPurchaseState.equals(new Object()));
    }

    @Test
    void testNoArgsConstructor() {
        AddPurchaseState emptyState = new AddPurchaseState();
        assertNotNull(emptyState);
        assertNull(emptyState.getStep());
        assertNull(emptyState.getCurrency());
        assertNull(emptyState.getPrice());
        assertNull(emptyState.getAmount());
        assertNull(emptyState.getName());
        assertNull(emptyState.getUser());
    }

    @Test
    void testAllArgsConstructor() {
        AddPurchaseState state = new AddPurchaseState(
                1L,
                AddPurchaseStep.WAITING_FOR_PRICE,
                mockCurrency,
                new BigDecimal("200.00"),
                3L,
                "Another Purchase",
                mockUser
        );

        assertEquals(1L, state.getId());
        assertEquals(AddPurchaseStep.WAITING_FOR_PRICE, state.getStep());
        assertEquals(new BigDecimal("200.00"), state.getPrice());
        assertEquals(3L, state.getAmount());
        assertEquals("Another Purchase", state.getName());
    }

    @Test
    void testToString() {
        String result = addPurchaseState.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testDecimalPrecision() {
        BigDecimal bigDecimal = new BigDecimal("9999999999999999999.99");
        addPurchaseState.setPrice(bigDecimal);
        assertEquals(bigDecimal, addPurchaseState.getPrice());
    }
}