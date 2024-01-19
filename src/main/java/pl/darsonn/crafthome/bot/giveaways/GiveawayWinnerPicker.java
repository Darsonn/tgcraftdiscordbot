package pl.darsonn.crafthome.bot.giveaways;

import pl.darsonn.crafthome.bot.database.DatabaseOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GiveawayWinnerPicker {
    private static final Random random = new Random();
    private static final DatabaseOperations databaseOperations = new DatabaseOperations();
    public static String chooseOneWinner(int id) {
        String winner = databaseOperations.getMemberIDByIDFromGiveawayTable(
                id,
                random.nextInt(0,
                        databaseOperations.getAmountOfMembersInGiveaway(id)
                )+1
        );
        System.out.println("winner: " + winner);
        return winner;
    }

    public static List<String> chooseMultipleWinners(int id, int amount) { //TODO
        List<String> winners = new ArrayList<>();

        int amountOfMembersInGiveaway = databaseOperations.getAmountOfMembersInGiveaway(id);
        if(amountOfMembersInGiveaway <= amount) {
            for(int i = 0; i < amountOfMembersInGiveaway; i++) {
                winners.add(databaseOperations.getMemberIDByIDFromGiveawayTable(id, i));
            }

            return winners;
        }

        for(int i = 0; i < amount; i++) {
            String memberID = databaseOperations.getMemberIDByIDFromGiveawayTable(
                    id,
                    random.nextInt(0,
                            databaseOperations.getAmountOfMembersInGiveaway(id)
                    )
            );

            if(!winners.isEmpty()) {
                if(winners.contains(memberID)) i--;
                else {
                    winners.add(memberID);
                }
            }
        }

        return winners;
    }
}
