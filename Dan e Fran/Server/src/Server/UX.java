package Server;

import java.util.Scanner;

public class UX 
{
    static Scanner scanner = new Scanner(System.in);
    
    public static int initMenu()
    {
        System.out.println("Welcome to Bunny Server!");
        System.out.println("\t1 - Start as main server;\n"
                    + "\t2 - Start as secondary server;\n"
                    + "\t3 - Exit.");

    return getIntOpt(1,3);
    }
    
    public static String askGeneric(String str)
    {
        System.out.println( str );
        
        return scanner.nextLine();
    }
    
    public static int getIntOpt(int min, int max)
    {
        boolean done = false;
        int input = max;
        
        System.out.println("Insert a number between " + min + " and " + max + ".\n");
        
        do
        {
            if (!scanner.hasNextInt())
            {
                System.out.println("Please insert an integer value.");
                scanner.nextLine();
            }
            else
            {
                input = scanner.nextInt();
                if (input < min || input > max)
                    System.out.println("The value must be between " + min + " and " + max + ".");
                else
                    done = true;            
            }                
        }while(!done);
        scanner.nextLine();
        return input;
    }
}
