
package irtrusbot;

/** A class that describes an event occurring in the IrcBot/client processing - ranging from connection state changes to received data
 *
 * @author crash
 */
public class IrcEvent {
    /** The type of event being handled. */
    public IrcEventType type=IrcEventType.UNDEFINED;
    /** The previous bot state at the time the Event was dispatched */
    public IrcState lastState;
    /** The current bot state at the time the Event was dispatched */ 
    public IrcState state;
    /** Field for a sent or received IRC Command object, for COMMAND type events - This is 'null' for inapplicable events. */ 
    public IrcCommand command;
    /** Field for a sent or received IRC Message (PRIVMSG) object, for CHAT type events - This is 'null' for inapplicable events. */ 
    public IrcMessage message;
    /** Indicates whether the Command/Message was incoming or outgoing, etc for COMMAND and CHAT events. */ 
    public IrcDirection direction=IrcDirection.RECEIVING;
    /** additional string data associated with the event, if any. */ 
    public String sdata;
    /** additional integer data associated with the event, if any. */ 
    public int idata;
    
    /** Constructs an Event from type, state, and command information
     * Note: attempts automatic interpretation of IRC Commands to Messages if applicable.
     * @param t The type of the event being dispatched.
     * @param ls The previous bot state at the time the Event was dispatched
     * @param s The current bot state at the time the Event was dispatched
     * @param ic A sent or received IRC Command object, for COMMAND type messages. This should be set to 'null' if it is not applicable.
     */
    public IrcEvent(IrcEventType t, IrcState ls, IrcState s, IrcCommand ic){
        type=t;
        lastState=ls;
        state=s;
        command=ic;
        message=null;
        sdata="";
        idata=0;
        if(ic!=null) try{
            message=new IrcMessage(ic);
        }catch(IrcMessageCommandException e){
            message=null;
        }
    }
}
