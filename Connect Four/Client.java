import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
/**
 * This is the client class for the multiplayer game of Connect Four.
 * 
 * @author Christopher Stump
 * @version 12/10/2016
 */
public class Client
{
    static Scanner keyboard = new Scanner(System.in);

    //Create the client socket.
    static Socket client;

    //Create the input and output streams to communicate with the server.
    static DataInputStream input;

    static DataOutputStream output;
    public static void main(String[] args){
        System.out.println("Connecting to server...");
        try{
            client = new Socket("localhost", 3166); //Establish the connection between the server and client.

            System.out.println("Connection successful!");

            //Open up data input and output streams to communicate with the server.
            input = new DataInputStream(client.getInputStream());
            output = new DataOutputStream(client.getOutputStream());

            //Start the game of connect four.
            playConnectFour();        
        }
        catch(IOException e){
            System.out.println("There was an error connecting to the server.");
        }
        finally{
            try{ //Close communication with server and all other forms of input and output.
                client.close();
                input.close();
                output.close();
                keyboard.close();
            }
            catch(IOException e){
                System.out.println("There was an error closing sockets and/or I/O");
            }
        }
        System.exit(0);
    }

    public static void playConnectFour(){
        boolean gameOver = false; //Used to tell when the game is ended.
        boolean playerTurn; //It is the players turn if this is true.
        boolean valid = false; //Was the clients move possible?
        int ID; //Is the player using Xs or Os.
        int choice = -1; //Holds the users choice of column to drop the chip.
        char chipType; //Stores and X or and O.
        String temp;
        try{            
            ID = input.readInt(); //Holds 1 if the player is player 1 or 2 if they are player 2.      
            if(ID == 1)
                chipType = 'X';
            else
                chipType = 'O';

            playerTurn = input.readBoolean(); //Used to control who starts the game. The first connection starts.
            while(!gameOver){ //This is where the game loop begins.
                
                System.out.print(input.readUTF()); //Print the board.

                //Code to execute when it is the clients turn.
                if(playerTurn){
                    
                    System.out.print("Drop an " + chipType + " at column (0-6): "); //Ask the user to drop a chip in one of the 7 columns.
                    
                    while(valid == false){ //While the clients move is not possible. (Column is already filled up.
                        
                        //Validate the users input.
                        while(choice < 0 || choice > 6){                            
                            temp = keyboard.nextLine();                            
                            if(isNumeric(temp) && !temp.contains(" ")){
                                choice = Integer.parseInt(temp);                                
                            }                            
                            if(choice < 0 || choice > 6){
                                System.out.println("That is not a valid entry. Try again.");
                                System.out.print("Enter your choice: ");
                            }
                        }
                        
                        
                        output.writeInt(choice); //Send the users output to the server.
                        valid = input.readBoolean(); //The server will let the client know if it is a valid move.
                        
                        choice = -1;
                        if(valid == false){ //If it is not valid then the server will need another input.
                            System.out.print("The column is full. Try again: ");
                        }
                    }
                    valid = false; //Reset the validity to false.
                }//If it is not their turn then a message displays that they are waiting on the other client.
                else if(!playerTurn){
                    System.out.println("Waiting on other player...");
                }

                playerTurn = input.readBoolean(); //Used to alternate between the first and second client.
                gameOver = input.readBoolean(); //Check to see if there is a win after every move.

                //When there is a winner, it displays the board and the who won.
                if(gameOver == true){ 
                    System.out.println(input.readUTF());
                    System.out.println(input.readUTF());
                }                
            }
        }
        catch(IOException e){
            System.out.println("Error communicating with the server.");
        }
    }

    /*
     * Test to see if a string is numeric.
     */
    public static boolean isNumeric(String str)  
    {  
        try  
        {  
            double d = Double.parseDouble(str);  
        }  
        catch(NumberFormatException nfe)  
        {  
            return false;  
        }  
        return true;  
    }
}
