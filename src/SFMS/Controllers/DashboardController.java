package SFMS.Controllers;


import SFMS.Main;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.bson.LazyBSONList;
import org.bson.types.ObjectId;

import java.awt.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;


public class DashboardController {



    @FXML
    private BorderPane parentPane;
    @FXML
    private HBox hBoxNavigation;
    @FXML
    private VBox vBoxNavigationButtons;
    @FXML
    private VBox vBoxIcons;
    @FXML
    private Label lblSignOut;

    @FXML
    private Label lblUsername;

    //Variable that controls the presence of the navigation sidebar
    boolean hidden = false;

    ObjectId adminID ;
    public void initialize()
    {
        lblSignOutOnAction();
        loadSelectedUI("HomeView");
    }

    //Get the user name from the sign in window.
    public void setUserName(String userName, ObjectId adminID)
    {
        lblUsername.setText(userName);
        this.adminID = adminID;
    }

    //Load the home view
    public void btnHomeOnAction(){loadSelectedUI("HomeView");}
    //Load the facilities view
    public void btnFacilitiesOnAction()
    {
        loadSelectedUI("FacilitiesView");
    }
    //Load the competitions view
    public void btnCompetitionsOnAction(){loadSelectedUI("CompetitionsView");}
    //Load the clients view
    public void btnClientsOnAction(){loadSelectedUI("ClientsView");}
    //Load the reports view
    public void btnReportsOnAction(){loadSelectedUI("ReportsView");}
    //Load the account view
    public void btnAccountOnAction(){loadSelectedUI("AccountView");}


    //Method that handles the navigation side bar
    public void imvMenuOnMouseClicked()
    {
        //Set the icons to the front so they're not hidden by the translating sidebar
        vBoxIcons.setViewOrder(-1.0);

        //Declare the translation of the sidebar
        TranslateTransition slide = new TranslateTransition();
        //Set the duration of the translation
        slide.setDuration(Duration.seconds(0.5));
        //Add the translation to the corresponding vBox
        slide.setNode(vBoxNavigationButtons);

        //if the sidebar is hidden, show it
        if(hidden)
         {
        //Use a timeline to represent the width increase of the navigation bar's parent pane synchronising to the buttons' translation
        KeyValue widthValue = new KeyValue(hBoxNavigation.maxWidthProperty(), hBoxNavigation.getWidth() + 138.8);
        KeyFrame frame = new KeyFrame(Duration.seconds(0.5), widthValue);
        Timeline timeline = new Timeline(frame);
        timeline.play();

        //Set the sidebar starting translation coordinates
        slide.setToX(0);
        //Play the translation
        slide.play();
        //Set the sidebar end translation coordinates
        vBoxNavigationButtons.setTranslateX(-vBoxNavigationButtons.getMaxWidth());
        hidden = false;
         }

        //If the side bar is showing, hide it
        else
        {
            //Use a timeline to represent the width decrease of the navigation bar's parent pane synchronising to the buttons' translation
            KeyValue widthValue = new KeyValue(hBoxNavigation.maxWidthProperty(), hBoxNavigation.getWidth() - vBoxNavigationButtons.getWidth());
            KeyFrame frame = new KeyFrame(Duration.seconds(0.5), widthValue);
            Timeline timeline = new Timeline(frame);
            timeline.play();

            //Set the sidebar starting translation coordinates
            slide.setToX(-vBoxNavigationButtons.getWidth());
            //Play the translation
            slide.play();
            //Set the sidebar end translation coordinates
            vBoxNavigationButtons.setTranslateX(0);
            hidden = true;

        }

    }

    //Allow the user to sign out if clicked on it
    public void lblSignOutOnAction()
    {
        //Change the cursor when user hovers over it
        lblSignOut.setOnMouseEntered( event -> {
            lblSignOut.setCursor(javafx.scene.Cursor.HAND);
        });
        lblSignOut.setOnMouseExited( event -> {
            lblSignOut.setCursor(Cursor.DEFAULT);
        });

        //Add the sign out action
        lblSignOut.setOnMouseClicked(Event -> {
            try
            {
                //Entry Confirmation dialog, asks the user for a closing confirmation
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Sign Out Confirmation");
                confirmAlert.setHeaderText("Are you sure you want to Sign Out?");
                customiseDialog(confirmAlert.getDialogPane());

                //Edit the "Ok" button of the dialog to display "Yes" instead
                javafx.scene.control.Button yesButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
                yesButton.setText("Yes");

                confirmAlert.initModality(Modality.APPLICATION_MODAL);
                Optional<ButtonType> result = confirmAlert.showAndWait();

                //Proceed if the user the sign out
                if (result.get() == ButtonType.OK) {

                    //display the sign in window from main
                    Stage dashboardStage = (Stage) lblSignOut.getScene().getWindow();
                    Main main = new Main();
                    main.start(dashboardStage);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    //Load the requested UI
    public void loadSelectedUI(String ui)
    {
        try
        {
            //Get the URL of the FXML document to be displayed a new scene
            URL url = Paths.get("./src/SFMS/Views/" + ui + ".fxml").toUri().toURL();
            //Load the document
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(url);
            Parent root = loader.load();


            //Pass the admin ID if the user selected the Account UI
            if(ui.equals("AccountView")) {
                AccountController controller = loader.getController();
                controller.setAdmin(adminID);
            }

            parentPane.setCenter(root);

            //set an event handler to close the sidebar when clicked on the requested UI
            root.setOnMouseClicked(event -> {
                if (!hidden) {
                    //Set the icons to the front so they're not hidden by the translating sidebar
                    vBoxIcons.setViewOrder(-1.0);

                    //Declare the translation of the sidebar
                    TranslateTransition slide = new TranslateTransition();
                    //Set the duration of the translation
                    slide.setDuration(Duration.seconds(0.5));
                    //Add the translation to the corresponding vBox
                    slide.setNode(vBoxNavigationButtons);

                    //Use a timeline to represent the width decrease of the navigation bar's parent pane synchronising to the buttons' translation
                    KeyValue widthValue = new KeyValue(hBoxNavigation.maxWidthProperty(), hBoxNavigation.getWidth() - vBoxNavigationButtons.getWidth());
                    KeyFrame frame = new KeyFrame(Duration.seconds(0.5), widthValue);
                    Timeline timeline = new Timeline(frame);
                    timeline.play();

                    //Set the sidebar starting translation coordinates
                    slide.setToX(-vBoxNavigationButtons.getWidth());
                    //Play the translation
                    slide.play();
                    //Set the sidebar end translation coordinates
                    vBoxNavigationButtons.setTranslateX(0);
                    hidden = true;
                }
            });

            System.out.println();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Customise small window
    public void customiseDialog(DialogPane dialog)
    {
        //Add a background color and change the font size
        dialog.setStyle("-fx-background-color:  #ff3232; -fx-font-size: 14px; -fx-font-weight: bold");
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
