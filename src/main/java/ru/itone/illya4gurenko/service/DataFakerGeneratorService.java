package ru.itone.illya4gurenko.service;

import net.datafaker.Faker;
import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.dto_files.BankClient;
import ru.itone.illya4gurenko.dto_files.Type;

import java.util.Locale;

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

    @Override
    public String generateData() {
        debug("generate new mock data client");
        BankClient bankClient = new BankClient(faker.name().fullName(),
                faker.finance().creditCard().replace("-", ""),
                faker.options().option(Type.class),
                faker.number().numberBetween(1, 1000001) + "");
        debug("generate client: {}", bankClient.toString());
        return bankClient.toString();
    }
}
