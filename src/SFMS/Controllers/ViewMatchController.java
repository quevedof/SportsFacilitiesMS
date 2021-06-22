package SFMS.Controllers;

import SFMS.Models.Booking;
import SFMS.Models.CompetitionMatch;
import SFMS.Models.DBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;


public class ViewMatchController {

    @FXML
    private Label lblFacility;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnAddResults;

    @FXML
    private Label lblInfo;

    @FXML
    private TextField txtTeam1;

    @FXML
    private TextField txtScore1;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblTime;

    @FXML
    private TextField txtTeam2;

    @FXML
    private TextField txtScore2;

    @FXML
    private TextArea txtExtraNotes;

    @FXML
    private VBox vbTeam1;

    @FXML
    private VBox vbTeam2;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnSave;


    //Match being shown on screen
    private CompetitionMatch displayedMatch;
    //Booking of the match
    private Booking matchBooking;


    public void setMatch(CompetitionMatch match)
    {
        this.displayedMatch = match;
        //Get the booking for the displayed match
        getMatchBooking(displayedMatch.getBookingID());
        //Display match details
        displayMatchDetails();

    }

    //Allows the user to save any changes made
    public void btnSaveOnAction()
    {
        //Validate entries
        if (fieldsValidation())
        {
            //Entry Confirmation dialog, asks the user for a saving confirmation
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Saving confirmation");
            confirmAlert.setHeaderText("Are you sure you want to save?");
            customiseDialog(confirmAlert.getDialogPane());

            //Edit the "Ok" button of the dialog to display "Yes" instead
            Button yesButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
            yesButton.setText("Yes");

            confirmAlert.initModality(Modality.APPLICATION_MODAL);
            Optional<ButtonType> result = confirmAlert.showAndWait();

            //Proceed if the user confirms the saving
            if (result.get() == ButtonType.OK) {

                try {

                    //Determine the winner based on the input scores
                    String winner =getWinner();

                    //Prepare the scores document;
                    //Document scores = new D

                    //Open database connection
                    DBConnection db = new DBConnection();
                    //Retrieve the matches collection to update
                    MongoCollection<Document> col = db.collectionRetrieval("CompetitionMatches");

                    //Create the updated match document with the given details to be added to the collection
                    Document matchDoc = new Document();
                    matchDoc.append("Team1", txtTeam1.getText())
                            .append("Team2", txtTeam2.getText())
                            .append("Winner", winner)
                            .append("Score",  new Document("Team1", Integer.valueOf(txtScore1.getText())).append("Team2", Integer.valueOf(txtScore2.getText())))
                            .append("ExtraNotes", txtExtraNotes.getText());

                    //update document
                    Document update = new Document("$set", matchDoc);
                    //Request the new version of the document after the update
                    FindOneAndUpdateOptions optionAfter = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
                    //update the document in the database
                    Document updatedMatch = col.findOneAndUpdate(eq("_id",displayedMatch.getMatchID()), update, optionAfter);
                    //Close the database connection
                    db.close();

                    //Update the match being displayed and show the updated version
                    displayedMatch = new CompetitionMatch(
                            (ObjectId) updatedMatch.get("_id"),
                            (ObjectId) updatedMatch.get("TournamentID"),
                            (ObjectId) updatedMatch.get("BookingID"),
                            (String) updatedMatch.get("Team1"),
                            (String) updatedMatch.get("Team2"),
                            (String) updatedMatch.get("Winner"),
                            (Document) updatedMatch.get("Score"),
                            (String) updatedMatch.get("ExtraNotes"));

                    setMatch(displayedMatch);
                    readOnly();

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
                    e.printStackTrace();
                }
            }
        }
    }

    //Allow the user to edit other details such as teams names or extra notes
    public void btnEditOnAction()
    {
        if(btnEdit.getText().equals("Edit"))
        {
            //Make other fields editable
            txtTeam1.setEditable(true);
            txtTeam1.setStyle("-fx-background-color: white");
            txtTeam2.setEditable(true);
            txtTeam2.setStyle("-fx-background-color: white");
            txtExtraNotes.setEditable(true);
            txtExtraNotes.setStyle("-fx-background-color: white");

            btnAddResults.setDisable(true);
            btnSave.setDisable(false);
            btnClose.setDisable(true);

            btnEdit.setText("Cancel");
        }

        else if (btnEdit.getText().equals("Cancel"))
        {
            btnEdit.setText("Edit");
            displayMatchDetails();
        }
    }

    //Allow the user to add results to the match
    public void btnAddResultsOnAction()
    {
        if (btnAddResults.getText().equals("Add Results"))
        {
            System.out.println("hi");
            //Make the score texts editable
            txtScore1.setEditable(true);
            txtScore1.setStyle("-fx-background-color: white");
            txtScore2.setEditable(true);
            txtScore2.setStyle("-fx-background-color: white");

            //Change the other controls
            btnSave.setDisable(false);
            btnEdit.setDisable(true);
            btnClose.setDisable(true);

            btnAddResults.setText("Cancel");
        }

        else if (btnAddResults.getText().equals("Cancel"))
        {
            btnAddResults.setText("Add Result");
            displayMatchDetails();
        }
    }

    //Close window
    public void btnCloseOnAction()
    {
        Stage matchStage = (Stage) btnClose.getScene().getWindow();
        matchStage.close();
    }
    //Display match details
    public void displayMatchDetails()
    {
        //Facility name
        lblFacility.setText(matchBooking.getBookFacility());
        //Date
        lblDate.setText(matchBooking.getBookDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        //Time
        lblTime.setText(matchBooking.getBookTime());
        //Team1
        txtTeam1.setText(displayedMatch.getTeam1());
        //Team 2
        txtTeam2.setText(displayedMatch.getTeam2());
        //Score for team 1
        txtScore1.setText(String.valueOf( displayedMatch.getScore().get("Team1")));
        //Score for team 2
        txtScore2.setText(String.valueOf( displayedMatch.getScore().get("Team2")));
        //Any extra Notes
        txtExtraNotes.setText(displayedMatch.getExtraNotes());

        readOnly();
    }

    //Make the editable nodes read only
    public void readOnly()
    {
        txtTeam1.setEditable(false);
        txtTeam1.setStyle("-fx-background-color: transparent");
        txtTeam2.setEditable(false);
        txtTeam2.setStyle("-fx-background-color: transparent");
        txtScore1.setEditable(false);
        txtScore1.setStyle("-fx-background-color: transparent");
        txtScore2.setEditable(false);
        txtScore2.setStyle("-fx-background-color: transparent");

        //Some decoration
        String vb1Style = "-fx-border-color: #006400; -fx-border-radius:  20px 0px 0px 20px; -fx-background-radius:  20px 0px 0px 20px;";
        String vb2Style = "-fx-border-color: #006400; -fx-border-radius:  0px 20px 20px 0px; -fx-background-radius:   0px 20px 20px 0px;";

        //Color the scores depending on the result
        if(Integer.parseInt(txtScore1.getText()) > Integer.parseInt(txtScore2.getText())) {

            vbTeam1.setStyle("-fx-background-color:   #32CD32; " + vb1Style);
            vbTeam2.setStyle("-fx-background-color:   #FF634780; " + vb2Style);
        }
        else if (Integer.parseInt(txtScore2.getText()) > Integer.parseInt(txtScore1.getText())) {

            vbTeam1.setStyle("-fx-background-color:   #FF634780; " + vb1Style);
            vbTeam2.setStyle("-fx-background-color:  #32CD32; " + vb2Style);
        }
        else
        {
            vbTeam1.setStyle("-fx-background-color: transparent; " + vb1Style);
            vbTeam2.setStyle("-fx-background-color: transparent; " + vb2Style);
        }
        txtExtraNotes.setEditable(false);
        btnSave.setDisable(true);
        btnEdit.setDisable(false);
        btnEdit.setText("Edit");
        btnAddResults.setDisable(false);
        btnAddResults.setText("Add Results");
        btnClose.setDisable(false);
    }

    //Get the booking that that match will be played in
    public void getMatchBooking(ObjectId bookingId)
    {
        DBConnection db = new DBConnection();
        matchBooking= db.getMatchBooking(bookingId);
        db.close();
    }

    //Ensure entered data is suitable
    public boolean fieldsValidation()
    {
        //Ensure the score text boxes have only digits
        if(!txtScore1.getText().matches("[0-9]+") || !txtScore2.getText().matches("[0-9]+"))
         {
             lblInfo.setText("Scores can only contain numbers.");
             lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
             return false;
         }

        //If some fields are empty, return them default values
        if (txtTeam1.getText().isEmpty())
        {
            txtTeam1.setText("Team 1");
        }

        if (txtTeam2.getText().isEmpty())
        {
            txtTeam2.setText("Team 2");
        }

        if (txtScore1.getText().isEmpty())
        {
            txtScore1.setText("0");
        }

        if (txtScore2.getText().isEmpty())
        {
            txtScore2.setText("0");
        }

        return true;
    }

    //Get the winner depending on the scores
    public String getWinner()
    {
        //If team 1's score is greater than team 2's, make team 1 the winner
        if(Integer.parseInt(txtScore1.getText()) > Integer.parseInt(txtScore2.getText()))
        {
            return txtTeam1.getText();
        }

        //If team 2's score is greater than team 1's, make team 2 the winner
        else if ( Integer.parseInt(txtScore2.getText()) > Integer.parseInt(txtScore1.getText()))
        {
            return txtTeam2.getText();
        }

        else
        {
            return "";
        }
    }

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
