package voidpointer.spigot.groupwhitelist.listener;

import lombok.AllArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import voidpointer.spigot.groupwhitelist.config.WhitelistConfig;
import voidpointer.spigot.groupwhitelist.event.WhitelistGroupRemoveEvent;
import voidpointer.spigot.groupwhitelist.locale.Locale;

import java.util.Objects;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static net.luckperms.api.query.QueryMode.NON_CONTEXTUAL;
import static org.bukkit.Bukkit.getOnlinePlayers;
import static org.bukkit.Bukkit.getPlayer;
import static org.bukkit.event.player.PlayerKickEvent.Cause.WHITELIST;
import static voidpointer.spigot.groupwhitelist.locale.LocaleKeys.KICK_GROUP_REMOVED;

@Setter
@AllArgsConstructor
public final class WhitelistListener implements Listener {
    private static final QueryOptions GROUPS_SEARCH_OPTIONS = QueryOptions.builder(NON_CONTEXTUAL)
            .flag(Flag.RESOLVE_INHERITANCE, true)
            .build();
    private static final UserManager USER_MANAGER = LuckPermsProvider.get().getUserManager();

    private final Executor executorService =
            newSingleThreadExecutor(runnable -> new Thread(runnable, "WhitelistListenerExecutor"));
    private final Plugin plugin;
    private WhitelistConfig whitelistConfig;
    private Locale locale;

    @Contract("_ -> this")
    public WhitelistListener register(@NotNull final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        return this;
    }

    @EventHandler
    public void onGroupRemovedFromWhitelist(final WhitelistGroupRemoveEvent removeEvent) {
        if (!whitelistConfig.shouldKickRemovedGroups())
            return;
        if (removeEvent.isAsynchronous())
            kickNonWhitelistedOnlinePlayers(removeEvent);
        else
            executorService.execute(() -> kickNonWhitelistedOnlinePlayers(removeEvent));
    }

    private void kickNonWhitelistedOnlinePlayers(final WhitelistGroupRemoveEvent removeEvent) {
        final Component kickMessage = locale.get(KICK_GROUP_REMOVED);
        getOnlinePlayers().parallelStream()
                .map(Entity::getUniqueId)
                .map(USER_MANAGER::getUser)
                .filter(Objects::nonNull)
                .filter(user -> user.getInheritedGroups(GROUPS_SEARCH_OPTIONS).stream()
                        .map(Group::getName)
                        .anyMatch(groupName -> !removeEvent.getWhitelistedGroups().contains(groupName))
                ).map(user -> getPlayer(user.getUniqueId()))
                .filter(Objects::nonNull)
                .forEach(player -> player.getScheduler().run(plugin, scheduledTask -> player.kick(kickMessage, WHITELIST), null));
    }
}
