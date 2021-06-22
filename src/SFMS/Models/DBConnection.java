package SFMS.Models;




import com.mongodb.*;
import com.mongodb.client.MongoClient;


import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.*;


//Class that handles interaction with the application's database.
public class DBConnection {

    //Declaring variable to store the required database
    private MongoDatabase db ;
    private MongoClient mongoClient;
    public DBConnection()
    {
        try {
            //Initializing a mongoClient object to establish connection to the local MongoDB
            mongoClient = MongoClients.create("mongodb://localhost:27017");
            //Initializing a mongoClient object to establish connection to the cloud database from  MongoDB (cluster)
            // mongoClient = MongoClients.create(
                // "mongodb+srv://fred123:hello123@cluster0.uab1o.mongodb.net/venueSystemDB?retryWrites=true&w=majority");
            //Accessing the required database
             db = mongoClient.getDatabase("venueSystemDB");
        } catch (Exception e) {
            System.out.println("ERROR in establishing connection to the Database.");
        }
    }

    //Method to retrieve any required collection from the database
    public MongoCollection<Document> collectionRetrieval(String Col)
    {
        //Retrieve the required collection and return it
        return db.getCollection(Col);
    }


    //Method that retrieves a specific date's bookings
    public ArrayList<Booking> getDateBookings(LocalDate date)
    {
        //ArrayList to store the list of bookings retrieved from the database
        ArrayList<Booking> bookingList= new ArrayList<>();

        //Retrieve the bookings collection
        MongoCollection<Document> collection = db.getCollection("Bookings");

        //Get the list of documents of bookings that are present in the specified date
        ArrayList<Document> bookingDocsList = collection.find((eq("bookDate", date))).into(new ArrayList<>());

        //Loop through all the documents to initialise booking instances and add them to the booking list
        for (Document booking : bookingDocsList) {

            //Create a new booking instance with the retrieved data
            Booking newBooking = new Booking(
                    (ObjectId) booking.get("_id"),
                    (String) booking.get("bookFacility"),
                    (String) booking.get("bookClientName"),
                    (String) booking.get("bookContactNum"),
                    (String) booking.get("bookContactEmail"),
                    date,
                    (String) booking.get("bookTime"),
                    (String) booking.get("bookDuration"),
                    (int) booking.get("bookNumPeople"),
                    (double) booking.get("bookPrice"),
                    (boolean) booking.get("CheckedIn"));

            //Add the booking instance to the arraylist
            bookingList.add(newBooking);
        }

        //return the arraylist
        return  bookingList;
    }

    //Method to retrieve a specific week's bookings
    public ArrayList<Booking> getBookings (String facName, LocalDate startDate, LocalDate endDate)
    {
        //ArrayList to store the list of bookings retrieved from the database
        ArrayList<Booking> bookingList= new ArrayList<>();

        //Retrieve the bookings collection
        MongoCollection<Document> collection = db.getCollection("Bookings");

        //Get the list of documents of bookings that are present in the specified week
        //It uses the Monday of the selected week as the starting date and the Sunday of the selected week for the ending date
        ArrayList<Document> bookingDocsList = collection.find(and(eq("bookFacility", facName), gte("bookDate", startDate), lte("bookDate", endDate))).into(new ArrayList<>());

        //Loop through all the documents to initialise booking instances and add them to the booking list
        for (Document booking : bookingDocsList) {

            //Format the ISO date received from MongoDB to Local Date
            Date dbDate = (Date) booking.get("bookDate");
            LocalDate bookDate = dbDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            //Create a new booking instance with the retrieved data
            Booking newBooking = new Booking(
                    (ObjectId) booking.get("_id"),
                    (String) booking.get("bookFacility"),
                    (String) booking.get("bookClientName"),
                    (String) booking.get("bookContactNum"),
                    (String) booking.get("bookContactEmail"),
                    bookDate,
                    (String) booking.get("bookTime"),
                    (String) booking.get("bookDuration"),
                    (int) booking.get("bookNumPeople"),
                    (double) booking.get("bookPrice"),
                    (boolean) booking.get("CheckedIn"));

            //Add the booking instance to the arraylist
            bookingList.add(newBooking);
        }

        //return the arraylist
        return  bookingList;
    }

    //Retrieves facilities stored in the database
    public ArrayList<Facility> getFacilities(String filter)
    {
        //ArrayList to store the facilities retrieved
        ArrayList<Facility> facilitiesList = new ArrayList<>();

        //Retrieve the facilities collection
        MongoCollection<Document> facCollection = db.getCollection("Facilities");
        //Arraylist to store the documents retrieved
        ArrayList<Document> facDocList = null;

        //If all facilities is required
        if (filter.equals("All Facilities")) {

             facDocList = facCollection.find().into(new ArrayList<>());
        }
        //If there's a specific filter
        else {
            //Get all the collection into a arraylist
             facDocList = facCollection.find(eq("Activity", filter)).into(new ArrayList<>());
        }

        //Create a new facility instance with the retrieved data and add it to the facilities arraylist
        for (Document facDoc : facDocList) {


            Facility facility = new Facility(
                    (ObjectId) facDoc.get("_id"),
                    (String) facDoc.get("Name"),
                    (String) facDoc.get("Activity"),
                    (String) facDoc.get("Type"),
                    (double) facDoc.get("£/30mins"),
                    (int) facDoc.get("MinPeople"),
                    (int) facDoc.get("MaxPeople"),
                    (String) facDoc.get("ExtraNotes"));

            //Add to the arraylist
            facilitiesList.add(facility);
        }

        //Return the list
        return  facilitiesList;
    }

    //Retrieves tournaments stored in the database based on a filter
    public ArrayList<Tournament> getTournaments(String filter)
    {
        //ArrayList to store the tournaments retrieved
        ArrayList<Tournament> tournamentsList = new ArrayList<>();

        //Retrieve the tournaments collection
        MongoCollection<Document> tourCollection = db.getCollection("Tournaments");
        //Arraylist to store the documents retrieved
        ArrayList<Document> tourDocList = null;

        //If all tournaments is required
        if (filter.equals("All Tournaments")) {

            tourDocList = tourCollection.find().into(new ArrayList<>());
        }
        //If there's a specific filter
        else {
            //Get all the collection into a arraylist
            tourDocList = tourCollection.find(eq("Activity", filter)).into(new ArrayList<>());
        }

        //Create a new tournament instance with the retrieved data and add it to the tournaments arraylist
        for (Document tourDoc : tourDocList) {

            //Format the ISO date received from MongoDB to Local Date
            Date dbdate = (Date) tourDoc.get("DateStart");
            LocalDate dateStart = dbdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            Date dbdate1 = (Date) tourDoc.get("DateEnd");
            LocalDate dateEnd = dbdate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            Tournament tournament = new Tournament(
                    (ObjectId) tourDoc.get("_id"),
                    (String) tourDoc.get("Name"),
                    (String) tourDoc.get("Activity"),
                    (String) tourDoc.get("Type"),
                    dateStart,
                    dateEnd,
                    (String) tourDoc.get("Time"),
                    (String) tourDoc.get("Age"),
                    (int) tourDoc.get("NoTeams"),
                    (String) tourDoc.get("Prize"),
                    (String) tourDoc.get("ExtraNotes")
                    );
            //Add to the arraylist
            tournamentsList.add(tournament);
        }

        //Return the list
        return  tournamentsList;
    }

    //Retrieves leagues stored in the database based on a filter
    public ArrayList<League> getLeagues(String filter)
    {
        //ArrayList to store the leagues retrieved
        ArrayList<League> leaguesList = new ArrayList<>();

        //Retrieve the leagues collection
        MongoCollection<Document> leaguesCollection = db.getCollection("Leagues");
        //Arraylist to store the documents retrieved
        ArrayList<Document> leaguesDocList = null;

        //If all leagues are required
        if (filter.equals("All Leagues")) {

            leaguesDocList = leaguesCollection.find().into(new ArrayList<>());
        }
        //If there's a specific filter
        else {
            //Get all the collection into a arraylist
            leaguesDocList = leaguesCollection.find(eq("Activity", filter)).into(new ArrayList<>());
        }

        //Create a new league instance with the retrieved data and add it to the leagues arraylist
        for (Document leagueDoc : leaguesDocList) {

            //Format the ISO date received from MongoDB to Local Date
            Date dbdate = (Date) leagueDoc.get("DateStart");
            LocalDate dateStart = dbdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            Date dbdate1 = (Date) leagueDoc.get("DateEnd");
            LocalDate dateEnd = dbdate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            League league = new League(
                    (ObjectId) leagueDoc.get("_id"),
                    (String) leagueDoc.get("Name"),
                    (String) leagueDoc.get("Activity"),
                    dateStart,
                    dateEnd,
                    (String) leagueDoc.get("Age"),
                    (ArrayList<String>) leagueDoc.get("Teams")
            );
            //Add to the arraylist
            leaguesList.add(league);
        }

        //Return the list
        return  leaguesList;
    }

    //Returns an arraylist with all matches that are associated with the given tournament id
    public ArrayList<CompetitionMatch> getMatches (ObjectId tournamentID)
    {
        //ArrayList to store the matches retrieved
        ArrayList<CompetitionMatch> matchesList = new ArrayList<>();

        //Retrieve the matches collection
        MongoCollection<Document> matchesCollection = db.getCollection("CompetitionMatches");
        //Arraylist to store the documents retrieved


        ArrayList<Document> matchesDocList = matchesCollection.find(eq("TournamentID", tournamentID)).into(new ArrayList<>());

        //Create a new facility instance with the retrieved data and add it to the facilities arraylist
        for (Document matchDoc : matchesDocList) {

            CompetitionMatch match = new CompetitionMatch(
                    (ObjectId) matchDoc.get("_id"),
                    (ObjectId) matchDoc.get("TournamentID"),
                    (ObjectId) matchDoc.get("BookingID"),
                    (String) matchDoc.get("Team1"),
                    (String) matchDoc.get("Team2"),
                    (String) matchDoc.get("Winner"),
                    (Document) matchDoc.get("Score"),
                    (String) matchDoc.get("ExtraNotes"));

            //Add to the arraylist
            matchesList.add(match);

        }
        return matchesList;
    }

    //Gets the the booking for a particular match
    public Booking getMatchBooking(ObjectId bookingId)
    {

        MongoCollection<Document> bookingCollection = db.getCollection("Bookings");

        Document bookingDoc = bookingCollection.find(eq("_id", bookingId)).first();

        //Format the ISO date received from MongoDB to Local Date
        Date dbDate = (Date) bookingDoc.get("bookDate");
        LocalDate bookDate = dbDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return  new Booking(
                (ObjectId) bookingDoc.get("_id"),
                (String) bookingDoc.get("bookFacility"),
                (String) bookingDoc.get("bookClientName"),
                (String) bookingDoc.get("bookContactNum"),
                (String) bookingDoc.get("bookContactEmail"),
                bookDate,
                (String) bookingDoc.get("bookTime"),
                (String) bookingDoc.get("bookDuration"),
                (int) bookingDoc.get("bookNumPeople"),
                (double) bookingDoc.get("bookPrice"),
                (boolean) bookingDoc.get("CheckedIn"));


    }

    //Returns an arraylist with all clients stored in the database
    public ArrayList<Client> getClients()
    {
        //ArrayList to store the clients retrieved
        ArrayList<Client> clientsList = new ArrayList<>();

        //Retrieve the clients collection
        MongoCollection<Document> clientCol = db.getCollection("Clients");
        //Arraylist to store the documents retrieved
        ArrayList<Document> clientDocList;
        //retrieve the clients from the database into an arraylist
        clientDocList = clientCol.find().into(new ArrayList<>());

        //Create a new client instance with the retrieved data and add it to the clients arraylist
        for (Document clientDoc : clientDocList) {


            Client client = new Client(
                    (ObjectId) clientDoc.get("_id"),
                    (String) clientDoc.get("FirstName"),
                    (String) clientDoc.get("LastName"),
                    (String) clientDoc.get("DoB"),
                    (String) clientDoc.get("Genre"),
                    (String) clientDoc.get("TelNum"),
                    (String) clientDoc.get("Email"),
                    (String) clientDoc.get("ExtraNotes"));

            //Add to the arraylist
            clientsList.add(client);
        }

        //Return the list
        return  clientsList;
    }

    //Returns an arraylist of all teams stored in the database
    public ArrayList<Team> getTeams()
    {
        //ArrayList to store the teams retrieved
        ArrayList<Team> teamsList = new ArrayList<>();

        //Retrieve the teams collection
        MongoCollection<Document> teamsCol = db.getCollection("Teams");
        //Arraylist to store the documents retrieved
        ArrayList<Document> teamsDocList ;
        //retrieve the teams from the database into an arraylist
        teamsDocList = teamsCol.find().into(new ArrayList<>());

        //Create a new team instance with the retrieved data and add it to the teams arraylist
        for (Document teamDoc : teamsDocList) {
            Team team = new Team(
                    (ObjectId) teamDoc.get("_id"),
                    (String) teamDoc.get("Name"),
                    (String) teamDoc.get("Captain"),
                    (int) teamDoc.get("NumMembers"),
                    (ArrayList<String>) teamDoc.get("Members"));

            //Add to the arraylist
            teamsList.add(team);
        }

        //Return the list
        return  teamsList;
    }

    //Retrieve a specific admin
    public Admin getAdmin(ObjectId adminID)
    {
        //Retrieve the admin collectiono
        MongoCollection<Document> adminsCollection = db.getCollection("Admins");

        //Retrieve the admin document
        Document adminDoc = adminsCollection.find(eq("_id", adminID)).first();

        //Instantiate a admin and return it
        return  new Admin(
                (ObjectId) adminDoc.get("_id"),
                (String) adminDoc.get("adminName"),
                (String) adminDoc.get("adminSurname"),
                (String) adminDoc.get("adminEmail"),
                (String) adminDoc.get("adminContactNum"),
                (String) adminDoc.get("adminUsername"),
                (String) adminDoc.get("adminPass"),
                (String) adminDoc.get("adminExtraNotes"));

    }

    //Closes the database connection
    public void close()
    {
        mongoClient.close();
    }

}
