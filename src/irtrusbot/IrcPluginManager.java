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
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/** Class used for managing and communicating with various plugins
 *
 * @author crash
 */
public class IrcPluginManager  {
    private IrcBot bot=null;
    private ArrayList<IrcPlugin> plugins=new ArrayList<IrcPlugin>();
    
    /** Construct the class and include an instance of the Bot constructing it
     * 
     * @param b The Bot object owning the plugins.
     */
    public IrcPluginManager(IrcBot b){
        bot=b;
    }
    
    /** Transmit a blank IrcEvent message to plugins with only the Type specified.
     * Useful for sending events that do not have attached data.
     * 
     * @param t The event type
     * @return true: None of the plugins receiving the event requested the action spawning it be canceled (ie: CANCEL_EVENT) | false: A plugin requested the action be canceled | NOTE: this does not account for use of STOP_PROPAGATION.
     * @throws Exception An error occurred within a plugin event handler.
     */
    public boolean dispatch(IrcEventType t)  throws Exception{
        return dispatch(t,null);
    }
    /** Transmit an IrcEvent message to plugins with the Type and IRC Command specified.
     * Generally used for COMMAND and CHAT events
     * 
     * @param t The event type
     * @param ic The IRC Command to send.
     * @return true: None of the plugins receiving the event requested the action spawning it be canceled (ie: CANCEL_EVENT) | false: A plugin requested the action be canceled | NOTE: this does not account for use of STOP_PROPAGATION.
     * @throws Exception An error occurred within a plugin event handler.
     */
    public boolean dispatch(IrcEventType t, IrcCommand ic)  throws Exception
    {
        //if(ic==null && t==IrcEventType.COMMAND) System.out.println("COMMAND NULL AT DISPATCHER1");
        return dispatch(new IrcEvent( t, bot.laststate,  bot.state, ic));
    }
    
    /** Transmit an IrcEvent message object to plugins.
     * 
     * @param event The event object containing fields describing the event.
     * @return true: None of the plugins receiving the event requested the action spawning it be canceled (ie: CANCEL_EVENT) | false: A plugin requested the action be canceled | NOTE: this does not account for use of STOP_PROPAGATION.
     * @throws Exception An error occurred within a plugin event handler.
     */
    public boolean dispatch(IrcEvent event)  throws Exception {
        //if(event.command==null && event.type==IrcEventType.COMMAND) System.out.println("COMMAND NULL AT DISPATCHER2");
        for (IrcPlugin plugin : plugins){
            if(plugin!=null){
                IrcEventAction action = plugin.handleEvent(event);
                if(action==IrcEventAction.STOP_PROPAGATING) return true;
                if(action==IrcEventAction.CANCEL_EVENT) return false;
            }
        }
        return true;
    }
    
    /** Add a plugin object instance to the plugins enabled for the bot.
     * The plugin will receive events dispatched by the Plugin Manager afterwards.
     * @param plugin The plugin object to communicate with.
     */
    public void addPlugin(IrcPlugin plugin){
        plugin.initialize(bot.session,bot,this);
        plugins.add(plugin);
    }
    
    /** Search the plugins directory [relative to the bot jar/class] for plugin JARs and load+enable them
     * 
     * @throws URISyntaxException An error occurred while constructing the plugin directory path.
     */
    public void loadAll() throws URISyntaxException {
        
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
                                plugins.add(plug);
                            }
                        }else{
                            System.out.println("   Main-Class attribute for plugin not set. Cannot load.");
                        }
                        
                        
                        

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{ System.out.println("plugin directory not found - skipping plugins."); }
        System.out.println("loading plugins done.");
    }
    
}
