package SFMS.Controllers;

import SFMS.Models.Booking;
import SFMS.Models.DBConnection;
import SFMS.Models.Facility;
import com.mongodb.client.MongoCollection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NewTournamentController {

    @FXML
    private Label lblInfo;

    @FXML
    private Label lblTimeInfo;

    @FXML
    private TextField txtName;

    @FXML
    private ComboBox<String> cbActivity;

    @FXML
    private ComboBox<String> cbType;

    @FXML
    private DatePicker dpDateStart;

    @FXML
    private DatePicker dpDateEnd;

    @FXML
    private ComboBox<String> cbTime;

    @FXML
    private TextField txtMinAge;

    @FXML
    private TextField txtMaxAge;

    @FXML
    private ComboBox<Integer> cbNoTeams;

    @FXML
    private TextField txtPrize;

    @FXML
    private TextArea txtExtraNotes;
    @FXML
    private Button btnClose;

    @FXML
    private Button btnSave;

    //Stores the bookings to be saved in the database after validating date availability
    ArrayList<Document> bookingsDocsToSave = new ArrayList<>();
    //Block availability flagger
    boolean[] blockAvailability= new boolean[5];

    //Number of bookings to be made
    int numBookingsToMake;

    public void initialize()
    {
        setInitialDetails();
    }

    //Proceed to save the tournament after some validation checks
    //Automatically creates bookings for the tournament matches
    public void btnSaveOnAction()
    {
        //Validate fields and date availability
        if (ValidateFields())
        {
            //Entry Confirmation dialog, asks the user for a saving confirmation
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Saving confirmation");
            confirmAlert.setHeaderText("Are you sure you want to save the given details?");
            customiseDialog(confirmAlert.getDialogPane());

            //Edit the "Ok" button of the dialog to display "Yes" instead
            Button yesButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
            yesButton.setText("Yes");

            confirmAlert.initModality(Modality.APPLICATION_MODAL);
            Optional<ButtonType> result = confirmAlert.showAndWait();

            //Proceed if the user confirms the saving
            if (result.get() == ButtonType.OK) {

                try {
                    //Open database connection
                    DBConnection db = new DBConnection();


                    //Retrieve the bookings collection to add the new bookings
                    MongoCollection<Document> bookingCol = db.collectionRetrieval("Bookings");

                    //Retrieve the matches collection to add a new match for each booking
                    MongoCollection<Document> matchesCol = db.collectionRetrieval("CompetitionMatches");

                    //Retrieve the tournament collection to add the new tournament
                    MongoCollection<Document> tourCol = db.collectionRetrieval("Tournaments");

                    //Add the tournament

                    //Create the new tournament document with the given details to be added to the collection
                    Document tourDoc = new Document("_id", new ObjectId());
                    tourDoc.append("Name", txtName.getText())
                            .append("Activity", cbActivity.getValue())
                            .append("Type", cbType.getValue())
                            .append("DateStart", dpDateStart.getValue())
                            .append("DateEnd", dpDateEnd.getValue())
                            .append("Time", cbTime.getValue())
                            .append("Age", txtMinAge.getText() + "-" + txtMaxAge.getText() + " y/o")
                            .append("NoTeams", cbNoTeams.getValue())
                            .append("Prize", txtPrize.getText())
                            .append("ExtraNotes", txtExtraNotes.getText());


                    //Adding the new bookings prepared in the validation method
                    //Add a new match for each booking
                    for(Document bookingDoc: bookingsDocsToSave)
                    {
                        //add the booking to the collection
                        bookingCol.insertOne(bookingDoc);

                        //Create a new match to be stored for the selected booking
                        Document matchDoc = new Document("_id", new ObjectId());
                        matchDoc.append("TournamentID", tourDoc.get("_id"))
                                .append("BookingID", bookingDoc.get("_id"))
                                .append("Team1", "Team 1")
                                .append("Team2", "Team 2")
                                .append("Winner", "")
                                .append("Score", new Document("Team1", 0).append("Team2", 0))
                                .append("ExtraNotes", "");

                        //Add the new match to the database
                        matchesCol.insertOne(matchDoc);

                    }

                    //If not errors were triggered when adding the bookings and matches, then add the tournament to the database
                    tourCol.insertOne(tourDoc);

                    //Close the database connection
                    db.close();

                    //Let the user know the details have been saved.
                    Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                    savedAlert.setTitle("Saved");
                    savedAlert.setHeaderText("Details have successfully been saved.");
                    customiseDialog(savedAlert.getDialogPane());
                    savedAlert.setContentText(null);
                    savedAlert.showAndWait();

                    //Close window to go back
                    closeStage();

                }
                //If for some reason, the details cannot be saved, let the user know
                catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("There was an unknown error when attempting to save the given details.");
                    errorAlert.setContentText(null);
                    customiseDialog(errorAlert.getDialogPane());
                    errorAlert.showAndWait();
                }
            }
        }
    }

    //Ask for confirmation when closing the stage
    public void btnCloseOnAction()
    {
        //Entry Confirmation dialog, asks the user for a canceling the addition confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel New Tournament");
        confirmAlert.setHeaderText("Are you sure you want to cancel?");
        customiseDialog(confirmAlert.getDialogPane());

        //Edit the "Ok" button of the dialog to display "Yes" instead
        Button yesButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
        yesButton.setText("Yes");

        confirmAlert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = confirmAlert.showAndWait();

        //Proceed if the user confirms the saving
        if (result.get() == ButtonType.OK) {
            closeStage();
        }
    }

    //Validation checks on the entered data
    public boolean ValidateFields()
    {
        //Ensure the name field is completed
        if(txtName.getText().isEmpty())
        {
            lblInfo.setText("Please enter a Tournament Name.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Ensure the name is not too long
        else if (txtName.getText().length() > 50)
        {
            lblInfo.setText("Tournament name is too long.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;

        }

        //Ensure a starting date is entered
        else if(dpDateEnd.getValue() == null)
        {
            lblInfo.setText("Please select a tournament starting date.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //ensure a end date is entered
        else if(dpDateEnd.getValue() == null)
        {
            lblInfo.setText("Please select a tournament ending date.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Ensure the min age is not too long
        else if (txtMinAge.getText().length() > 3)
        {
            lblInfo.setText("Minimum age is too long.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;

        }

        //Ensure the max age is not too long
        else if (txtMaxAge.getText().length() > 3)
        {
            lblInfo.setText("Maximum age is too long.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;

        }

        //Ensure the Min age field only has numbers
        else if(!txtMinAge.getText().matches("[0-9]+"))
        {
            lblInfo.setText("Minimum age of participants can only contain numbers.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Ensure the Max age field only has numbers
        else if(!txtMaxAge.getText().matches("[0-9]+"))
        {
            lblInfo.setText("Maximum age of participants can only contain numbers.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Check if the dates selected have time blocks available to create bookings for the tournament
        else if (!CheckDateAvailability()){
            lblInfo.setText("There are not enough time blocks available on the selected dates \n " +
                    "to perform the tournament. Please select different dates.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        else {
            return true;
        }
    }

    //Check if there are enough time slots available to perform the tournament, if so make the bookings
    public boolean CheckDateAvailability() {

        //Get the facilities that can perform the tournament
        ArrayList<Facility> facilitiesList = new ArrayList<>();
        DBConnection db = new DBConnection();
        facilitiesList = db.getFacilities(cbActivity.getValue());


        //Check the date and time block availability in each facility
        for (Facility fac : facilitiesList) {

            //Clear
            bookingsDocsToSave.clear();
            //get the total number of bookings to be made for the tournament
            numBookingsToMake = getBookingsToMake();

            //make sure the date is refreshed for each facility loop
            LocalDate dateStart = dpDateStart.getValue();
            LocalDate dateEnd = dpDateEnd.getValue();


            //Make sure the right facility is appointed to the tournament
            if (cbActivity.getValue().equals("Football"))
            {
                //If the tournament's type does not fit the facility's type, go next facility
                if(!fac.getFacType().equals(cbType.getValue()))
                {
                    //Go next facility
                    continue;
                }
            }

            //Get all bookings that exist in between the tournament start and end dates in the selected facility
            ArrayList<Booking> bookingsList = new ArrayList<>();
            bookingsList = db.getBookings(fac.getFacName(), dateStart, dateEnd);

            //Attempt to book a time block from the start date until the end date,
            while (!dateStart.isAfter(dateEnd)) {
                //Refresh block availability for each date
                Arrays.fill(blockAvailability, true);

                //Check all bookings returned
                for (Booking booking : bookingsList) {
                    //Check if there are any existing bookings in the date
                    if (dateStart.isEqual(booking.getBookDate())) {
                        //check for available time blocks
                        checkDate(booking);
                    }
                }

                //If the tournament is held in the morning
                if (cbTime.getValue().equals("Morning")) {
                    //Booking for time block 1
                    if(blockAvailability[0])
                    {
                        Document booking1 = new Document("_id", new ObjectId());
                        booking1.append("bookFacility", fac.getFacName())
                                .append("bookClientName", "Tournament: " + txtName.getText())
                                .append("bookContactNum", "")
                                .append("bookContactEmail", "tournament@hotmail.com")
                                .append("bookDate", dateStart)
                                .append("bookTime", "09:00")
                                .append("bookDuration", "120 min.")
                                .append("bookNumPeople", fac.getFacMaxPeople())
                                .append("bookPrice", fac.getFacPrice() * 3)
                                .append("CheckedIn", false);

                        //Add the booking document created to the arraylist to be saved later
                        bookingsDocsToSave.add(booking1);
                        //Decrease the number of bookings to be made
                        numBookingsToMake -= 1;
                        //If all bookings have been made, return true to validate that all bookings can be made
                        if (numBookingsToMake == 0) {
                            db.close();
                            return true;
                        }
                    }

                    //Booking for time block 2
                    if(blockAvailability[1]) {
                        Document booking2 = new Document("_id", new ObjectId());
                        booking2.append("bookFacility", fac.getFacName())
                                .append("bookClientName", "Tournament: " + txtName.getText())
                                .append("bookContactNum", "")
                                .append("bookContactEmail", "tournament@hotmail.com")
                                .append("bookDate", dateStart)
                                .append("bookTime", "11:00")
                                .append("bookDuration", "120 min.")
                                .append("bookNumPeople", fac.getFacMaxPeople())
                                .append("bookPrice", fac.getFacPrice() * 3)
                                .append("CheckedIn", false);

                        bookingsDocsToSave.add(booking2);
                        numBookingsToMake -= 1;
                        if (numBookingsToMake == 0) {
                            db.close();
                            return true;
                        }
                    }

                    //if there are still bookings left to be made go for next date
                    dateStart = LocalDate.of(dateStart.getYear(), dateStart.getMonth(), dateStart.getDayOfMonth()).plusDays(1);
                }

                //If the tournament is held in the evening
                else if (cbTime.getValue().equals("Evening")) {
                    //Booking for time block 3
                    if(blockAvailability[2]) {
                        Document booking1 = new Document("_id", new ObjectId());
                        booking1.append("bookFacility", fac.getFacName())
                                .append("bookClientName", "Tournament: " + txtName.getText())
                                .append("bookContactNum", "")
                                .append("bookContactEmail", "tournament@hotmail.com")
                                .append("bookDate", dateStart)
                                .append("bookTime", "15:00")
                                .append("bookDuration", "120 min.")
                                .append("bookNumPeople", fac.getFacMaxPeople())
                                .append("bookPrice", fac.getFacPrice() * 3)
                                .append("CheckedIn", false);

                        //Add the booking document created to the arraylist to be saved later
                        bookingsDocsToSave.add(booking1);
                        //Decrease the number of bookings to be made
                        numBookingsToMake -= 1;
                        //If all bookings have been made, return true to validate that all bookings can be made
                        if (numBookingsToMake == 0) {
                            db.close();
                            return true;
                        }
                    }

                    //Booking for time block 4
                    if(blockAvailability[3]) {
                        Document booking2 = new Document("_id", new ObjectId());
                        booking2.append("bookFacility", fac.getFacName())
                                .append("bookClientName", "Tournament: " + txtName.getText())
                                .append("bookContactNum", "")
                                .append("bookContactEmail", "tournament@hotmail.com")
                                .append("bookDate", dateStart)
                                .append("bookTime", "17:00")
                                .append("bookDuration", "120 min.")
                                .append("bookNumPeople", fac.getFacMaxPeople())
                                .append("bookPrice", fac.getFacPrice() * 3)
                                .append("CheckedIn", false);

                        //Add the booking document created to the arraylist to be saved later
                        bookingsDocsToSave.add(booking2);
                        //Decrease the number of bookings to be made
                        numBookingsToMake -= 1;
                        //If all bookings have been made, return true to validate that all bookings can be made
                        if (numBookingsToMake == 0) {
                            db.close();
                            return true;
                        }
                    }

                    //Booking for time block 5
                    if(blockAvailability[4]) {
                        Document booking3 = new Document("_id", new ObjectId());
                        booking3.append("bookFacility", fac.getFacName())
                                .append("bookClientName", "Tournament: " + txtName.getText())
                                .append("bookContactNum", "")
                                .append("bookContactEmail", "tournament@hotmail.com")
                                .append("bookDate", dateStart)
                                .append("bookTime", "19:00")
                                .append("bookDuration", "120 min.")
                                .append("bookNumPeople", fac.getFacMaxPeople())
                                .append("bookPrice", fac.getFacPrice() * 3)
                                .append("CheckedIn", false);

                        //Add the booking document created to the arraylist to be saved later
                        bookingsDocsToSave.add(booking3);
                        //Decrease the number of bookings to be made
                        numBookingsToMake -= 1;
                        //If all bookings have been made, return true to validate that all bookings can be made
                        if (numBookingsToMake == 0) {
                            db.close();
                            return true;
                        }
                    }

                    //if there are still bookings left to be made go for next date
                    dateStart = LocalDate.of(dateStart.getYear(), dateStart.getMonth(), dateStart.getDayOfMonth()).plusDays(1);
                }
            }
        }

        db.close();
        //if all facilities and dates have been checked and it wasn't possible to make all bookings, return false
        return false;
    }

    //Check the time block availability for a certain booking
    public void checkDate(Booking booking)
    {
        //If the tournament is organised in the morning, check the morning blocks in the date
        if (cbTime.getValue().equals("Morning")) {
            //block 1
            if (booking.getBookTime().equals("09:00") || booking.getBookTime().equals("09:30") || booking.getBookTime().equals("10:00") || booking.getBookTime().equals("10:30"))
            {
                blockAvailability[0] = false;
            }
            //block 2
            else if (booking.getBookTime().equals("11:00") || booking.getBookTime().equals("11:30") || booking.getBookTime().equals("12:00") || booking.getBookTime().equals("12:30"))
            {
                blockAvailability[1] = false;
            }
        }

        //If the tournament is organised in the evening, check the evening blocks in each booking in the current date
        else if (cbTime.getValue().equals("Evening")) {

            //block 3
            if (booking.getBookTime().equals("15:00") || booking.getBookTime().equals("15:30") || booking.getBookTime().equals("16:00") || booking.getBookTime().equals("16:30"))
            {
                blockAvailability[2] = false;
            }
            //block 4
            else if (booking.getBookTime().equals("17:00") || booking.getBookTime().equals("17:30") || booking.getBookTime().equals("18:00") || booking.getBookTime().equals("18:30"))
            {
                blockAvailability[3] = false;
            }
            //block 5
            else if (booking.getBookTime().equals("19:00") || booking.getBookTime().equals("19:30") || booking.getBookTime().equals("20:00") || booking.getBookTime().equals("20:30"))
            {
                blockAvailability[4] = false;
            }
        }
    }

    //Total bookings to be made
    public int getBookingsToMake()
    {
        //If the tournament has 4 teams, the total number of matches will be 3
        if(cbNoTeams.getValue() == 4)
        {
            return 3;
        }

        //If the tournament has 8 teams, the total number of matches will be 7
        else if (cbNoTeams.getValue() == 8)
        {
            return 7;
        }

        //If the tournament has 16 teams, the total number of matches will be 15
        else if (cbNoTeams.getValue() == 16)
        {
            return 15;
        }
        else
        {
            return 0;
        }
    }

    //Set some initial details of the window
    public void setInitialDetails()
    {
        //Populate the activity combo box
        cbActivity.getItems().addAll("Football", "Tennis", "Basketball", "Badminton");
        cbActivity.getSelectionModel().selectFirst();

        //Type combo
        cbType.getItems().addAll("5-a-side", "7-a-side", "11-a-side");

        //Populate the time cb
        cbTime.getItems().addAll("Morning", "Evening");
        cbTime.getSelectionModel().selectFirst();
        lblTimeInfo.setText("09:00 - 13:00");

        //Populate the number of teams cb
        cbNoTeams.getItems().addAll(4, 8, 16);
        cbNoTeams.getSelectionModel().selectFirst();


    }


    //When the user changes the activity, give the relevant type options
    public void cbActivityOnAction()
    {
        cbType.getItems().clear();

        if(cbActivity.getValue().equals("Football"))
        {
            cbType.getItems().addAll("5-a-side", "7-a-side", "11-a-side");
        }

        else if (cbActivity.getValue().equals("Tennis"))
        {
            cbType.getItems().addAll("Singles (3 sets)", "Doubles (3 sets)", "Singles (5 sets)", "Doubles (5 sets)");
        }

        else if(cbActivity.getValue().equals("Badminton"))
        {
            cbType.getItems().addAll("Singles (3 sets)", "Doubles (3 sets)");
        }

        else if(cbActivity.getValue().equals("Basketball"))
        {
            cbType.getItems().addAll("Basketball");
        }

    }

    //When the user selects a time, let them know what time the selected options means
    public void cbTimeOnAction()
    {
        if(cbTime.getValue().equals("Morning"))
        {
            lblTimeInfo.setText("09:00 - 13:00");
        }
        else
        {
            lblTimeInfo.setText(("15:00 - 19:00"));
        }
    }
    //Close the open stage
    public void closeStage()
    {
        Stage newTournamentStage = (Stage) btnClose.getScene().getWindow();
        newTournamentStage.close();

    }


    //Small customisation of dialogs
    public void customiseDialog(DialogPane dialog)
    {
        //Add a background color and change the font size
        dialog.setStyle("-fx-background-color:  #006400; -fx-font-size: 14px; -fx-font-weight: bold");
        //Add the application icon to the dialog
        //Get the Stage and add an icon
        Stage stage = (Stage) dialog.getScene().getWindow();
        try {
            stage.getIcons().add(new Image(Paths.get("src/SFMS/Views/Images/footballIcon.jpg").toUri().toURL().toExternalForm()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
