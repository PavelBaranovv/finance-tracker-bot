package financetracker.bot;

import financetracker.constant.Message;
import financetracker.service.AddPurchaseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class FinanceTrackerBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final AddPurchaseService addPurchaseService;

    public FinanceTrackerBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            AddPurchaseService addPurchaseService
    ) {
        super(botToken);
        this.botUsername = botUsername;
        this.addPurchaseService = addPurchaseService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
            return;
        }

        if (update.getMessage() == null || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText();
        Long userId = update.getMessage().getFrom().getId();
        String chatId = update.getMessage().getChatId().toString();

        if (handleCommandIfAny(text, chatId, update)) {
            return;
        }

        addPurchaseService.handleOngoingDialogs(this, userId, chatId, text);
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        addPurchaseService.handleCallback(this, callbackQuery);
    }

    private boolean handleCommandIfAny(String text, String chatId, Update update) {
        if ("/start".equals(text)) {
            sendText(chatId, Message.welcome);
            return true;
        }

        if ("/add_purchase".equals(text)) {
            addPurchaseService.startAddPurchase(this, update, chatId);
            return true;
        }

        return false;
    }

    public void sendText(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        sendMessage(message);
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void editMessage(EditMessageText edit) {
        try {
            execute(edit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

