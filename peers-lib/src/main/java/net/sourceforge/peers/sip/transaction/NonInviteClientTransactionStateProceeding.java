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
import net.sourceforge.peers.sip.transport.SipResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class NonInviteClientTransactionStateProceeding extends
        NonInviteClientTransactionState {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public NonInviteClientTransactionStateProceeding(String id,
                                                     NonInviteClientTransaction nonInviteClientTransaction) {
        super(id, nonInviteClientTransaction);
    }

    @Override
    public void timerEFires() {
        NonInviteClientTransactionState nextState = nonInviteClientTransaction.PROCEEDING;
        nonInviteClientTransaction.setState(nextState);
        ++nonInviteClientTransaction.nbRetrans;
        nonInviteClientTransaction.sendRetrans(RFC3261.TIMER_T2);
    }

    @Override
    public void timerFFires() {
        timerFFiresOrTransportError();
    }

    @Override
    public void transportError() {
        timerFFiresOrTransportError();
    }

    private void timerFFiresOrTransportError() {
        NonInviteClientTransactionState nextState = nonInviteClientTransaction.TERMINATED;
        nonInviteClientTransaction.setState(nextState);
        nonInviteClientTransaction.transactionUser.transactionTimeout(
                nonInviteClientTransaction);
    }

    @Override
    public void received1xx() {
        NonInviteClientTransactionState nextState = nonInviteClientTransaction.PROCEEDING;
        nonInviteClientTransaction.setState(nextState);
    }

    @Override
    public void received200To699() {
        NonInviteClientTransactionState nextState = nonInviteClientTransaction.COMPLETED;
        nonInviteClientTransaction.setState(nextState);
        SipResponse response = nonInviteClientTransaction.getLastResponse();
        int code = response.getStatusCode();
        if (code < RFC3261.CODE_MIN_REDIR) {
            nonInviteClientTransaction.transactionUser.successResponseReceived(
                    response, nonInviteClientTransaction);
        } else {
            nonInviteClientTransaction.transactionUser.errResponseReceived(
                    response);
        }
    }

}
