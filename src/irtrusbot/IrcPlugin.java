
package irtrusbot;

import java.util.Properties;

/** Skeleton/parent class for all IrcBot plugins
 * If you are writing your own plugin, your plugin class should extend IrcPlugin and provide an override of the handleEvent() method.
 * Developers should make their plugin class the "Main-Class" of their project (entry-point).
 *
 * @author crash
 */
public class IrcPlugin {
    /** The IRC session object used by the bot - Provides control over the bot connection to the plugin.  */
    public IrcSession session=null;
    /** The bot object the plugin is attached to - Provides control over specific bot operations to the plugin. */
    public IrcBot bot=null;
    /** The Plugin Manager controlling the plugin - Provides inter-plugin communication support. */
    private IrcPluginManager manager=null;
    
    
    
    
    /** 
     * The priority level of the plugin
     *  This value determines the order in which the plugin receives event, and thus can filter events from being broadcast to other plugins
     *  The default value of this property is DEFAULT which receives filtered preprocessed events
     *  NOTE: plugins added with the same priority will be adjusted in priority value to maintain the order in which they were loaded.
     * @see IrcPluginPriority
     */
    public int priority=IrcPluginPriority.DEFAULT;
    
    /**
     * Properties for the plugin [retrieved from configDirectory/pluginname.properties]
     */
    public Properties config=new Properties();
    /**
     * Default properties set by the plugin at Construction time which can be overridden by the config file.
     */
    public Properties defaults=new Properties();
    
    
    /** Whether the plugin is enabled or disabled by the Plugin Manager */
    public boolean enabled=true;
    
    /** 
     * Name of the plugin - This should be set in subclasses by the constructor
     * Generally it is suggested to use an easy to type name as this is used with IrcPluginManagerfindPlugin(name) 
     * NOTE: any Non-"word" characters [A-Za-z0-9_] will be replaced by _ during plugin initialization.
     * @see IrcPluginManager#findPlugin(java.lang.String)
     */
    public String name="";
    /**
     * Version string of the plugin - unused
     */
    public String version="";
    /**
     * Description string of the plugin - unused
     */
    public String description="";
    
    
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
        loadConfig();
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
    /**
     * Method used by PluginManager to start and time-limit plugin handling inside a thread
     * @param event the event to be handled by the plugin
     * @param max_seconds the maximum number of seconds the thread is allowed to run
     * @return an action value specifying how the PluginManager and Bot should continue (see handleEvent
     * @see IrcPlugin#handleEvent(irtrusbot.IrcEvent)
     */
    public final IrcEventAction startHandler(IrcEvent event, int max_seconds){
        //set conditions for handler
        IrcPluginThread thread = new IrcPluginThread(this,event);
        //System.out.println("Starting thread{");
        thread.start();
        try{
            //wait for N seconds before continuing this thread
            thread.join(max_seconds*1000);
            if(thread.isAlive()){
                //if the thread is not dead, kill it.
                thread.interrupt();
                System.out.println("Plugin timeout exceeded for "+name+" - killing thread.");
            }
        }catch(SecurityException e){
                System.out.println("ERROR: Could not kill plugin thread.");
        }catch(Exception e){
            //we don't really want to handle an exception, but we cant let plugin thread interruptions kill the program.
        }
        return thread.getAction();
    }
    

    
    /** Post an event to all other loaded plugins in the attached Plugin Manager, if any.
     * @param event Event to post.
     */
    public void postEvent(IrcEvent event){
        if(manager!=null){
            manager.postEvent(event);
        }
    }
    
    /** Post an event to all other loaded plugins
     * NOTE: this method changes the priority levels of the event to be all-inclusive.
     * @param event Event to post.
     */
    public void postEventAll(IrcEvent event){
        if(manager!=null){
            event.priority_min=IrcPluginPriority.MIN;
            event.priority_min=IrcPluginPriority.MAX;
            manager.postEvent(event);
        }
    }
    
    /** Post an event to all other loaded plugins with the next priority number (may be in the same level)
     * NOTE: this method changes the priority levels of the event to include all values in the range [current+1,max], including only plugins that would normally receive messages after the current plugin.
     * @param event Event to post.
     */
    public void postEventNext(IrcEvent event){
        if(manager!=null){
            event.priority_min=priority+1;
            event.priority_min=IrcPluginPriority.MAX;
            manager.postEvent(event);
        }
    }
    
    /** Post an event to be handled by the bot rather than plugins.
     * By default, events are usually handled by the bot (such as outgoing IRC commands) after plugins are allowed to process them.
     * This method allows sending an event that will bypass most plugins.
     * NOTE: this method changes the priority levels of the event to the range [BOT,BOT], excluding all plugins.
     * @param event Event to post.
     */
    public void postEventBot(IrcEvent event){
        if(manager!=null){
            event.priority_min=IrcPluginPriority.BOT;
            event.priority_min=IrcPluginPriority.BOT;
            manager.postEvent(event);
        }
    }
    
    /**
     * Loads configuration options for this plugin into 'config' from the config file - 
     * If no properties were previously saved, this will create a blank properties object
     */
    public void loadConfig(){
        if(manager!=null){
            config=manager.loadPluginProperties(name,defaults);
            if(config==null) config=new Properties();
        }
    }
    
    /**
     * Saves configuration options for this plugin from 'config' to the config file -
     * If no 'config' is unset (loadConfig never called), this method will not attempt to save anything
     */
    public void saveConfig(){
        if(manager!=null && config!=null){
            manager.savePluginProperties(name,config);
        }
    }
    
    
}
