package SFMS.Models;

import org.bson.types.ObjectId;

import java.time.LocalDate;

public class Booking {

    private ObjectId bookId;
    private String bookFacility;
    private String bookClientName;
    private String bookContactNum;
    private String bookContactEmail;
    private LocalDate bookDate;
    private String bookTime;
    private String bookDuration;
    private int bookNumPeople;
    private double bookPrice;
    private boolean checkedIn;


    //Constructor
    public Booking(ObjectId bookId, String bookFacility, String bookClientName, String bookContactNum, String bookContactEmail, LocalDate bookDate, String bookTime, String bookDuration, int bookNumPeople, double bookPrice, boolean checkedIn) {
        this.bookId = bookId;
        this.bookFacility = bookFacility;
        this.bookClientName = bookClientName;
        this.bookContactNum = bookContactNum;
        this.bookContactEmail = bookContactEmail;
        this.bookDate = bookDate;
        this.bookTime = bookTime;
        this.bookDuration = bookDuration;
        this.bookNumPeople = bookNumPeople;
        this.bookPrice = bookPrice;
        this.checkedIn = checkedIn;
    }

    //Getters and Setters
    public ObjectId getBookId() {
        return bookId;
    }

    public void setBookId(ObjectId bookId) {
        this.bookId = bookId;
    }

    public String getBookFacility() {
        return bookFacility;
    }

    public void setBookFacility(String bookFacility) {
        this.bookFacility = bookFacility;
    }

    public String getBookClientName() {
        return bookClientName;
    }

    public void setBookClientName(String bookClientName) {
        this.bookClientName = bookClientName;
    }

    public String getBookContactNum() { return bookContactNum; }

    public void setBookContactNum(String bookContactNum) {
        this.bookContactNum = bookContactNum;
    }

    public String getBookContactEmail() { return bookContactEmail; }

    public void setBookContactEmail(String bookContactEmail) {
        this.bookContactEmail = bookContactEmail;
    }

    public LocalDate getBookDate() {
        return bookDate;
    }

    public void setBookDate(LocalDate bookDate) {
        this.bookDate = bookDate;
    }

    public String getBookTime() {
        return bookTime;
    }

    public void setBookTime(String bookTime) {
        this.bookTime = bookTime;
    }

    public String getBookDuration() {
        return bookDuration;
    }

    public void setBookDuration(String bookDuration) {
        this.bookDuration = bookDuration;
    }

    public int getBookNumPeople() {
        return bookNumPeople;
    }

    public void setBookNumPeople(int bookNumPeople) {
        this.bookNumPeople = bookNumPeople;
    }

    public double getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(double bookPrice) {
        this.bookPrice = bookPrice;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}
