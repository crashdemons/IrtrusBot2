package irtrusbot;

import java.util.HashMap;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author crash
 */
public class Plugin_UserCommands extends IrcPlugin  {
    public String prefix="+";
    public Map<String,Plugin_UserCommands_CommandHandler> commands=new HashMap<String,Plugin_UserCommands_CommandHandler>();
    
    
    public void add(Plugin_UserCommands_CommandHandler handler){
        commands.put(handler.name, handler);
    }
    public void handleUnknownCommand(Plugin_UserCommands_Command userCommand,IrcEvent event) throws Exception {
        bot.sendReply(event.message,"Unknown command: "+userCommand.name,false);
    }
    
    
    public IrcEventAction handleUserCommand(IrcMessage im, IrcEvent event) throws Exception
    {
        //commands.
        String rawCmd=im.text;
        rawCmd=rawCmd.substring(prefix.length());
        Plugin_UserCommands_Command userCmd = new Plugin_UserCommands_Command(rawCmd);
        Plugin_UserCommands_CommandHandler handler = commands.get(userCmd.name);
        if(handler==null) handleUnknownCommand(userCmd,event);
        else handler.handleUserCommand(this, userCmd, event);
        return IrcEventAction.CONTINUE;
    }
    
    @Override
    public IrcEventAction handleEvent(IrcEvent event) throws Exception
    {
        if(event.type==IrcEventType.CHAT && event.direction==IrcDirection.RECEIVING)
            if(event.message!=null)
                if(event.message.text.startsWith(prefix))
                    return handleUserCommand(event.message,event);
        return IrcEventAction.CONTINUE;
    }
}
