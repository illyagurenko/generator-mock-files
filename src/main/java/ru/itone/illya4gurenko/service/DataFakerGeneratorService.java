package ru.itone.illya4gurenko.service;

import net.datafaker.Faker;
import net.datafaker.providers.base.Finance;
import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.struct_file.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Сервис генерации реалистичных моковых данных клиентов.
 * Использует библиотеку {@link Faker}
 */
public class DataFakerGeneratorService extends Base implements DataGenerator {
    private static final DataFakerGeneratorService INSTANCE = new DataFakerGeneratorService();

    private final Faker faker;

    public DataFakerGeneratorService() {
        String locale = config.getFakerLocale();
        info("init DataFakeGeneratotService with locale: {}", locale);
        this.faker = new Faker(new Locale(locale));
    }

    public static DataFakerGeneratorService getInstance() {
        return INSTANCE;
    }

    /**
     * Генерирует случайные данные клиента (ФИО, счет карты, тип операции и сумму)
     * и форматирует их в строку фиксированной ширины.
     *
     * @return Сформированная строка с данными банковского клиента
     * @throws RuntimeException Если при генерации данных произошел сбой библиотеки Faker
     */
    @Override
    public String generateData() {
        //debug("generate new mock data client");
        try {
            BankClient bankClient = new BankClient(
                    faker.name().fullName(),
                    faker.finance().creditCard(Finance.CreditCardType.AMERICAN_EXPRESS).replace("-", ""),
                    faker.options().option(Type.class),
                    String.valueOf(faker.number().numberBetween(1, 1000001))
            );
            //debug("client data generated successfully: {}", bankClient);
            return bankClient.toString();
        } catch (Exception e) {
            error("error generate mock data row", e);
            throw new RuntimeException("data generation error", e);
        }
    }
    @Override
    public GruVistaTab generateGruVistaRecord() {
        try {
            LocalDateTime now = LocalDateTime.now();

            return new GruVistaTab(
                    faker.number().randomNumber(10, true),
                    faker.finance().creditCard().replace("-", ""),
                    "RUB",
                    BigDecimal.valueOf(faker.number().randomDouble(3, 10, 100000)),
                    faker.options().option(Type.class),
                    now,
                    faker.number().randomNumber(8, true),
                    faker.number().randomNumber(12, true),
                    BigDecimal.valueOf(faker.number().randomDouble(3, 1000, 50000)),
                    BigDecimal.valueOf(faker.number().randomDouble(3, 50000, 100000)),
                    "Mock transaction " + faker.regexify("[A-Z0-9]{10}"),
                    faker.number().randomNumber(5, true),
                    FocStatus.WAIT,
                    now,
                    ProcType.IMMEDIATE,
                    UUID.randomUUID().toString()
            );
        } catch (Exception e) {
            error("error generating mock GruVistaTab record", e);
            throw new RuntimeException("data generation error", e);
        }
    }
}
