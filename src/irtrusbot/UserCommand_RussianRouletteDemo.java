/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

import java.util.Random;

/**
 *
 * @author crash
 */
public class UserCommand_RussianRouletteDemo extends Plugin_UserCommands_CommandHandler {
    static private int rr_chamber=0;
    static private int rr_bulletchamber=-1;
    static private final Random rand = new Random();
    
    @Override
    public void handleUserCommand(IrcPlugin plugin, Plugin_UserCommands_Command userCommand,IrcEvent event) throws Exception{
        String response = russianRouletteFire();
        plugin.bot.sendReply(event.message, response, false);
    }
    public UserCommand_RussianRouletteDemo(){
        name="rr";
        help="Take a shot with Russian Roulette chat style! Use this command to pull the trigger and advance the barrel.";
        russianRouletteSpin();
    }
     private static void russianRouletteSpin(){//randomize the bullet placement for a round of russian roulette.
        rr_bulletchamber=rand.nextInt(6);// range is [0,6)      .
    }
    private static String russianRouletteFire()
    {
        if(rr_bulletchamber==-1) russianRouletteSpin();//uninitialized chamber # on first firing.
        if(rr_chamber==rr_bulletchamber){
            russianRouletteSpin();//randomize rr_bulletchamber for the next around
            rr_chamber=0;
            return "Bang! You were shot. <Loads another round and spins the chambers>";
        }
        rr_chamber=(rr_chamber+1)%6;//increment the firing chamber with 0-5 wraparound (for next attempt)
        return "Click! You were lucky... this time.";
    }
    
}
