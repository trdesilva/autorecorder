package io.github.trdesilva.autorecorder;

import io.github.trdesilva.autorecorder.ui.cli.MainCli;

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
        
        MainCli cli = new MainCli(settings);
        cli.run();
        
        listener.stop();
    }
}
