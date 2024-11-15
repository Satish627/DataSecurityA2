import org.interfaces.IPrintServer;

import java.rmi.RemoteException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            IPrintServer server = GetServer.getServerFromRmi();
            Scanner scanner = new Scanner(System.in);

            // Step 1: Login before accessing other functions
            String token = null;
            while (token == null) {
                System.out.print("Enter email: ");
                String email = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                token = server.login(email, password);
                if (token != null) {
                    System.out.println("Login successful!");
                } else {
                    System.out.println("Login failed. Please try again.");
                }
            }

            // Main loop for user actions
            while (true) {
                System.out.println("\nSelect a printer:");
                System.out.println("1. Printer1\n2. Printer2\n3. Printer3");
                int printer = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                System.out.println("\nSelect an action to take:");
                System.out.println("1. Print\n2. Queue\n3. TopQueue\n4. Status\n5. ReadConfig\n6. SetConfig\n7. Exit");
                int action = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (action == 7) {
                    System.out.println("Exiting gracefully...");
                    break;
                }

                try {
                    switch (action) {
                        case 1: // Print
                            System.out.println("Enter the filename to print:");
                            String filename = scanner.nextLine();
                            server.print(token, filename, "Printer" + printer);
                            System.out.println("File sent to printer.");
                            break;

                        case 2: // Queue
                            String queue = server.queue(token, "Printer" + printer);
                            System.out.println("Current queue:\n" + queue);
                            break;

                        case 3: // TopQueue
                            if (!server.hasPermission(token, "TopQueue")) {
                                System.out.println("You do not have permission to perform this action.");
                                break;
                            }
                            System.out.println("Enter the job number to prioritize:");
                            int job = scanner.nextInt();
                            scanner.nextLine(); // Consume newline
                            server.topQueue(token, "Printer" + printer, job);
                            System.out.println("Job moved to top of queue.");
                            break;

                        case 4: // Status
                            String status = server.status(token, "Printer" + printer);
                            System.out.println("Printer status:\n" + status);
                            break;

                        case 5: // ReadConfig
                            if (!server.hasPermission(token, "ReadConfig")) {
                                System.out.println("You do not have permission to perform this action. Please try another task.");
                                break;
                            }

                            System.out.println("Enter the configuration parameter to read:");
                            String parameter = scanner.nextLine();
                            String configValue = server.readConfig(token, parameter);
                            System.out.println("Configuration value: " + configValue);
                            break;


                        case 6: // SetConfig
                            // Pre-check for permission
                            if (!server.hasPermission(token, "SetConfig")) {
                                System.out.println("You do not have permission to perform this action. Please try another task.");
                                break;
                            }

                            System.out.println("Enter the configuration parameter to set:");
                            String param = scanner.nextLine();
                            System.out.println("Enter the value to set:");
                            String value = scanner.nextLine();
                            server.setConfig(token, param, value);
                            System.out.println("Configuration updated successfully.");
                            break;

                        default:
                            System.out.println("Invalid action. Please try again.");
                    }
                } catch (RemoteException e) {
                    System.out.println("\nError: " + e.getMessage());
                    System.out.println("Please select a valid action or verify your permissions.");
                }
            }
        } catch (RemoteException e) {
            System.out.println("Error connecting to the server. Please try again later.");
            e.printStackTrace();
        }
    }
}
