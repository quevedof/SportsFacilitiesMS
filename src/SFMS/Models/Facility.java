package SFMS.Models;

import org.bson.types.ObjectId;

public class Facility {

    //Variables a facility must have
    private ObjectId facId;
    private String facName;
    private String facActivity;
    private String facType;
    private double facPrice;
    private int facMinPeople;
    private int facMaxPeople;
    private String facExtraNotes;

    //Constructor
    public Facility(ObjectId facId, String facName, String facActivity, String facType, double facPrice, int facMinPeople, int facMaxPeople, String facExtraNotes)
    {
        this.facId = facId;
        this.facName = facName;
        this.facActivity = facActivity;
        this.facType = facType;
        this.facPrice = facPrice;
        this.facMinPeople = facMinPeople;
        this.facMaxPeople = facMaxPeople;
        this.facExtraNotes = facExtraNotes;
    }

    //Getters and setters
    public String getFacName() {
        return facName;
    }

    public void setFacName(String facName) {
        this.facName = facName;
    }

    public String getFacActivity() {
        return facActivity;
    }

    public void setFacActivity(String facActivity) {
        this.facActivity = facActivity;
    }

    public String getFacType() {
        return facType;
    }

    public void setFacType(String facType) {
        this.facType = facType;
    }

    public double getFacPrice() {
        return facPrice;
    }

    public void setFacPrice(double facPrice) {
        this.facPrice = facPrice;
    }

    public int getFacMinPeople() {
        return facMinPeople;
    }

    public void setFacMinPeople(int facMinPeople) {
        this.facMinPeople = facMinPeople;
    }

    public int getFacMaxPeople() {
        return facMaxPeople;
    }

    public void setFacMaxPeople(int facMaxPeople) {
        this.facMaxPeople = facMaxPeople;
    }

    public String getFacExtraNotes() {
        return facExtraNotes;
    }

    public void setFacExtraNotes(String facExtraNotes) {
        this.facExtraNotes = facExtraNotes;
    }

    public ObjectId getFacId() {
        return facId;
    }

    public void setFacId(ObjectId facId) {
        this.facId = facId;
    }
}
