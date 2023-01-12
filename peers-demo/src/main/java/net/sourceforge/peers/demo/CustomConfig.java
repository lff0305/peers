package net.sourceforge.peers.demo;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;

public class CustomConfig implements Config {

    private InetAddress publicIpAddress;

    @Override
    public InetAddress getLocalInetAddress() {
        InetAddress inetAddress;
        try {
            // if you have only one active network interface, getLocalHost()
            // should be enough
            //inetAddress = InetAddress.getLocalHost();
            // if you have several network interfaces like I do,
            // select the right one after running ipconfig or ifconfig
            inetAddress = InetAddress.getByName(getLocalAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
        return inetAddress;
    }

    @Override
    public String getLocalAddress() {
        return "0.0.0.0";
    }

    @Override
    public InetAddress getPublicInetAddress() { return publicIpAddress; }

    @Override
    public String getPublicAddress() {
        return "18.136.33.9";
    }

    @Override public String getUserPart() { return "+6599991234"; }
    @Override public String getDomain() { return "34.199.3.47"; }
    @Override public String getPassword() { return "Telepathy@2020"; }

//    @Override public String getUserPart() { return "tester"; }
//    @Override public String getDomain() { return "10.12.10.192"; }
//    @Override public String getPassword() { return "Telepathy2020"; }
    @Override
    public MediaMode getMediaMode() { return MediaMode.file; }

    @Override public String getAuthorizationUsername() { return getUserPart(); }

    @Override
    public void setPublicInetAddress(InetAddress inetAddress) {
        publicIpAddress = inetAddress;
    }
    
    @Override public SipURI getOutboundProxy() { return null; }
    @Override public int getSipPort() { return 30100; }
    @Override public boolean isMediaDebug() { return false; }
    @Override public String getMediaFile() {
        return "media.raw";
    }
    @Override public int getRtpPort() { return 30102; }
    @Override public void setLocalInetAddress(InetAddress inetAddress) { }
    @Override public void setUserPart(String userPart) { }
    @Override public void setDomain(String domain) { }
    @Override public void setPassword(String password) { }
    @Override public void setOutboundProxy(SipURI outboundProxy) { }
    @Override public void setSipPort(int sipPort) { }
    @Override public void setMediaMode(MediaMode mediaMode) { }
    @Override public void setMediaDebug(boolean mediaDebug) { }
    @Override public void setMediaFile(String mediaFile) { }
    @Override public void setRtpPort(int rtpPort) { }
    @Override public void save() { }
    @Override public void setAuthorizationUsername(String authorizationUsername) { }
    
}
