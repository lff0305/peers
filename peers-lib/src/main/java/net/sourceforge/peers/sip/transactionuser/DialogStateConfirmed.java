/*
    This file is part of Peers, a java SIP softphone.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    Copyright 2007, 2008, 2009, 2010 Yohann Martineau 
*/

package net.sourceforge.peers.sip.transactionuser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class DialogStateConfirmed extends DialogState {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public DialogStateConfirmed(String id, Dialog dialog) {
        super(id, dialog);
    }

    @Override
    public void receivedOrSent101To199() {
        logger.error(id + " invalid transition");
        throw new IllegalStateException();
    }

    @Override
    public void receivedOrSent2xx() {
        logger.error(id + " invalid transition");
        throw new IllegalStateException();
    }

    @Override
    public void receivedOrSent300To699() {
        logger.error(id + " invalid transition");
        throw new IllegalStateException();
    }

    @Override
    public void receivedOrSentBye() {
        DialogState nextState = dialog.TERMINATED;
        dialog.setState(nextState);
    }

}
