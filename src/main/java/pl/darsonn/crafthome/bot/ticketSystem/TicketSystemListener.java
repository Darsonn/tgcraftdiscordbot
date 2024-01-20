package pl.darsonn.crafthome.bot.ticketSystem;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import pl.darsonn.crafthome.bot.Utils;
import pl.darsonn.crafthome.bot.embedMessagesGenerator.EmbedMessageGenerator;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.EnumSet;

public class TicketSystemListener extends ListenerAdapter {
    TicketsDatabaseOperations databaseOperations = new TicketsDatabaseOperations();
    EmbedMessageGenerator embedMessageGenerator = new EmbedMessageGenerator();
    TicketLogs ticketLogs = new TicketLogs();

    public void interactionListener(ButtonInteractionEvent event, Button component) {
        switch (Objects.requireNonNull(component.getId())) {
            case "main-open-ticket" -> createTicket(event);
            case "close-ticket" -> removeTicket(event);
        }
    }

    public void createTicket(ButtonInteractionEvent event) {
        Category category = Objects.requireNonNull(event.getGuild()).getCategoryById(Utils.TicketUtils.getCategoryID());
        String textChannel = "ticket-"+ Objects.requireNonNull(event.getMember()).getEffectiveName();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        if(event.getJDA().getTextChannelsByName(textChannel, true).isEmpty()) {
            ChannelAction<TextChannel> channelAction = Objects.requireNonNull(category).createTextChannel(textChannel);
            channelAction.addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null);
            channelAction.queue(channel -> {
                String channelID = channel.getId();
                databaseOperations.createTicket(event.getMember(), "ticket", channel, timestamp);
                sendPanelInTicket(event, channelID, "ticket", timestamp);
                event.reply("Utworzono ticket " + channel.getAsMention()).setEphemeral(true).queue();
            });
        } else {
            event.reply("Nie możesz utworzyć nowego ticketa, najpierw musisz usunąć poprzedni!").setEphemeral(true).queue();
        }
    }

    public void sendPanelInTicket(ButtonInteractionEvent event, String channelID, String ticketType, Timestamp timestamp) {
        TextChannel ticket = event.getJDA().getTextChannelById(channelID);
        ticketLogs.createTicket(Objects.requireNonNull(event.getMember()), channelID, timestamp);
        embedMessageGenerator.sendPanelInTicket(ticket, event.getMember(), ticketType);
    }

    public void removeTicket(ButtonInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();

        channel.delete().reason("Ticket closed.").queue();

        ticketLogs.deleteTicket(Objects.requireNonNull(event.getMember()), channel.getId());
    }
}
