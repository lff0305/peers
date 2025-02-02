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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class NonInviteServerTransactionStateTrying extends
        NonInviteServerTransactionState {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public NonInviteServerTransactionStateTrying(String id,
                                                 NonInviteServerTransaction nonInviteServerTransaction) {
        super(id, nonInviteServerTransaction);
    }

    @Override
    public void received1xx() {
        NonInviteServerTransactionState nextState =
                nonInviteServerTransaction.PROCEEDING;
        nonInviteServerTransaction.setState(nextState);
        nonInviteServerTransaction.sendLastResponse();
    }

    @Override
    public void received200To699() {
        NonInviteServerTransactionState nextState =
                nonInviteServerTransaction.COMPLETED;
        nonInviteServerTransaction.setState(nextState);
    }

}
