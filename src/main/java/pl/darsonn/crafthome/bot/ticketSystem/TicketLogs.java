package pl.darsonn.crafthome.bot.ticketSystem;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import pl.darsonn.crafthome.bot.embedMessagesGenerator.EmbedMessageGenerator;

import java.sql.Timestamp;
import java.util.Objects;

public class TicketLogs {
    EmbedMessageGenerator embedMessageGenerator = new EmbedMessageGenerator();
    private final String ticketLogsChannelID = "1176179466456793088";
    public void createTicket(Member member, String channelID, Timestamp timestamp) {
        TextChannel ticketLogsChannel = member.getJDA().getTextChannelById(ticketLogsChannelID);
        embedMessageGenerator.sendInformationAboutCreationNewTicket(Objects.requireNonNull(ticketLogsChannel), member, channelID, timestamp);
    }

    public void deleteTicket(Member member, String id) {
        TextChannel ticketLogsChannel = member.getJDA().getTextChannelById(ticketLogsChannelID);
        Timestamp closeDate = new Timestamp(System.currentTimeMillis());
        embedMessageGenerator.sendInformationAboutDeletingTicket(Objects.requireNonNull(ticketLogsChannel), member, id, closeDate);
    }
}
