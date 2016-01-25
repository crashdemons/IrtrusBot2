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
public class IrcEvent {
    public IrcEventType type=IrcEventType.UNDEFINED;
    public IrcState lastState;
    public IrcState state;
    public IrcCommand command;
    public IrcMessage message;
    public IrcDirection direction=IrcDirection.RECEIVING;
    public String sreserved;
    public int ireserved;
    

    public IrcEvent(IrcEventType t, IrcState ls, IrcState s, IrcCommand ic){
        type=t;
        lastState=ls;
        state=s;
        command=ic;
        if(ic!=null) try{
            message=new IrcMessage(ic);
        }catch(IrcMessageCommandException e){
            message=null;
        }
    }
}
