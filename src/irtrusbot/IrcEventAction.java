
package irtrusbot;

/** Enumeration describing what action should be taken on a processed IrcEvent message
 * This value is returned by plugin eventHandlers.
 *
 * @author crash
 */
public enum IrcEventAction {
    /** Continue propagating/dispatching the event to other plugins */
    CONTINUE,
    /** Stop propagating/dispatching the event to other plugins */
    STOP_PROPAGATING,
    /** Stop propagating/dispatching the event to other plugins and signal the Bot to cancel the operation that caused the event to be dispatched. */
    CANCEL_EVENT
}
