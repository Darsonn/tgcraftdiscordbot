package pl.darsonn.crafthome.bot.ticketSystem;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import pl.darsonn.crafthome.bot.database.DatabaseOperations;

import java.sql.*;

public class TicketsDatabaseOperations extends DatabaseOperations {
    private final Connection connection;
    private Statement statement;

    public TicketsDatabaseOperations() {
        connection = super.getConnection();
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
}
