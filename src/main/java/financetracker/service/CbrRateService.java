package financetracker.service;

import financetracker.entity.Currency;
import financetracker.entity.CurrencyRate;
import financetracker.entity.ExchangeRate;
import financetracker.repository.CurrencyRateRepository;
import financetracker.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Service
@RequiredArgsConstructor
public class CbrRateService {

    private static final String CBR_URL = "https://www.cbr.ru/scripts/XML_daily.asp";

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRateRepository currencyRateRepository;

    public ExchangeRate getForToday(List<Currency> currencies) {
        LocalDate today = LocalDate.now();
        Optional<ExchangeRate> existing = exchangeRateRepository.findByDate(today);
        if (existing.isPresent()) {
            return existing.get();
        }

        String xml = getXml();
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setDate(today);

        List<CurrencyRate> currencyRates = mapToCurrencyRates(xml, currencies, exchangeRate);
        exchangeRate.setCurrencyRates(currencyRates);

        ExchangeRate saved = exchangeRateRepository.save(exchangeRate);
        currencyRates.forEach(cr -> cr.setExchangeRate(saved));
        currencyRateRepository.saveAll(currencyRates);
        return saved;
    }

    private String getXml() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CBR_URL))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить курсы ЦБ", e);
        }
    }

    private List<CurrencyRate> mapToCurrencyRates(String xml, List<Currency> currencies, ExchangeRate exchangeRate) {
        try {
            Map<String, Currency> currencyByCode = currencies.stream()
                    .collect(Collectors.toMap(Currency::getCode, c -> c));

            List<CurrencyRate> result = new ArrayList<>();

            Currency rub = currencyByCode.get("RUB");
            if (rub != null) {
                result.add(CurrencyRate.builder()
                        .currency(rub)
                        .nominal(BigDecimal.ONE)
                        .value(BigDecimal.ONE)
                        .exchangeRate(exchangeRate)
                        .build());
            }

            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            document.getDocumentElement().normalize();

            NodeList list = document.getElementsByTagName("Valute");
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                String charCode = getTagValue(element, "CharCode");
                String nominalStr = getTagValue(element, "Nominal");
                String valueStr = getTagValue(element, "Value");

                Currency currency = currencyByCode.get(charCode);
                if (currency == null) {
                    continue;
                }

                BigDecimal nominal = new BigDecimal(nominalStr.replace(",", "."));
                BigDecimal value = new BigDecimal(valueStr.replace(",", "."));

                result.add(CurrencyRate.builder()
                        .currency(currency)
                        .nominal(nominal)
                        .value(value)
                        .exchangeRate(exchangeRate)
                        .build());
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось распарсить XML ЦБ", e);
        }
    }

    private String getTagValue(Element element, String tag) {
        return element.getElementsByTagName(tag).item(0).getTextContent();
    }
}


