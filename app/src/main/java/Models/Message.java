package Models;

public class Message {
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUserame(String name) {
        this.username = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    String message;

    public Message(String message, String name) {
        this.message = message;
        this.username = name;
    }

    String username;
    String key;

    public Message(String message, String name, String key) {
        this.message = message;
        this.username = name;
        this.key = key;
    }

    public Message() {
    }

    public String toString() {

        return "Message{"+
                "message='"+message +'\''+
                ", username='"+username+'\''+
                ", key='"+key+ '\''+
                "}";
    }


}


