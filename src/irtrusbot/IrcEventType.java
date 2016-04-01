/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

/** Enumeration describing the type of event propagated to the plugin handler.
 *
 * @author crash
 */
public enum IrcEventType {
    /** The event is invalid */
    UNDEFINED,
    /** The event is notifying of an IRC Command being sent or received */
    COMMAND,
    /** The event is notifying of a Bot/connection state change */
    STATE,
    /** The event is notifying of an IRC Message [PRIVMSG] being sent or received */
    CHAT,
    /** The event is notifying the plugin that it has been disabled by the Plugin Manager and will not receive events. */
    PLUGIN_DISABLED,
    /** The event is notifying the plugin that it has been re-enabled by the Plugin Manager and will again receive events. */
    PLUGIN_ENABLED,
    /** The event is notifying that the Bot has been initialized and plugins should start operating. */
    BOT_START,
    /** The event is notifying that the Bot is being stopped and plugins should finish operations. */
    BOT_STOP,
    /** This is a custom event to be used by inter-plugin communication. */
    CUSTOM,
    /** This event specifies that a bot "tick" has occurred - A tick occurs about every 50ms or 20 Ticks/second.*/
    TICK
}
