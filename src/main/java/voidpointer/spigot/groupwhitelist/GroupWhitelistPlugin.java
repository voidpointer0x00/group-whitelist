package voidpointer.spigot.groupwhitelist;

import co.aikar.commands.PaperCommandManager;
import org.bukkit.plugin.java.JavaPlugin;
import voidpointer.spigot.groupwhitelist.command.GroupWhitelistCommand;
import voidpointer.spigot.groupwhitelist.config.LocaleConfig;
import voidpointer.spigot.groupwhitelist.config.WhitelistConfig;
import voidpointer.spigot.groupwhitelist.config.loader.ConfigLoader;
import voidpointer.spigot.groupwhitelist.config.reload.AutoReloadConfigService;
import voidpointer.spigot.groupwhitelist.event.WhitelistGroupRemoveEvent;
import voidpointer.spigot.groupwhitelist.listener.PingListener;
import voidpointer.spigot.groupwhitelist.listener.PlayerListener;
import voidpointer.spigot.groupwhitelist.listener.WhitelistListener;
import voidpointer.spigot.groupwhitelist.locale.Locale;
import voidpointer.spigot.groupwhitelist.service.WhitelistService;

import java.util.Set;
import java.util.stream.Collectors;

public final class GroupWhitelistPlugin extends JavaPlugin {
    private AutoReloadConfigService autoReloadConfigService;

    private ConfigLoader<WhitelistConfig> whitelistConfigLoader;
    private WhitelistConfig whitelistConfig;
    private LocaleConfig localeConfig;

    private WhitelistService whitelistService;

    private PingListener pingListener;
    private PlayerListener playerListener;
    private WhitelistListener whitelistListener;
    private GroupWhitelistCommand groupWhitelistCommand;

    @Override public void onLoad() {
        loadConfiguration();
        getSLF4JLogger().info("Loaded");
    }

    @Override public void onEnable() {
        whitelistService = new WhitelistService(whitelistConfig, whitelistConfigLoader);
        /* events */
        this.pingListener = new PingListener(whitelistConfig).register(this);
        this.playerListener = new PlayerListener(whitelistConfig, localeConfig).register(this);
        this.whitelistListener = new WhitelistListener(this, whitelistConfig, localeConfig).register(this);
        /* commands */
        var cmdManager = new PaperCommandManager(this);
        cmdManager.registerDependency(WhitelistConfig.class, whitelistConfig);
        cmdManager.registerDependency(WhitelistService.class, whitelistService);
        cmdManager.registerDependency(Locale.class, localeConfig);
        groupWhitelistCommand = new GroupWhitelistCommand();
        cmdManager.registerCommand(groupWhitelistCommand);

        getSLF4JLogger().info("Enabled");
    }

    @Override public void onDisable() {
        if (autoReloadConfigService != null)
            autoReloadConfigService.shutdown();
        else
            getSLF4JLogger().error("Auto reload config service was not initialized, skipped shutdown");
        if (whitelistListener != null)
            whitelistListener.shutdown();
        getSLF4JLogger().info("Disabled");
    }

    private void loadConfiguration() {
        getSLF4JLogger().trace("Loading configuration");
        this.whitelistConfigLoader = new ConfigLoader<>(getDataFolder().toPath(), WhitelistConfig.class);
        this.whitelistConfig = whitelistConfigLoader.loadAndSaveDefaultIfNotExists();
        var localeConfigLoader = new ConfigLoader<>(getDataFolder().toPath(), LocaleConfig.class);
        this.localeConfig = localeConfigLoader.loadAndSaveDefaultIfNotExists();

        autoReloadConfigService = new AutoReloadConfigService();
        if (autoReloadConfigService.startWatchingForModifications()) {
            autoReloadConfigService.subscribeToReload(whitelistConfigLoader, this::onConfigReload);
            autoReloadConfigService.subscribeToReload(localeConfigLoader, this::onLocaleReload);
        }
    }

    private void onConfigReload(final WhitelistConfig whitelistConfig) {
        if (pingListener != null)
            pingListener.setWhitelistConfig(whitelistConfig);
        if (playerListener != null)
            playerListener.setWhitelistConfig(whitelistConfig);
        if (whitelistListener != null)
            whitelistListener.setWhitelistConfig(whitelistConfig);
        if (whitelistService != null)
            whitelistService.setWhitelistConfig(whitelistConfig);
        if (groupWhitelistCommand != null)
            groupWhitelistCommand.setWhitelistConfig(whitelistConfig);
        Set<String> newlyWhitelistedGroups = whitelistConfig.whitelistGroups();
        Set<String> removedGroups = this.whitelistConfig.whitelistGroups().stream()
                .filter((group) -> !newlyWhitelistedGroups.contains(group))
                .collect(Collectors.toSet());
        if (!removedGroups.isEmpty()) {
            var removedGroupsEvent = new WhitelistGroupRemoveEvent(removedGroups, newlyWhitelistedGroups);
            getServer().getPluginManager().callEvent(removedGroupsEvent);
        }
        this.whitelistConfig = whitelistConfig;
        getSLF4JLogger().info("Reloaded whitelist config");
    }

    private void onLocaleReload(final LocaleConfig localeConfig) {
        if (pingListener != null)
            pingListener.setWhitelistConfig(whitelistConfig);
        if (playerListener != null)
            playerListener.setLocale(localeConfig);
        if (whitelistListener != null)
            whitelistListener.setLocale(localeConfig);
        getSLF4JLogger().info("Reloaded locale config");
    }
}
