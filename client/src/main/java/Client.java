import org.interfaces.IPrintServer;

import java.rmi.RemoteException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            IPrintServer server = GetServer.getServerFromRmi();
            Scanner scanner = new Scanner(System.in);

            // Step 1: Login before accessing other functions
            String token= null;
            while (token == null) {
                System.out.print("Enter email: ");
                String email = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                token = server.login(email, password);
                if (token!= null) {
                    System.out.println("Login successful!");
                } else {
                    System.out.println("Login failed. Please try again.");
                }
            }

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
                        server.print(token,filename, "Printer" + printer);
                        break;
                    case 2:
                        System.out.println(server.queue(token,"Printer" + printer));
                        break;
                    case 3:
                        System.out.println("Enter the job number to top");
                        int job = scanner.nextInt();
                        scanner.nextLine();
                        server.topQueue(token,"Printer" + printer, job);
                        break;
                    case 4:
                        System.out.println(server.status(token,"Printer" + printer));
                        break;
                    case 5:
                        System.out.println("Enter the parameter to read");
                        String parameter = scanner.nextLine();
                        System.out.println(server.readConfig(token,parameter));
                        break;
                    case 6:
                        System.out.println("Enter the parameter to set");
                        String parameter1 = scanner.nextLine();
                        System.out.println("Enter the value to set");
                        String value = scanner.nextLine();
                        server.setConfig(token,parameter1, value);
                        break;

                }

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}