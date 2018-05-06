package ninja.oscaz.sparkchat;

import org.eclipse.jetty.websocket.api.Session;

import java.util.UUID;

class LiveSocketConnection {

    private final String socketId = UUID.randomUUID().toString();
    private String username;
    private String channel;
    private boolean isPrivate;

    private Session session;

    private boolean switching = false;

    LiveSocketConnection(String username, String channel) {
        this.username = username;
        this.channel = channel;
    }

    String getSocketId() {
        return socketId;
    }

    String getUsername() {
        return username;
    }

    String getChannel() {
        return channel;
    }

    void setChannel(String channel) {
        this.channel = channel;
    }

    void setUsername(String username) {
        this.username = username;
    }

    Session getSession() {
        return session;
    }

    void setSession(Session session) {
        this.session = session;
    }

    boolean isSwitching() {
        return switching;
    }

    void setSwitching(boolean switching) {
        this.switching = switching;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
