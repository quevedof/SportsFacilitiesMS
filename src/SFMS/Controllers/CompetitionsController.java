package SFMS.Controllers;

import SFMS.Models.DBConnection;
import SFMS.Models.League;
import SFMS.Models.Tournament;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.net.URL;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CompetitionsController {

    @FXML
    private Button btnNewTournament;

    @FXML
    private Button btnNewLeague;

    @FXML
    private ComboBox<String> cbActivity;
    @FXML
    private  ComboBox<String> cbActivity1;

    @FXML
    private GridPane gpTournamentsList;

    @FXML
    private GridPane gpLeaguesList;

    //Store the displayed tournaments
    ArrayList<Tournament> displayedTournamentsList = new ArrayList<>();

    //Store the displayed leagues
    ArrayList<League> displayedLeaguesList = new ArrayList<>();

    public void initialize()
    {
        displayTournaments("All Tournaments");
        displayLeagues("All Leagues");
        populateActivityCB();
    }

    //Opens up a window to add a new tournament
    public void btnNewTournamentOnAction()
    {
        //Open a new fxml view
        try
        {
            //Declare and instantiate a new stage to display a new window
            Stage newTournamentStage = new Stage();
            //Get the current stage to make the new stage's parent
            Stage parentStage = (Stage) btnNewTournament.getScene().getWindow();
            newTournamentStage.initOwner(parentStage);
            //Edit the modality so the user cannot do anything else until the new window is closed
            newTournamentStage.initModality(Modality.WINDOW_MODAL);
            //Remove the default OS windows designs
            newTournamentStage.initStyle(StageStyle.UNDECORATED);

            //Get the URL of the FXML document to be displayed a new scene
            URL url = Paths.get("./src/SFMS/Views/NewTournamentView.fxml").toUri().toURL();
            //Load the document
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(url);
            Parent root = loader.load();

            //Declare and initialize a new scene with the retrieved FXML file
            Scene newTournamentScene = new Scene(root);
            //Set the new scene to the new stage
            newTournamentStage.setScene(newTournamentScene);

            //Create a simple fade in transition to the new scene
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            //Show the scene and refresh the list when the window closes
            newTournamentStage.showAndWait();
            displayTournaments("All Tournaments");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    //Opens up a window to add a new league
    public void btnNewLeagueOnAction()
    {
        //Open a new fxml view
        try
        {
            //Declare and instantiate a new stage to display a new window
            Stage newLeagueStage = new Stage();
            //Get the current stage to make the new stage's parent
            Stage parentStage = (Stage) btnNewLeague.getScene().getWindow();
            newLeagueStage.initOwner(parentStage);
            //Edit the modality so the user cannot do anything else until the new window is closed
            newLeagueStage.initModality(Modality.WINDOW_MODAL);
            //Remove the default OS windows designs
            newLeagueStage.initStyle(StageStyle.UNDECORATED);

            //Get the URL of the FXML document to be displayed a new scene
            URL url = Paths.get("./src/SFMS/Views/NewLeagueView.fxml").toUri().toURL();
            //Load the document
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(url);
            Parent root = loader.load();

            //Declare and initialize a new scene with the retrieved FXML file
            Scene newLeagueScene = new Scene(root);
            //Set the new scene to the new stage
            newLeagueStage.setScene(newLeagueScene);

            //Create a simple fade in transition to the new scene
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            //Show the scene and refresh the list when the window closes
            newLeagueStage.showAndWait();
            displayLeagues("All Leagues");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    //Opens up details of the selected tournament
    public void btnViewOnAction(ActionEvent event)
    {

        //Get the button clicked to show the correct tournament
        Button sourceButton = (Button) event.getSource();
        Tournament tournament = retrieveRequiredTournament(sourceButton);

        //Open a new fxml view
        try
        {
            //Declare and instantiate a new stage to display a new window
            Stage viewTournamentStage = new Stage();
            //Get the current stage to make the new stage's parent
            Stage parentStage = (Stage) btnNewTournament.getScene().getWindow();
            viewTournamentStage.initOwner(parentStage);
            //Edit the modality so the user cannot do anything else until the new window is closed
            viewTournamentStage.initModality(Modality.WINDOW_MODAL);
            //Remove the default OS windows designs
            viewTournamentStage.initStyle(StageStyle.UNDECORATED);

            //Get the URL of the FXML document to be displayed a new scene
            URL url = Paths.get("./src/SFMS/Views/ViewTournamentView.fxml").toUri().toURL();
            //Load the document
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(url);
            Parent root = loader.load();

            //Get the controller of FMXL file to pass some data to be displayed in the new stage
            ViewTournamentController controller = loader.getController();
            //Pass required data
            controller.setTournamentSelected(tournament);

            //Declare and initialize a new scene with the retrieved FXML file
            Scene viewTournamentScene = new Scene(root);
            //Set the new scene to the new stage
            viewTournamentStage.setScene(viewTournamentScene);

            //Create a simple fade in transition to the new scene
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            //Show the scene and refresh the list when the window closes
            viewTournamentStage.showAndWait();
            displayTournaments("All Tournaments");


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    //Opens up details of the selected league
    public void btnLeagueViewOnAction(ActionEvent event)
    {

        //Get the button clicked to show the correct tournament
        Button sourceButton = (Button) event.getSource();
        League league = retrieveRequiredLeague(sourceButton);

        //Open a new fxml view
        try
        {
            //Declare and instantiate a new stage to display a new window
            Stage viewLeagueStage = new Stage();
            //Get the current stage to make the new stage's parent
            Stage parentStage = (Stage) btnNewLeague.getScene().getWindow();
            viewLeagueStage.initOwner(parentStage);
            //Edit the modality so the user cannot do anything else until the new window is closed
            viewLeagueStage.initModality(Modality.WINDOW_MODAL);
            //Remove the default OS windows designs
            viewLeagueStage.initStyle(StageStyle.UNDECORATED);

            //Get the URL of the FXML document to be displayed a new scene
            URL url = Paths.get("./src/SFMS/Views/ViewLeagueView.fxml").toUri().toURL();
            //Load the document
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(url);
            Parent root = loader.load();

            //Get the controller of FMXL file to pass some data to be displayed in the new stage
            ViewLeagueController controller = loader.getController();
            //Pass required data
            controller.setLeagueSelected(league);

            //Declare and initialize a new scene with the retrieved FXML file
            Scene viewLeagueScene = new Scene(root);
            //Set the new scene to the new stage
            viewLeagueStage.setScene(viewLeagueScene);

            //Create a simple fade in transition to the new scene
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            //Show the scene and refresh the list when the window closes
            viewLeagueStage.showAndWait();
            displayLeagues("All Leagues");


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }


    //Combo box that allows a filtered search
    public void cbActivityOnAction()
    {
        displayTournaments(cbActivity.getValue());
    }

    //Combo box that allows a filtered search
    public void cbActivity1OnAction()
    {
        displayLeagues(cbActivity1.getValue());
    }

    //Display saved tournaments
    public void displayTournaments(String filter)
    {

        if (displayedTournamentsList.size() > 0)
        {
            displayedTournamentsList.clear();
        }

        //Ensure only two rows are present at the beginning of the facilities addition
        //The firs row is the titles, no need to delete, the second one is the row to add a tournament
        while(gpTournamentsList.getRowConstraints().size() > 1)
        {
            gpTournamentsList.getRowConstraints().remove(1);
        }

        //Remove any tournament being displayed on the first row
        gpTournamentsList.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null);

        //Get the tournaments list
        DBConnection db = new DBConnection();
        ArrayList<Tournament>  tournamentsList = db.getTournaments(filter);
        db.close();
        //Start the displaying from the second row in the grid pane
        int row = 1;
        for (Tournament tour : tournamentsList)
        {
            //Add a new row to display the tournament
            gpTournamentsList.getRowConstraints().add(new RowConstraints(81, 81, 81));

            //Format the dates to a user friendly format
            String startDate = tour.getDateStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String endDate = tour.getDateEnd().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            //design the tournament row and add it to the grid pane
            createTournamentRow(tour.getName(), tour.getActivity(), startDate, endDate, tour.getAge(), row);

            row+=1;
            //Add to the arraylist to have it the same order as the displayed ones
            displayedTournamentsList.add(tour);

        }
    }

    //Get the required tournament from the arraylist
    public Tournament retrieveRequiredTournament(Button button)
    {
        //Get the row index of the button to get the correct tournament
        Integer rowNum = GridPane.getRowIndex(button.getParent());
        return displayedTournamentsList.get(rowNum -1);

    }

    //Create the tournament rows that are added to the grip pane
    public void createTournamentRow(String name, String activity, String dateStart, String dateEnd, String age, int row)
    {
        //Anchor panes for the cells
        String paneStyle = "-fx-border-width: 3px 0px 3px 0px; -fx-border-color: #006400;";
        AnchorPane namePane = new AnchorPane();
        namePane.setStyle(paneStyle);
        AnchorPane activityPane = new AnchorPane();
        activityPane.setStyle(paneStyle);
        AnchorPane datesPane = new AnchorPane();
        datesPane.setStyle(paneStyle);
        AnchorPane agePane = new AnchorPane();
        agePane.setStyle(paneStyle);
        AnchorPane viewPane = new AnchorPane();
        viewPane.setStyle(paneStyle);

        //Labels to be displayed and added to their corresponding anchor panes
        //Style for the labels
        String style = "-fx-font-size: 14px; -fx-font-weight: bold;";
        Label lblName = new Label();
        lblName.setText(name);
        lblName.setStyle(style);
        lblName.setLayoutX(14);
        lblName.setLayoutY(30);
        lblName.setMaxWidth(80);
        namePane.getChildren().add(lblName);

        Label lblActivity = new Label();
        lblActivity.setText(activity);
        lblActivity.setStyle(style);
        lblActivity.setLayoutX(14);
        lblActivity.setLayoutY(30);
        activityPane.getChildren().add(lblActivity);

        Label lblDates = new Label();
        lblDates.setText(dateStart + " - " + dateEnd);
        lblDates.setStyle(style);
        lblDates.setLayoutX(4);
        lblDates.setLayoutY(30);
        datesPane.getChildren().add(lblDates);

        Label lblAge = new Label();
        lblAge.setText(age);
        lblAge.setStyle(style);
        lblAge.setLayoutX(11);
        lblAge.setLayoutY(30);
        agePane.getChildren().add(lblAge);

        Button btnView = new Button();
        btnView.setText("View");
        btnView.setLayoutX(15);
        btnView.setLayoutY(30);
        //Adjust a bit more of style details
        btnView.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; ");

        //Add the button to a CSS class to customise it
        try {
            URL url = Paths.get("./src/SFMS/Views/Style.css").toUri().toURL();
            btnView.getStylesheets().add(url.toExternalForm());
            btnView.getStyleClass().add("Save");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //::Method reference
        btnView.setOnAction(this::btnViewOnAction);
        viewPane.getChildren().add(btnView);

        //Add the panes to their corresponding cells
        gpTournamentsList.add(namePane, 0, row);
        gpTournamentsList.add(activityPane, 1, row);
        gpTournamentsList.add(datesPane, 2, row);
        gpTournamentsList.add(agePane, 3, row);
        gpTournamentsList.add(viewPane, 4, row);

    }

    //Display saved leagues
    public void displayLeagues(String filter)
    {

        if (displayedLeaguesList.size() > 0)
        {
            displayedLeaguesList.clear();
        }

        //Ensure only two rows are present at the beginning of the facilities addition
        //The firs row is the titles, no need to delete, the second one is the row to add a league
        while(gpLeaguesList.getRowConstraints().size() > 1)
        {
            gpLeaguesList.getRowConstraints().remove(1);
        }

        //Remove any league being displayed on the first row
        gpLeaguesList.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null);

        //Get the leagues list
        DBConnection db = new DBConnection();
        ArrayList<League>  leaguesList = db.getLeagues(filter);
        db.close();
        //Start the displaying from the second row in the grid pane
        int row = 1;
        for (League league : leaguesList)
        {
            //Add a new row to display the tournament
            gpLeaguesList.getRowConstraints().add(new RowConstraints(81, 81, 81));

            //Format the dates to a user friendly format
            String startDate = league.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String endDate = league.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            //design the tournament row and add it to the grid pane
            createLeagueRow(league.getName(), league.getActivity(), startDate, endDate, league.getAge(), row);

            row+=1;
            //Add to the arraylist to have it the same order as the displayed ones
            displayedLeaguesList.add(league);

        }
    }

    //Create the league row that are added to the grip pane
    public void createLeagueRow(String name, String activity, String dateStart, String dateEnd, String age, int row)
    {
        //Anchor panes for the cells
        String paneStyle = "-fx-border-width: 3px 0px 3px 0px; -fx-border-color:  #000080;";
        AnchorPane namePane = new AnchorPane();
        namePane.setStyle(paneStyle);
        AnchorPane activityPane = new AnchorPane();
        activityPane.setStyle(paneStyle);
        AnchorPane datesPane = new AnchorPane();
        datesPane.setStyle(paneStyle);
        AnchorPane agePane = new AnchorPane();
        agePane.setStyle(paneStyle);
        AnchorPane viewPane = new AnchorPane();
        viewPane.setStyle(paneStyle);

        //Labels to be displayed and added to their corresponding anchor panes
        //Style for the labels
        String style = "-fx-font-size: 14px; -fx-font-weight: bold;";
        Label lblName = new Label();
        lblName.setText(name);
        lblName.setStyle(style);
        lblName.setLayoutX(14);
        lblName.setLayoutY(30);
        lblName.setMaxWidth(80);
        namePane.getChildren().add(lblName);

        Label lblActivity = new Label();
        lblActivity.setText(activity);
        lblActivity.setStyle(style);
        lblActivity.setLayoutX(14);
        lblActivity.setLayoutY(30);
        activityPane.getChildren().add(lblActivity);

        Label lblDates = new Label();
        lblDates.setText(dateStart + " - " + dateEnd);
        lblDates.setStyle(style);
        lblDates.setLayoutX(4);
        lblDates.setLayoutY(30);
        datesPane.getChildren().add(lblDates);

        Label lblAge = new Label();
        lblAge.setText(age);
        lblAge.setStyle(style);
        lblAge.setLayoutX(11);
        lblAge.setLayoutY(30);
        agePane.getChildren().add(lblAge);

        Button btnView = new Button();
        btnView.setText("View");
        btnView.setLayoutX(15);
        btnView.setLayoutY(30);
        //Adjust a bit more of style details
        btnView.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; ");

        //Add the button to a CSS class to customise it
        try {
            URL url = Paths.get("./src/SFMS/Views/Style.css").toUri().toURL();
            btnView.getStylesheets().add(url.toExternalForm());
            btnView.getStyleClass().add("Save");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //::Method reference
        btnView.setOnAction(this::btnLeagueViewOnAction);
        viewPane.getChildren().add(btnView);

        //Add the panes to their corresponding cells
        gpLeaguesList.add(namePane, 0, row);
        gpLeaguesList.add(activityPane, 1, row);
        gpLeaguesList.add(datesPane, 2, row);
        gpLeaguesList.add(agePane, 3, row);
        gpLeaguesList.add(viewPane, 4, row);

    }

    //Get the required league from the arraylist
    public League retrieveRequiredLeague(Button button)
    {
        //Get the row index of the button to get the correct league
        Integer rowNum = GridPane.getRowIndex(button.getParent());
        return displayedLeaguesList.get(rowNum -1);

    }

    //Populate the filter box
    public void populateActivityCB()
    {
        cbActivity.getItems().addAll("Football", "Tennis", "Basketball", "Badminton", "All Tournaments");
        cbActivity1.getItems().addAll("Football", "Tennis", "Basketball", "Badminton", "All Leagues");

    }

}


