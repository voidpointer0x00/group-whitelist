package voidpointer.spigot.groupwhitelist;

import co.aikar.commands.PaperCommandManager;
import org.bukkit.plugin.java.JavaPlugin;
import voidpointer.spigot.groupwhitelist.command.GroupWhitelistCommand;
import voidpointer.spigot.groupwhitelist.config.LocaleConfig;
import voidpointer.spigot.groupwhitelist.config.WhitelistConfig;
import voidpointer.spigot.groupwhitelist.config.loader.ConfigLoader;
import voidpointer.spigot.groupwhitelist.config.reload.AutoReloadConfigService;
import voidpointer.spigot.groupwhitelist.listener.PlayerListener;
import voidpointer.spigot.groupwhitelist.locale.Locale;
import voidpointer.spigot.groupwhitelist.service.WhitelistService;

public final class GroupWhitelistPlugin extends JavaPlugin {
    private AutoReloadConfigService autoReloadConfigService;

    private ConfigLoader<WhitelistConfig> whitelistConfigLoader;
    private WhitelistConfig whitelistConfig;
    private LocaleConfig localeConfig;

    private WhitelistService whitelistService;

    private PlayerListener playerListener;
    private GroupWhitelistCommand groupWhitelistCommand;

    @Override public void onLoad() {
        loadConfiguration();
        getSLF4JLogger().info("Loaded");
    }

    @Override public void onEnable() {
        whitelistService = new WhitelistService(whitelistConfig, whitelistConfigLoader);
        /* events */
        this.playerListener = new PlayerListener(whitelistConfig, localeConfig).register(this);
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
            autoReloadConfigService.subscribeToReload(whitelistConfigLoader, updated -> {
                if (playerListener != null)
                    playerListener.setWhitelistConfig(updated);
                if (whitelistService != null)
                    whitelistService.setWhitelistConfig(updated);
                if (groupWhitelistCommand != null)
                    groupWhitelistCommand.setWhitelistConfig(updated);
                getSLF4JLogger().info("Reloaded whitelist config");
            });
            autoReloadConfigService.subscribeToReload(localeConfigLoader, updated -> {
                if (playerListener != null)
                    playerListener.setLocale(updated);
                getSLF4JLogger().info("Reloaded locale config");
            });
        }
    }
}
