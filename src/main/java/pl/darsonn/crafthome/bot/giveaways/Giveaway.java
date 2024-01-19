package pl.darsonn.crafthome.bot.giveaways;

import java.time.LocalDateTime;

public class Giveaway {
    private int id;
    private String name;
    private String creatorID;
    private LocalDateTime endDate;
    private int winners;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public int getWinners() {
        return winners;
    }

    public Giveaway(int id, String name, String creatorID, LocalDateTime endDate, int winners) {
        this.id = id;
        this.name = name;
        this.creatorID = creatorID;
        this.endDate = endDate;
        this.winners = winners;
    }
}
