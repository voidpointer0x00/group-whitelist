package voidpointer.spigot.groupwhitelist.event;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
public final class WhitelistGroupRemoveEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Set<String> removedGroups;
    private final Set<String> whitelistedGroups;

    public WhitelistGroupRemoveEvent(final Set<String> removedGroups, final Set<String> whitelistedGroups) {
        super(!Bukkit.isPrimaryThread());
        this.removedGroups = removedGroups;
        this.whitelistedGroups = whitelistedGroups;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
