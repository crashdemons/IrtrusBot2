
package irtrusbot;

/** An Enumeration describing whether a message of interest is being sent, received, or internally simulated.
 *
 * @author crash
 */
public enum IrcDirection {
    /** The direction of the message is unknown */
    UNKNOWN,
    /** The message is being internally simulated and was neither sent nor received. */
    SIMULATED,
    /** The message is being sent */
    SENDING,
    /** The message is being received. */
    RECEIVING
}
