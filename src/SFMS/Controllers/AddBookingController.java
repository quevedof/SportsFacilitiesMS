package SFMS.Controllers;


import SFMS.Models.DBConnection;
import com.mongodb.client.MongoCollection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;


public class AddBookingController
{
    @FXML
    private Button btnClose;

    @FXML
    private TextField txtFacName;
    @FXML
    private TextField txtClientName;
    @FXML
    private TextField txtContactNum;
    @FXML
    private TextField txtContactEmail;
    @FXML
    private TextField txtDate;
    @FXML
    private ComboBox<String> cbTime;
    @FXML
    private ComboBox<String> cbDuration;
    @FXML
    private ComboBox<Integer> cbNumPeople;
    @FXML
    private TextField txtPrice;
    @FXML
    private Label lblInfo;

    @FXML
    private Button btnSave;

    //Stores the time block of the selected calendar cell
    private int block;
    //Stores the date
    private LocalDate dateSave;
    //Stores the price of the selected facility
    private double price;
    //Stores the final price which depends on the duration
    private double finalPrice;
    //Stores the minimum and maximum number of people in the selected facility
    private int minPeople;
    private int maxPeople;

    private boolean[]  timesAvailableArray;
    private VBox viewBox;
    private VBox nextTimeBlock;

    public void initialize()
    {
        //Remove the initial system focus of the first text field
        txtFacName.setFocusTraversable(false);

    }

    //Sets all initial data that's sent from the previous window
    public void setInitialData(String facName, String dateDisplay, LocalDate dateSave, int block, boolean[] timesAvailableArray, VBox viewBox, VBox nextTimeBlock, double price, int minPeople, int maxPeople)
    {
        //Display the name of the chosen facility
        txtFacName.setText(facName);
        //Display the date chosen as a string to be user friendly
        txtDate.setText(dateDisplay);
        //Assign the required data format to save to the database
        this.dateSave = dateSave;
        //Assign the times of the blocks that should be added to the combo box
        this.timesAvailableArray = timesAvailableArray;
        //Assign the time block
        this.viewBox = viewBox;
        //Assign the next time block
        this.nextTimeBlock = nextTimeBlock;
        //Assign the time block, price and maximum people
        this.block = block;
        this.price = price;
        this.minPeople = minPeople;
        this.maxPeople = maxPeople;

        //Populate the Time, Duration and Number of People combo boxes
        populateBlock();
        populateDuration();
        populateNumPeople();

    }

    //Ask for confirmation when closing the stage
    public void btnCloseOnAction()
    {
        //Entry Confirmation dialog, asks the user for a canceling the addition confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel New Booking");
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

    //Close the open stage
    public void closeStage()
    {
        Stage bookingStage = (Stage) btnClose.getScene().getWindow();
        bookingStage.close();
    }

    //Saving the booking in the database
    public void btnSaveOnAction()
    {
        if (fieldsValidation())
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
                    //Retrieve the bookings collection to add the new booking
                    MongoCollection<Document> col = db.collectionRetrieval("Bookings");

                    //Create the new booking document with the given details to be added to the collection
                    Document bookingDoc = new Document("_id", new ObjectId());
                    bookingDoc.append("bookFacility", txtFacName.getText())
                            .append("bookClientName", txtClientName.getText())
                            .append("bookContactNum", txtContactNum.getText())
                            .append("bookContactEmail", txtContactEmail.getText())
                            .append("bookDate", dateSave)
                            .append("bookTime", cbTime.getValue())
                            .append("bookDuration", cbDuration.getValue())
                            .append("bookNumPeople", cbNumPeople.getValue())
                            .append("bookPrice", finalPrice)
                            .append("CheckedIn", false);

                    //save the document in the database
                    col.insertOne(bookingDoc);
                    //Close the database connection
                    db.close();
                    //Let the user know the details have been saved.
                    Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                    savedAlert.setTitle("Saved");
                    savedAlert.setHeaderText("Details have successfully been saved.");
                    customiseDialog(savedAlert.getDialogPane());
                    savedAlert.setContentText(null);
                    savedAlert.showAndWait();

                    //Close window to go back to the calendar
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

    //Validate fields input
    public boolean fieldsValidation() {

        //Check there is an input for client name
        if (txtClientName.getText().isEmpty()) {
            lblInfo.setText("Please enter a Client Name for the booking.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Check at least one form of contact is filled
        else if (txtContactNum.getText().isEmpty() && txtContactEmail.getText().isEmpty()) {
            lblInfo.setText("At least one form of contact is needed.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Check the user has selected a duration
        else if (cbDuration.getValue()== null) {
            lblInfo.setText("Please select a Duration for the booking.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Check the user has selected the number of people
        else if (cbNumPeople.getValue() == null) {
            lblInfo.setText("Please select a Number of People for the booking.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //validate the contact number field
        else if (!txtContactNum.getText().isEmpty()) {
            //Check for length of the input
            if (txtContactNum.getText().length() > 14) {
                lblInfo.setText("Contact Number is too long. Please check again.");
                lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                return false;
            }

            //Check for invalid characters
            else if (txtContactNum.getText().matches(".*[a-zA-Z].*")) {
                lblInfo.setText("Contact Number cannot contain letters.");
                lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                return false;
            }
        }

        //validate the email field
        else if (!txtContactEmail.getText().isEmpty()) {
            if (!txtContactEmail.getText().contains("@")) {
                lblInfo.setText("Please enter a valid email address.");
                lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; ");
                return false;
            }
        }

        //If everything is validated return true
        return true;
    }

    //When the selection of the time changes, change the duration availability accordingly
    public void cbTimeOnAction()
    {
        populateDuration();
    }

    //Whenever the selection of the duration is changed, update the price field
    public void cbDurationOnAction()
    {
        switch (cbDuration.getValue())
        {
            case "30 min.":
                finalPrice = price;
                txtPrice.setText( "£" + finalPrice);

                break;

            case "60 min.":
                finalPrice = price * 2;
                txtPrice.setText( "£" + finalPrice);
                break;

            case "90 min.":
                finalPrice = price * 3;
                txtPrice.setText( "£" + finalPrice);
                break;

            case "120 min.":
                finalPrice = price * 4;
                txtPrice.setText( "£" + finalPrice);
                break;
        }
    }

    //Populates the Time combo box depending on the time block chosen in the calendar
    public void populateBlock()
    {
        switch (block)
        {
            case 1:
                if (timesAvailableArray[0]) {
                    cbTime.getItems().add("09:00");
                }
                if (timesAvailableArray[1]) {
                    cbTime.getItems().add("09:30");
                }
                if (timesAvailableArray[2]) {
                    cbTime.getItems().add("10:00");
                }
                if (timesAvailableArray[3]) {
                    cbTime.getItems().add("10:30");
                }

                break;

            case 2:
                if (timesAvailableArray[0]) {
                    cbTime.getItems().add("11:00");
                }
                if (timesAvailableArray[1]) {
                    cbTime.getItems().add("11:30");
                }
                if (timesAvailableArray[2]) {
                    cbTime.getItems().add("12:00");
                }
                if (timesAvailableArray[3]) {
                    cbTime.getItems().add("12:30");
                }
                break;

            case 3:
                if (timesAvailableArray[0]) {
                    cbTime.getItems().add("13:00");
                }
                if (timesAvailableArray[1]) {
                    cbTime.getItems().add("13:30");
                }
                if (timesAvailableArray[2]) {
                    cbTime.getItems().add("14:00");
                }
                if (timesAvailableArray[3]) {
                    cbTime.getItems().add("14:30");
                }
                break;

            case 4:
                if (timesAvailableArray[0]) {
                    cbTime.getItems().add("15:00");
                }
                if (timesAvailableArray[1]) {
                    cbTime.getItems().add("15:30");
                }
                if (timesAvailableArray[2]) {
                    cbTime.getItems().add("16:00");
                }
                if (timesAvailableArray[3]) {
                    cbTime.getItems().add("16:30");
                }
                break;

            case 5:
                if (timesAvailableArray[0]) {
                    cbTime.getItems().add("17:00");
                }
                if (timesAvailableArray[1]) {
                    cbTime.getItems().add("17:30");
                }
                if (timesAvailableArray[2]) {
                    cbTime.getItems().add("18:00");
                }
                if (timesAvailableArray[3]) {
                    cbTime.getItems().add("18:30");
                }
                break;

            case 6:
                if (timesAvailableArray[0]) {
                    cbTime.getItems().add("19:00");
                }
                if (timesAvailableArray[1]) {
                    cbTime.getItems().add("19:30");
                }
                if (timesAvailableArray[2]) {
                    cbTime.getItems().add("20:00");
                }
                if (timesAvailableArray[3]) {
                    cbTime.getItems().add("20:30");
                }
                break;
        }
        cbTime.getSelectionModel().selectFirst();
    }

    //Populates the Duration combo box with available durations
    public void populateDuration()
    {
        cbDuration.getItems().clear();

        //If the block is empty, add all durations keeping in mind the next block
        if (viewBox.getChildren().size() == 0)
        {
            //If the user selects the first time in the block, all durations are available to be selected
            if(cbTime.getValue().equals("09:00")
                    || cbTime.getValue().equals("11:00")
                    || cbTime.getValue().equals("13:00")
                    || cbTime.getValue().equals("15:00")
                    || cbTime.getValue().equals("17:00")
                    || cbTime.getValue().equals("19:00"))
            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");
                cbDuration.getItems().add("90 min.");
                cbDuration.getItems().add("120 min.");
            }

            //If the user selects the second time, check for the next block before adding the 120 min duration
            else if(cbTime.getValue().equals("09:30")
                    || cbTime.getValue().equals("11:30")
                    || cbTime.getValue().equals("13:30")
                    || cbTime.getValue().equals("15:30")
                    || cbTime.getValue().equals("17:30")
                    || cbTime.getValue().equals("19:30"))
            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");
                cbDuration.getItems().add("90 min.");
                //If it's the last time block, no need to check because more duration time cannot be added
                if (!cbTime.getValue().equals("19:30"))
                {
                    //Check next block, if empty add the duration
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("120 min.");
                    }

                    //If it's not empty, check if the first label is empty
                    else if (nextTimeBlock.getChildren().size() > 0) {

                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        if (lbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }

            //If the user selects the third time, check for the next block before adding the 90, 120 min duration
            else if(cbTime.getValue().equals("10:00")
                    || cbTime.getValue().equals("12:00")
                    || cbTime.getValue().equals("14:00")
                    || cbTime.getValue().equals("16:00")
                    || cbTime.getValue().equals("18:00")
                    || cbTime.getValue().equals("20:00"))
            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");

                if (!cbTime.getValue().equals("20:00")) {
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("90 min.");
                        cbDuration.getItems().add("120 min.");
                    }

                    //Check next block
                    else if (nextTimeBlock.getChildren().size() == 1) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        if (lbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    } else if (nextTimeBlock.getChildren().size() >= 2) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label lbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }

            //If the user selects the fourth time, check for the next block before adding the 60, 90, 120 min duration
            else if(cbTime.getValue().equals("10:30")
                    || cbTime.getValue().equals("12:30")
                    || cbTime.getValue().equals("14:30")
                    || cbTime.getValue().equals("16:30")
                    || cbTime.getValue().equals("18:30")
                    || cbTime.getValue().equals("20:30"))
            {
                cbDuration.getItems().add("30 min.");

                if (!cbTime.getValue().equals("20:30")) {
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("60 min.");
                        cbDuration.getItems().add("90 min.");
                        cbDuration.getItems().add("120 min.");
                    }

                    //Check next block
                    else if (nextTimeBlock.getChildren().size() == 1) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);

                        if (lbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    } else if (nextTimeBlock.getChildren().size() == 2) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label lbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    } else if (nextTimeBlock.getChildren().size() >= 3) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label lbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        Label lbl3 = (Label) nextTimeBlock.getChildren().get(2);
                        if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty() && !lbl3.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty() && lbl3.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }
        }

        //If the block has one label
        else if (viewBox.getChildren().size() == 1)
        {
            //User cannot select the first time because if only one label is present, it is part of the previous block's booking
            //If the user selects the second time, check for the next block before adding the 120 min duration
            if(cbTime.getValue().equals("09:30")
                    || cbTime.getValue().equals("11:30")
                    || cbTime.getValue().equals("13:30")
                    || cbTime.getValue().equals("15:30")
                    || cbTime.getValue().equals("17:30")
                    || cbTime.getValue().equals("19:30"))
            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");
                cbDuration.getItems().add("90 min.");

                if (!cbTime.getValue().equals("19:30")) {
                    //Check next block, if empty add the duration
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("120 min.");
                    }

                    //If it's not empty, check if the first label is empty
                    else if (nextTimeBlock.getChildren().size() > 0) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        if (lbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }

            //If the user selects the third time, check for the next block before adding the 90, 120 min duration
            else if(cbTime.getValue().equals("10:00")
                    || cbTime.getValue().equals("12:00")
                    || cbTime.getValue().equals("14:00")
                    || cbTime.getValue().equals("16:00")
                    || cbTime.getValue().equals("18:00")
                    || cbTime.getValue().equals("20:00"))
            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");

                if (!cbTime.getValue().equals("20:00")) {
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("90 min.");
                        cbDuration.getItems().add("120 min.");
                    }

                    //Check next block
                    else if (nextTimeBlock.getChildren().size() == 1) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);

                        if (lbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }

                    } else if (nextTimeBlock.getChildren().size() >= 2) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label lbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }

            //If the user selects the fourth time, check for the next block before adding the 60, 90, 120 min duration
            else if(cbTime.getValue().equals("10:30")
                    || cbTime.getValue().equals("12:30")
                    || cbTime.getValue().equals("14:30")
                    || cbTime.getValue().equals("16:30")
                    || cbTime.getValue().equals("18:30")
                    || cbTime.getValue().equals("20:30"))
            {
                cbDuration.getItems().add("30 min.");

                if (!cbTime.getValue().equals("20:30")) {
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("60 min.");
                        cbDuration.getItems().add("90 min.");
                        cbDuration.getItems().add("120 min.");
                    }

                    //Check next block
                    else if (nextTimeBlock.getChildren().size() == 1) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);

                        if (lbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    } else if (nextTimeBlock.getChildren().size() == 2) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label lbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    } else if (nextTimeBlock.getChildren().size() >= 3) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label lbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        Label lbl3 = (Label) nextTimeBlock.getChildren().get(2);
                        if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty() && !lbl3.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty() && lbl3.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }
        }

        //If the box has 2 labels
        else if (viewBox.getChildren().size() == 2)
        {
            //User cannot select the first and second time because if only 2 labels are present, they are part of the previous block's booking

            //If the user selects the third time, check for the next block before adding the 90, 120 min duration
            if(cbTime.getValue().equals("10:00")
                    || cbTime.getValue().equals("12:00")
                    || cbTime.getValue().equals("14:00")
                    || cbTime.getValue().equals("16:00")
                    || cbTime.getValue().equals("18:00")
                    || cbTime.getValue().equals("20:00"))
            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");

                if (!cbTime.getValue().equals("20:00")) {
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("90 min.");
                        cbDuration.getItems().add("120 min.");
                    }

                    //Check next block
                    else if (nextTimeBlock.getChildren().size() == 1) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);

                        if (lbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }

                    } else if (nextTimeBlock.getChildren().size() >= 2) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label lbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }

            //If the user selects the fourth time, check for the next block before adding the 60, 90, 120 min duration
            else if(cbTime.getValue().equals("10:30")
                    || cbTime.getValue().equals("12:30")
                    || cbTime.getValue().equals("14:30")
                    || cbTime.getValue().equals("16:30")
                    || cbTime.getValue().equals("18:30")
                    || cbTime.getValue().equals("20:30"))
            {
                cbDuration.getItems().add("30 min.");

                if (!cbTime.getValue().equals("20:30")) {
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("60 min.");
                        cbDuration.getItems().add("90 min.");
                        cbDuration.getItems().add("120 min.");
                    }

                    //Check next block
                    else if (nextTimeBlock.getChildren().size() == 1) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);

                        if (lbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    } else if (nextTimeBlock.getChildren().size() == 2) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label lbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    } else if (nextTimeBlock.getChildren().size() >= 3) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label lbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        Label lbl3 = (Label) nextTimeBlock.getChildren().get(2);
                        if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty() && !lbl3.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty() && lbl3.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }
        }

        //If the box has exactly 3 labels
        else if (viewBox.getChildren().size() == 3)
        {
            //User cannot select the first, second and third time because if only 3 labels are present, they are part of the previous block's booking

            //If the user selects the fourth time, check for the next block before adding the 60, 90, 120 min duration
            if(cbTime.getValue().equals("10:30")
                    || cbTime.getValue().equals("12:30")
                    || cbTime.getValue().equals("14:30")
                    || cbTime.getValue().equals("16:30")
                    || cbTime.getValue().equals("18:30")
                    || cbTime.getValue().equals("20:30"))
            {
                cbDuration.getItems().add("30 min.");

                if (!cbTime.getValue().equals("20:30")) {
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("60 min.");
                        cbDuration.getItems().add("90 min.");
                        cbDuration.getItems().add("120 min.");
                    }

                    //Check next block
                    else if (nextTimeBlock.getChildren().size() == 1) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);

                        if (lbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    } else if (nextTimeBlock.getChildren().size() == 2) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label lbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    } else if (nextTimeBlock.getChildren().size() >= 3) {
                        Label lbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label lbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        Label lbl3 = (Label) nextTimeBlock.getChildren().get(2);
                        if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty() && !lbl3.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                        } else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty() && lbl3.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }
        }

        //If the box has all 4 labels
        else if (viewBox.getChildren().size() == 4)
        {

            //Get labels from the current time block
            Label lbl1 = (Label) viewBox.getChildren().get(0);
            Label lbl2 = (Label) viewBox.getChildren().get(1);
            Label lbl3 = (Label) viewBox.getChildren().get(2);
            Label lbl4 = (Label) viewBox.getChildren().get(3);

            //Time 1
            //If first label is empty but the second one isn't, add 30 mins if the user selects the first time of the block
            if (lbl1.getText().isEmpty() && !lbl2.getText().isEmpty() &&
                    (cbTime.getValue().equals("09:00")
                            || cbTime.getValue().equals("11:00")
                            || cbTime.getValue().equals("13:00")
                            || cbTime.getValue().equals("15:00")
                            || cbTime.getValue().equals("17:00")
                            || cbTime.getValue().equals("19:00")) )
            {
                cbDuration.getItems().add("30 min.");
            }
            //If first and second labels are empty and the third one isn't, add 30, 60 mins if the user selects the first time of the block
            else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty() && !lbl3.getText().isEmpty() &&
                    (cbTime.getValue().equals("09:00")
                            || cbTime.getValue().equals("11:00")
                            || cbTime.getValue().equals("13:00")
                            || cbTime.getValue().equals("15:00")
                            || cbTime.getValue().equals("17:00")
                            || cbTime.getValue().equals("19:00")))

            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");
            }
            //If the first, second, and third labels are empty and the fourth one isn't, add 30, 60, 90 mins if the user selects the first time of the block
            else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty() && lbl3.getText().isEmpty() && !lbl4.getText().isEmpty() &&
                    (cbTime.getValue().equals("09:00")
                            || cbTime.getValue().equals("11:00")
                            || cbTime.getValue().equals("13:00")
                            || cbTime.getValue().equals("15:00")
                            || cbTime.getValue().equals("17:00")
                            || cbTime.getValue().equals("19:00")))
            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");
                cbDuration.getItems().add("90 min.");
            }

            //If all labels are empty, add all durations if the user selects the first time of the block
            else if (lbl1.getText().isEmpty() && lbl2.getText().isEmpty() && lbl3.getText().isEmpty() && lbl4.getText().isEmpty() &&
                    (cbTime.getValue().equals("09:00")
                            || cbTime.getValue().equals("11:00")
                            || cbTime.getValue().equals("13:00")
                            || cbTime.getValue().equals("15:00")
                            || cbTime.getValue().equals("17:00")
                            || cbTime.getValue().equals("19:00")))
            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");
                cbDuration.getItems().add("90 min.");
                cbDuration.getItems().add("120 min.");
            }

            //Time 2
            else if (lbl2.getText().isEmpty() && !lbl3.getText().isEmpty() &&
                    (cbTime.getValue().equals("09:30")
                            || cbTime.getValue().equals("11:30")
                            || cbTime.getValue().equals("13:30")
                            || cbTime.getValue().equals("15:30")
                            || cbTime.getValue().equals("17:30")
                            || cbTime.getValue().equals("19:30")))
            {
                cbDuration.getItems().add("30 min.");
            }

            else if (lbl2.getText().isEmpty() && lbl3.getText().isEmpty() && !lbl4.getText().isEmpty() &&
                    (cbTime.getValue().equals("09:30")
                            || cbTime.getValue().equals("11:30")
                            || cbTime.getValue().equals("13:30")
                            || cbTime.getValue().equals("15:30")
                            || cbTime.getValue().equals("17:30")
                            || cbTime.getValue().equals("19:30")))

            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");
            }

            else if (lbl2.getText().isEmpty() && lbl3.getText().isEmpty() && lbl4.getText().isEmpty() &&
                    (cbTime.getValue().equals("09:30")
                            || cbTime.getValue().equals("11:30")
                            || cbTime.getValue().equals("13:30")
                            || cbTime.getValue().equals("15:30")
                            || cbTime.getValue().equals("17:30")
                            || cbTime.getValue().equals("19:30")))
            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");
                cbDuration.getItems().add("90 min.");
                if (!cbTime.getValue().equals("19:30"))
                {
                    //Check next block, if empty add the duration
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("120 min.");
                    }

                    //If it's not empty, check if the first label is empty
                    else if (nextTimeBlock.getChildren().size() > 0) {
                        Label nextlbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        if (nextlbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }

            //Time 3
            else if (lbl3.getText().isEmpty() && !lbl4.getText().isEmpty() &&
                    (cbTime.getValue().equals("10:00")
                            || cbTime.getValue().equals("12:00")
                            || cbTime.getValue().equals("14:00")
                            || cbTime.getValue().equals("16:00")
                            || cbTime.getValue().equals("18:00")
                            || cbTime.getValue().equals("20:00")))
            {
                cbDuration.getItems().add("30 min.");
            }

            else if (lbl3.getText().isEmpty() && lbl4.getText().isEmpty() &&
                    (cbTime.getValue().equals("10:00")
                            || cbTime.getValue().equals("12:00")
                            || cbTime.getValue().equals("14:00")
                            || cbTime.getValue().equals("16:00")
                            || cbTime.getValue().equals("18:00")
                            || cbTime.getValue().equals("20:00")))

            {
                cbDuration.getItems().add("30 min.");
                cbDuration.getItems().add("60 min.");

                if (!cbTime.getValue().equals("20:00")) {
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("90 min.");
                        cbDuration.getItems().add("120 min.");
                    }

                    //Check next block
                    else if (nextTimeBlock.getChildren().size() == 1) {
                        Label nextlbl1 = (Label) nextTimeBlock.getChildren().get(0);

                        if (nextlbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }

                    } else if (nextTimeBlock.getChildren().size() >= 2) {
                        Label nextlbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label nextlbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        if (nextlbl1.getText().isEmpty() && !nextlbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                        } else if (nextlbl1.getText().isEmpty() && nextlbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }

            //Time 4
            else if (cbTime.getValue().equals("10:30")
                    || cbTime.getValue().equals("12:30")
                    || cbTime.getValue().equals("14:30")
                    || cbTime.getValue().equals("16:30")
                    || cbTime.getValue().equals("18:30")
                    || cbTime.getValue().equals("20:30"))
            {
                cbDuration.getItems().add("30 min.");

                if (!cbTime.getValue().equals("20:30")) {
                    //Check next block
                    if (nextTimeBlock.getChildren().size() == 0) {
                        cbDuration.getItems().add("60 min.");
                        cbDuration.getItems().add("90 min.");
                        cbDuration.getItems().add("120 min.");
                    }
                    else if (nextTimeBlock.getChildren().size() == 1) {
                        Label nextlbl1 = (Label) nextTimeBlock.getChildren().get(0);

                        if (nextlbl1.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                    else if (nextTimeBlock.getChildren().size() == 2) {
                        Label nextlbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label nextlbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        if (nextlbl1.getText().isEmpty() && !nextlbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                        } else if (nextlbl1.getText().isEmpty() && nextlbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                    else if (nextTimeBlock.getChildren().size() >= 3) {
                        Label nextlbl1 = (Label) nextTimeBlock.getChildren().get(0);
                        Label nextlbl2 = (Label) nextTimeBlock.getChildren().get(1);
                        Label nextlbl3 = (Label) nextTimeBlock.getChildren().get(2);
                        if (nextlbl1.getText().isEmpty() && !nextlbl2.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                        } else if (nextlbl1.getText().isEmpty() && nextlbl2.getText().isEmpty() && !nextlbl3.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");

                        } else if (nextlbl1.getText().isEmpty() && nextlbl2.getText().isEmpty() && nextlbl3.getText().isEmpty()) {
                            cbDuration.getItems().add("60 min.");
                            cbDuration.getItems().add("90 min.");
                            cbDuration.getItems().add("120 min.");
                        }
                    }
                }
            }
        }
    }

    //Populate the number of people combo box
    public  void populateNumPeople()
    {
        //Set the first item as the minimum and the last item as the maximum of people
        while (minPeople <= maxPeople)
        {
            cbNumPeople.getItems().add(minPeople);
            minPeople += 1;
        }
    }

    //Customise small windows
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
