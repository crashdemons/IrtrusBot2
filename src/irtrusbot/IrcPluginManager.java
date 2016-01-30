/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/** Class used for managing and communicating with various plugins
 *
 * @author crash
 */
public class IrcPluginManager {
    
    
    private IrcBot bot=null;
    private ConcurrentHashMap<String,IrcPlugin> plugins=new ConcurrentHashMap<String,IrcPlugin>();
    private ConcurrentSkipListMap<Integer,IrcPlugin> plugins_priority=new ConcurrentSkipListMap<Integer,IrcPlugin>();
    ConcurrentLinkedQueue<IrcEvent> events = new ConcurrentLinkedQueue<IrcEvent>();
    
    /** Construct the class and include an instance of the Bot constructing it
     * 
     * @param b The Bot object owning the plugins.
     */
    public IrcPluginManager(IrcBot b){
        bot=b;
    }
    
    /** Queues a blank IrcEvent message for processing with only the Type specified.
     * Useful for sending events that do not have attached data.
     * 
     * @param t The event type
     */
    public void postEvent(IrcEventType t) {
        postEvent(t,null);
    }
    /** Queues an IrcEvent message for processing with the Type and IRC Command specified.
     * Generally used for COMMAND and CHAT events
     * 
     * @param t The event type
     * @param ic The IRC Command to send.
     */
    public void postEvent(IrcEventType t, IrcCommand ic)
    {
        //if(ic==null && t==IrcEventType.COMMAND) System.out.println("COMMAND NULL AT DISPATCHER1");
        postEvent(new IrcEvent( t, bot.laststate,  bot.state, ic));
    }
    
    /** Queues an IrcEvent message object for processing.
     * 
     * @param event The event object containing fields describing the event.
     */
    public void postEvent(IrcEvent event) {
        events.add(event);
    }
    
    /** Synchronously processes an IrcEvent object against plugins
     * This method should generally not be used by plugins if it can be avoided. Prefer postEvent() instead.
     * PLUGIN event messages will cause the plugin to be enabled or disabled within the time of this function call.
     * [WARNING: This should NOT be called by a plugin or thread]
     * @param event Event to be processed
     * @return The event action indicated by plugin(s). CANCEL_EVENT implies higher post-processing (such as data transmission actions) should be canceled.
     * @throws Exception An error occurred within a plugin event handler.
     * @see #postEvent(IrcEvent)
     */
    public IrcEventAction sendEvent(IrcEvent event)  throws Exception {
        for (Map.Entry<Integer,IrcPlugin> entry : plugins_priority.entrySet()){
            Integer priority=entry.getKey();
            if(priority<event.priority_min || priority>event.priority_max) continue;//do not send events to plugins not in the correct priority range.
            IrcPlugin plugin=entry.getValue();
            if(plugin!=null){
                String name=plugin.name;
                if(event.type==IrcEventType.PLUGIN_ENABLED && name.equals(event.sdata)) plugin.enabled=true;
                if(plugin.enabled){
                    IrcEventAction action = plugin.startHandler(event,5);//5 secs, we can make this configurable.
                    if(action==IrcEventAction.STOP_PROPAGATING || action==IrcEventAction.CANCEL_EVENT) return action;
                }
                if(event.type==IrcEventType.PLUGIN_DISABLED && name.equals(event.sdata)) plugin.enabled=false;
            }
        }
        return IrcEventAction.CONTINUE;
    }
    
    
    
    /** 
     Find a plugin object instance by the plugin name
     
     This only searches loaded plugins.
     @param name_search The plugin name to check for.
     @return The instance of the matching plugin is returned, otherwise 'null' is returned for no match.
     */
    public IrcPlugin findPlugin(String name_search){
        for (Map.Entry<String,IrcPlugin> entry : plugins.entrySet()){
            String name=entry.getKey();
            if(name_search.equals(name)) return entry.getValue();
        }
        return null;
    }
   
    /** Queue a request for a plugin to be enabled
     * When the plugin is enabled, it will receive Events (all plugins are by default enabled)
     * NOTE: this takes effect when the PLUGIN message is "sent" next, not immediately.
     * @param name Name of the plugin to enable (corresponds to IrcPlugin.name)
     */
    public void enablePlugin(String name){
        IrcEvent event = new IrcEvent(IrcEventType.PLUGIN_ENABLED,null,null,null);
        event.sdata=name;
        postEvent(event);
    }
    
    /** Queue a request for a plugin to be disabled
     * When the plugin is disabled, it will not receive Events (all plugins are by default enabled)
     * NOTE: this takes effect when the PLUGIN message is "sent" next, not immediately.
     * @param name Name of the plugin to disable (corresponds to IrcPlugin.name)
     */
    public void disablePlugin(String name){
        IrcEvent event = new IrcEvent(IrcEventType.PLUGIN_DISABLED,null,null,null);
        event.sdata=name;
        postEvent(event);
        
    }
    
    /** Add a plugin object instance to the plugins enabled for the bot.
     * The plugin will receive events dispatched by the Plugin Manager afterwards.
     * @param plugin The plugin object to communicate with.
     */
    public void addPlugin(IrcPlugin plugin){
        plugin.initialize(bot.session,bot,this);
        plugins.put(plugin.name, plugin);
        
        if(plugin.priority<IrcPluginPriority.PLUGIN_MIN) plugin.priority=IrcPluginPriority.PLUGIN_MIN;
        if(plugin.priority>IrcPluginPriority.PLUGIN_MAX) plugin.priority=IrcPluginPriority.PLUGIN_MAX;
        
        while(plugins_priority.get(plugin.priority)!=null) plugin.priority++;
        plugins_priority.put(plugin.priority,plugin);
    }
    
    /** Search the plugins directory [relative to the bot jar/class] for plugin JARs and load+enable them
     * @return number of plugins loaded.
     * @throws URISyntaxException An error occurred while constructing the plugin directory path.
     */
    public int loadPlugins() throws URISyntaxException {
        int total=0;
        File path = new File(IrtrusBot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        if(path.isFile()) path=path.getParentFile();//jar file path conditionally.
        String pluginpath=path.toString()+File.separator+"plugins";
        System.out.println("loading plugins: "+pluginpath);
        File plugDir = new File(pluginpath);
        if (plugDir.exists() && plugDir.isDirectory()) {
            for (File plugFile : plugDir.listFiles()) {
                if (plugFile.exists() && plugFile.isFile() && plugFile.getName().endsWith(".jar")) {
                    try {
                        System.out.println("   reading "+plugFile);
                        JarFile jarfile = new JarFile(plugFile);
                        Attributes attrs = jarfile.getManifest().getMainAttributes();
                        String class_name = "";
                        try{class_name=attrs.getValue("Main-Class");}catch(Exception e){ /* No attribute existed, do not change class_name*/ }
                        if(class_name.length()>0){
                            URL[] urls = new URL[] {plugFile.toURI().toURL()};
                            ClassLoader loader = new URLClassLoader(urls);
                            System.out.println("   loading "+class_name);
                            Class<?> c = loader.loadClass(class_name);
                            Object plugInst = c.newInstance();
                            if (plugInst instanceof IrcPlugin) {
                                IrcPlugin plug = (IrcPlugin) plugInst;
                                addPlugin(plug);
                                total++;
                            }
                        }else{
                            System.out.println("   Main-Class attribute for plugin not set. Cannot load.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }else System.out.println("plugin directory not found - skipping plugins.");
        System.out.println("loading plugins done.");
        return total;
    }
    
}
