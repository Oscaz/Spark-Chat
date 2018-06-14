package ninja.oscaz.sparkchat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ninja.oscaz.sparkchat.pojo.WebSocketPost;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static j2html.TagCreator.*;

public class Main {

    // Object for decoding Json
    static final Gson GSON = new GsonBuilder().create();

    // Hashmap to contain socket uuids as keys to LiveSocketConnections
    static Map<String, LiveSocketConnection> sockets = new ConcurrentHashMap<>();

    // Main method (called upon startup)
    public static void main(String[] args) {
        // Initializing logger plugin (maven dependency)
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        // Setting logger level (configurable)
        root.setLevel(Level.INFO);

        // Initializing webserver through SparkJava
        WebServer.initialize();

        // Resolving pages
        Spark.get("/", WebServer.resolve("enter.vtl"));
        Spark.get("/index", WebServer.resolve("index.vtl"));
        Spark.get("/chatroom", WebServer.resolve("generalchat.vtl"));
        Spark.get("/support", WebServer.resolve("support.vtl"));
        Spark.get("/invaders", WebServer.resolve("invaders.vtl"));

        // A Post for client to receive authentication token (socket-id) responsible as a key for LiveSocketConnection
        Spark.post("/authenticate/", ((request, response) -> {
            if (request.session(true).attribute("socket-id") != null) {
                return request.session().attribute("socket-id");
            } else {
                LiveSocketConnection connection = new LiveSocketConnection("", "");
                sockets.put(connection.getSocketId(), connection);
                request.session(true).attribute("socket-id", connection.getSocketId());
                return connection.getSocketId();
            }
        }));

        // A Post for the client to receive open chatrooms (and number online)
        // (private rooms ignored) to display as buttons to join in page /index
        Spark.post("/chatrooms/", ((request, response) -> {
            List<String> chatrooms = new ArrayList<>();
            Map<String, Integer> chatroomNumbers = new HashMap<>();
            for (LiveSocketConnection connection : sockets.values()) {
                if (connection.getChannel().equals("") || connection.getChannel() == null || connection.isPrivate()) continue;
                if (chatrooms.contains(connection.getChannel().toLowerCase())) {
                    chatroomNumbers.put(connection.getChannel().toLowerCase(), chatroomNumbers.get(connection.getChannel()) + 1);
                    continue;
                }
                chatrooms.add(connection.getChannel().toLowerCase());
                chatroomNumbers.put(connection.getChannel().toLowerCase(), 1);
            }
            StringBuilder builder = new StringBuilder("");
            for (String chatroom : chatrooms) {
                String chatroomC = chatroom.substring(0, 1).toUpperCase() + chatroom.substring(1);
                builder.append(chatroomC);
                builder.append(":");
                builder.append(chatroomNumbers.get(chatroom));
                builder.append("/");
            }
            if (builder.length() > 0) {
                builder.delete(builder.length() - 1, builder.length() - 1);
            }
            request.session(true).attribute("chat-rooms", builder.toString());
            return builder.toString();
        }));
    }

    //Sends a message from one user to all users within the same channel
    static void broadcastMessage(LiveSocketConnection connection, String channel, String message) {

        WebSocketPost post;

        if (connection == null) {
            System.out.println(createHtmlMessageFromConsole(message));
            post = new WebSocketPost("chat-message", createHtmlMessageFromConsole(message));
        }
        else
            post = new WebSocketPost("chat-message", createHtmlMessageFromSender(connection.getUsername(), message, connection.getAvatar()));

        System.out.println(post);

        String json = GSON.toJson(post);

        sockets.values().stream()
                .filter(socket -> Objects.equals(channel, socket.getChannel()))
                .forEach(socket -> {
                    if (socket.getSession() != null && socket.getSession().isOpen()) {
                        try {
                            socket.getSession().getRemote().sendString(json);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    //Builds a HTML element with a sender-name, a message, and a timestamp, using J2HTML
    private static String createHtmlMessageFromSender(String sender, String message, int avatar) {
        return div(
                div(attrs(".timestamp"), new SimpleDateFormat("HH:mm:ss").format(new Date())).withClass("datetime"),
                div(p(img().withSrc("img/avatar/" + avatar + ".png"), (b(" " + sender + " says: ")))).withClass("message-user"),
                p(message)
        ).withClass("messageBox").render();

    }

    //Builds a HTML element with a sender-name, a message, and a timestamp, using J2HTML
    private static String createHtmlMessageFromConsole(String message) {
        return div(
                div(attrs(".timestamp"), new SimpleDateFormat("HH:mm:ss").format(new Date())).withClass("datetime"),
                p(message)
        ).withClass("messageBox").render();

    }

}
