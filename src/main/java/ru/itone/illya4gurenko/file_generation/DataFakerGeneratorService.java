package ru.itone.illya4gurenko.file_generation;

import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itone.illya4gurenko.model.BankClient;
import ru.itone.illya4gurenko.model.Type;
import java.util.Locale;

public class DataFakerGeneratorService implements DataGenerator {
    private static final Logger log = LoggerFactory.getLogger(DataFakerGeneratorService.class);
    private final Faker faker;

    public DataFakerGeneratorService(String locale) {
        log.info("init DataFakeGeneratotService with locale: {}", locale);
        this.faker = new Faker(new Locale(locale));
    }

    @Override
    public String generateData() {
        log.debug("generate new mock data client");
        BankClient bankClient = new BankClient(faker.name().fullName(),
                faker.finance().creditCard().replace("-", ""),
                faker.options().option(Type.class),
                faker.number().numberBetween(1, 1000001) + "");
        log.debug("generate client: {}", bankClient.toString());
        return bankClient.toString();
    }
}
