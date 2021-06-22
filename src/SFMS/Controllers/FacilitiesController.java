package SFMS.Controllers;

import SFMS.Models.DBConnection;
import SFMS.Models.Facility;
import com.mongodb.client.MongoCollection;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FacilitiesController
{

    @FXML
    private ComboBox<String> cbActivity;

    @FXML
    private Button btnAddFacility;

    @FXML
    private GridPane gpFacilitiesList;


    //Display all facilities when the section loads
    public void initialize()
    {
        displayFacilities("All Facilities");
        populateActivityCB();
    }

    //Method that allows the display of bookings of a facility
    public void btnBookingsOnAction(ActionEvent event)
    {
        //Get the button clicked to show the correct facility's bookings
        Button sourceButton = (Button) event.getSource();
        String facName = retrieveRequiredFacility(sourceButton);

        //Ensure the facility exists before opening the bookings view
        DBConnection db = new DBConnection();
        MongoCollection<Document> facCol = db.collectionRetrieval("Facilities");
        Document facDoc = facCol.find(eq("Name", facName)).first();
        db.close();
        if(facDoc == null)
        {
            //Let the user know the facility does not exist
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Facility Does Not Exist");
            errorAlert.setHeaderText("The selected facility does not exist anymore, please refresh the page.");
            errorAlert.setContentText(null);
            customiseDialog(errorAlert.getDialogPane());
            errorAlert.showAndWait();
        }
        else {
            //Open the bookings view
            try {
                //Declare and instantiate a new stage to display a new window
                Stage bookingStage = new Stage();
                //Get the current stage to make the new stage's parent
                Stage parentStage = (Stage) btnAddFacility.getScene().getWindow();
                bookingStage.initOwner(parentStage);
                //Edit the modality so the user cannot do anything else until the new window is closed
                bookingStage.initModality(Modality.WINDOW_MODAL);
                //Remove the default OS windows designs
                bookingStage.initStyle(StageStyle.UNDECORATED);

                //Get the URL of the FXML document to be displayed a new scene
                URL url = Paths.get("./src/SFMS/Views/BookingsView.fxml").toUri().toURL();
                //Load the document
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(url);
                Parent root = loader.load();

                //Get the controller of FMXL file to pass some data to be displayed in the new stage
                BookingsController controller = loader.getController();
                //Pass required data
                controller.setFacilityName(facName);

                Scene bookingsScene = new Scene(root);
                //Set the new scene to the new stage
                bookingStage.setScene(bookingsScene);
                //Show the scene
                bookingStage.show();

                //Create a simple fade in transition to the new scene
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Button to add a new facility
    public void btnAddFacilityOnAction()
    {
        //Open a new fxml view
        try
        {
            //Declare and instantiate a new stage to display a new window
            Stage newFacilityStage = new Stage();
            //Get the current stage to make the new stage's parent
            Stage parentStage = (Stage) btnAddFacility.getScene().getWindow();
            newFacilityStage.initOwner(parentStage);
            //Edit the modality so the user cannot do anything else until the new window is closed
            newFacilityStage.initModality(Modality.WINDOW_MODAL);
            //Remove the default OS windows designs
            newFacilityStage.initStyle(StageStyle.UNDECORATED);

            //Get the URL of the FXML document to be displayed a new scene
            URL url = Paths.get("./src/SFMS/Views/NewFacilityView.fxml").toUri().toURL();
            //Load the document
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(url);
            Parent root = loader.load();


            //Declare and initialize a new scene with the retrieved FXML file
            Scene newFacilityScene = new Scene(root);
            //Set the new scene to the new stage
            newFacilityStage.setScene(newFacilityScene);

            //Create a simple fade in transition to the new scene
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            //Show the scene and refresh the list
            newFacilityStage.showAndWait();
            displayFacilities("All Facilities");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Populate the filter box
    public void populateActivityCB()
    {
        cbActivity.getItems().addAll("Football", "Tennis", "Basketball", "Badminton", "Other", "All Facilities");
    }

    //Show full information of the facility, window will also allow the edition and removal of a facility
    public void btnInfoOnAction(ActionEvent event)
    {
        //Get the button clicked to show the correct facility
        Button sourceButton = (Button) event.getSource();
        String facName = retrieveRequiredFacility(sourceButton);

        //Open a new fxml view
        try
        {
            //Declare and instantiate a new stage to display a new window
            Stage infoFacilityStage = new Stage();
            //Get the current stage to make the new stage's parent
            Stage parentStage = (Stage) btnAddFacility.getScene().getWindow();
            infoFacilityStage.initOwner(parentStage);
            //Edit the modality so the user cannot do anything else until the new window is closed
            infoFacilityStage.initModality(Modality.WINDOW_MODAL);
            //Remove the default OS windows designs
            infoFacilityStage.initStyle(StageStyle.UNDECORATED);

            //Get the URL of the FXML document to be displayed a new scene
            URL url = Paths.get("./src/SFMS/Views/ViewFacilityView.fxml").toUri().toURL();
            //Load the document
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(url);
            Parent root = loader.load();

            //Get the controller of FMXL file to pass some data to be displayed in the new stage
            ViewFacilityController controller = loader.getController();
            //Pass required data
            controller.retrieveSelectedFacility(facName);

            //Declare and initialize a new scene with the retrieved FXML file
            Scene infoFacilityScene = new Scene(root);
            //Set the new scene to the new stage
            infoFacilityStage.setScene(infoFacilityScene);

            //Create a simple fade in transition to the new scene
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            //Show the scene and refresh the list when the window closes
            infoFacilityStage.showAndWait();
            displayFacilities("All Facilities");


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    //Display all saved facilities
    public void displayFacilities(String filter)
    {
        //Clear any fields to refresh
        if (gpFacilitiesList.getChildren().size() > 0) {
            gpFacilitiesList.getChildren().clear();
        }

        //Ensure only one row is present at the beginning of the facilities addition
        while(gpFacilitiesList.getRowConstraints().size() > 1)
        {
            gpFacilitiesList.getRowConstraints().remove(1);
        }

        //Get the facilities list
        DBConnection db = new DBConnection();
        ArrayList<Facility>  facilitiesList = db.getFacilities(filter);
        db.close();
        //Start the displaying from the first cell in the grid pane
        int row = 0;
        int column = 0;
        for (Facility fac : facilitiesList)
        {
            //If column index is 4, increase the row number and add a new row for the cell to be displayed
            if (column == 4)
            {
                row += 1;;
                column = 0;
                gpFacilitiesList.getRowConstraints().add(new RowConstraints(310, 310, 310));
            }

            //Create the cell design with the retrieved data and display it on the specified row and column
            createFacilityCell(fac.getFacName(), fac.getFacActivity(), fac.getFacType(), fac.getFacPrice(), row, column);

            //add the next booking to the next column
            column += 1;


        }
    }

    //Combo box that allows a filtered search
    public void cbActivityOnAction()
    {
        displayFacilities(cbActivity.getValue());
    }

    //Design and create a new cell to display a facility in the grid pane
    public void createFacilityCell(String name, String activity, String type, double price, int row, int col)
    {
        //Anchor pane that hold the picture of the facility
        AnchorPane picPane = new AnchorPane();
        picPane.setStyle("-fx-border-width: 0 0 5 0; -fx-border-color:  #006400");
        picPane.setPrefWidth(220);
        picPane.setPrefHeight(111);
        //Get the picture and add it to the anchor pane
        picPane.getChildren().add(getPicture(activity));

        //HBox that holds all the information
        HBox detailsHBox = new HBox();

        //VBox to have labels description of the details
        VBox labels1VBox = new VBox();
        //Insets instance to allow the addition of a margin to the labels
        Insets lbls1Margin = new Insets(10, 0, 0, 10);
        //Style string for the labels
        String labelsStyle = "-fx-font-size: 16px; -fx-font-weight: bold;";
        Label lblName = new Label("Name:");
        lblName.setStyle(labelsStyle);
        VBox.setMargin(lblName, lbls1Margin);
        Label lblActivity = new Label("Activity:");
        lblActivity.setStyle(labelsStyle);
        VBox.setMargin(lblActivity, lbls1Margin);
        Label lblType = new Label("Type:");
        lblType.setStyle(labelsStyle);
        VBox.setMargin(lblType, lbls1Margin);
        Label lblPrice = new Label("£/30mins:");
        lblPrice.setStyle(labelsStyle);
        VBox.setMargin(lblPrice, lbls1Margin);
        //Button to display all info of the selected facilty
        Button btnInfo = new Button("Full Info.");
        //Style the button
        btnInfo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-underline: true;");
        //Set preferred widtha and height
        btnInfo.setPrefWidth(80);
        btnInfo.setPrefHeight(30);
        VBox.setMargin(btnInfo, lbls1Margin);
        //On Action listener to display full info details
        //:: Method reference, functional interface behaviour
        btnInfo.setOnAction(this::btnInfoOnAction);
        //Add all the labels and the button to the first VBox
        labels1VBox.getChildren().addAll(lblName, lblActivity, lblType, lblPrice, btnInfo);

        //Second VBox to have the details
        VBox labels2VBox = new VBox();
        //Inset instance to add a margin
        Insets lbls2Margin = new Insets( 10, 0, 0, 2);
        Label lblName1 = new Label(name);
        lblName1.setStyle(labelsStyle);
        VBox.setMargin(lblName1, lbls2Margin);
        Label lblActivity1 = new Label(activity);
        lblActivity1.setStyle(labelsStyle);
        VBox.setMargin(lblActivity1, lbls2Margin);
        Label lblType1 = new Label(type);
        lblType1.setStyle(labelsStyle);
        VBox.setMargin(lblType1, lbls2Margin);
        Label lblPrice1 = new Label("£" + price);
        lblPrice1.setStyle(labelsStyle);
        VBox.setMargin(lblPrice1, lbls2Margin);

        Button btnBookings = new Button("Bookings");

        //Add the button to a CSS class to customise it
        try {
            URL url = Paths.get("./src/SFMS/Views/Style.css").toUri().toURL();
            btnBookings.getStylesheets().add(url.toExternalForm());
            btnBookings.getStyleClass().add("Save");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Adjust a bit more of style details
        btnBookings.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; ");
        VBox.setMargin(btnBookings, new Insets(5, 0, 0, 20));
        //On Action listener to display the bookings window for the selected facility
        //::Method reference
        btnBookings.setOnAction(this::btnBookingsOnAction);
        //Add all the labels and the button to the second VBox
        labels2VBox.getChildren().addAll(lblName1, lblActivity1, lblType1, lblPrice1, btnBookings);

        //Add both VBoxes to the HBox that contains all details nodes
        detailsHBox.getChildren().addAll(labels1VBox, labels2VBox);

        //Style the hbox
        VBox newFacilityVBox = new VBox(picPane, detailsHBox);
        newFacilityVBox.setStyle("-fx-border-width: 5; -fx-border-color: #006400; -fx-border-radius: 10; " +
                "-fx-background-radius: 10; -fx-background-color:  #B4D6E4");

        //add to the grid pane
        gpFacilitiesList.add(newFacilityVBox, col, row);


    }

    //Gets the corresponding picture of the facility
    public ImageView getPicture(String activity)
    {

        Image img = null;
        //Use the activity name to call the files
        try {
            URL greenTickURL = Paths.get("src/SFMS/Views/Images/" + activity.toLowerCase() + "Icon.jpg").toUri().toURL();
             img = new Image(greenTickURL.toExternalForm());
        }catch (Exception e){
            e.printStackTrace();
        }

        //Initialise the picture view
        ImageView picture = new ImageView(img);
        picture.setFitWidth(113);
        picture.setFitHeight(94);
        picture.setLayoutX(65);
        picture.setLayoutY(6);

        //return it
        return picture;
    }

    //Retrieves the facility clicked
    public String retrieveRequiredFacility(Button button)
    {

        //Navigate through the nodes to get the label that contains the name of the Facility
        HBox detailsHBox= (HBox) button.getParent().getParent();
        VBox secondVBox = (VBox) detailsHBox.getChildren().get(1);
        Label facName = (Label) secondVBox.getChildren().get(0);

        //Return the name of the selected facility
       return facName.getText();
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
