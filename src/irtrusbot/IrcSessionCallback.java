/*
 * Copyright (C) 2017 crash
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

/** Interface defining methods to be called by special IrcSession events
 * (such as notifying of sudden connection errors)
 * @author crash
 */
public interface IrcSessionCallback {
    /** Method to handle connection error events occurring in an IrcSession instance
     * generally this is provided so that an object owning the IrcSession can provide higher level control or feedback.
     * @param e Exception captured relating to the connection error
     */
    public void onConnectionError(Exception e);
}
