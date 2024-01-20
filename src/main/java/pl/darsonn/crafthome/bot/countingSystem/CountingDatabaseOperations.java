package pl.darsonn.crafthome.bot.countingSystem;

import net.dv8tion.jda.api.entities.Member;
import pl.darsonn.crafthome.bot.database.DatabaseOperations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CountingDatabaseOperations extends DatabaseOperations {
    private final Connection connection;
    private Statement statement;

    public CountingDatabaseOperations() {
        connection = super.getConnection();
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
