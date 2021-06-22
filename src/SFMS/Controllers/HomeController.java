package SFMS.Controllers;

import SFMS.Models.Booking;
import SFMS.Models.DBConnection;
import SFMS.Models.Facility;
import SFMS.Models.Tournament;
import com.mongodb.client.MongoCollection;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
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
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.net.URL;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class HomeController {

    @FXML
    private GridPane gpFacilitiesList;

    @FXML
    private GridPane gpBookingsList;

    @FXML
    private Label lblDay;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblTotalBookingsNum;

    @FXML
    private Label lblCheckedInNum;

    @FXML
    private Label lblNotCheckedInNum;

    @FXML
    private Label lblWeekDates;

    @FXML
    private GridPane gpTournamentsList;

    //The bookings being displayed
    ArrayList<Booking> bookingsArrayList = new ArrayList<>();

    //The facilities being displayed
    ArrayList<Facility> facilitiesArrayList = new ArrayList<>();


    public void initialize()
    {
        //database connection
        DBConnection db = new DBConnection();
        displayCurrentDate();
        displayBookingsList(db);
        displayFacilities(db);
        displayTournaments(db);
        db.close();
    }

    //Display current date
    public void displayCurrentDate()
    {
        //get the current date
        LocalDate currentDate = LocalDate.now();
        //get and display the day string
        lblDay.setText(currentDate.getDayOfWeek().toString());
        //get and display the date
        lblDate.setText(currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    //Displays all bookings for the current date
    public void displayBookingsList(DBConnection db)
    {
        //Store the number of checked in bookings
        int checkedInNum = 0;
        //Clear the list
        if (bookingsArrayList.size() > 0)
        {
            bookingsArrayList.clear();
        }

        //Ensure only two rows are present at the beginning of the bookings addition
        //The firs row is the titles, no need to delete, the second one is the row to add a tournament
        while(gpBookingsList.getRowConstraints().size() > 1)
        {
            gpBookingsList.getRowConstraints().remove(1);
        }

        //Remove any booking being displayed on the first row
        gpBookingsList.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null);

        //Get the bookings list
        bookingsArrayList = db.getDateBookings(LocalDate.now());
        //Start the displaying from the second row in the grid pane
        int row = 1;
        for (Booking booking : bookingsArrayList)
        {
            //Add a new row to display the booking
            gpBookingsList.getRowConstraints().add(new RowConstraints(40, 40, 40));

            //Get one of the contact options, prioritise the phone number
            String contact = "";
            if(!booking.getBookContactNum().equals(""))
            {
                contact = booking.getBookContactNum();
            }
            else
            {
                contact = booking.getBookContactEmail();
            }


            //design the booking row and add it to the grid pane
            createBookingRow(booking, contact, row);
            row+=1;

            if (booking.isCheckedIn())
            {
                checkedInNum +=1;
            }

        }

        //Display the numbers
        lblTotalBookingsNum.setText(String.valueOf(bookingsArrayList.size()));
        lblCheckedInNum.setText((String.valueOf(checkedInNum)));
        lblNotCheckedInNum.setText(String.valueOf(bookingsArrayList.size() - checkedInNum));

    }

    //Display facilities fullness
    public void displayFacilities(DBConnection db)
    {
        //dates of the beginning and end of the current week
        LocalDate startDate =  LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endDate =  LocalDate.now().with(DayOfWeek.SUNDAY);
        lblWeekDates.setText("(" + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " +
                endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");

        //Retrieve the required week's bookings
        //ArrayList<Booking> bookingsList= db.getBookings( lblFacName.getText(), mondayDate, sundayDate);


        //Clear the list
        if (facilitiesArrayList.size() > 0)
        {
            facilitiesArrayList.clear();
        }

        //Ensure only two rows are present at the beginning of the facilities addition
        //The firs row is the titles, no need to delete, the second one is the row to add a tournament
        while(gpFacilitiesList.getRowConstraints().size() > 1)
        {
            gpFacilitiesList.getRowConstraints().remove(1);
        }

        //Remove any facilities being displayed on the first row
        gpFacilitiesList.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null);

        //Get the facilities list
        facilitiesArrayList = db.getFacilities("All Facilities");

        //Start the displaying from the second row in the grid pane
        int row = 1;
        for (Facility facility : facilitiesArrayList)
        {
            //Add a new row to display the booking
            gpFacilitiesList.getRowConstraints().add(new RowConstraints(55, 55, 55));


            double progressNum = getProgressNum(facility.getFacName(), startDate, endDate );


            //design the booking row and add it to the grid pane
            createFacilityRow(facility.getFacName(), progressNum,  row);
            row+=1;

        }
    }

    //Display tournaments list
    public void displayTournaments(DBConnection db)
    {


        //Ensure only two rows are present at the beginning of the bookings addition
        //The firs row is the titles, no need to delete, the second one is the row to add a tournament
        while(gpTournamentsList.getRowConstraints().size() > 1)
        {
            gpTournamentsList.getRowConstraints().remove(1);
        }

        //Remove any tournament being displayed on the first row
        gpTournamentsList.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null);

        //Get the tournaments list
        ArrayList<Tournament> tournamentArrayList= db.getTournaments("All Tournaments");

        //Start the displaying from the second row in the grid pane
        int row = 1;
        for (Tournament tournament : tournamentArrayList)
        {
            //Add a new row to display the tournament
            gpTournamentsList.getRowConstraints().add(new RowConstraints(40, 40, 40));


            //design the tournament row and add it to the grid pane
            //add only if the current date has an ongoing tournament
            if (LocalDate.now().isAfter(tournament.getDateStart()) && LocalDate.now().isBefore(tournament.getDateEnd()) ||
                    LocalDate.now().isEqual(tournament.getDateStart()) || LocalDate.now().isEqual(tournament.getDateEnd()) ) {
                //get the dates
                String dates = tournament.getDateStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " +
                        tournament.getDateEnd().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                //add row
                createTournamentRow(tournament.getName(), tournament.getActivity(), dates, tournament.getNoTeams(), row);
                row += 1;
            }
            //Add to the arraylist to have it the same order as the displayed ones
            // displayedTournamentsList.add(tour);
        }


    }

    //Create the tournaments rows that are to bea dded to the tournament grid pane
    public void createTournamentRow(String name, String activity, String dates, int noTeams, int row)
    {
        //Anchor panes for the cells
        String paneStyle = "-fx-border-width: 3px 0px 3px 0px; -fx-border-color: #000000;";
        AnchorPane namePane = new AnchorPane();
        namePane.setStyle(paneStyle);
        AnchorPane activityPane = new AnchorPane();
        activityPane.setStyle(paneStyle);
        AnchorPane datesPane = new AnchorPane();
        datesPane.setStyle(paneStyle);
        AnchorPane numTeamsPane = new AnchorPane();
        numTeamsPane.setStyle(paneStyle);

        //Name Label
        String style = "-fx-font-size: 14px; -fx-font-weight: bold;";
        Label lblName = new Label(name);
        lblName.setStyle(style);
        lblName.setLayoutX(6);
        lblName.setLayoutY(12);
        lblName.setMaxWidth(94);
        namePane.getChildren().add(lblName);

        //activity label
        Label lblActivity = new Label(activity);
        lblActivity.setStyle(style);
        lblActivity.setLayoutX(6);
        lblActivity.setLayoutY(12);
        lblActivity.maxWidth(94);
        activityPane.getChildren().add(lblActivity);

        //dates label
        Label lblDates = new Label(dates);
        lblDates.setStyle(style);
        lblDates.setLayoutX(20);
        lblDates.setLayoutY(12);
        lblDates.maxWidth(161);
        datesPane.getChildren().add(lblDates);

        //Number of teams label
        Label lblNumTeams = new Label(String.valueOf(noTeams));
        lblNumTeams.setStyle(style);
        lblNumTeams.setLayoutX(45);
        lblNumTeams.setLayoutY(12);
        numTeamsPane.getChildren().add(lblNumTeams);

        //Add the panes to their corresponding cells
        gpTournamentsList.add(namePane, 0, row);
        gpTournamentsList.add(activityPane, 1, row);
        gpTournamentsList.add(datesPane, 2, row);
        gpTournamentsList.add(numTeamsPane, 3, row);
    }

    //Create the facilities rows that are to be added to the facilities grid pane
    public void createFacilityRow(String name, double progressNum, int row)
    {
        //Anchor panes for the cells
        String paneStyle = "-fx-border-width: 3px 0px 3px 0px; -fx-border-color: #000080;";
        AnchorPane namePane = new AnchorPane();
        namePane.setStyle(paneStyle);
        AnchorPane fullnessPane = new AnchorPane();
        fullnessPane.setStyle(paneStyle);

        //Name Label
        String style = "-fx-font-size: 16px; -fx-font-weight: bold;";
        Label lblName = new Label();
        lblName.setText(name);
        lblName.setStyle(style);
        lblName.setLayoutX(58);
        lblName.setLayoutY(15);
        lblName.setMaxWidth(171);
        namePane.getChildren().add(lblName);

        //Fullness pane
        //progress bar
        ProgressBar fullnessBar = new ProgressBar(progressNum);
       fullnessBar.setStyle("-fx-accent: red;");
        fullnessBar.setLayoutX(20);
        fullnessBar.setLayoutY(14);
        fullnessBar.setPrefWidth(246);
        fullnessBar.setPrefHeight(28);

        //progress label
        Label lblProgress = new Label();
        lblProgress.setStyle(style);
        lblProgress.setText((int)(progressNum * 100) + "%");
        lblProgress.setLayoutX(296);
        lblProgress.setLayoutY(14);

        fullnessPane.getChildren().addAll(fullnessBar, lblProgress);

        //Add the panes to their corresponding cells
        gpFacilitiesList.add(namePane, 0, row);
        gpFacilitiesList.add(fullnessPane, 1, row);

    }

    //Create the bookings rows that are to be added to the bookings grid pane
    public void createBookingRow(Booking booking, String contact, int row)
    {
        //Anchor panes for the cells
        String paneStyle = "-fx-border-width: 3px 0px 3px 0px; -fx-border-color: #006400";
        AnchorPane namePane = new AnchorPane();
        namePane.setStyle(paneStyle);
        AnchorPane facilityPane = new AnchorPane();
        facilityPane.setStyle(paneStyle);
        AnchorPane contactPane = new AnchorPane();
        contactPane.setStyle(paneStyle);
        AnchorPane timePane = new AnchorPane();
        timePane.setStyle(paneStyle);
        AnchorPane viewPane = new AnchorPane();
        viewPane.setStyle(paneStyle);
        AnchorPane checkInPane = new AnchorPane();
        checkInPane.setStyle(paneStyle);

        //Labels to be displayed and added to their corresponding anchor panes
        //Style for the labels
        //Name Label
        String style = "-fx-font-size: 16px; -fx-font-weight: bold;";
        Label lblName = new Label(booking.getBookClientName());
        lblName.setStyle(style);
        lblName.setLayoutX(17);
        lblName.setLayoutY(7);
        lblName.setMaxWidth(158);
        namePane.getChildren().add(lblName);

        //Facility Label
        Label lblFacility = new Label(booking.getBookFacility());
        lblFacility.setStyle(style);
        lblFacility.setLayoutX(17);
        lblFacility.setLayoutY(7);
        lblFacility.setMaxWidth(162);
        facilityPane.getChildren().add(lblFacility);

        //Contact Label
        Label lblContact = new Label(contact);
        lblContact.setStyle(style);
        lblContact.setLayoutX(20);
        lblContact.setLayoutY(7);
        lblContact.setMaxWidth(170);
        contactPane.getChildren().add(lblContact);

        //Time Label
        Label lblTime = new Label(booking.getBookTime());
        lblTime.setStyle(style);
        lblTime.setLayoutX(27);
        lblTime.setLayoutY(7);
        timePane.getChildren().add(lblTime);

        //View Label
        Label lblView = new Label("View");
        lblView.setStyle(style);
        lblView.setLayoutX(17);
        lblView.setLayoutY(7);
        lblView.setUnderline(true);
        lblViewOnMouseClicked(lblView, booking);
        //lblView.setOnMouseClicked( event -> { lblViewOnMouseClicked(lblView, booking);});
        viewPane.getChildren().add(lblView);

        //Check In Label
        Label lblCheckIn = new Label("Checked In");
        lblCheckIn.setStyle(style);
        lblCheckIn.setLayoutX(8);
        lblCheckIn.setLayoutY(7);

        //If the booking is not checked in, allow the user to click on the label and check it in
        if(!booking.isCheckedIn())
        {
            lblCheckIn.setText("Check In");
            lblCheckIn.setUnderline(true);
            //Underline the text and change the cursor type when user hovers over the label
            lblCheckIn.setOnMouseEntered( event -> {
                lblCheckIn.setCursor(Cursor.HAND);
            });
            lblCheckIn.setOnMouseExited( event -> {
                lblCheckIn.setCursor(Cursor.DEFAULT);
            });
            //On action
            lblCheckIn.setOnMouseClicked( event -> { lblCheckInOnMouseClicked(booking);}); ;
        }

        checkInPane.getChildren().add(lblCheckIn);

        //Add the panes to their corresponding cells
        gpBookingsList.add(namePane, 0, row);
        gpBookingsList.add(facilityPane, 1, row);
        gpBookingsList.add(contactPane, 2, row);
        gpBookingsList.add(timePane, 3, row);
        gpBookingsList.add(viewPane, 4, row);
        gpBookingsList.add(checkInPane, 5, row);
    }

    //On click action for the view label
    public void  lblViewOnMouseClicked(Label lblView, Booking booking)
    {
        //Underline the text and change the cursor type when user hovers over the label
        lblView.setOnMouseEntered( event -> {
            lblView.setCursor(Cursor.HAND);
        });
        lblView.setOnMouseExited( event -> {
            lblView.setCursor(Cursor.DEFAULT);
        });

        //Open the booking view
        lblView.setOnMouseClicked( event -> {
            try
            {
                //Declare and instantiate a new stage to display a new window
                Stage viewBookingStage = new Stage();
                //Get the current stage to make the new stage's parent
                Stage parentStage = (Stage) lblView.getScene().getWindow();
                viewBookingStage.initOwner(parentStage);
                //Edit the modality so the user cannot do anything else until the new window is closed
                viewBookingStage.initModality(Modality.WINDOW_MODAL);
                //Remove the default OS windows designs
                viewBookingStage.initStyle(StageStyle.UNDECORATED);

                //Get the URL of the FXML document to be displayed a new scene
                URL url = Paths.get("./src/SFMS/Views/ViewBookingView.fxml").toUri().toURL();
                //Load the document
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(url);
                Parent root = loader.load();

                //Get the controller of FMXL file to pass some data to be displayed in the new stage
                ViewBookingController controller = loader.getController();
                //Pass required data
                controller.setData(booking);


                //Declare and initialize a new scene with the retrieved FXML file
                Scene viewBookingScene = new Scene(root);
                //Set the new scene to the new stage
                viewBookingStage.setScene(viewBookingScene);
                //Create a simple fade in transition to the new scene
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
                //Show the scene and refresh
                viewBookingStage.showAndWait();
                DBConnection db = new DBConnection();
                displayBookingsList(db);
                db.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        });
    }

    //On click action for the Check in label
    public void lblCheckInOnMouseClicked(Booking booking)
    {
        //Entry Confirmation dialog, asks the user for a check in confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Check In Confirmation");
        confirmAlert.setHeaderText("Are you sure you want proceed with the booking check in?");
        //Customise the small dialog
        customiseDialog(confirmAlert.getDialogPane());
        //Edit the "Ok" button of the dialog to display "Yes" instead
        Button yesButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
        yesButton.setText("Yes");

        confirmAlert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = confirmAlert.showAndWait();

        //Proceed if the user confirms the check in
        if (result.get() == ButtonType.OK) {

            try {
                //Open database connection
                DBConnection db = new DBConnection();
                //Retrieve the bookings collection to edit the booking
                MongoCollection<Document> col = db.collectionRetrieval("Bookings");
                //Update the booking
                col.updateOne(eq("_id", booking.getBookId()), set("CheckedIn", true));
                //Close the database connection
                db.close();

                //Let the user know the booking has been checked in
                Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                savedAlert.setTitle("Checked In");
                savedAlert.setHeaderText("The booking has successfully been Checked In");
                customiseDialog(savedAlert.getDialogPane());
                savedAlert.setContentText(null);
                savedAlert.showAndWait();

                DBConnection db1 =new DBConnection();
                displayBookingsList(db1);
                db1.close();

            }
            //If for some reason, the details cannot be saved, let the user know
            catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("There was an unknown error when attempting check in the selected booking");
                errorAlert.setContentText(null);
                customiseDialog(errorAlert.getDialogPane());
                errorAlert.showAndWait();
            }
        }
    }

    //Gets the percentage of fullness of a facility for a week
    public Double getProgressNum(String facName, LocalDate startDate, LocalDate endDate )
    {
        //get all bookings for a facility in the current week
        DBConnection db = new DBConnection();
        ArrayList<Booking> bookingsArrayList = db.getBookings(facName, startDate, endDate);
        db.close();
        //maximum number of 30min durations in a week
        int maxWeeklyDuration = 168;
        //total number of 30min duration in the given week
        int totalDuration = 0;
        //Add each duration from each booking
        for (Booking booking: bookingsArrayList)
        {
            switch (booking.getBookDuration())
            {
                case "30 min.":
                    totalDuration += 1;
                    break;
                case "60 min.":
                    totalDuration += 2;
                    break;
                case "90 min.":
                    totalDuration += 3;
                    break;
                case "120 min.":
                    totalDuration += 4;
                    break;
            }
        }

        //Get the duration fraction
        Double result = (double)totalDuration/maxWeeklyDuration;
        //Format
        DecimalFormat df = new DecimalFormat("#.##");
        result = Double.valueOf(df.format(result));
        //return duration fraction
        return  result;

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
