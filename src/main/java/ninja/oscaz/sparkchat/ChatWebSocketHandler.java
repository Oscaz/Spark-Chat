package ninja.oscaz.sparkchat;

import ninja.oscaz.sparkchat.pojo.WebSocketMessage;
import ninja.oscaz.sparkchat.pojo.WebSocketPost;
import ninja.oscaz.sparkchat.pojo.WebSocketSupportMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.stream.IntStream;

@WebSocket
public class ChatWebSocketHandler {

    // Triggers on web socket connecting, debugs.
    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        print(() -> System.out.println("Session started, user is " + user));
    }

    // Triggers on web socket closing, if user in room & not currently switching room, leave chat
    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        // Find socket equal to socket closer
        Main.sockets.values().stream()
                .filter(connection -> user.equals(connection.getSession()))
                .findFirst()
                .ifPresent(connection -> {
                    // If they are in transit return
                    if (connection.isSwitching()) return;
                    // Else, take their channel and inform they have left.
                    Main.broadcastMessage(null, connection.getChannel(), connection.getUsername() + " left the chat");
                    connection.setChannel("");
                });

    }

    // Triggers when data is passed, known as a websocketmessage
    @OnWebSocketMessage
    public void onMessage(Session user, String data) {
        // If message json type is support, trigger email.
        if (Main.GSON.fromJson(data, WebSocketMessage.class).getType().equalsIgnoreCase("support")) {
            WebSocketSupportMessage message = Main.GSON.fromJson(data, WebSocketSupportMessage.class);
            Email.sendToSelf("Support suggestion", "From " + message.getName() + " (" + message.getEmail() + ")" + "\n" +
                       message.getMessage()
            );
            return;
        }

        System.out.println(data);
        WebSocketMessage message = Main.GSON.fromJson(data, WebSocketMessage.class);
        System.out.println(message);

        // If message json type is authenticate, return socket-id to associate with session
        if (message.getType().equalsIgnoreCase("authenticate")) {
            // update socket to fit authentication uuid
            Main.sockets.get(message.getContents()).setSession(user);
            // Set boolean switching so when user leaves chat room unannounced,
            // We know that they are actually in transit (between index and chatroom)
            // and not leaving as it appears.
            if (Main.sockets.get(message.getContents()).isSwitching()) {
                Main.sockets.get(message.getContents()).setSwitching(false);
            }
        }

        // If message json type is choosing username, change LiveSocketConnection username to input
        else if (message.getType().equalsIgnoreCase("put-username")) {
            // Find LiveSocketConnection that has session equal to currently requesting session
            Main.sockets.values().stream()
                    .filter(connection -> user.equals(connection.getSession()))
                    .findFirst()
                    .ifPresent(connection -> {
                        // if nobody else is in use of the username (if self is in use it goes to else close)
                        if (Main.sockets.values().stream()
                                .filter(target -> target.getUsername().equalsIgnoreCase(message.getContents()))
                                .toArray().length == 0) {
                            connection.setUsername(message.getContents());
                            WebSocketPost post = new WebSocketPost("username-response", "true");
                            String json = Main.GSON.toJson(post);
                            this.sendMessage(json, connection.getSession());
                        } else {

                            Main.sockets.values().stream()
                                .filter(target -> target.getSession() == connection.getSession())
                                .findFirst()
                                .ifPresent(target -> {
                                    // Checking if user-name belongs to self (caused by going to front page without warning)
                                    if (target.getUsername().equalsIgnoreCase(connection.getUsername())) {
                                        connection.setUsername(message.getContents());
                                        WebSocketPost post = new WebSocketPost("username-response", "true");
                                        String json = Main.GSON.toJson(post);
                                        this.sendMessage(json, connection.getSession());
                                    } else {
                                        WebSocketPost post = new WebSocketPost("username-response", "false");
                                        String json = Main.GSON.toJson(post);
                                        this.sendMessage(json, connection.getSession());
                                    }
                                });
                        }
                    });
        }

        // If message json type is sending message to channel, find all users of common channel (including sender) and send message
        else if (message.getType().equalsIgnoreCase("chat-message")) {
            Main.sockets.values().stream()
                    .filter(connection -> user.equals(connection.getSession()))
                    .findFirst()
                    // call utility method to broadcast message from arg0 (connection username), arg1 (channel), arg2 (message contents)
                    .ifPresent(connection -> Main.broadcastMessage(connection, connection.getChannel(), message.getContents()));
        }

        // If message json type is switch-channel, set channel in LiveSocketConnection to input,
        // and set boolean switching to true, so as not to trigger a leave event within method onClose.
        // When user reconnects to said channel in page /generalchat, set switching to false to allow leaving.
        else if (message.getType().equalsIgnoreCase("switch-channel")) {
            Main.sockets.values().stream()
                    .filter(connection -> user.equals(connection.getSession()))
                    .findFirst()
                    .ifPresent(connection -> {
                        connection.setSwitching(true);
                        connection.setPrivate(message.isPrivateRoom());
                        Main.sockets.values().forEach(socket -> {
                            if (socket.getChannel().equalsIgnoreCase(message.getContents().toLowerCase())) {
                                if (socket.isPrivate()) connection.setPrivate(true);
                            }
                        });
                        connection.setChannel(message.getContents().toLowerCase());
                        Main.broadcastMessage(null, message.getContents().toLowerCase(), connection.getUsername() + " joined the chat");
                    });
        }

        // If json type is exit-chat, alert others of channel that said user has left chat, and set channel
        // of user to nothing.
        else if (message.getType().equalsIgnoreCase("exit-chat")) {
            Main.sockets.values().stream()
                    .filter(connection -> user.equals(connection.getSession()))
                    .findFirst()
                    .ifPresent(connection -> {
                        Main.broadcastMessage(null, connection.getChannel(), connection.getUsername() + " left the chat");
                        Main.sockets.get(connection.getSocketId()).setChannel("");
                    });
        }
    }


    // Debug print method for debug.
    private void print(Runnable runnable) {
        IntStream.range(0, 3).forEach(i -> System.out.println());
        runnable.run();
        IntStream.range(0, 3).forEach(i -> System.out.println());
    }

    // Util method to send session a message (json)
    private void sendMessage(String message, Session session) {
        try { session.getRemote().sendString(message); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

}