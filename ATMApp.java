import java.util.Scanner;

import interfaces.ATMService;
import models.BankAccount;
import models.SavingAccount;
import services.ATMServiceImpl;

public class ATMApp {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // Multiple accounts (Beginner friendly + real world style)
        BankAccount[] accounts = {
            new SavingAccount(1001, 1234, 5000),
            new SavingAccount(1002, 4321, 8000),
            new SavingAccount(1003, 1111, 12000)
        };

        System.out.println("====== ATM SYSTEM ======");

        try {
            System.out.print("Enter Account Number: ");
            int enteredAccountNumber = sc.nextInt();

            BankAccount selectedAccount = null;

            // Find account by account number
            for (BankAccount acc : accounts) {
                if (acc.getAccountNumber() == enteredAccountNumber) {
                    selectedAccount = acc;
                    break;
                }
            }

            if (selectedAccount == null) {
                System.out.println("Account not found!");
                sc.close();
                return;
            }

            System.out.print("Enter 4-digit PIN: ");
            int enteredPin = sc.nextInt();

            if (!selectedAccount.authenticate(enteredPin)) {
                System.out.println("Wrong PIN! Access denied.");
                sc.close();
                return;
            }

            ATMService atm = new ATMServiceImpl(selectedAccount);

            int choice;

            do {
                System.out.println("\n====== ATM MENU ======");
                System.out.println("1. Check Balance");
                System.out.println("2. Deposit Money");
                System.out.println("3. Withdraw Money");
                System.out.println("4. Change PIN");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");

                try {
                    choice = sc.nextInt();

                    switch (choice) {
                        case 1:
                            atm.checkBalance();
                            break;

                        case 2:
                            System.out.print("Enter deposit amount: ");
                            double depositAmount = sc.nextDouble();
                            atm.deposit(depositAmount);
                            break;

                        case 3:
                            System.out.print("Enter withdrawal amount: ");
                            double withdrawAmount = sc.nextDouble();
                            atm.withdraw(withdrawAmount);
                            break;

                        case 4:
                            System.out.print("Enter old PIN: ");
                            int oldPin = sc.nextInt();
                            System.out.print("Enter new 4-digit PIN: ");
                            int newPin = sc.nextInt();
                            atm.changePin(oldPin, newPin);
                            break;

                        case 5:
                            System.out.println("Thank you for using ATM.");
                            break;

                        default:
                            System.out.println("Invalid choice! Please enter 1 to 5.");
                    }

                } catch (Exception e) {
                    System.out.println("Invalid input! Please enter numbers only.");
                    sc.nextLine(); // clear invalid input
                    choice = 0;
                }

            } while (choice != 5);

        } catch (Exception e) {
            System.out.println("Invalid input! Please enter numeric values only.");
        }

        sc.close();
    }
}
