package pl.darsonn.crafthome.bot.giveaways;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GiveawayManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void scheduleGiveaway(Giveaway giveaway) {
        LocalDateTime endTime = giveaway.getEndDate();
        Instant endDate = endTime.atZone(ZoneId.systemDefault()).toInstant();
        Duration duration = Duration.between(Instant.now(), endDate);
        long initialDelayInSeconds = Math.max(0, duration.getSeconds());
        initialDelayInSeconds = Math.abs(initialDelayInSeconds);
        System.out.println(LocalDateTime.now());
        System.out.println(endDate);
        System.out.println(duration.getSeconds());

        System.out.println("Rozpoczęcię giveaway o id: " + giveaway.getId());

        Runnable task = new Runnable() {
            @Override
            public void run() {
                endGiveaway(giveaway);
            }
        };

        System.out.println(duration);
        System.out.println(initialDelayInSeconds);

        scheduler.schedule(task, duration.getSeconds(), TimeUnit.SECONDS);
        scheduler.shutdown();
//        scheduler.schedule(() -> endGiveaway(giveaway),
//                initialDelayInSeconds, TimeUnit.SECONDS);   //TODO
    }

    public void endGiveaway(Giveaway giveaway) {
        int winners = giveaway.getWinners();

        System.out.println("end in manager");

        if(winners > 1) {
            System.out.println("winners > 1");
            GiveawaySystemListener.endGiveaway(
                    giveaway,
                    GiveawayWinnerPicker.chooseMultipleWinners(giveaway.getId(), winners)
            );
        } else {
            System.out.println("winners <= 1");
            GiveawaySystemListener.endGiveaway(
                    giveaway,
                    GiveawayWinnerPicker.chooseOneWinner(giveaway.getId())
            );
        }

    }
}
