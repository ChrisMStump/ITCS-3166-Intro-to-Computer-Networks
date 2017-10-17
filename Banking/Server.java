import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
/**
 * This is the Server class for a Clinet-Server Project which will take client information and process it.
 * 
 * @author Christopher Stump
 * @version 10/6/2016
 */
public class Server
{
    static DataInputStream input; //Used to establish connection and exchange data with the client.
    static DataOutputStream output;
    static ServerSocket ss;
    static Socket s;
    public static void main(String args[]) throws IOException
    {        
        try{
            System.out.println("Server is started.");
            ss = new ServerSocket(3166); //Listen on the specified port
            System.out.println("Waiting for client request");
            s = ss.accept(); //Accept connection from client
            System.out.println("Client connected");

            input = new DataInputStream(s.getInputStream()); //Open up input and output streams to communicate with client.
            output = new DataOutputStream(s.getOutputStream());
            
            doWork(); //Call the doWork method to do all the banking
        }
        catch(IOException e){
            System.out.println("Server Error."); //If the server failed to connect with the host
        }
        finally{ //After the program has finished executing the server will close all of the sockets and I/O
            try{
                input.close();
                output.close();
                ss.close();
                s.close();
                System.out.println("All sockets and I/O have been closed");
            }
            catch(IOException e){
                System.out.println("Error closing sockets and I/O.");
            }
        }
        System.out.println("Server stopped");
    }

    public static void doWork(){
        int choice;
        double amount = 0;
        double newBalance = 0;
        Account client = new Account();
        DecimalFormat df = new DecimalFormat("###.##"); //Used to round the amount to two decimal places.
        try{
            do{
                choice = input.readInt();
                System.out.println("The client has entered choice: " + choice);
                if(choice == 1){ //Calls the getBalance method in the Account class to get the clients balance
                    output.writeDouble(client.getBalance()); //Send balance to client
                }
                if(choice == 2){ //Calls the deposit method in the Account class and updates their balance
                    amount = input.readDouble();
                    newBalance = client.desposit(amount);
                    newBalance = Double.parseDouble(df.format(newBalance));
                    System.out.println("The client deposited $" + df.format(amount));
                    output.writeUTF("You chose to deposit $" + df.format(amount) + ", so your new balance is: $" + df.format(newBalance)); //Tell the client how much they deposited and their new balance
                }
                if(choice == 3){ //Calls the withdraw method in the Account class and updates their balance
                    amount = input.readDouble();
                    if(client.getBalance() - amount >= 0){ //They are able to withdraw the amount from their balance
                        newBalance = client.withdraw(amount);
                        newBalance = Double.parseDouble(df.format(newBalance));
                        System.out.println("The client withdrew $" + df.format(amount));
                        output.writeUTF("You chose to withdraw $" + df.format(amount) + ", so your new balance is: $" + df.format(newBalance)); //Tell the client how much they withdrew and their new balance
                    }
                    else{ //They are not able to withdraw the amount
                        System.out.println("The client tried to withdraw more than their balance.");
                        output.writeUTF("Taking out $" + df.format(amount) + " from your balance of $" + df.format(client.getBalance()) + " would overdraw your balance.");
                    }
                }
                if(choice == 4){
                    System.out.println("The client has chose to exit the program");
                    output.writeUTF("Thank you for banking with us today!");
                }
            }
            while(choice != 4); //Loop through this until the client wishes to end the session
        }
        catch(IOException e){
        }
    }
}
