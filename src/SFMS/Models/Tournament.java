package SFMS.Models;

import org.bson.types.ObjectId;

import java.time.LocalDate;

public class Tournament {

    private ObjectId id;
    private String name;
    private String activity;
    private String type;
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private String time;
    private String age;
    private int noTeams;
    private String prize;
    private String extraNotes;


    public Tournament(ObjectId id, String name, String activity, String type, LocalDate dateStart, LocalDate dateEnd, String time, String age, int noTeams, String prize, String extraNotes) {
        this.id = id;
        this.name = name;
        this.activity = activity;
        this.type = type;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.time = time;
        this.age = age;
        this.noTeams = noTeams;
        this.prize = prize;
        this.extraNotes = extraNotes;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
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

    public LocalDate getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDate dateStart) {
        this.dateStart = dateStart;
    }

    public LocalDate getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDate dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public int getNoTeams() {
        return noTeams;
    }

    public void setNoTeams(int noTeams) {
        this.noTeams = noTeams;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public String getExtraNotes() {
        return extraNotes;
    }

    public void setExtraNotes(String extraNotes) {
        this.extraNotes = extraNotes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
