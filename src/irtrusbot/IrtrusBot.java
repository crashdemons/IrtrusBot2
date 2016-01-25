package irtrusbot;
import java.util.Random;

/** Main bot class, used for testing the library
 * @author Surtri [Surtri at irc.freenode.net]
 * @author crashdemons
 */
public class IrtrusBot
{

    
    /** Entry point for the process
     * 
     * @param args command-line arguments (unused)
     * @throws Exception .
     */
    
    public static void main(String[] args) throws Exception
    {
        IrcBot bot = new IrcBot();
        
        Plugin_AutoLogin login = new Plugin_AutoLogin();
        login.channels.add("#IrtrusBot2");
        login.channels.add("#cicada");
        login.channels.add("##426699k");
        
        Plugin_UserCommands commands = new Plugin_UserCommands();
        commands.prefix="+";
        UserCommand_RussianRouletteDemo rr=new UserCommand_RussianRouletteDemo();
        commands.add(rr);
        
        bot.addPlugin(login);
        bot.addPlugin(commands);
        bot.session.setAccountDetails("IrtrusBot2_1","IrtrusBot2","Java Hacks Bot","");//no password=""
        bot.session.setConnectionDetails("irc.freenode.net", 6667);
        bot.start();
        
        //Plugin_UserCommands_Command userCmd = new Plugin_UserCommands_Command("x");
        //System.out.println(userCmd.name);
        
       
        while(bot.state!=IrcState.QUIT)
            bot.tick();
        bot.stop();
    }


}




/*
Debugging: function to print every property of an object:
import java.lang.reflect.*;

        public static void debugObject(Object obj){
            StringBuilder sb = new StringBuilder();
            sb.append(obj.getClass().getName());
            sb.append(": ");
            for (Field f : obj.getClass().getDeclaredFields()) {
                sb.append(f.getName());
                sb.append("=");
                try{
                    sb.append(f.get(obj));
                }catch(IllegalAccessException e){
                    sb.append("***PRIVATE***");
                }
                sb.append(", ");
            }
            System.out.println(sb.toString());
        }
*/