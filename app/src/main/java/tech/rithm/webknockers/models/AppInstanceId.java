package tech.rithm.webknockers.models;

/**
 * Created by rithm on 2/14/2017.
 */

public class AppInstanceId {

    private String app_instance_token;

    public AppInstanceId() {

    }

    public AppInstanceId(String app_instance_token){
        this.app_instance_token = app_instance_token;
    }

    public String getApp_instance_token() {
        return app_instance_token;
    }

    public void setApp_instance_token(String app_instance_token) {
        this.app_instance_token = app_instance_token;
    }
}
