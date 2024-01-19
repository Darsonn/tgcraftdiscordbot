package pl.darsonn.crafthome.bot.embedMessagesGenerator;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import pl.darsonn.crafthome.bot.DiscordBot;
import pl.darsonn.crafthome.bot.database.DatabaseOperations;
import pl.darsonn.crafthome.bot.countingSystem.CountingSystemListener;

import java.awt.*;
import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Objects;

public class EmbedMessageGenerator {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
    LocalDateTime time = LocalDateTime.now();
    EmbedBuilder embedBuilder = new EmbedBuilder();
    DatabaseOperations databaseOperation = DiscordBot.databaseOperations;

    public void sendRulesEmbedMessage(SlashCommandInteractionEvent event) {
        TextChannel textChannel = event.getGuildChannel().asTextChannel();

        embedBuilder.clear();

        embedBuilder.setTitle("TgCraft");
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.addBlankField(false);

        embedBuilder.addField("Zasady ogólne - panujące na serwerze TgCraft",
                """
                        1. Szanuj innych użytkowników.
                        2. Zakaz wysyłania treści dla osób pełnoletnich.
                        3. Nie promuj żadnych treści o charakterze rasistowskim, seksistowskim, homofobicznym ani innym obraźliwym.
                        4. Wszelkie treści spamu będą natychmiastowo usuwane.
                        5. Reklama jest dozwolona tylko i wyłącznie na przeznaczonych do tego kanałach lub za zgodą administracji

                        Na serwerze dodatkowo obowiązują ogólnie ustanowione zasady [Discord Terms of Service](https://discord.com/terms)
                        """, false);

        embedBuilder.setTimestamp(Instant.now());

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void sendTicketPanelEmbedMessage(SlashCommandInteractionEvent event) {
        TextChannel textChannel = event.getGuildChannel().asTextChannel();

        embedBuilder.clear();

        embedBuilder.setTitle("TgCraft Ticket Support");
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.addField("Uwaga!",
                "W przypadku utworzenia ticketa bez powodu będą wyciągane z tego tytułu konsekwencje.", true);

        textChannel.sendMessageEmbeds(embedBuilder.build())
                .addActionRow(
                        Button.danger("main-open-ticket", "Otwórz ticket")
                ).queue();
    }

    public void sendPanelInTicket(TextChannel ticket, Member member, String ticketType) {
        embedBuilder.clear();

        embedBuilder.setTitle("TgCraft Ticket Support");
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.setDescription("Dziękujemy za kontakt z TgCraft Support.\n" +
                "Proszę opisać swój problem i czekać na odpowiedź z naszej strony.");


        embedBuilder.setFooter("Created at " + dtf.format(time));

        ticket.sendMessage("||<@"+ member.getId()+">||").queue();
        ticket.sendMessageEmbeds(embedBuilder.build())
                .addActionRow(
                        Button.danger("close-ticket", "Zamknij ticket")
                )
                .queue();
    }

    public void sendInformationAboutCreationNewTicket(TextChannel ticketLogsChannel, Member member, String channelID, Timestamp timestamp) {
        embedBuilder.clear();

        embedBuilder.setTitle("Ticket created - " + Objects.requireNonNull(member.getGuild().getChannelById(TextChannel.class, channelID)).getName());
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setDescription("Created by " + member.getAsMention() + " at " + timestamp +
                "\n<#" + channelID + ">");

        ticketLogsChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void sendInformationAboutDeletingTicket(TextChannel ticketLogsChannel, Member member, String channelID, Timestamp closingDate) {
        embedBuilder.clear();

        embedBuilder.setTitle("Ticket closed - " + Objects.requireNonNull(member.getGuild().getChannelById(TextChannel.class, channelID)).getName());
        embedBuilder.setColor(Color.RED);
        embedBuilder.addField("Opened", "by <@" + databaseOperation.getTicketOpener(channelID) + ">\nat " +
                databaseOperation.getTicketCreateDate(channelID), false);
        embedBuilder.addField("Deleted", "by " + member.getAsMention() + "\nat " +
                closingDate, false);

        ticketLogsChannel.sendMessageEmbeds(embedBuilder.build()).queue();

        databaseOperation.closeTicket(channelID, member.getId(), closingDate);

    }

    public void sendWelcomeMessage(TextChannel welcomeChannel, Member member) {
        embedBuilder.clear();

        String avatarUrl = member.getAvatarUrl();
        if (avatarUrl == null){
            avatarUrl = "https://i.imgur.com/SvMeoSp.png";
        }
        embedBuilder.setAuthor(member.getEffectiveName(), null, avatarUrl);

        embedBuilder.setColor(Color.MAGENTA);
        embedBuilder.setDescription("Witamy na serwerze **TgCraft.pl**!");
        embedBuilder.setFooter("Miłej gry!");
        embedBuilder.setTimestamp(Instant.now());

        welcomeChannel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
//            if (member.getGuild().getEmojiById("1187167529970450552") != null){
//                message.addReaction(member.getGuild().getEmojiById("1187167529970450552")).queue();
//            }
            message.addReaction(Emoji.fromUnicode("U+1F44B")).queue();
        });
    }

    public void sendStatusRolesEmbedMessage(StringSelectInteractionEvent event) {
        TextChannel admChannel = Objects.requireNonNull(event.getGuild()).getTextChannelById("1176229979349073950"),
        devChannel = event.getGuild().getTextChannelById("1176230022047080448"),
        creatorChannel = event.getGuild().getTextChannelById("1176230082742857769");

        boolean isAdm = false, isDev = false, isCreator = false;
        TextChannel textChannel = event.getChannel().asTextChannel();

        textChannel.getHistory().retrievePast(2)
                .map(messages -> messages.get(0))
                .queue(message -> {
                    if(message.getAuthor().isBot()) message.delete().queue();
                });

        for (var element : event.getSelectedOptions()) {
            if(element.getValue().equals("administrator")) isAdm = true;
            if(element.getValue().equals("developer")) isDev = true;
            if(element.getValue().equals("creator")) isCreator = true;
        }

        embedBuilder.clear();

        embedBuilder.setTitle("TgCraft - statusy rekrutacji");
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.addBlankField(false);

        String wynik = isAdm ? ":white_check_mark: Otwarta" : ":x: Zamknięta";
        embedBuilder.addField("Administrator", wynik + "\n<#1176229979349073950>", true);

        wynik = isDev ? ":white_check_mark: Otwarta" : ":x: Zamknięta";
        embedBuilder.addField("Developer", wynik + "\n<#1176230022047080448>", true);

        wynik = isCreator ? ":white_check_mark: Otwarta" : ":x: Zamknięta";
        embedBuilder.addField("Twórca", wynik + "\n<#1176230082742857769>", true);

        embedBuilder.addBlankField(false);

        embedBuilder.addField("Proces aplikacji", "Aby zaaplikować należy wysłać wiadomość według wzoru podanym na odpowiednim kanale " +
                "oraz oczekiwać na wynik rozpatrzenia podania na kanale <#1175804183321006192>", true);

        textChannel.sendMessageEmbeds(embedBuilder.build())
                .addActionRow(
                        isAdm ? Button.success("requirements-adm", "Wymagania na Administratora").asEnabled() : Button.success("requirements-adm", "Wymagania na Administratora").asDisabled(),
                        isDev ? Button.primary("requirements-dev", "Wymagania na Developera").asEnabled() : Button.primary("requirements-dev", "Wymagania na Developera").asDisabled(),
                        isCreator ? Button.danger("requirements-tworca", "Wymagania na Twórcę").asEnabled() : Button.danger("requirements-tworca", "Wymagania na Twórcę").asDisabled()
                ).queue();

        event.reply("Message sent!").setEphemeral(true).queue();

        if(isAdm) {
            Objects.requireNonNull(admChannel).getManager().putRolePermissionOverride(1175837018941554728L, EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL), null).queue();
        } else {
            Objects.requireNonNull(admChannel).getManager().putRolePermissionOverride(1175837018941554728L, EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND)).queue();
        }

        if(isDev) {
            Objects.requireNonNull(devChannel).getManager().putRolePermissionOverride(1175837018941554728L, EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL), null).queue();
        } else {
            Objects.requireNonNull(devChannel).getManager().putRolePermissionOverride(1175837018941554728L, EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND)).queue();
        }

        if(isCreator) {
            Objects.requireNonNull(creatorChannel).getManager().putRolePermissionOverride(1175837018941554728L, EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL), null).queue();
        } else {
            Objects.requireNonNull(creatorChannel).getManager().putRolePermissionOverride(1175837018941554728L, EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND)).queue();
        }

        admChannel.getHistory().retrievePast(2)
                .map(messages -> messages.get(0))
                .queue(message -> {
                    if(message.getAuthor().isBot()) message.delete().queue();
                });
        devChannel.getHistory().retrievePast(2)
                .map(messages -> messages.get(0))
                .queue(message -> {
                    if(message.getAuthor().isBot()) message.delete().queue();
                });
        creatorChannel.getHistory().retrievePast(2)
                .map(messages -> messages.get(0))
                .queue(message -> {
                    if(message.getAuthor().isBot()) message.delete().queue();
                });

        String wzorWiadomosci = """
                - Nick discord:\s
                - Discord ID:\s
                - Wiek:\s
                - Doświadczenie na podobnym stanowisku:\s""";

        admChannel.sendMessage("# Podanie do administracji: \n\n" + wzorWiadomosci + "\n- Ilość czasu który możesz poświęcić dla serwera: ").queue();
        devChannel.sendMessage("# Podanie na developera: \n\n" + wzorWiadomosci + "\n- Portfolio: ").queue();
        creatorChannel.sendMessage("# Podanie na twórcę: \n\n" + wzorWiadomosci + "\n- Portfolio: ").queue();
    }

    public void sendWIPEmbedMessage(SlashCommandInteractionEvent event) {
        TextChannel textChannel = event.getChannel().asTextChannel();

        embedBuilder.clear();

        embedBuilder.setTitle("TgCraft - WIP");
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.setDescription("Ten element nie został jeszcze ukończony.");

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();

        event.reply("Utworzono wiadomość").setEphemeral(true).queue();
    }

    public void sendInviteMessage(SlashCommandInteractionEvent event) {
        embedBuilder.clear();

        embedBuilder.setTitle("TgCraft - Invite Link");
        embedBuilder.setColor(Color.MAGENTA);
        embedBuilder.setDescription("https://discord.gg/4JzuQJmdSS");

        event.replyEmbeds(embedBuilder.build()).queue();
    }

    public void sendVeryficationEmbedMessage(SlashCommandInteractionEvent event) {
        TextChannel textChannel = event.getChannel().asTextChannel();

        embedBuilder.clear();

        embedBuilder.setTitle("TgCraft - Weryfikacja");
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.setDescription("Klinij przycisk poniżej, aby się zweryfikować i otrzymać rolę <@&1175837018941554728>.");

        textChannel.sendMessageEmbeds(embedBuilder.build())
                .addActionRow(
                        Button.success("verification", "Zweryfikuj się")
                ).queue();

        event.reply("Utworzono wiadomość").setEphemeral(true).queue();
    }

    public void sendStartupEmbedMessage(TextChannel logsChannel) {
        embedBuilder.clear();

        embedBuilder.setColor(Color.GREEN);

        embedBuilder.setTitle("Bot został uruchomiony ponownie!");
        embedBuilder.setDescription("Najczęściej oznacza to dodanie nowych funkcjonalności lub naprawienie błedów :>");

        logsChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void sendStoppingEmbedMessage(JDA jda) {
        TextChannel logsChannel = jda.getTextChannelById("1176845996739788851");

        embedBuilder.clear();

        embedBuilder.setColor(Color.RED);

        embedBuilder.setTitle("Serwer przechodzi w tryb Offline!");
        embedBuilder.setDescription("Brak możliwości połączenia się z serwerem");

        logsChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void sendPlayerJoinedMessage(JDA jda, String displayName, String ipAdress) {
        TextChannel logsChannel = jda.getTextChannelById("1176845996739788851");

        embedBuilder.clear();

        embedBuilder.setColor(Color.CYAN);

        embedBuilder.setTitle(displayName + " dołączył na serwer");
        embedBuilder.setDescription("IP: " + ipAdress);

        logsChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void sendPlayerLeftMessage(JDA jda, String displayName, String ipAdress) {
        TextChannel logsChannel = jda.getTextChannelById("1176845996739788851");

        embedBuilder.clear();

        embedBuilder.setColor(Color.BLUE);

        embedBuilder.setTitle(displayName + " opuścił serwer");
        embedBuilder.setDescription("IP: " + ipAdress);

        logsChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void sendNewApplicationEmbedMessage(MessageReceivedEvent event, String message) {
        String type;
        TextChannel applicationChannel = event.getGuild().getTextChannelById("1176257469534711929");

        embedBuilder.clear();

        embedBuilder.setColor(Color.YELLOW);

        switch(event.getMessage().getChannel().getId()) {
            case "1176229979349073950" -> {
                embedBuilder.setTitle("Nowe podanie do administracji");
                type = "adm";
            }
            case "1176230022047080448" -> {
                embedBuilder.setTitle("Nowe podanie na developera");
                type = "dev";
            }
            case "1176230082742857769" -> {
                embedBuilder.setTitle("Nowe podanie na twórcę");
                type = "creator";
            }
            default -> type = "unknown";
        }

        embedBuilder.addField("Autor podania:", "<@" + event.getAuthor().getId() + "> | " + event.getAuthor().getName(), true);
        embedBuilder.addField("Kanał podania: ", "<#" + event.getChannel().getId() + ">", true);
        embedBuilder.addBlankField(false);
        embedBuilder.addField("Treść podania:", message, false);

        embedBuilder.setTimestamp(Instant.now());

        applicationChannel.sendMessageEmbeds(embedBuilder.build())
                .addActionRow(
                        Button.success(event.getAuthor().getId() + "-accept-application", "Akceptuj"),
                        Button.danger(event.getAuthor().getId() + "-reject-application", "Odrzuć"),
                        Button.primary(event.getAuthor().getId() + "-ignore-application", "Ignoruj")
                ).queue((message1) -> databaseOperation.createApplication(event.getAuthor().getId(), type, message1.getId()));
    }

    public void sendResultApplicationMessage(ButtonInteractionEvent event, String option, String type) {
        TextChannel resultChannel = event.getGuild().getTextChannelById("1175804183321006192");

        String value = event.getComponentId();
        String id = "";

        for(int i = 0; i < value.length(); i++) {
            if(value.charAt(i) != '-') {
                id += value.charAt(i);
            } else break;
        }

        embedBuilder.clear();

        if(option.equals("Zaakceptowane")) {
            String idOfOpiekun = databaseOperation.getIDOfOpiekun(type);

            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setTitle("Wynik podania - pozytywny!");
            embedBuilder.setDescription("<@" + id + ">, Twoje podanie zostało rozpatrzone pozytywnie!\n" +
                    "Zgłoś się w wiadomości prywatnej do: <@" + idOfOpiekun + ">");
            resultChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            updateApplicationMessage(event, id, option);
        } else if(option.equals("Odrzucone")) {
            embedBuilder.setColor(Color.RED);
            embedBuilder.setTitle("Wynik podania - negatywny!");
            embedBuilder.setDescription("<@" + id + ">, niestety Twoje podanie zostało odrzucone.");
            resultChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            updateApplicationMessage(event, id, option);
        } else {
            updateApplicationMessage(event, id, option);
        }
    }

    public void updateApplicationMessage(ButtonInteractionEvent event, String applicantID, String result) {
        TextChannel channel = event.getGuild().getTextChannelById("1176257469534711929");

        embedBuilder.clear();

        embedBuilder.setTitle("Podanie - rozpatrzone");

        switch(result) {
            case "Zaakceptowane" -> embedBuilder.setColor(Color.GREEN);
            case "Odrzucone" -> embedBuilder.setColor(Color.RED);
            case "Zignorowane" -> embedBuilder.setColor(Color.BLUE);
        }

        embedBuilder.addField("Autor podania:", "<@" + applicantID + ">", true);
        embedBuilder.addBlankField(true);
        embedBuilder.addField("Rozpatrzone przez:", "<@" + event.getMember().getId() + ">", true);
        embedBuilder.addField("Wynik podania:", result, false);
        embedBuilder.addBlankField(true);
        embedBuilder.setTimestamp(Instant.now());

        channel.retrieveMessageById(databaseOperation.getApplicationMessageID(applicantID)).queue((message -> {
            MessageEmbed messageEmbed = message.getEmbeds().get(0);

            embedBuilder.addField("Kanał podania:", messageEmbed.getFields().get(1).getValue(), true);
            embedBuilder.addField("Treść podania:", messageEmbed.getFields().get(3).getValue(), false);

            message.editMessageEmbeds(embedBuilder.build()).setActionRow(
                    Button.success("temp1", "Akceptuj").asDisabled(),
                    Button.danger("temp2", "Odrzuć").asDisabled(),
                    Button.primary("temp3", "Ignoruj").asDisabled()
            ).queue();
        }));

        databaseOperation.closeApplication(applicantID);

        event.reply("Operacja przebiegła pomyślnie :>").setEphemeral(true).queue();
    }

    public void sendWezwanieMessage(JDA jda, String admin, String wezwany, String channelName, String uid_wezwany) {
        TextChannel channel = jda.getTextChannelById("1175784899710566410");

        String wezwanyID = databaseOperation.getDiscordIDFromUID(uid_wezwany);

        String idChannel = "1175771970680389683";

        switch(channelName) {
            case "Pomoc1" -> idChannel = "1175771970680389683";
            case "Pomoc2" -> idChannel = "1175772057317953637";
            case "Pomoc3" -> idChannel = "1175772184866734190";
            case "Sprawdzanko" -> idChannel = "1175784958548246569";
        }

        embedBuilder.clear();

        embedBuilder.setTitle(wezwany + " - został wezwany");
        embedBuilder.setColor(Color.RED);

        embedBuilder.setDescription("Gracz został wezwany na kanał <#" + idChannel + "> przez " + admin);

        channel.sendMessage("<@" + wezwanyID + ">").queue();
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void sendAutoRoleEmbedMessage(SlashCommandInteractionEvent event) {
        TextChannel textChannel = event.getChannel().asTextChannel();

        embedBuilder.clear();

        embedBuilder.setColor(Color.MAGENTA);
        embedBuilder.setTitle("TgCraft - Ping Role");
        embedBuilder.setDescription("Wybierz kategorie powiadomień jakie chcesz otrzymywać");

        textChannel.sendMessageEmbeds(embedBuilder.build()).addActionRow(
                Button.success("giveaway-pingrole", "Giveaway").asDisabled(),
                Button.primary("changelog-pingrole", "Changelog"),
                Button.danger("rolls-pingrole", "Ankiety").asDisabled()
        ).queue();

        event.reply("Pomyślnie wysłano Auto Role Embed Message").setEphemeral(true).queue();
    }

    public void sendRulesShopEmbedMessage(SlashCommandInteractionEvent event) {
        TextChannel textChannel = event.getChannel().asTextChannel();

        embedBuilder.clear();
        embedBuilder.setTitle("TgCraft - Regulamin Sklepu");
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.addField("Regulamin sklepu", """
                1. Kupujący podejmuje zakup na własną odpowiedzialność;\s
                2. Zakupy nie podlegają zwrotowi;\s
                3. Właściciel nie odpowiada za brak wiedzy na temat regulaminu;\s
                4. -||- nie odpowiada za błędną interpretację opisu danego produktu;\s
                5. Właściciel nie ponosi odpowiedzialności za źle wypełnione rubryki formularza podczas realizowania zakupu;\s
                6. Wszelkie błędy lub niedopatrzenia podczas realizacji zamówienia prosimy zgłaszać Właścicielowi lub Administracji serwera (w tym Developerom).\s
                7. Kiedy kupujący zorientuje się, że popełnił błąd, może zgłosić takową sytuację do Właściciela lub Administratora serwera, w przeciągu 24h od zrealizowania zakupu;\s
                8. Podczas wpisywania kwoty zakupu musi ona się zgadzać do ceny usługi i jej okresu trwania! W przypadku wpisania mniejszej kwoty niż powinna być, usługa nie zostanie zrealizowana;""", false);

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();

        event.reply("Pomyślnie wysłano wiadomość").setEphemeral(true).queue();
    }

    public void sendPriceListEmbedMessage(SlashCommandInteractionEvent event) {
        TextChannel textChannel = event.getChannel().asTextChannel();

        embedBuilder.clear();

        embedBuilder.setTitle("TgCraft - Cennik");
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.addField("VIP", """
                Ranga VIP posiada:\s
                - dostęp do /kit VIP\s
                - -||- do komendy /ec\s
                - -||- do komendy /wb\s
                - unikalny prefix VIP\s""", true);

        embedBuilder.addBlankField(true);

        embedBuilder.addField("SVIP", """
                Ranga SVIP posiada:\s
                - dostęp do /kit VIP\s
                - -||- do /kit SVIP\s
                - -||- do komendy /ec\s
                - -||- do komendy /wb\s
                - -||- do komendy /repair\s
                - unikalny prefix SVIP\s""", true);


        embedBuilder.addField("MVIP", """
                Ranga MVIP posiada:\s
                - dostęp do /kit VIP\s
                - -||- do /kit SVIP\s
                - -||- do /kit MVIP\s
                - -||- do komendy /ec\s
                - -||- do komendy /wb\s
                - -||- do komendy /feed\s
                - -||- do komendy /repair\s
                - -||- do komendy /repair all\s
                - unikalny prefix MVIP\s""", true);

        embedBuilder.addBlankField(true);

        embedBuilder.addField("MVIP+", """
                Ranga MVIP+ posiada:\s
                - dostęp do /kit VIP\s
                - -||- do /kit SVIP\s
                - -||- do /kit MVIP\s
                - -||- do /kit MVIP+\s
                - -||- do komendy /ec\s
                - -||- do komendy /wb\s
                - -||- do komendy /heal\s
                - -||- do komendy /feed\s
                - -||- do komendy /repair\s
                - -||- do komendy /repair all\s
                - unikalny prefix MVIP+\s""", true);

        embedBuilder.addField("**Podgląd prefix'ów i kit'ów jest dostępny na serwerze pod komendą /rangi**", "", false);

        embedBuilder.addBlankField(false);

        embedBuilder.addField("**Cennik**", "**Cena za | tydzień | miesiąc | edycję**", false);

//        embedBuilder.addField("|", """
//                VIP      |       8zł      |       25zł     |       35zł
//                SVIP     |       11zł     |       28zł     |       38zł
//                MVIP     |       14zł     |       34zł     |       42zł
//                MVIP+    |       18zł     |       45zł     |       55zł
//                """, true);
        embedBuilder.addField("VIP--------8zł------25zł----35zł" + "\n" +
                        "SVIP------11zł------28zł-----38zł" + "\n" +
                        "MVIP-----14zł------34zł-----42zł" + "\n" +
                        "MVIP+----18zł------45zł-----55zł", "", true);


        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();

        event.reply("Pomyślnie wysłano wiadomość").setEphemeral(true).queue();
    }

    public void sendInstrukcjeEmbedMessage(SlashCommandInteractionEvent event) {
        TextChannel textChannel = event.getChannel().asTextChannel();

        File zdjecieKroku1 = new File("krok_1.png");
        File zdjecieKroku2 = new File("krok_2.png");
        File zdjecieKroku3 = new File("krok_3.png");

        embedBuilder.setTitle("Poradnik jak dokonać zakupu:");
        embedBuilder.setColor(Color.MAGENTA);
        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();

        embedBuilder.clear();
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.setTitle("Krok 1");
        embedBuilder.setDescription("Wchodzimy na strone \nhttps://tipply.pl/@tgcraft\n\n(Patrz zdjęcie poniżej)");
        embedBuilder.setImage("attachment://krok_1.png");
        textChannel.sendMessageEmbeds(embedBuilder.build()).addFiles(FileUpload.fromData(zdjecieKroku1, "krok_1.png")).queue();

        embedBuilder.setTitle("Krok 2");
        embedBuilder.setDescription("Wypełniamy rubryki. \n-W miejscu gdzie trzeba wpisać nazwę, wpisujemy nick z minecraft (W przypadku wpisania złej nazwy, prosimy skontaktować się z administracją do 24h od zakupu)\n" +
                "              -Miejsce na adres email wypełniamy zgodnie z jego przeznaczeniem\n" +
                "              -W miejscu wiadomości wpisujemy nazwę usługi i jej okres trwania\n" +
                "              (Patrz zdjęcie poniżej)");
        embedBuilder.setImage("attachment://krok_2.png");
        textChannel.sendMessageEmbeds(embedBuilder.build()).addFiles(FileUpload.fromData(zdjecieKroku2, "krok_2.png")).queue();

        embedBuilder.setTitle("Krok 3");
        embedBuilder.setDescription("Wypełniamy rubryki. \n-W miejscu na kwotę, wpisujemy cenę za daną usługę i jej okres zgodnie z cennikiem na kanale <#1185670601931767949> \n" +
                "              Następnie wybieramy opcje płatności i akceptujemy regulamin\n" +
                "              -Od momentu zakupu administracja ma 24 godziny na sfinalizowanie zakupu\n" +
                "                (Patrz zdjęcie poniżej)");
        embedBuilder.setImage("attachment://krok_3.png");
        textChannel.sendMessageEmbeds(embedBuilder.build()).addFiles(FileUpload.fromData(zdjecieKroku3, "krok_3.png")).queue();

        event.reply("Pomyślnie wysłano wiadomość").setEphemeral(true).queue();
    }

    public void sendBanEmbedMessage(SlashCommandInteractionEvent event, String username, String reason, int hours, boolean odwolanie, String userID) {
        TextChannel textChannel = event.getGuild().getTextChannelById("1175780185807794216");

        embedBuilder.clear();

        embedBuilder.setTitle("Gracz o nicku: " + username + " został zbanowany");
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.setDescription("Czas: **" + hours + " godzin**\nPowód: **" + reason + "**\n" + (odwolanie ? "**Z możliwością odwołania na kanale **<#1175780204082376715>":"**Bez możliwości odwołania**") + "\nWystawione przez: <@" + userID + ">");

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void sendTgCraftTeamMembersEmbedMessage(SlashCommandInteractionEvent event) {
        TextChannel textChannel = event.getGuild().getTextChannelById("1187360209308495873");

        embedBuilder.clear();

        embedBuilder.setTitle("Zespół TgCraft.pl");
        embedBuilder.setColor(Color.MAGENTA);

        embedBuilder.addBlankField(true);
        embedBuilder.addField("Zarząd", """
                ╭✧**Właściciel**\s
                ┝✧<@657974312501772342>\s
                ╰✧<@951563322300444742>\s
                 
                ╭✧**Współwłaściciel**\s
                ╰✧<@1047965351750664302>\s
                 
                ╭✧**Zarząd**\s
                ╰✧<@702188269416218806>""", true);
        embedBuilder.addBlankField(true);

        embedBuilder.addField("Zespół Administracyjny", """
                ╭✧**Head Admin**\s
                ╰✧brak\s
                 
                ╭✧**Admini**\s
                ┝✧<@700398673007345684>\s
                ╰✧<@1135789201242202205>\s
                 
                ╭✧**Moderatorzy**\s
                ╰✧<@689155071619104805>\s
                
                ╭✧**Junior Moderatorzy**\s
                ╰✧brak\s
                
                ╭✧**Helperzy**\s
                ┝✧<@558004722137694209>
                ┝✧<@421023458781036554>
                ╰✧<@785387930998407189>""", true);
        embedBuilder.addBlankField(true);
        embedBuilder.addField("Zespół Developerski", """
                ╭✧**Head Developer**\s
                ╰✧<@951563322300444742>\s
                
                ╭✧**Developerzy**\s
                ╰✧<@1047965351750664302>""", true);


        event.reply("Pomyślnie wysłano rozpiskę członków administracji tgcraft.pl").setEphemeral(true).queue();
        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void sendInvalidNumberException(MessageReceivedEvent event, boolean incorrectNumber) {
        embedBuilder.clear();

        embedBuilder.setTitle(incorrectNumber ? "Wpisałeś niepoprawną liczbę!" : "Wpisana przez Ciebie wartość nie jest liczbą!");
        embedBuilder.setColor(Color.RED);
        embedBuilder.setDescription("Zaczynamy od początku!\nJa zaczynam od cyfry **1**!");

        event.getMessage().replyEmbeds(embedBuilder.build()).queue(message -> {
            message.addReaction(Emoji.fromUnicode("U+0031 U+20E3")).queue();
        });
    }

    public void sendRestartNumberMessage(SlashCommandInteractionEvent event) {
        embedBuilder.clear();

        embedBuilder.setTitle("Liczba została zrestartowana!");
        embedBuilder.setColor(Color.MAGENTA);
        embedBuilder.setDescription("Ja zaczynam od cyfry **1**!");
        embedBuilder.setFooter("Restart wykonany przez: " + event.getMember().getEffectiveName());

        event.getJDA().getTextChannelById(CountingSystemListener.countingChannelID).sendMessageEmbeds(embedBuilder.build()).queue(message -> {
            message.addReaction(Emoji.fromUnicode("U+0031 U+20E3")).queue();
        });
        event.reply("Pomyślnie zrestartowano liczbę!").setEphemeral(true).queue();
    }

    public void sendHelpCountingEmbedMessage(SlashCommandInteractionEvent event) {
        embedBuilder.clear();

        embedBuilder.setTitle("Zasady liczenia");
        embedBuilder.setDescription("""
                1. Każda liczba powinna być większa o jeden od poprzedniej.
                2. Możliwe są działania matematyczne zapisane w jednej wiadomości korzystąjąc z 4 podstawowych znaków działań matematycznych (+, -, *, /).
                3. Zabronione jest dzielenie przez 0.
                4. Każda wiadomość nie będąca liczbą będzie restartowała grę.
                4.1 Aby napisać coś innego niż liczbę możesz posłużyć się '/*' przed wiadomością.
                5. Wszystkie błędy prosimy o zgłaszanie na kanale <#1175773470467035206>.
                6. Po 3 błędnych wprowadzonych wartościach otrzymujesz karną rolę <@&1197295251057016883>.
                7. Po dojściu do liczby **500** gra jest restartowana.""");
        embedBuilder.setColor(Color.MAGENTA);
        embedBuilder.setFooter("by Darsonn");

        event.getChannel().asTextChannel().sendMessageEmbeds(embedBuilder.build()).queue();
        event.reply("Pomyślnie wysłano wiadomośc na temat liczenia").setEphemeral(true).queue();
    }
}
