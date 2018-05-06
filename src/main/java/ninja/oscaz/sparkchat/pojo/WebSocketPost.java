package ninja.oscaz.sparkchat.pojo;

public class WebSocketPost {

    // POJO for containing post (authenticate / chatrooms)

    private String contentType;
    private String contents;

    public WebSocketPost(String contentType, String contents) {
        this.contentType = contentType;
        this.contents = contents;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

}
