package SFMS.Models;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

//Custom dialog for requesting a password
public class PasswordDialog extends Dialog<String> {

    //Stores the password entered
    private PasswordField passwordField;

    public PasswordDialog() {
        //Set title and header
        setTitle("Save Confirmation");
        setHeaderText("Please enter your password to save changes made:");

        //Button type
        ButtonType passwordButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(passwordButtonType, ButtonType.CANCEL);

        //password field
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        //HBox to display the field
        HBox hBox = new HBox();
        hBox.getChildren().add(passwordField);
        hBox.setPadding(new Insets(20));
        HBox.setHgrow(passwordField, Priority.ALWAYS);

        //place the content
        getDialogPane().setContent(hBox);

        //Focus on the text field
        Platform.runLater(() -> passwordField.requestFocus());

        //Ensure the answer the of the dialog is the text field
        setResultConverter(dialogButton -> {
            if (dialogButton == passwordButtonType) {
                return passwordField.getText();
            }
            return null;
        });
    }
}