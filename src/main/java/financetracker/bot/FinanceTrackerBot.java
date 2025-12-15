package financetracker.bot;

import financetracker.constant.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class FinanceTrackerBot extends TelegramLongPollingBot {

    private final String botUsername;

    public FinanceTrackerBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername
    ) {
        super(botToken);
        this.botUsername = botUsername;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() == null || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText();

        if ("/start".equals(text)) {
            String chatId = update.getMessage().getChatId().toString();
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(Message.welcome);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}


