package irtrusbot;
/**
 * @author Surtri <Surtri at irc.freenode.net>
 * modified heavily by crashdemons
 */

import java.util.Random;

public class IrtrusBot
{
    static private int rr_chamber=0;
    static private int rr_bulletchamber=-1;
    static private final Random rand = new Random();
    
	public static void main(String[] args) throws Exception
	{
            System.out.println("========================================");
            startIRC();
            System.out.println("========================================");
	}

	private static void startIRC() throws Exception
	{
		String server = "irc.freenode.net", nick = "IrtrusBot2", user = "IrtrusBot2", channel = "#irtrusbot", chan = "";
		
                IrcSession irc=new IrcSession();
                irc.setAccountDetails(nick,user,"Java Hacks Bot","");//no password=""
                irc.setConnectionDetails(server, 6667);
    
                irc.connect();//establish TCP connection.
                irc.loginwait();//send NICK+USER and wait for 001-004 or failure
                irc.identify();//send nickserv identify if necessary
                irc.sendMessage(irc.account,nick,"++userinit");//this is sent so that we can obtain our full hostmask/identd state easily - useful for post length calculations later.
                irc.sendRawLine("JOIN "+channel);
                
                IrcCommand ic;
                while((ic=irc.readCommand())!=null && irc.isConnected()){
			//System.out.println("Entered loop...");
			//debugObject(ic);
                        
                        if(ic.type.equals("PRIVMSG")){
                            IrcMessage im=new IrcMessage(ic);
                            
                            boolean fromAdmin=(im.from.nick.equals("crashdemons") || im.from.nick.equals("Surtri"));
                            boolean fromSelf =im.from.nick.equals(nick);
                            
                            
                            if(im.text.startsWith("++userinit") && fromSelf){ irc.account=im.from; }//this is our test PM to ourself that grabs the username/hostname for us.
                            
                            if(im.text.startsWith("+quit") && fromAdmin){ irc.disconnect(); break; }
                            if(im.text.startsWith("+rr")){
                                String text=russianRouletteFire();
                                irc.sendReply(im, text, false);//reply to the user in whatever tab he's in (PM or channel)
                            }
                        }
                        if(ic.type.equals("PING")) irc.sendRawLine("PONG :"+ic.parameters.get(0));//pong back with the first parameter of the PING.
	
                        
                }
                //socket has disconnected.
        }
        
        private static void russianRouletteSpin(){//randomize the bullet placement for a round of russian roulette.
            rr_bulletchamber=rand.nextInt(6);// range is [0,6)      .
        }
        
	private static String russianRouletteFire()
	{
            if(rr_bulletchamber==-1) russianRouletteSpin();//uninitialized chamber # on first firing.
            if(rr_chamber==rr_bulletchamber){
                russianRouletteSpin();//randomize rr_bulletchamber for the next around
                rr_chamber=0;
                return "Bang! You were shot. <Loads another round and spins the chambers>";
            }
            rr_chamber=(rr_chamber+1)%6;//increment the firing chamber with 0-5 wraparound (for next attempt)
            return "Click! You were lucky... this time.";
	}
        //-----------------------------------------------


}




/*
Debugging: function to print every property of an object:
import java.lang.reflect.*;

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
*/