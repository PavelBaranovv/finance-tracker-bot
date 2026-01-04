package financetracker.service;

import financetracker.bot.FinanceTrackerBot;
import financetracker.constant.Callback;
import financetracker.constant.Message;
import financetracker.entity.Currency;
import financetracker.entity.Purchase;
import financetracker.entity.User;
import financetracker.repository.CurrencyRepository;
import financetracker.repository.PurchaseRepository;
import financetracker.service.statistic.StatisticCalculator;
import financetracker.service.statistic.StatisticPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticService {

    private final PurchaseRepository purchaseRepository;
    private final CurrencyRepository currencyRepository;
    private final UserService userService;
    private final StatisticCalculator calculator;

    public void startViewStatistic(FinanceTrackerBot bot, Update update, String chatId) {
        SendMessage msg = new SendMessage(chatId, Message.choosePeriod);
        msg.setReplyMarkup(buildPeriodKeyboard());
        bot.sendMessage(msg);
    }

    @Transactional(readOnly = true)
    public void viewRecentPurchases(FinanceTrackerBot bot, Update update, String chatId) {
        try {
            Long telegramUserId = update.getMessage().getFrom().getId();
            String username = update.getMessage().getFrom().getUserName();
            
            User user = userService.resolveOrCreateUser(telegramUserId, username);
            List<Purchase> purchases = purchaseRepository.findTop10ByUserOrderByIdDescWithCurrency(user);
            
            if (purchases.isEmpty()) {
                bot.sendText(chatId, Message.noRecentPurchases);
                return;
            }
            
            String message = buildRecentPurchasesText(purchases);
            SendMessage sendMessage = new SendMessage(chatId, message);
            sendMessage.setParseMode("HTML");
            bot.sendMessage(sendMessage);
        } catch (Exception e) {
            log.error("Error viewing recent purchases", e);
            bot.sendText(chatId, "Ошибка при получении списка покупок");
        }
    }

    private String buildRecentPurchasesText(List<Purchase> purchases) {
        StringBuilder sb = new StringBuilder(Message.recentPurchasesHeader).append("\n");
        
        for (Purchase purchase : purchases) {
            sb.append("<blockquote>");
            sb.append("<b>").append(escapeHtml(purchase.getName())).append("</b> ");
            sb.append(purchase.getAmount()).append(" шт.\n");
            
            BigDecimal roundedPrice = purchase.getPrice().setScale(2, RoundingMode.HALF_UP);
            String priceStr = roundedPrice.stripTrailingZeros().toPlainString();
            String currencySymbol = purchase.getCurrency().getSymbol() != null 
                    ? purchase.getCurrency().getSymbol() 
                    : purchase.getCurrency().getCode();
            
            sb.append(priceStr).append(" ").append(currencySymbol);
            sb.append("</blockquote>\n");
        }
        
        return sb.toString().trim();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }


    public void handleCallback(FinanceTrackerBot bot, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        
        if (data != null && data.startsWith(Callback.VIEW_IN_OTHER_CURRENCY_PREFIX)) {
            handleViewInOtherCurrency(bot, callbackQuery, data);
            return;
        }

        if (data != null && data.startsWith(Callback.CHANGE_PURCHASE_CURRENCY_PREFIX)) {
            handleChangeCurrency(bot, callbackQuery, data);
            return;
        }

        StatisticPeriod period = StatisticPeriod.fromCallback(data);
        if (period == null) {
            return;
        }

        try {
            User user = resolveUser(callbackQuery);
            String text = buildStatisticText(user, period);
            editMessage(bot, callbackQuery, text, period);
        } catch (Exception e) {
            log.error("Statistic error", e);
            editMessage(bot, callbackQuery, Message.statisticError, null);
        }
    }

    private User resolveUser(CallbackQuery callbackQuery) {
        return userService.resolveOrCreateUser(
                callbackQuery.getFrom().getId(),
                callbackQuery.getFrom().getUserName()
        );
    }

    private String buildStatisticText(User user, StatisticPeriod period) {
        LocalDate today = LocalDate.now();

        LocalDate from = switch (period) {
            case DAY -> today;
            case DAYS_7 -> today.minusDays(6);
            case MONTH -> today.withDayOfMonth(1);
            case YEAR -> today.withDayOfYear(1);
        };

        List<Purchase> purchases =
                purchaseRepository.findByUserAndExchangeRate_DateBetween(
                        user, from, today
                );

        if (purchases.isEmpty()) {
            return period.header() + "\n" + Message.statisticEmpty;
        }

        Map<Currency, BigDecimal> totals =
                calculator.aggregateByCurrency(purchases);

        BigDecimal totalInUserCurrency =
                calculator.calculateTotalInUserCurrency(purchases, user);

        StringBuilder sb = new StringBuilder(period.header()).append("\n");

        totals.forEach((currency, amount) ->
                sb.append(formatCurrencyLine(currency, amount)).append("\n")
        );

        appendTotal(sb, totalInUserCurrency, user);

        return sb.toString().trim();
    }

    private String formatCurrencyLine(Currency currency, BigDecimal amount) {
        BigDecimal rounded = amount.setScale(2, RoundingMode.HALF_UP);
        return rounded.stripTrailingZeros().toPlainString() + " " +
                (currency.getSymbol() != null ? currency.getSymbol() : currency.getCode());
    }

    private void appendTotal(StringBuilder sb, BigDecimal total, User user) {
        if (user.getCurrency() == null) return;

        BigDecimal rounded = total.setScale(2, RoundingMode.HALF_UP);
        sb.append("\n")
                .append(String.format(
                        Message.totalLine,
                        rounded.stripTrailingZeros().toPlainString(),
                        user.getCurrency().getSymbol() != null
                                ? user.getCurrency().getSymbol()
                                : user.getCurrency().getCode()
                ));
    }

    private void editMessage(FinanceTrackerBot bot, CallbackQuery callbackQuery, String text, StatisticPeriod period) {
        EditMessageText edit = new EditMessageText();
        edit.setChatId(callbackQuery.getMessage().getChatId().toString());
        edit.setMessageId(callbackQuery.getMessage().getMessageId());
        edit.setText(text);
        edit.setReplyMarkup(buildStatisticKeyboard(period));
        bot.editMessage(edit);
    }

    private InlineKeyboardMarkup buildPeriodKeyboard() {
        return new InlineKeyboardMarkup(List.of(
                List.of(button(Message.periodDayButton, StatisticPeriod.DAY)),
                List.of(button(Message.period7DaysButton, StatisticPeriod.DAYS_7)),
                List.of(button(Message.periodMonthButton, StatisticPeriod.MONTH)),
                List.of(button(Message.periodYearButton, StatisticPeriod.YEAR))
        ));
    }

    private InlineKeyboardMarkup buildStatisticKeyboard(StatisticPeriod period) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(button(Message.periodDayButton, StatisticPeriod.DAY)));
        rows.add(List.of(button(Message.period7DaysButton, StatisticPeriod.DAYS_7)));
        rows.add(List.of(button(Message.periodMonthButton, StatisticPeriod.MONTH)));
        rows.add(List.of(button(Message.periodYearButton, StatisticPeriod.YEAR)));

        InlineKeyboardButton changeCurrencyButton = new InlineKeyboardButton(Message.viewInOtherCurrency);
        changeCurrencyButton.setCallbackData(Callback.VIEW_IN_OTHER_CURRENCY_PREFIX + period.callback());
        rows.add(List.of(changeCurrencyButton));
        
        return new InlineKeyboardMarkup(rows);
    }

    private InlineKeyboardButton button(String text, StatisticPeriod period) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(period.callback());
        return button;
    }

    private void handleViewInOtherCurrency(FinanceTrackerBot bot, CallbackQuery callbackQuery, String data) {
        String chatId = callbackQuery.getMessage().getChatId().toString();
        String periodCallback = data.substring((Callback.VIEW_IN_OTHER_CURRENCY_PREFIX).length());
        
        List<Currency> currencies = currencyRepository.findAll();
        if (currencies.isEmpty()) {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId);
            edit.setMessageId(callbackQuery.getMessage().getMessageId());
            edit.setText(Message.noCurrencies);
            bot.editMessage(edit);
            return;
        }

        InlineKeyboardMarkup markup = buildCurrencyKeyboard(currencies, periodCallback);
        EditMessageText edit = new EditMessageText();
        edit.setChatId(chatId);
        edit.setMessageId(callbackQuery.getMessage().getMessageId());
        edit.setText(Message.chooseCurrencyForPurchase);
        edit.setReplyMarkup(markup);
        bot.editMessage(edit);
    }

    private void handleChangeCurrency(FinanceTrackerBot bot, CallbackQuery callbackQuery, String data) {
        // CHG_CUR_<periodCallback>|<currencyCode>
        String withoutPrefix = data.substring(Callback.CHANGE_PURCHASE_CURRENCY_PREFIX.length());
        int separatorIndex = withoutPrefix.lastIndexOf('|');
        if (separatorIndex == -1 || separatorIndex == withoutPrefix.length() - 1) {
            log.error("Invalid callback format: {}", data);
            return;
        }
        
        String periodCallback = withoutPrefix.substring(0, separatorIndex);
        String currencyCode = withoutPrefix.substring(separatorIndex + 1);
        String chatId = callbackQuery.getMessage().getChatId().toString();

        try {
            User user = resolveUser(callbackQuery);
            Currency newCurrency = currencyRepository.findByCode(currencyCode).orElse(null);
            
            if (newCurrency == null) {
                log.warn("Currency not found: code={}, available currencies: {}", 
                        currencyCode, 
                        currencyRepository.findAll().stream().map(Currency::getCode).toList());
                bot.sendText(chatId, "Валюта не найдена");
                return;
            }

            user.setCurrency(newCurrency);
            userService.updateUser(user);

            StatisticPeriod period = StatisticPeriod.fromCallback(periodCallback);
            if (period == null) {
                period = StatisticPeriod.DAY;
            }
            
            String text = buildStatisticText(user, period);
            
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId);
            edit.setMessageId(callbackQuery.getMessage().getMessageId());
            edit.setText(text);
            edit.setReplyMarkup(buildStatisticKeyboard(period));
            bot.editMessage(edit);
        } catch (Exception e) {
            log.error("Error changing currency", e);
            bot.sendText(chatId, "Ошибка при смене валюты");
        }
    }

    private InlineKeyboardMarkup buildCurrencyKeyboard(List<Currency> currencies, String periodCallback) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>(4);
        
        for (Currency currency : currencies) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(currency.getCode());
            button.setCallbackData(Callback.CHANGE_PURCHASE_CURRENCY_PREFIX + periodCallback + "|" + currency.getCode());
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
}
