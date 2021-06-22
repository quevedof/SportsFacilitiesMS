package SFMS.Controllers;

import SFMS.Models.League;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class ViewLeagueController {
    @FXML
    private Label lblLeagueName;

    @FXML Button btnClose;

    @FXML
    private Label lblTable1;

    @FXML
    private Label lblTable2;

    @FXML
    private Label lblTable3;

    @FXML
    private Label lblTable4;

    @FXML
    private Label lblTable5;

    @FXML
    private Label lblTable6;

    @FXML
    private Label lblTable7;

    @FXML
    private Label lblTable8;

    @FXML
    private Label lblTable9;

    @FXML
    private Label lblTable10;

    @FXML
    private TextField txtTeam1;
    @FXML
    private TextField txtTeam11;
    @FXML
    private TextField txtTeam12;
    @FXML
    private TextField txtTeam13;
    @FXML
    private TextField txtTeam14;

    @FXML
    private TextField txtTeam2;
    @FXML
    private TextField txtTeam21;
    @FXML
    private TextField txtTeam22;
    @FXML
    private TextField txtTeam23;
    @FXML
    private TextField txtTeam24;

    //get the league
    public void setLeagueSelected(League league)
    {
        lblLeagueName.setText(league.getName());
        displayAllTeams(league);
    }

    //Display all teams into their corresponding labels
    public void displayAllTeams(League league)
    {
        lblTable1.setText(league.getTeams().get(0));
        txtTeam1.setText(league.getTeams().get(0));

        lblTable2.setText(league.getTeams().get(1));
        txtTeam2.setText(league.getTeams().get(1));

        lblTable3.setText(league.getTeams().get(2));
        txtTeam11.setText(league.getTeams().get(2));

        lblTable4.setText(league.getTeams().get(3));
        txtTeam21.setText(league.getTeams().get(3));

        lblTable5.setText(league.getTeams().get(4));
        txtTeam12.setText(league.getTeams().get(4));

        lblTable6.setText(league.getTeams().get(5));
        txtTeam22.setText(league.getTeams().get(5));

        lblTable7.setText(league.getTeams().get(6));
        txtTeam13.setText(league.getTeams().get(6));

        lblTable8.setText(league.getTeams().get(7));
        txtTeam23.setText(league.getTeams().get(7));

        lblTable9.setText(league.getTeams().get(8));
        txtTeam14.setText(league.getTeams().get(8));

        lblTable10.setText(league.getTeams().get(9));
        txtTeam24.setText(league.getTeams().get(9));

    }
    //close
    public void btnCloseOnAction()
    {
        Stage newTournamentStage = (Stage) btnClose.getScene().getWindow();
        newTournamentStage.close();
    }

}
