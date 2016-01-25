/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

/**
 *
 * @author crash
 */
public class IrcPlugin implements IrcPluginInterface {
    public IrcSession session;
    public IrcBot bot;
    IrcEventDispatcher dispatcher;
    public void initialize(IrcSession sess,IrcBot b,IrcEventDispatcher disp){
        session=sess;
        bot=b;
        dispatcher=disp;
    }
    public IrcEventAction handleEvent(IrcEvent event) throws Exception
    {
        return IrcEventAction.CONTINUE;
    }
}
