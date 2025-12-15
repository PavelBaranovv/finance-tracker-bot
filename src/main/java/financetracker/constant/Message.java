package financetracker.constant;

public interface Message {
    String welcome = "Привет! Я бот, который поможет Вам эффективно отслеживать Ваши расходы в различных валютах.";

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
}
