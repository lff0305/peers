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
    
    Copyright 2010-2013 Yohann Martineau 
*/

package net.sourceforge.peers.rtp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import net.sourceforge.peers.media.AbstractSoundManager;
import net.sourceforge.peers.media.CaptureRtpSender;
import net.sourceforge.peers.media.RTPManager;
import net.sourceforge.peers.media.RtpSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * can be instantiated on UAC INVITE sending or on UAS 200 OK sending
 */
public class RtpSession {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private InetAddress remoteAddress;
    private int remotePort;
    private DatagramSocket datagramSocket;
    private ExecutorService executorService;
    private List<RtpListener> rtpListeners;
    private RtpParser rtpParser;
    private FileOutputStream rtpSessionOutput;
    private FileOutputStream rtpSessionInput;
    private boolean mediaDebug;
    private String peersHome;

    private final String callId;

    public RtpSession(String callId, InetAddress localAddress, DatagramSocket datagramSocket, boolean mediaDebug, String peersHome) {
        this.callId = callId;
        this.mediaDebug = mediaDebug;
        this.peersHome = peersHome;
        this.datagramSocket = datagramSocket;
        rtpListeners = new ArrayList<>();
        rtpParser = new RtpParser();
        executorService = Executors.newSingleThreadExecutor();
    }

    public synchronized void start() {
        logger.info("RTP Session Start, mediaDebug = {}, remoteAddress = {}, remotePort = {}", this.mediaDebug, this.remoteAddress, this.remotePort);
        if (mediaDebug) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String date = simpleDateFormat.format(new Date());
            String dir = peersHome + File.separator + AbstractSoundManager.MEDIA_DIR + File.separator;
            String fileName = dir + date + "_rtp_session.output";
            try {
                rtpSessionOutput = new FileOutputStream(fileName);
                fileName = dir + date + "_rtp_session.input";
                rtpSessionInput = new FileOutputStream(fileName);
            } catch (FileNotFoundException e) {
                logger.error("cannot create file", e);
                return;
            }
        }
        Receiver receiver = new Receiver(callId);
        executorService.submit(receiver);
        executorService.submit(()-> {
            long ts = receiver.lastRTPTimestamp;
            long idle = 3000;
            while (true) {
                if (ts != 0 && System.currentTimeMillis() - ts > idle) {
                    logger.info("RTP expired for {}", idle, callId);
                }

                CaptureRtpSender sender = RTPManager.getSender(callId);
                if (sender == null) {
                    logger.error("Failed to get sender for {}", callId);
                    return;
                }

                RtpSender rtpSender = sender.getRtpSender();
                rtpSender.sendWAV("i_want_to_make_an_appointment_for_my_car.wav");

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void stop() {
        executorService.shutdown();
    }

    public void addRtpListener(RtpListener rtpListener) {
        rtpListeners.add(rtpListener);
    }

    public synchronized void send(RtpPacket rtpPacket) {
        if (datagramSocket == null) {
            logger.warn("UDP Socket has been closed");
            return;
        }

        if (rtpPacket.getData().length == 0) {
            return;
        }

        byte[] buf = rtpParser.encode(rtpPacket);
        final DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, remoteAddress, remotePort);
        if (!datagramSocket.isClosed()) {
            try {
                logger.info("Send RTP {} bytes", buf.length);
                datagramSocket.send(datagramPacket);
            } catch (IOException e) {
                logger.error("Failed to send", e);
            }
        }

        if (mediaDebug) {
            try {
                rtpSessionOutput.write(buf);
            } catch (IOException e) {
                logger.error("cannot write to file", e);
            }
        }
    }

    public void setRemoteAddress(InetAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    private void closeFileAndDatagramSocket() {
        if (mediaDebug) {
            try {
                rtpSessionOutput.close();
                rtpSessionInput.close();
            } catch (IOException e) {
                logger.error("cannot close file", e);
            }
        }
        datagramSocket.close();
    }

    class Receiver implements Runnable {

        private final String callId;

        public Receiver(String callId) {
            this.callId = callId;
        }
        private long lastRTPTimestamp = -1;

        @Override
        public void run() {

            logger.info("Receiver started {}", datagramSocket.getLocalPort());

            while (true) {
                int receiveBufferSize;
                try {
                    receiveBufferSize = datagramSocket.getReceiveBufferSize();
                } catch (SocketException e) {
                    logger.error("cannot get datagram socket receive buffer size", e);
                    return;
                }
                byte[] buf = new byte[receiveBufferSize];
                final DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                try {
                    datagramSocket.receive(datagramPacket);
                    logger.info("Received {}", datagramPacket.getData() == null ? 0: datagramPacket.getLength());
                    lastRTPTimestamp = System.currentTimeMillis();
                    process(datagramPacket);
                    continue;
                } catch (SocketTimeoutException e) {
                    // no data
                } catch (IOException e) {
                    logger.error("cannot receive packet", e);
                    return;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        private void process(DatagramPacket datagramPacket) {
            byte[] data = datagramPacket.getData();
            int offset = datagramPacket.getOffset();
            int length = datagramPacket.getLength();
            byte[] trimmedData = new byte[length];
            System.arraycopy(data, offset, trimmedData, 0, length);
            RtpPacket rtpPacket = rtpParser.decode(trimmedData);
            for (RtpListener rtpListener : rtpListeners) {
                rtpListener.receivedRtpPacket(rtpPacket);
            }
        }
    }

    public boolean isSocketClosed() {
        if (datagramSocket == null) {
            return true;
        }
        return datagramSocket.isClosed();
    }

}
