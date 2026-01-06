package financetracker.integration;

import financetracker.entity.Currency;
import financetracker.entity.Purchase;
import financetracker.entity.User;
import financetracker.repository.CurrencyRepository;
import financetracker.repository.PurchaseRepository;
import financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
})
class SimpleRepositoryIntegrationTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    private Currency rubCurrency;
    private Currency usdCurrency;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Очистка
        purchaseRepository.deleteAll();
        currencyRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем валюты
        rubCurrency = currencyRepository.save(
                Currency.builder()
                        .code("RUB")
                        .name("Российский рубль")
                        .symbol("₽")
                        .build()
        );

        usdCurrency = currencyRepository.save(
                Currency.builder()
                        .code("USD")
                        .name("Доллар США")
                        .symbol("$")
                        .build()
        );

        // Создаем пользователя
        testUser = userRepository.save(
                User.builder()
                        .username("test_user")
                        .currency(rubCurrency)
                        .build()
        );
    }

    @Test
    void testUserStory_AddAndViewPurchases() {
        // 1. Создаем покупки
        Purchase purchase1 = Purchase.builder()
                .name("Кофе")
                .price(BigDecimal.valueOf(29.99))
                .amount(2L)
                .currency(usdCurrency)
                .user(testUser)
                .build();

        Purchase purchase2 = Purchase.builder()
                .name("Обед")
                .price(BigDecimal.valueOf(150))
                .amount(1L)
                .currency(rubCurrency)
                .user(testUser)
                .build();

        purchaseRepository.saveAll(List.of(purchase1, purchase2));

        // 2. Проверяем сохранение
        assertThat(purchaseRepository.count()).isEqualTo(2);

        // 3. Получаем последние покупки
        List<Purchase> recent = purchaseRepository.findTop10ByUserOrderByIdDescWithCurrency(testUser);
        assertThat(recent).hasSize(2);

        // 4. Проверяем данные
        assertThat(recent.get(0).getName()).isEqualTo("Обед");
        assertThat(recent.get(0).getCurrency().getCode()).isEqualTo("RUB");

        assertThat(recent.get(1).getName()).isEqualTo("Кофе");
        assertThat(recent.get(1).getCurrency().getCode()).isEqualTo("USD");

        // 5. Проверяем агрегацию
        BigDecimal totalRub = BigDecimal.ZERO;
        BigDecimal totalUsd = BigDecimal.ZERO;

        for (Purchase p : recent) {
            BigDecimal total = p.getPrice().multiply(BigDecimal.valueOf(p.getAmount()));
            if ("RUB".equals(p.getCurrency().getCode())) {
                totalRub = totalRub.add(total);
            } else {
                totalUsd = totalUsd.add(total);
            }
        }

        assertThat(totalRub).isEqualByComparingTo("150");
        assertThat(totalUsd).isEqualByComparingTo("59.98");
    }

    @Test
    void testFindByUsername() {
        // Поиск пользователя по имени
        var foundUser = userRepository.findByUsername("test_user");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getCurrency().getCode()).isEqualTo("RUB");
    }

    @Test
    void testCurrencyOperations() {
        // Поиск валюты по коду
        var foundRub = currencyRepository.findByCode("RUB");
        var foundUsd = currencyRepository.findByCode("USD");
        var foundEur = currencyRepository.findByCode("EUR");

        assertThat(foundRub).isPresent();
        assertThat(foundUsd).isPresent();
        assertThat(foundEur).isEmpty();
    }
}