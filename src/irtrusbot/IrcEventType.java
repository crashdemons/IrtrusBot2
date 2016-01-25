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
    UNDEFINED,COMMAND,STATE,CHAT,PLUGIN_DISABLED,PLUGIN_ENABLED,BOT_START,BOT_STOP
}
