/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author crash
 */


/*
Process:

begin program
construct bot
add plugins
start()
...
tick()
...
stop()
exit program
*/
public class IrcBot {
    IrcEventDispatcher dispatcher = new IrcEventDispatcher();
    public IrcSession session = new IrcSession();
    public IrcState state=IrcState.DISCONNECTED;
    public IrcState laststate=IrcState.DISCONNECTED;
    
    public void addPlugin(IrcPluginInterface plugin){
        dispatcher.addPlugin(plugin);
    }
    
    public void updateState(IrcState newstate) throws Exception{
        laststate=state;
        state=newstate;
        dispatcher.dispatch(IrcEventType.STATE);
        laststate=state;
    }
    public void connect()  throws Exception{
        System.out.println("connect called");
        IrcState newstate=IrcState.DISCONNECTED;
        if(session.connect()) newstate=IrcState.CONNECTED;
        updateState(newstate);
    }
    public void disconnect()  throws Exception{
        session.disconnect();
        updateState(IrcState.DISCONNECTED);
    }
    public void quit()  throws Exception{
        session.disconnect();
        updateState(IrcState.QUIT);
    }
    public void start() throws Exception{
        dispatcher.dispatch(IrcEventType.BOT_START);
    }
    public void stop() throws Exception{
        dispatcher.dispatch(IrcEventType.BOT_STOP);
    }
    
    public void sendReply(IrcMessage im, String text, boolean direct)  throws Exception{
        IrcMessage imr=im.getReply(session.account, text, direct);
        sendMessage(imr);
    }
    public void sendMessage(String to, String text)  throws Exception{
        IrcMessage im=new IrcMessage(session.account,to,text);
        sendMessage(im);
    }
    public void sendMessage(IrcMessage im) throws Exception{
        //IrcMessage im=new IrcMessage(session.account,to,text);
        IrcEvent event = new IrcEvent(IrcEventType.CHAT,laststate,state,null);
        event.message=im;
        event.direction=IrcDirection.SENDING;
        if(!dispatcher.dispatch(event)) return;
        sendRaw(im.toOutgoing());
    }
    public void sendCommand(IrcCommand ic) throws Exception{
        String line=ic.toString();
        sendRaw(ic.toString());
    }
    public void sendRaw(String line) throws Exception{
        IrcCommand ic=new IrcCommand(line);
        IrcEvent event = new IrcEvent(IrcEventType.COMMAND,laststate,state,ic);
        event.direction=IrcDirection.SENDING;
        if(!dispatcher.dispatch(event)) return;
        session.sendRawLine(line);
    }
    public void tick() throws Exception{
        if(session.isConnected()){
            IrcCommand ic=session.readCommand();
            if(ic!=null) process_command(ic);
        }else disconnect();
    }
    
    public void process_command(IrcCommand ic) throws Exception{
        //if(ic==null) System.out.println("COMMAND NULL AT PROCESS");
        if(!dispatcher.dispatch(IrcEventType.COMMAND,ic)) return;
        if(ic.type.equals("PRIVMSG")){
            IrcMessage im = new IrcMessage(ic);
            process_message(im);
        }
        //additional command processing.
    }
    public void process_message(IrcMessage im) throws Exception{
        IrcEvent event = new IrcEvent(IrcEventType.CHAT,laststate,state,null);
        event.message=im;
        if(!dispatcher.dispatch(event)) return;
        //additional message processing.
    }
    public IrcBot(){
        dispatcher.bot=this;
    }
}
