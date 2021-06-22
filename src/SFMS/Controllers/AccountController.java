package SFMS.Controllers;

import SFMS.Models.Admin;
import SFMS.Models.DBConnection;
import SFMS.Models.PasswordDialog;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.nio.file.Paths;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

public class AccountController {

    @FXML
    private AnchorPane passwordPane;

    @FXML
    private Label lblInfo;

    @FXML
    private Label lblInfo1;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSurname;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTelNumber;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextArea txtExtraNotes;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnEdit;
    @FXML
    private PasswordField txtOldPass;

    @FXML
    private PasswordField txtNewPass;

    @FXML
    private PasswordField txtConfirmPass;

    @FXML
    private Button btnSavePass;

    @FXML
    private Button btnChangePass;

    private Admin displayedAdmin;

    //Gets the admin
    public void setAdmin(ObjectId adminID)
    {
        //Get the admin from the database
        DBConnection db = new DBConnection();
        displayedAdmin = db.getAdmin(adminID);
        db.close();
        displayAdminDetails();
    }



    //Saves any changes made to the account details
    @FXML
    public void btnSaveOnAction() {

        if(fieldsValidation())
        {
            //Get the customised password dialog to ask for the user password
            PasswordDialog passwordDialog = new PasswordDialog();
            customiseDialog(passwordDialog.getDialogPane());
            passwordDialog.initModality(Modality.APPLICATION_MODAL);
            Optional<String> result = passwordDialog.showAndWait();

            // Get the response value
            //Optional<String> result = inputDialog.showAndWait();
            result.ifPresent(enteredPass -> {

                //If the password is correct, save changes
                if(enteredPass.equals(displayedAdmin.getPassword()))
                {
                    try {
                    //Open database connection
                    DBConnection db = new DBConnection();
                    //Retrieve the admins collection to update the admin
                    MongoCollection<Document> col = db.collectionRetrieval("Admins");

                    //Create the updated admin document
                    Document adminDoc = new Document();
                    adminDoc.append("adminName", txtName.getText())
                            .append("adminSurname", txtSurname.getText())
                            .append("adminUsername", txtUsername.getText())
                            .append("adminPass", txtPassword.getText())
                            .append("adminEmail", txtEmail.getText())
                            .append("adminContactNum", txtTelNumber.getText())
                            .append("adminExtraNotes", txtExtraNotes.getText());


                    //update document
                    Document update = new Document("$set", adminDoc);
                    //Request the new version of the document after the update
                    FindOneAndUpdateOptions optionAfter = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
                    //update the document in the database
                    Document updatedAdmin = col.findOneAndUpdate(eq("_id", displayedAdmin.getAdminID()), update, optionAfter);
                    //Close the database connection
                    db.close();
                    //Display the new version of the document
                    displayedAdmin = new Admin(
                            (ObjectId) updatedAdmin.get("_id"),
                            (String) updatedAdmin.get("adminName"),
                            (String) updatedAdmin.get("adminSurname"),
                            (String) updatedAdmin.get("adminEmail"),
                            (String) updatedAdmin.get("adminContactNum"),
                            (String) updatedAdmin.get("adminUsername"),
                            (String) updatedAdmin.get("adminPass"),
                            (String) updatedAdmin.get("adminExtraNotes"));

                    displayAdminDetails();
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

                else
                {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Incorrect Password.");
                    errorAlert.setContentText(null);
                    customiseDialog(errorAlert.getDialogPane());
                    errorAlert.showAndWait();
                }

            });
        }
    }

    //Allows the edition of the account details
    @FXML
    public void btnEditOnAction()
    {
        if(btnEdit.getText().equals("Edit"))
        {
            enableControls();
            btnEdit.setText("Cancel");
        }
        else
        {
            disableControls();
            displayAdminDetails();
        }
    }


    //Allows the user to change the password
    @FXML
    public void btnChangePassOnAction(){

        if(btnChangePass.getText().equals("Change"))
        {
            passwordPane.setVisible(true);
            btnChangePass.setText("Cancel");
        }
        else
        {
            txtOldPass.clear();
            txtNewPass.clear();
            txtConfirmPass.clear();
            passwordPane.setVisible(false);
            btnChangePass.setText("Change");
            lblInfo1.setText("");
        }

    }

    //Saves new password
    @FXML
    public void btnSavePassOnAction(){
        //Check the old password is correct
        if (validateNewPassword()) {

            //Entry Confirmation dialog, asks the user for a saving confirmation
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Saving confirmation");
            confirmAlert.setHeaderText("Are you sure you want to change the password?");
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
                    //Retrieve the admins collection to update the admin
                    MongoCollection<Document> col = db.collectionRetrieval("Admins");

                    //Create the updated admin document
                    Document adminDoc = new Document();
                    adminDoc.append("adminPass", txtNewPass.getText());

                    //update document
                    Document update = new Document("$set", adminDoc);
                    //Request the new version of the document after the update
                    FindOneAndUpdateOptions optionAfter = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
                    //update the document in the database
                    Document updatedAdmin = col.findOneAndUpdate(eq("_id", displayedAdmin.getAdminID()), update, optionAfter);
                    //Close the database connection
                    db.close();
                    //Display the new version of the document
                    displayedAdmin = new Admin(
                            (ObjectId) updatedAdmin.get("_id"),
                            (String) updatedAdmin.get("adminName"),
                            (String) updatedAdmin.get("adminSurname"),
                            (String) updatedAdmin.get("adminEmail"),
                            (String) updatedAdmin.get("adminContactNum"),
                            (String) updatedAdmin.get("adminUsername"),
                            (String) updatedAdmin.get("adminPass"),
                            (String) updatedAdmin.get("adminExtraNotes"));

                    displayAdminDetails();
                    //Let the user know the details have been saved.
                    Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                    savedAlert.setTitle("Saved");
                    savedAlert.setHeaderText("Password has successfully been updated.");
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

    //Validate the password change inputs
    public boolean validateNewPassword()
    {
        //Check for empty fields
        if (txtOldPass.getText().isEmpty() || txtNewPass.getText().isEmpty() || txtConfirmPass.getText().isEmpty()) {
            //Inform the user about the error
            lblInfo1.setText("Please fill in all fields.");
            lblInfo1.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //Ensure the old password matches the stored password
        else if (!txtOldPass.getText().equals(displayedAdmin.getPassword()))
        {
            lblInfo1.setText("Old password does not match stored password,\n please try again.");
            lblInfo1.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }
        //Ensure the new pass and confirm pass match
        else if (!txtNewPass.getText().equals(txtConfirmPass.getText()))
        {
            lblInfo1.setText("New password does not match confirm password,\n please try again.");
            lblInfo1.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        else
        {
            return true;
        }
    }

    //Method validating any changes made
    public boolean fieldsValidation()
    {

        //Retrieve the admin collection to check for repeated Admins
        DBConnection db = new DBConnection();
        MongoCollection<Document> col = db.collectionRetrieval("Admins");

        //Check for empty fields
        if (txtName.getText().isEmpty() || txtSurname.getText().isEmpty() || txtEmail.getText().isEmpty() || txtTelNumber.getText().isEmpty() || txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            //Inform the user about the error
            lblInfo.setText("Please fill in all fields.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Check for already existing email
        if ( col.find(eq("adminEmail", txtEmail.getText())).first() != null)
        {
            //Ensure the repeated email is not from the displayed admin
            if(!txtEmail.getText().equals(displayedAdmin.getEmail()))
            {
                lblInfo.setText("Entered Email already exists.");
                lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                return false;
            }
        }
        //Check for already existing username
        if ( col.find(eq("adminUsername", txtUsername.getText())).first() != null)
        {
            //Ensure the repeated email is not from the displayed admin
            if(!txtUsername.getText().equals(displayedAdmin.getUsername()))
            {
                lblInfo.setText("Entered username already exists.");
                lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                return false;
            }
        }

        //Check the entered email includes '@'
        if(!txtEmail.getText().contains("@"))
        {
            lblInfo.setText("Please enter a valid email address.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            return false;
        }

        //Check if the entered telephone number is too long
        else if (txtTelNumber.getText().length() > 14)
        {
            lblInfo.setText("Contact Number is too long. Please check again.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; ");
            return false;
        }

        //Check if the entered telephone number has any invalid characters
        else if (txtTelNumber.getText().matches(".*[a-zA-Z].*"))
        {
            lblInfo.setText("Contact Number cannot contain letters.");
            lblInfo.setStyle("-fx-font-size: 16; -fx-text-fill: red; ");
            return false;
        }
        //If everything is validated return true.
        return true;
    }

    //Displays all details of the signed in admin
    public void displayAdminDetails()
    {
        txtName.setText(displayedAdmin.getFirstName());
        txtSurname.setText(displayedAdmin.getLastName());
        txtEmail.setText(displayedAdmin.getEmail());
        txtTelNumber.setText(displayedAdmin.getContactNum());
        txtUsername.setText(displayedAdmin.getUsername());
        txtPassword.setText(displayedAdmin.getPassword());
        txtExtraNotes.setText(displayedAdmin.getExtraNotes());

        disableControls();
    }

    //Disable not needed controls
    public void disableControls()
    {
        txtName.setDisable(true);
        txtSurname.setDisable(true);
        txtEmail.setDisable(true);
        txtTelNumber.setDisable(true);
        txtUsername.setDisable(true);
        txtPassword.setDisable(true);
        txtExtraNotes.setDisable(true);


        btnSave.setDisable(true);
        btnEdit.setText("Edit");
        btnChangePass.setText("Change");
        passwordPane.setVisible(false);
        lblInfo.setText("");
    }

    //Enable controls when they're needed
    public void enableControls()
    {
        txtName.setDisable(false);
        txtSurname.setDisable(false);
        txtEmail.setDisable(false);
        txtTelNumber.setDisable(false);
        txtUsername.setDisable(false);
        txtExtraNotes.setDisable(false);

        btnSave.setDisable(false);
    }

    //Customise small window
    public void customiseDialog(DialogPane dialog)
    {
        //Add a background color and change the font size
        dialog.setStyle("-fx-background-color:   #98FB98; -fx-font-size: 14px; -fx-font-weight: bold");
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
