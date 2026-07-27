package ru.itone.illya4gurenko.file_generation;

import net.datafaker.Faker;
import ru.itone.illya4gurenko.model.BankClient;
import ru.itone.illya4gurenko.model.Type;

import java.util.Locale;

public class DataFakerGenerator implements DataGenerator {
    private final Faker faker;

    public DataFakerGenerator(String locale) {
        this.faker = new Faker(new Locale("locale"));
    }

    @Override
    public String generateData() {
        BankClient bankClient = new BankClient(faker.name().fullName(),
                faker.finance().creditCard().replace("-", ""),
                faker.options().option(Type.class),
                faker.number().numberBetween(1, 1000001) + "");
        return bankClient.toString();
    }
}
