/*
 * Copyright (C) 2016 crashdemons
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

/** Class providing plugin priority level defaults.
 * These priority levels determine the order (lowest to highest) in which plugins receive and control events.
 * @author crash
 */
public class IrcPluginPriority {
    /** Raw priority - this level should be reserved for plugins that require high accuracy command processing, such as connection/login control */
    public static final int RAW=1000;
    /** Filter priority - this level of priority should be used for plugins that filter data from other plugins.*/
    public static final int FILTER=2000;
    /** Preprocess priority - plugins of this level format or manipulate events before they are sent to default level plugins */
    public static final int PREPROCESS=3000;
    /** Default priority - plugins of this level received filtered pre-process events and control what goes to post-processing and frontend plugins*/
    public static final int DEFAULT=4000;
    /** Postprocess priority - plugins of this level format or manipulate events before they go to the frontend.*/
    public static final int POSTPROCESS=5000;
    /** Frontend priority - plugins of this level provide information to a user or offer interactive capabilities based on fully formatted and processed events.*/
    public static final int FRONTEND=6000;
    
    /** 
     * Bot priority - this priority level is reserved for the data transmission and event processing actions taken by the IrcBot class after plugin processing
     * <br>
     * For practical purposes, all event messages are seen by the bot, this level provides a value outside of plugin ranges that Events can use to bypass plugin processing.
     * <br>
     * This value will always be within the range [MIN,MAX] but outside of [PLUGIN_MIN,PLUGIN_MAX].
     */
    public static final int BOT=99000;
    
    
    
    /** Numerical range between priority levels. The number of elements allowable in each priority level. */
    public static final int LEVEL_ELEMENTS=1000;//FILTER-RAW
    
    
    /** Minimum supported priority value */
    public static final int MIN=Integer.MIN_VALUE;
    /** Maximum supported priority value */
    public static final int MAX=Integer.MAX_VALUE;
    
    /** Minimum allowed priority value for a plugin */
    public static final int PLUGIN_MIN=RAW;
    /** Maximum allowed priority value for a plugin */
    public static final int PLUGIN_MAX=FRONTEND+(LEVEL_ELEMENTS-1);
    
    /** 
     * Retrieves the last priority value in a level defined by this class.
     * this is equivalent to level+(LEVEL_ELEMENTS-1) and nextlevel-1
     * 
     * @param priority_level the priority level to find the last level of
     * @return The priority value of the last element of the level provided.
     */
    public int getLastElementInLevel(int priority_level){
        return priority_level+(LEVEL_ELEMENTS-1);
    }
}
