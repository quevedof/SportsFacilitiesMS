package SFMS.Controllers;

import SFMS.Models.DBConnection;
import com.mongodb.client.MongoCollection;
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

public class NewFacilityController {


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

    public void initialize()
    {
        populateActivityCombo();
    }


    //Saving the facility in the database
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
                    //Retrieve the bookings collection to add the new booking
                    MongoCollection<Document> col = db.collectionRetrieval("Facilities");

                    //Create the new facility document with the given details to be added to the collection
                    Document facilityDoc = new Document("_id", new ObjectId());
                    facilityDoc.append("bookFacility", txtFacName.getText())
                            .append("Name", txtFacName.getText())
                            .append("Activity", cbActivity.getValue())
                            .append("Type", txtType.getText())
                            .append("£/30mins", Double.parseDouble(txtPrice.getText()))
                            .append("MinPeople", Integer.valueOf(txtMinPeople.getText()))
                            .append("MaxPeople", Integer.valueOf(txtMaxPeople.getText()))
                            .append("ExtraNotes", txtExtraNotes.getText());

                    //save the document in the database
                    col.insertOne(facilityDoc);
                    //Close the database connection
                    db.close();
                    //Let the user know the details have been saved.
                    Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                    savedAlert.setTitle("Saved");
                    savedAlert.setHeaderText("Details have successfully been saved.");
                    customiseDialog(savedAlert.getDialogPane());
                    savedAlert.setContentText(null);
                    savedAlert.showAndWait();

                    //Close window to go back to the facilities view
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
                    e.printStackTrace();
                }
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
            //Open database connection to check for duplicated
            DBConnection db = new DBConnection();
            //Retrieve the facilities collection to check for a repeated facility name
            MongoCollection<Document> col = db.collectionRetrieval("Facilities");
            //Check for facility name uniqueness to ensure the user doesn't enter an already existing facility
             if ( col.find(eq("Name", txtFacName.getText())).first() != null)
            {
                 lblInfo.setText("Entered Facility already exists.");
                 lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                 return false;
            }
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
        cbActivity.getSelectionModel().selectFirst();
    }

    //Ask for confirmation when closing the stage
    public void btnCloseOnAction()
    {
        //Entry Confirmation dialog, asks the user for a canceling the addition confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel New Facility");
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
        Stage newFacilityStage = (Stage) btnClose.getScene().getWindow();
        newFacilityStage.close();

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
