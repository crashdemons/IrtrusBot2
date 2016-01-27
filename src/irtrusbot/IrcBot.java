
package irtrusbot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/** Class implementing the skeleton of controllable IRC bot or client with plugin support.
 *
 * @author crash
 */
public class IrcBot {
    private IrcPluginManager manager = new IrcPluginManager(this);
    /** Session object used for controlling the IRC connection
     * 
     */
    public IrcSession session = new IrcSession();
    /** The current state of the Bot/connection
     * 
     */
    public IrcState state=IrcState.DISCONNECTED;
    /** The previous state of the Bot/connection
     * 
     */
    public IrcState laststate=IrcState.DISCONNECTED;
    
    /** Add and initialize a plugin for the bot
     *  Plugins added at this point will be enabled and will receive Event messages from the bot.
     * @param plugin plugin class object to add.
     */
    public void addPlugin(IrcPlugin plugin){
        manager.addPlugin(plugin);
    }
    
    /** Requests that the Plugin Manager loads and adds all plugins found in the plugin directory
     * @return Number of plugins loaded
     * @throws URISyntaxException An error occurred while building the plugin directory path.
     */
    public int loadPlugins() throws URISyntaxException{
        return manager.loadPlugins();
    }
    
    
    /** Updates the internal IrcState of the bot
     * NOTE: this function also dispatches an Event message of type STATE to plugins with information about the previous and current state.
     * 
     * @param newstate the new state to store for the bot.
     * @throws Exception An error occurred while dispatching messages to plugins.
     */
    public void updateState(IrcState newstate) throws Exception{
        laststate=state;
        state=newstate;
        manager.postEvent(IrcEventType.STATE);
        laststate=state;
    }
    
    /** Connects to the IRC server
     * NOTE: updates the internal bot state to CONNECTED or DISCONNECTED depending on success.
     * @throws UnknownHostException The server hostname provided [to the session connection details] could not be resolved.
     * @throws Exception An error occurred while dispatching messages to plugins.
     */
    public void connect()  throws UnknownHostException,Exception{
        System.out.println("connect called");
        IrcState newstate=IrcState.DISCONNECTED;
        if(session.connect()) newstate=IrcState.CONNECTED;
        updateState(newstate);
    }
    
    /** Disconnects from the IRC server.
     * NOTE: updates the internal bot state to DISCONNECTED always.
     * @see #disconnect()
     * @throws IOException A networking error occurred.
     * @throws Exception An error occurred while dispatching messages to plugins.
     */
    public void disconnect()  throws IOException, Exception{
        session.disconnect();
        updateState(IrcState.DISCONNECTED);
    }
    /** Disconnects from the IRC server
     * NOTE: updates the internal bot state to QUIT always.
     * This state is used, as opposed to DISCONNECTED, to indicate a unrecoverable error or human-triggered disconnection, possibly to prevent reconnection.
     * @see #disconnect()
     * @throws IOException A networking error occurred.
     * @throws Exception An error occurred while dispatching messages to plugins.
     */
    public void quit() throws IOException, Exception{
        session.disconnect();
        updateState(IrcState.QUIT);
    }
    
    /** Queue the BOT_START message to plugins to indicate beginning of operations.
     * No Event messages should be sent/received before this event
     * This method should be used after: loading plugins, configuring  - but before: connecting, ticking, quitting
     */
    public void start(){
        manager.postEvent(IrcEventType.BOT_START);
    }
    /** Dispatch the BOT_STOP message to plugins to indicate end of operations.
     * No Event messages should be sent/received after this.
     * NOTE: this message is sent synchronously. All plugin handling must finish before continuing.
     * This method should be used before the bot instance is released.
     * @throws Exception An error occurred while dispatching plugin messages.
     */
    public void stop() throws Exception{
        manager.sendEvent(new IrcEvent(IrcEventType.BOT_STOP,laststate,state,null));//force synchronous/immediate sending of 
    }
    
    /** Queue sending a PRIVMSG [chat message] in reply to a received IrcMessage
     * This function also dispatches a CHAT and COMMAND (direction=SENDING) message to plugins.
     * Sending can canceled by a plugin returning the CANCEL_EVENT IrcEventAction in reply to either of these Event messages
     * @param im The IrcMessage received that it is desired to respond to.
     * @param text The message to respond with.
     * @param direct Whether the message should be sent directly to the original sender (true), or can be sent to an intermediate channel (false)
     */
    public void sendReply(IrcMessage im, String text, boolean direct) {
        IrcMessage imr=im.getReply(session.account, text, direct);
        sendMessage(imr);
    }

    /** Queue sending a PRIVMSG [chat message] to a specific destination
     * This function also dispatches a CHAT and COMMAND (direction=SENDING) message to plugins.
     * Sending can canceled by a plugin returning the CANCEL_EVENT IrcEventAction in reply to either of these Event messages
     * @param to The destination, this can be a Nickname or a Channel name (must start with a hash symbol #)
     * @param text The message to send
     */
    public void sendMessage(String to, String text) {
        IrcMessage im=new IrcMessage(session.account,to,text);
        sendMessage(im);
    }
    
    /** Queue sending a PRIVMSG [chat message] defined by an IrcMessage object.
     * This function also dispatches a CHAT and COMMAND (direction=SENDING) message to plugins
     * Sending can canceled by a plugin returning the CANCEL_EVENT IrcEventAction in reply to either of these Event messages
     * @param im The IrcMessage to send; this is formatted to "outgoing" syntax before transmitting.
     * @see IrcMessage#toOutgoing() 
     */
    public void sendMessage(IrcMessage im) {
        //IrcMessage im=new IrcMessage(session.account,to,text);
        IrcEvent event = new IrcEvent(IrcEventType.CHAT,laststate,state,null);
        event.message=im;
        event.direction=IrcDirection.SENDING;
        manager.postEvent(event);
        sendRaw(im.toOutgoing());//queue a COMMAND message also
    }
    
    /** Queue sending an IRC Command defined by an IrcCommand object
     * This function also dispatches a COMMAND (direction=SENDING) message to plugins
     * Sending can canceled by a plugin returning the CANCEL_EVENT IrcEventAction in reply to these Event messages
     * @param ic The IrcCommand to send.
     */
    public void sendCommand(IrcCommand ic) {
        IrcEvent event = new IrcEvent(IrcEventType.COMMAND,laststate,state,ic);
        event.direction=IrcDirection.SENDING;
        manager.postEvent(event);
    }
    
    /** Queue sending a raw IRC Command string to the server
     * This function also dispatches a COMMAND (direction=SENDING) message to plugins
     * Sending can canceled by a plugin returning the CANCEL_EVENT IrcEventAction in reply to these Event messages
     * @param line the raw IRC Command string to send
     */
    public void sendRaw(String line){
        IrcCommand ic=new IrcCommand(line);
        IrcEvent event = new IrcEvent(IrcEventType.COMMAND,laststate,state,ic);
        event.direction=IrcDirection.SENDING;
        manager.postEvent(event);
    }
    
    /** Perform repetitive tasks for the bot such as sending/receiving commands and handling disconnections.
     * This function should be used in a loop until the bot state becomes QUIT
     * Commands received are passed along to process_command()
     * Multiple event messages may be dispatched depending on the level of processing a received command receives.
     * @see #process_command
     * @throws IOException A networking error occurred.
     * @throws Exception An error occurred while sending messages to plugins.
     */
    public void poll() throws Exception{
        //process any queued events
        IrcEvent event=manager.events.poll();
        if(event!=null){
            IrcEventAction action = manager.sendEvent(event);
            //send any queued IRC Commands (outgoing)
            if(action!=IrcEventAction.CANCEL_EVENT && event.direction==IrcDirection.SENDING && event.type==IrcEventType.COMMAND){
                session.sendCommand(event.command);
            }
            
        }
        
        //process any waiting IRC Commands (incoming)
        if(session.isConnected()){
            IrcCommand ic=session.readCommand();
            if(ic!=null) process_command(ic);
        }else disconnect();
        
        
        
    }
    
    /** Process a received IRC Command
     * If the command can be determined to be a PRIVMSG [chat message], it is further processed by process_message()
     * This function also dispatches a COMMAND (direction=RECEIVING) message to plugins
     * The above additional processing can canceled by a plugin returning the CANCEL_EVENT IrcEventAction in reply to these Event messages
     * @param ic An object representing the IRC Command received.
     * @see #process_message
     * @throws IrcMessageCommandException A malformed PRIVMSG was received from the server.
     * @throws Exception An error occurred while sending messages to plugins.
     */
    public void process_command(IrcCommand ic) throws IrcMessageCommandException, Exception{
        //if(ic==null) System.out.println("COMMAND NULL AT PROCESS");
        manager.postEvent(IrcEventType.COMMAND,ic);
        if(ic.type.equals("PRIVMSG")){
            IrcMessage im = new IrcMessage(ic);
            process_message(im);
        }
        //additional command processing.
    }
    
    /** Processes an IRC PRIVMSG [chat message] command
     * This function dispatches a CHAT (direction=RECEIVING) message to plugins
     * Any additional processing can canceled by a plugin returning the CANCEL_EVENT IrcEventAction in reply to these Event messages (NOTE: none implemented at this time)
     * @param im An object representing the message to be processed.
     * @throws Exception An error occurred while sending messages to plugins.
     */
    public void process_message(IrcMessage im) throws Exception{
        IrcEvent event = new IrcEvent(IrcEventType.CHAT,laststate,state,null);
        event.message=im;
        manager.postEvent(event);
        //additional message processing.
    }
    
    /** Constructs the object and sets any important instance information in contained objects.
     * 
     */
    public IrcBot(){
        //manager.bot=this;
    }
}

/*
Process:

begin program
construct bot
add plugins
start()
...
poll()
...
stop()
exit program
*/