package ninja.oscaz.sparkchat.pojo;

public class WebSocketMessage {

    // POJO for containing socket message (username, message, leave, join, etc)

    private String type;
    private String contents;
    private boolean privateRoom;

    public WebSocketMessage(String type, String contents, boolean privateRoom) {
        this.type = type;
        this.contents = contents;
        this.privateRoom = privateRoom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public boolean isPrivateRoom() {
        return privateRoom;
    }

    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }

}