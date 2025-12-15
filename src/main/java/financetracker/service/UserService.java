package financetracker.service;

import financetracker.entity.Currency;
import financetracker.entity.User;
import financetracker.repository.CurrencyRepository;
import financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;

    public User resolveOrCreateUser(Long telegramUserId, String rawUsername) {
        String effectiveUsername = (rawUsername == null || rawUsername.isBlank())
                ? "tg_" + telegramUserId
                : rawUsername;

        Currency rubCurrency = currencyRepository.findByCode("RUB")
                .orElseThrow(() -> new IllegalStateException("Currency RUB not found"));

        User user = userRepository
                .findByUsername(effectiveUsername)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(effectiveUsername)
                        .currency(rubCurrency)
                        .build()));

        if (user.getCurrency() == null) {
            user.setCurrency(rubCurrency);
            userRepository.save(user);
        }

        return user;
    }
}


