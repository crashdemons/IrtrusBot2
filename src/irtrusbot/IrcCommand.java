/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package irtrusbot;

/**
 *
 * @author Crashdemons
 */

import java.util.ArrayList;


public class IrcCommand {
    public ArrayList<String> elements = new ArrayList<String>();
    public ArrayList<String> parameters = new ArrayList<String>();

    public String type;
    public int ntype;
    public boolean isNumeric;
    
    public IrcOrigin origin;
    public boolean hasOrigin;


    public boolean isType(String t){return type.equals(t);}

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

    public IrcCommand(String s){ interpret(s);}
}
