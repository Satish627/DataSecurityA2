import org.interfaces.IPrintServer;
import org.models.TokenResponse;

import java.rmi.RemoteException;
import java.util.Optional;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {

            IPrintServer server = GetServer.getServerFromRmi();
            Scanner scanner = new Scanner(System.in);
            String token = login(server, scanner);

            while (true) {
                System.out.println("Select a printer");
                System.out.println("1. Printer1 , 2. Printer2, 3. Printer3");
                int printer = scanner.nextInt();
                scanner.nextLine();
                System.out.println("Select an action to take :");
                System.out.println("1. Print, 2. Queue, 3. TopQueue, 4. Status, 5. ReadConfig, 6. SetConfig, 7. Exit");
                int action = scanner.nextInt();
                scanner.nextLine();

                if (action == 7) {
                    System.out.println("Exiting gracefully ...");
                    break;
                }

                switch (action) {
                    case 1:
                        System.out.println("Enter the filename to print");
                        String filename = scanner.nextLine();
                        server.print(filename, "Printer" + printer, token);
                        break;
                    case 2:
                        System.out.println(server.queue("Printer" + printer, token));
                        break;
                    case 3:
                        System.out.println("Enter the job number to top");
                        int job = scanner.nextInt();
                        scanner.nextLine();
                        server.topQueue("Printer" + printer, job, token);
                        break;
                    case 4:
                        System.out.println(server.status("Printer" + printer, token));
                        break;
                    case 5:
                        System.out.println("Enter the parameter to read");
                        String parameter = scanner.nextLine();
                        System.out.println(server.readConfig(parameter, token));
                        break;
                    case 6:
                        System.out.println("Enter the parameter to set");
                        String parameter1 = scanner.nextLine();
                        System.out.println("Enter the value to set");
                        String value = scanner.nextLine();
                        server.setConfig(parameter1, value, token);
                        break;
                    default:
                        System.out.println("Invalid choice, Please try again");

                }

            }
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("Error occurred while communicating with the server");
        }
    }


    private static String login(IPrintServer server, Scanner scanner) throws RemoteException {
        while (true) {
            System.out.println("1. Login with existing account,  2. Sign up for a new account");
            int choice = scanner.nextInt();
            scanner.nextLine();

            String email = null;
            String password = null;

            switch (choice) {
                case 1:
                    System.out.print("Enter email: ");
                    email = scanner.nextLine();
                    System.out.print("Enter password: ");
                    password = scanner.nextLine();
                    TokenResponse loginResponse = server.login(email, password);
                    if (loginResponse.wasSuccessful()) {
                        return loginResponse.getToken();
                    } else {
                        System.out.println("Invalid credentials, Please try again");
                    }
                case 2:
                    System.out.print("Enter email: ");
                    email = scanner.nextLine();
                    System.out.print("Enter password: ");
                    password = scanner.nextLine();
                    TokenResponse signUpResponse = server.signUp(email, password);
                    if (signUpResponse.wasSuccessful()) {
                        return signUpResponse.getToken();
                    } else {
                        System.out.println("Invalid credentials, Please try again");
                    }
                default:
                    System.out.println("Invalid choice, Please try again");
            }

        }

    }
}