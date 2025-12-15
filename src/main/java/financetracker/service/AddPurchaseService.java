package financetracker.service;

import financetracker.bot.AddPurchaseState;
import financetracker.bot.FinanceTrackerBot;
import financetracker.constant.Message;
import financetracker.entity.Currency;
import financetracker.entity.Purchase;
import financetracker.entity.User;
import financetracker.repository.CurrencyRepository;
import financetracker.repository.ExchangeRateRepository;
import financetracker.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddPurchaseService {

    private final CurrencyRepository currencyRepository;
    private final PurchaseRepository purchaseRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final CbrRateService cbrRateService;
    private final UserService userService;

    private final Map<Long, AddPurchaseState> addPurchaseStates = new HashMap<>();

    public void startAddPurchase(FinanceTrackerBot bot, Update update, String chatId) {
         Long userId = update.getMessage().getFrom().getId();

         User user;
         try {
             user = userService.resolveOrCreateUser(
                     userId,
                     update.getMessage().getFrom().getUserName()
             );
         } catch (IllegalStateException e) {
             log.error("Failed to resolve or create user for telegramUserId={}. Reason: {}", userId, e.getMessage(), e);
             bot.sendText(chatId, Message.noRubCurrency);
             return;
         }

        List<Currency> currencies = currencyRepository.findAll();
        if (currencies.isEmpty()) {
            log.error("No currencies configured in DB, cannot start add purchase flow for userId={}", userId);
            bot.sendText(chatId, Message.noCurrencies);
            return;
        }

        AddPurchaseState state = new AddPurchaseState();
        state.setStep(AddPurchaseState.Step.WAITING_FOR_CURRENCY);
        state.setUser(user);
        addPurchaseStates.put(userId, state);

        InlineKeyboardMarkup markup = buildCurrencyKeyboard(currencies);
        SendMessage msg = new SendMessage(chatId, Message.chooseCurrency);
        msg.setReplyMarkup(markup);
        bot.sendMessage(msg);
    }

    public void handleCallback(FinanceTrackerBot bot, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long userId = callbackQuery.getFrom().getId();
        String chatId = callbackQuery.getMessage().getChatId().toString();

        AddPurchaseState state = addPurchaseStates.get(userId);
        if (state == null || state.getStep() != AddPurchaseState.Step.WAITING_FOR_CURRENCY) {
            return;
        }

        if (data != null && data.startsWith("CUR_")) {
            handleCurrencyChosen(bot, callbackQuery, state, chatId, data);
        }
    }

    public void handleOngoingDialogs(FinanceTrackerBot bot,
                                     Long userId,
                                     String chatId,
                                     String text) {
        AddPurchaseState state = addPurchaseStates.get(userId);
        if (state == null) {
            return;
        }

        switch (state.getStep()) {
            case WAITING_FOR_PRICE -> handlePriceStep(bot, chatId, state, text);
            case WAITING_FOR_AMOUNT -> handleAmountStep(bot, chatId, state, text);
            case WAITING_FOR_NAME -> handleNameStep(bot, chatId, userId, state, text);
            default -> {
            }
        }
    }

    private InlineKeyboardMarkup buildCurrencyKeyboard(List<Currency> currencies) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>(4);
        for (Currency currency : currencies) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(currency.getCode());
            button.setCallbackData("CUR_" + currency.getCode());
            currentRow.add(button);
            if (currentRow.size() == 4) {
                rows.add(currentRow);
                currentRow = new ArrayList<>(4);
            }
        }
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private void handleCurrencyChosen(FinanceTrackerBot bot,
                                      CallbackQuery callbackQuery,
                                      AddPurchaseState state,
                                      String chatId,
                                      String data) {
        String code = data.substring("CUR_".length());
        currencyRepository.findByCode(code).ifPresent(currency -> {
            state.setCurrency(currency);
            state.setStep(AddPurchaseState.Step.WAITING_FOR_PRICE);

            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId);
            edit.setMessageId(callbackQuery.getMessage().getMessageId());
            edit.setText(String.format(Message.currencySelected, currency.getName()));
            bot.editMessage(edit);

            bot.sendText(chatId, Message.askPrice);
        });
    }

    private void handlePriceStep(FinanceTrackerBot bot,
                                 String chatId,
                                 AddPurchaseState state,
                                 String text) {
        try {
            BigDecimal price = new BigDecimal(text.replace(",", ".")); // локаль проще не трогать
            state.setPrice(price);
            state.setStep(AddPurchaseState.Step.WAITING_FOR_AMOUNT);
            bot.sendText(chatId, Message.askAmount);
        } catch (NumberFormatException e) {
            bot.sendText(chatId, Message.priceParseError);
        }
    }

    private void handleAmountStep(FinanceTrackerBot bot,
                                  String chatId,
                                  AddPurchaseState state,
                                  String text) {
        try {
            Long amount = Long.parseLong(text);
            state.setAmount(amount);
            state.setStep(AddPurchaseState.Step.WAITING_FOR_NAME);
            bot.sendText(chatId, Message.askName);
        } catch (NumberFormatException e) {
            bot.sendText(chatId, Message.amountParseError);
        }
    }

    private void handleNameStep(FinanceTrackerBot bot,
                                String chatId,
                                Long userId,
                                AddPurchaseState state,
                                String text) {
        state.setName(text);

        LocalDate today = LocalDate.now();
        var exchangeRate = exchangeRateRepository.findByDate(today)
                .orElseGet(() -> cbrRateService.getForToday(currencyRepository.findAll()));

        Purchase purchase = Purchase.builder()
                .name(state.getName())
                .price(state.getPrice())
                .amount(state.getAmount())
                .currency(state.getCurrency())
                .user(state.getUser())
                .exchangeRate(exchangeRate)
                .build();
        purchaseRepository.save(purchase);

        addPurchaseStates.remove(userId);
        bot.sendText(chatId, Message.purchaseSaved);
    }
}


