import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
/**
 * This is the server class for the connect four game.
 * 
 * @author Christopher Stump
 * @version 12/10/2016
 */
public class Server
{   
    //Create the server socket and a socket for each client.
    static ServerSocket server;

    static Socket clientOne;

    static Socket clientTwo;

    //Create input and output streams to send information to each client.
    static DataOutputStream outputOne;

    static DataOutputStream outputTwo;

    static DataInputStream inputOne;

    static DataInputStream inputTwo;

    static final int PORT = 3166;

    public static void main(String args[]) throws IOException
    {
        System.out.println("Server is started.");
        try{            
            server = new ServerSocket(3166); //Listen on the specified port

            System.out.println("Waiting for first clients connection.");
            clientOne = server.accept(); //Accept connection from first client.
            System.out.println("Client One: Connected");

            //Create the input and output streams between the first client.
            inputOne = new DataInputStream(clientOne.getInputStream());
            outputOne = new DataOutputStream(clientOne.getOutputStream());

            outputOne.writeInt(1); //Send the ID number one to the first client.

            System.out.println("Waiting for second clients connection.");
            clientTwo = server.accept(); //Accept connection from second client.
            System.out.println("Client Two: Connected");          

            //Create the input and output streams between the second client.
            inputTwo = new DataInputStream(clientTwo.getInputStream());            
            outputTwo = new DataOutputStream(clientTwo.getOutputStream());

            outputTwo.writeInt(2); //Send the ID number two to the second client.

            playConnectFour(); //Call the doWork method to do all the banking
        }
        catch(IOException e){
            System.out.println("Failed to connect with clients."); //If the server failed to connect with the host
        }
        finally{ //After the program has finished executing the server will close all of the sockets and I/O
            try{
                inputOne.close();
                inputTwo.close();
                outputOne.close();
                outputTwo.close();
                clientOne.close();
                clientTwo.close();
                server.close();
                System.out.println("All sockets and I/O have been closed");
            }
            catch(IOException e){
                System.out.println("Error closing sockets and I/O.");
            }
        }
        System.out.println("Server stopped");
    }

    public static void playConnectFour(){
        boolean gameOver = false; //Sentinel used to end the while loop when the game has ended.        

        char board[][] = new char[6][7]; //Create a blank connect four board.

        char chipType; //Holds the type of chip the client has.

        int columnPosition = 0; //Which column the client chose.

        boolean playerTurn = true; //Client 1 is true and Client 2 is false.

        boolean sentinel = true;

        String boardString; //The board in string format to send to the clients.

        try{
            outputOne.writeBoolean(true); //Tell client one he is first to go.
            outputTwo.writeBoolean(false); //Client two does not go first.
            while(!gameOver){
                if(sentinel){
                    boardString = printBoard(board); //Print the board on server and both clients terminals.
                    outputOne.writeUTF(boardString);
                    outputTwo.writeUTF(boardString);}
                if(playerTurn){ //If player one the chip type is X's.
                    columnPosition = inputOne.readInt();
                    chipType = 'X';
                }
                //Player two's turn.
                else{ //Otherwise player two's chips are O's.
                    columnPosition = inputTwo.readInt();
                    chipType = 'O';
                }

                if(dropChip(board,columnPosition, chipType)){ //Are we able to place 
                    System.out.println("Player " + chipType + " placed a chip in column " + columnPosition + ".");

                    if(playerTurn){ //Validate that the client correctly placed a chip and allow the next client to go.
                        outputOne.writeBoolean(true);
                        outputTwo.writeBoolean(true);
                        outputOne.writeBoolean(false);
                        sentinel = true;
                    }
                    else if(!playerTurn){
                        outputTwo.writeBoolean(true);
                        outputOne.writeBoolean(true);
                        outputTwo.writeBoolean(false);
                        sentinel = true;
                    }                   

                    if(checkWin(board, columnPosition, chipType)){ //Check for a win.                        
                        gameOver = true;
                        boardString = printBoard(board);
                        outputOne.writeBoolean(true);
                        outputTwo.writeBoolean(true);
                        outputOne.writeUTF(boardString);
                        outputTwo.writeUTF(boardString);
                        outputOne.writeUTF("Player " + chipType + " has won!");
                        outputTwo.writeUTF("Player " + chipType + " has won!");
                        System.out.println("Player " + chipType + " has won!");
                        break;
                    }

                    if(checkTie(board)){ //Check for a tie.
                        gameOver = true;
                        boardString = printBoard(board);
                        outputOne.writeBoolean(true);
                        outputTwo.writeBoolean(true);
                        outputOne.writeUTF(boardString);
                        outputTwo.writeUTF(boardString);
                        outputOne.writeUTF("It's a tie!");
                        outputTwo.writeUTF("It's a tie!");
                        System.out.println("It's a tie!");
                        break;
                    }

                    outputOne.writeBoolean(false); //It's not game over yet.
                    outputTwo.writeBoolean(false);
                    playerTurn = !playerTurn; //Tell the server it is the next clients turn.
                }
                else{
                    if(playerTurn){
                        outputOne.writeBoolean(false); //The column was full.
                        sentinel = false;
                    }
                    else if(!playerTurn){
                        outputTwo.writeBoolean(false);
                        sentinel = false;
                    }
                }
            }
        }
        catch(IOException e){
        }
    }

    /*
     * Drops a chip to the bottom most empty position in the column.
     */
    public static boolean dropChip(char[][] board, int columnPosition, char chipType){
        for(int i = 5; i >= 0; i--){
            if(board[i][columnPosition] == 0){
                board[i][columnPosition] = chipType;
                return true;
            }
        }
        return false;
    }

    /*
     * Prints the board and stores it in a string to send to the client.
     */
    public static String printBoard(char[][] board){
        String boardString = "";
        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 7; j++){
                if(board[i][j] == 0){
                    boardString = boardString.concat("| ");
                    System.out.print("| ");
                }
                else{
                    boardString = boardString.concat("|" + board[i][j]);                   
                    System.out.print("|" + board[i][j]);                    
                }
            }
            boardString = boardString.concat("|");
            System.out.println("|");
            boardString = boardString.concat("\n");
        }
        System.out.println();
        return boardString;
    }

    /*
     * Check in all four ways for a win: vertically, horizontally, major diagonal and minor diagonal.
     */
    public static boolean checkWin(char[][] board, int columnPosition, char chipType){
        //Before we can check horizontal, vertical and diagonal win, we must find out which row the chip was placed.
        int rowPosition = getRow(board, columnPosition);
        if(checkVertical(board, columnPosition, chipType, rowPosition)){
            return true;
        }
        if(checkHorizontal(board, columnPosition, chipType, rowPosition)){
            return true;
        }
        if(checkMajorDiagonal(board, columnPosition, chipType, rowPosition)){
            return true;
        }
        if(checkMinorDiagonal(board, columnPosition, chipType, rowPosition)){
            return true;
        }
        return false;
    }

    /*
     * Used to determine which row the chip landed from the most recent turn.
     */
    public static int getRow(char[][] board, int columnPosition){
        int count;
        for(count = 0; count < 6; count++){
            if(board[count][columnPosition] != 0){
                break;
            }
        }
        return count;
    }

    /*
     * Checks to see if there is a horizontal win.
     */
    public static boolean checkHorizontal(char[][] board, int columnPosition, char chipType, int rowPosition){
        int count = 1;
        for(int i = columnPosition - 1; i >= 0; i--){ //Check to the left.
            if(chipType == board[rowPosition][i]){
                count++;
            }
            else{
                break;
            }
        }
        if(count >=4){
            return true;
        }
        for(int i = columnPosition + 1; i < board[0].length; i++){ //Check to the right.
            if(chipType == board[rowPosition][i]){
                count++;
            }
            else{
                break;
            }            
        }
        if(count >= 4){
            return true;
        }
        return false;
    }

    /*
     * Check to see if there is a vertical win.
     */
    public static boolean checkVertical(char[][] board, int columnPosition, char chipType, int rowPosition){
        int count = 1;
        if((rowPosition+4) <= 6){
            for(int i = rowPosition + 1; i <= (rowPosition +3); i++){
                if(chipType == board[i][columnPosition]){
                    count++;
                }
                else
                    break;
            }
            if(count == 4){
                return true;
            }
        }
        return false;
    }

    /*
     * Check for a win in the major diagonal.
     */
    public static boolean checkMajorDiagonal(char[][] board, int columnPosition, char chipType, int rowPosition){
        int count = 1;
        for(int i = rowPosition - 1, j = columnPosition - 1; i >= 0 && j >= 0; i--, j--){ //Check the lower left part of the diagonal.
            if(chipType == board[i][j]){
                count++;                
            }
            else{
                break;
            }
        }
        if(count >= 4){
            return true;
        }
        for(int i = rowPosition + 1, j = columnPosition + 1; i < board.length && j < board[0].length; i++, j++){ //Check the upper right part of the diagonal.
            if(chipType == board[i][j]){
                count++;
            }
            else{
                break;
            }
        }
        if(count >= 4){
            return true;
        }
        return false;
    }

    /*
     * Check for a win in the minor diagonal.
     */
    public static boolean checkMinorDiagonal(char[][] board, int columnPosition, char chipType, int rowPosition){
        int count = 1;
        for(int i = rowPosition + 1, j = columnPosition - 1; i < board.length && j >= 0; i++, j--){ //Check the lower right part of the diagonal.
            if(chipType == board[i][j]){
                count++;
            }
            else{
                break;
            }
        }
        if(count >= 4){
            return true;
        }
        for(int i = rowPosition - 1, j = columnPosition + 1; i >= 0 && j < board[0].length; i--, j++){ //Check the upper left part of the diagonal.
            if(chipType == board[i][j]){
                count++;
            }
            else{
                break;
            }
        }
        if(count >= 4){
            return true;
        }
        return false;
    }

    /*
     * Check for a tie.
     */
    public static boolean checkTie(char[][] board){
        int count = 0;
        for(int i = 0; i < 7; i++){ //If the top row is completely filled before a tie, then it is a tie.
            if(board[0][i] != 0){
                count++;
            }
        }
        if(count == 7){
            return true;
        }
        return false;
    }
}
