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

package net.sourceforge.peers.sip.transport;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.sip.RFC3261;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final int KEEY_ALIVE_INTERVAL = 25; // seconds

    protected InetAddress inetAddress;
    protected int port;
    protected int localPort;
    private Config config;
    private String transportName;
    private Timer timer;

    public MessageSender(int localPort, InetAddress inetAddress,
                         int port, Config config,
                         String transportName) {
        super();
        this.localPort = localPort;
        this.inetAddress = inetAddress;
        this.port = port;
        this.config = config;
        this.transportName = transportName;
        timer = new Timer(getClass().getSimpleName() + " "
                + Timer.class.getSimpleName());
        //TODO check config
        timer.scheduleAtFixedRate(new KeepAlive(), 0,
                1000 * KEEY_ALIVE_INTERVAL);
    }

    public abstract void sendMessage(SipMessage sipMessage) throws IOException;

    public abstract void sendBytes(byte[] bytes) throws IOException;

    public String getContact() {
        StringBuffer buf = new StringBuffer();
        String myAddress = config.getPublicAddress();
        if (myAddress == null) {
            myAddress = config.getLocalAddress();
        }
        buf.append(myAddress);
        buf.append(RFC3261.TRANSPORT_PORT_SEP);
        //buf.append(config.getSipPort());
        buf.append(localPort);
        buf.append(RFC3261.PARAM_SEPARATOR);
        buf.append(RFC3261.PARAM_TRANSPORT);
        buf.append(RFC3261.PARAM_ASSIGNMENT);
        buf.append(transportName);
        return buf.toString();
    }

    public int getLocalPort() {
        return localPort;
    }

    public void stopKeepAlives() {
        timer.cancel();
    }

    class KeepAlive extends TimerTask {

        @Override
        public void run() {
            byte[] bytes = (RFC3261.CRLF + RFC3261.CRLF).getBytes();
            try {
                sendBytes(bytes);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

}
