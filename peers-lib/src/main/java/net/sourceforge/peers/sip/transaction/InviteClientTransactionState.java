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

package net.sourceforge.peers.sip.transaction;

import net.sourceforge.peers.sip.AbstractState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;


public abstract class InviteClientTransactionState extends AbstractState {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected InviteClientTransaction inviteClientTransaction;

    public InviteClientTransactionState(String id,
                                        InviteClientTransaction inviteClientTransaction) {
        super(id);
        this.inviteClientTransaction = inviteClientTransaction;
    }

    public void start() {
    }

    public void timerAFires() {
    }

    public void timerBFires() {
    }

    public void received2xx() {
    }

    public void received1xx() {
    }

    public void received300To699() {
    }

    public void transportError() {
    }

    public void timerDFires() {
    }

}
