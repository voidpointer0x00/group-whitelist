package voidpointer.spigot.groupwhitelist.service;

import lombok.AllArgsConstructor;
import lombok.Setter;
import voidpointer.spigot.groupwhitelist.config.WhitelistConfig;
import voidpointer.spigot.groupwhitelist.config.loader.ConfigLoader;

@AllArgsConstructor
public class WhitelistService {
    @Setter
    private WhitelistConfig whitelistConfig;
    private ConfigLoader<WhitelistConfig> whitelistConfigLoader;

    /**
     * Turns on the whitelist and returns
     * {@code true} if the whitelist was not on.
     */
    public boolean turnOn() {
        if (whitelistConfig.turnOn()) {
            whitelistConfigLoader.save(whitelistConfig);
            return true;
        }
        return false;
    }

    /**
     * Turns off the whitelist and returns
     * {@code true} if the whitelist was not off.
     */
    public boolean turnOff() {
        if (whitelistConfig.turnOff()) {
            whitelistConfigLoader.save(whitelistConfig);
            return true;
        }
        return false;
    }

    /**
     * Adds a given group to the whitelist and returns
     * {@code true} if whitelist <b>did not</b> contain
     * the given group before.
     */
    public boolean add(final String groupName) {
        if (whitelistConfig.addGroup(groupName)) {
            whitelistConfigLoader.save(whitelistConfig);
            return true;
        }
        return false;
    }

    /**
     * Removes a given group from the whitelist and returns
     * {@code true} if the given group was whitelisted before.
     */
    public boolean remove(final String groupName) {
        if (whitelistConfig.removeGroup(groupName)) {
            whitelistConfigLoader.save(whitelistConfig);
            return true;
        }
        return false;
    }
}
