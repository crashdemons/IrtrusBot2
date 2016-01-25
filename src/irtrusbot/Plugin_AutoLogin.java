/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

import java.util.ArrayList;

/**
 *
 * @author crash
 */
public class Plugin_AutoLogin extends IrcPlugin {
    
    public boolean doAutoJoin=true;
    public boolean doAutoReconnect=true;
    ArrayList<String> channels = new ArrayList<String>();
    
    
    public IrcEventAction handleCommand(IrcEvent event,IrcState state,IrcCommand ic) throws Exception{
        System.out.println(ic.type+" | STATE: "+state.toString());
        if(state==IrcState.LOGIN_WAIT){
            IrcLoginState check = bot.session.logincheck(ic);
            System.out.println(ic.type+" | LOGINSTATE: "+check.toString());
            switch(check)
            {
                case SUCCESS:
                    bot.updateState(IrcState.LOGGED_IN);
                    break;
                case FAILURE:
                    bot.quit();
                    break;
            }
            
        }
        return IrcEventAction.CONTINUE;
    }
    public IrcEventAction handleStateChange(IrcEvent event,IrcState state) throws Exception{
        switch(state){
            case CONNECTED:
               bot.session.login();
               bot.updateState(IrcState.LOGIN_WAIT);
               break;
            case LOGGED_IN://TODO: send userinit to capture full origin string for self!
                if(doAutoJoin)
                    for(String channel : channels){
                        bot.sendRaw("JOIN "+channel);
                    }
                    bot.updateState(IrcState.JOINED);
                break;
            case DISCONNECTED:
               if(doAutoReconnect) bot.connect();
               break;
        }
        return IrcEventAction.CONTINUE;
    }
    
    
    @Override
    public IrcEventAction handleEvent(IrcEvent event) throws Exception
    {
        if(event.type==IrcEventType.STATE && event.state==event.lastState) ;
        else System.out.println("EVENT: "+event.type.toString()+" "+event.lastState.toString()+" "+event.state.toString());
        
        switch(event.type){
            case BOT_START:
                bot.connect();
                break;
            case STATE:
                if(event.state!=event.lastState)
                    return handleStateChange(event,event.state);
                break;
            case COMMAND:
                if(event.command!=null) return handleCommand(event,event.state,event.command);
                else System.out.println("COMMAND NULL");
                break;
            case BOT_STOP:
                bot.session.disconnect();
                break;
        }
        
        return IrcEventAction.CONTINUE;
    }
    
}
