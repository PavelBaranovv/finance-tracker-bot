// FinanceTrackerBotTest.java
package financetracker.bot;

import financetracker.service.AddPurchaseService;
import financetracker.service.StatisticService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.*;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class FinanceTrackerBotTest {

    private FinanceTrackerBot bot;
    private TestAddPurchaseService testAddPurchaseService;
    private TestStatisticService testStatisticService;

    // Простая тестовая реализация AddPurchaseService
    static class TestAddPurchaseService extends AddPurchaseService {
        private boolean handleOngoingDialogsCalled = false;
        private boolean startAddPurchaseCalled = false;
        private boolean handleCallbackCalled = false;

        private FinanceTrackerBot lastBot;
        private Long lastUserId;
        private String lastChatId;
        private String lastText;
        private String lastUsername;
        private Update lastUpdate;
        private CallbackQuery lastCallbackQuery;

        public TestAddPurchaseService() {
            // Вызываем конструктор суперкласса с null значениями
            super(null, null, null, null, null, null);
        }

        @Override
        public void handleOngoingDialogs(FinanceTrackerBot bot, Long userId, String chatId, String text, String username) {
            handleOngoingDialogsCalled = true;
            lastBot = bot;
            lastUserId = userId;
            lastChatId = chatId;
            lastText = text;
            lastUsername = username;
        }

        @Override
        public void startAddPurchase(FinanceTrackerBot bot, Update update, String chatId) {
            startAddPurchaseCalled = true;
            lastBot = bot;
            lastUpdate = update;
            lastChatId = chatId;
        }

        @Override
        public void handleCallback(FinanceTrackerBot bot, CallbackQuery callbackQuery) {
            handleCallbackCalled = true;
            lastBot = bot;
            lastCallbackQuery = callbackQuery;
        }

        // Геттеры для проверок
        public boolean isHandleOngoingDialogsCalled() {
            return handleOngoingDialogsCalled;
        }

        public boolean isStartAddPurchaseCalled() {
            return startAddPurchaseCalled;
        }

        public boolean isHandleCallbackCalled() {
            return handleCallbackCalled;
        }

        public FinanceTrackerBot getLastBot() {
            return lastBot;
        }

        public Long getLastUserId() {
            return lastUserId;
        }

        public String getLastChatId() {
            return lastChatId;
        }

        public String getLastText() {
            return lastText;
        }

        public String getLastUsername() {
            return lastUsername;
        }

        public Update getLastUpdate() {
            return lastUpdate;
        }

        public CallbackQuery getLastCallbackQuery() {
            return lastCallbackQuery;
        }
    }

    // Простая тестовая реализация StatisticService
    static class TestStatisticService extends StatisticService {
        private boolean startViewStatisticCalled = false;
        private boolean viewRecentPurchasesCalled = false;
        private boolean handleCallbackCalled = false;

        private FinanceTrackerBot lastBot;
        private Update lastUpdate;
        private String lastChatId;
        private CallbackQuery lastCallbackQuery;

        public TestStatisticService() {
            // Вызываем конструктор суперкласса с null значениями
            super(null, null, null, null);
        }

        @Override
        public void startViewStatistic(FinanceTrackerBot bot, Update update, String chatId) {
            startViewStatisticCalled = true;
            lastBot = bot;
            lastUpdate = update;
            lastChatId = chatId;
        }

        @Override
        public void viewRecentPurchases(FinanceTrackerBot bot, Update update, String chatId) {
            viewRecentPurchasesCalled = true;
            lastBot = bot;
            lastUpdate = update;
            lastChatId = chatId;
        }

        @Override
        public void handleCallback(FinanceTrackerBot bot, CallbackQuery callbackQuery) {
            handleCallbackCalled = true;
            lastBot = bot;
            lastCallbackQuery = callbackQuery;
        }

        // Геттеры для проверок
        public boolean isStartViewStatisticCalled() {
            return startViewStatisticCalled;
        }

        public boolean isViewRecentPurchasesCalled() {
            return viewRecentPurchasesCalled;
        }

        public boolean isHandleCallbackCalled() {
            return handleCallbackCalled;
        }

        public FinanceTrackerBot getLastBot() {
            return lastBot;
        }

        public Update getLastUpdate() {
            return lastUpdate;
        }

        public String getLastChatId() {
            return lastChatId;
        }

        public CallbackQuery getLastCallbackQuery() {
            return lastCallbackQuery;
        }
    }

    @BeforeEach
    void setUp() {
        testAddPurchaseService = new TestAddPurchaseService();
        testStatisticService = new TestStatisticService();
        bot = new FinanceTrackerBot(
                "test-token",
                "test-bot",
                testAddPurchaseService,
                testStatisticService
        );
    }

    @Test
    void getBotUsername_ShouldReturnConfiguredUsername() {
        assertEquals("test-bot", bot.getBotUsername());
    }

    @Test
    void onUpdateReceived_WithTextMessage_ShouldCallAddPurchaseService() {
        // Arrange
        Update update = createTextUpdate("Some text", 123L, 456L);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        assertTrue(testAddPurchaseService.isHandleOngoingDialogsCalled());
        assertEquals(bot, testAddPurchaseService.getLastBot());
        assertEquals(123L, testAddPurchaseService.getLastUserId());
        assertEquals("456", testAddPurchaseService.getLastChatId());
        assertEquals("Some text", testAddPurchaseService.getLastText());
        assertEquals("testuser", testAddPurchaseService.getLastUsername());
    }

    @Test
    void onUpdateReceived_WithNullMessage_ShouldDoNothing() {
        // Arrange
        Update update = new Update();
        update.setMessage(null);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        assertFalse(testAddPurchaseService.isHandleOngoingDialogsCalled());
        assertFalse(testStatisticService.isStartViewStatisticCalled());
    }

    @Test
    void onUpdateReceived_WithMessageWithoutText_ShouldDoNothing() {
        // Arrange
        Update update = new Update();
        Message message = new Message();
        message.setText(null);
        message.setFrom(new User());
        message.setChat(new Chat(456L, "private"));
        update.setMessage(message);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        assertFalse(testAddPurchaseService.isHandleOngoingDialogsCalled());
        assertFalse(testStatisticService.isStartViewStatisticCalled());
    }

    @Test
    void handleCommandIfAny_StartCommand_ShouldSendWelcomeMessage() throws Exception {
        // Arrange
        Update update = new Update();

        // Создаем тестовый бот с переопределенным sendText
        TestBotWithSendText testBot = new TestBotWithSendText(
                "test-token",
                "test-bot",
                testAddPurchaseService,
                testStatisticService
        );

        // Act
        boolean result = invokePrivateMethod(testBot, "handleCommandIfAny",
                "/start", "123", update);

        // Assert
        assertTrue(result);
        // Проверяем, что сообщение было отправлено
        assertNotNull(testBot.getLastSentText());
        assertFalse(testBot.getLastSentText().isEmpty());
        assertEquals("123", testBot.getLastChatId());
    }

    @Test
    void handleCommandIfAny_AddPurchaseCommand_ShouldCallService() throws Exception {
        // Arrange
        Update update = new Update();

        // Act
        boolean result = invokePrivateMethod(bot, "handleCommandIfAny",
                "/add_purchase", "123", update);

        // Assert
        assertTrue(result);
        assertTrue(testAddPurchaseService.isStartAddPurchaseCalled());
        assertEquals(bot, testAddPurchaseService.getLastBot());
        assertEquals(update, testAddPurchaseService.getLastUpdate());
        assertEquals("123", testAddPurchaseService.getLastChatId());
    }

    @Test
    void handleCommandIfAny_ViewStatisticCommand_ShouldCallService() throws Exception {
        // Arrange
        Update update = new Update();

        // Act
        boolean result = invokePrivateMethod(bot, "handleCommandIfAny",
                "/view_statistic", "123", update);

        // Assert
        assertTrue(result);
        assertTrue(testStatisticService.isStartViewStatisticCalled());
        assertEquals(bot, testStatisticService.getLastBot());
        assertEquals(update, testStatisticService.getLastUpdate());
        assertEquals("123", testStatisticService.getLastChatId());
    }

    @Test
    void handleCommandIfAny_ViewRecentCommand_ShouldCallService() throws Exception {
        // Arrange
        Update update = new Update();

        // Act
        boolean result = invokePrivateMethod(bot, "handleCommandIfAny",
                "/view_recent", "123", update);

        // Assert
        assertTrue(result);
        assertTrue(testStatisticService.isViewRecentPurchasesCalled());
        assertEquals(bot, testStatisticService.getLastBot());
        assertEquals(update, testStatisticService.getLastUpdate());
        assertEquals("123", testStatisticService.getLastChatId());
    }

    @Test
    void handleCommandIfAny_UnknownCommand_ShouldReturnFalse() throws Exception {
        // Arrange
        Update update = new Update();

        // Act
        boolean result = invokePrivateMethod(bot, "handleCommandIfAny",
                "/unknown", "123", update);

        // Assert
        assertFalse(result);
        assertFalse(testAddPurchaseService.isStartAddPurchaseCalled());
        assertFalse(testStatisticService.isStartViewStatisticCalled());
    }

    @Test
    void handleCommandIfAny_RegularText_ShouldReturnFalse() throws Exception {
        // Arrange
        Update update = new Update();

        // Act
        boolean result = invokePrivateMethod(bot, "handleCommandIfAny",
                "regular text", "123", update);

        // Assert
        assertFalse(result);
        assertFalse(testAddPurchaseService.isStartAddPurchaseCalled());
        assertFalse(testStatisticService.isStartViewStatisticCalled());
    }

    @Test
    void sendText_ShouldCreateAndSendMessage() {
        // Arrange
        TestBotWithSendText testBot = new TestBotWithSendText(
                "test-token",
                "test-bot",
                testAddPurchaseService,
                testStatisticService
        );

        // Act
        testBot.sendText("123", "Test message");

        // Assert
        assertEquals("Test message", testBot.getLastSentText());
        assertEquals("123", testBot.getLastChatId());
    }

    @Test
    void constructor_ShouldInitializeCorrectly() {
        // Arrange
        String botToken = "test-token-123";
        String botUsername = "finance-test-bot";

        // Act
        FinanceTrackerBot newBot = new FinanceTrackerBot(
                botToken,
                botUsername,
                testAddPurchaseService,
                testStatisticService
        );

        // Assert
        assertEquals(botUsername, newBot.getBotUsername());
    }

    // Тестовый класс для проверки отправки текста
    static class TestBotWithSendText extends FinanceTrackerBot {
        private String lastSentText = "";
        private String lastChatId = "";

        public TestBotWithSendText(String botToken, String botUsername,
                                   AddPurchaseService addPurchaseService,
                                   StatisticService statisticService) {
            super(botToken, botUsername, addPurchaseService, statisticService);
        }

        @Override
        public void sendText(String chatId, String text) {
            lastSentText = text;
            lastChatId = chatId;
        }

        public String getLastSentText() {
            return lastSentText;
        }

        public String getLastChatId() {
            return lastChatId;
        }
    }

    // Вспомогательный метод для создания текстового Update
    private Update createTextUpdate(String text, Long userId, Long chatId) {
        Update update = new Update();
        Message message = new Message();
        User user = new User();
        Chat chat = new Chat();

        user.setId(userId);
        user.setUserName("testuser");

        chat.setId(chatId);
        chat.setType("private");

        message.setText(text);
        message.setFrom(user);
        message.setChat(chat);

        update.setMessage(message);
        return update;
    }

    // Вспомогательный метод для вызова приватных методов через рефлексию
    private boolean invokePrivateMethod(FinanceTrackerBot bot, String methodName,
                                        String text, String chatId, Update update) throws Exception {
        Method method = FinanceTrackerBot.class.getDeclaredMethod(
                "handleCommandIfAny", String.class, String.class, Update.class);
        method.setAccessible(true);
        return (boolean) method.invoke(bot, text, chatId, update);
    }
}