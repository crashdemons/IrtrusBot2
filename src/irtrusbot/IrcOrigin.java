/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package irtrusbot;
/**
 *
 * @author Crash
 */

import java.util.ArrayList;
import java.util.Arrays;

public class IrcOrigin {
    public ArrayList<String> elements = new ArrayList<String>();
    public String nick,user,host,server;
    public boolean isUser;
    public boolean isIdented;
    public boolean isServer;
    
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
    public IrcOrigin(String origin_string){ interpret(origin_string); }
    public IrcOrigin(String nickname,String username,String hostname){
        interpret(":"+nickname+"!"+username+"@"+hostname);
    }
    
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
