package SFMS.Models;

import org.bson.types.ObjectId;

import java.util.ArrayList;

public class Team {

    private ObjectId teamID;
    private String name;
    private String captain;
    private int numMembers;
    private ArrayList<String> members;

    public Team(ObjectId teamID, String name, String captain, int numMembers, ArrayList<String> members) {
        this.teamID = teamID;
        this.name = name;
        this.captain = captain;
        this.numMembers = numMembers;
        this.members = members;
    }

    public ObjectId getTeamID() {
        return teamID;
    }

    public void setTeamID(ObjectId teamID) {
        this.teamID = teamID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaptain() {
        return captain;
    }

    public void setCaptain(String captain) {
        this.captain = captain;
    }

    public int getNumMembers() {
        return numMembers;
    }

    public void setNumMembers(int numMembers) {
        this.numMembers = numMembers;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }
}
