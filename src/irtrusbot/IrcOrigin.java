package irtrusbot;

import java.util.*;

/** IRC origin/user information class.
 * Contains and translates information for the sender's format {@code nick!user@host} and ident status.
 * Used to access discrete parts of the information given as well as formatting for command length limit calculations.
 * @author Crashdemons
 */
public class IrcOrigin {
    
    /** list of tokens from the origin string*/
    public ArrayList<String> elements = new ArrayList<String>();
    
    /** nickname used by the sender*/
    public String nick;
    
    /** username used by the sender (note: does not contain IDENT tilde)
     * @see isIdented
     */
    public String user;
    
    /** hostname used by the sender*/
    public String host;
    
    /** server name sending the message, if the message was not sent by a user*/
    public String server;
    
    /**State whether or not the message was sent by a User (has nick,user,host and isIdented properties set) */
    public boolean isUser;
    /**State whether or not the user sending the message successfully used IDENTd*/
    public boolean isIdented;
    /**State whether or not the message was sent by an IRC Server (has server property set) */
    public boolean isServer;
    
    /** tokenize the origin string and set object properties*/
    public final void interpret(String s){
        if(s.startsWith(":")) s=s.substring(1);//trim off leading :
        elements = new ArrayList<String>(Arrays.asList(s.split("[!@]")));
        
        nick=user=host=server="";//initialize all of variables these to ""
        isUser=isServer=isIdented=false;
        if(elements.size()==3) isUser=true;//a user origin should have exactly 3 parts: nick, user, host.
        if(elements.size()==1) isServer=true;//server origins just state the server-name.

        if(isUser){
            nick=elements.get(0);
            user=elements.get(1);
            host=elements.get(2);
            if(user.startsWith("~")){ user=user.substring(1); isIdented=false;}
            else isIdented=true;
        }
        if(isServer){
            server=elements.get(0);
        }
    }
    
    /** Create an Origin information object from an origin/sender string [taken from an incoming command]
     * 
     * @param origin_string string indicating the origin/sender of an IRC command.
     */
    public IrcOrigin(String origin_string){ interpret(origin_string); }
    
    /** Create an Origin information object from discrete parts of User sender information strings
     * 
     * @param nickname nickname of a sender
     * @param username username of a sender
     * @param hostname hostname of a sender
     */
    public IrcOrigin(String nickname,String username,String hostname){
        interpret(":"+nickname+"!"+username+"@"+hostname);
    }
    
    /** Translate property information into an Origin string (as seen in front of incoming IRC commands)
     * 
     * @return String representation of the sender.
     */
    @Override
    public String toString(){
        if(isServer){
            return server;
        }
        if(isUser){
            String u=user;
            if(!isIdented) u="~"+user;
            return nick+"!"+u+"@"+host;
        }
        return "";
    }
}
