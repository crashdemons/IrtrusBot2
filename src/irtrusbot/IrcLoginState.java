/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

/** Enumeration describing the state of the login process as used by IrcSession methods.
 *
 * @author crash
 */
public enum IrcLoginState {
    /** The login process is still waiting for success/failure indications. */
    WAIT,
    /** The login process has succeeded. */
    SUCCESS,
    /** The login process has failed. */
    FAILURE
}
