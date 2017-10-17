import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
/**
 * This is the Client class for a Client-Server Project which will give users options to interact
 * with the server.
 * 
 * @author Christopher Stump
 * @version 10/6/2016
 */
public class Client
{
    static Scanner keyboard = new Scanner(System.in);
    static Socket client;
    static DataInputStream input;
    static DataOutputStream output;
    public static void main(String args[])
    {   
        //Attempt to connect to the server
        System.out.println("Connecting to server...");
        try{
            client = new Socket("localhost", 3166); //Create a socket between the client and server.
            System.out.println("Connection successful!");

            input = new DataInputStream(client.getInputStream());//Open up input and output streams to communicate with server.
            output = new DataOutputStream(client.getOutputStream());

            int choice = 0;
            while(choice != 4){
                choice = getInput();
                System.out.println("************************************************************");
                sendInfo(choice);
            }
        }
        catch(IOException e){
            System.out.println("Error connecting to server.");
        }
        finally{
            try{ //Close all sockets and I/O once the program ends
                client.close();
                input.close();
                output.close();
                keyboard.close();
            }
            catch(IOException e){
                System.out.println("Error closing sockets and I/O.");
            }
        }
        System.exit(0);
    }

    public static int getInput(){
        int choice;

        System.out.println("************************************************************");
        System.out.println("Select From The List Below:" + "\n1. View Current Balance" +
            "\n2. Deposit Money" + "\n3. Withdraw Money" + "\n4. Exit");
        System.out.println("************************************************************");

        do{ //A do while loop that continues until the user enters a valid choice.
            System.out.print("Enter your choice: ");
            while(!keyboard.hasNextInt()){ //Is the choice numeric?
                System.out.println("That is not a valid entry. Try again.");
                System.out.print("Enter your choice: ");
                keyboard.next();
            }
            choice = keyboard.nextInt();
            if(choice < 1 || choice > 4){ //If it is numeric, but not valid.
                System.out.println("That is not a valid entry. Try again.");
            }
        }
        while(choice < 1 || choice > 4);

        return choice;
    }

    public static void sendInfo(int option){
        double amount = 0;
        String response = "";
        DecimalFormat df = new DecimalFormat("###.##"); //Used to round the amount to two decimal places.
        if(option == 1){ //If the user wants to view their current balance.
            try{
                output.writeInt(1); //Send the option to the server and the server will respond with the user's balance.
                System.out.println("Current balance: $" + df.format(input.readDouble()));
            }
            catch(IOException e){
                System.out.println("Error communicating with server.");
                System.exit(-1); //If an error occurs
            }
        }
        
        if(option == 2){ //If the user wants to deposit money into their account            
            do{ //A do while loop that continues until the user enters a valid choice.
                System.out.print("Enter the amount you wish to deposit: ");
                while(!keyboard.hasNextDouble()){ //Is the choice numeric?
                    System.out.println("That is not a valid entry. Try again.");
                    System.out.print("Enter the amount you wish to deposit: ");
                    keyboard.next();
                }
                amount = keyboard.nextDouble();
                if(amount < 0){ //You cannot deposit negative numbers.
                    System.out.println("That is not a valid entry. Try again.");
                }
            }
            while(amount < 0);
            amount = Double.parseDouble(df.format(amount));
            try{
                output.writeInt(2); //Send the option to the server and the server will respond with a response followed by the amount that was deposited.
                output.writeDouble(amount);
                response = input.readUTF();
                System.out.println(response);
            }
            catch(IOException e){
                System.out.println("Error communicating with server.");
                System.exit(-1);
            }
        }
        
        if(option == 3){            
            do{ //A do while loop that continues until the user enters a valid choice.
                System.out.print("Enter the amount you wish to withdraw: ");
                while(!keyboard.hasNextDouble()){ //Is the choice numeric?
                    System.out.println("That is not a valid entry. Try again.");
                    System.out.print("Enter the amount you wish to withdraw: ");
                    keyboard.next();
                }
                amount = keyboard.nextDouble();
                if(amount < 0){ //You cannot withdraw+ negative numbers.
                    System.out.println("That is not a valid entry. Try again.");
                }
            }
            while(amount < 0);
            amount = Double.parseDouble(df.format(amount));
            try{
                output.writeInt(3); //Send the option to the server and the server will respond with a response followed by the amount that was withdrawn or not.
                output.writeDouble(amount);
                response = input.readUTF();
                System.out.println(response);
            }
            catch(IOException e){
                System.out.println("Error communicating with server.");
                System.exit(-1);
            }
        }
        if(option == 4){ //When the client ends the program the server will respond with a thank you message and the program will begin to close sockets
            try{
                output.writeInt(4);
                response = input.readUTF();
                System.out.println(response);
            }
            catch(IOException e){
                System.out.println("Error communicating with server.");
                System.exit(-1);
            }
        }
        System.out.println("\n\n\n\n");
    }
}
