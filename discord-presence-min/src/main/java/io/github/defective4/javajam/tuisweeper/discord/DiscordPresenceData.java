package io.github.defective4.javajam.tuisweeper.discord;

/**
 * Presence data that can be presented to Discord API
 *
 * @author Defective
 */
public class DiscordPresenceData extends OrderedStructure {
    public static class Builder {

        private String state;
        private String details;
        private long startTimestamp;
        private long endTimestamp;
        private String largeImageKey;
        private String largeImageText;
        private String smallImageKey;
        private String smallImageText;
        private String partyId;
        private int partySize;
        private int partyMax;
        private int partyPrivacy;
        private String matchSecret;
        private String joinSecret;
        private String spectateSecret;

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public Builder setDetails(String details) {
            this.details = details;
            return this;
        }

        public Builder setStartTimestamp(long startTimestamp) {
            this.startTimestamp = startTimestamp;
            return this;
        }

        public Builder setEndTimestamp(long endTimestamp) {
            this.endTimestamp = endTimestamp;
            return this;
        }

        public Builder setLargeImageKey(String largeImageKey) {
            this.largeImageKey = largeImageKey;
            return this;
        }

        public Builder setLargeImageText(String largeImageText) {
            this.largeImageText = largeImageText;
            return this;
        }

        public Builder setSmallImageKey(String smallImageKey) {
            this.smallImageKey = smallImageKey;
            return this;
        }

        public Builder setSmallImageText(String smallImageText) {
            this.smallImageText = smallImageText;
            return this;
        }

        public Builder setPartyId(String partyId) {
            this.partyId = partyId;
            return this;
        }

        public Builder setPartySize(int partySize) {
            this.partySize = partySize;
            return this;
        }

        public Builder setPartyMax(int partyMax) {
            this.partyMax = partyMax;
            return this;
        }

        public Builder setPartyPrivacy(int partyPrivacy) {
            this.partyPrivacy = partyPrivacy;
            return this;
        }

        public Builder setMatchSecret(String matchSecret) {
            this.matchSecret = matchSecret;
            return this;
        }

        public Builder setJoinSecret(String joinSecret) {
            this.joinSecret = joinSecret;
            return this;
        }

        public Builder setSpectateSecret(String spectateSecret) {
            this.spectateSecret = spectateSecret;
            return this;
        }

        public DiscordPresenceData createDiscordPresenceData() {
            return new DiscordPresenceData(state,
                                           details,
                                           startTimestamp,
                                           endTimestamp,
                                           largeImageKey,
                                           largeImageText,
                                           smallImageKey,
                                           smallImageText,
                                           partyId,
                                           partySize,
                                           partyMax,
                                           partyPrivacy,
                                           matchSecret,
                                           joinSecret,
                                           spectateSecret);
        }
    }

    public String state;   /* max 128 bytes */
    public String details; /* max 128 bytes */
    public long startTimestamp;
    public long endTimestamp;
    public String largeImageKey;  /* max 32 bytes */
    public String largeImageText; /* max 128 bytes */
    public String smallImageKey;  /* max 32 bytes */
    public String smallImageText; /* max 128 bytes */
    public String partyId;        /* max 128 bytes */
    public int partySize;
    public int partyMax;
    public int partyPrivacy;
    public String matchSecret;    /* max 128 bytes */
    public String joinSecret;     /* max 128 bytes */
    public String spectateSecret; /* max 128 bytes */
    public int instance = 1;

    public DiscordPresenceData(String state, String details, long startTimestamp, long endTimestamp, String largeImageKey, String largeImageText, String smallImageKey, String smallImageText, String partyId, int partySize, int partyMax, int partyPrivacy, String matchSecret, String joinSecret, String spectateSecret) {
        this.state = state;
        this.details = details;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.largeImageKey = largeImageKey;
        this.largeImageText = largeImageText;
        this.smallImageKey = smallImageKey;
        this.smallImageText = smallImageText;
        this.partyId = partyId;
        this.partySize = partySize;
        this.partyMax = partyMax;
        this.partyPrivacy = partyPrivacy;
        this.matchSecret = matchSecret;
        this.joinSecret = joinSecret;
        this.spectateSecret = spectateSecret;
    }
}
