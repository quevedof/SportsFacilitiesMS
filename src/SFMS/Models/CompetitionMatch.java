package SFMS.Models;

import org.bson.Document;
import org.bson.types.ObjectId;

//Matches created when a tournament is created
public class CompetitionMatch {

    private ObjectId matchID;
    private ObjectId tournamentID;
    private ObjectId bookingID;
    private String team1;
    private String team2;
    private String winner;
    private Document score;
    private String extraNotes;


    public CompetitionMatch(ObjectId matchID, ObjectId tournamentID, ObjectId bookingID, String team1, String team2, String winner, Document score, String extraNotes) {
        this.matchID = matchID;
        this.tournamentID = tournamentID;
        this.bookingID = bookingID;
        this.team1 = team1;
        this.team2 = team2;
        this.winner = winner;
        this.score = score;
        this.extraNotes = extraNotes;
    }

    public ObjectId getMatchID() {
        return matchID;
    }

    public void setMatchID(ObjectId matchID) {
        this.matchID = matchID;
    }

    public ObjectId getTournamentID() {
        return tournamentID;
    }

    public void setTournamentID(ObjectId tournamentID) {
        this.tournamentID = tournamentID;
    }

    public ObjectId getBookingID() {
        return bookingID;
    }

    public void setBookingID(ObjectId bookingID) {
        this.bookingID = bookingID;
    }

    public String getTeam1() {
        return team1;
    }

    public void setTeam1(String team1) {
        this.team1 = team1;
    }

    public String getTeam2() {
        return team2;
    }

    public void setTeam2(String team2) {
        this.team2 = team2;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Document getScore() {
        return score;
    }

    public void setScore(Document score) {
        this.score = score;
    }

    public String getExtraNotes() {
        return extraNotes;
    }

    public void setExtraNotes(String extraNotes) {
        this.extraNotes = extraNotes;
    }
}
