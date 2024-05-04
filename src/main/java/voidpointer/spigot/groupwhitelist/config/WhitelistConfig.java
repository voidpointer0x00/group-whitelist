package voidpointer.spigot.groupwhitelist.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.luckperms.api.model.group.Group;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
@ConfigSerializable
public final class WhitelistConfig {
    @Comment("A list of groups that are allowed on the server")
    private Set<String> whitelistGroups = Set.of("whitelisted");

    @Comment("Whether to filter joining players based on whitelist group")
    private boolean isEnabled = false;

    @Comment("Whether to automatically kick players if their group was removed from the whitelist")
    private boolean shouldKickRemovedGroups = true;

    @Comment("Whether if the plugin should cancel server list ping event")
    private boolean shouldDropPing = false;

    /**
     * Turns on this whitelist and returns
     * {@code true} if the whitelist was not on.
     */
    public boolean turnOn() {
        return !isEnabled && (isEnabled = true);
    }

    /**
     * Turns off this whitelist and returns
     * {@code true} if the whitelist was not off.
     */
    public boolean turnOff() {
        //noinspection ConstantValue
        return isEnabled && !(isEnabled = false);
    }

    public boolean isGroupWhitelisted(final Group group) {
        return whitelistGroups != null && whitelistGroups.contains(group.getName());
    }

    public Set<String> whitelistGroups() {
        return whitelistGroups == null ? emptySet() : unmodifiableSet(whitelistGroups);
    }

    /**
     * Adds a given group to this whitelist and returns
     * {@code true} if whitelist <b>did not</b> contain
     * the given group before.
     */
    public boolean addGroup(final String groupName) {
        try {
            return this.whitelistGroups.add(groupName);
        } catch (final UnsupportedOperationException unsupportedOperationException) {
            Set<String> whitelistGroups = ConcurrentHashMap.newKeySet();
            whitelistGroups.addAll(this.whitelistGroups);
            boolean isNewlyAdded = whitelistGroups.add(groupName);
            this.whitelistGroups = whitelistGroups;
            return isNewlyAdded;
        }
    }

    /**
     * Removes a given group from this whitelist and returns
     * {@code true} if the given group was whitelisted before.
     */
    public boolean removeGroup(final String groupName) {
        try {
            return this.whitelistGroups.remove(groupName);
        } catch (final UnsupportedOperationException unsupportedOperationException) {
            Set<String> whitelistGroups = ConcurrentHashMap.newKeySet();
            whitelistGroups.addAll(this.whitelistGroups);
            boolean wasRemoved = whitelistGroups.remove(groupName);
            this.whitelistGroups = whitelistGroups;
            return wasRemoved;
        }
    }
}
