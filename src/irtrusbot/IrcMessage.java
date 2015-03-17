/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

/**
 *
 * @author crashdemons <crashdemons -at- github.com>
 */
public class IrcMessage {
    public IrcOrigin from;
    public String to;
    public String text;
    private String type="PRIVMSG";//for future ability to implement "NOTICE"

    public String toIncoming() {
        return ":" + from.toString() + " "+type+" " + to + " :" + text;
    }

    public String toOutgoing() {
        return type+" " + to + " :" + text;
    }
    
    public int getEstimatedLength(){ return toIncoming().length(); }//calculate the server-side command length (limiting factor)
    public int getEstimatedRemaining(){ return (512-2)-toIncoming().length(); }//calculate the number of chars remaining 'till server-side max
    public boolean isMessageValidLength(){ return (512-2)>=toIncoming().length(); }//validate if the server-side version of the command is under the command length limit.

    public IrcMessage getReply(IrcOrigin replyfrom, String replytext, boolean direct) {
        String dest = from.nick;
        if (isChannel(to) && !direct) {
            dest = to;//replies to channel messages inside the channel (when direct replying is off)
        }
        return new IrcMessage(replyfrom, dest, replytext);
    }
    public IrcMessage(IrcCommand ic) throws IrcMessageCommandException{//create a new message from a PRIVMSG command
        if(!ic.type.equals("PRIVMSG")) throw new IrcMessageCommandException("IrcMessage expects command of type PRIVMSG/NOTICE");
        if(!ic.hasOrigin) throw new IrcMessageCommandException("IrcMessage expects an incoming PRIVMSG with an origin (default).");
        if(ic.parameters.size()<2) throw new IrcMessageCommandException("IrcMessage expects PRIVMSG of at least two fields (destination and text)");
        from=ic.origin;
        to=ic.parameters.get(0);
        text=ic.parameters.get(1);
    }
    public IrcMessage(IrcOrigin ofrom, String sto, String stext) {//create a new message from incoming fields
        from = ofrom;
        to = sto;
        text = stext;
    }

    private boolean isChannel(String nick) {
        return nick.startsWith("#");
    }
}

class IrcMessageCommandException extends Exception{
  public IrcMessageCommandException(String message){ super(message); }
  
}