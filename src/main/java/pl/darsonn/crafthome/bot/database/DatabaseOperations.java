package pl.darsonn.crafthome.bot.database;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import pl.darsonn.crafthome.bot.giveaways.Giveaway;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    public void createTicket(Member member, String type, TextChannel channel, Timestamp timeOfOpeningTicket) {
        String request = "INSERT INTO `tickets`(`ID`,`DisplayName` , `OpenerID`, `CloserID`, `OpenedTime`, `ClosedTime`, `Type`, `ChannelName`, `ChannelID`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setString(1, null);
            statement.setString(2, member.getEffectiveName());
            statement.setString(3, member.getId());
            statement.setString(4, null);
            statement.setString(5, String.valueOf(timeOfOpeningTicket));
            statement.setString(6, null);
            statement.setString(7, type);
            statement.setString(8, channel.getName());
            statement.setString(9, channel.getId());
            statement.execute();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public void closeTicket(String ticketChannelID, String ticketCloserID, Timestamp closingTime) {
        setClosedTime(ticketChannelID, closingTime);
        setTicketCloser(ticketChannelID, ticketCloserID);
    }

    public Timestamp getTicketCreateDate(String ticketChannelID) {
        String request = "SELECT * FROM tickets WHERE ChannelID = '" + ticketChannelID + "'";
        Timestamp ticketCreateDate = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                ticketCreateDate = rs.getTimestamp("OpenedTime");
            }
            return ticketCreateDate;
        } catch (SQLException e) {
            logError(e);
            return null;
        }
    }

    public String getTicketOpener(String ticketChannelID) {
        String request = "SELECT * FROM tickets WHERE ChannelID = '" + ticketChannelID + "'";
        String ticketOpener = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                ticketOpener = rs.getString("OpenerID");
            }
            return ticketOpener;
        } catch (SQLException e) {
            logError(e);
            return null;
        }
    }

    public void setTicketCloser(String ticketChannelID, String ticketCloserID) {
        String request = "UPDATE tickets SET CloserID = ? WHERE ChannelID = " + ticketChannelID;
        try (final var statement = connection.prepareStatement(request)) {
            statement.setString(1, ticketCloserID);
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public void setClosedTime(String ticketChannelID, Timestamp timestamp) {
        String request = "UPDATE tickets SET ClosedTime = ? WHERE ChannelID = " + ticketChannelID;
        try (final var statement = connection.prepareStatement(request)) {
            statement.setTimestamp(1, timestamp);
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public void cleanDatabaseFromClosedTickets() {
        String request = "DELETE FROM `tickets` WHERE `CloserID` != ?;";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setInt(1, 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
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

    public int getLastNumberFromCounting() {
        String request = "SELECT * FROM `counting`";
        int number = 0;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                number = rs.getInt("number");
            }
            return number;
        } catch (SQLException e) {
            logError(e);
            return -1;
        }
    }

    public String getLastNumberMemberFromCounting() {
        String request = "SELECT * FROM `counting`";
        String ticketOpener = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                ticketOpener = rs.getString("discordID");
            }
            return ticketOpener;
        } catch (SQLException e) {
            logError(e);
            return null;
        }
    }

    public void updateLastNumber(int number) {
        String request = "UPDATE `counting` SET `number` = ?;";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setInt(1, number);
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public void updateLastNumberMember(String discordID) {
        String request = "UPDATE `counting` SET `discordID` = ?;";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setString(1, discordID);
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public List<Giveaway> getGivewaysList() {
        List<Giveaway> giveawaysList = new ArrayList<>();

        String request = "SELECT * FROM `giveawayslist`;";
        try(PreparedStatement preparedStatement = connection.prepareStatement(request)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String creatorID = resultSet.getString("creatorID");
                Timestamp timestamp = resultSet.getTimestamp("enddate");
                LocalDateTime localDateTime = timestamp.toLocalDateTime();
                int winners = resultSet.getInt("winners");

                Giveaway giveaway = new Giveaway(id, name, creatorID, localDateTime, winners);
                giveawaysList.add(giveaway);
            }
        } catch (SQLException e) {
            logError(e);
        }

        return giveawaysList;
    }

    public Giveaway getGiveawayByID(String giveawayID) {
        String request = "SELECT * FROM `giveawayslist` WHERE `id` = '" + giveawayID + "';";

        try(PreparedStatement preparedStatement = connection.prepareStatement(request)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String creatorID = resultSet.getString("creatorID");
                Timestamp timestamp = resultSet.getTimestamp("enddate");
                LocalDateTime localDateTime = timestamp.toLocalDateTime();
                int winners = resultSet.getInt("winners");

                return new Giveaway(id, name, creatorID, localDateTime, winners);
            }
        } catch (SQLException e) {
            logError(e);
        }
        return null;
    }

    public void insertNewGiveaway(Timestamp endTimestamp, String name, int winners, String creatorID) {
        String request = "INSERT INTO `giveawayslist`(`id`, `name`, `creatorID`, `enddate`, `winners`) VALUES (?,?,?,?,?)";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setString(1, null);
            statement.setString(2, name);
            statement.setString(3, creatorID);
            statement.setTimestamp(4, endTimestamp);
            statement.setInt(5, winners);
            statement.execute();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public String getGiveawayIDByCreatorID(String creatorID) {
        String request = "SELECT * FROM `giveawayslist` WHERE `creatorID` = '" + creatorID + "';";
        String id = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                id = rs.getString("id");
            }
            return id;
        } catch (SQLException e) {
            logError(e);
            return null;
        }
    }

    public void createNewGiveawayTable(String id) {
        String request = "CREATE TABLE `giveaway" + id + "`(id int not null auto_increment primary key, memberID varchar(25));";
        try (final var statement = connection.prepareStatement(request)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public void removeGiveawayFromList(int id) {
        String request = "DELETE FROM `giveawayslist` WHERE `id` = ?;";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public void updateGiveawayMessageID(String creatorID, String messageID) {
        String request = "UPDATE `giveawayslist` SET `messageID` = ? WHERE `creatorID` = ?;";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setString(1, messageID);
            statement.setString(2, creatorID);
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public void deleteGiveawayTable(String id) {
        String request = "DROP TABLE `giveaway" + id + "`;";
        try (final var statement = connection.prepareStatement(request)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public int getAmountOfMembersInGiveaway(int id) {
        String request = "SELECT COUNT(memberID) FROM `giveaway" + id + "`;";
        int amount = 0;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                amount = rs.getInt("count(memberID)");
            }
            return amount;
        } catch (SQLException e) {
            logError(e);
            return -1;
        }
    }

    public String getMemberIDByIDFromGiveawayTable(int giveawayID, int ID) {
        String request = "SELECT * FROM `giveaway" + giveawayID + "` WHERE `id` = " + ID + ";";
        String memberID = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                memberID = rs.getString("memberID");
            }
            return memberID;
        } catch (SQLException e) {
            logError(e);
            return null;
        }
    }
    public boolean checkIfMemberIDExistsInGiveaway(int giveawayID, String memberID) {
        String request = "SELECT COUNT(*) AS count FROM `giveaway" + giveawayID + "` WHERE `memberID` = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(request)) {
            preparedStatement.setString(1, memberID);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                int rowCount = rs.getInt("count");
                return rowCount > 0;
            }

            return false;
        } catch (SQLException e) {
            logError(e);
            return false;
        }
    }


    public void addMemberToGiveaway(int giveawayID, String memberID) {
        String request = "INSERT INTO `giveaway" + giveawayID + "`(`id`, `memberID`) VALUES (?,?)";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setString(1, null);
            statement.setString(2, memberID);
            statement.execute();
        } catch (SQLException e) {
            logError(e);
        }
    }

    public String getGiveawayMessageIDByGiveawayID(int giveawayID) {
        String request = "SELECT * FROM `giveawayslist` WHERE `id` = " + giveawayID+ ";";
        String messageID = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                messageID = rs.getString("messageID");
            }
            return messageID;
        } catch (SQLException e) {
            logError(e);
            return null;
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

    public int getMembersCountingFailsCount(Member member) {
        String request = "SELECT * FROM `members` WHERE `DiscordID` = " + member.getId()+ ";";
        int messageID = 0;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(request);
            while (rs.next()) {
                messageID = rs.getInt("FailedCounting");
            }
            return messageID;
        } catch (SQLException e) {
            logError(e);
            return 0;
        }
    }

    public void updateFailedCounting(Member member) {
        String request = "UPDATE `members` SET `FailedCounting` = ? WHERE `DiscordID` = ?;";
        try (final var statement = connection.prepareStatement(request)) {
            statement.setInt(1, getMembersCountingFailsCount(member)+1);
            statement.setString(2, member.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            logError(e);
        }
    }
}
