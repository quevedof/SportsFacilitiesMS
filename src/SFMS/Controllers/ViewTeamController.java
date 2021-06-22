package SFMS.Controllers;

import SFMS.Models.DBConnection;
import SFMS.Models.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import static com.mongodb.client.model.Filters.eq;

public class ViewTeamController {

    @FXML
    private Label lblInfo;

    @FXML
    private TextField txtCaptain;

    @FXML
    private TextField txtNumMembers;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnRemoveMember;

    @FXML
    private Button btnRemove;

    @FXML
    private Button btnAddMember;

    @FXML
    private GridPane gpTeamMembersList;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnNew;

    @FXML
    private TextField txtTeamName;

    @FXML
    private Label lblTeamName;

    @FXML
    private Button btnClose;


    //Displayed team
    private Team displayedTeam;

    //Team members array;
    ArrayList<String> teamMembersArray;

    //New team's members
    ArrayList<String> newTeamArray = new ArrayList<>();

    //Selected team member index
    int selectedMemberIndex = -1;

    //Get the team sent to the form
    public void setTeam(Team team)
    {
        this.displayedTeam = team;
        showTeamDetails(team);
    }

    //Save details displayed on screen
    @FXML
    public void btnSaveOnAction() {

        if (fieldsValidation()) {
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
                    //Retrieve the teams collection to add the new team
                    MongoCollection<Document> teamsCol = db.collectionRetrieval("Teams");

                    //This means it's a new team
                    if (btnNew.getText().equals("Cancel")) {
                        Document newTeamDoc = new Document()
                                .append("_id", new ObjectId())
                                .append("Name", txtTeamName.getText())
                                .append("Captain", txtCaptain.getText())
                                .append("NumMembers", newTeamArray.size())
                                .append("Members", newTeamArray);

                        teamsCol.insertOne(newTeamDoc);

                        displayedTeam = new Team((ObjectId) newTeamDoc.get("_id"),
                                (String) newTeamDoc.get("Name"),
                                (String) newTeamDoc.get("Captain"),
                                (int) newTeamDoc.get("NumMembers"),
                                (ArrayList<String>) newTeamDoc.get("Members"));
                    }
                    //Team edition
                    else {
                        //Create the updated team document with the given details to be added to the collection
                        Document teamDoc = new Document()
                                .append("Name", txtTeamName.getText())
                                .append("Captain", txtCaptain.getText())
                                .append("NumMembers", teamMembersArray.size())
                                .append("Members", teamMembersArray);

                        //update document
                        Document update = new Document("$set", teamDoc);
                        //Request the new version of the document after the update
                        FindOneAndUpdateOptions optionAfter = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
                        //update the document in the database
                       Document updatedDoc = teamsCol.findOneAndUpdate(eq("_id", displayedTeam.getTeamID()), update, optionAfter);

                        displayedTeam = new Team((ObjectId) updatedDoc.get("_id"),
                                (String) updatedDoc.get("Name"),
                                (String) updatedDoc.get("Captain"),
                                (int) updatedDoc.get("NumMembers"),
                                (ArrayList<String>) updatedDoc.get("Members"));
                    }

                    //Close the database connection
                    db.close();
                    //Display the new version of the document
                    showTeamDetails(displayedTeam);

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

    //Validate the the fields
    public boolean fieldsValidation()
    {
        //Ensure team name is not empty
        if (txtTeamName.getText().isEmpty()) {
            lblInfo.setText("Please enter a Team Name.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //ensure team captain is not empty
        else if (txtCaptain.getText().isEmpty()) {
            lblInfo.setText("Please enter a Team Captain.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        else
        {
            return true;
        }
    }

    //Allow the user to add a new team
    @FXML
    public void btnNewOnAction() {
        if (btnNew.getText().equals("New"))
        {
            //Clear fields
            txtTeamName.clear();
            txtCaptain.clear();
            txtNumMembers.clear();
            gpTeamMembersList.getChildren().clear();
            enableControls();
            btnNew.setText("Cancel");
            btnEdit.setDisable(true);
        }
        //If the button text is "Cancel" then cancel
        else
        {
            disableControls();
            showTeamDetails(this.displayedTeam);
        }

    }

    //Allow the user to edit the displayed team
    @FXML
    public void btnEditOnAction() {

        if (btnEdit.getText().equals("Edit"))
        {
            enableControls();
            txtTeamName.setVisible(true);
            txtTeamName.setText(displayedTeam.getName());
            btnEdit.setText("Cancel");
            btnNew.setDisable(true);
        }
        //If the button text is "Cancel" then cancel
        else
        {
            disableControls();
            showTeamDetails(this.displayedTeam);
        }

    }

    //Removes the displayed team
    @FXML
    public void btnRemoveOnAction() {
        //Entry Confirmation dialog, asks the user for a removal confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Team Removal Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to remove the displayed Team?");
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
                //Retrieve the teams collection to remove the team
                MongoCollection<Document> col = db.collectionRetrieval("Teams");

                //Remove the team
                col.deleteOne(eq("_id", displayedTeam.getTeamID()));
                //Close the database connection
                db.close();
                //Let the user know the details have been saved.
                Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                savedAlert.setTitle("Team Removed");
                savedAlert.setHeaderText("The Team has successfully been removed.");
                customiseDialog(savedAlert.getDialogPane());
                savedAlert.setContentText(null);
                savedAlert.showAndWait();

                //Close window to go back to the clients window
                btnCloseOnAction();

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

    //Allow the user to add a new team member
    @FXML
    public void btnAddMemberOnAction() {

        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("New Team Member");
        inputDialog.setHeaderText(null);
        inputDialog.setContentText("Please enter the new Team Member:");
        customiseDialog(inputDialog.getDialogPane());
        //Edit the "Ok" button of the dialog to display "Yes" instead
        Button yesButton = (Button) inputDialog.getDialogPane().lookupButton(ButtonType.OK);
        yesButton.setText("Add");
        //Modality
        inputDialog.initModality(Modality.APPLICATION_MODAL);

        // Get the response value
        Optional<String> result = inputDialog.showAndWait();
        result.ifPresent(newMember -> {

            //If the user is adding a new team, create a new members array and add it to the
            if(btnNew.getText().equals("Cancel"))
            {
                newTeamArray.add(newMember);
                //Let the user know the details have been saved.
                Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                savedAlert.setTitle("Saved");
                savedAlert.setHeaderText("Team Member Added");
                customiseDialog(savedAlert.getDialogPane());
                savedAlert.setContentText(null);
                savedAlert.showAndWait();
                displayTeamMembers(newTeamArray);

            }

            else {
                //Add the new member to the list
                teamMembersArray.add(newMember);
                try {
                    //Open database connection
                    DBConnection db = new DBConnection();
                    //Retrieve the teams collection to update the team members array
                    MongoCollection<Document> col = db.collectionRetrieval("Teams");

                    //Create the updated team members document with the given details to be added to the collection
                    Document teamDoc = new Document();
                    teamDoc.append("NumMembers", teamMembersArray.size())
                            .append("Members", teamMembersArray);

                    //update document
                    Document update = new Document("$set", teamDoc);
                    //Request the new version of the document after the update
                    FindOneAndUpdateOptions optionAfter = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
                    //update the document in the database
                    Document updatedDoc = col.findOneAndUpdate(eq("_id", displayedTeam.getTeamID()), update, optionAfter);
                    displayedTeam = new Team(
                            (ObjectId) updatedDoc.get("_id"),
                            (String) updatedDoc.get("Name"),
                            (String) updatedDoc.get("Captain"),
                            (int) updatedDoc.get("NumMembers"),
                            (ArrayList<String>) updatedDoc.get("Members"));
                    //Close the database connection

                    db.close();
                    //Display the new version of the document
                    showTeamDetails(displayedTeam);

                    //Let the user know the details have been saved.
                    Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                    savedAlert.setTitle("Saved");
                    savedAlert.setHeaderText("Team Member Added");
                    customiseDialog(savedAlert.getDialogPane());
                    savedAlert.setContentText(null);
                    savedAlert.showAndWait();

                }
                //If for some reason, the details cannot be saved, let the user know
                catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("There was an unknown error when attempting to add a new team member");
                    customiseDialog(errorAlert.getDialogPane());
                    errorAlert.showAndWait();

                }
            }
            });


    }

    //Removes a team member
    @FXML
    void btnRemoveMemberOnAction() {
        if (selectedMemberIndex == -1)
        {
            //Let the user know a member is not selected
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Team Member Not Selected");
            errorAlert.setHeaderText("Please select a Team Member to remove");
            errorAlert.setContentText(null);
            customiseDialog(errorAlert.getDialogPane());
            errorAlert.showAndWait();
        }
        else
        {
            //Entry Confirmation dialog, asks the user for a removal confirmation
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Team Member Removal Confirmation");
            confirmAlert.setHeaderText("Are you sure you want to remove the selected Team Member from the displayed team?");
            customiseDialog(confirmAlert.getDialogPane());

            //Edit the "Ok" button of the dialog to display "Yes" instead
            Button yesButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
            yesButton.setText("Yes");

            confirmAlert.initModality(Modality.APPLICATION_MODAL);
            Optional<ButtonType> result = confirmAlert.showAndWait();

            //Proceed if the user confirms the removal
            if (result.get() == ButtonType.OK) {

                if(btnNew.getText().equals("Cancel"))
                {
                    newTeamArray.remove(selectedMemberIndex);
                    displayTeamMembers(newTeamArray);
                    //Let the user know the details have been saved.
                    Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                    savedAlert.setTitle("Saved");
                    savedAlert.setHeaderText("Team Member Removed");
                    customiseDialog(savedAlert.getDialogPane());
                    savedAlert.setContentText(null);
                    savedAlert.showAndWait();
                }

                else {
                    //Remove the selected member from the list
                    teamMembersArray.remove(selectedMemberIndex);
                    try {
                        //Open database connection
                        DBConnection db = new DBConnection();
                        //Retrieve the teams collection to update the team members array
                        MongoCollection<Document> col = db.collectionRetrieval("Teams");

                        //Create the updated team members document with the given details to be added to the collection
                        Document teamDoc = new Document();
                        teamDoc.append("NumMembers", teamMembersArray.size())
                                .append("Members", teamMembersArray);

                        //update document
                        Document update = new Document("$set", teamDoc);
                        //Request the new version of the document after the update
                        FindOneAndUpdateOptions optionAfter = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
                        //update the document in the database
                        Document updatedDoc = col.findOneAndUpdate(eq("_id", displayedTeam.getTeamID()), update, optionAfter);
                        displayedTeam = new Team(
                                (ObjectId) updatedDoc.get("_id"),
                                (String) updatedDoc.get("Name"),
                                (String) updatedDoc.get("Captain"),
                                (int) updatedDoc.get("NumMembers"),
                                (ArrayList<String>) updatedDoc.get("Members"));
                        //Close the database connection
                        db.close();
                        //Display the new version of the document
                        showTeamDetails(displayedTeam);

                        //Let the user know the details have been saved.
                        Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                        savedAlert.setTitle("Saved");
                        savedAlert.setHeaderText("Team Member Removed");
                        customiseDialog(savedAlert.getDialogPane());
                        savedAlert.setContentText(null);
                        savedAlert.showAndWait();

                    }
                    //If for some reason, the details cannot be saved, let the user know
                    catch (Exception e) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("There was an unknown error when attempting to remove selected team member");
                        customiseDialog(errorAlert.getDialogPane());
                        errorAlert.showAndWait();

                    }
                }
            }
        }
    }

    @FXML
    public void btnCloseOnAction() {
        Stage viewTeamStage = (Stage) btnClose.getScene().getWindow();
        viewTeamStage.close();
    }

    //Show all details of a specific team
    public void showTeamDetails(Team team)
    {
        lblTeamName.setText(team.getName());
        txtCaptain.setText(team.getCaptain());
        txtNumMembers.setText(String.valueOf(team.getNumMembers()));

        //display all members of the team
        displayTeamMembers(team.getMembers());
        teamMembersArray = team.getMembers();

        //Disable not needed controls
        disableControls();
        btnNew.setText("New");
        btnEdit.setText("Edit");


    }

    //Display all members of a team in the grid pane
    public void displayTeamMembers(ArrayList<String> members)
    {
        //Clear any fields to refresh
        if (gpTeamMembersList.getChildren().size() > 0) {
            gpTeamMembersList.getChildren().clear();
        }

        //Ensure only two rows are present at the beginning of the team member addition
        //The firs row is the titles, no need to delete, the second one is the row to add a team member
        while(gpTeamMembersList.getRowConstraints().size() > 1)
        {
            gpTeamMembersList.getRowConstraints().remove(1);
        }

        //Start the displaying from the first row in the grid pane
        int row = 0;
        //add each team member to the grid pane
        for (String teamMember : members) {


            //Add a new row to display the team member
            gpTeamMembersList.getRowConstraints().add(new RowConstraints(40, 40, 40));

            //design the client row and add it to the grid pane
            createTeamMemberRow(teamMember, row);
            row += 1;
        }
    }

    //Creat the team member row to be added to the grid pane
    public void createTeamMemberRow(String teamMember, int row)
    {
        //Anchor Pane
        AnchorPane memberPane = new AnchorPane();
        memberPane.setStyle("-fx-border-width: 3px 0px 3px 0px; -fx-border-color: #006400;");
        memberPane.setOnMouseClicked(mouseEvent -> {
            //Get the index of the selected member
            selectedMemberIndex = GridPane.getRowIndex(memberPane);

            //ensure all other rows are not selected
            for(Node anchorPane: gpTeamMembersList.getChildren())
            {
                if (GridPane.getRowIndex(anchorPane) != selectedMemberIndex)
                {
                anchorPane.setStyle("-fx-border-width: 3px 0px 3px 0px; -fx-border-color: #006400; -fx-background-color: transparent");
                }
                else
                {
                    //change the color to show the user the member is selected
                    memberPane.setStyle("-fx-background-color: #32CD32");
                }
            }
        });

        //Label
        Label memberLabel = new Label(teamMember);
        memberLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        memberLabel.setLayoutX(9);
        memberLabel.setLayoutY(10);
        memberLabel.setMaxWidth(140);

        memberPane.getChildren().add(memberLabel);
        //Add the pane to the grid cell
        gpTeamMembersList.add(memberPane, 0, row);
    }

    //Disable controls when they're not needed
    public void disableControls()
    {
        //Text fields
        txtCaptain.setDisable(true);
        txtNumMembers.setDisable(true);
        txtTeamName.setVisible(false);
        lblTeamName.setVisible(true);

        //Buttons
        btnClose.setDisable(false);
        btnSave.setDisable(true);
        btnNew.setDisable(false);
        btnEdit.setDisable(false);
        btnRemove.setDisable(false);

    }

    //Enable controls when needed
    public void enableControls()
    {
        //Text fields
        txtCaptain.setDisable(false);
        txtNumMembers.setDisable(true);
        txtTeamName.setVisible(true);
        lblTeamName.setVisible(false);

        //Buttons
        btnClose.setDisable(true);
        btnSave.setDisable(false);
        btnRemove.setDisable(true);
    }

    //Small customisation of dialogs
    public void customiseDialog(DialogPane dialog)
    {
        //Add a background color and change the font size
        dialog.setStyle("-fx-background-color: #B4D6E4; -fx-font-size: 14px; -fx-font-weight: bold");
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
