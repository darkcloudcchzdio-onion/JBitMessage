package onion.darkcloudcchzdio.jbitmessage.pipes;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = -1050855183687692054L;
    private String source;
    private String destination;
    private String content;

    public Message() {
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
