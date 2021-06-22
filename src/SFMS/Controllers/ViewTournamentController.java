package SFMS.Controllers;

import SFMS.Models.CompetitionMatch;
import SFMS.Models.DBConnection;
import SFMS.Models.Tournament;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class ViewTournamentController {

    @FXML
    private Button btnClose;

    @FXML
    private Label lblName;

    @FXML
    private HBox hbBracket;

    @FXML
    private Label lblInfo;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtActivity;

    @FXML
    private TextField txtType;

    @FXML
    private TextField txtStartDate;

    @FXML
    private TextField txtEndDate;

    @FXML
    private TextField txtTime;

    @FXML
    private TextField txtMinAge;

    @FXML
    private TextField txtMaxAge;

    @FXML
    private TextField txtNoTeams;

    @FXML
    private TextField txtPrize;

    @FXML
    private TextArea txtExtraNotes;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnRemove;

    //Tournament being displayed on screen
    Tournament tournamentSelected;

    //ArrayList of the matches that belong to the displayed tournament
    ArrayList<CompetitionMatch> matchesList = new ArrayList<>();


     //Display the tournament selected
    public void setTournamentSelected(Tournament tour)
    {
        this.tournamentSelected = tour;
        //details
        displayTournamentDetails();

        //Get all matches
        displayAllMatches();
    }

    //Save any changes made to the tournament details
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
                    //Retrieve the tournaments collection to update the details
                    MongoCollection<Document> col = db.collectionRetrieval("Tournaments");

                    //Create the updated tournament document with the given details to be added to the collection
                    Document tournamentDoc = new Document();
                    tournamentDoc.append("Name", txtName.getText())
                            .append("Age", txtMinAge.getText() + "-" + txtMaxAge.getText() + " y/o")
                            .append("Prize", txtPrize.getText())
                            .append("ExtraNotes", txtExtraNotes.getText());

                    //update document
                    Document update = new Document("$set", tournamentDoc);
                    //Request the new version of the document after the update
                    FindOneAndUpdateOptions optionAfter = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
                    //update the document in the database
                    Document updatedDoc = col.findOneAndUpdate(eq("_id",tournamentSelected.getId()), update, optionAfter);


                    //Update the tournament name on the matches' bookings too
                    MongoCollection<Document> matchesCol = db.collectionRetrieval("Matches");
                    ArrayList<Document> matchesDocs = matchesCol.find(eq("TournamentID", tournamentSelected.getId())).into(new ArrayList<>());

                    MongoCollection<Document> bookingsCol = db.collectionRetrieval("Bookings");
                    for (Document matchDoc: matchesDocs)
                    {
                        bookingsCol.updateOne(eq("_id", matchDoc.get("BookingID")), set("bookClientName", updatedDoc.get("Name")));
                    }


                    //Close the database connection
                    db.close();

                    //Assign the tournament updated instance to the new updated tournament document
                    //Format the ISO date received from MongoDB to Local Date
                    Date dbdate = (Date) updatedDoc.get("DateStart");
                    LocalDate dateStart = dbdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Date dbdate1 = (Date) updatedDoc.get("DateEnd");
                    LocalDate dateEnd = dbdate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    tournamentSelected = new Tournament(
                            (ObjectId) updatedDoc.get("_id"),
                            (String) updatedDoc.get("Name"),
                            (String) updatedDoc.get("Activity"),
                            (String) updatedDoc.get("Type"),
                            dateStart,
                            dateEnd,
                            (String) updatedDoc.get("Time"),
                            (String) updatedDoc.get("Age"),
                            (int) updatedDoc.get("NoTeams"),
                            (String) updatedDoc.get("Prize"),
                            (String) updatedDoc.get("ExtraNotes")
                    );

                    //Display the new version of the document
                    displayTournamentDetails();

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

    //Validation for data input
    public boolean fieldsValidation()
    {
        if (txtName.getText().isEmpty())
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

    //Allow the user to edit some tournament details
    public void btnEditOnAction()
    {
        if (btnEdit.getText().equals("Edit"))
        {
            enableTextFields();
        }

        else if(btnEdit.getText().equals("Cancel"))
        {
            displayTournamentDetails();
        }
    }

    //Allow the user to remove the tournament from the database
    public void btnRemoveOnAction()
    {
        //Entry Confirmation dialog, asks the user for a removal confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Tournament Removal Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to remove the displayed Tournament?\n" +
                "All bookings associated with the selected tournament will also be removed.");
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

                //Remove all matches of the tournament from the database
                MongoCollection<Document> matchesCol = db.collectionRetrieval("CompetitionMatches");
                //Get all all matches
                ArrayList<Document> matches = matchesCol.find(eq("TournamentID", tournamentSelected.getId())).into(new ArrayList<>());
                //Remove all matches
                matchesCol.deleteMany(eq("TournamentID", tournamentSelected.getId()));

                //Loop through all the matches and remove their corresponding bookings
                MongoCollection<Document> bookingsCol = db.collectionRetrieval("Bookings");
                for(Document matchDoc: matches)
                {
                    bookingsCol.deleteOne(eq("_id", matchDoc.get("BookingID")));
                }


                //Retrieve the tournament collection to remove the selected tournament
                MongoCollection<Document> tournamentsCol = db.collectionRetrieval("Tournaments");
                //Remove the tournament
                tournamentsCol.deleteOne(eq("_id", tournamentSelected.getId()));
                //Close the database connection
                db.close();

                //Let the user know the the removal was successful.
                Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                savedAlert.setTitle("Tournament Removed");
                savedAlert.setHeaderText("The selected Tournament has successfully been removed.");
                customiseDialog(savedAlert.getDialogPane());
                savedAlert.setContentText(null);
                savedAlert.showAndWait();

                //Close the tournament window
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

    //Get all matches that belong to the selected tournament
    public void displayAllMatches()
    {
        //Get all required matches from the database
        DBConnection db = new DBConnection();
        matchesList = db.getMatches(tournamentSelected.getId());
        db.close();


        // bracket based on the number of teams in the tournament
        createTeamBracket(tournamentSelected.getNoTeams(), matchesList);

    }


    //Displays all details of the selected tournament
    public void displayTournamentDetails()
    {
        lblName.setText(tournamentSelected.getName());
        txtName.setText(tournamentSelected.getName());
        txtActivity.setText(tournamentSelected.getActivity());
        txtType.setText(tournamentSelected.getType());
        txtStartDate.setText(tournamentSelected.getDateStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtEndDate.setText(tournamentSelected.getDateEnd().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtTime.setText(tournamentSelected.getTime());
        //Split the age
        String[] age = tournamentSelected.getAge().split("[- ]");
        String minAge = age[0];
        String maxAge = age[1];

        txtMinAge.setText(minAge);
        txtMaxAge.setText(maxAge);
        txtNoTeams.setText(String.valueOf( tournamentSelected.getNoTeams()));
        txtPrize.setText(tournamentSelected.getPrize());
        txtExtraNotes.setText(tournamentSelected.getExtraNotes());

        disableTextFields();
    }

    //Disable all textfields
    public void disableTextFields()
    {
        txtName.setDisable(true);
        txtActivity.setDisable(true);
        txtType.setDisable(true);
        txtStartDate.setDisable(true);
        txtEndDate.setDisable(true);
        txtTime.setDisable(true);
        txtMinAge.setDisable(true);
        txtMaxAge.setDisable(true);
        txtNoTeams.setDisable(true);
        txtPrize.setDisable(true);
        txtExtraNotes.setDisable(true);

        btnSave.setDisable(true);
        btnClose.setDisable(false);
        btnRemove.setDisable(false);
        btnEdit.setText("Edit");
        lblInfo.setText("");
    }

    //Enable only the fields that can be edited
    public void enableTextFields()
    {
        txtName.setDisable(false);
        txtMinAge.setDisable(false);
        txtMaxAge.setDisable(false);
        txtPrize.setDisable(false);
        txtExtraNotes.setDisable(false);

        //Allow the user to either cancel or save
        btnSave.setDisable(false);
        btnClose.setDisable(true);
        btnRemove.setDisable(true);
        btnEdit.setText("Cancel");

    }

    //Create and display the bracket
    public void createTeamBracket(int teamsNum, ArrayList<CompetitionMatch> matchesList)
    {
        //Clear any children in the pane
        if (hbBracket.getChildren().size() > 0)
        {
            hbBracket.getChildren().clear();
        }

        //Declare all possible vboxes that can exist in the bracket
        VBox vb1 = new VBox();
        vb1.setPrefWidth(226);
        VBox vb2 = new VBox();
        vb2.prefWidth(232);
        VBox vb3 = new VBox();
        VBox vb4 = new VBox();


        //Create the right amount of matches in the bracket depending on the tournament's size
        if (teamsNum == 4) {
            //Add upper pane, then lower pane for the first vb
            vb1.getChildren().addAll(getUpperPane(matchesList.get(0)), getLowerPane(matchesList.get(1)));

            //If the previous matches have winners, add them to the next match
            if(!matchesList.get(0).getWinner().equals(""))
            {
                matchesList.get(2).setTeam1(matchesList.get(0).getWinner());
            }
            if(!matchesList.get(1).getWinner().equals(""))
            {
                matchesList.get(2).setTeam2(matchesList.get(1).getWinner());
            }

            //Add final pane for the second vb
            vb2.getChildren().addAll(getFinalPane(matchesList.get(2)));

            //add both vbs to the bracket hb
            hbBracket.getChildren().addAll(vb1, vb2);
        }

        else if (teamsNum == 8)
        {
            //Add two upper panes and two lower panes for the first vb
            vb1.getChildren().addAll(getUpperPane(matchesList.get(0)), getLowerPane(matchesList.get(1)), getUpperPane(matchesList.get(2)), getLowerPane(matchesList.get(3)));

            //If the previous matches have winners, add them to the next round's match
            if(!matchesList.get(0).getWinner().equals(""))
            {
                matchesList.get(4).setTeam1(matchesList.get(0).getWinner());
            }
            if(!matchesList.get(1).getWinner().equals(""))
            {
                matchesList.get(4).setTeam2(matchesList.get(1).getWinner());
            }

            if(!matchesList.get(2).getWinner().equals(""))
            {
                matchesList.get(5).setTeam1(matchesList.get(2).getWinner());
            }
            if(!matchesList.get(3).getWinner().equals(""))
            {
                matchesList.get(5).setTeam2(matchesList.get(3).getWinner());
            }

            //Add two next round Panes to the second vb
            vb2.getChildren().addAll(getNexRoundUpperPane("1", matchesList.get(4)), getNextRoundLowerPane("1", matchesList.get(5)));
            //If the previous matches have winners, add them to the next round's match
            if(!matchesList.get(4).getWinner().equals(""))
            {
                matchesList.get(6).setTeam1(matchesList.get(4).getWinner());
            }
            if(!matchesList.get(5).getWinner().equals(""))
            {
                matchesList.get(4).setTeam2(matchesList.get(5).getWinner());
            }
            //Add one final pane to the third vb
            AnchorPane finalPane = getFinalPane(matchesList.get(6));
            VBox.setMargin(finalPane, new Insets(225, 0, 0, 3));
            vb3.getChildren().addAll(finalPane);

            //add all vbs to the hbox that holds the bracket
            hbBracket.getChildren().addAll(vb1, vb2, vb3);
        }

        else if (teamsNum == 16)
        {

            //Add 4 upper panes and 4 lower panes for the first vb
            vb1.getChildren().addAll(getUpperPane(matchesList.get(0)), getLowerPane(matchesList.get(1)), getUpperPane(matchesList.get(2)), getLowerPane(matchesList.get(3)),
                    getUpperPane(matchesList.get(4)), getLowerPane(matchesList.get(5)), getUpperPane(matchesList.get(6)), getLowerPane(matchesList.get(7)));

            //If the previous matches have winners, add them to the next round's match
            if(!matchesList.get(0).getWinner().equals(""))
            {
                matchesList.get(8).setTeam1(matchesList.get(0).getWinner());
            }
            if(!matchesList.get(1).getWinner().equals(""))
            {
                matchesList.get(8).setTeam2(matchesList.get(1).getWinner());
            }

            if(!matchesList.get(2).getWinner().equals(""))
            {
                matchesList.get(9).setTeam1(matchesList.get(2).getWinner());
            }
            if(!matchesList.get(3).getWinner().equals(""))
            {
                matchesList.get(9).setTeam2(matchesList.get(3).getWinner());
            }

            //If the previous matches have winners, add them to the next round's match
            if(!matchesList.get(4).getWinner().equals(""))
            {
                matchesList.get(10).setTeam1(matchesList.get(4).getWinner());
            }
            if(!matchesList.get(5).getWinner().equals(""))
            {
                matchesList.get(10).setTeam2(matchesList.get(5).getWinner());
            }

            if(!matchesList.get(6).getWinner().equals(""))
            {
                matchesList.get(11).setTeam1(matchesList.get(6).getWinner());
            }
            if(!matchesList.get(7).getWinner().equals(""))
            {
                matchesList.get(11).setTeam2(matchesList.get(7).getWinner());
            }
            //Add 4 next round Panes to the second vb
            //Input correct margins
            AnchorPane pane1 = getNexRoundUpperPane("1", matchesList.get(10));
            VBox.setMargin(pane1, new Insets(155, 0, 0, 0));
            AnchorPane pane2 = getNextRoundLowerPane("1", matchesList.get(11));
            VBox.setMargin(pane2, new Insets(60, 0, 0, 0));
            vb2.getChildren().addAll(getNexRoundUpperPane("1", matchesList.get(8)), getNextRoundLowerPane("1", matchesList.get(9)), pane1, pane2);

            //If the previous matches have winners, add them to the next round's match
            if(!matchesList.get(8).getWinner().equals(""))
            {
                matchesList.get(12).setTeam1(matchesList.get(8).getWinner());
            }
            if(!matchesList.get(9).getWinner().equals(""))
            {
                matchesList.get(12).setTeam2(matchesList.get(9).getWinner());
            }

            if(!matchesList.get(10).getWinner().equals(""))
            {
                matchesList.get(13).setTeam1(matchesList.get(10).getWinner());
            }
            if(!matchesList.get(11).getWinner().equals(""))
            {
                matchesList.get(13).setTeam2(matchesList.get(11).getWinner());
            }
            //Add 2 to the third
            //Correct margins
            AnchorPane pane11 = getNexRoundUpperPane("2", matchesList.get(12));
            VBox.setMargin(pane11, new Insets(225, 0, 0, 3));
            AnchorPane pane22 = getNextRoundLowerPane("2", matchesList.get(13));
            VBox.setMargin(pane22, new Insets(200, 0, 0, 3));
            vb3.getChildren().addAll(pane11, pane22);

            //If the previous matches have winners, add them to the next round's match
            if(!matchesList.get(12).getWinner().equals(""))
            {
                matchesList.get(14).setTeam1(matchesList.get(12).getWinner());
            }
            if(!matchesList.get(13).getWinner().equals(""))
            {
                matchesList.get(14).setTeam2(matchesList.get(13).getWinner());
            }

            //Add one final pane to the fourth vb
            AnchorPane finalPane = getFinalPane(matchesList.get(14));
            VBox.setMargin(finalPane, new Insets(510, 0, 0, 3));
            vb4.getChildren().addAll(finalPane);

            //add all vbs to the bracket hb
            hbBracket.getChildren().addAll(vb1, vb2, vb3, vb4);
        }

    }

    //Anchor pane for upper matches
    public AnchorPane getUpperPane(CompetitionMatch match)
    {
        //Anchor pane that holds an upper match
        AnchorPane upperMatch = new AnchorPane();
        upperMatch.setPrefWidth(200);
        upperMatch.setPrefHeight(158);

        //Label that opens up the match info
        Label lblInfo = new Label();
        lblInfo.setText("Info.");
        lblInfo.setStyle("-fx-font-size: 14px; -fx-underline: true;");
        lblInfo.setLayoutX(16);
        lblInfo.setLayoutY(32);
        setLblInfo(lblInfo, match);

        //Progress lines as imageview
        ImageView upperBracketLineImage = getBracketLine("Upper");

        //Get the gridpane that will display the match teams and the result
        GridPane gpMatch = getMatchGridPane(match);

        //add the grid pane, the info label and the picture to the anchor pane
        upperMatch.getChildren().addAll(gpMatch, lblInfo, upperBracketLineImage);

        return upperMatch;
    }

    //Anchor pane for lower matches
    public AnchorPane getLowerPane(CompetitionMatch match)
    {
        //LOWER match
        //Anchor pane that holds an upper match
        AnchorPane lowerMatch = new AnchorPane();
        lowerMatch.setPrefWidth(200);
        lowerMatch.setPrefHeight(158);

        //Label that opens up the match info
        Label lblInfo1 = new Label();
        lblInfo1.setText("Info.");
        lblInfo1.setStyle("-fx-font-size: 14px; -fx-underline: true;");
        lblInfo1.setLayoutX(16);
        lblInfo1.setLayoutY(32);
        setLblInfo(lblInfo1, match);

        //Progress lines as imageview
        ImageView lowerBracketLineImage = getBracketLine("Lower");

        //Get the gridpane that will display the match teams and the result
        GridPane gpMatch1 = getMatchGridPane(match);

        //add the grid pane, the info label and the picture to the anchor pane
        lowerMatch.getChildren().addAll(gpMatch1, lblInfo1, lowerBracketLineImage);

        //return the pane
        return lowerMatch;
    }

    //Next round upper pane
    public AnchorPane getNexRoundUpperPane(String round, CompetitionMatch match)
    {
        //Anchor pane that holds an upper match
        AnchorPane nextMatch = new AnchorPane();
        nextMatch.setPrefWidth(200);
        nextMatch.setPrefHeight(158);
        VBox.setMargin(nextMatch, new Insets(75, 0, 0, 0));

        //Label that opens up the match info
        Label lblInfo2 = new Label("Info.");
        lblInfo2.setStyle("-fx-font-size: 14px; -fx-underline: true;");
        lblInfo2.setLayoutX(29);
        lblInfo2.setLayoutY(34);
        //Set the label info with all the function
        setLblInfo(lblInfo2, match);

        //Progress lines as imageview
        ImageView nextBracketLineImage = getBracketLine("Final Round");
        ImageView nextBracketLineImage1 = null;
        if(round.equals("1"))
        {
            nextBracketLineImage1  = getBracketLine("Upper2");
        }

        else if (round.equals("2"))
        {
            nextBracketLineImage1  = getBracketLine("Upper22");
        }

        //Get the gridpane that will display the match teams and the result
        GridPane gpMatch2 = getMatchGridPane(match);
        gpMatch2.setLayoutX(30);
        gpMatch2.setLayoutY(60);

        //add the grid pane, the info label and the picture to the anchor pane
        nextMatch.getChildren().addAll(gpMatch2, lblInfo2, nextBracketLineImage, nextBracketLineImage1);

        return nextMatch;
    }


    //Next round lower pane
    public AnchorPane getNextRoundLowerPane(String round, CompetitionMatch match)
    {
        //Anchor pane that holds an upper match
        AnchorPane nextMatch = new AnchorPane();
        nextMatch.setPrefWidth(200);
        nextMatch.setPrefHeight(158);
        VBox.setMargin(nextMatch, new Insets(60, 0, 0, 0));

        //Label that opens up the match info
        Label lblInfo2 = new Label("Info.");
        lblInfo2.setStyle("-fx-font-size: 14px; -fx-underline: true;");
        lblInfo2.setLayoutX(29);
        lblInfo2.setLayoutY(34);
        setLblInfo(lblInfo2, match);

        //Progress lines as imageview
        ImageView nextBracketLineImage = getBracketLine("Final Round");
        ImageView nextBracketLineImage1 = null;
        if(round.equals("1"))
        {
            nextBracketLineImage1  = getBracketLine("Lower2");
        }

        else if (round.equals("2"))
        {
            nextBracketLineImage1  = getBracketLine("Lower22");
        }

        //Get the gridpane that will display the match teams and the result
        GridPane gpMatch2 = getMatchGridPane(match);
        gpMatch2.setLayoutX(30);
        gpMatch2.setLayoutY(60);

        //add the grid pane, the info label and the picture to the anchor pane
        nextMatch.getChildren().addAll(gpMatch2, lblInfo2, nextBracketLineImage, nextBracketLineImage1);

        return nextMatch;
    }
    //Final round pane
    public AnchorPane getFinalPane(CompetitionMatch match)
    {
        //final round match

        //Anchor pane that holds an upper match
        AnchorPane nextMatch = new AnchorPane();
        nextMatch.setPrefWidth(200);
        nextMatch.setPrefHeight(158);
        VBox.setMargin(nextMatch, new Insets(70, 0, 0, 0));

        //Label that opens up the match info
        Label lblInfo2 = new Label("Info.");
        lblInfo2.setStyle("-fx-font-size: 14px; -fx-underline: true;");
        lblInfo2.setLayoutX(29);
        lblInfo2.setLayoutY(34);
        setLblInfo(lblInfo2, match);

        //Final label
        Label lblFinal = new Label("The Final");
        lblFinal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        lblFinal.setLayoutX(80);
        lblFinal.setLayoutY(6);

        //Progress lines as imageview
        ImageView nextBracketLineImage = getBracketLine("Final Round");

        //Get the gridpane that will display the match teams and the result
        GridPane gpMatch2 = getMatchGridPane(match);
        gpMatch2.setLayoutX(30);
        gpMatch2.setLayoutY(60);

        //add the grid pane, the info label and the picture to the anchor pane
        nextMatch.getChildren().addAll(gpMatch2, lblInfo2, lblFinal, nextBracketLineImage);

        return nextMatch;
    }

    //Grid pane that shows the team names and score
    public GridPane getMatchGridPane(CompetitionMatch match)
    {
        //Declare grid pane
        GridPane gpMatch = new GridPane();
        //Set preferred width and height
        gpMatch.setMinWidth(170.4);
        gpMatch.setPrefWidth(170.4);
        gpMatch.setMaxWidth(170.4);
        gpMatch.setMinHeight(85.6);
        gpMatch.setPrefHeight(85.6);
        gpMatch.setMaxHeight(85.6);
        //Make lines visible
        gpMatch.setGridLinesVisible(true);
        //Set layout of the grid
        gpMatch.setLayoutX(16);
        gpMatch.setLayoutY(60);
        //Add two rows
        gpMatch.getRowConstraints().add(new RowConstraints(42, 42, 42));
        gpMatch.getRowConstraints().add(new RowConstraints(42, 42, 42));
        //Add two columns
        gpMatch.getColumnConstraints().add(new ColumnConstraints(135, 135, 135));
        gpMatch.getColumnConstraints().add(new ColumnConstraints(34, 34, 34));

        //Both labels that will display the team names with a small margin
        Label lblTeam1 = new Label(match.getTeam1());
        lblTeam1.setStyle("-fx-font-size: 16px; -fx-font-style: italic; -fx-font-weight: bold");
        GridPane.setMargin(lblTeam1, new Insets(0, 0, 0, 15));
        Label lblTeam2 = new Label(match.getTeam2());
        lblTeam2.setStyle("-fx-font-size: 16px; -fx-font-style: italic; -fx-font-weight: bold");
        GridPane.setMargin(lblTeam2, new Insets(0, 0, 0, 15));

        //Both labels that will display the score with a small margin
        Label lblScore1 = new Label(String.valueOf( match.getScore().get("Team1")));
        lblScore1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold");
        GridPane.setMargin(lblScore1, new Insets(0, 0, 0, 12));
        Label lblScore2 = new Label(String.valueOf( match.getScore().get("Team2")));
        lblScore2.setStyle("-fx-font-size: 16px; -fx-font-weight: bold");
        GridPane.setMargin(lblScore2, new Insets(0, 0, 0, 12));

        //Panes to color for the winner team's score
        Pane score1Pane = new Pane();
        Pane score2Pane = new Pane();

        //Color the winner
        if (lblTeam1.getText().equals(match.getWinner()))
        {
            score1Pane.setStyle("-fx-background-color:  #98FB98");
        }
        else if (lblTeam2.getText().equals(match.getWinner()))
        {
            score2Pane.setStyle("-fx-background-color:  #98FB98");
        }

        //Add all four labels and the color panes to the grid pane
        //col, row
        gpMatch.add(lblTeam1, 0, 0);
        gpMatch.add(lblTeam2, 0, 1);
        gpMatch.add(score1Pane, 1, 0);
        gpMatch.add(lblScore1, 1, 0);
        gpMatch.add(score2Pane, 1, 1);
        gpMatch.add(lblScore2, 1, 1);

        return gpMatch;
    }

    //Set the label that is displayed next to each match to display full details and to record results
    public void setLblInfo(Label lbl, CompetitionMatch match)
    {
        //Change the cursor when the user hovers over the info label
        lbl.setOnMouseEntered( event -> {
            lbl.setCursor(Cursor.HAND);
        });
        lbl.setOnMouseExited( event -> {
            lbl.setCursor(Cursor.DEFAULT);
        });

        //Open the match view window if they user clicks on the label
        lbl.setOnMouseClicked( event -> {
            try
            {
                //Declare and instantiate a new stage to display a new window
                Stage viewMatchStage = new Stage();
                //Get the current stage to make the new stage's parent
                Stage parentStage = (Stage) btnClose.getScene().getWindow();
                viewMatchStage.initOwner(parentStage);
                //Edit the modality so the user cannot do anything else until the new window is closed
                viewMatchStage.initModality(Modality.WINDOW_MODAL);
                //Remove the default OS windows designs
                viewMatchStage.initStyle(StageStyle.UNDECORATED);

                //Get the URL of the FXML document to be displayed a new scene
                URL url = Paths.get("./src/SFMS/Views/ViewMatchView.fxml").toUri().toURL();
                //Load the document
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(url);
                Parent root = loader.load();

                //Get the controller of FMXL file to pass some data to be displayed in the new stage
                ViewMatchController controller = loader.getController();
                //Pass required data
                controller.setMatch(match);


                //Declare and initialize a new scene with the retrieved FXML file
                Scene viewMatchScene = new Scene(root);
                //Set the new scene to the new stage
                viewMatchStage.setScene(viewMatchScene);
                //Create a simple fade in transition to the new scene
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
                //Show the scene and refresh
                viewMatchStage.showAndWait();
                displayAllMatches();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        });
    }


    //Get the image that represents the lines showing team progression
    //The image depends on the match's round
    public ImageView getBracketLine(String match)
    {

        Image img = null;

        //Get the corresponding picture for each match
        if (match.equals("Upper"))
        {

            try {
                URL greenTickURL = Paths.get("src/SFMS/Views/Images/bracketLineTop.png").toUri().toURL();
                img = new Image(greenTickURL.toExternalForm());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Initialise the picture view
            ImageView picture = new ImageView(img);
            picture.setFitWidth(36);
            picture.setFitHeight(55);
            picture.setLayoutX(187);
            picture.setLayoutY(102);

            //return it
            return picture;
        }

        else if(match.equals("Upper2"))
        {

            try {
                URL greenTickURL = Paths.get("src/SFMS/Views/Images/bracketLineTop.png").toUri().toURL();
                img = new Image(greenTickURL.toExternalForm());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Initialise the picture view
            ImageView picture = new ImageView(img);
            picture.setFitWidth(36);
            picture.setFitHeight(157);
            picture.setLayoutX(200);
            picture.setLayoutY(99);

            //return it
            return picture;
        }

        else if(match.equals("Upper22"))
        {

            try {
                URL greenTickURL = Paths.get("src/SFMS/Views/Images/bracketLineTop.png").toUri().toURL();
                img = new Image(greenTickURL.toExternalForm());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Initialise the picture view
            ImageView picture = new ImageView(img);
            picture.setFitWidth(36);
            picture.setFitHeight(310);
            picture.setLayoutX(200);
            picture.setLayoutY(99);

            //return it
            return picture;
        }
        else if (match.equals("Lower"))
        {

            try {
                URL greenTickURL = Paths.get("src/SFMS/Views/Images/bracketLineTop1.png").toUri().toURL();
                img = new Image(greenTickURL.toExternalForm());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Initialise the picture view
            ImageView picture = new ImageView(img);
            picture.setFitWidth(36);
            picture.setFitHeight(109);
            picture.setLayoutX(187);
            picture.setLayoutY(-4);

            //return it
            return picture;
        }

        else if(match.equals("Lower2"))
        {

            try {
                URL greenTickURL = Paths.get("src/SFMS/Views/Images/bracketLineTop1.png").toUri().toURL();
                img = new Image(greenTickURL.toExternalForm());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Initialise the picture view
            ImageView picture = new ImageView(img);
            picture.setFitWidth(36);
            picture.setFitHeight(170);
            picture.setLayoutX(200);
            picture.setLayoutY(-62);

            //return it
            return picture;
        }

        else if(match.equals("Lower22"))
        {

            try {
                URL greenTickURL = Paths.get("src/SFMS/Views/Images/bracketLineTop1.png").toUri().toURL();
                img = new Image(greenTickURL.toExternalForm());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Initialise the picture view
            ImageView picture = new ImageView(img);
            picture.setFitWidth(36);
            picture.setFitHeight(315);
            picture.setLayoutX(200);
            picture.setLayoutY(-201);

            //return it
            return picture;
        }


        else if( match.equals("Final Round"))
        {
            //Use the activity name to call the files
            try {
                URL greenTickURL = Paths.get("src/SFMS/Views/Images/bracketLine.png").toUri().toURL();
                img = new Image(greenTickURL.toExternalForm());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Initialise the picture view
            ImageView picture = new ImageView(img);
            picture.setFitWidth(32);
            picture.setFitHeight(16);
            picture.setLayoutX(-3);
            picture.setLayoutY(96);

            //return it
            return picture;
        }
        else {
            return null;
        }
    }

    //Close the window
    public void btnCloseOnAction()
    {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
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
