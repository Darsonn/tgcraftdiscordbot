package pl.darsonn.crafthome.bot.database;

import net.dv8tion.jda.api.entities.Member;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseOperations {
    private Connection connection;
    private Statement statement;
    private static final Logger logger = Logger.getLogger(DatabaseOperations.class.getName());

    public DatabaseOperations() {
        String request = "jdbc:mysql://localhost:3306/crafthomebot?useUnicode=true&characterEncoding=utf8";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(request, "root", "merkUUry1005");
            logger.log(Level.FINE, "Połączenie z bazą danych jest poprawne");
        } catch (ClassNotFoundException | SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(101);
        }
    }

    public Connection getConnection() {
        String request = "jdbc:mysql://localhost:3306/crafthomebot?useUnicode=true&characterEncoding=utf8";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(request, "root", "merkUUry1005");
            logger.log(Level.FINE, "Połączenie z bazą danych jest poprawne");
            return connection;

        } catch (ClassNotFoundException | SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(101);
            return null;
        }
    }

    public void logError(SQLException e) {
        logger.log(Level.WARNING, "Błąd podczas manipulacji danymi w bazie danych", e);
    }

    public void createApplication(String applicantID, String type, String messageID) {
        String request = "INSERT INTO `applications`(`ID`, `Type`, `ApplicantID`, `Status`, `MessageID`) VALUES (?,?,?,?,?)";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setString(1, null);
            statement.setString(2, type);
            statement.setString(3, applicantID);
            statement.setString(4, "created");
            statement.setString(5, messageID);
            statement.execute();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public String getApplicationMessageID(String applicantID) {
        String request = "SELECT * FROM `applications` WHERE `ApplicantID` = '" + applicantID + "' AND `Status` = 'created'";
        String ticketOpener = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                ticketOpener = rs.getString("MessageID");
            }
            return ticketOpener;
        } catch (SQLException e) {
            logError(e);
            return null;
        }
    }

    public void closeApplication(String applicantID) {
        String request = "DELETE FROM `applications` WHERE `ApplicantID` = ?;";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setString(1, applicantID);
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public String getIDOfOpiekun(String type) {
        String request = "SELECT * FROM `opiekunowie` WHERE `Type` = '" + type + "'";
        String id = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                id = rs.getString("UserID");
            }
            return id;
        } catch (SQLException e) {
            logError(e);
            return null;
        }
    }

    public void updateIDOfOpiekun(String type, String discordID) {
        String request = "UPDATE `opiekunowie` SET `UserID` = ? WHERE `Type` = '" + type + "';";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setString(1, discordID);
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public void insertMemberIntoDatabase(Member member) {
        if(checkIfMemberExistsInDatabase(member)) {
            return;
        }
        String request = "INSERT INTO `members`(`DiscordID`, `MessagesSent`, `FailedCounting`) VALUES (?,?,?)";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setString(1, member.getId());
            statement.setInt(2, 0);
            statement.setInt(3, 0);
            statement.execute();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public boolean checkIfMemberExistsInDatabase(Member member) {
        String request = "SELECT `DiscordID` FROM `members` WHERE `DiscordID` = " + member.getId()+ ";";
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                if(!rs.getString("DiscordID").isEmpty()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            logError(e);
        }
        return false;
    }

    public int getMembersMessagesSent(Member member) {
        String request = "SELECT * FROM `members` WHERE `DiscordID` = " + member.getId()+ ";";
        int messageID = 0;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                messageID = rs.getInt("MessagesSent");
            }
            return messageID;
        } catch (SQLException e) {
            logError(e);
            return 0;
        }
    }

    public void updateSentMessages(Member member) {
        String request = "UPDATE `members` SET `MessagesSent` = ? WHERE `DiscordID` = ?;";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setInt(1, getMembersMessagesSent(member)+1);
            statement.setString(2, member.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }
}
