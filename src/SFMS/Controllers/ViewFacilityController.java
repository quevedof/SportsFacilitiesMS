package SFMS.Controllers;

import SFMS.Models.DBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.nio.file.Paths;
import java.util.Optional;
import static com.mongodb.client.model.Filters.eq;

public class ViewFacilityController {
    @FXML
    private Label lblInfo;

    @FXML
    private TextField txtFacName;

    @FXML
    private ComboBox<String> cbActivity;

    @FXML
    private TextField txtType;

    @FXML
    private TextField txtPrice;

    @FXML
    private TextField txtMinPeople;

    @FXML
    private TextField txtMaxPeople;

    @FXML
    private TextArea txtExtraNotes;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnEdit;

    //Stores the facility being viewed
    Document displayedFac;

    //Populate the
    public void initialize()
    {
        populateActivityCombo();

    }

    //Retrieves the facility that was selected in the previous window, method called before the file load in the previous controller
    public void retrieveSelectedFacility(String facName)
    {
        //Open database connection
        DBConnection db = new DBConnection();
        //Retrieve the facilities collection
        MongoCollection<Document> facCol = db.collectionRetrieval("Facilities");
        //Retrieve the required facility
        displayedFac = facCol.find(eq("Name", facName)).first();
        //Close database connection
        db.close();
        //If the documents is null, let the user know the facility doest not exist
        if(displayedFac == null)
        {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Facility Does Not Exist");
            errorAlert.setHeaderText("The selected facility does not exist anymore, please refresh the page.");
            errorAlert.setContentText(null);
            customiseDialog(errorAlert.getDialogPane());
            errorAlert.showAndWait();
            closeStage();

        }else{
            //Display the retrieved document
            displaySelectedFacility();
        }


    }

    //Displays the details of the selected facility
    public void displaySelectedFacility()
    {
        //Set the retrieved details to the fields
        txtFacName.setText((String) displayedFac.get("Name"));
        cbActivity.setValue((String) displayedFac.get("Activity"));
        txtType.setText((String) displayedFac.get("Type"));
        txtPrice.setText(Double.toString( (Double) displayedFac.get("£/30mins")));
        txtMinPeople.setText(String.valueOf((int) displayedFac.get("MinPeople")));
        txtMaxPeople.setText(String.valueOf((int) displayedFac.get("MaxPeople")));
        txtExtraNotes.setText((String) displayedFac.get("ExtraNotes"));

    }

    //Saving the updated facility in the database
    public void btnSaveOnAction()
    {

        //Validate entries
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
                    //Retrieve the facilities collection to add the new facility
                    MongoCollection<Document> col = db.collectionRetrieval("Facilities");

                    //Create the updated facility document with the given details to be added to the collection
                    Document facilityDoc = new Document();
                    facilityDoc.append("Name", txtFacName.getText())
                            .append("Activity", cbActivity.getValue())
                            .append("Type", txtType.getText())
                            .append("£/30mins", Double.parseDouble(txtPrice.getText()))
                            .append("MinPeople", Integer.valueOf(txtMinPeople.getText()))
                            .append("MaxPeople", Integer.valueOf(txtMaxPeople.getText()))
                            .append("ExtraNotes", txtExtraNotes.getText());

                    //update document
                    Document update = new Document("$set", facilityDoc);
                    //Request the new version of the document after the update
                    FindOneAndUpdateOptions optionAfter = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
                    //update the document in the database
                    displayedFac = col.findOneAndUpdate(eq("_id",displayedFac.get("_id")), update, optionAfter);
                    //Close the database connection
                    db.close();
                    //Display the new version of the document
                    displaySelectedFacility();
                    disableFields();

                    //Let the user know the details have been saved.
                    Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                    savedAlert.setTitle("Saved");
                    savedAlert.setHeaderText("Details have successfully been updated.");
                    customiseDialog(savedAlert.getDialogPane());
                    savedAlert.setContentText(null);
                    savedAlert.showAndWait();


                }
                //If for some reason, the details cannot be saved, let the user know
                catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("There was an unknown error when attempting to save the given details.");
                    customiseDialog(errorAlert.getDialogPane());
                    errorAlert.showAndWait();

                }
            }
        }
    }

    //Allows the amendment of the selected facility's details
    public void btnEditOnAction() {
        //Enable all nodes to allow the amendment of the facility details
        if (btnEdit.getText().equals("Edit")) {
            txtFacName.setDisable(false);
            cbActivity.setDisable(false);
            txtType.setDisable(false);
            txtPrice.setDisable(false);
            txtMinPeople.setDisable(false);
            txtMaxPeople.setDisable(false);
            txtExtraNotes.setDisable(false);
            btnSave.setDisable(false);
            //Don't let the user close the window when editing
            btnClose.setDisable(true);
            //Give the user the option to cancel
            btnEdit.setText("Cancel");
        }

        //If the user decides to cancel, ignore the changes and disable all nodes
        else if (btnEdit.getText().equals("Cancel")) {

            //Entry Confirmation dialog, asks the user for a canceling confirmation
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Cancel Editing");
            confirmAlert.setHeaderText("Are you sure you want to discard any changes made?");
            customiseDialog(confirmAlert.getDialogPane());

            //Edit the "Ok" button of the dialog to display "Yes" instead
            Button yesButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
            yesButton.setText("Yes");

            confirmAlert.initModality(Modality.APPLICATION_MODAL);
            Optional<ButtonType> result = confirmAlert.showAndWait();

            //Proceed if the user confirms the cancellation
            if (result.get() == ButtonType.OK) {
                //Ignore any changes and display original details of the facility being viewed
                displaySelectedFacility();
                //Disable all nodes
               disableFields();
            }
        }
    }

    //Disable fields after the facility is updated
    public void disableFields()
    {
        txtFacName.setDisable(true);
        cbActivity.setDisable(true);
        txtType.setDisable(true);
        txtPrice.setDisable(true);
        txtMinPeople.setDisable(true);
        txtMaxPeople.setDisable(true);
        txtExtraNotes.setDisable(true);
        btnSave.setDisable(true);
        btnClose.setDisable(false);
        //Give the user back the option to edit
        btnEdit.setText("Edit");
    }

    //Removal of the selected facility from the database
    public void btnRemoveOnAction()
    {
        //Entry Confirmation dialog, asks the user for a removal confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Facility Removal Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to remove the displayed Facility?");
        customiseDialog(confirmAlert.getDialogPane());

        //Edit the "Ok" button of the dialog to display "Yes" instead
        Button yesButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
        yesButton.setText("Yes");

        confirmAlert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = confirmAlert.showAndWait();

        //Proceed if the user confirms the removal
        if (result.get() == ButtonType.OK) {

            try {
                //Open database connection
                DBConnection db = new DBConnection();
                //Retrieve the facilities collection to remove the facility
                MongoCollection<Document> col = db.collectionRetrieval("Facilities");

                //Remove the facility
                col.deleteOne(eq("_id", displayedFac.get("_id")));
                //Close the database connection
                db.close();
                //Let the user know the details have been saved.
                Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                savedAlert.setTitle("Facility Removed");
                savedAlert.setHeaderText("The Facility has successfully been removed.");
                customiseDialog(savedAlert.getDialogPane());
                savedAlert.setContentText(null);
                savedAlert.showAndWait();

                //Close window to go back to the facilities
                closeStage();

            }
            //If for some reason, the facility could not be removed, let the user know
            catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("There was an unknown error when attempting to remove the selected facility");
                errorAlert.setContentText(null);
                customiseDialog(errorAlert.getDialogPane());
                errorAlert.showAndWait();
            }
        }
    }

    //Validate fields input
    public boolean fieldsValidation() {

        //Check there is an input for facility name
        if (txtFacName.getText().isEmpty()) {
            lblInfo.setText("Please enter a Facility Name.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        else
        {

            //Open database connection to check for duplicates
            DBConnection db = new DBConnection();
            //Retrieve the facilities collection to check for a repeated facility name
            MongoCollection<Document> col = db.collectionRetrieval("Facilities");
            //Try to get an existing document
            Document facDoc = col.find(eq("Name", txtFacName.getText())).first();
            //Check for facility name uniqueness to ensure the user doesn't enter an already existing facility
            if ( facDoc != null)
            {
                //Make sure the existing document is not the one being edited
                ObjectId id = (ObjectId) facDoc.get("_id");
                if(!id.equals(displayedFac.get("_id"))) {
                    lblInfo.setText("Entered Facility already exists.");
                    lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    return false;
                }
            }
            db.close();

        }

        //Ensure type field is not empty
        if(txtType.getText().isEmpty())
        {
            lblInfo.setText("Please enter a Facility Type.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Ensure price field is not empty
        else if(txtPrice.getText().isEmpty())
        {
            lblInfo.setText("Please enter a price for 30 mins of facility usage.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Ensure min people is not empty
        else if( txtMinPeople.getText().isEmpty())
        {
            lblInfo.setText("Please enter a minimum number of people.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Ensure Max num people is not empty
        else if( txtMaxPeople.getText().isEmpty())
        {
            lblInfo.setText("Please enter a maximum number of people.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Ensure prices has only digits and decimal dot
        else if (!txtPrice.getText().matches("[0-9.]+"))
        {
            lblInfo.setText("Price field can only contain numbers and a \ndecimal dot, e.g. 10.00");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Ensure the min people has only digits
        else if(!txtMinPeople.getText().matches("[0-9]+"))
        {
            lblInfo.setText("Minimum number of people can only contain numbers.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Ensure the max people has only digits
        else if(!txtMaxPeople.getText().matches("[0-9]+"))
        {
            lblInfo.setText("Maximum number of people can only contain numbers.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        else
        {
            return true;
        }
    }

    //Populate the Activity Combo Box
    public void populateActivityCombo()
    {
        cbActivity.getItems().addAll("Football", "Tennis", "Basketball", "Badminton", "Other");
    }

    //Ask for confirmation when closing the stage
    public void btnCloseOnAction()
    {
        //Entry Confirmation dialog, asks the user for a closing confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Close Facility View");
        confirmAlert.setHeaderText("Are you sure you want to close the Facility View?");
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

    //Customise small window
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
