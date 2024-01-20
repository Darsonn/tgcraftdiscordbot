package pl.darsonn.crafthome.bot;

public class Utils {
    public static class Basics {
        private static final String serverName = "TgCraft";
        private static final String rulesChannelID = "1175771711090729031";
        private static final String welcomeChannelID = "1175771786424635412";
        private static final String verificationChannelID = "1175773706971254804";
        private static final String changelogChannelID = "1175943559455723581";
        private static final String pingRolesChannelID = "1178092855160471643";
        private static final String countingChannelID = "1195753440446533683";

        public static String getServerName() {
            return serverName;
        }

        public static String getRulesChannelID() {
            return rulesChannelID;
        }

        public static String getWelcomeChannelID() {
            return welcomeChannelID;
        }

        public static String getVerificationChannelID() {
            return verificationChannelID;
        }

        public static String getChangelogChannelID() {
            return changelogChannelID;
        }

        public static String getPingRolesChannelID() {
            return pingRolesChannelID;
        }

        public static String getCountingChannelID() {
            return countingChannelID;
        }
    }

    public static class StatisticsVoiceChannel {
        private static final String categoryID = "1175776786127265802";
        private static final String statusVoiceChannelID = "1175777045561749614";
        private static final String memberCounterVoiceChannelID = "1175780112965312564";
        private static final String newMemberVoiceChannelID = "1186063744107810877";

        public static String getCategoryID() {
            return categoryID;
        }
        public static String getStatusVoiceChannelID() {
            return statusVoiceChannelID;
        }

        public static String getMemberCounterVoiceChannelID() {
            return memberCounterVoiceChannelID;
        }

        public static String getNewMemberVoiceChannelID() {
            return newMemberVoiceChannelID;
        }
    }

    public static class ShopChannels {
        private static final String rulesChannelID = "1185700672277053441";
        private static final String priceListChannelID = "1185670601931767949";
        private static final String tutorialChannelID = "1185690442591903947";

        public static String getRulesChannelID() {
            return rulesChannelID;
        }

        public static String getPriceListChannelID() {
            return priceListChannelID;
        }

        public static String getTutorialChannelID() {
            return tutorialChannelID;
        }
    }

    public static class ApplicationChannels {
        private static final String statusChannelID = "1175804211112460360";
        private static final String resultsChannelID = "1175804183321006192";
        private static final String administrationChannelID = "1176229979349073950";
        private static final String developerChannelID = "1176230022047080448";
        private static final String creatorChannelID = "1176230082742857769";
        private static final String checkingChannelID = "1176257469534711929";

        public static String getStatusChannelID() {
            return statusChannelID;
        }

        public static String getResultsChannelID() {
            return resultsChannelID;
        }

        public static String getAdministrationChannelID() {
            return administrationChannelID;
        }

        public static String getDeveloperChannelID() {
            return developerChannelID;
        }

        public static String getCreatorChannelID() {
            return creatorChannelID;
        }

        public static String getCheckingChannelID() {
            return checkingChannelID;
        }
    }

    public static class TicketUtils {
        private static final String categoryID = "1176178980206948412";
        private static final String ticketLogsID = "1176179466456793088";
        private static final String openingTicketsChannelID = "1176179319689723995";

        public static String getCategoryID() {
            return categoryID;
        }

        public static String getTicketLogsID() {
            return ticketLogsID;
        }

        public static String getOpeningTicketsChannelID() {
            return openingTicketsChannelID;
        }
    }
}
