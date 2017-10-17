
/**
 * This is a class to hold money for a client.
 * 
 * @author Christopher Stump 
 * @version 10/7/2016
 */
public class Account
{
    private static double balance;
    /*
     * Every new client starts out with $0 in their account.
     */
    public Account()
    {
        balance = 0;
    }
    /*
     * This method is used when a client wants to deposit money into their account.
     */
    public static double desposit(double amount){
        balance += amount;
        return balance;
    }
    /*
     * This method is used when a client wants to withdraw money from their acocunt.
     */
    public static double withdraw(double amount){
        balance -= amount;
        return balance;
    }
    /*
     * This method is used to retrieve the clients current balance.
     */
    public static double getBalance(){
        return balance;
    }
}
