package io.github.trdesilva.autorecorder.ui.cli;

import io.github.trdesilva.autorecorder.Settings;

import java.util.Scanner;

public abstract class Cli
{
    protected Scanner scanner = new Scanner(System.in);
    protected Settings settings;
    
    public Cli(Settings settings)
    {
        this.settings = settings;
    }
    
    public abstract void run();
    
    protected void print(String message, Object... args)
    {
        System.out.printf(message + "\n", args);
    }
    
    protected String readLine()
    {
        return scanner.nextLine().strip();
    }
    
    protected String chooseFromList(String prompt, String... options)
    {
        print(prompt);
        print("0: exit");
        int optionNumber = 1;
        for(String option : options)
        {
            print("%d: %s", optionNumber++, option);
        }
        
        while(true)
        {
            String answer = readLine();
            try
            {
                int number = Integer.parseInt(answer);
                if(number == 0)
                {
                    return "exit";
                }
                else if(0 < number && number <= options.length)
                {
                    return options[number - 1];
                }
                print("Not a valid option");
            }
            catch(NumberFormatException e)
            {
                print("Not a valid number");
            }
        }
    }
}
