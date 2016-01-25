/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

/** Enumeration describing the current state of an IrcBot instance
 *
 * @author crash
 */
public enum IrcState {
    UNDEFINED,DISCONNECTED,CONNECTED,LOGIN_WAIT,LOGGED_IN,JOINED,QUIT
};
