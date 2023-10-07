package io.github.defective4.javajam.tuisweeper.discord;

public class DiscordUser extends OrderedStructure {
    public String userId;
    public String username;
    public String discriminator;
    public String avatar;

    @Override
    public String toString() {
        return "DiscordUser{" +
               "userId='" + userId + '\'' +
               ", username='" + username + '\'' +
               ", discriminator='" + discriminator + '\'' +
               ", avatar='" + avatar + '\'' +
               '}';
    }
}
