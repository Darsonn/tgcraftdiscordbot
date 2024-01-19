package pl.darsonn.crafthome.bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import pl.darsonn.crafthome.bot.DiscordBot;
import pl.darsonn.crafthome.bot.database.DatabaseOperations;
import pl.darsonn.crafthome.bot.embedMessagesGenerator.EmbedMessageGenerator;
import pl.darsonn.crafthome.bot.ticketSystem.TicketSystemListener;
import pl.darsonn.crafthome.bot.countingSystem.CountingSystemListener;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class EventListener extends ListenerAdapter {
    EmbedMessageGenerator embedMessageGenerator = new EmbedMessageGenerator();
    TicketSystemListener ticketSystemListener = new TicketSystemListener();
    private String[] systems = new String[] {"Rules", "Tickets", "Status roles", "WIP", "Weryfikacja", "Autorole", "Cennik", "Instrukcje", "Regulamin sklepu", "TgCraft Team", "Pomoc liczenia", "Restart liczenia"};
    private String[] statusy = new String[] {"\uD83D\uDFE2Serwer uruchomiony", "\uD83D\uDD27Trwają prace techniczne", "\uD83D\uDD34Serwer wyłączony"};

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Member member = event.getMember();

        if(member.getUser().isBot()) return;
        changeCountOfMembers(event.getGuild());
        changeLastJoinedPlayer(event);

        embedMessageGenerator.sendWelcomeMessage(Objects.requireNonNull(event.getGuild().getTextChannelById("1175771786424635412")), member);
        DiscordBot.databaseOperations.insertMemberIntoDatabase(member);
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        changeCountOfMembers(event.getGuild());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
//        TextChannel logsChannel = event.getJDA().getTextChannelById("1176845996739788851");
//        embedMessageGenerator.sendStartupEmbedMessage(logsChannel);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "invite" -> embedMessageGenerator.sendInviteMessage(event);
            case "setup" -> setupCommand(event);
            case "changelog" -> changelogCommand(event);
            case "purge" -> purgeCommand(event);
            case "status" -> statusCommand(event);
            case "setusuer" -> setUserCommand(event);
            case "banuser" -> banCommand(event);
            case "restart" -> restartBotCommand(event);
            case "memberinfo" -> memberInfoCommand(event);
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if(event.getComponentId().equals("choose-open-positions")) embedMessageGenerator.sendStatusRolesEmbedMessage(event);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Button component = event.getComponent();

        if(Objects.requireNonNull(component.getId()).endsWith("-ticket")) {
            ticketSystemListener.interactionListener(event, component);
        } else if(component.getId().equals("getInformationsChangelog")) {
            event.reply("(+) - oznacza dodanie nowych funkcjonalności\n" +
                    "(-) - oznacza usunięcie funkcjonalności\n" +
                    "(/) - oznacza poprawienie/zmianę funkcjonalności").setEphemeral(true).queue();
        } else if(component.getId().equals("verification")) {
            Objects.requireNonNull(event.getGuild()).addRoleToMember(Objects.requireNonNull(event.getMember()), Objects.requireNonNull(event.getJDA().getRoleById("1175837018941554728"))).queue();
            event.reply("Pomyślnie zostałeś zweryfikowany!").setEphemeral(true).queue();
        } else if(Objects.requireNonNull(component.getId()).contains("application")) {
            applicationButton(event);
        } else if(component.getId().contains("pingrole")) {
            pingRoleButtons(event);
        } else if(component.getId().equals(":delete") || component.getId().contains(":prune:")) {
            String[] id = event.getComponentId().split(":");
            String authorId = id[0];
            String type = id[1];

            if (!authorId.equals(event.getUser().getId()))
                return;
            event.deferEdit().queue();

            MessageChannel channel = event.getChannel();
            switch (type)
            {
                case "prune":
                    int amount = Integer.parseInt(id[2]);
                    event.getChannel().getIterableHistory()
                            .skipTo(event.getMessageIdLong())
                            .takeAsync(amount)
                            .thenAccept(channel::purgeMessages);
                case "delete":
                    event.getHook().deleteOriginal().queue();
            }
        } else if(component.getId().contains("requirements-")) {
            event.reply("Przykro nam, ale na ten moment nie zostały wprowadzone żadne wymagania na to stanowisko.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if(event.getName().equals("setup") && event.getFocusedOption().getName().equals("setup")) {
            List<Command.Choice> options = Stream.of(systems)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                    .map(word -> new Command.Choice(word, word))
                    .toList();
            event.replyChoices(options).queue();
        } else if(event.getName().equals("status") && event.getFocusedOption().getName().equals("status")) {
            List<Command.Choice> options = Stream.of(statusy)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                    .map(word -> new Command.Choice(word, word))
                    .toList();
            event.replyChoices(options).queue();
        } else if(event.getName().equals("setusuer") && event.getFocusedOption().getName().equals("type")) {
            List<Command.Choice> options = Stream.of("Administrator", "Developer", "Twórca")
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                    .map(word -> new Command.Choice(word, word))
                    .toList();
            event.replyChoices(options).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if((event.getChannel().getId().equals("1176229979349073950") ||
        event.getChannel().getId().equals("1176230022047080448") ||
        event.getChannel().getId().equals("1176230082742857769")) && !(event.getAuthor().isBot())) {
            embedMessageGenerator.sendNewApplicationEmbedMessage(event, event.getMessage().getContentRaw());
            event.getMessage().delete().queue();
        } else if(event.getChannel().getId().equals(CountingSystemListener.countingChannelID) && !event.getAuthor().isBot()) {
            CountingSystemListener.newMessage(event);
        }

        DiscordBot.databaseOperations.updateSentMessages(event.getMember());
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        if(event.getName().equals("Ekran"))
            streamingCommand(event);
    }

    private void changelogCommand(SlashCommandInteractionEvent event) {
        TextChannel textChannel = Objects.requireNonNull(event.getGuild()).getTextChannelById("1175943559455723581");

        if(event.getMember().getRoles().contains(event.getGuild().getRoleById("1175839371195334796"))) {
            event.getChannel().getHistory().retrievePast(1)
                    .map(messages -> messages.get(0))
                    .queue(message -> {
                        if(!message.getAuthor().isBot()) {
                            Objects.requireNonNull(textChannel).sendMessage(Objects.requireNonNull(event.getJDA().getRoleById("1178088556464439296")).getAsMention() + "\n\n" + message.getContentRaw())
                                    .addActionRow(
                                            Button.success("getInformationsChangelog", "Zobacz znaczenie symboli")
                                    )
                                    .queue();
                            event.reply("Wysłano wiadomość na <#1175943559455723581>!").setEphemeral(true).queue();
                        } else {
                            event.reply("Błąd! Nie znaleziono wiadomości do wysłania.").setEphemeral(true).queue();
                        }
                    });
        } else {
            event.reply("Nie posiadasz wymaganych permisji, aby wywołać to polecenie!").setEphemeral(true).queue();
        }
    }

    private void setupCommand(SlashCommandInteractionEvent event) {
        switch(Objects.requireNonNull(event.getOption("setup")).getAsString().toLowerCase()) {
            case "rules" -> {
                event.reply("Rules embed message sent!").setEphemeral(true).queue();
                embedMessageGenerator.sendRulesEmbedMessage(event);
            }
            case "tickets" -> {
                event.reply("Ticket panel has been created!").setEphemeral(true).queue();
                embedMessageGenerator.sendTicketPanelEmbedMessage(event);
            }
            case "status roles" -> {
                event.reply("Choose open positions")
                        .addActionRow(
                                StringSelectMenu.create("choose-open-positions")
                                        .addOption("Administrator", "administrator")
                                        .addOption("Developer", "developer")
                                        .addOption("Creator", "creator")
                                        .addOption("None", "none")
                                        .setMaxValues(3)
                                        .build()
                        ).setEphemeral(true).queue();
            }
            case "wip" -> embedMessageGenerator.sendWIPEmbedMessage(event);
            case "weryfikacja" -> embedMessageGenerator.sendVeryficationEmbedMessage(event);
            case "autorole" -> embedMessageGenerator.sendAutoRoleEmbedMessage(event);
            case "cennik" -> embedMessageGenerator.sendPriceListEmbedMessage(event);
            case "instrukcje" -> embedMessageGenerator.sendInstrukcjeEmbedMessage(event);
            case "regulamin sklepu" -> embedMessageGenerator.sendRulesShopEmbedMessage(event);
            case "tgcraft team" -> embedMessageGenerator.sendTgCraftTeamMembersEmbedMessage(event);
            case "pomoc liczenia" -> embedMessageGenerator.sendHelpCountingEmbedMessage(event);
            case "restart liczenia" -> CountingSystemListener.restartCounting(event);
        }
    }

    private void applicationButton(ButtonInteractionEvent event) {
        String type = "tworca";
        switch(event.getMessage().getEmbeds().get(0).getFields().get(1).getValue()) {
            case "<#1176229979349073950>" -> type = "administracja";
            case "<#1176230022047080448>" -> type = "developer";
            case "<#1176230082742857769>" -> type = "tworca";
        }
        if(event.getComponent().getId().contains("accept-application")) embedMessageGenerator.sendResultApplicationMessage(event, "Zaakceptowane", type);
        else if(event.getComponent().getId().contains("reject-application")) embedMessageGenerator.sendResultApplicationMessage(event, "Odrzucone", type);
        else embedMessageGenerator.sendResultApplicationMessage(event, "Zignorowane", type);
    }

    private void pingRoleButtons(ButtonInteractionEvent event) {
        switch (event.getComponent().getId()) {
            case "giveaway-pingrole" -> {
                if(!event.getMember().getRoles().contains(event.getGuild().getRoleById("1178124717945802842"))) {
                    event.getGuild().addRoleToMember(event.getMember(), event.getJDA().getRoleById("1178124717945802842")).queue();
                    event.reply("Pomyślnie nadano rolę <@&1178124717945802842>!").setEphemeral(true).queue();
                } else {
                    event.getGuild().removeRoleFromMember(event.getMember(), event.getJDA().getRoleById("1178124717945802842")).queue();
                    event.reply("Pomyślnie zabrano rolę <@&1178124717945802842>!").setEphemeral(true).queue();
                }
            }
            case "changelog-pingrole" -> {
                if (!event.getMember().getRoles().contains(event.getGuild().getRoleById("1178088556464439296"))) {
                    event.getGuild().addRoleToMember(event.getMember(), event.getJDA().getRoleById("1178088556464439296")).queue();
                    event.reply("Pomyślnie nadano rolę <@&1178088556464439296>!").setEphemeral(true).queue();
                } else {
                    event.getGuild().removeRoleFromMember(event.getMember(), event.getJDA().getRoleById("1178088556464439296")).queue();
                    event.reply("Pomyślnie zabrano rolę <@&1178088556464439296>!").setEphemeral(true).queue();
                }
            }
            case "rolls-pingrole" -> {
                if (!event.getMember().getRoles().contains(event.getGuild().getRoleById("1178124651835174924"))) {
                    event.getGuild().addRoleToMember(event.getMember(), event.getJDA().getRoleById("1178124651835174924")).queue();
                    event.reply("Pomyślnie nadano rolę <@&1178124651835174924>!").setEphemeral(true).queue();
                } else {
                    event.getGuild().removeRoleFromMember(event.getMember(), event.getJDA().getRoleById("1178124651835174924")).queue();
                    event.reply("Pomyślnie zabrano rolę <@&1178124651835174924>!").setEphemeral(true).queue();
                }
            }
        }
    }

    private void purgeCommand(SlashCommandInteractionEvent event) {
        OptionMapping amountOption = event.getOption("amount");
        int amount = amountOption == null
                ? 100
                : (int) Math.min(200, Math.max(2, amountOption.getAsLong()));
        String userId = event.getUser().getId();
        event.reply("To usunie " + amount + " wiadomości.\nJesteś pewny?")
                .addActionRow(
                        Button.secondary(userId + ":delete", "Rezygnuję"),
                        Button.danger(userId + ":prune:" + amount, "Tak"))
                .setEphemeral(true).queue();
    }

    private void statusCommand(SlashCommandInteractionEvent event) {
        VoiceChannel voiceChannel = event.getJDA().getVoiceChannelById("1175777045561749614");
        voiceChannel.getManager().setName(event.getOption("status").getAsString()).queue();

        event.reply("Pomyślnie zmieniono status na: " + event.getOption("status").getAsString()).setEphemeral(true).queue();
    }

    private void setUserCommand(SlashCommandInteractionEvent event) {
        String option = "tworca";
        switch(event.getOption("type").getAsString()) {
            case "Administrator" -> option = "administracja";
            case "Developer" -> option = "developer";
            case "Twórca" -> option = "tworca";
        }

        DiscordBot.databaseOperations.updateIDOfOpiekun(option, event.getOption("user").getAsString());

        event.reply("Pomyślnie zmieniono opiekuna: " + event.getOption("type").getAsString() + " na użytkownika <@" + event.getOption("user").getAsString() + ">").setEphemeral(true).queue();
    }

    private void changeCountOfMembers(Guild guild) {
        VoiceChannel counterChannel = guild.getVoiceChannelById("1175780112965312564");

        guild.loadMembers().onSuccess(members -> {
            int users = 0;
            for(Member member: members){
                if (!member.getUser().isBot()) users++;
            }

            counterChannel.getManager().setName("\uD83D\uDC64Ilość użytkowników: " + users).queue();
        });
    }

    private void changeLastJoinedPlayer(GuildMemberJoinEvent event) {
        VoiceChannel newMemberChannel = event.getGuild().getVoiceChannelById("1186063744107810877");

        newMemberChannel.getManager().setName("\uD83D\uDC4BNowy: " + event.getMember().getEffectiveName()).queue();
    }

    private void banCommand(SlashCommandInteractionEvent event) {
        if(!event.getMember().getRoles().contains(event.getGuild().getRoleById("1175928484200202330"))) {
            event.reply("Nie jesteś uprawniony do wykonania tej komendy!").setEphemeral(true).queue();
            return;
        }
        String username = event.getOption("user").getAsString();
        String reason = event.getOption("powod").getAsString();
        int time = event.getOption("czas").getAsInt();
        boolean odwolanie = event.getOption("odwolanie").getAsBoolean();

        embedMessageGenerator.sendBanEmbedMessage(event, username, reason, time, odwolanie, event.getUser().getId());

        event.reply("Poprawnie wystawiono bana graczowi o nicku: " + username).setEphemeral(true).queue();
    }

    private void restartBotCommand(SlashCommandInteractionEvent event) {
        if(!event.getMember().getId().equals("951563322300444742")) {
            event.reply("Niestety nie jesteś uprawniony do restartowania bota!").setEphemeral(true).queue();
            return;
        }

        try {
            event.reply("Pomyślnie wykonano restart bota!").setEphemeral(true).queue();
            Process process = Runtime.getRuntime().exec("sudo systemctl restart discordbot.service");
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            event.reply("Niestety restart bota nie powiódł się!").setEphemeral(true).queue();
            throw new RuntimeException(e);
        }
    }

    private void streamingCommand(UserContextInteractionEvent event) {
        if(!event.getMember().getRoles().contains(event.getGuild().getRoleById("1175928484200202330"))) {
            event.reply("Nie posiadasz roli <@&1175928484200202330>!").setEphemeral(true).queue();
            return;
        }

        Member member = event.getTargetMember();

        if(member.getRoles().contains(event.getGuild().getRoleById("1186353183921033276"))) {
            event.getGuild().removeRoleFromMember(member, event.getGuild().getRoleById("1186353183921033276")).queue();
            event.reply("Pomyślnie zabrano rolę umożliwiającą udostępnianie ekranu użytkownikowi <@" + member.getId() + ">").setEphemeral(true).queue();
        } else {
            event.getGuild().addRoleToMember(member, event.getGuild().getRoleById("1186353183921033276")).queue();
            event.reply("Pomyślnie nadano rolę umożliwiającą udostępnianie ekranu użytkownikowi <@" + member.getId() + ">").setEphemeral(true).queue();
        }
    }

    private void memberInfoCommand(SlashCommandInteractionEvent event) {
        Member member;
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if(event.getOption("user") == null)
            member = event.getMember();
        else
            member = event.getOption("user").getAsMember();

        embedBuilder.setTitle("Informacje o użytkowniku");
        embedBuilder.setDescription(member.getAsMention());
        embedBuilder.setColor(member.getColor());
        embedBuilder.addField("Data dołączenia",
                member.getTimeJoined().format(DateTimeFormatter.ISO_DATE).replace("Z", ""),
                true);
        embedBuilder.addField("Ilość wysłanych wiadomość",
                DiscordBot.databaseOperations.getMembersMessagesSent(member) + "", true);

        StringBuilder roleString = new StringBuilder();
        for(Role role : member.getRoles()) {
            if(!role.getId().equals("1175769792137936968"))
                roleString.append("<@&").append(role.getId()).append(">\n");
        }
        embedBuilder.addField("Role", roleString.toString(), false);

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
