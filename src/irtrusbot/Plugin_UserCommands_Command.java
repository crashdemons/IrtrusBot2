/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author crash
 */
public class Plugin_UserCommands_Command {
    public String name="";
    public String parameter="";
    public ArrayList<String> parameters = new ArrayList<String>();
    public Plugin_UserCommands_Command(String rawCommand){
        int pSpace=rawCommand.indexOf(' ');
        name=rawCommand;
        if(pSpace!=-1){
            name=rawCommand.substring(0, pSpace);
            parameter=rawCommand.substring(pSpace);
            parameters = new ArrayList<String>(
                    Arrays.asList( 
                        parameter.split("\\s+")
                    )
            );
        }
    }
}
