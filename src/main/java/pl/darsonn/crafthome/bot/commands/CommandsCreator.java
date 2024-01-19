package pl.darsonn.crafthome.bot.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class CommandsCreator {
    public void createCommands(JDA jda) {
        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                Commands.slash("invite", "Zaproszenie na serwer")
                        .setGuildOnly(false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        );

        commands.addCommands(
                Commands.slash("setup", "Ustawia odpowiedni system")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                        .addOption(STRING, "setup", "Wybierz system", true, true)
        );

        commands.addCommands(
                Commands.slash("changelog", "Wysyła wiadomość z nowościami na kanale changelog")
                        .setGuildOnly(false)
        );

        commands.addCommands(
                Commands.slash("purge", "Usuwa określoną ilość wiadomości")
                        .addOption(INTEGER, "amount", "Ile usunąć wiadomości? (Domyślnie jest ustawione 100)")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
        );

        commands.addCommands(
                Commands.slash("status", "Ustawia określony status serwera")
                        .addOption(STRING, "status", "Wpisz status", true, true)
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );

        commands.addCommands(
                Commands.slash("setusuer", "Ustawia określoną osobę jako opiekuna podań")
                        .addOptions(
                                new OptionData(USER, "user", "Wybierz użytkownika", true, false),
                                new OptionData(STRING, "type", "Wybierz typ podań", true, true)
                        )
                        .setGuildOnly(false)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        );

        commands.addCommands(
                Commands.slash("banuser", "Wypisuje nową wiadomość na kanale banroom")
                        .addOptions(
                                new OptionData(STRING, "user", "Wpisz nick gracza", true, false),
                                new OptionData(STRING, "powod", "Wpisz powód bana", true, false),
                                new OptionData(INTEGER, "czas", "Wpisz czas trwania bana w godzinach", true, false),
                                new OptionData(BOOLEAN, "odwolanie", "Czy jest możliwość odwołania się?", true, false)
                        )
                        .setGuildOnly(false)
        );

        commands.addCommands(
                Commands.slash("restart", "Restartuje bota")
                        .setGuildOnly(false)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        );

        commands.addCommands(
                Commands.context(Command.Type.USER, "Ekran")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS))
                        .setGuildOnly(false)
        );

        commands.addCommands(
                Commands.slash("giveaways", "Zobacz dostępne opcje związane z konkursami")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        );

        commands.addCommands(
                Commands.slash("memberinfo", "Zobacz podstawowe informacje o użytkowniku")
                        .addOption(USER, "user", "Wybierz użytkownika", false, false)
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        );

        commands.queue();
    }
}
