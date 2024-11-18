import org.interfaces.IPrintServer;
import org.models.Empty;
import org.models.ServerResponse;
import org.models.TokenResponse;

import java.rmi.RemoteException;
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
                        ServerResponse<Empty> response = server.print(filename, "Printer" + printer, token);
                        handleResponse(response);
                        break;
                    case 2:
                        ServerResponse<String> response1 = server.queue("Printer" + printer, token);
                        handleResponse(response1);
                        break;
                    case 3:
                        System.out.println("Enter the job number to top");
                        int job = scanner.nextInt();
                        scanner.nextLine();
                        ServerResponse<Empty> serverResponse = server.topQueue("Printer" + printer, job, token);
                        handleResponse(serverResponse);
                        break;
                    case 4:
                        ServerResponse<String> status = server.status("Printer" + printer, token);
                        handleResponse(status);
                        if (status.wasSuccess()) System.out.println(status);
                        break;
                    case 5:
                        System.out.println("Enter the parameter to read");
                        String parameter = scanner.nextLine();
                        ServerResponse<String> serverResponse1 = server.readConfig(parameter, token);
                        handleResponse(serverResponse1);
                        if (serverResponse1.wasSuccess())
                            System.out.println(serverResponse1);
                        break;
                    case 6:
                        System.out.println("Enter the parameter to set");
                        String parameter1 = scanner.nextLine();
                        System.out.println("Enter the value to set");
                        String value = scanner.nextLine();

                        ServerResponse<Empty> serverResponse2 = server.setConfig(parameter1, value, token);
                        handleResponse(serverResponse2);
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


    private static void handleResponse(ServerResponse response) {
        if (!response.wasAllowed()) {
            System.out.println("You are not allowed to perform this action");
        }
        if (!response.wasSuccess()) {
            System.out.println("Action failed");
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
                        return loginResponse.token();
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
                        return signUpResponse.token();
                    } else {
                        System.out.println("Invalid credentials, Please try again");
                    }
                default:
                    System.out.println("Invalid choice, Please try again");
            }

        }

    }
}