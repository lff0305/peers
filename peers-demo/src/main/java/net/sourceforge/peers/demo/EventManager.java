package net.sourceforge.peers.demo;

import java.lang.invoke.MethodHandles;
import java.net.SocketException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.javaxsound.JavaxSoundManager;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventManager implements SipListener {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private UserAgent userAgent;
    private SipRequest sipRequest;
    private CommandsReader commandsReader;
    
    public EventManager() throws SocketException {
        Config config = new CustomConfig();
        JavaxSoundManager javaxSoundManager = new JavaxSoundManager(false, null);
        userAgent = new UserAgent(this, config, javaxSoundManager);
        new Thread() {
            public void run() {
                try {
                    userAgent.register();
                } catch (SipUriSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        commandsReader = new CommandsReader(this);
        commandsReader.start();
    }
    
    
    // commands methods
    public void call(final String callee) {
        new Thread() {
            @Override
            public void run() {
                try {
                    sipRequest = userAgent.invite(callee, null);
                } catch (SipUriSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    
    public void hangup() {
        new Thread() {
            @Override
            public void run() {
                userAgent.terminate(sipRequest);
            }
        }.start();
    }
    
    
    // SipListener methods
    
    public void registering(SipRequest sipRequest) {
        logger.info("Registering {}", sipRequest);
    }

    public void registerSuccessful(SipResponse sipResponse) {
        logger.info("RegisterSuccessful {}", sipResponse);
    }

    public void registerFailed(SipResponse sipResponse) {
        logger.info("Register Failed {}", sipResponse);
    }

    public void incomingCall(SipRequest sipRequest, SipResponse provResponse) {
        logger.info("incomingCall ");
    }

    public void remoteHangup(SipRequest sipRequest) {
        logger.info("remoteHangup");
    }

    public void ringing(SipResponse sipResponse) {
        logger.info("ringing");
    }

    public void calleePickup(SipResponse sipResponse) {
        logger.info("calleePickup");
    }

    public void error(SipResponse sipResponse) {
        logger.info("error {}", sipResponse);
    }

    public static void main(String[] args) {
        try {
            new EventManager();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void dtmf() {
        try {
            String digits = "9999";
            userAgent.getMediaManager().sendDtmf('9');
            Thread.sleep(100);
            userAgent.getMediaManager().sendDtmf('9');
            Thread.sleep(100);
            userAgent.getMediaManager().sendDtmf('9');
            Thread.sleep(100);
            userAgent.getMediaManager().sendDtmf('9');
            logger.info(digits);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
