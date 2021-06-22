package SFMS.Controllers;

import SFMS.Models.Admin;
import SFMS.Models.DBConnection;
import com.mongodb.client.MongoCollection;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.PopupWindow;
import javafx.util.Duration;
import org.bson.Document;
import org.bson.types.ObjectId;


import java.net.URL;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

public class RegistrationController {

    //Controls
    @FXML
    private AnchorPane regContainer;
    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtContactNum;
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtPassword;
    @FXML
    private TextField txtPassword1;
    @FXML
    private Pane paneInfo;
    @FXML
    private Label lblStrength;
    @FXML
    private Button btnBack;
    @FXML
    private ImageView qMark;


    //Label that will be added to the scene for user information
    private Label lblInfo = new Label();
    //Database instance to allow its connection
    private DBConnection db = new DBConnection();


    //Add the tool tip to the image when the scene loads
    public void initialize()
    {
        qmarkToolTip();
    }

    //Take the user back to the sing in view
    public void btnBackOnAction(ActionEvent event) {

        try {
            //Get the required scene
            URL url = Paths.get("./src/SFMS/Views/SignInView.fxml").toUri().toURL();
            Parent root = FXMLLoader.load(url);
            //Get the scene from the button
            Scene scene = btnBack.getScene();

            //Animated scene transition from top to bottom
            root.translateYProperty().set(-1 * scene.getHeight());
            AnchorPane parentContainer = (AnchorPane) scene.getRoot();
            parentContainer.getChildren().add(root);
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(root.translateYProperty(), 0, Interpolator.EASE_IN);
            KeyFrame kf = new KeyFrame(Duration.seconds(1), kv);
            timeline.getKeyFrames().add(kf);
            //Remove the current displayed window
            timeline.setOnFinished(event1 -> {
                parentContainer.getChildren().remove(regContainer);
            });
            timeline.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Method that allows the registration of a new admin
    public void btnRegisterOnAction(ActionEvent event)
    {

        //Check all fields are valid
        if (fieldsValidation()) {

            //Entry Confirmation dialog, asks the user for a saving confirmation
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Saving confirmation");
            confirmAlert.setHeaderText("Are you sure you want to save the given details?");

            //Edit the "Ok" button of the dialog to display "Yes" instead
            Button yesButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
            yesButton.setText("Yes");

            confirmAlert.initModality(Modality.APPLICATION_MODAL);
            Optional<ButtonType> result = confirmAlert.showAndWait();

            //Proceed if the user confirms the saving
            if (result.get() == ButtonType.OK) {

                try {

                    //Initialize an Admin instance with the details the user has entered
                    Admin admin = new Admin(new ObjectId(), txtFirstName.getText(), txtLastName.getText(), txtEmail.getText(), txtContactNum.getText(), txtUsername.getText(), txtPassword.getText(), "");

                    //Retrieve the admin collection to add the new admin
                    MongoCollection<Document> col = db.collectionRetrieval("Admins");

                    //Create the new admin document to be added to the collection
                    Document adminDoc = new Document("_id", admin.getAdminID());
                    adminDoc.append("adminName", admin.getFirstName())
                            .append("adminSurname", admin.getLastName())
                            .append("adminUsername", admin.getUsername())
                            .append("adminPass", admin.getPassword())
                            .append("adminEmail", admin.getEmail())
                            .append("adminContactNum", admin.getContactNum());
                    //save the document in the database
                    col.insertOne(adminDoc);
                    //Close the database connection
                    db.close();
                    //Let the user know the details have been saved.
                    Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                    savedAlert.setTitle("Saved");
                    savedAlert.setHeaderText("Details have successfully been saved.");
                    savedAlert.setContentText(null);
                    savedAlert.showAndWait();

                    //clear all fields
                    clearFields();

                }
                //If for some reason, the details cannot be saved, let the user know
                catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("There was an unknown error when attempting to save the given details.");
                    errorAlert.showAndWait();

                }
            }
        }
    }

    //Method to check if all fields are filled by the user
    public boolean fieldsValidation()
    {
        //If the label is present, remove it to display a new updated message
        if (paneInfo.getChildren().contains(lblInfo)) {
            paneInfo.getChildren().remove(lblInfo);
        }

        //Retrieve the admin collection to check for repeated Admins
        MongoCollection<Document> col = db.collectionRetrieval("Admins");


        //Check for empty fields
        if (txtFirstName.getText().isEmpty() || txtLastName.getText().isEmpty() || txtEmail.getText().isEmpty() || txtContactNum.getText().isEmpty() || txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            //Inform the user about the error
            lblInfo.setText("Please fill in all fields.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-label-padding: 10 0 0 160;");
            paneInfo.getChildren().add(lblInfo);
            return false;
        }

        //Check for already existing email
        else if ( col.find(eq("adminEmail", txtEmail.getText())).first() != null)
        {
            lblInfo.setText("Entered Email already exists.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-label-padding: 10 0 0 150;");
            paneInfo.getChildren().add(lblInfo);
            return false;
        }
        //Check for already existing username
        else if ( col.find(eq("adminUsername", txtUsername.getText())).first() != null)
        {
            lblInfo.setText("Entered Username already exists.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-label-padding: 10 0 0 150;");
            paneInfo.getChildren().add(lblInfo);
            return false;
        }

        //Check the user has entered the same password twice
        else if(!txtPassword.getText().equals(txtPassword1.getText()))
        {
            lblInfo.setText("Re-entered password does not match.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-label-padding: 10 0 0 150;");
            paneInfo.getChildren().add(lblInfo);
            return false;
        }

        //Check the entered email includes '@'
        else if(!txtEmail.getText().contains("@"))
        {
            lblInfo.setText("Please enter a valid email address.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-label-padding: 10 0 0 150;");
            paneInfo.getChildren().add(lblInfo);
            return false;
        }

        //Check if the entered telephone number is too long
        else if (txtContactNum.getText().length() > 14)
        {
            lblInfo.setText("Contact Number is too long. Please check again.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-label-padding: 10 0 0 130;");
            paneInfo.getChildren().add(lblInfo);
            return false;
        }

        //Check if the entered telephone number has any invalid characters
        else if (txtContactNum.getText().matches(".*[a-zA-Z].*"))
        {
            lblInfo.setText("Contact Number cannot contain letters.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-label-padding: 10 0 0 140;");
            paneInfo.getChildren().add(lblInfo);
            return false;
        }
        else {
            //If everything is validated return true.
            return true;
        }
    }

    //Clear all fields of the view
    public void clearFields()
    {
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        txtContactNum.clear();
        txtUsername.clear();
        txtPassword.clear();
        txtPassword1.clear();
    }

    //Method to let the user know the password strength
    public void txtPasswordOnKeyReleased()
    {

        //Boolean variables to record the presence of a type of character
        boolean letter =  false;
        boolean numeric = false;
        boolean sChar = false;
        String pass = txtPassword.getText();

        //Search for letters, numbers and special characters in the password
        for (char c : pass.toCharArray())
        {
            if( Character.isLetter(c))
            {
                letter = true;
            }

            if( Character.isDigit(c))
            {
                numeric = true;
            }
            if( !Character.isLetterOrDigit(c))
            {
                sChar = true;
            }
        }

        //Only letters
        if (letter && !numeric && !sChar)
        {
            lblStrength.setText(null);
            lblStrength.setText("WEAK");
            lblStrength.setFont(Font.font("Calibri", FontWeight.BOLD, 18));
            lblStrength.setStyle("-fx-text-fill:red;");
        }

        //Only numbers
        else if (!letter && numeric && !sChar)
        {
            lblStrength.setText(null);
            lblStrength.setText("WEAK");
            lblStrength.setFont(Font.font("Calibri", FontWeight.BOLD, 18));
            lblStrength.setStyle("-fx-text-fill:red;");
        }

        //Only special characters
        else if (!letter && !numeric && sChar)
        {
            lblStrength.setText(null);
            lblStrength.setText("WEAK");
            lblStrength.setFont(Font.font("Calibri", FontWeight.BOLD, 18));
            lblStrength.setStyle("-fx-text-fill:red;");
        }

        //Only numbers and letters
        else if (letter && numeric && !sChar)
        {
            lblStrength.setText(null);
            lblStrength.setText("AVERAGE");
            lblStrength.setFont(Font.font("Calibri", FontWeight.BOLD, 18));
            lblStrength.setStyle("-fx-text-fill:#FF8C00;");
        }

        //Only letters and special characters
        else if (letter && !numeric && sChar)
        {
            lblStrength.setText(null);
            lblStrength.setText("AVERAGE");
            lblStrength.setFont(Font.font("Calibri", FontWeight.BOLD, 18));
            lblStrength.setStyle("-fx-text-fill:#FF8C00;");
        }

        //Only numbers and special chars
        else if (!letter && numeric && sChar)
        {
            lblStrength.setText(null);
            lblStrength.setText("AVERAGE");
            lblStrength.setFont(Font.font("Calibri", FontWeight.BOLD, 18));
            lblStrength.setStyle("-fx-text-fill:#FF8C00;");
        }

        //Letters, numbers and special characters
        else if (letter && numeric && sChar)
        {
            lblStrength.setText(null);
            lblStrength.setText("STRONG");
            lblStrength.setFont(Font.font("Calibri", FontWeight.BOLD, 18));
            lblStrength.setStyle("-fx-text-fill:green;");
        }
    }

    //Method that adds a tool tip to the question mark to inform the user about the different password strengths
    public void qmarkToolTip()
    {

        Tooltip tp = new Tooltip();
        tp.setStyle("-fx-font: 14px Calibri; -fx-background-color: #000080");
        //Reposition the tooltip
        tp.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_RIGHT);
        //Increase the text show duration to give the user more time to read
        tp.setShowDuration(new Duration(20 * 1000));
        //Decrease the show text delay
        tp.setShowDelay(new Duration (0.3 * 1000));
        //Set the text of the tool tip
        tp.setText("WEAK: Only letters / Numbers / Special Characters \n" +
                "AVERAGE: Letters & Numbers / Letters & S. Char / Numbers & S. Chars \n" +
                "STRONG: Letters & Numbers & Special Characters");

        //Add the tool tip to the question mark on the view
        Tooltip.install(qMark, tp);

    }
}
