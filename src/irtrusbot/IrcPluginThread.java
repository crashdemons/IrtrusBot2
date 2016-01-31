/*
 * Copyright (C) 2016 crash
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package irtrusbot;

/**
 *
 * @author crash
 */
public class IrcPluginThread extends Thread {
    private final IrcPlugin plugin;
    private final IrcEvent currentEvent;
    private IrcEventAction lastAction=IrcEventAction.CONTINUE;;
    public IrcPluginThread(IrcPlugin plug, IrcEvent event){
        plugin=plug;
        currentEvent=event;
    }
    //code for the new thread,
    //take our preset arguments from outside the thread (currentEvent, lastAction) and use them.
    /**
     * Method used to start plugin event handling code in a controllable Thread
     */
    @Override
    public final void run(){
        try{
            lastAction=plugin.handleEvent(currentEvent);
        }catch(Exception e){
            //we don't care what exception an arbitrary plugin generated, frankly.
            //maybe we could implement logging here.
        }
    }
    public final IrcEventAction getAction(){
        return lastAction;
    }
}
