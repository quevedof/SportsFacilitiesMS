package SFMS.Controllers;

import SFMS.Models.Booking;
import SFMS.Models.DBConnection;
import com.mongodb.client.MongoCollection;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class BookingsController {

    @FXML
    private GridPane gpCalendar;

    @FXML
    private Label lblFacName;
    @FXML
    private Button btnClose;

    @FXML
    private Label lblMonday;
    @FXML
    private Label lblTuesday;
    @FXML
    private Label lblWednesday;
    @FXML
    private Label lblThursday;
    @FXML
    private Label lblFriday;
    @FXML
    private Label lblSaturday;
    @FXML
    private Label lblSunday;

    @FXML
    private DatePicker dpBookings;

    //Variables to store data needed when booking a slot
    private String selectedFacName;
    private String selectedDate;
    private LocalDate selectedDateDisplay;
    private int selectedBlock;
    private int minPeople;
    private int maxPeople;
    private double price;
    private boolean[] timesAvailableArray = new boolean[5];
    private VBox viewBox;
    private VBox nextTimeBlock;

    //Timer that allows the executing of refreshing the calendar evert certain time
    private final Timer t = new Timer();

    //Local date variables to store the dates of the selected week
    LocalDate mondayDate;
    LocalDate tuesdayDate;
    LocalDate wednesdayDate;
    LocalDate thursdayDate;
    LocalDate fridayDate;
    LocalDate saturdayDate;
    LocalDate sundayDate;

    public void initialize()
    {
        //Display the current week's bookings
        dpBookings.setValue(LocalDate.now());

        //Display all bookings in the calendar
        refreshCalendar();


        //Schedule a new thread that will be updating the calendar every 5 secs
        //Gives close to real time updating to prevent double booking
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                //Make sure the thread uses the JAVAFX application thread because the method updates the UI
                Platform.runLater((new Runnable() {
                    @Override
                    public void run() {
                        refreshCalendar();
                    }
                }));
            }
        }, 0, 5000);

    }

    //Gets the facility name from previous window (facilities window)
    public void setFacilityName(String facName)
    {
        lblFacName.setText(facName);
    }

    //Every times the user selects a new date, refresh the calendar
    public void dpBookingsOnAction()
    {
        refreshCalendar();
    }

    //Opens a new window to be able to add a new booking to the selected slot
    public void btnAddBookingOnAction(ActionEvent event)
    {

        Button sourceButton = (Button) event.getSource();
        retrieveRequiredData(sourceButton);

        try
        {
            //Declare and instantiate a new stage to display a new window
            Stage addBookingStage = new Stage();
            //Get the current stage to make the new stage's parent
            Stage parentStage = (Stage) btnClose.getScene().getWindow();
            addBookingStage.initOwner(parentStage);
            //Edit the modality so the user cannot do anything else until the new window is closed
            addBookingStage.initModality(Modality.WINDOW_MODAL);
            //Remove the default OS windows designs
            addBookingStage.initStyle(StageStyle.UNDECORATED);

            //Get the URL of the FXML document to be displayed a new scene
            URL url = Paths.get("./src/SFMS/Views/AddBookingView.fxml").toUri().toURL();
            //Load the document
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(url);
            Parent root = loader.load();

            //Get the controller of FMXL file to pass some data to be displayed in the new stage
            AddBookingController controller = loader.getController();
            //Pass required data
            controller.setInitialData(selectedFacName, selectedDate, selectedDateDisplay, selectedBlock, timesAvailableArray, viewBox, nextTimeBlock, price, minPeople, maxPeople);


            //Declare and initialize a new scene with the retrieved FXML file
            Scene addBookingScene = new Scene(root);
            //Set the new scene to the new stage
            addBookingStage.setScene(addBookingScene);
            //Create a simple fade in transition to the new scene
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            //Show the scene and refresh the calendar
            addBookingStage.showAndWait();
            refreshCalendar();


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Refreshes each cell of the calendar
    public void refreshCalendar()
    {
        //get date selected
        LocalDate daySelected = dpBookings.getValue();
        //Get the dates of the selected week
        mondayDate = daySelected.with(DayOfWeek.MONDAY);
        tuesdayDate = daySelected.with(DayOfWeek.TUESDAY);
        wednesdayDate = daySelected.with(DayOfWeek.WEDNESDAY);
        thursdayDate = daySelected.with(DayOfWeek.THURSDAY);
        fridayDate = daySelected.with(DayOfWeek.FRIDAY);
        saturdayDate = daySelected.with(DayOfWeek.SATURDAY);
        sundayDate = daySelected.with(DayOfWeek.SUNDAY);


        //Display the dates of the selected day's week on the calendar
        lblMonday.setText( "Monday:\n" + mondayDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblTuesday.setText( "Tuesday:\n" +tuesdayDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblWednesday.setText( "Wednesday:\n" + wednesdayDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblThursday.setText( "Thursday:\n" + thursdayDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblFriday.setText( "Friday:\n" + fridayDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblSaturday.setText( "Saturday:\n" + saturdayDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblSunday.setText( "Sunday:\n" + sundayDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        //hide add booking buttons previous to the current date
        hideAddButtons();
        hideButtonsTime();
        //Open database connection
        DBConnection db = new DBConnection();
        //Retrieve the required week's bookings
        ArrayList<Booking> bookingsList= db.getBookings( lblFacName.getText(), mondayDate, sundayDate);
        //Close database connection
        db.close();

        //Clear all cells
        clearCells();

        //Add each booking to the corresponding cell in the calendar
        for (Booking booking: bookingsList)
        {
            //Get the row and column the booking should be displayed on
            int row = getTimeBlock(booking.getBookTime());
            int column = getBookingColumn(booking.getBookDate());

            //Search for the required cell in the grid pane
            for (Node node : gpCalendar.getChildren()) {

                //Ignore the first row and column, they are not part of the cells of the calendar
                if( GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {

                    //Arrived to the required calendar cell
                    if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {

                        //Get the anchorpane of the cell to add the corresponding bookings as Labels
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            //Get the HBox containing three VBoxes
                            if (nd instanceof HBox) {
                                //Get all three vertical boxes to display: client names and view labels and tick image
                                VBox vb1 = (VBox) ((HBox) nd).getChildren().get(0);
                                VBox vb2 = (VBox) ((HBox) nd).getChildren().get(1);
                                VBox vb3 = (VBox) ((HBox) nd).getChildren().get(2);

                                //Get the next time block in case the duration of the booking goes to the next block
                                VBox nextBlockVB1 = getNextBlockVB1(row, column);
                                VBox nextBlockVB2 = getNextBlockVB2(row, column);
                                VBox nextBlockVB3 = getNextBlockVB3(row, column);

                                //Display the bookings
                                displayBookings(booking, vb1, vb2, vb3, nextBlockVB1, nextBlockVB2, nextBlockVB3);
                            }
                        }
                    }
                    //Disable add booking buttons of cells that are fully booked
                    disableButtons((AnchorPane) node);

                }

            }
        }
    }

    //Clear all cells before displaying new bookings
    public void clearCells() {

        //Loop through all the cells in the calendar
        for (Node node : gpCalendar.getChildren()) {

            //Arrived to the required calendar cell
            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {

                //Get the anchorpane of the cell
                for (Node nd : ((AnchorPane) node).getChildren()) {
                    if (nd instanceof HBox) {
                        //Get all three vertical boxes
                        VBox vb1 = (VBox) ((HBox) nd).getChildren().get(0);
                        VBox vb2 = (VBox) ((HBox) nd).getChildren().get(1);
                        VBox vb3 = (VBox) ((HBox) nd).getChildren().get(2);

                        //Clear the vboxes
                        if (vb1.getChildren().size() > 0) {
                            vb1.getChildren().clear();
                            vb2.getChildren().clear();
                            vb3.getChildren().clear();
                        }
                    }

                    //Reset all buttons
                    if (nd instanceof Button)
                    {
                        nd.setDisable(false);
                    }
                }
            }
        }
    }

    //Method that displays the bookings as labels in the calendar
    public void displayBookings(Booking booking, VBox vb1, VBox vb2, VBox vb3, VBox nextBlockVB1, VBox nextBlockVB2, VBox nextBlockVB3)
    {

        //The 4 labels in the cell
        Label lbl1 = new Label();
        Label lbl2 = new Label();
        Label lbl3 = new Label();
        Label lbl4 = new Label();

        //4 labels to view/remove the booking
        Label lblView1 = new Label();
        Label lblView2 = new Label();
        Label lblView3 = new Label();
        Label lblView4 = new Label();

        //Image views for ticks that display if a booking is checked in or not
        ImageView checkIn1 = new ImageView();
        checkIn1.setFitWidth(17);
        checkIn1.setFitHeight(14);
        checkIn1.setVisible(false);
        ImageView checkIn2 = new ImageView();
        checkIn2.setFitWidth(17);
        checkIn2.setFitHeight(14);
        checkIn2.setVisible(false);
        ImageView checkIn3 = new ImageView();
        checkIn3.setFitWidth(17);
        checkIn3.setFitHeight(14);
        checkIn3.setVisible(false);
        ImageView checkIn4 = new ImageView();
        checkIn4.setFitWidth(17);
        checkIn4.setFitHeight(14);
        checkIn4.setVisible(false);

        //In case the booking's duration goes to the next block, add these check in image view
        ImageView nextBlockCheckIn1 = new ImageView();
        nextBlockCheckIn1.setFitWidth(17);
        nextBlockCheckIn1.setFitHeight(14);
        nextBlockCheckIn1.setVisible(false);
        ImageView nextBlockCheckIn2 = new ImageView();
        nextBlockCheckIn1.setFitWidth(17);
        nextBlockCheckIn1.setFitHeight(14);
        nextBlockCheckIn1.setVisible(false);
        ImageView nextBlockCheckIn3 = new ImageView();
        nextBlockCheckIn1.setFitWidth(17);
        nextBlockCheckIn1.setFitHeight(14);
        nextBlockCheckIn1.setVisible(false);

        //Add the required amount of labels and buttons needed to always have exactly 4 in teach VBox in the cell

        if (vb1.getChildren().size() == 0)
        {
            //Add labels
            vb1.getChildren().addAll(lbl1, lbl2, lbl3, lbl4);
            //Add the view labels
            vb2.getChildren().addAll(lblView1, lblView2, lblView3, lblView4);
            //Add the check in images
            vb3.getChildren().addAll(checkIn1, checkIn2, checkIn3, checkIn4);
        }

        //Assign the pre-existing instances, if there are are any
        else if (vb1.getChildren().size() == 1)
        {
            lbl1 = (Label) vb1.getChildren().get(0);

            lblView1  = (Label) vb2.getChildren().get(0);

            checkIn1 = (ImageView) vb3.getChildren().get(0);

            vb1.getChildren().addAll(lbl2, lbl3, lbl4);
            vb2.getChildren().addAll(lblView2, lblView3, lblView4);
            vb3.getChildren().addAll(checkIn2, checkIn3, checkIn4);
        }

        else if (vb1.getChildren().size() == 2)
        {
            lbl1 = (Label) vb1.getChildren().get(0);
            lbl2 = (Label) vb1.getChildren().get(1);

            lblView1  = (Label) vb2.getChildren().get(0);
            lblView2  = (Label) vb2.getChildren().get(1);

            checkIn1 = (ImageView) vb3.getChildren().get(0);
            checkIn2 = (ImageView) vb3.getChildren().get(1);

            vb1.getChildren().addAll(lbl3, lbl4);
            vb2.getChildren().addAll(lblView3, lblView4);
            vb3.getChildren().addAll(checkIn3, checkIn4);
        }

        else if (vb1.getChildren().size() == 3)
        {
            lbl1 = (Label) vb1.getChildren().get(0);
            lbl2 = (Label) vb1.getChildren().get(1);
            lbl3 = (Label) vb1.getChildren().get(2);

            lblView1  = (Label) vb2.getChildren().get(0);
            lblView2  = (Label) vb2.getChildren().get(1);
            lblView3  = (Label) vb2.getChildren().get(2);

            checkIn1 = (ImageView) vb3.getChildren().get(0);
            checkIn2 = (ImageView) vb3.getChildren().get(1);
            checkIn3 = (ImageView) vb3.getChildren().get(2);

            vb1.getChildren().addAll(lbl4);
            vb2.getChildren().addAll(lblView4);
            vb3.getChildren().addAll(checkIn4);
        }

        else if (vb1.getChildren().size() == 4)
        {
            lbl1 = (Label) vb1.getChildren().get(0);
            lbl2 = (Label) vb1.getChildren().get(1);
            lbl3 = (Label) vb1.getChildren().get(2);
            lbl4 = (Label) vb1.getChildren().get(3);

            lblView1  = (Label) vb2.getChildren().get(0);
            lblView2  = (Label) vb2.getChildren().get(1);
            lblView3  = (Label) vb2.getChildren().get(2);
            lblView4  = (Label) vb2.getChildren().get(3);

            checkIn1 = (ImageView) vb3.getChildren().get(0);
            checkIn2 = (ImageView) vb3.getChildren().get(1);
            checkIn3 = (ImageView) vb3.getChildren().get(2);
            checkIn4 = (ImageView) vb3.getChildren().get(3);
        }

        //Display the booking in their corresponding time blocks, and displaying their duration
        //Block 1
        if (booking.getBookTime().equals("09:00") && booking.getBookDuration().equals("30 min."))
        {
            //Display a label with the client's name
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            //Display the view labels to allow the user to view the booking details
            displayViewLabel(booking, lblView1);
            //Display the check in status in an image
            displayTickImg(booking, checkIn1);
        }

        else if (booking.getBookTime().equals("09:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            //Add another label to symbolise that the booking occupies this time slot as well
            lbl2.setText("09:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("09:00") && booking.getBookDuration().equals("90 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("09:30 - " + booking.getBookClientName());
            lbl3.setText("10:00 - " + booking.getBookClientName());

        }

        else if (booking.getBookTime().equals("09:00") && booking.getBookDuration().equals("120 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("09:30 - " + booking.getBookClientName());
            lbl3.setText("10:00 - " + booking.getBookClientName());
            lbl4.setText("10:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("09:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);
        }

        else if (booking.getBookTime().equals("09:30") && booking.getBookDuration().equals("60 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("10:00 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("09:30") && booking.getBookDuration().equals("90 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("10:00 - " + booking.getBookClientName());
            lbl4.setText("10:30 - " + booking.getBookClientName());
        }

        //Duration goes to the next block time
        else if (booking.getBookTime().equals("09:30") && booking.getBookDuration().equals("120 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("10:00 - " + booking.getBookClientName());
            lbl4.setText("10:30 - " + booking.getBookClientName());

            //Add 2 labels and an imageview to the next time block because of the duration time
            nextBlockVB1.getChildren().addAll(new Label( "11:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("10:00") && booking.getBookDuration().equals("30 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);
        }

        else if (booking.getBookTime().equals("10:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("10:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("10:00") && booking.getBookDuration().equals("90 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("10:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(
                    new Label( "11:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("10:00") && booking.getBookDuration().equals("120 min.")) {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("10:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(
                    new Label("11:00 - " + booking.getBookClientName()),
                    new Label("11:30 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2);
        }

        else if (booking.getBookTime().equals("10:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);
        }

        else if (booking.getBookTime().equals("10:30") && booking.getBookDuration().equals("60 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "11:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);

        }

        else if (booking.getBookTime().equals("10:30") && booking.getBookDuration().equals("90 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "11:00 - " + booking.getBookClientName()),
                    new Label( "11:30 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2);
        }

        else if (booking.getBookTime().equals("10:30") && booking.getBookDuration().equals("120 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "11:00 - " + booking.getBookClientName()),
                    new Label( "11:30 - " + booking.getBookClientName()),
                    new Label( "12:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2, nextBlockCheckIn3);
        }


        //Block 2
        else if (booking.getBookTime().equals("11:00") && booking.getBookDuration().equals("30 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);
        }

        else if (booking.getBookTime().equals("11:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("11:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("11:00") && booking.getBookDuration().equals("90 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("11:30 - " + booking.getBookClientName());
            lbl3.setText("12:00 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("11:00") && booking.getBookDuration().equals("120 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("11:30 - " + booking.getBookClientName());
            lbl3.setText("12:00 - " + booking.getBookClientName());
            lbl4.setText("12:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("11:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);
        }

        else if (booking.getBookTime().equals("11:30") && booking.getBookDuration().equals("60 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("12:00 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("11:30") && booking.getBookDuration().equals("90 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("12:00 - " + booking.getBookClientName());
            lbl4.setText("12:30 - " + booking.getBookClientName());
        }

        //Duration goes to the next block time
        else if (booking.getBookTime().equals("11:30") && booking.getBookDuration().equals("120 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("12:00 - " + booking.getBookClientName());
            lbl4.setText("12:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(new Label( "13:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("12:00") && booking.getBookDuration().equals("30 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);
        }

        else if (booking.getBookTime().equals("12:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("12:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("12:00") && booking.getBookDuration().equals("90 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("12:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(
                    new Label( "13:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("12:00") && booking.getBookDuration().equals("120 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("12:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(
                    new Label( "13:00 - " + booking.getBookClientName()),
                    new Label( "13:30 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2);
        }

        else if (booking.getBookTime().equals("12:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);
        }

        else if (booking.getBookTime().equals("12:30") && booking.getBookDuration().equals("60 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "13:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("12:30") && booking.getBookDuration().equals("90 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "13:00 - " + booking.getBookClientName()),
                    new Label( "13:30 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2);
        }

        else if (booking.getBookTime().equals("12:30") && booking.getBookDuration().equals("120 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "13:00 - " + booking.getBookClientName()),
                    new Label( "13:30 - " + booking.getBookClientName()),
                    new Label( "14:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2, nextBlockCheckIn3);
        }


        //Block 3
        else if (booking.getBookTime().equals("13:00") && booking.getBookDuration().equals("30 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);
        }

        else if (booking.getBookTime().equals("13:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("13:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("13:00") && booking.getBookDuration().equals("90 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("13:30 - " + booking.getBookClientName());
            lbl3.setText("14:00 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("13:00") && booking.getBookDuration().equals("120 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("13:30 - " + booking.getBookClientName());
            lbl3.setText("14:00 - " + booking.getBookClientName());
            lbl4.setText("14:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("13:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);
        }

        else if (booking.getBookTime().equals("13:30") && booking.getBookDuration().equals("60 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("14:00 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("13:30") && booking.getBookDuration().equals("90 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("14:00 - " + booking.getBookClientName());
            lbl4.setText("14:30 - " + booking.getBookClientName());
        }

        //Duration goes to the next block time
        else if (booking.getBookTime().equals("13:30") && booking.getBookDuration().equals("120 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("14:00 - " + booking.getBookClientName());
            lbl4.setText("14:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(new Label( "15:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("14:00") && booking.getBookDuration().equals("30 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);
        }

        else if (booking.getBookTime().equals("14:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("14:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("14:00") && booking.getBookDuration().equals("90 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("14:30 -  " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(
                    new Label( "15:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("14:00") && booking.getBookDuration().equals("120 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("14:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(
                    new Label( "15:00 - " + booking.getBookClientName()),
                    new Label( "15:30 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2);
        }

        else if (booking.getBookTime().equals("14:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);
        }

        else if (booking.getBookTime().equals("14:30") && booking.getBookDuration().equals("60 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "15:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("14:30") && booking.getBookDuration().equals("90 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "15:00 - " + booking.getBookClientName()),
                    new Label( "15:30 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2);
        }

        else if (booking.getBookTime().equals("14:30") && booking.getBookDuration().equals("120 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "15:00 - " + booking.getBookClientName()),
                    new Label( "15:30 - " + booking.getBookClientName()),
                    new Label( "16:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2, nextBlockCheckIn3);
        }


        //Block 4
        else if (booking.getBookTime().equals("15:00") && booking.getBookDuration().equals("30 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);
        }

        else if (booking.getBookTime().equals("15:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("15:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("15:00") && booking.getBookDuration().equals("90 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("15:30 - " + booking.getBookClientName());
            lbl3.setText("16:00 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("15:00") && booking.getBookDuration().equals("120 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("15:30 - " + booking.getBookClientName());
            lbl3.setText("16:00 - " + booking.getBookClientName());
            lbl4.setText("16:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("15:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);
        }

        else if (booking.getBookTime().equals("15:30") && booking.getBookDuration().equals("60 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("16:00 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("15:30") && booking.getBookDuration().equals("90 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("16:00 - " + booking.getBookClientName());
            lbl4.setText("16:30 - " + booking.getBookClientName());
        }

        //Duration goes to the next block time
        else if (booking.getBookTime().equals("15:30") && booking.getBookDuration().equals("120 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("16:00 - " + booking.getBookClientName());
            lbl4.setText("16:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(new Label( "17:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("16:00") && booking.getBookDuration().equals("30 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);
        }

        else if (booking.getBookTime().equals("16:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("16:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("16:00") && booking.getBookDuration().equals("90 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("16:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(
                    new Label( "17:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("16:00") && booking.getBookDuration().equals("120 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("16:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(
                    new Label( "17:00 - " + booking.getBookClientName()),
                    new Label( "17:30 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2);
        }

        else if (booking.getBookTime().equals("16:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);
        }

        else if (booking.getBookTime().equals("16:30") && booking.getBookDuration().equals("60 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "17:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("16:30") && booking.getBookDuration().equals("90 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "17:00 - " + booking.getBookClientName()),
                    new Label( "17:30 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2);
        }

        else if (booking.getBookTime().equals("16:30") && booking.getBookDuration().equals("120 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "17:00 -  " + booking.getBookClientName()),
                    new Label( "17:30 -  " + booking.getBookClientName()),
                    new Label( "18:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2, nextBlockCheckIn3);
        }


        //Block 5
        else if (booking.getBookTime().equals("17:00") && booking.getBookDuration().equals("30 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);
        }

        else if (booking.getBookTime().equals("17:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("17:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("17:00") && booking.getBookDuration().equals("90 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("17:30 - " + booking.getBookClientName());
            lbl3.setText("18:00 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("17:00") && booking.getBookDuration().equals("120 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("17:30 - " + booking.getBookClientName());
            lbl3.setText("18:00 - " + booking.getBookClientName());
            lbl4.setText("18:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("17:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);
        }

        else if (booking.getBookTime().equals("17:30") && booking.getBookDuration().equals("60 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("18:00 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("17:30") && booking.getBookDuration().equals("90 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("18:00 - " + booking.getBookClientName());
            lbl4.setText("18:30 - " + booking.getBookClientName());
        }

        //Duration goes to the next block time
        else if (booking.getBookTime().equals("17:30") && booking.getBookDuration().equals("120 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("18:00 - " + booking.getBookClientName());
            lbl4.setText("18:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(new Label( "19:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("18:00") && booking.getBookDuration().equals("30 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);
        }

        else if (booking.getBookTime().equals("18:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("18:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("18:00") && booking.getBookDuration().equals("90 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("18:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(
                    new Label( "19:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("18:00") && booking.getBookDuration().equals("120 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("18:30 - " + booking.getBookClientName());

            nextBlockVB1.getChildren().addAll(
                    new Label( "19:00 - " + booking.getBookClientName()),
                    new Label( "19:30 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2);
        }

        else if (booking.getBookTime().equals("18:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);
        }

        else if (booking.getBookTime().equals("18:30") && booking.getBookDuration().equals("60 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "19:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1);
        }

        else if (booking.getBookTime().equals("18:30") && booking.getBookDuration().equals("90 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "19:00 - " + booking.getBookClientName()),
                    new Label( "19:30 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2);
        }

        else if (booking.getBookTime().equals("18:30") && booking.getBookDuration().equals("120 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);

            nextBlockVB1.getChildren().addAll(
                    new Label( "19:00 - " + booking.getBookClientName()),
                    new Label( "19:30 - " + booking.getBookClientName()),
                    new Label( "20:00 - " + booking.getBookClientName()));
            nextBlockVB2.getChildren().addAll(new Label(), new Label(), new Label());
            nextBlockVB3.getChildren().addAll(nextBlockCheckIn1, nextBlockCheckIn2, nextBlockCheckIn3);
        }


        //Block 6
        else if (booking.getBookTime().equals("19:00") && booking.getBookDuration().equals("30 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);
        }

        else if (booking.getBookTime().equals("19:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("19:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("19:00") && booking.getBookDuration().equals("90 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("19:30 - " + booking.getBookClientName());
            lbl3.setText("20:00 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("19:00") && booking.getBookDuration().equals("120 min."))
        {
            lbl1.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView1);
            displayTickImg(booking, checkIn1);

            lbl2.setText("19:30 - " + booking.getBookClientName());
            lbl3.setText("20:00 - " + booking.getBookClientName());
            lbl4.setText("20:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("19:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);
        }

        else if (booking.getBookTime().equals("19:30") && booking.getBookDuration().equals("60 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("20:00 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("19:30") && booking.getBookDuration().equals("90 min."))
        {
            lbl2.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView2);
            displayTickImg(booking, checkIn2);

            lbl3.setText("20:00 - " + booking.getBookClientName());
            lbl4.setText("20:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("20:00") && booking.getBookDuration().equals("30 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);
        }

        else if (booking.getBookTime().equals("20:00") && booking.getBookDuration().equals("60 min."))
        {
            lbl3.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView3);
            displayTickImg(booking, checkIn3);

            lbl4.setText("20:30 - " + booking.getBookClientName());
        }

        else if (booking.getBookTime().equals("20:30") && booking.getBookDuration().equals("30 min."))
        {
            lbl4.setText(booking.getBookTime() + " - " + booking.getBookClientName());
            displayViewLabel(booking, lblView4);
            displayTickImg(booking, checkIn4);
        }

        else
        {
            System.out.println("Could not find the time block for the following booking: " + booking.toString());
        }

    }

    //Display a label that allows the viewing of a booking
    public void displayViewLabel(Booking booking, Label viewLabel)
    {

        viewLabel.setText("View");
        //Underline the text and change the cursor type when user hovers over the label
        viewLabel.setOnMouseEntered( event -> {
            viewLabel.setUnderline(true);
            viewLabel.setCursor(Cursor.HAND);
        });
        viewLabel.setOnMouseExited( event -> {
            viewLabel.setUnderline(false);
            viewLabel.setCursor(Cursor.DEFAULT);
        });

        //Open the booking view if they user clicks on the label
        viewLabel.setOnMouseClicked( event -> {
            try
            {
                //Declare and instantiate a new stage to display a new window
                Stage viewBookingStage = new Stage();
                //Get the current stage to make the new stage's parent
                Stage parentStage = (Stage) btnClose.getScene().getWindow();
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
                Scene addBookingScene = new Scene(root);
                //Set the new scene to the new stage
                viewBookingStage.setScene(addBookingScene);
                //Create a simple fade in transition to the new scene
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
                //Show the scene and refresg
                viewBookingStage.showAndWait();
                refreshCalendar();


            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        });



    }

    //Display a tick showing if the booking is checked in or not
    public void displayTickImg(Booking booking, ImageView imgv)
    {
        //Ensure the bookings is on the current date or before the current date to give the option to check in
        if(!booking.getBookDate().isAfter(LocalDate.now())) {

            if (booking.isCheckedIn()) {
                //Get a green tick image
                try {
                    URL greenTickURL = Paths.get("src/SFMS/Views/Images/greenTick.png").toUri().toURL();
                    Image img = new Image(greenTickURL.toExternalForm());
                    //set the green tick to the imageview
                    imgv.setImage(img);
                    //Make the image visible
                    imgv.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //Get a red tick image and assign extra actions for the image view
                //Let the user click on it to check in a booking
                try {
                    URL redTickURL = Paths.get("src/SFMS/Views/Images/redTick.png").toUri().toURL();
                    Image img = new Image(redTickURL.toExternalForm());
                    //Add a tool tip, also assign it to a variable so it can be removed
                    Tooltip tp = tickToolTip(imgv);
                    //set the image to the imageview
                    imgv.setImage(img);
                    //Make the image visible
                    imgv.setVisible(true);

                    //Change the user's pointer design when it enters the imageview
                    imgv.setOnMouseEntered(event -> {
                        imgv.setCursor(Cursor.HAND);
                    });
                    imgv.setOnMouseExited(event -> {
                        imgv.setCursor(Cursor.DEFAULT);
                    });

                    //Mouse click event handler to allow the user check in the booking
                    imgv.setOnMouseClicked(event -> {
                        final boolean success = bookingCheckIn(booking);
                        //If the check in was successful, remove the event handlers and the tooltip and change the tick color
                        if (success) {
                            //Remove event handlers
                            imgv.setOnMouseClicked(null);
                            imgv.setOnMouseEntered(null);
                            //Remove the tooltip
                            Tooltip.uninstall(imgv, tp);
                            //Change the tick color
                            try {
                                imgv.setImage(new Image(Paths.get("src/SFMS/Views/Images/greenTick.png").toUri().toURL().toExternalForm()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Method that checks in a booking
    public boolean bookingCheckIn(Booking booking)
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

                return true;
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
        return false;
    }

    //Customise small dialogs
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

    //Add a toolTip to the red tick to let the user know it can be clicked
    // Also return it so it can be removed if needed to
    public Tooltip  tickToolTip(ImageView tickImgV)
    {
        Tooltip tp = new Tooltip();
        tp.setStyle("-fx-font: 13px Calibri; -fx-background-color: #006400");
        //Reposition the tooltip
        tp.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_RIGHT);
        //Customise the show duration
        tp.setShowDuration(new Duration(5 * 1000));
        //Decrease the show text delay
        tp.setShowDelay(new Duration(0.1 * 1000));
        //Set the text of the tool tip
        tp.setText("Check In");

        //Add the tool tip to the tick imageview
        Tooltip.install(tickImgV, tp);
        return tp;
    }

    //search for the booking labels VBox in the next time block of the same date
    public VBox getNextBlockVB1(int row, int column)
    {
        //Search for the required cell in the grid pane
        for (Node node : gpCalendar.getChildren()) {

            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {
                //add 1 to the row to ge the next time block
                if (GridPane.getRowIndex(node) == row + 1 && GridPane.getColumnIndex(node) == column) {
                    //get the vBox
                    for (Node nd : ((AnchorPane) node).getChildren()) {
                        if (nd instanceof HBox) {
                            //return the required vb
                            return (VBox) ((HBox) nd).getChildren().get(0);
                        }
                    }
                }
            }
        }

        return null;
    }

    //search for the view labels VBox in the next time block of the same date
    public VBox getNextBlockVB2(int row, int column)
    {
        //Search for the required cell in the grid pane
        for (Node node : gpCalendar.getChildren()) {

            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {

                //add 1 to the row to ge the next time block
                if (GridPane.getRowIndex(node) == row + 1 && GridPane.getColumnIndex(node) == column) {
                    //get the vBox
                    for (Node nd : ((AnchorPane) node).getChildren()) {
                        if (nd instanceof HBox) {
                            //return the required vb
                            return (VBox) ((HBox) nd).getChildren().get(1);
                        }
                    }
                }
            }
        }

        return null;
    }

    //search for the imageview VBox in the next time block of the same date
    public VBox getNextBlockVB3(int row, int column)
    {
        //Search for the required cell in the grid pane
        for (Node node : gpCalendar.getChildren()) {

            if(GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {

                if (GridPane.getRowIndex(node) == row + 1 && GridPane.getColumnIndex(node) == column) {
                    //get the vBox
                    for (Node nd : ((AnchorPane) node).getChildren()) {
                        if (nd instanceof HBox) {
                            //return the required vb
                            return (VBox) ((HBox) nd).getChildren().get(2);
                        }
                    }
                }
            }
        }

        return null;
    }

    //Get the column of the booking by comparing the retrieved date and the displayed dates in the calendar
    public int getBookingColumn(LocalDate date)
    {
        if (date.equals(mondayDate))
        {
            return 1;
        }

        else if (date.equals(tuesdayDate))
        {
            return 2;
        }
        else if (date.equals(wednesdayDate))
        {
            return 3;
        }

        else if (date.equals(thursdayDate))
        {
            return 4;
        }

        else if (date.equals(fridayDate))
        {
            return 5;
        }

        else if (date.equals(saturdayDate))
        {
            return 6;
        }
        else if (date.equals(sundayDate))
        {
            return 7;
        }
        else
        {
            return 0;
        }
    }

    //Get the time block the booking is placed in by comparing the time with the blocks in the calendar
    public int getTimeBlock(String time)
    {

        if (time.equals("09:00") || time.equals("09:30") || time.equals("10:00") || time.equals("10:30"))
        {
            return  1;
        }
        else if (time.equals("11:00") || time.equals("11:30") || time.equals("12:00") || time.equals("12:30"))
        {
            return 2;
        }
        else if (time.equals("13:00") || time.equals("13:30") || time.equals("14:00") || time.equals("14:30"))
        {
            return 3;
        }
        else if (time.equals("15:00") || time.equals("15:30") || time.equals("16:00") || time.equals("16:30"))
        {
            return 4;
        }
        else if (time.equals("17:00") || time.equals("17:30") || time.equals("18:00") || time.equals("18:30"))
        {
            return 5;
        }
        else if (time.equals("19:00") || time.equals("19:30") || time.equals("20:00") || time.equals("20:30"))
        {
            return 6;
        }
        else
        {
            return 0;
        }
    }



    //Retrieves all data needed to have initial data in the add new booking window
    public void retrieveRequiredData(Button button)
    {
        //Retrieve the facility name
        selectedFacName = lblFacName.getText();

        //Retrieve the time block and date
        int rowIndex = GridPane.getRowIndex(button.getParent());
        int colIndex = GridPane.getColumnIndex(button.getParent());

        //time block
        switch (rowIndex) {
            case 1:
                selectedBlock = 1;
                break;

            case 2:
                selectedBlock = 2;
                break;
            case 3:
                selectedBlock = 3;
                break;
            case 4:
                selectedBlock = 4;
                break;
            case 5:
                selectedBlock = 5;
                break;
            case 6:
                selectedBlock = 6;
                break;
        }
        //Get the times of the block that are available
        AnchorPane cell =  (AnchorPane) button.getParent();
        HBox vBoxesList = (HBox) cell.getChildren().get(0);
        viewBox = (VBox) vBoxesList.getChildren().get(0);

        //reset the times array
        Arrays.fill(timesAvailableArray, Boolean.FALSE);

        //If the block is empty, make all times available
        if (viewBox.getChildren().size() == 0)
        {
            timesAvailableArray[0] = true;
            timesAvailableArray[1] = true;
            timesAvailableArray[2] = true;
            timesAvailableArray[3] = true;
        }

        //If the block has one label, make it available only if it's empty
        else if (viewBox.getChildren().size() == 1)
        {
            Label lbl1 = (Label) viewBox.getChildren().get(0);
            if (lbl1.getText().isEmpty()) {
                timesAvailableArray[0] = true;
            }
            //The rest of time slots are empty
            timesAvailableArray[1] = true;
            timesAvailableArray[2] = true;
            timesAvailableArray[3] = true;
        }
        else if (viewBox.getChildren().size() == 2) {
            Label lbl1 = (Label) viewBox.getChildren().get(0);
            Label lbl2 = (Label) viewBox.getChildren().get(1);
            if (lbl1.getText().isEmpty()) {
                timesAvailableArray[0] = true;
            }
            if (lbl2.getText().isEmpty()) {
                timesAvailableArray[1] = true;
            }

            timesAvailableArray[2] = true;
            timesAvailableArray[3] = true;
        }
        else if (viewBox.getChildren().size() == 3)
        {
            Label lbl1 = (Label) viewBox.getChildren().get(0);
            Label lbl2 = (Label) viewBox.getChildren().get(1);
            Label lbl3 = (Label) viewBox.getChildren().get(2);
            if (lbl1.getText().isEmpty()) {
                timesAvailableArray[0] = true;
            }
            if (lbl2.getText().isEmpty()) {
                timesAvailableArray[1] = true;
            }
            if (lbl3.getText().isEmpty()) {
                timesAvailableArray[1] = true;
            }

            timesAvailableArray[3] = true;
        }
        else if (viewBox.getChildren().size() == 4)
        {
            Label lbl1 = (Label) viewBox.getChildren().get(0);
            Label lbl2 = (Label) viewBox.getChildren().get(1);
            Label lbl3 = (Label) viewBox.getChildren().get(2);
            Label lbl4 = (Label) viewBox.getChildren().get(3);

            System.out.println(lbl1.getText());
            System.out.println(lbl2.getText());
            System.out.println(lbl3.getText());
            System.out.println(lbl4.getText());

            if (lbl1.getText().isEmpty())
            {
                timesAvailableArray[0] = true;
            }
            if (lbl2.getText().isEmpty())
            {
                timesAvailableArray[1] = true;
            }
            if (lbl3.getText().isEmpty())
            {
                timesAvailableArray[2] = true;
            }
            if (lbl4.getText().isEmpty())
            {
                timesAvailableArray[3] = true;
            }
        }


        //Retrieve the next time block to to assist in finding the duration availability
        nextTimeBlock = getNextBlockVB1( rowIndex, colIndex);


        //Date
        switch (colIndex)
        {
            case 1:
                selectedDate = dpBookings.getValue().with(DayOfWeek.MONDAY).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                selectedDateDisplay = dpBookings.getValue().with(DayOfWeek.MONDAY);
                break;
            case 2:
                selectedDate = dpBookings.getValue().with(DayOfWeek.TUESDAY).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                selectedDateDisplay = dpBookings.getValue().with(DayOfWeek.TUESDAY);
                break;
            case 3:
                selectedDate = dpBookings.getValue().with(DayOfWeek.WEDNESDAY).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                selectedDateDisplay = dpBookings.getValue().with(DayOfWeek.WEDNESDAY);
                break;
            case 4:
                selectedDate = dpBookings.getValue().with(DayOfWeek.THURSDAY).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                selectedDateDisplay = dpBookings.getValue().with(DayOfWeek.THURSDAY);
                break;
            case 5:
                selectedDate = dpBookings.getValue().with(DayOfWeek.FRIDAY).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                selectedDateDisplay = dpBookings.getValue().with(DayOfWeek.FRIDAY);
                break;
            case 6:
                selectedDate = dpBookings.getValue().with(DayOfWeek.SATURDAY).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                selectedDateDisplay = dpBookings.getValue().with(DayOfWeek.SATURDAY);
                break;
            case 7:
                selectedDate = dpBookings.getValue().with(DayOfWeek.SUNDAY).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                selectedDateDisplay = dpBookings.getValue().with(DayOfWeek.SUNDAY);
                break;
        }

        //Database instance to allow its connection
        DBConnection db = new DBConnection();
        //Retrieve the Facilities collection to get the price of the selected facility
        MongoCollection<Document> col = db.collectionRetrieval("Facilities");
        //Retrieve the required facility
        Document facilityDoc = col.find(eq("Name", lblFacName.getText())).first() ;
        //Retrieve the price, min people and max people
        price = facilityDoc.getDouble("£/30mins");
        minPeople = facilityDoc.getInteger("MinPeople");
        maxPeople = facilityDoc.getInteger("MaxPeople");
        //Close the database connection
        db.close();

    }

    //hide add booking buttons previous to the current date
    public void hideAddButtons() {
        LocalDate currentDate = LocalDate.now();
        //Loop through all cells in the calendar
        for (Node node : gpCalendar.getChildren()) {
            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {


                    //If the date of monday is before the current day, hide all buttons in column 1
                    if(GridPane.getColumnIndex(node) == 1) {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide
                                nd.setVisible(!mondayDate.isBefore(currentDate));
                            }
                        }
                    }

                    //If the date of tuesday is before the current day, hide all buttons in column 2
                if(GridPane.getColumnIndex(node) == 2) {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide
                                nd.setVisible(!tuesdayDate.isBefore(currentDate));
                            }
                        }
                    }

                    //If the date of wednesday is before the current day, hide all buttons in column 3
                if(GridPane.getColumnIndex(node) == 3) {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide
                                nd.setVisible(!wednesdayDate.isBefore(currentDate));
                            }
                        }
                    }

                    //If the date of thursday is before the current day, hide all buttons in column 4
                if(GridPane.getColumnIndex(node) == 4){
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide
                                nd.setVisible(!thursdayDate.isBefore(currentDate));
                            }
                        }
                    }

                    //If the date of friday is before the current day, hide all buttons in column 5
                if(GridPane.getColumnIndex(node) == 5) {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide
                                nd.setVisible(!fridayDate.isBefore(currentDate));

                            }
                        }
                    }

                    //If the date of saturday is before the current day, hide all buttons in column 6
                if(GridPane.getColumnIndex(node) == 6) {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide
                                nd.setVisible(!saturdayDate.isBefore(currentDate));
                            }
                        }
                    }

                    //If the date of sunday is before the current day, hide all buttons in column 7
                if(GridPane.getColumnIndex(node) == 7) {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide
                                nd.setVisible(!sundayDate.isBefore(currentDate));
                            }
                        }
                    }


            }
        }
    }

    //Hide the buttons that are before the current time in the current date
    public void hideButtonsTime()
    {
        //Current date and time
        LocalDate currentDate = LocalDate.now();
        LocalTime time = LocalTime.now();

        //loop through all the cells
        for (Node node : gpCalendar.getChildren()) {
            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {

                    //block 1
                    if (GridPane.getRowIndex(node) == 1) {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide the button if it's in the current date and the time is before the current time
                                if (mondayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 1) {
                                    nd.setVisible(!LocalTime.parse("10:30").isBefore(time));
                                } else if (tuesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 2) {
                                    nd.setVisible(!LocalTime.parse("10:30").isBefore(time));
                                } else if (wednesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 3) {
                                    nd.setVisible(!LocalTime.parse("10:30").isBefore(time));
                                } else if (thursdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 4) {
                                    nd.setVisible(!LocalTime.parse("10:30").isBefore(time));
                                } else if (fridayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 5) {
                                    nd.setVisible(!LocalTime.parse("10:30").isBefore(time));
                                } else if (saturdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 6) {
                                    nd.setVisible(!LocalTime.parse("10:30").isBefore(time));
                                } else if (sundayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 7) {
                                    nd.setVisible(!LocalTime.parse("10:30").isBefore(time));
                                }
                            }
                        }
                    }

                    //Block 2
                if (GridPane.getRowIndex(node) == 2)  {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide the button if it's in the current date and the time is before the current time
                                if (mondayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 1) {
                                    nd.setVisible(!LocalTime.parse("12:30").isBefore(time));
                                } else if (tuesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 2) {
                                    nd.setVisible(!LocalTime.parse("12:30").isBefore(time));
                                } else if (wednesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 3) {
                                    nd.setVisible(!LocalTime.parse("12:30").isBefore(time));
                                } else if (thursdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 4) {
                                    nd.setVisible(!LocalTime.parse("12:30").isBefore(time));
                                } else if (fridayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 5) {
                                    nd.setVisible(!LocalTime.parse("12:30").isBefore(time));
                                } else if (saturdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 6) {
                                    nd.setVisible(!LocalTime.parse("12:30").isBefore(time));
                                } else if (sundayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 7) {
                                    nd.setVisible(!LocalTime.parse("12:30").isBefore(time));
                                }
                            }
                        }
                    }

                    //Block 3
                if (GridPane.getRowIndex(node) == 3)  {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide the button if it's in the current date and the time is before the current time
                                if (mondayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 1) {
                                    nd.setVisible(!LocalTime.parse("14:30").isBefore(time));
                                } else if (tuesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 2) {
                                    nd.setVisible(!LocalTime.parse("14:30").isBefore(time));
                                } else if (wednesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 3) {
                                    nd.setVisible(!LocalTime.parse("14:30").isBefore(time));
                                } else if (thursdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 4) {
                                    nd.setVisible(!LocalTime.parse("14:30").isBefore(time));
                                } else if (fridayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 5) {
                                    nd.setVisible(!LocalTime.parse("14:30").isBefore(time));
                                } else if (saturdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 6) {
                                    nd.setVisible(!LocalTime.parse("14:30").isBefore(time));
                                } else if (sundayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 7) {
                                    nd.setVisible(!LocalTime.parse("14:30").isBefore(time));
                                }
                            }
                        }
                    }

                    //Block 4
                if (GridPane.getRowIndex(node) == 4)  {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide the button if it's in the current date and the time is before the current time
                                if (mondayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 1) {
                                    nd.setVisible(!LocalTime.parse("16:30").isBefore(time));
                                } else if (tuesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 2) {
                                    nd.setVisible(!LocalTime.parse("16:30").isBefore(time));
                                } else if (wednesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 3) {
                                    nd.setVisible(!LocalTime.parse("16:30").isBefore(time));
                                } else if (thursdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 4) {
                                    nd.setVisible(!LocalTime.parse("16:30").isBefore(time));
                                } else if (fridayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 5) {
                                    nd.setVisible(!LocalTime.parse("16:30").isBefore(time));
                                } else if (saturdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 6) {
                                    nd.setVisible(!LocalTime.parse("16:30").isBefore(time));
                                } else if (sundayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 7) {
                                    nd.setVisible(!LocalTime.parse("16:30").isBefore(time));
                                }
                            }
                        }
                    }

                    //Block 5
                if (GridPane.getRowIndex(node) == 5)  {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide the button if it's in the current date and the time is before the current time
                                if (mondayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 1) {
                                    nd.setVisible(!LocalTime.parse("18:30").isBefore(time));
                                } else if (tuesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 2) {
                                    nd.setVisible(!LocalTime.parse("18:30").isBefore(time));
                                } else if (wednesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 3) {
                                    nd.setVisible(!LocalTime.parse("18:30").isBefore(time));
                                } else if (thursdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 4) {
                                    nd.setVisible(!LocalTime.parse("18:30").isBefore(time));
                                } else if (fridayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 5) {
                                    nd.setVisible(!LocalTime.parse("18:30").isBefore(time));
                                } else if (saturdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 6) {
                                    nd.setVisible(!LocalTime.parse("18:30").isBefore(time));
                                } else if (sundayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 7) {
                                    nd.setVisible(!LocalTime.parse("18:30").isBefore(time));
                                }
                            }
                        }
                    }

                    //Block 6
                if (GridPane.getRowIndex(node) == 6)  {
                        for (Node nd : ((AnchorPane) node).getChildren()) {
                            if (nd instanceof Button) {
                                //hide the button if it's in the current date and the time is before the current time
                                if (mondayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 1) {
                                    nd.setVisible(!LocalTime.parse("20:30").isBefore(time));
                                } else if (tuesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 2) {
                                    nd.setVisible(!LocalTime.parse("20:30").isBefore(time));
                                } else if (wednesdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 3) {
                                    nd.setVisible(!LocalTime.parse("20:30").isBefore(time));
                                } else if (thursdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 4) {
                                    nd.setVisible(!LocalTime.parse("20:30").isBefore(time));
                                } else if (fridayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 5) {
                                    nd.setVisible(!LocalTime.parse("20:30").isBefore(time));
                                } else if (saturdayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 6) {
                                    nd.setVisible(!LocalTime.parse("20:30").isBefore(time));
                                } else if (sundayDate.isEqual(currentDate) && GridPane.getColumnIndex(node) == 7) {
                                    nd.setVisible(!LocalTime.parse("20:30").isBefore(time));
                                }
                            }
                        }
                    }

            }
        }
    }

    //Disable add booking buttons in cells that are fully booked
    public void disableButtons( AnchorPane node)
    {

        boolean full = false;
        //Explore each cell
        for (Node nd : node.getChildren())
        {
            if (nd instanceof HBox)
            {

                VBox labelsBox = (VBox) ((HBox) nd).getChildren().get(0);
                //Check the vbox that holds the labels is full
                if(labelsBox.getChildren().size() == 4)
                {
                    Label lbl1 = (Label) labelsBox.getChildren().get(0);
                    Label lbl2 = (Label) labelsBox.getChildren().get(1);
                    Label lbl3 = (Label) labelsBox.getChildren().get(2);
                    Label lbl4 = (Label) labelsBox.getChildren().get(3);
                    if(!lbl1.getText().isEmpty() && !lbl2.getText().isEmpty() && !lbl3.getText().isEmpty() && !lbl4.getText().isEmpty())
                    {
                        full = true;
                    }
                }
            }
            //Disable the button if the vbox holding the labels is full
            if(nd instanceof Button && full)
            {
                //System.out.println(full);
                nd.setDisable(true);

            }
        }
    }

    //Closes the current stage
    public void btnCloseOnAction()
    {
        //End the timer that refreshes the calendar
        t.cancel();
        //Close the stage
       Stage bookingStage = (Stage) btnClose.getScene().getWindow();
       bookingStage.close();

    }

    //Unit test for the getTimeBlock method
    @Test
    public void getTimeBlockTest()
    {
        assertEquals(4, getTimeBlock("16:30"));
    }

}


