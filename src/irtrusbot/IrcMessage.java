package irtrusbot;

/** IRC Privmsg/Notice class.
 * Provides a manipulable representation of IRC Message type commands.
 * Can be used to create, interpret, or validate PRIVMSG lines.
 * @author crashdemons [crashdemons -at- github.com]
 */
public class IrcMessage {
    /** Origin object indicating information about the sender of the message. */
    public IrcOrigin from;
    /** Nickname or Channel-name of the destination for the message*/
    public String to;
    /** The message text being transmitted by the command between the sender and destination.*/
    public String text;
    //for future ability to implement "NOTICE"
    private final String type="PRIVMSG";

    /** Convert an IRC Message into the server-sent (incoming) string format.
     * This conversion is useful for determining the maximum message length, as both the incoming and outgoing commands must be under 512 chars.
    * @return Incoming-formatted string representation of the IRC Message command eg. {@code :nick!user@host PRIVMSG to :text}.
     */
    public String toIncoming() {
        return ":" + from.toString() + " "+type+" " + to + " :" + text;
    }

    /** Convert an IRC Message into the client-sent (outgoing) string format.
     * This conversion is used to prepare messages to be sent an IRC server.
     * @return Outgoing-formatted string representation of the IRC Message command eg. {@code PRIVMSG to :text}.
     */
    public String toOutgoing() {
        return type+" " + to + " :" + text;
    }
    //(roughly 495-(nick+user+host+destination+text) for outgoing messages)
    
    /** Calculates the length of the server-sent format of the message.
     * Used to identify the consumption of the command char limit. 
     * This value is equivalent of IrcMessage.toIncoming().length()
     * NOTE: this function requires the 'from' field to be filled out correctly for correct calculation.
     * @return Length of the server-sent-format IRC command.
     */
    public int getEstimatedLength(){ return toIncoming().length(); }//calculate the server-side command length (limiting factor)
    
    /**Calculates the remaining characters until the maximum IRC message length is reached.
     * NOTE: this function requires the 'from' field to be filled out correctly for correct calculation.
     * @return Positive: Number of characters remaining.  Negative: Number of characters over-limit.
     */
    public int getEstimatedRemaining(){ return (512-2)-toIncoming().length(); }//calculate the number of chars remaining 'till server-side max
    
    /**Determines if the message length is within IRC limitations.
     * NOTE: this function requires the 'from' field to be filled out correctly for correct calculation.
     * Equivalent to {@code IrcMessage.getEstimatedRemaining()>=0}
     * @return True: message length is within limitations. False: message length is too high for an IRC Command.
     */
    public boolean isMessageValidLength(){ return (512-2)>=toIncoming().length(); }//validate if the server-side version of the command is under the command length limit.

    /**Generates an IRC Message in the form of a reply to given message.
     * This reply will be sent in a way that the 'from' field user will receive it.
     * However, depending on the the original 'to' field, and the parameters given, this reply may be in the channel.
     * @param replyfrom Origin information about the user replying to the message. (necessary for length calculations)
     * @param replytext Message to send in reply, to the sender of the first message.
     * @param direct Determines if the reply should be sent back directly (private message), or [conditionally] in the channel the first sender used.
     * @return IrcMessage object for the reply message
     */
    public IrcMessage getReply(IrcOrigin replyfrom, String replytext, boolean direct) {
        String dest = from.nick;
        if (isChannel(to) && !direct) {
            dest = to;//replies to channel messages inside the channel (when direct replying is off)
        }
        return new IrcMessage(replyfrom, dest, replytext);
    }
    
    /** Interprets an IRC message (PRIVMSG) object from an generic IRC Command object.
     * 
     * @param ic IRC Command object being interpreted as an IRC Message (PRIVMSG)
     * @throws IrcMessageCommandException default exception for IRC Commands incompatible with IrcMessage.
     */
    public IrcMessage(IrcCommand ic) throws IrcMessageCommandException{//create a new message from a PRIVMSG command
        if(!ic.type.equals("PRIVMSG")) throw new IrcMessageCommandException("IrcMessage expects command of type PRIVMSG/NOTICE");
        if(!ic.hasOrigin) throw new IrcMessageCommandException("IrcMessage expects an incoming PRIVMSG with an origin (default).");
        if(ic.parameters.size()<2) throw new IrcMessageCommandException("IrcMessage expects PRIVMSG of at least two fields (destination and text)");
        from=ic.origin;
        to=ic.parameters.get(0);
        text=ic.parameters.get(1);
    }
    
     /** Creates an IRC message (PRIVMSG) object from details about a message received or to be sent.
      * 
      * @param ofrom Origin information about the sender of the message (necessary for length calculations)
      * @param sto Nickname for the destination of this message.
      * @param stext Text of the message being sent
      */
    public IrcMessage(IrcOrigin ofrom, String sto, String stext) {//create a new message from incoming fields
        from = ofrom;
        to = sto;
        text = stext;
    }

    private boolean isChannel(String nick) {
        return nick.startsWith("#");
    }
}
