package pl.darsonn.crafthome.bot.countingSystem;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.darsonn.crafthome.bot.Utils;
import pl.darsonn.crafthome.bot.database.DatabaseOperations;
import pl.darsonn.crafthome.bot.embedMessagesGenerator.EmbedMessageGenerator;

import java.awt.*;
import java.util.List;

public class CountingSystemListener {
    public static String countingChannelID = Utils.Basics.getCountingChannelID();
//    public static String countingChannelID = "1195704767536705636";
    public static String escapingChatters = "/*";
    private static final CountingDatabaseOperations databaseOperations = new CountingDatabaseOperations();
    private static final EmbedMessageGenerator embedMessageGenerator = new EmbedMessageGenerator();

    public static void newMessage(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();

        if(message.startsWith(escapingChatters)) return;

        if(isSameSenderAsLastMessage(event.getAuthor().getId())) {
            event.getMessage().reply("<@" + event.getAuthor().getId() + ">\nTo nie Twoja kolej!").queue();
            event.getMessage().delete().queue();
            addReactionsToMessage(event, -1);
            return;
        }

        int number = isNumeric(message) ?
                Integer.parseInt(message) :
                MathExpressionEvaluator.evaluateMathExpression(message);

        if(number == -125744) {
            event.getMessage().reply("Zaraz perma dostaniesz kolego za dzielenie przez zero!").queue();
            addReactionsToMessage(event, -1);
            failedCounting(event);
            return;
        }

        if(isNumberLowerOrSameAsLastNumber(number) || number == -125743 || message.startsWith("0")) {
            embedMessageGenerator.sendInvalidNumberException(event, !(number == -1));
            updateInDatabase(1, "bot");
            addReactionsToMessage(event, -1);
            failedCounting(event);
            return;
        }

        if(number == 500) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Gratulacje!");
            embedBuilder.setDescription("""
                    Udało się Wam dojść do wyniku **500** bez żadnego błedu!

                    Zaczynamy od początku!
                    Ja zaczynam od cyfry **1**!""");
            embedBuilder.setColor(Color.MAGENTA);

            event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue(message1 -> {
                message1.addReaction(Emoji.fromUnicode("U+0031 U+20E3")).queue();
            });

            updateInDatabase(1, "bot");
            addReactionsToMessage(event, number);
            return;
        }
        updateInDatabase(number, event.getAuthor().getId());
        addReactionsToMessage(event, number);
    }

    public static void restartCounting(SlashCommandInteractionEvent event) {
        embedMessageGenerator.sendRestartNumberMessage(event);
        updateInDatabase(1, "bot");
    }

    public static void updateInDatabase(int number, String discordID) {
        databaseOperations.updateLastNumber(number);
        databaseOperations.updateLastNumberMember(discordID);
    }

    private static void failedCounting(MessageReceivedEvent event) {
        databaseOperations.updateFailedCounting(event.getMember());

        if(databaseOperations.getMembersCountingFailsCount(event.getMember()) > 3 &&
            !event.getMember().getRoles().contains(event.getGuild().getRoleById("1197295251057016883"))) {
            event.getGuild().addRoleToMember(
                    event.getMember().getUser(),
                    event.getGuild().getRoleById("1197295251057016883")).queue();

            event.getChannel().sendMessage("<@" + event.getMember().getId() +
                    "> Gratulacje!\nWłaśnie uzyskałeś unikatową rolę <@&1197295251057016883>").queue();
        }
    }

    private static boolean isSameSenderAsLastMessage(String discordID) {
        return databaseOperations.getLastNumberMemberFromCounting().equals(discordID);
    }

    private static boolean isNumberLowerOrSameAsLastNumber(int number) {
        return !(databaseOperations.getLastNumberFromCounting()+1 == number);
    }

    private static boolean isNumeric(String message) {
        try {
            int d = Integer.parseInt(message);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static void addReactionsToMessage(MessageReceivedEvent event, int number) {
        String stringNumber = String.valueOf(number);
        String reactionUnicode = null;

        if(number == -1) {
            event.getMessage().addReaction(Emoji.fromUnicode("U+274C")).queue();
            return;
        }

        for(int i = 0; i < stringNumber.length(); i++) {
            switch(stringNumber.charAt(i)) {
                case '0' -> reactionUnicode = "U+0030 U+20E3";
                case '1' -> reactionUnicode = "U+0031 U+20E3";
                case '2' -> reactionUnicode = "U+0032 U+20E3";
                case '3' -> reactionUnicode = "U+0033 U+20E3";
                case '4' -> reactionUnicode = "U+0034 U+20E3";
                case '5' -> reactionUnicode = "U+0035 U+20E3";
                case '6' -> reactionUnicode = "U+0036 U+20E3";
                case '7' -> reactionUnicode = "U+0037 U+20E3";
                case '8' -> reactionUnicode = "U+0038 U+20E3";
                case '9' -> reactionUnicode = "U+0039 U+20E3";
            }
            event.getMessage().addReaction(Emoji.fromUnicode(reactionUnicode)).queue();
        }
    }
}
