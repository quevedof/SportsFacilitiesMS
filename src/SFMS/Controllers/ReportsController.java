package SFMS.Controllers;

import SFMS.Models.Booking;
import SFMS.Models.DBConnection;
import SFMS.Models.Facility;
import SFMS.Models.Tournament;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;

import java.awt.print.Book;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ReportsController {
    @FXML
    private LineChart<String,Number> lgBookings;

    @FXML
    private PieChart pcFacilities;

    @FXML
    private ComboBox<String> cbMonths;

    @FXML
    private ListView<String> lvActivities;

    @FXML
    private ProgressBar pbRevenue;

    @FXML
    private Label lblRevenue;

    @FXML
    private Label lblTotalBookings;

    @FXML
    private ProgressBar pbTournaments;

    @FXML
    private Label lblTournaments;

    //All bookings in the selected month
    ArrayList<Booking> bookingsArrayList = new ArrayList<>();

    //Populate the combo and line chart and pie chart
    public void initialize()
    {
        setCbMonths();
        populateLineChart();
        populatePieChart();
        populateProgressBars();
        populateActivitiesList();
    }

    //Set data to the combo box
    public void setCbMonths()
    {
        //Add every month to the drop box
        cbMonths.getItems().addAll("January", "February", "March",
                "April", "May", "June",
                "July", "August", "September",
                "October", "November", "December");
        //Select the current month
        cbMonths.getSelectionModel().select(LocalDate.now().getMonthValue() -1);
    }

    //Change the graph when the user changes the month
    public void cbMonthsOnAction()
    {
        populateLineChart();
        populatePieChart();
        populateProgressBars();
        populateActivitiesList();
    }

    //Populate the line chart
    public void populateLineChart()
    {
        //Clear previous data
        lgBookings.getData().clear();
        bookingsArrayList.clear();

        //Title the line chart
        lgBookings.setTitle(cbMonths.getValue());


        //Create a new series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("All Facilities");

        //Get the month selected by the user
        int selectedMonth = cbMonths.getSelectionModel().getSelectedIndex() + 1;
        YearMonth month = YearMonth.of(LocalDate.now().getYear(), selectedMonth);

        //Get the length of the month
        int monthLength = month.lengthOfMonth();

        //Add a new piece of data for each day in the month
        for (int i = 1;  i  < monthLength + 1; i++)
        {
            series.getData().add(new XYChart.Data<>(String.valueOf(i), getDayBookings(i, selectedMonth)));
        }

        //Add the series to the line chart
        lgBookings.getData().add(series);
        //Display total number of bookings for the selected month
        lblTotalBookings.setText(String.valueOf(bookingsArrayList.size()));
    }

    //Populates the pie chart
    public void populatePieChart()
    {
        //Make the selected month the title
        pcFacilities.setTitle(cbMonths.getValue());
        //Pie chart collection
        ObservableList<PieChart.Data> pieChartList = FXCollections.observableArrayList();

        //Get all facilities
        DBConnection db = new DBConnection();
        ArrayList<Facility> facilityArrayList = db.getFacilities("All Facilities");
        db.close();
        //Loop through each facility to get their corresponding number of bookings throughout the selected month
        for (Facility fac : facilityArrayList) {
            pieChartList.add(new PieChart.Data(fac.getFacName() + " (" + getMonthBookings(fac.getFacName()) + ")", getMonthBookings(fac.getFacName())));
        }
        //Add the data to the pie chart
        pcFacilities.setData(pieChartList);


        //If all values are empty, clear the chart
        boolean empty = true;
        for (PieChart.Data data : pieChartList)
        {
            System.out.println(data.getPieValue());
            if (data.getPieValue() != 0)
            {
                empty = false;
            }
        }
        if (empty)
        {
            pcFacilities.setTitle("No Data");
            pcFacilities.getData().clear();
        }
    }

    //Get the number of bookings made for each facility
    public int getMonthBookings(String fac)
    {
        //stores the number of bookings
        int facBookings = 0;
        //Loop through all bookings in the selected, add 1 to the count if the bookings is for the required facility
        for(Booking booking: bookingsArrayList)
        {
            if (booking.getBookFacility().equals(fac))
            {
                facBookings += 1;
            }
        }
        return  facBookings;
    }

    //Get the number of bookings for a certain day number in month
    public int getDayBookings(int day, int month)
    {
        //get the year
        int year = LocalDate.now().getYear();
        //Initialize the string date
        String dateString ="";
        //ensure there's a 0 in front of the single digit day and month
        if (day < 10 && month < 10) {
             dateString = "0" + day + "/" + "0" + month + "/" + year;
        }
        else if (day < 10)
        {
            dateString = "0" + day + "/" + month + "/" + year;
        }
        else if (month < 10)
        {
            dateString =  day + "/" + "0" + month + "/" + year;
        }
        else
        {
            dateString =  day + "/"  + month + "/" + year;
        }

        //Format for the local date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        //Initialize a local date with the required date
        LocalDate date = LocalDate.parse(dateString, formatter);

        //Retrieve all bookings made for the required date from the database
        DBConnection db = new DBConnection();
        //get the number for the certain date
        int numBookings = db.getDateBookings(date).size();
        //Add all bookings to the bookings list
        bookingsArrayList.addAll(db.getDateBookings(date));
        db.close();
        return numBookings;

    }

    //Populate the progress bars that show the revenue and the number of tournaments organised
    public void populateProgressBars()
    {
        //Revenue bar
        //Store the expected revenue;
        double expectedRevenue = 7000.00;
        //Stores total revenue
        double totalRevenue = 0.00;
        //Loop through all bookings and add the revenues for the ones that have been checked in
        for (Booking booking: bookingsArrayList)
        {
            if (booking.isCheckedIn())
            {
                totalRevenue = totalRevenue + booking.getBookPrice();
            }
        }

        //fraction for the progress bar
        double revenueFraction = totalRevenue/expectedRevenue;
        pbRevenue.setProgress(revenueFraction);

        //percentage to display it to the label

        lblRevenue.setText((int)(revenueFraction*100) + "%");


        //Tournaments progress bar
        //Expected tournaments
        double expectedTournaments = 15;
        double totalTournaments = 0.00;

        //Get all tournaments that have been organised in the month
        DBConnection db = new DBConnection();
        ArrayList<Tournament> tournamentArrayList = db.getTournaments("All Tournaments");
        db.close();
        for (Tournament tournament : tournamentArrayList)
        {
            if(tournament.getDateStart().getMonth().getValue() == cbMonths.getSelectionModel().getSelectedIndex() + 1)
            {
                totalTournaments = totalTournaments + 1;
            }
        }

        //fraction for the progress bar
        double tourFraction = totalTournaments/expectedTournaments;
        pbTournaments.setProgress(tourFraction);

        //percentage to display it to the label
        lblTournaments.setText((int)(tourFraction*100) + "%");
    }

    //List view of the most popular activity
    public void populateActivitiesList()
    {
        lvActivities.getItems().clear();
        lvActivities.getItems().addAll("1. Football", "2. Badminton ", "3. Tennis");
    }

}
