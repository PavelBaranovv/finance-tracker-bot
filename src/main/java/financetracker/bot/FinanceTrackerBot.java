package financetracker.bot;

import financetracker.constant.Message;
import financetracker.service.AddPurchaseService;
import financetracker.service.StatisticService;
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
    private final StatisticService statisticService;

    public FinanceTrackerBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            AddPurchaseService addPurchaseService,
            StatisticService statisticService
    ) {
        super(botToken);
        this.botUsername = botUsername;
        this.addPurchaseService = addPurchaseService;
        this.statisticService = statisticService;
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

        handleTextMessage(update);
    }

    private void handleTextMessage(Update update) {
        String text = update.getMessage().getText();
        Long userId = update.getMessage().getFrom().getId();
        String chatId = update.getMessage().getChatId().toString();
        String username = update.getMessage().getFrom().getUserName();

        if (handleCommandIfAny(text, chatId, update)) {
            return;
        }

        addPurchaseService.handleOngoingDialogs(this, userId, chatId, text, username);
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (data != null && data.startsWith(financetracker.constant.Callback.CURRENCY_PREFIX)) {
            addPurchaseService.handleCallback(this, callbackQuery);
        } else if (data != null && (data.startsWith("STAT_") || data.startsWith(financetracker.constant.Callback.VIEW_IN_OTHER_CURRENCY_PREFIX) || data.startsWith(financetracker.constant.Callback.CHANGE_PURCHASE_CURRENCY_PREFIX))) {
            statisticService.handleCallback(this, callbackQuery);
        }
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

        if ("/view_statistic".equals(text)) {
            statisticService.startViewStatistic(this, update, chatId);
            return true;
        }

        if ("/view_recent".equals(text)) {
            statisticService.viewRecentPurchases(this, update, chatId);
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

