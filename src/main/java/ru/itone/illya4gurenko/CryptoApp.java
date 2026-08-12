package ru.itone.illya4gurenko;

import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.security.AESCryptoService;

import java.util.Scanner;

/**
 * Вспомогательное приложение.
 * Используется для примера работы де/шифрования паролей
 */
public class CryptoApp extends Base {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String key = args[0];
        AESCryptoService crypto = getCryptoService();

        while (true) {
            System.out.println("1 - encode, 2 - decode, 0 - break: ");
            String cmd = scanner.nextLine();

            if ("1".equals(cmd)) {
                System.out.println("input password: ");
                System.out.println("res: " + "[" + crypto.encrypt(scanner.nextLine()) + "]");
            } else if ("2".equals(cmd)) {
                System.out.println("input encode password: ");
                System.out.println("res: " + "[" + crypto.decrypt(scanner.nextLine())+ "]");
            } else if ("0".equals(cmd)) {
                break;
            }
        }
    }
}
