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

package net.sourceforge.peers.media;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

import net.sourceforge.peers.rtp.RFC3551;
import net.sourceforge.peers.rtp.RtpListener;
import net.sourceforge.peers.rtp.RtpPacket;
import net.sourceforge.peers.rtp.RtpSession;
import net.sourceforge.peers.sdp.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncomingRtpReader implements RtpListener {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private RtpSession rtpSession;
    private AbstractSoundManager soundManager;
    private Decoder decoder;

    public IncomingRtpReader(RtpSession rtpSession, AbstractSoundManager soundManager, Codec codec)
            throws IOException {
        logger.info("IncomingRtpReader for {} playback codec {}", rtpSession, codec.toString().trim());
        this.rtpSession = rtpSession;
        this.soundManager = soundManager;
        switch (codec.getPayloadType()) {
            case RFC3551.PAYLOAD_TYPE_PCMU:
                decoder = new PcmuDecoder();
                break;
            case RFC3551.PAYLOAD_TYPE_PCMA:
                decoder = new PcmaDecoder();
                break;
            default:
                throw new RuntimeException("unsupported payload type");
        }
        rtpSession.addRtpListener(this);
    }

    public void start() {
        rtpSession.start();
    }

    @Override
    public void receivedRtpPacket(RtpPacket rtpPacket) {
        byte[] rawBuf = decoder.process(rtpPacket.getData());
        logger.info("Receive {} bytes from RTP", rawBuf.length);
        try {
            FileOutputStream bos = new FileOutputStream("/audio/media.raw", true);
            bos.write(rawBuf);
            bos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
