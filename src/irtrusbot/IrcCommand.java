package irtrusbot;

import java.util.*;

/** IRC Command class.
 *  Represents an IRC command line in the form of different properties and provides related functions.
 *
 * @author Crashdemons
 */
public class IrcCommand {
    /** list of tokens for the IRC Command*/
    public ArrayList<String> elements = new ArrayList<String>();
    /** list of parameters for the IRC Command (tokens after the command name) */
    public ArrayList<String> parameters = new ArrayList<String>();

    /**Command type string (eg: 001, PRIVMSG, JOIN, ERROR) */
    public String type;
    /**Integer representation for numeric command types (-1 when non-numeric)*/
    public int ntype;
    /**State whether or not the command type is numeric*/
    public boolean isNumeric;
    
    /** Origin information about the sender of a command/message - usually a user or the server*/
    public IrcOrigin origin;
    /**State whether or not the command has an origin field preceding it*/
    public boolean hasOrigin;

    /** Determines of the given type string is the same as the current command type
     * 
     * @param t Command type string to compare (eg: 001, PRIVMSG, ERROR)
     * @return True: the type strings match | False: they do not match.
     */
    public boolean isType(String t){return type.equals(t);}

    /** Tokenizes an IRC Command string into parts (supports space-containing fields at end of command)
     * 
     * @param s IRC Command string to tokenize
     * @return ArrayList collection of token strings.
     */
    public static ArrayList<String> tokenize(String s){
        int iMax=s.length()-1;
        ArrayList<String> parts = new ArrayList<String>();
        char c;
        String part="";
        boolean allowspaces=false; //current token allows spaces (by IRC definition, this is the final token)
        boolean terminate=false; //current token is the final token - force adding to the token list and
        boolean append; //controls whether to append the current char to the current token
        boolean addnow; //controls whether to add the current token to the list of tokens at a given iteration
        
        for(int i=0;i<=iMax;i++){
            addnow=false;//do not force adding the token to the list
            append=true;//default for all chars - force adding the char to the token.
            c=s.charAt(i);
            
            if(i==iMax) terminate=true;//force adding the last token to the token list.

            //newlines and nulls terminate IRC commands
            if(c=='\n' || c=='\r' || c==0) {
                append=false;//do not include the newlines as part of the command parameter
                terminate=true;
            }
            //if a command parameter token starts with ":" it denotes it's the last parameter in the command and accepts spaces.
            if(c==':' && part.isEmpty() && i>0 && !allowspaces){
                append=false;//do not include the colon indicator as part of the command parameter
                allowspaces=true;
            }
            //we've not yet reached a special "last part" that accepts spaces, so this space delimits the token
            if(c==' ' && !allowspaces){
                append=false;//do not include delimiting spaces as part of the command parameter.
                addnow=true;//a new token needs to be started, so add this one and start blank for the next.
            }

            //following above instructions set by flags.
            if(append) part+=c;//append current char to current token string.
            if(addnow || terminate){//add the current token string to the list of tokens.
                parts.add(part);//add the token to the list of command parts
                part="";//clear this token to start working on the next
            }
            if(terminate) break;
        }
        return parts;
    }

    /** Tokenize, and determine property values from a given IRC Command string
     * 
     * @param s IRC Command string
     */
    public final void interpret(String s){
        origin=null;
        parameters.clear();
        elements=tokenize(s);
        int iType;//index of the type element text, determined below

        if(elements.get(0).startsWith(":")){
            hasOrigin=true;
            origin=new IrcOrigin(elements.get(0));
            iType=1;
        }else{
            iType=0;
        }
        
        //if parameters after the type exist, create the parameters list from them.
        if(elements.size()>(iType+1)) parameters=new ArrayList<String>(elements.subList(iType+1,elements.size()));
        
        
        
        //set the type to the element indicated by itype, determined far above.
        type=elements.get(iType);
        //set the numerical value of the type
        try{ ntype=Integer.parseInt(type); }catch(Exception e){ ntype=-1; }
        
    }

    /** Create an IRC Command object with properties interpreted from an IRC Command string.
     * 
     * @param s IRC Command string
     */
    public IrcCommand(String s){ interpret(s);}
    
    
    private String getParamString(){
        String s="";
        boolean first=true;
        int last=parameters.size()-1;
        for (int i = 0; i <= last; i++) {
            String param=parameters.get(i);
            if(!first) s+=" ";
            first=false;
            if(param.indexOf(' ')!=-1 || i==last) s+=":";
            s+=param;
        }
        return s;
    }
    
    
        /** Convert an IRC Message into the server-sent (incoming) string format.
     * This conversion is useful for determining the maximum message length, as both the incoming and outgoing commands must be under 512 chars.
    * @return Incoming-formatted string representation of the IRC Message command eg. {@code :nick!user@host PRIVMSG to :text}.
     */
    public String toIncoming() {
        return ":" + origin.toString() + " "+type+" " + getParamString();
    }

    /** Convert an IRC Message into the client-sent (outgoing) string format.
     * This conversion is used to prepare messages to be sent an IRC server.
     * @return Outgoing-formatted string representation of the IRC Message command eg. {@code PRIVMSG to :text}.
     */
    public String toOutgoing() {
        return type+" " + getParamString();
    }
    
    @Override
    public String toString() {
        if(hasOrigin) return toIncoming();
        return toOutgoing();
    }
    
}
