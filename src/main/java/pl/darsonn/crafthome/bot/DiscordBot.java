package pl.darsonn.crafthome.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import pl.darsonn.crafthome.bot.commands.CommandsCreator;
import pl.darsonn.crafthome.bot.commands.EventListener;
import pl.darsonn.crafthome.bot.database.DatabaseOperations;
import pl.darsonn.crafthome.bot.embedMessagesGenerator.EmbedMessageGenerator;
import pl.darsonn.crafthome.bot.giveaways.GiveawaySystemListener;

import java.util.EnumSet;

public class DiscordBot {
    public static JDA bot;
    public static boolean isUnderAttack;
    public static DatabaseOperations databaseOperations;
    public EmbedMessageGenerator embedMessageGenerator;
    public void startBot(String token) {
        bot = JDABuilder.createLight(token, EnumSet.noneOf(GatewayIntent.class))
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS
                )
                .setActivity(Activity.playing(Utils.Basics.getServerName() + ".pl"))
                .addEventListeners(
                        new EventListener(),
                        new GiveawaySystemListener())
                .build();

        CommandsCreator commandsCreator = new CommandsCreator();
        commandsCreator.createCommands(bot);

        databaseOperations = new DatabaseOperations();
        databaseOperations.cleanDatabaseFromClosedTickets();

        embedMessageGenerator = new EmbedMessageGenerator();

        GiveawaySystemListener.checkForActiveGiveaways();

        isUnderAttack = false;
    }

    public static boolean getIsUnderAttack() {
        return isUnderAttack;
    }

    public static void setIsUnderAttack(boolean isUnderAttack) {
        DiscordBot.isUnderAttack = isUnderAttack;
    }
}
