package voidpointer.spigot.groupwhitelist.listener;

import lombok.AllArgsConstructor;
import lombok.Setter;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import voidpointer.spigot.groupwhitelist.config.WhitelistConfig;
import voidpointer.spigot.groupwhitelist.locale.Locale;

import java.util.Optional;

import static net.luckperms.api.query.QueryMode.NON_CONTEXTUAL;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST;
import static voidpointer.spigot.groupwhitelist.locale.LocaleKeys.NOT_WHITELISTED;

@AllArgsConstructor
public final class PlayerListener implements Listener {
    private static final QueryOptions GROUPS_SEARCH_OPTIONS = QueryOptions.builder(NON_CONTEXTUAL)
            .flag(Flag.RESOLVE_INHERITANCE, true)
            .build();

    private final UserManager userManager = LuckPermsProvider.get().getUserManager();
    @Setter private WhitelistConfig whitelistConfig;
    @Setter private Locale locale;

    @Contract("_ -> this")
    public PlayerListener register(@NotNull final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        return this;
    }

    @EventHandler
    public void onPlayerPreLogin(final AsyncPlayerPreLoginEvent event) {
        if (!whitelistConfig.isEnabled())
            return;
        boolean isAllowed = Optional.ofNullable(userManager.getUser(event.getUniqueId()))
                .map(user -> user.getInheritedGroups(GROUPS_SEARCH_OPTIONS))
                .map(groups -> groups.stream().map(Group::getName).anyMatch(whitelistConfig::isGroupWhitelisted))
                .orElse(false);
        if (!isAllowed) {
            event.setLoginResult(KICK_WHITELIST);
            event.kickMessage(locale.get(NOT_WHITELISTED));
        }
    }
}
