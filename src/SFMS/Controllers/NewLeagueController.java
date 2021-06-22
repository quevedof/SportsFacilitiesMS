package SFMS.Controllers;

import SFMS.Models.DBConnection;
import SFMS.Models.Facility;
import com.mongodb.DB;
import com.mongodb.client.MongoCollection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;

public class NewLeagueController {
    @FXML
    private Label lblInfo;

    @FXML
    private TextField txtName;

    @FXML
    private ComboBox<String> cbActivity;

    @FXML
    private DatePicker dpDateStart;

    @FXML
    private TextField txtMinAge;

    @FXML
    private TextField txtMaxAge;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnSave;


    @FXML
    private TextField txtTeam1;

    @FXML
    private TextField txtTeam6;

    @FXML
    private TextField txtTeam2;

    @FXML
    private TextField txtTeam3;

    @FXML
    private TextField txtTeam4;

    @FXML
    private TextField txtTeam5;

    @FXML
    private TextField txtTeam7;

    @FXML
    private TextField txtTeam8;

    @FXML
    private TextField txtTeam9;

    @FXML
    private TextField txtTeam10;

    public void initialize()
    {
        setInitialDetails();
    }


    @FXML
    public void btnCloseOnAction() {
        //Entry Confirmation dialog, asks the user for a canceling the addition confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel New League");
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

    //Proceed to save the league after some validation checks
    //Automatically creates bookings for the league matches
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

                    //Retrieve the tournament collection to add the new tournament
                    MongoCollection<Document> leaguesCol = db.collectionRetrieval("Leagues");

                    //Create teams array
                    ArrayList<String> teamsArrayList = new ArrayList<>();
                    teamsArrayList.add(txtTeam1.getText());
                    teamsArrayList.add(txtTeam2.getText());
                    teamsArrayList.add(txtTeam3.getText());
                    teamsArrayList.add(txtTeam4.getText());
                    teamsArrayList.add(txtTeam5.getText());
                    teamsArrayList.add(txtTeam6.getText());
                    teamsArrayList.add(txtTeam7.getText());
                    teamsArrayList.add(txtTeam8.getText());
                    teamsArrayList.add(txtTeam9.getText());
                    teamsArrayList.add(txtTeam10.getText());
                    //work out the league end date
                    LocalDate endDate = dpDateStart.getValue().plus(9, ChronoUnit.WEEKS);

                    //Add the league
                    //Create the new league document with the given details to be added to the collection
                    Document leagueDoc = new Document("_id", new ObjectId());
                    leagueDoc.append("Name", txtName.getText())
                            .append("Activity", cbActivity.getValue())
                            .append("DateStart", dpDateStart.getValue())
                            .append("DateEnd",endDate)
                            .append("Age", txtMinAge.getText() + "-" + txtMaxAge.getText() + " y/o")
                            .append("Teams", teamsArrayList);

                    leaguesCol.insertOne(leagueDoc);

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

    //Validation checks on the entered data
    public boolean ValidateFields()
    {
        //Ensure the name field is completed
        if(txtName.getText().isEmpty())
        {
            lblInfo.setText("Please enter a League Name.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Ensure the name is not too long
        else if (txtName.getText().length() > 50)
        {
            lblInfo.setText("League name is too long.");
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

        else {
            return true;
        }
    }

    //Set some initial details of the window
    public void setInitialDetails()
    {
        //Populate the activity combo box
        cbActivity.getItems().addAll("Football", "Tennis", "Basketball", "Badminton");
        cbActivity.getSelectionModel().selectFirst();

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
