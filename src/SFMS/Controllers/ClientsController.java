package SFMS.Controllers;

import SFMS.Models.Client;
import SFMS.Models.DBConnection;
import SFMS.Models.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import javafx.animation.FadeTransition;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;


public class ClientsController {
    @FXML
    private Label lblInfo;

    @FXML
    private TextField txtID;

    @FXML
    private TextField txtFirstName;

    @FXML
    private TextField txtLastName;

    @FXML
    private TextField txtDOB;

    @FXML
    private ComboBox<String> cbGenre;

    @FXML
    private TextField txtTelNumber;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextArea txtExtraNotes;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnRemove;

    @FXML
    private Button btnNew;

    @FXML
    private GridPane gpClients;

    @FXML
    private GridPane gpTeams;

    @FXML
    private TextField txtSearch;

    //Clients array list
    ArrayList<Client> clientsArrayList = new ArrayList<>();

    //Teams array list
    ArrayList<Team> teamsArrayList = new ArrayList<>();
    public void initialize()
    {
        //Open database
        DBConnection db = new DBConnection();
        displayClients(db, false);
        displayFullDetails(clientsArrayList.get(0));
        displayTeams(db);
        db.close();
    }

    //Allows the user to save any changes
    public void btnSaveOnAction()
    {
        //Validate the fields
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
                        //Retrieve the clients collection to add the new client
                        MongoCollection<Document> col = db.collectionRetrieval("Clients");

                        Client showClient = null;

                        //if the field that holds ID is empty, it is a add a new client request
                        if (txtID.getText().isEmpty()) {
                            //Create the new client document with the given details to be added to the collection
                            ObjectId newObjectID = new ObjectId();
                            Document clientDoc = new Document("_id", newObjectID);
                            clientDoc.append("FirstName", txtFirstName.getText())
                                    .append("LastName", txtLastName.getText())
                                    .append("DoB", txtDOB.getText())
                                    .append("Genre", cbGenre.getValue())
                                    .append("TelNum", txtTelNumber.getText())
                                    .append("Email", txtEmail.getText())
                                    .append("ExtraNotes", txtExtraNotes.getText());

                            //save the document in the database
                            col.insertOne(clientDoc);

                            //assign the new client details to display them
                            showClient = new Client(newObjectID,
                                    txtFirstName.getText(),
                                    txtLastName.getText(),
                                    txtDOB.getText(),
                                    cbGenre.getValue(),
                                    txtTelNumber.getText(),
                                    txtEmail.getText(),
                                    txtExtraNotes.getText());
                        }
                        //If the text field that holds the ID is not empty, it is an update request
                        else {

                            //Create the updated client document with the given details to be added to the collection
                            Document clientDoc = new Document();
                            clientDoc.append("FirstName", txtFirstName.getText())
                                    .append("LastName", txtLastName.getText())
                                    .append("DoB", txtDOB.getText())
                                    .append("Genre", cbGenre.getValue())
                                    .append("TelNum", txtTelNumber.getText())
                                    .append("Email", txtEmail.getText())
                                    .append("ExtraNotes", txtExtraNotes.getText());

                            //update document
                            Document update = new Document("$set", clientDoc);
                            //Request the new version of the document after the update
                            FindOneAndUpdateOptions optionAfter = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
                            //update the document in the database
                            ObjectId clientID = new ObjectId(txtID.getText());
                            Document updatedDoc = col.findOneAndUpdate(eq("_id",clientID), update, optionAfter);

                            //assign the updated client details to display them
                            showClient = new Client((ObjectId) updatedDoc.get("_id"),
                                    txtFirstName.getText(),
                                    txtLastName.getText(),
                                    txtDOB.getText(),
                                    cbGenre.getValue(),
                                    txtTelNumber.getText(),
                                    txtEmail.getText(),
                                    txtExtraNotes.getText());
                        }


                        //Let the user know the details have been saved.
                        Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                        savedAlert.setTitle("Saved");
                        savedAlert.setHeaderText("Details have successfully been saved.");
                        customiseDialog(savedAlert.getDialogPane());
                        savedAlert.setContentText(null);
                        savedAlert.showAndWait();

                        //refresh the clients table
                        displayFullDetails(showClient);
                        displayClients(db, false);
                        //Close the database connection
                        db.close();

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

    //Allows the user to input details for a new client
    public void btnNewOnAction()
    {
        if (btnNew.getText().equals("New")) {
            //Clear all fields
            txtID.clear();
            txtFirstName.clear();
            txtLastName.clear();
            txtDOB.clear();
            txtDOB.setPromptText("dd/MM/yyyy");
            cbGenre.setPromptText("Select Genre");
            txtTelNumber.clear();
            txtEmail.clear();
            txtExtraNotes.clear();
            enableControls();

            btnSave.setDisable(false);
            btnNew.setText("Cancel");
            btnEdit.setDisable(true);
            btnRemove.setDisable(true);
        }

        else
        {
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
                //Ignore any changes and display the first client in the list
               displayFullDetails(clientsArrayList.get(0));
            }
        }

    }

    //Allows the user to edit the selected client
    public void btnEditOnAction()
    {
        if (btnEdit.getText().equals("Edit")) {
            //Enable all fields except the ID
            enableControls();

            btnSave.setDisable(false);
            btnEdit.setText("Cancel");
            btnNew.setDisable(true);
            btnRemove.setDisable(false);
        }

        else
        {
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

                //Ignore any changes and display the client that was being displayed
                for (Client client: clientsArrayList)
                {
                    if (client.getClientID().equals( new ObjectId(txtID.getText())))
                    {
                        displayFullDetails(client);
                    }
                }
            }
        }

    }

    //Allows the user to remove a client from the database
    public void btnRemoveOnAction()
    {
        //Entry Confirmation dialog, asks the user for a removal confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Client Removal Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to remove the displayed Client?");
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
                //Retrieve the clients collection to remove the client
                MongoCollection<Document> col = db.collectionRetrieval("Clients");

                //Remove the client
                col.deleteOne(eq("_id", new ObjectId(txtID.getText())));
                //refresh the clients list
                displayClients(db, false);
                //Close the database connection
                db.close();
                //Let the user know the details have been saved.
                Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                savedAlert.setTitle("Client Removed");
                savedAlert.setHeaderText("The Client has successfully been removed.");
                customiseDialog(savedAlert.getDialogPane());
                savedAlert.setContentText(null);
                savedAlert.showAndWait();
                //Display the firs client of the list
                displayFullDetails(clientsArrayList.get(0));


            }
            //If for some reason, the facility could not be removed, let the user know
            catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("There was an unknown error when attempting to remove the selected client");
                errorAlert.setContentText(null);
                customiseDialog(errorAlert.getDialogPane());
                errorAlert.showAndWait();
            }
        }
    }

    //validate the fields
    public boolean fieldsValidation()
    {
        //Ensure the first name field is not empty
        if (txtFirstName.getText().isEmpty()) {
            lblInfo.setText("Please fill the 'First Name' field");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //Ensure the last name field is not empty
        else if (txtLastName.getText().isEmpty()) {
            lblInfo.setText("Please fill the 'Last Name' field");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //Ensure the DoB field is not empty
        else if (txtDOB.getText().isEmpty()) {
            lblInfo.setText("Please fill the 'Date of Birth' field");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //Ensure a genre is selected
        else if (cbGenre.getValue().isEmpty()) {
            lblInfo.setText("Please select a Genre");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //Ensure the tel. number field is not empty
        else if (txtTelNumber.getText().isEmpty() && txtEmail.getText().isEmpty()) {
            lblInfo.setText("Please enter at least one form of contact");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Ensure the lengths are not too long
        else if (txtFirstName.getText().length() > 50) {
            lblInfo.setText("Fist Name is too long, please double check");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //Ensure the lengths are not too long
        else if (txtLastName.getText().length() > 50) {
            lblInfo.setText("Last Name is too long, please double check");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //validate the DOB
        else if (!validateDoB())
        {
            lblInfo.setText("Date of Birth is not valid, please double check.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //Check for length of the telephone number
        else if (txtTelNumber.getText().length() > 14) {
            lblInfo.setText("Telephone Number is too long. Please check again.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //Check for telephone invalid characters
        else if (txtTelNumber.getText().matches(".*[a-zA-Z].*")) {
            lblInfo.setText("Telephone Number cannot contain letters.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //validate the email field
        else if (!txtEmail.getText().contains("@")) {
            lblInfo.setText("Please enter a valid email address.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; ");
            return false;
        }

        return true;
    }

    //validate the DoB field
    public boolean validateDoB()
    {
        //Store the input date
        String fullDate = txtDOB.getText();
        //Ensure it has a forwards slash
         if(fullDate.contains("/")) {
            //split the different inputs
             String[] datesArray = fullDate.split("/");
             //check that there are 3 splits
             if(datesArray.length == 3)
             {
                 //check each date number has the right length
                 if (datesArray[0].length() > 2 || datesArray[1].length() > 2 || datesArray[2].length() > 4)
                 {
                     return false;
                 }

                 else
                 {
                     //Check the input are only numbers
                     if (!datesArray[0].matches("[0-9]+") || !datesArray[1].matches("[0-9]+") || !datesArray[2].matches("[0-9]+") )
                     {
                         return false;
                     }
                     else
                     {
                         //Check the numbers are valid
                         int day = Integer.parseInt(datesArray[0]);
                         int month = Integer.parseInt(datesArray[1]);
                         int year = Integer.parseInt(datesArray[2]);

                         //Year: min 10 years old, maximum 150 years old
                         if (day > 31 || month > 12 || year > 2011 || year < 1871 )
                         {
                             return false;
                         }
                     }
                 }
             }
             else
             {
                 return false;
             }
         }
         else
         {
             return false;
         }
         //If an invalid data was not found return true
         return true;
    }

    //Disable full details controls
    public void disableControls()
    {
        //Details
        txtFirstName.setDisable(true);
        txtLastName.setDisable(true);
        txtDOB.setDisable(true);
        cbGenre.setDisable(true);
        txtTelNumber.setDisable(true);
        txtEmail.setDisable(true);
        txtExtraNotes.setDisable(true);

        //Buttons
        btnSave.setDisable(true);
        btnNew.setDisable(false);
        btnEdit.setDisable(false);
        btnRemove.setDisable(false);
        btnNew.setText("New");
        btnEdit.setText("Edit");
        lblInfo.setText("");
    }

    //Enable controls
    public void enableControls()
    {
        txtFirstName.setDisable(false);
        txtLastName.setDisable(false);
        txtDOB.setDisable(false);
        txtDOB.setPromptText("dd/MM/yyyy");
        cbGenre.setDisable(false);
        txtTelNumber.setDisable(false);
        txtEmail.setDisable(false);
        txtExtraNotes.setDisable(false);
    }

    //Display the clients into the grid pane
    public void displayClients(DBConnection db, boolean search)
    {
        //Clear the list
        if (clientsArrayList.size() > 0)
        {
            clientsArrayList.clear();
        }

        //Ensure only two rows are present at the beginning of the clients addition
        //The firs row is the titles, no need to delete, the second one is the row to add a client
        while(gpClients.getRowConstraints().size() > 1)
        {
            gpClients.getRowConstraints().remove(1);
        }

        //Remove any clients being displayed on the first row
        gpClients.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null);

        //Get the clients list
        clientsArrayList = db.getClients();
        //Start the displaying from the second row in the grid pane
        int row = 1;
        //If the request doesn't come from a search input, display all clients
        if (!search) {
            for (Client client : clientsArrayList) {
                //Add a new row to display the client
                gpClients.getRowConstraints().add(new RowConstraints(50, 50, 50));

                //design the client row and add it to the grid pane
                createClientRow(client, row);
                row += 1;


            }

        }
        //If the display request comes from a search query, display only the clients that match the search filter
        else
        {
            for (Client client : clientsArrayList) {
                if(client.getLastName().toLowerCase().startsWith(txtSearch.getText().toLowerCase())) {
                    //Add a new row to display the client
                    gpClients.getRowConstraints().add(new RowConstraints(50, 50, 50));
                    //design the client row and add it to the grid pane
                    createClientRow(client, row);
                    row += 1;
                }

            }
        }
    }

    //Display teams into the grid pane
    public void displayTeams(DBConnection db)
    {
        //Clear the list
        if (teamsArrayList.size() > 0)
        {
            teamsArrayList.clear();
        }

        //Ensure only two rows are present at the beginning of the teams addition
        //The firs row is the titles, no need to delete, the second one is the row to add a client
        while(gpTeams.getRowConstraints().size() > 1)
        {
            gpTeams.getRowConstraints().remove(1);
        }

        //Remove any team being displayed on the first row
        gpTeams.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null);

        //Get the teams list
        teamsArrayList = db.getTeams();
        //Start the displaying from the second row in the grid pane
        int row = 1;
        for (Team team : teamsArrayList) {
            //Add a new row to display the team

            gpTeams.getRowConstraints().add(new RowConstraints(50, 50, 50));

            //design the client row and add it to the grid pane
            createTeamRow(team, row);
            row += 1;
        }
    }

    //Create the team row to tbe added to the grid pane
    public void createTeamRow(Team team, int row)
    {
        //Anchor panes for the cells
        String paneStyle = "-fx-border-width: 3px 0px 3px 0px; -fx-border-color: #000000;";
        AnchorPane namePane = new AnchorPane();
        namePane.setStyle(paneStyle);
        AnchorPane captainPane = new AnchorPane();
        captainPane.setStyle(paneStyle);
        AnchorPane numMembersPane = new AnchorPane();
        numMembersPane.setStyle(paneStyle);
        AnchorPane viewPane = new AnchorPane();
        viewPane.setStyle(paneStyle);


        //Name Label
        String style = "-fx-font-size: 14px; -fx-font-weight: bold;";
        Label lblName = new Label(team.getName());
        lblName.setStyle(style);
        lblName.setLayoutX(14);
        lblName.setLayoutY(15);
        lblName.setMaxWidth(135);
        namePane.getChildren().add(lblName);

        //Captain Label
        Label lblCaptain = new Label(team.getCaptain());
        lblCaptain.setStyle(style);
        lblCaptain.setLayoutX(14);
        lblCaptain.setLayoutY(15);
        lblCaptain.maxWidth(119);
        captainPane.getChildren().add(lblCaptain);

        //Num. Members Label
        Label lblNumMembers = new Label(String.valueOf(team.getNumMembers()));
        lblNumMembers.setStyle(style);
        lblNumMembers.setLayoutX(52);
        lblNumMembers.setLayoutY(15);
        numMembersPane.getChildren().add(lblNumMembers);

        //View Label
        Label lblView = new Label("View");
        lblView.setStyle(style);
        lblView.setLayoutX(14);
        lblView.setLayoutY(15);
        lblView.setUnderline(true);
        lblViewActions(lblView, team);
        viewPane.getChildren().add(lblView);

        //Add the panes to their corresponding cells
        gpTeams.add(namePane, 0, row);
        gpTeams.add(captainPane, 1, row);
        gpTeams.add(numMembersPane, 2, row);
        gpTeams.add(viewPane, 3, row);
    }

    public void lblViewActions(Label lblView, Team team)
    {
        //change the cursor type when user hovers over the label
        lblView.setOnMouseEntered( event -> {
            lblView.setCursor(Cursor.HAND);
        });
        lblView.setOnMouseExited( event -> {
            lblView.setCursor(Cursor.DEFAULT);
        });

        //Open the team view
        lblView.setOnMouseClicked( event -> {
            try
            {
                //Declare and instantiate a new stage to display a new window
                Stage viewTeamStage = new Stage();
                //Get the current stage to make the new stage's parent
                Stage parentStage = (Stage) lblView.getScene().getWindow();
                viewTeamStage.initOwner(parentStage);
                //Edit the modality so the user cannot do anything else until the new window is closed
                viewTeamStage.initModality(Modality.WINDOW_MODAL);
                //Remove the default OS windows designs
                viewTeamStage.initStyle(StageStyle.UNDECORATED);

                //Get the URL of the FXML document to be displayed a new scene
                URL url = Paths.get("./src/SFMS/Views/ViewTeamView.fxml").toUri().toURL();
                //Load the document
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(url);
                Parent root = loader.load();

                //Get the controller of FMXL file to pass some data to be displayed in the new stage
                ViewTeamController controller = loader.getController();
                //Pass required data
                controller.setTeam(team);


                //Declare and initialize a new scene with the retrieved FXML file
                Scene viewTeamScene = new Scene(root);
                //Set the new scene to the new stage
                viewTeamStage.setScene(viewTeamScene);
                //Create a simple fade in transition to the new scene
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
                //Show the scene and refresh
                viewTeamStage.showAndWait();
                DBConnection db = new DBConnection();
                displayTeams(db);
                db.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        });
    }

    //Create the client row to be added the grid pane
    public void createClientRow(Client client, int row)
    {
        //Anchor panes for the cells
        String paneStyle = "-fx-border-width: 3px 0px 3px 0px; -fx-border-color: #006400;";
        AnchorPane firstNamePane = new AnchorPane();
        firstNamePane.setStyle(paneStyle);
        displayFullDetailsSelection(client, firstNamePane);
//        firstNamePane.setOnMouseClicked(mouseEvent -> {
//            //Get the index of the selected member
//            int selectedMemberIndex = GridPane.getRowIndex(firstNamePane);
//
//
//            //ensure all other rows are not selected
//            for(Node anchorPane: gpClients.getChildren())
//            {
//                if (GridPane.getRowIndex(anchorPane) != selectedMemberIndex)
//                {
//                    anchorPane.setStyle("-fx-border-width: 3px 0px 3px 0px; -fx-border-color: #006400; -fx-background-color: transparent");
//                }
//                else
//                {
//                    //change the color to show the user the member is selected
//                    firstNamePane.setStyle("-fx-background-color: #32CD32");
//                }
//
//            }
//
//        });

        AnchorPane lastNamePane = new AnchorPane();
        lastNamePane.setStyle(paneStyle);
        displayFullDetailsSelection(client, lastNamePane);
        AnchorPane emailPane = new AnchorPane();
        emailPane.setStyle(paneStyle);
        displayFullDetailsSelection(client, emailPane);
        AnchorPane telNumberPane = new AnchorPane();
        telNumberPane.setStyle(paneStyle);
        displayFullDetailsSelection(client, telNumberPane);

        //First Name Label
        String style = "-fx-font-size: 14px; -fx-font-weight: bold;";
        Label lblFirstName = new Label(client.getFirstName());
        lblFirstName.setStyle(style);
        lblFirstName.setLayoutX(12);
        lblFirstName.setLayoutY(17);
        lblFirstName.setMaxWidth(112);
        firstNamePane.getChildren().add(lblFirstName);

        //Last Name label
        Label lblLastName = new Label(client.getLastName());
        lblLastName.setStyle(style);
        lblLastName.setLayoutX(7);
        lblLastName.setLayoutY(17);
        lblLastName.maxWidth(126);
        lastNamePane.getChildren().add(lblLastName);

        //Email label
        Label lblEmail = new Label(client.getEmail());
        lblEmail.setStyle(style);
        lblEmail.setLayoutX(7);
        lblEmail.setLayoutY(17);
        lblEmail.maxWidth(159);
        emailPane.getChildren().add(lblEmail);

        //Telephone Number label
        Label lblTelNumber = new Label(client.getTelNum());
        lblTelNumber.setStyle(style);
        lblTelNumber.setLayoutX(30);
        lblTelNumber.setLayoutY(12);
        telNumberPane.getChildren().add(lblTelNumber);

        //Add the panes to their corresponding cells
        gpClients.add(firstNamePane, 0, row);
        gpClients.add(lastNamePane, 1, row);
        gpClients.add(emailPane, 2, row);
        gpClients.add(telNumberPane, 3, row);
    }

    //Display a client's full details
    public void displayFullDetails(Client client) {
        txtID.setText(client.getClientID().toString());
        txtFirstName.setText(client.getFirstName());
        txtLastName.setText(client.getLastName());
        txtDOB.setText(client.getDoB());

        //populate the combo box and show the corresponding value
        cbGenre.getItems().clear();
        cbGenre.getItems().addAll("Male", "Female", "Prefer Not To Say");
        cbGenre.setStyle("-fx-font-weight: bold");
        switch (client.getGenre()) {
            case "Male":
                cbGenre.getSelectionModel().select(0);
                break;
            case "Female":
                cbGenre.getSelectionModel().select(1);
                break;
            case "Prefer Not To Say":
                cbGenre.getSelectionModel().select(3);
                break;

        }

        txtTelNumber.setText(client.getTelNum());
        txtEmail.setText(client.getEmail());
        txtExtraNotes.setText(client.getExtraNotes());
        disableControls();
    }

    //when the displays full details is called from a client selection
    public void displayFullDetailsSelection(Client client, AnchorPane pane)
    {
        //Click event
        pane.setOnMouseClicked( mouseEvent -> {

            //display full details
            displayFullDetails(client);
            //get the index of the selected row
            int selectedClientIndex = GridPane.getRowIndex(pane);

            //ensure all other rows are not selected
            for(Node anchorPane: gpClients.getChildren())
            {
                //ignore the first row
                if(GridPane.getRowIndex(anchorPane) != null) {

                    //Color only the selected row
                    if (GridPane.getRowIndex(anchorPane) != selectedClientIndex) {
                        anchorPane.setStyle("-fx-border-width: 3px 0px 3px 0px; -fx-border-color: #006400; -fx-background-color: transparent");
                    } else {
                        //change the color to show the user which client is selected
                        anchorPane.setStyle("-fx-background-color: #32CD32");
                    }
                }
            }});
    }

    //Make the clients table update for each key release on the search text field
    public void txtSearchOnKeyReleased()
    {
        //If the search box empty, display all clients
        DBConnection db = new DBConnection();
        displayClients(db, !txtSearch.getText().isEmpty());
        db.close();
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
