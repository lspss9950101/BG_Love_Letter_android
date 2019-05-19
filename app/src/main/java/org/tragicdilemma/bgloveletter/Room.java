package org.tragicdilemma.bgloveletter;

import org.json.JSONException;
import org.json.JSONObject;

public class Room {
    private String creator, roomNumber;
    private Integer playerCount;
    private Boolean isStarted;

    public Room(String obj){
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonObj != null){
            try {
                this.creator = jsonObj.getString("creator");
                this.roomNumber = jsonObj.getString("roomNumber");
                this.playerCount = jsonObj.getInt("player");
                this.isStarted = jsonObj.getBoolean("started");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getCreator(){
        return creator;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public Boolean getState() {
        return isStarted;
    }
}
