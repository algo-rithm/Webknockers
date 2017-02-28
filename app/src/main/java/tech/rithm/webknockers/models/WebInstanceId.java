package tech.rithm.webknockers.models;

/**
 * Created by rithm on 2/14/2017.
 */

public class WebInstanceId {

    private String website_instance_token;

    public WebInstanceId() {

    }

    public WebInstanceId(String website_instance_token){
        this.website_instance_token = website_instance_token;
    }

    public String getWebsite_instance_token() {
        return website_instance_token;
    }

    public void setWebsite_instance_token(String website_instance_token) {
        this.website_instance_token = website_instance_token;
    }
}
