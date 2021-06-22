package SFMS.Models;

import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;

public class League {

    private ObjectId leagueID;
    private String name;
    private String activity;
    private LocalDate startDate;
    private LocalDate endDate;
    private String age;
    private ArrayList<String> teams;

    public League(ObjectId leagueID, String name, String activity, LocalDate startDate, LocalDate endDate, String age, ArrayList<String> teams) {
        this.leagueID = leagueID;
        this.name = name;
        this.activity = activity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.age = age;
        this.teams = teams;
    }

    public ObjectId getLeagueID() {
        return leagueID;
    }

    public void setLeagueID(ObjectId leagueID) {
        this.leagueID = leagueID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public ArrayList<String> getTeams() {
        return teams;
    }

    public void setTeams(ArrayList<String> teams) {
        this.teams = teams;
    }
}
