package irtrusbot;

/**
 *
 * @author crashdemons <crashdemons -at- github.com>
 */

import java.io.*;
import java.net.*;


public class IrcSession {
    public IrcOrigin account;
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
        IrcCommand ic;
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
        received++;
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
        return ( (ntype>=431 && ntype<=436)   //all fatal nickname errors
               ||(ntype>=451 && ntype<=465) );//notregistered, needmoreparams, ... passwordmismatch, banned, etc.
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
        this.account = new IrcOrigin("","","");
        sent=received=0;
    }
}
