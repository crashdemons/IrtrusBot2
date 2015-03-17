package irtrusbot;

/** Exception for IRC Commands incompatible with IrcMessage constructors.
 * ie: commands that are not type PRIVMSG, or do not conform to RFC2812 $3.3.1 (Private messages)
 * @author crashdemons <crashdemons -at- github.com>
 */
@SuppressWarnings("serial")
public class IrcMessageCommandException extends Exception{
  public IrcMessageCommandException(String message){ super(message); }
  
}
