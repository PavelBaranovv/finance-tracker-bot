package financetracker.bot;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(FinanceTrackerBot financeTrackerBot) throws Exception {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(financeTrackerBot);
            return botsApi;
        } catch (TelegramApiRequestException e) {
            throw new RuntimeException("Не удалось инициализировать Telegram бота. Проверьте токен.", e);
        }
    }
}


