package pl.darsonn.crafthome;

import pl.darsonn.crafthome.bot.DiscordBot;

public class Main {
    public static void main(String[] args) {
        DiscordBot discordBot = new DiscordBot();

        discordBot.startBot(args[0]);
    }
}