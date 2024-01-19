package pl.darsonn.crafthome.bot.giveaways;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import pl.darsonn.crafthome.bot.DiscordBot;
import pl.darsonn.crafthome.bot.database.DatabaseOperations;
import pl.darsonn.crafthome.bot.embedMessagesGenerator.EmbedMessageGenerator;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GiveawaySystemListener extends ListenerAdapter {
//    public static final String giveawaysChannelID = "1195426077561061506";

    public static final String giveawaysChannelID = "1195704767536705636";
    private static final EmbedMessageGenerator embedMessageGenerator = new EmbedMessageGenerator();
    private static final DatabaseOperations databaseOperations = new DatabaseOperations();

    public static void checkForActiveGiveaways() {
        List<Giveaway> giveaways = databaseOperations.getGivewaysList();

        if(!giveaways.isEmpty()) {
            GiveawayManager giveawayManager;

            for (Giveaway giveaway : giveaways) {
                giveawayManager = new GiveawayManager();
                giveawayManager.scheduleGiveaway(giveaway);
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("giveaways")) {
            showGiveawaysOptions(event);
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if(event.getComponentId().equals("choose-giveaway-operation"))
            selectInteractionHandler(event);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(event.getComponent().getId().startsWith("end-giveaway")) {
            endGiveawayModalCreator(event);
        }
        else if(event.getComponent().getId().startsWith("delete-giveaway")) {
            deleteGiveawayModalCreator(event);
        } else if(event.getComponent().getId().equals("reload-giveaway")) {
            sendGiveawaysList(event,
                    databaseOperations.getGivewaysList(),
                    getPermissionsLevel(event.getMember()));
        } else if(event.getComponent().getId().startsWith("join-giveaway+")) {
            int giveawayID = Integer.parseInt(event.getComponent().getId().substring("join-giveaway+".length()));

            if(databaseOperations.checkIfMemberIDExistsInGiveaway(
                    giveawayID,
                    event.getMember().getId()
            )) {
                event.reply("Nie możesz dołączyć drugi raz do tego samego konkursu!")
                        .setEphemeral(true)
                        .queue();
            } else {
                event.reply("Pomyślnie dołączono do konkursu!");

                databaseOperations.addMemberToGiveaway(giveawayID, event.getMember().getId());
                updateGiveawayMessage(event, giveawayID);
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if(event.getModalId().equals("giveaway-creation-modal")) {
            String duration = event.getValue("duration").getAsString();
            String name = event.getValue("name").getAsString();
            String winners = event.getValue("winners").getAsString();

            if(!isNumeric(winners)) {
                event.reply("Wartość wygrani musi być liczbą!").setEphemeral(true).queue();
                return;
            }

            long seconds = convertStringToSeconds(duration);
            if(seconds == -1) {
                event.reply("Błędnie podana jednostka czasu trwania!").setEphemeral(true).queue();
                return;
            }

            long time = calculateFutureTimestamp(seconds);

            event.reply("Pomyślnie utworzono konkurs o nazwie: **" + name +
                    "** który zakończy się <t:" + time +
                    ">").setEphemeral(true).queue();

            databaseOperations.insertNewGiveaway(
                    new Timestamp(time),
                    name,
                    Integer.parseInt(winners),
                    event.getMember().getId()
            );

            databaseOperations.createNewGiveawayTable(
                    databaseOperations.getGiveawayIDByCreatorID(
                            event.getMember().getId()
                    )
            );

            sendGiveawayMessage(event, time);

            GiveawayManager giveawayManager = new GiveawayManager();
            giveawayManager.scheduleGiveaway(
                    databaseOperations.getGiveawayByID(
                            databaseOperations.getGiveawayIDByCreatorID(
                                    event.getMember().getId()
                            )
                    )
            );
        } else if(event.getModalId().equals("giveaway-delete-modal")) {
            String sure = event.getValue("sure").getAsString();
            String stringId = event.getValue("id").getAsString();

            if(!isNumeric(stringId)) {
                event.reply("Nie udało się usunąć konkursu! Wprowadzone ID jest niepoprawne.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            int id = Integer.parseInt(stringId);

            if(sure.equalsIgnoreCase("tak")) {
                event.reply("Pomyślnie usunięto konkurs o ID: **" + id + "**")
                        .setEphemeral(true)
                        .queue();

                endGiveaway(
                        databaseOperations.getGiveawayByID(
                            databaseOperations.getGiveawayIDByCreatorID(
                                event.getMember().getId()
                            )
                        )
                );
            } else {
                event.reply("Nie usunięto konkursu z powodu niepotwierdzenia w oknie dialogowym")
                        .setEphemeral(true)
                        .queue();
            }
        } else if(event.getModalId().equals("giveaway-end-modal")) {
            String sure = event.getValue("sure").getAsString();
            String stringId = event.getValue("id").getAsString();

            if (!isNumeric(stringId)) {
                event.reply("Nie udało się zakończyć konkursu! Wprowadzone ID jest niepoprawne.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            int id = Integer.parseInt(stringId);

            if (sure.equalsIgnoreCase("tak")) {
                event.reply("Pomyślnie zakończono konkurs o ID: **" + id + "**")
                        .setEphemeral(true)
                        .queue();

                GiveawayManager giveawayManager = new GiveawayManager();
                giveawayManager.endGiveaway(
                        databaseOperations.getGiveawayByID(
                                databaseOperations.getGiveawayIDByCreatorID(
                                        event.getMember().getId()
                                )
                        )
                );
                databaseOperations.removeGiveawayFromList(id);
            } else {
                event.reply("Nie zakończono konkursu z powodu niepotwierdzenia w oknie dialogowym")
                        .setEphemeral(true)
                        .queue();
            }
        }
    }

    public static void endGiveaway(Giveaway giveaway) {
        databaseOperations.removeGiveawayFromList(giveaway.getId());
        databaseOperations.deleteGiveawayTable(String.valueOf(giveaway.getId()));
    }
    public static void endGiveaway(Giveaway giveaway, String winnerID) {
        System.out.println("end");
        EmbedBuilder embedBuilder = new EmbedBuilder();
        TextChannel textChannel = DiscordBot.bot.getTextChannelById(giveawaysChannelID);
        System.out.println(databaseOperations.getGiveawayMessageIDByGiveawayID(
                giveaway.getId()
        ));

        textChannel.retrieveMessageById(databaseOperations.getGiveawayMessageIDByGiveawayID(
                giveaway.getId()
        )).queue((message -> {
            System.out.println(message.getAuthor().getName() + " " + message.getId());
            embedBuilder.setTitle(message.getEmbeds().get(0).getTitle());
            embedBuilder.setColor(Color.MAGENTA);
            for(int i = 0; i < 4; i++) {
                System.out.println("i = " + i);
                if(i == 3) {
                    System.out.println("in i = 3");
                    embedBuilder.addField("Zwycięzca: ",
                            "<@" + winnerID + ">", false);
                } else {
                    System.out.println("out of j");
                    embedBuilder.addField(message.getEmbeds().get(0).getFields().get(i).getName(),
                            message.getEmbeds().get(0).getFields().get(i).getValue(), false);
                }
            }
            System.out.println("Out of for");
            embedBuilder.setTimestamp(Instant.now());

            System.out.println("Before edit");
            message.editMessageEmbeds(embedBuilder.build()).queue();
        }));

        System.out.println("Before remove");

        databaseOperations.removeGiveawayFromList(giveaway.getId());
        databaseOperations.deleteGiveawayTable(String.valueOf(giveaway.getId()));
    }
    public static void endGiveaway(Giveaway giveaway, List<String> winnersID) {
        System.out.println("endGiveaway in systemListener");

        EmbedBuilder embedBuilder = new EmbedBuilder();
        TextChannel textChannel = DiscordBot.bot.getTextChannelById(giveawaysChannelID);


        textChannel.retrieveMessageById(databaseOperations.getGiveawayMessageIDByGiveawayID(
                giveaway.getId()
        )).queue((message -> {
            embedBuilder.setTitle(message.getEmbeds().get(0).getTitle());
            embedBuilder.setColor(Color.MAGENTA);
            for(int i = 0; i < 4; i++) {
                if(i == 3) {
                    String winners = "";
                    for (String string : winnersID) {
                        winners += "<@" + string + ">\n";
                    }
                    embedBuilder.addField("Zwycięzcy: ",
                            winners, false);
                } else {
                    embedBuilder.addField(message.getEmbeds().get(0).getFields().get(i).getName(),
                            message.getEmbeds().get(0).getFields().get(i).getValue(), false);
                }
            }
            embedBuilder.setTimestamp(Instant.now());

            message.editMessageEmbeds(embedBuilder.build()).queue();
        }));

        databaseOperations.removeGiveawayFromList(giveaway.getId());
        databaseOperations.deleteGiveawayTable(String.valueOf(giveaway.getId()));
    }

    private static void showGiveawaysOptions(SlashCommandInteractionEvent event) {
        event.reply("Wybierz co chcesz wykonać")
                .addActionRow(createStringSelectMenu(getPermissionsLevel(event.getMember())))
                .setEphemeral(true)
                .queue();
    }

    private static void selectInteractionHandler(StringSelectInteractionEvent event) {
        String value = event.getInteraction().getSelectedOptions().get(0).getValue();

        switch(value) {
            case "show-giveaways" -> {
                sendGiveawaysList(
                        event,
                        databaseOperations.getGivewaysList(),
                        getPermissionsLevel(event.getMember()));
            }
            case "create-giveaway" -> {
                createGiveawayCreationModal(event);
            }
        }
    }

    private static int getPermissionsLevel(Member member) {
        int permissions = 0;

//        if(member.getRoles().contains(event.getJDA().getRoleById("1175928484200202330")))
//            permissions = 1;    //TgCraft Team

        if(member.getRoles().contains(member.getJDA().getRoleById("1175836198573453353")) ||  //Właściciel
                member.getRoles().contains(member.getJDA().getRoleById("1187138676589858926")) ||     //Współwłaściciel
                member.getRoles().contains(member.getJDA().getRoleById("1187138851978878997")))       //Zarząd
            permissions = 1;    //Zarząd

        return permissions;
    }

    private static long convertStringToSeconds(String input) {
        long totalSeconds = 0;

        // Pattern do dopasowania liczb i jednostek czasu
        Pattern pattern = Pattern.compile("(\\d+)([smhDd])");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "s":
                    totalSeconds += value;
                    break;
                case "m":
                    totalSeconds += value * 60;
                    break;
                case "h":
                    totalSeconds += value * 3600;
                    break;
                case "D":
                case "d":
                    totalSeconds += value * 86400;
                    break;
                default:
                    System.err.println("Nieznana jednostka czasu: " + unit);
                    return -1;
            }
        }

        return totalSeconds;
    }

    private static StringSelectMenu createStringSelectMenu(int permissions) {
        StringSelectMenu.Builder builder = StringSelectMenu.create("choose-giveaway-operation");

        if(permissions == 1) {
            builder.addOption("Zobacz wszystkie aktywne konkursy", "show-giveaways");
            builder.addOption("Utwórz nowy konkurs", "create-giveaway");
        } else {
            builder.addOption("Zobacz wszystkie aktywne konkursy", "show-giveaways");
        }

        return builder.build();
    }

    public static long calculateFutureMillis(long secondsToAdd) {
        long currentTimeMillis = Instant.now().toEpochMilli();
        long futureTimeMillis = currentTimeMillis + (secondsToAdd * 1000);

        return futureTimeMillis;
    }

    public static Timestamp getTimestampFromMillis(long millis) {
        return new Timestamp(millis);
    }

    public static long calculateFutureTimestamp(long secondsToAdd) {
        Instant instant = Instant.now();
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneOffset systemOffset = systemZone.getRules().getOffset(instant);

        long currentTimeMillis = instant.toEpochMilli();
        long futureTimeMillis = currentTimeMillis + (secondsToAdd * 1000);

        return new Timestamp(futureTimeMillis).toLocalDateTime().toEpochSecond(systemOffset);
    }

    private static boolean isNumeric(String value) {
        try {
            double d = Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static void sendGiveawaysList(StringSelectInteractionEvent event, List<Giveaway> giveawaysList, int permissions) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Lista aktywnych konkursów");
        embedBuilder.setColor(Color.MAGENTA);

        if(giveawaysList.isEmpty()) {
            embedBuilder.setDescription("Aktualnie brak aktywnych konkursów");
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            return;
        }

        for(int i = 0; i < giveawaysList.size(); i++) {
            embedBuilder.addField("Tytuł", giveawaysList.get(i).getName(), false);
            embedBuilder.addField("ID", String.valueOf(giveawaysList.get(i).getId()), false);
            embedBuilder.addField("Utworzone przez", String.valueOf(giveawaysList.get(i).getCreatorID()), false);
            embedBuilder.addField("Kończy się", "<t:" + giveawaysList.get(i).getEndDate().toEpochSecond(ZoneOffset.UTC) + ">", false);
            embedBuilder.addField("Ilość wygranych", String.valueOf(giveawaysList.get(i).getWinners()), false);

            embedBuilder.addBlankField(false);
        }

        if(permissions == 0) {
            event.replyEmbeds(embedBuilder.build())
                    .setEphemeral(true)
                    .queue();

            return;
        }

        event.replyEmbeds(embedBuilder.build())
                .addActionRow(
                        Button.success("reload-giveaway", "Odśwież"),
                        Button.primary("end-giveaway", "Zakończ konkurs"),
                        Button.danger("delete-giveaway", "Usuń konkurs")
                )
                .setEphemeral(true)
                .queue();
    }

    private static void sendGiveawaysList(ButtonInteractionEvent event, List<Giveaway> giveawaysList, int permissions) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Lista aktywnych konkursów");
        embedBuilder.setColor(Color.MAGENTA);

        if(giveawaysList.isEmpty()) {
            embedBuilder.setDescription("Aktualnie brak aktywnych konkursów");
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            return;
        }

        for(int i = 0; i < giveawaysList.size(); i++) {
            embedBuilder.addField("Tytuł", giveawaysList.get(i).getName(), false);
            embedBuilder.addField("ID", String.valueOf(giveawaysList.get(i).getId()), false);
            embedBuilder.addField("Utworzone przez", String.valueOf(giveawaysList.get(i).getCreatorID()), false);
            embedBuilder.addField("Kończy się", "<t:" + giveawaysList.get(i).getEndDate().toEpochSecond(ZoneOffset.UTC) + ">", false);
            embedBuilder.addField("Ilość wygranych", String.valueOf(giveawaysList.get(i).getWinners()), false);

            embedBuilder.addBlankField(false);
        }

        if(permissions == 0) {
            event.replyEmbeds(embedBuilder.build())
                    .setEphemeral(true)
                    .queue();

            return;
        }

        if(giveawaysList.size() < 2) {
            event.replyEmbeds(embedBuilder.build())
                    .addActionRow(
                            Button.success("reload-giveaway", "Odśwież"),
                            Button.primary("end-giveaway-" + giveawaysList.get(0).getId(), "Zakończ konkurs"),
                            Button.danger("delete-giveaway-" + giveawaysList.get(0).getId(), "Usuń konkurs")
                    )
                    .setEphemeral(true)
                    .queue();
        } else {
            event.replyEmbeds(embedBuilder.build())
                    .addActionRow(
                            Button.success("reload-giveaway", "Odśwież"),
                            Button.primary("end-giveaway", "Zakończ konkurs"),
                            Button.danger("delete-giveaway", "Usuń konkurs")
                    )
                    .setEphemeral(true)
                    .queue();
        }
    }

    private static void createGiveawayCreationModal(StringSelectInteractionEvent event) {
        TextInput duration = TextInput.create("duration", "Czas trwania", TextInputStyle.SHORT)
                .setPlaceholder("Czas trwania np. 12h, 10m, 2d")
                .setMinLength(2)
                .setMaxLength(12)
                .build();

        TextInput name = TextInput.create("name", "Tytuł", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Wpisz tytuł konkursu")
                .setMinLength(4)
                .setMaxLength(150)
                .build();

        TextInput winners = TextInput.create("winners", "Ilość wygranych", TextInputStyle.SHORT)
                .setPlaceholder("Wpisz ilość wygranych")
                .setMinLength(1)
                .setMaxLength(2)
                .build();

        Modal modal = Modal.create("giveaway-creation-modal", "Tworzenie konkursu")
                .addComponents(ActionRow.of(duration), ActionRow.of(name), ActionRow.of(winners))
                .build();

        event.replyModal(modal).queue();
    }

    private static void endGiveawayModalCreator(ButtonInteractionEvent event) {
        TextInput id;
        if(event.getComponent().getId().startsWith("end-giveaway-")) {
            id = TextInput.create("id", "ID", TextInputStyle.SHORT)
                    .setPlaceholder("Podaj ID konkursu do zakończenia")
                    .setValue(event.getComponent().getId().substring("end-giveaway-".length()))
                    .setMinLength(1)
                    .setMaxLength(6)
                    .build();
        } else {
            id = TextInput.create("id", "ID", TextInputStyle.SHORT)
                    .setPlaceholder("Podaj ID konkursu do zakończenia")
                    .setMinLength(1)
                    .setMaxLength(6)
                    .build();
        }

        TextInput sure = TextInput.create("sure", "Czy jesteś pewny?", TextInputStyle.SHORT)
                .setPlaceholder("Tak/Nie")
                .setMinLength(3)
                .setMaxLength(3)
                .build();


        Modal modal = Modal.create("giveaway-end-modal", "Zakończenie konkursu")
                .addComponents(ActionRow.of(id), ActionRow.of(sure))
                .build();

        event.replyModal(modal).queue();
    }

    private static void deleteGiveawayModalCreator(ButtonInteractionEvent event) {
        TextInput id;

        if(event.getComponent().getId().startsWith("delete-giveaway-")) {
            id = TextInput.create("id", "ID", TextInputStyle.SHORT)
                    .setPlaceholder("Podaj ID konkursu do usunięcia")
                    .setValue(event.getComponent().getId().substring("delete-giveaway-".length()))
                    .setMinLength(1)
                    .setMaxLength(6)
                    .build();

        } else {
            id = TextInput.create("id", "ID", TextInputStyle.SHORT)
                    .setPlaceholder("Podaj ID konkursu do usunięcia")
                    .setMinLength(1)
                    .setMaxLength(6)
                    .build();

        }

        TextInput sure = TextInput.create("sure", "Czy jesteś pewny?", TextInputStyle.SHORT)
                .setPlaceholder("Tak/Nie")
                .setMinLength(3)
                .setMaxLength(3)
                .build();


        Modal modal = Modal.create("giveaway-delete-modal", "Usuwanie konkursu")
                .addComponents(ActionRow.of(id), ActionRow.of(sure))
                .build();

        event.replyModal(modal).queue();
    }

    public static void sendGiveawayMessage(ModalInteractionEvent event, long timestamp) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(event.getValue("name").getAsString());
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.addField("Koniec za:", "<t:" + timestamp + ":R> " +
                "(<t:" + timestamp + ":f>)\n", false);
        embedBuilder.addField("Utworzony przez:",
                "<@" + event.getMember().getId() + ">\n", false);
        embedBuilder.addField("Ilość osób biorących udział:",
                "0", false);
        embedBuilder.addField("Ilość wygranych:",
                event.getValue("winners").getAsString(), false);

        embedBuilder.setTimestamp(Instant.now());

        event.getJDA().getTextChannelById(giveawaysChannelID).sendMessageEmbeds(
                embedBuilder.build()
        ).addActionRow(
                Button.success("join-giveaway+" +
                        databaseOperations.getGiveawayIDByCreatorID(event.getMember().getId()),
                        "Dołącz")
        ).queue(message -> {
            databaseOperations.updateGiveawayMessageID(event.getMember().getId(), message.getId());
        });
    }

    public static void updateGiveawayMessage(ButtonInteractionEvent event, int giveawayID) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.clear();
        int members = databaseOperations.getAmountOfMembersInGiveaway(giveawayID);

        embedBuilder.setTitle(event.getMessage().getEmbeds().get(0).getTitle());
        embedBuilder.setColor(Color.MAGENTA);
        for(int i = 0; i < 4; i++) {
            if(i == 2) {
                embedBuilder.addField(event.getMessage().getEmbeds().get(0).getFields().get(i).getName(),
                        String.valueOf(members), false);
            } else {
                embedBuilder.addField(event.getMessage().getEmbeds().get(0).getFields().get(i).getName(),
                        event.getMessage().getEmbeds().get(0).getFields().get(i).getValue(), false);
            }
        }
        embedBuilder.setTimestamp(Instant.now());

        event.editMessageEmbeds(embedBuilder.build()).queue();
    }
}
