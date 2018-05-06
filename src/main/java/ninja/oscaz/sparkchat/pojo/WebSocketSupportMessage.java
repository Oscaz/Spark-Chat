package ninja.oscaz.sparkchat.pojo;

public class WebSocketSupportMessage {

    // POJO for keeping info of a support message (suggestion)

    private String type;
    private String message;
    private String email;
    private String name;

    public WebSocketSupportMessage(String type, String message, String email, String name) {
        this.type = type;
        this.message = message;
        this.email = email;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
