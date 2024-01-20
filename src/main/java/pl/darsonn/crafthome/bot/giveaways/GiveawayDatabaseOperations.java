package pl.darsonn.crafthome.bot.giveaways;

import pl.darsonn.crafthome.bot.database.DatabaseOperations;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GiveawayDatabaseOperations extends DatabaseOperations {
    private final Connection connection;
    private Statement statement;

    public GiveawayDatabaseOperations() {
        connection = super.getConnection();
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
}
