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

package net.sourceforge.peers.media;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import net.sourceforge.peers.rtp.RtpPacket;
import net.sourceforge.peers.rtp.RtpSession;
import net.sourceforge.peers.sdp.Codec;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaManager {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final int DEFAULT_CLOCK = 8000; // Hz

    private UserAgent userAgent;
    private CaptureRtpSender captureRtpSender;
    private IncomingRtpReader incomingRtpReader;
    private RtpSession rtpSession;
    private DtmfFactory dtmfFactory;
    private DatagramSocket datagramSocket;
    private FileReader fileReader;

    public MediaManager(UserAgent userAgent) {
        this.userAgent = userAgent;
        dtmfFactory = new DtmfFactory();
    }

    private void startRtpSessionOnSuccessResponse(String callId, String localAddress, String remoteAddress, int remotePort, Codec codec) {

        logger.info("startRtpSessionOnSuccessResponse {}", callId);

        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(localAddress);
        } catch (UnknownHostException e) {
            logger.error("unknown host: " + localAddress, e);
            return;
        }

        rtpSession = new RtpSession(callId, inetAddress, datagramSocket, userAgent.isMediaDebug(), userAgent.getPeersHome());

        try {
            inetAddress = InetAddress.getByName(remoteAddress);
            rtpSession.setRemoteAddress(inetAddress);
        } catch (UnknownHostException e) {
            logger.error("unknown host: " + remoteAddress, e);
        }
        rtpSession.setRemotePort(remotePort);


        try {
            captureRtpSender = new CaptureRtpSender(rtpSession, userAgent.isMediaDebug(), codec, userAgent.getPeersHome());
            logger.info("CaptureRtpSender created for {}", callId);
            RTPManager.pubSender(callId, captureRtpSender);
        } catch (IOException e) {
            logger.error("input/output error", e);
            return;
        }

        try {
            captureRtpSender.start();
        } catch (IOException e) {
            logger.error("input/output error", e);
        }
    }

    public void successResponseReceived(String callId, String localAddress, String remoteAddress, int remotePort, Codec codec) {

        logger.info("SuccessResponseReceived, {} media {}", callId, userAgent.getMediaMode().toString());

        switch (userAgent.getMediaMode()) {
            case echo:
                Echo echo;
                try {
                    echo = new Echo(datagramSocket, remoteAddress, remotePort);
                } catch (UnknownHostException e) {
                    logger.error("unknown host amongst "
                            + localAddress + " or " + remoteAddress);
                    return;
                }
                userAgent.setEcho(echo);
                Thread echoThread = new Thread(echo, Echo.class.getSimpleName());
                echoThread.start();
                break;
            case file:
                startRtpSessionOnSuccessResponse(callId, localAddress, remoteAddress, remotePort, codec);
                try {
                    incomingRtpReader = new IncomingRtpReader(captureRtpSender.getRtpSession(), null, codec);
                } catch (IOException e) {
                    logger.error("input/output error", e);
                    return;
                }
                incomingRtpReader.start();
                break;
            case none:
            default:
                break;
        }
    }

    private void startRtpSession(String destAddress, int destPort, Codec codec, SoundSource soundSource) {

        logger.info("Start RTP Session");

        rtpSession = new RtpSession(null, userAgent.getConfig().getLocalInetAddress(), datagramSocket, userAgent.isMediaDebug(), userAgent.getPeersHome());

        try {
            InetAddress inetAddress = InetAddress.getByName(destAddress);
            rtpSession.setRemoteAddress(inetAddress);
        } catch (UnknownHostException e) {
            logger.error("unknown host: " + destAddress, e);
        }
        rtpSession.setRemotePort(destPort);

        try {
            captureRtpSender = new CaptureRtpSender(rtpSession, userAgent.isMediaDebug(), codec, userAgent.getPeersHome());
        } catch (IOException e) {
            logger.error("input/output error", e);
            return;
        }
        try {
            captureRtpSender.start();
        } catch (IOException e) {
            logger.error("input/output error", e);
        }

    }

    public void handleAck(String destAddress, int destPort, Codec codec) {
        switch (userAgent.getMediaMode()) {
            case echo:
                Echo echo;
                try {
                    echo = new Echo(datagramSocket, destAddress, destPort);
                } catch (UnknownHostException e) {
                    logger.error("unknown host amongst "
                            + userAgent.getConfig().getLocalInetAddress()
                            .getHostAddress() + " or " + destAddress);
                    return;
                }
                userAgent.setEcho(echo);
                Thread echoThread = new Thread(echo, Echo.class.getSimpleName());
                echoThread.start();
                break;
            case file:
                if (fileReader != null) {
                    fileReader.close();
                }
                String fileName = userAgent.getConfig().getMediaFile();
                fileReader = new FileReader(fileName);
                startRtpSession(destAddress, destPort, codec, fileReader);
                try {
                    incomingRtpReader = new IncomingRtpReader(rtpSession, null, codec);
                } catch (IOException e) {
                    logger.error("input/output error", e);
                    return;
                }
                incomingRtpReader.start();
                break;
            case none:
            default:
                break;
        }
    }

    public void updateRemote(String destAddress, int destPort, Codec codec) {
        switch (userAgent.getMediaMode()) {
            case echo:
                //TODO update echo socket
                break;
            case file:
                try {
                    InetAddress inetAddress = InetAddress.getByName(destAddress);
                    rtpSession.setRemoteAddress(inetAddress);
                } catch (UnknownHostException e) {
                    logger.error("unknown host: " + destAddress, e);
                }
                rtpSession.setRemotePort(destPort);
                break;

            default:
                break;
        }

    }

    public void sendDtmf(char digit) {
        if (captureRtpSender != null) {
            List<RtpPacket> rtpPackets = dtmfFactory.createDtmfPackets(digit);
            RtpSender rtpSender = captureRtpSender.getRtpSender();
            rtpSender.pushPackets(rtpPackets);
        } else {
            logger.info("!!!!");
        }
    }
    public void sendDtmf(String digits) {
        if (captureRtpSender != null) {
            List<RtpPacket> rtpPackets = dtmfFactory.createDtmfPackets(digits);
            RtpSender rtpSender = captureRtpSender.getRtpSender();
            rtpSender.pushPackets(rtpPackets);
        } else {
            logger.info("!!!!");
        }
    }

    public void stopSession() {
        if (rtpSession != null) {
            rtpSession.stop();
            while (!rtpSession.isSocketClosed()) {
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    logger.debug("sleep interrupted");
                }
            }
            rtpSession = null;
        }
        if (incomingRtpReader != null) {
            incomingRtpReader = null;
        }
        if (captureRtpSender != null) {
            captureRtpSender.stop();
            captureRtpSender = null;
        }
        if (datagramSocket != null) {
            datagramSocket = null;
        }

        switch (userAgent.getMediaMode()) {
            case echo:
                Echo echo = userAgent.getEcho();
                if (echo != null) {
                    echo.stop();
                    userAgent.setEcho(null);
                }
                break;
            case file:
                // fileReader.close();
                break;
            default:
                break;
        }
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public FileReader getFileReader() {
        return fileReader;
    }

}
