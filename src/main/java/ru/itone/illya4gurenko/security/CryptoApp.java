package ru.itone.illya4gurenko.security;

import java.util.Scanner;

public class CryptoApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String key = args[0];
        AESCryptoService crypto = new AESCryptoService(key);

        while (true) {
            System.out.println("1 - encode, 2 - decode, 0 - break: ");
            String cmd = scanner.nextLine();

            if ("1".equals(cmd)) {
                System.out.println("input password: ");
                System.out.println("res: " + crypto.encrypt(scanner.nextLine()));
            } else if ("2".equals(cmd)) {
                System.out.println("input encode password: ");
                System.out.println("res: " + crypto.decrypt(scanner.nextLine()));
            } else if ("0".equals(cmd)) {
                break;
            }
        }
    }
}
