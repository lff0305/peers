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

package net.sourceforge.peers.media;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.CountDownLatch;

import net.sourceforge.peers.rtp.RFC3551;
import net.sourceforge.peers.rtp.RtpSession;
import net.sourceforge.peers.sdp.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CaptureRtpSender {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final int PIPE_SIZE = 4096;

    private RtpSession rtpSession;
    private Encoder encoder;
    private RtpSender rtpSender;

    public CaptureRtpSender(RtpSession rtpSession, boolean mediaDebug, Codec codec, String peersHome)
            throws IOException {
        super();
        this.rtpSession = rtpSession;
        // the use of PipedInputStream and PipedOutputStream in Capture,
        // Encoder and RtpSender imposes a synchronization point at the
        // end of life of those threads to a void read end dead exceptions
        switch (codec.getPayloadType()) {
            case RFC3551.PAYLOAD_TYPE_PCMU:
                encoder = new PcmuEncoder();
                break;
            case RFC3551.PAYLOAD_TYPE_PCMA:
                encoder = new PcmaEncoder();
                break;
            default:
                throw new RuntimeException("unknown payload type");
        }
        rtpSender = new RtpSender(rtpSession, codec);
    }

    public void start() throws IOException {

        rtpSender.setStopped(false);

        Thread rtpSenderThread = new Thread(rtpSender, RtpSender.class.getSimpleName());

        rtpSenderThread.start();

    }

    public void stop() {
        if (rtpSender != null) {
            rtpSender.setStopped(true);
        }
    }

    public synchronized RtpSession getRtpSession() {
        return rtpSession;
    }

    public RtpSender getRtpSender() {
        return rtpSender;
    }

}
