package tech.rithm.webknockers.models;

/**
 * Created by rithm on 3/2/2017.
 */

public class ChatRoom {

    private long createdDate;
    private long lastReadDate;
    private int hasNewMSG;
    private int numMSGS;
    private String table;

    public ChatRoom(){

    }

    public ChatRoom(long created_date, long last_read_date, boolean has_new_msg, int num_msgs, String table_name) {

    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getLastReadDate() {
        return lastReadDate;
    }

    public void setLastReadDate(long lastReadDate) {
        this.lastReadDate = lastReadDate;
    }

    public int getHasNewMSG() {
        return hasNewMSG;
    }

    public void setHasNewMSG(int hasNewMSG) {
        this.hasNewMSG = hasNewMSG;
    }

    public int getNumMSGS() {
        return numMSGS;
    }

    public void setNumMSGS(int numMSGS) {
        this.numMSGS = numMSGS;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public String toString(){
        return "** table->[" + table + "] hasNewMsg->["+String.valueOf(hasNewMSG)+ "]";
    }
}
