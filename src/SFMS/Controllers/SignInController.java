package SFMS.Controllers;


import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.mongodb.client.MongoCollection;
import SFMS.Models.DBConnection;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.Pair;
import org.bson.Document;
import org.bson.types.ObjectId;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;


public class SignInController {

    @FXML
    private AnchorPane parentContainer;

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblForgotPass;
    @FXML
    private Pane paneInfo;

    @FXML
    private Button btnSignIn;
    @FXML
    private Button btnQuit;
    @FXML
    private Button btnRegister;

    //Label that will be added to the scene for user information
    private Label lblInfo = new Label();


    public void initialize()
    {
        setLblForgotPassActions();
    }


    public void btnSignInOnAction(ActionEvent event)
    {

        //check if fields are filled
        if (userEntriesValidation())
        {

            //Database object to allow its connection
            DBConnection db = new DBConnection();
            //Retrieve the admin collection
            MongoCollection<Document> col = db.collectionRetrieval("Admins");
            //Retrieve the particular document with the given username
            Document usernameDoc = col.find(eq("adminUsername", txtUsername.getText())).first() ;

            //If the document doesn't exist, let the user know
            if (usernameDoc == null)
            {
                //Let the user know the username does not exist
                lblInfo.setText("Username does not exist. Please try again or Register it.");
                lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-label-padding: 10 0 0 60;");
                paneInfo.getChildren().add(lblInfo);

            }
            else
            {
                //if the credentials are correct, let the user log in
                if (txtPassword.getText().equals(usernameDoc.get("adminPass")))
                {

                    try
                    {
                        //Display the Dashboard window as a new scene in the same stage
                        Stage stage = (Stage) btnSignIn.getScene().getWindow();
                       // URL url = Paths.get("./src/SFMS/Views/DashboardView.fxml").toUri().toURL();
                       // Scene dashboardScene = new Scene(FXMLLoader.load(url));

                        //Get the URL of the FXML document to be displayed a new scene
                        URL url1 = Paths.get("./src/SFMS/Views/DashboardView.fxml").toUri().toURL();
                        //Load the document
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(url1);
                        Parent root = loader.load();

                        //Get the controller of FMXL file to pass some data to be displayed in the new scene
                        DashboardController controller = loader.getController();
                        //Pass required data
                        controller.setUserName(usernameDoc.get("adminName") + " " + usernameDoc.get("adminSurname"), (ObjectId) usernameDoc.get("_id"));

                        Scene dashboardScene = new Scene(root);
                        stage.setScene(dashboardScene);
                        stage.setMaximized(true);




                    }
                    catch  (Exception e)
                    {
                        e.printStackTrace();
                        System.out.println("Error in opening the dashboard");
                    }
                }
                else
                {
                    //Let the user know the password is wrong.
                    lblInfo.setText("Wrong password. Please try again.");
                    lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-label-padding: 10 0 0 130;");
                    paneInfo.getChildren().add(lblInfo);
                }
            }

            //close the db connection
            db.close();
        }
    }

    //Implementation of BtnQuit which closes the application.
    public void btnQuitOnAction (ActionEvent event)
    {

        Stage stage = (Stage) btnQuit.getScene().getWindow();
        //Fire the Window_Close_Request to trigger the exit confirmation dialog
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));

    }

    //Method that opens the Register View
    public void btnRegisterOnAction (ActionEvent event)
    {
        try
        {
            //Get the required scene
            URL url = Paths.get("./src/SFMS/Views/RegistrationView.fxml").toUri().toURL();
            Parent root = FXMLLoader.load(url);
            //Get the current Scene
            Scene scene = btnSignIn.getScene();
            //Animated scene transition
            //Set Y of the new scene to Height of window
            root.translateYProperty().set(scene.getHeight());
            //Add the new scene to the parent container
            parentContainer.getChildren().add(root);

            //Create a new Timeline animation
            Timeline timeline = new Timeline();
            //Animate Y property
            KeyValue kv = new KeyValue(root.translateYProperty(),0, Interpolator.EASE_IN);
            KeyFrame kf = new KeyFrame(Duration.seconds(1), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Method to check if both fields are filled by the user
    public boolean userEntriesValidation()
    {
        //If the label is present, remove it to display a new updated message
        if(paneInfo.getChildren().contains(lblInfo))
        {
            paneInfo.getChildren().remove(lblInfo);
        }

        //Check for empty fields
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty())
        {
            //Inform the user about the error
            lblInfo.setText("Please fill in both fields.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-label-padding: 10 0 0 150;");
            paneInfo.getChildren().add(lblInfo);
            return false;
        }
        else
        {
            return true;
        }

    }

    public void setLblForgotPassActions()
    {
        //Underline the text and change the cursor type when user hovers over the label
        lblForgotPass.setOnMouseEntered( event -> {
            lblForgotPass.setUnderline(true);
            lblForgotPass.setCursor(Cursor.HAND);
        });
        lblForgotPass.setOnMouseExited( event -> {
            lblForgotPass.setUnderline(false);
            lblForgotPass.setCursor(Cursor.DEFAULT);
        });

        //Action when user clicks on the label
        lblForgotPass.setOnMouseClicked(mouseEvent -> {

            //Asks for the user to input their username or email address
            TextInputDialog inputDialog = new TextInputDialog();
            inputDialog.setTitle("Password Reset");
            inputDialog.setHeaderText(null);
            inputDialog.setContentText("Please enter your Username/Email Address:");
            customiseDialog(inputDialog.getDialogPane());
            //Edit the "Ok" button of the dialog to display "Yes" instead
            Button yesButton = (Button) inputDialog.getDialogPane().lookupButton(ButtonType.OK);
            yesButton.setText("Submit");
            //Modality
            inputDialog.initModality(Modality.APPLICATION_MODAL);

            // Get the response value
            Optional<String> result = inputDialog.showAndWait();
            result.ifPresent(this::sendEmail);

        });
    }

    //Sends an email to the user with a OTC
    public void sendEmail(String input)
    {
        //Database object to allow its connection
        DBConnection db = new DBConnection();
        //Retrieve the admin collection
        MongoCollection<Document> col = db.collectionRetrieval("Admins");

        //If user entered an email
        if (input.contains("@"))
        {
            //Try to find a admin with the given email
            Document usernameDoc = col.find(eq("adminEmail", input)).first() ;

            //If the email exists, send the reset password email
            if(usernameDoc != null) {
                //get the adminID
                ObjectId adminID = usernameDoc.getObjectId("_id");
                createAndSendEmail(input, adminID);

            }
            else
            {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("The given email does not exist, please try again.");
                errorAlert.setContentText(null);
                customiseDialog(errorAlert.getDialogPane());
                errorAlert.showAndWait();
            }
        }
        //If the input is the username, get their email address
        else
        {
            //Try to find a admin with the given username
            Document usernameDoc = col.find(eq("adminUsername", input)).first() ;

            //If the email exists, send the reset password email
            if(usernameDoc != null) {

                //get the adminID
                ObjectId adminID = usernameDoc.getObjectId("_id");
                String email = (String) usernameDoc.get("adminEmail");

                createAndSendEmail(email, adminID);

            }
            else
            {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("The given username does not exist, please try again.");
                errorAlert.setContentText(null);
                customiseDialog(errorAlert.getDialogPane());
                errorAlert.showAndWait();
            }
        }
        db.close();
    }

    //Creates and sends an email with a OTC
    public void createAndSendEmail(String email, ObjectId adminID)
    {

        //Sender email ID
        final String username = "missinggchallenger@gmail.com";
        //Sender Email password
        final String password = "immissingg";

        //SMTP properties
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        //Authenticate the sender's credentials
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            //Create a random number code
            String code = randomCode();

            //Form the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("missinggchallenger@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(email));
            message.setSubject("Password Reset");
            message.setText("Your SFMS code to reset your password is: " + code + "\n" +
                    "Yours sincerely,\n The SFMS Team.");

            Transport.send(message);

            //verify the OTC
            verifyOTC(code, adminID);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    //Returns a random 6 digit code for password reset
    public String randomCode()
    {
        //Randomise each digit
        int randomDigit1 = ThreadLocalRandom.current().nextInt(0, 10);
        int randomDigit2 = ThreadLocalRandom.current().nextInt(0, 10);
        int randomDigit3 = ThreadLocalRandom.current().nextInt(0, 10);
        int randomDigit4 = ThreadLocalRandom.current().nextInt(0, 10);
        int randomDigit5 = ThreadLocalRandom.current().nextInt(0, 10);
        int randomDigit6 = ThreadLocalRandom.current().nextInt(0, 10);

        //Return the string
        return String.valueOf(randomDigit1) + randomDigit2 + randomDigit3 +
                randomDigit4 + randomDigit5 + randomDigit6;
    }

    //Asks the user for the OTC code sent to their email
    public void verifyOTC(String code, ObjectId adminID)
    {
        //Asks for the user to input the OTC
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Code Verification");
        inputDialog.setHeaderText(null);
        inputDialog.setContentText("Please enter the 6-digit code sent to your email:");
        customiseDialog(inputDialog.getDialogPane());
        //Edit the "Ok" button of the dialog to display "Submit" instead
        Button yesButton = (Button) inputDialog.getDialogPane().lookupButton(ButtonType.OK);
        yesButton.setText("Submit");
        //Modality
        inputDialog.initModality(Modality.APPLICATION_MODAL);

        // Get the response value
        Optional<String> result = inputDialog.showAndWait();
        result.ifPresent(enteredCode -> {

            //If the code is correct, let the user change the password
            if(enteredCode.equals(code))
            {
                changePasswordDialog(adminID);
            }

            else
            {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Incorrect Code.");
                errorAlert.setContentText(null);
                customiseDialog(errorAlert.getDialogPane());
                errorAlert.showAndWait();
            }

        });
    }

    //Display an input dialog that allows the user to input a new password
    public void changePasswordDialog(ObjectId adminID)
    {
        // Create the custom dialog.
        Dialog<Pair<String, String>> inputDialog = new Dialog<>();
        inputDialog.setTitle("New Password");
        inputDialog.setHeaderText(null);
        customiseDialog(inputDialog.getDialogPane());

        // Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        inputDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        //Modality
        inputDialog.initModality(Modality.APPLICATION_MODAL);

        // Create grid pane, labels and fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField newPass = new PasswordField();
        PasswordField confirmPass = new PasswordField();

        grid.add(new Label("New Password:"), 0, 0);
        grid.add(newPass, 1, 0);
        grid.add(new Label("Confirm Password:"), 0, 1);
        grid.add(confirmPass, 1, 1);

        // Enable/Disable save button depending on whether a password was entered.
        Node saveButton = inputDialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Do some validation.
        newPass.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        //Add the grid to the dialog
        inputDialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(newPass::requestFocus);

        // Convert the result to a password-password-pair when the save button is clicked.
        inputDialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(newPass.getText(), confirmPass.getText());
            }
            return null;
        });

        //Get the result
        Optional<Pair<String, String>> result = inputDialog.showAndWait();
        result.ifPresent(passwords -> {

            //Check both passwords match
            if (passwords.getKey().equals(passwords.getValue()))
            {
                //save new password
                DBConnection db = new DBConnection();
                //Retrieve the admin collection
                MongoCollection<Document> col = db.collectionRetrieval("Admins");
                // update one document
                col.updateOne(eq("_id", adminID), set("adminPass", passwords.getValue()));
                db.close();
                //Let the user know the password has been saved
                Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                savedAlert.setTitle("New Password Saved");
                savedAlert.setHeaderText("The new password has successfully been saved.");
                customiseDialog(savedAlert.getDialogPane());
                savedAlert.setContentText(null);
                savedAlert.showAndWait();
            }
            else
            {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Entered Passwords do not match.");
                errorAlert.setContentText(null);
                customiseDialog(errorAlert.getDialogPane());
                errorAlert.showAndWait();
            }
        });
    }

    //Small customisation of dialogs
    public void customiseDialog(DialogPane dialog)
    {
        //Add a background color and change the font size
        dialog.setStyle("-fx-background-color: #B4D6E4; -fx-font-size: 14px; -fx-font-weight: bold");
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
