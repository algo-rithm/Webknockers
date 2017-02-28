package tech.rithm.webknockers.models;

/**
 * Created by rithm on 2/13/2017.
 */

public class WebChatMessage {

    private String id;
    private String text;
    private String name;
    private String photoUrl;
    private Long timeStamp;

    public WebChatMessage() {

    }

    public WebChatMessage(String text, String name, String photoUrl, Long timeStamp) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.timeStamp = timeStamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
