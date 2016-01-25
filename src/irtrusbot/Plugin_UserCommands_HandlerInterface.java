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
public interface Plugin_UserCommands_HandlerInterface {
    public void handleUserCommand(IrcPlugin plugin, Plugin_UserCommands_Command userCommand,IrcEvent event) throws Exception;
}
