package SFMS.Models;

import org.bson.types.ObjectId;

public class Admin {

    //Declare the Admin Attributes
    private ObjectId adminID;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNum;
    private String username;
    private String password;
    private String extraNotes;

    //Constructor
    public Admin(ObjectId adminID, String firstName, String lastName, String email, String contactNum, String username, String password, String extraNotes)
    {
        this.adminID = adminID;
        this.firstName  = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contactNum = contactNum;
        this.username = username;
        this.password = password;
        this.extraNotes = extraNotes;
    }

    //Getters and Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNum() {
        return contactNum;
    }

    public void setContactNum(String contactNum) {
        this.contactNum = contactNum;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ObjectId getAdminID() {
        return adminID;
    }

    public void setAdminID(ObjectId adminID) {
        this.adminID = adminID;
    }

    public String getExtraNotes() {
        return extraNotes;
    }

    public void setExtraNotes(String extraNotes) {
        this.extraNotes = extraNotes;
    }
}
