/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

/**
 *
 * @author crash
 */
public interface IrcPluginInterface {
    public void initialize(IrcSession sess,IrcBot b,IrcEventDispatcher disp);
    public IrcEventAction handleEvent(IrcEvent event) throws Exception;
}
