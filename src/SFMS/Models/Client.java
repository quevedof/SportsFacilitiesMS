package SFMS.Models;

import org.bson.types.ObjectId;

public class Client {

    private ObjectId clientID;
    private String firstName;
    private String lastName;
    private String DoB;
    private String genre;
    private String telNum;
    private String email;
    private String extraNotes;

    public Client(ObjectId clientID, String firstName, String lastName, String doB, String genre, String telNum, String email, String extraNotes) {
        this.clientID = clientID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.DoB = doB;
        this.genre = genre;
        this.telNum = telNum;
        this.email = email;
        this.extraNotes = extraNotes;
    }

    public ObjectId getClientID() {
        return clientID;
    }

    public void setClientID(ObjectId clientID) {
        this.clientID = clientID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDoB() {
        return DoB;
    }

    public void setDoB(String doB) {
        DoB = doB;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTelNum() {
        return telNum;
    }

    public void setTelNum(String telNum) {
        this.telNum = telNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExtraNotes() {
        return extraNotes;
    }

    public void setExtraNotes(String extraNotes) {
        this.extraNotes = extraNotes;
    }
}
