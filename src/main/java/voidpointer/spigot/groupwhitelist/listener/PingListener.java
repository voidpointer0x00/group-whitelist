package voidpointer.spigot.groupwhitelist.listener;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import voidpointer.spigot.groupwhitelist.config.WhitelistConfig;

@Setter
@AllArgsConstructor
public final class PingListener implements Listener {
    private WhitelistConfig whitelistConfig;

    @Contract("_ -> this")
    public PingListener register(@NotNull final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        return this;
    }

    @EventHandler
    private void dropForNonWhitelisted(@NotNull final PaperServerListPingEvent event) {
        if (whitelistConfig.isEnabled())
            event.setCancelled(whitelistConfig.hideStatus());
    }
}
