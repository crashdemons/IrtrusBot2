/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author crash
 */
public class IrcEventDispatcher {
    public IrcBot bot=null;
    public ArrayList<IrcPluginInterface> plugins=new ArrayList<IrcPluginInterface>();
    public boolean dispatch(IrcEventType t)  throws Exception{
        return dispatch(t,null);
    }
    public boolean dispatch(IrcEventType t, IrcCommand ic)  throws Exception
    {
        //if(ic==null && t==IrcEventType.COMMAND) System.out.println("COMMAND NULL AT DISPATCHER1");
        return dispatch(new IrcEvent( t, bot.laststate,  bot.state, ic));
    }
    public boolean dispatch(IrcEvent event)  throws Exception {
        //if(event.command==null && event.type==IrcEventType.COMMAND) System.out.println("COMMAND NULL AT DISPATCHER2");
        for (IrcPluginInterface plugin : plugins){
            if(plugin!=null){
                IrcEventAction action = plugin.handleEvent(event);
                if(action==IrcEventAction.STOP_PROPAGATING) return true;
                if(action==IrcEventAction.CANCEL_EVENT) return false;
            }
        }
        return true;
    }
    public void addPlugin(IrcPluginInterface plugin){
        plugin.initialize(bot.session,bot,this);
        plugins.add(plugin);
    }
    
}
