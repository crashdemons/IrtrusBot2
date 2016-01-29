
package irtrusbot;

/** Skeleton/parent class for all IrcBot plugins
 * If you are writing your own plugin, your plugin class should extend IrcPlugin and provide an override of the handleEvent() method.
 * Developers should make their plugin class the "Main-Class" of their project (entry-point).
 *
 * @author crash
 */
public class IrcPlugin extends Thread {
    /** The IRC session object used by the bot - Provides control over the bot connection to the plugin.  */
    public IrcSession session=null;
    /** The bot object the plugin is attached to - Provides control over specific bot operations to the plugin. */
    public IrcBot bot=null;
    /** The Plugin Manager controlling the plugin - Provides inter-plugin communication support. */
    IrcPluginManager manager=null;
    
    /** Whether the plugin is enabled or disabled by the Plugin Manager */
    public boolean enabled=true;
    
    public String name="";
    public String version="";
    public String description="";
    
    IrcEvent currentEvent=null;
    IrcEventAction lastAction=null;
    
    /** A dummy method provided so that developers can set their plugin class as the "Main-Class" (entry-point) of their program without receiving errors.
     * 
     * @param args Program arguments (unused)
     */
    public static void main(String[] args){;}//unused.
    
    /**
     * Method used by the Plugin Manager to set instance fields in the plugin [to provide control to the plugin] during loading.
     * @param sess The IrcSession object controlling the bot connection.
     * @param b The Bot object instance.
     * @param disp The Plugin Manager object instance.
     */
    public final void initialize(IrcSession sess,IrcBot b,IrcPluginManager disp){
        session=sess;
        bot=b;
        manager=disp;
    }
    
    /** IrcEvent message handler.
     * This method receives Events dispatched by through Plugin Manager (by the Bot and other plugins)
     * 
     * @param event Event being transmitted to the plugin
     * @return an action value specifying how the PluginManager and Bot should continue | CONTINUE: continue to dispatch events to other plugins and take no special action | STOP_PROPAGATING: stop dispatching this event to other plugins and take no special action | CANCEL_EVENT: stop dispatching this event and attempt to cancel the action that dispatched the event (eg: sending data).
     * @throws Exception An unspecified error caused by the plugin developer's code.
     */
    public IrcEventAction handleEvent(IrcEvent event) throws Exception
    {
        return IrcEventAction.CONTINUE;
    }
    
    //wrapper to call the threaded version of the event handler.
    //basically, we store parameters into properties, invoke the thread, which passes the properties as arguments.
    //then we wait a set time before killing the thread.
    public IrcEventAction startHandler(IrcEvent event, int max_seconds){
        //set conditions for handler
        currentEvent=event;
        //set the default action
        lastAction=IrcEventAction.CONTINUE;
        start();
        try{
            join(max_seconds*1000);
            if(this.isAlive()){
                interrupt();
                System.out.println("Plugin timeout exceeded for "+name+" - killing thread.");
            }
        }catch(SecurityException e){
                System.out.println("ERROR: Could not kill plugin thread.");
        }catch(Exception e){
            //we don't really want to handle an exception, but we cant let plugin thread interruptions kill the program.
        }
        return lastAction;
    }
    
    //code for the new thread,
    //take our preset arguments from outside the thread (currentEvent, lastAction) and use them.
    @Override
    public void run(){
        try{
            lastAction=handleEvent(currentEvent);
        }catch(Exception e){
            //we don't care what exception an arbitrary plugin generated, frankly.
            //maybe we could implement logging here.
        }
    }
    
    
    
}
