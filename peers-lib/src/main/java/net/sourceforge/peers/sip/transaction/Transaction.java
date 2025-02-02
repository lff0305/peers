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
    
    Copyright 2007-2013 Yohann Martineau 
*/

package net.sourceforge.peers.sip.transaction;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class Transaction {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final char ID_SEPARATOR = '|';

    protected String branchId;
    protected String method;

    protected SipRequest request;
    protected List<SipResponse> responses;

    protected Timer timer;
    protected TransportManager transportManager;
    protected TransactionManager transactionManager;


    protected Transaction(String branchId, String method, Timer timer,
                          TransportManager transportManager,
                          TransactionManager transactionManager) {
        this.branchId = branchId;
        this.method = method;
        this.timer = timer;
        this.transportManager = transportManager;
        this.transactionManager = transactionManager;
        responses = Collections.synchronizedList(new ArrayList<>());
    }

    protected String getId() {
        StringBuffer buf = new StringBuffer();
        buf.append(branchId).append(ID_SEPARATOR);
        buf.append(method);
        return buf.toString();
    }

    public SipResponse getLastResponse() {
        if (responses.isEmpty()) {
            return null;
        }
        return responses.get(responses.size() - 1);
    }

    public SipRequest getRequest() {
        return request;
    }

}
