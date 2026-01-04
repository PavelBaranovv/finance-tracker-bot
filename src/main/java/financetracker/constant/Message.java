package financetracker.constant;

public interface Message {
    String welcome = """
            Привет! Я бот, который поможет Вам эффективно отслеживать Ваши расходы в различных валютах.
            /add_purchase – добавить покупку
            /view_statistic – просмотр статистики расходов
            /view_recent – просмотр последних расходов""";

    String noRubCurrency = "Сервис временно недоступен. Попробуйте повторить попытку позже";
    String noCurrencies = "Ошибка получения списка валют. Пожалуйста, попробуйте позже";
    String chooseCurrency = "Выберите валюту:";
    String currencySelected = "Выбрана валюта %s";
    String askPrice = "Введите цену (например, 123.45):";
    String priceParseError = "Не смог прочитать число. Введите цену ещё раз, например: 123.45";
    String askAmount = "Введите количество (целое число):";
    String amountParseError = "Не смог прочитать количество. Введите целое число, например: 2";
    String askName = "Введите название покупки:";
    String purchaseSaved = "✅ Покупка сохранена";
    String viewInOtherCurrency = "Смотреть в другой валюте";
    String chooseCurrencyForPurchase = "Выберите валюту для просмотра покупки:";

    // Статистика
    String choosePeriod = "Выберите период:";
    String periodDayButton = "За день";
    String period7DaysButton = "За 7 дней";
    String periodMonthButton = "За месяц";
    String periodYearButton = "За год";

    String periodDayHeader = "За сегодня вы потратили:";
    String period7DaysHeader = "За последние 7 дней вы потратили:";
    String periodMonthHeader = "За текущий месяц вы потратили:";
    String periodYearHeader = "За текущий год вы потратили:";

    String statisticEmpty = "0";
    String statisticError = "Не удалось получить статистику. Пожалуйста, попробуйте позже";
    String totalLine = "Итого: %s %s";
    
    String recentPurchasesHeader = "Ваши последние траты:";
    String noRecentPurchases = "У вас пока нет покупок";
}
