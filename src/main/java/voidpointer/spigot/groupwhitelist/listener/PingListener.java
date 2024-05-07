package voidpointer.spigot.groupwhitelist.listener;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import voidpointer.spigot.groupwhitelist.config.WhitelistConfig;

public interface PingListener {
    @Contract("_ -> this")
    @NotNull PingListener register(final @NotNull Plugin plugin);

    void setWhitelistConfig(@NotNull WhitelistConfig whitelistConfig);
}
