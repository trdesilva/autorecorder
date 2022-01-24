package io.github.trdesilva.autorecorder;

import java.util.Scanner;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("starting");
        Settings settings = new Settings();
        settings.populate();
        Obs obs = new Obs(settings);
        GameListener listener = new GameListener(obs, settings);
        listener.start();
        System.out.println("listening for games, press enter to stop");
        new Scanner(System.in).nextLine();
        listener.stop();
    }
}
