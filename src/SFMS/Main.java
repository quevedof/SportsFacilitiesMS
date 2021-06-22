package SFMS;


import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.nio.file.Paths;
import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Views/SignInView.fxml"));
        primaryStage.setTitle("Sports Facilities MS");
        primaryStage.setOnCloseRequest(confirmExitEventHandler);
        showBarIcon(primaryStage);
        primaryStage.setScene(new Scene(root, 821, 650));
        primaryStage.show();
    }

    //change to test a git branch
    public static void main(String[] args) {
        launch(args);
    }


    //Event handler that displays a dialog that asks for user confirmation when closing the application
    private EventHandler<WindowEvent> confirmExitEventHandler =  event ->
    {
        Alert exitConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
        exitConfirmation.setTitle("Exit Confirmation");
        exitConfirmation.setHeaderText("Are you sure you want to exit?");
        customiseDialog(exitConfirmation.getDialogPane());
        //Make sure the user answers the dialog
        exitConfirmation.initModality(Modality.APPLICATION_MODAL);
        //Edit the "Ok" button of the dialog
        Button exitButton = (Button) exitConfirmation.getDialogPane().lookupButton(ButtonType.OK);
        exitButton.setText("Exit");


        //Consume the event if the user does not confirm the Exit
        Optional<ButtonType> exitResponse = exitConfirmation.showAndWait();
        if (!ButtonType.OK.equals(exitResponse.get())) {
            event.consume();
        }

    };
    //Method to add an icon to the windows top bar
    public void showBarIcon(Stage stage)
    {
        try {
            // Add a custom icon.
            stage.getIcons().add(new Image(Paths.get("src/SFMS/Views/Images/footballIcon.jpg").toUri().toURL().toExternalForm()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

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
