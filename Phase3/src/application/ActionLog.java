package application;

import java.sql.Timestamp;

public class ActionLog {
    private int id;
    private String username;
    private String actionDescription;
    private Timestamp logTime;
    
    public ActionLog(int id, String username, String actionDescription, Timestamp logTime) {
        this.id = id;
        this.username = username;
        this.actionDescription = actionDescription;
        this.logTime = logTime;
    }
    
    public int getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getActionDescription() {
        return actionDescription;
    }
    
    public Timestamp getLogTime() {
        return logTime;
    }
    
    @Override
    public String toString() {
        return String.format("Log[id=%d, user=%s, action=%s, time=%s]", 
            id, username, actionDescription, logTime.toString());
    }
}
