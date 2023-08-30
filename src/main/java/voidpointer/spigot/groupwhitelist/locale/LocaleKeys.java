package voidpointer.spigot.groupwhitelist.locale;

public enum LocaleKeys implements LocaleKey {
    NOT_WHITELISTED("<red>You are not whitelisted on this server."),
    GROUP_ADDED("<dark_gray> » <white>Group <green><group></green> is now allowed on this server."),
    GROUP_REMOVED("<dark_gray> » <white>Group <red><group></red> is no longer allowed on this server."),
    GROUP_ALREADY_ADDED("<dark_gray> » <white>Group <green><group></green> is <u>already</u> allowed on this server."),
    GROUP_ALREADY_REMOVED("<dark_gray> » <white>Group <red><group></red> is <u>already</u> not allowed on this server."),
    GROUPS_LIST("<dark_gray> » <white>Allowed groups (<green><total></green>): <green><groups></green>."),
    GROUPS_DELIMITER("</green><white>, </white><green>"),
    WHITELIST_ON("<dark_gray> » <white>Group whitelist is now turned <green>on</green>."),
    WHITELIST_ALREADY_ON("<dark_gray> » <white>Group whitelist is <u>already</u> turned <green>on</green>."),
    WHITELIST_OFF("<dark_gray> » <white>Group whitelist is now turned <red>off</red>."),
    WHITELIST_ALREADY_OFF("<dark_gray> » <white>Group whitelist is <u>already</u> turned <red>off</red>."),
    KICK_GROUP_REMOVED("<red>Your group is no longer allowed on this server."),
    KICK_NO_LONGER_IN_GROUP("<red>You are no longer allowed on this server.");

    private final String key, value;

    LocaleKeys(final String value) {
        this.key = toString().toLowerCase().replace('_', '-');
        this.value = value;
    }

    @Override public String key() {
        return key;
    }

    @Override public String value() {
        return value;
    }
}
