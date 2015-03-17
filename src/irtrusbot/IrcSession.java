/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;


import java.io.*;
import java.lang.reflect.Field;
import java.net.*;

/**
 *
 * @author crashdemons <crashdemons -at- github.com>
 */
public class IrcSession {
    public IrcOrigin account=new IrcOrigin("","","");
    String rname="";
    String pass="";
    
    
    
    public String server="";
    public int port=6667;
    public int sent;
    public int received;
    
    private Socket socket = null;
    private BufferedWriter writer = null;
    private BufferedReader reader = null;
    
    public void disconnect() throws IOException{
        if(socket.isConnected()){
            sendRawLine("QUIT :disconnecting");
            try{Thread.sleep(1000);}catch(Exception e){}
            socket.close();
        }
        socket=null;
        writer=null;
        reader=null;
    }
    public boolean connect() throws UnknownHostException,IOException{
        socket=new Socket(server, 6667);
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return socket.isConnected();
    }
    public boolean isConnected(){ if(socket==null) return false; return socket.isConnected(); }
    
    
    public void login() throws IOException{
        if(!pass.isEmpty()) sendRawLine("PASS "+pass);
        sendRawLine("NICK "+account.nick);
        sendRawLine("USER "+account.user+" 8 * :"+rname);
    }
    public void loginwait() throws IOException{
        login();
        IrcCommand ic=null;
        while((ic=readCommand())!=null && isConnected()){
            if( ic.ntype>=1 && ic.ntype<=4) break;//RFC2812 $5.1 "The server sends Replies 001 to 004 to a user upon successful registration."
            if(isFatalCommand(ic)) break;
        }
        
    }
    public void identify() throws IOException{
        if(!pass.isEmpty()) sendMessage(new IrcMessage(account,"NickServ","IDENTIFY "+pass));
    }
    
    
    
    public String readRawLine() throws IOException{
        if(!isConnected()) return null;
        String line=reader.readLine();
        if(line==null) throw new IOException("null");
        if(line!=null) received++;
        System.out.println("< "+line);
        return line;
    }
    public void sendRawLine(String line) throws IOException{
        if(!isConnected()) return;
        System.out.println("> "+line);
        writer.write(line + "\r\n");
        writer.flush();
        sent++;
    }
    public void sendMessage(IrcMessage im) throws IOException{
        sendRawLine(im.toOutgoing());
    }
    public void sendMessage(IrcOrigin from,String to, String text) throws IOException{
        sendRawLine((new IrcMessage(from,to,text)).toOutgoing());
    }
    public void sendReply(IrcMessage im,String text, boolean direct) throws IOException{
        sendMessage(im.getReply(account, text, direct));
    }
    public boolean isFatalNumeric(int ntype){
        //RFC2812 $5.2 "Error replies are found in the range from 400 to 599."
        if(ntype>=431 && ntype<=436) return true;//all fatal nickname errors
        if(ntype>=451 && ntype<=465) return true;//notregistered, needmoreparams, ... passwordmismatch, banned, etc.
        return false;
    }
    public boolean isFatalCommand(IrcCommand ic){
        return ( isFatalNumeric(ic.ntype) || ic.type.equals("ERROR") );
    }
    public IrcCommand readCommand() throws IOException{
        String line = readRawLine();
        if(line==null) return null;
        IrcCommand ic = new IrcCommand(line);
        //RFC2812 $3.7.4 "The ERROR message is also used before terminating a client connection."
        if(isFatalCommand(ic)) disconnect();
        //debugObject(ic);
        return ic;
    }
    
    
    public void setConnectionDetails(String sServer,int nPort){
        server=sServer;
        port=nPort;
    }
    public void setAccountDetails(String nickname,String username,String realname,String password){
        account.nick=nickname;
        account.user=username;
        rname=realname;
        pass=password;
    }
    public IrcSession(){
        sent=received=0;
    }
            public static void debugObject(Object obj){
            StringBuilder sb = new StringBuilder();
            sb.append(obj.getClass().getName());
            sb.append(": ");
            for (Field f : obj.getClass().getDeclaredFields()) {
                sb.append(f.getName());
                sb.append("=");
                try{
                    sb.append(f.get(obj));
                }catch(IllegalAccessException e){
                    sb.append("***PRIVATE***");
                }
                sb.append(", ");
            }
            System.out.println(sb.toString());
        }
}
