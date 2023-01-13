package net.sourceforge.peers.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RTPManager {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static Map<String, CaptureRtpSender> senderMap = new ConcurrentHashMap<>();

    public static boolean pubSender(String callerId, CaptureRtpSender sender) {
        if (senderMap.putIfAbsent(callerId, sender) != null) {
            logger.warn("Sender {} already exists", callerId);
            return false;
        } else {
            logger.info("Sender {} put done.");
        }
        return true;
    }

    public static CaptureRtpSender getSender(String callId) {
        return senderMap.get(callId);
    }
}
