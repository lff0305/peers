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

import net.sourceforge.peers.sip.RFC3261;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class NonInviteClientTransactionStateCompleted extends
        NonInviteClientTransactionState {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public NonInviteClientTransactionStateCompleted(String id,
                                                    NonInviteClientTransaction nonInviteClientTransaction) {
        super(id, nonInviteClientTransaction);
        int delay = 0;
        if (RFC3261.TRANSPORT_UDP.equals(
                nonInviteClientTransaction.transport)) {
            delay = RFC3261.TIMER_T4;
        }
        nonInviteClientTransaction.timer.schedule(
                nonInviteClientTransaction.new TimerK(), delay);
    }

    @Override
    public void timerKFires() {
        NonInviteClientTransactionState nextState =
                nonInviteClientTransaction.TERMINATED;
        nonInviteClientTransaction.setState(nextState);
    }

}
