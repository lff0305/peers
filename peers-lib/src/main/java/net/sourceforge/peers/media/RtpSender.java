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
    
    Copyright 2008, 2009, 2010, 2011 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import net.sourceforge.peers.rtp.RtpPacket;
import net.sourceforge.peers.rtp.RtpSession;
import net.sourceforge.peers.sdp.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtpSender implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private RtpSession rtpSession;
    private boolean isStopped;
    private Codec codec;
    private List<RtpPacket> pushedPackets;

    public RtpSender(PipedInputStream encodedData, RtpSession rtpSession,
                     boolean mediaDebug, Codec codec, String peersHome,
                     CountDownLatch latch) {
        this.rtpSession = rtpSession;
        this.codec = codec;
        isStopped = false;
        pushedPackets = Collections.synchronizedList(new ArrayList<RtpPacket>());
    }

    public void run() {
        RtpPacket rtpPacket = new RtpPacket();
        rtpPacket.setVersion(2);
        rtpPacket.setPadding(false);
        rtpPacket.setExtension(false);
        rtpPacket.setCsrcCount(0);
        rtpPacket.setMarker(false);
        rtpPacket.setPayloadType(codec.getPayloadType());
        Random random = new Random();
        int sequenceNumber = random.nextInt();
        rtpPacket.setSequenceNumber(sequenceNumber);
        rtpPacket.setSsrc(random.nextInt());
        int buf_size = Capture.BUFFER_SIZE / 2;
        byte[] buffer = new byte[buf_size];
        int timestamp = 0;
        int numBytesRead;
        int tempBytesRead;
        long sleepTime = 0;
        long offset = 0;
        long lastSentTime = System.nanoTime();
        // indicate if its the first time that we send a packet (dont wait)
        boolean firstTime = true;

        while (!isStopped) {
            numBytesRead = 0;
            boolean send = false;
            byte[] trimmedBuffer;
            if (numBytesRead < buffer.length) {
                trimmedBuffer = new byte[numBytesRead];
                System.arraycopy(buffer, 0, trimmedBuffer, 0, numBytesRead);
            } else {
                trimmedBuffer = buffer;
            }
            if (pushedPackets.size() > 0) {
                RtpPacket pushedPacket = pushedPackets.remove(0);
                rtpPacket.setMarker(pushedPacket.isMarker());
                rtpPacket.setPayloadType(pushedPacket.getPayloadType());
                rtpPacket.setIncrementTimeStamp(pushedPacket.isIncrementTimeStamp());
                byte[] data = pushedPacket.getData();
                rtpPacket.setData(data);
                send = true;
                logger.info("Send {} bytes", data.length);
            } else {
                if (rtpPacket.getPayloadType() != codec.getPayloadType()) {
                    rtpPacket.setPayloadType(codec.getPayloadType());
                    rtpPacket.setMarker(false);
                }
                rtpPacket.setData(trimmedBuffer);
            }

            rtpPacket.setSequenceNumber(sequenceNumber++);
            if (rtpPacket.isIncrementTimeStamp()) {
                timestamp += buf_size;
            }
            rtpPacket.setTimestamp(timestamp);
            if (firstTime) {
                rtpSession.send(rtpPacket);
                lastSentTime = System.nanoTime();
                firstTime = false;
                continue;
            }
            sleepTime = 19500000 - (System.nanoTime() - lastSentTime) + offset;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(Math.round(sleepTime / 1000000f));
                } catch (InterruptedException e) {
                    logger.error("Thread interrupted", e);
                    return;
                }
                rtpSession.send(rtpPacket);
                lastSentTime = System.nanoTime();
                offset = 0;
            } else {
                rtpSession.send(rtpPacket);
                lastSentTime = System.nanoTime();
                if (sleepTime < -20000000) {
                    offset = sleepTime + 20000000;
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public void pushPackets(List<RtpPacket> rtpPackets) {
        this.pushedPackets.addAll(rtpPackets);
    }

    public void sendWAV(String file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] headers = new byte[44];
            fis.read(headers);
            byte[] buffer = new byte[320];
            int size = fis.read(buffer);
            PcmuEncoder encoder = new PcmuEncoder();
            while (size != -1) {
                byte[] data = encoder.process(buffer, size);
                send(data);
                size = fis.read(buffer);
            }
        } catch (FileNotFoundException e) {
            logger.error("File not found {}", file, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void send(byte[] data) {
        RtpPacket rtpPacket = new RtpPacket();
        rtpPacket.setVersion(2);
        rtpPacket.setPadding(false);
        rtpPacket.setExtension(false);
        rtpPacket.setCsrcCount(0);
        rtpPacket.setMarker(false);
        rtpPacket.setPayloadType(codec.getPayloadType());
        Random random = new Random();
        int sequenceNumber = random.nextInt();
        rtpPacket.setSequenceNumber(sequenceNumber);
        rtpPacket.setSsrc(random.nextInt());

        rtpPacket.setData(data);

        pushedPackets.add(rtpPacket);
    }
}
