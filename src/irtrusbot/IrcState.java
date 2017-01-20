/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

/** Enumeration describing the current state of an IrcBot instance and connection
 *
 * @author crash
 */
public enum IrcState {
    /** The state is unknown */
    UNDEFINED,
    /** The bot is currently disconnected and can be reconnected */
    DISCONNECTED,
    /** The bot is currently connected to an IRC Server */
    CONNECTED,
    /** The bot has sent login data and is waiting for a response */
    LOGIN_WAIT,
    /** The bot has successfully logged in and can join channels or message users */
    LOGGED_IN,
    /** The bot has joined a channel */
    JOINED,
    /** The bot failed login, received a fatal error message (or user-interaction) and has disconnected - do not reconnect automatically - program is exiting */
    QUIT,
    /** The bot failed login, received a fatal error message (or user-interaction) and has disconnected - do not reconnect automatically - intermediate state used during program shutdown */
    QUITTING
    
};
