package SFMS.Controllers;

import SFMS.Models.Booking;
import SFMS.Models.DBConnection;
import com.mongodb.client.MongoCollection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

public class ViewBookingController {
    @FXML
    private TextField txtFacName;

    @FXML
    private TextField txtClientName;

    @FXML
    private TextField txtContactNum;

    @FXML
    private TextField txtContactEmail;

    @FXML
    private TextField txtDate;

    @FXML
    private TextField txtTime;

    @FXML
    private TextField txtDuration;

    @FXML
    private TextField txtNumPeople;

    @FXML
    private TextField txtPrice;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnRemove;

    private ObjectId bookingID;
    public void setData(Booking booking)
    {
        bookingID = booking.getBookId();
        txtFacName.setText(booking.getBookFacility());
        txtClientName.setText(booking.getBookClientName());
        txtContactNum.setText(booking.getBookContactNum());
        txtContactEmail.setText(booking.getBookContactEmail());
        txtDate.setText(booking.getBookDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtTime.setText(booking.getBookTime());
        txtDuration.setText(booking.getBookDuration());
        txtNumPeople.setText(String.valueOf( booking.getBookNumPeople()));
        txtPrice.setText("£" + booking.getBookPrice());
    }

    public void btnCloseOnAction()
    {
        Stage bookingStage = (Stage) btnClose.getScene().getWindow();
        bookingStage.close();
    }

    //Removing a booking from the database
    public void btnRemoveOnAction()
    {
        //Entry Confirmation dialog, asks the user for a removal confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Booking Removal Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to remove the displayed booking?");
        customiseDialog(confirmAlert.getDialogPane());

        //Edit the "Ok" button of the dialog to display "Yes" instead
        Button yesButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
        yesButton.setText("Yes");

        confirmAlert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = confirmAlert.showAndWait();

        //Proceed if the user confirms the removal
        if (result.get() == ButtonType.OK) {

            try {
                //Open database connection
                DBConnection db = new DBConnection();
                //Retrieve the bookings collection to remove the booking
                MongoCollection<Document> col = db.collectionRetrieval("Bookings");

                //Remove the booking
                col.deleteOne(eq("_id", bookingID));
                //Close the database connection
                db.close();
                //Let the user know the details have been saved.
                Alert savedAlert = new Alert(Alert.AlertType.INFORMATION);
                savedAlert.setTitle("Booking Removed");
                savedAlert.setHeaderText("The booking has successfully been removed.");
                customiseDialog(savedAlert.getDialogPane());
                savedAlert.setContentText(null);
                savedAlert.showAndWait();

                //Close window to go back to the calendar
                btnCloseOnAction();

            }
            //If for some reason, the booking could not be removed, let the user know
            catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("There was an unknown error when attempting to remove the selected booking");
                errorAlert.setContentText(null);
                customiseDialog(errorAlert.getDialogPane());
                errorAlert.showAndWait();

            }
        }
    }

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
