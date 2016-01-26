package irtrusbot;
import java.util.Random;

/** Main bot class, used to load the Bot, plugins, and run the poll loop.
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
        System.out.println("Loading IrtrusBot...");
        IrcBot bot = new IrcBot();
        if(bot.loadPlugins()==0) System.out.println("Warning: No plugins have been loaded - IrtrusBot will perform no actions by default without connection control plugin.");
        System.out.println("Starting IrtrusBot...");
        bot.start();
        while( bot.state!=IrcState.QUIT)
            bot.poll();
        System.out.println("Stopping IrtrusBot...");
        bot.stop();
    }


}
