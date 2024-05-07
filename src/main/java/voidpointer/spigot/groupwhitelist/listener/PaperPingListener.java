package voidpointer.spigot.groupwhitelist.listener;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import voidpointer.spigot.groupwhitelist.config.WhitelistConfig;

@Slf4j
@Setter
@AllArgsConstructor
public final class PaperPingListener implements PingListener, Listener {
    private WhitelistConfig whitelistConfig;

    public @NotNull PaperPingListener register(@NotNull final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        return this;
    }

    @EventHandler
    private void dropForNonWhitelisted(@NotNull final PaperServerListPingEvent event) {
        if (whitelistConfig.isEnabled())
            event.setCancelled(whitelistConfig.hideStatus());
        /* TODO because the secret is now in hostname, it is possible to compare it here, so do that */
    }
}
