package irtrusbot;



import java.io.*;
import java.net.*;

/** Class maintaining information and useful actions pertaining to a client session connected to a server.
 *
 * @author crashdemons [crashdemons -at- github.com]
 */
public class IrcSession {
    /** Origin information about the client/user of the session.
     * Contains the user's Nickname, Username text, Hostname, and Ident status
     */
    public IrcOrigin account;
    String rname="";
    String pass="";
    
    //public boolean doIdentify=false;
    
    
    /** IRC Server being connected to*/
    public String server="";
    /** Plaintext IRC port used for connection*/
    public int port=6667;
    /** Running count of IRC Commands sent during the object lifetime*/
    public int sent;
    /** Running count of IRC Commands received during the object lifetime*/
    public int received;
    
    private Socket socket = null;
    private BufferedWriter writer = null;
    private BufferedReader reader = null;
    
    /** QUIT (if possible) and close the current IRC connection
     * NOTE: this function also sets all writers and sockets to null.
     * @throws IOException Error occurred while sending QUIT or closing socket.
     */
    public void disconnect() throws IOException{
        if(isConnected()){
            sendRawLine("QUIT :disconnecting");
            try{Thread.sleep(1000);}catch(Exception e){}
            socket.close();
        }
        socket=null;
        writer=null;
        reader=null;
    }
    
    /** Connect to the configured IRC server and port.
     * 
     * @return True: connection successful. False: connection failed.
     * @throws UnknownHostException The hostname could not be found
     * @throws IOException Error occurred while creating socket streams.
     */
    public boolean connect() throws UnknownHostException,IOException{
        socket=new Socket(server, 6667);//TODO: error-check  new Socket(...) before creating streams.
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return socket.isConnected();
    }
    
    /** Determine if the session is currently connected to the IRC Server
     * 
     * @return True: currently connected to server. False: disconnected/not connected to any server.
     */
    public boolean isConnected(){ 
        if(socket==null) return false;
        else return socket.isConnected();
    }
    
    /** Sends the configured user information to the server (for initial connections).
     * This includes NICK and USER commands.
     * NOTE: if a password is available, a PASS command will be attempted.
     * @throws IOException Error sending data.
     */
    public void login() throws IOException{
        if(!pass.isEmpty()) sendRawLine("PASS "+pass);
        sendRawLine("NICK "+account.nick);
        sendRawLine("USER "+account.user+" 8 * :"+rname);
    }
    
    /** Sends user information and waits for success or failure indicators (001-004 or specific error codes)
     * @see #login()
     * @throws IOException Error reading or sending data.
     * @return the result of the login attempt, described by the IrcLoginState value. | SUCCESS: A command 001-004 was received | FAILURE: The connection is not open or a fatal error command was received.
     */
    public IrcLoginState loginwait() throws IOException{
        login();
        IrcCommand ic;
        while((ic=readCommand())!=null && isConnected()){
            if( ic.ntype>=1 && ic.ntype<=4) return IrcLoginState.SUCCESS;//RFC2812 $5.1 "The server sends Replies 001 to 004 to a user upon successful registration."
            if(isFatalCommand(ic)) break;
        }
        return IrcLoginState.FAILURE;
    }
    
    /** Checks an IrcCommand received after a login() attempt and suggests whether the login succeeded, failed, or we're still waiting for a proper response.
     * SUCCESS occurs when a message 001-004 are sent per RFC2812 $5.1
     * 
     * Using login() and logincheck() together is non-blocking alternative to using loginwait()
     * @see #login()
     * @see #loginwait()
     * @param ic The IrcCommand to check 
     * @return the result of the login attempt thus far, described by the IrcLoginState value. | SUCCESS: The command type was 001-004 | FAILURE: The connection is not open or a fatal error command was received. | WAIT: the command was of no significance to the login process.
     */
    public IrcLoginState logincheck(IrcCommand ic){
        if(!isConnected()) return IrcLoginState.FAILURE;
        if(ic!=null){
            if( ic.ntype>=1 && ic.ntype<=4) return IrcLoginState.SUCCESS;
            if(isFatalCommand(ic)) return IrcLoginState.FAILURE;
        }
        return IrcLoginState.WAIT;
    }
    
    
    /** Sends a Nickserv Identify command, if a password is set.
     * The exact format used is {@code PRIVMSG NickServ :IDENTIFY passwordhere}
     * @throws IOException Error sending data.
     */
    public void identify() throws IOException{
        if(!pass.isEmpty()) sendMessage(new IrcMessage(account,"NickServ","IDENTIFY "+pass));
    }
    
    
    /** Read a single IRC Command line of text from the server
     * 
     * @return Connected: Raw IRC Command string  |  Not Connected: null
     * @throws IOException Error reading data.
     */
    public String readRawLine() throws IOException{
        if(!isConnected()) return null;
        String line=reader.readLine();
        if(line==null) throw new IOException("null");
        received++;
        System.out.println("< "+line);
        return line;
    }
    
    /** Send a single IRC Command line of text to the server
     * 
     * @param line Raw IRC Command string to send
     * @throws IOException Error sending data.
     */
    public void sendRawLine(String line) throws IOException{
        if(!isConnected()) return;
        System.out.println("> "+line);
        writer.write(line + "\r\n");
        writer.flush();
        sent++;
    }
    
    /** Converts and sends an IRC Message object as a PRIVMSG to the server.
     * 
     * @param im IRC Message object
     * @throws IOException Error sending data
     */
    public void sendMessage(IrcMessage im) throws IOException{
        sendRawLine(im.toOutgoing());
    }
    
    /** Formats and sends a message (PRIVMSG) [based on message details] to the server
     * 
     * @param from Origin information for the sender (generally 'account')
     * @param to Nickname or Channel string for the destination of the message
     * @param text Message text being sent
     * @throws IOException Error sending data.
     */
    public void sendMessage(IrcOrigin from,String to, String text) throws IOException{
        sendRawLine((new IrcMessage(from,to,text)).toOutgoing());
    }
    
    /** Formats and sends a message (PRIVMSG) [based on message details] to the server.
     * NOTE: this function assumes 'account' to be from/origin field.
     * @param to Nickname or Channel string for the destination of the message
     * @param text Message text being sent
     * @throws IOException Error sending data.
     * @see account
     */
    public void sendMessage(String to, String text) throws IOException{
        sendRawLine((new IrcMessage(account,to,text)).toOutgoing());
    }
    
    /** Creates, Converts, and sends a reply message to an IRC Message object as a PRIVMSG to the server
     * NOTE: this function assumes 'account' to be from/origin field of the reply sent.
     * @param im IRC Message object being replied-to.
     * @param text Message text being sent in reply.
     * @param direct True: Message is sent via private-message to the replyee | False: message will the channel the replyee used, if possible.
     * @throws IOException Error sending data.
     */
    public void sendReply(IrcMessage im,String text, boolean direct) throws IOException{
        sendMessage(im.getReply(account, text, direct));
    }
    
    private boolean isFatalNumeric(int ntype){
        //RFC2812 $5.2 "Error replies are found in the range from 400 to 599."
        return ( (ntype>=431 && ntype<=436)   //all fatal nickname errors
               ||(ntype>=451 && ntype<=465) );//notregistered, needmoreparams, ... passwordmismatch, banned, etc.
    }
    private boolean isFatalCommand(IrcCommand ic){
        return ( isFatalNumeric(ic.ntype) || ic.type.equals("ERROR") );
    }
    
    /** Reads in a single IRC Command (in object form) from the server.
     * NOTE: Fatal commands automatically force disconnection.
     * @return Success: IRC Command object | Failure: null
     * @throws IOException Error reading data.
     */
    public IrcCommand readCommand() throws IOException{
        String line = readRawLine();
        if(line==null) return null;
        IrcCommand ic = new IrcCommand(line);
        //RFC2812 $3.7.4 "The ERROR message is also used before terminating a client connection."
        if(isFatalCommand(ic)) disconnect();
        //debugObject(ic);
        return ic;
    }
    
    /** Set details about the IRC Server to connect to.
     * 
     * @param sServer Hostname string of the server (domain name)
     * @param nPort Port to connect to (usually 6667) NOTE: must be plaintext not SASL/SSL/etc.
     */
    public void setConnectionDetails(String sServer,int nPort){
        server=sServer;
        port=nPort;
    }
    
    /** Set user information (transmitted to server on initial connections).
     * This user information is used in login() and identify().
     * @param nickname nickname to use for the session
     * @param username username/ident name to use during the session
     * @param realname "real name" text to use during the session
     * @param password password to identify with
     * @see #login()
     */
    public void setAccountDetails(String nickname,String username,String realname,String password){
        account.nick=nickname;
        account.user=username;
        rname=realname;
        pass=password;
    }
    
    /** Create an IRC Session instance - specific to one client/user.
     * Multiple instanced may be created (per-connection) as required without issue.
     */
    public IrcSession(){
        this.account = new IrcOrigin("","","");
        sent=received=0;
    }
}
