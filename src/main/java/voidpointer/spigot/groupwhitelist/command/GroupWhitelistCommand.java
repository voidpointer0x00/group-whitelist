package voidpointer.spigot.groupwhitelist.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.Setter;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import voidpointer.spigot.groupwhitelist.config.WhitelistConfig;
import voidpointer.spigot.groupwhitelist.event.WhitelistGroupRemoveEvent;
import voidpointer.spigot.groupwhitelist.locale.Locale;
import voidpointer.spigot.groupwhitelist.locale.LocaleKey;
import voidpointer.spigot.groupwhitelist.service.WhitelistService;

import java.util.Set;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed;
import static voidpointer.spigot.groupwhitelist.locale.LocaleKeys.*;

@CommandAlias("gwl|group-whitelist")
public final class GroupWhitelistCommand extends BaseCommand {
    @Dependency
    private WhitelistService whitelistService;
    @Setter
    @Dependency
    private WhitelistConfig whitelistConfig;
    @Dependency
    private Locale locale;
    @Dependency
    private Server server;

    @Subcommand("on")
    @CommandPermission("group-whitelist.on")
    @Description("Turns on group whitelist")
    private void on(final CommandSender sender) {
        sender.sendMessage(locale.get(whitelistService.turnOn() ? WHITELIST_ON : WHITELIST_ALREADY_ON));
    }

    @Subcommand("off")
    @CommandPermission("group-whitelist.off")
    @Description("Turns off group whitelist")
    private void off(final CommandSender sender) {
        sender.sendMessage(locale.get(whitelistService.turnOff() ? WHITELIST_OFF : WHITELIST_ALREADY_OFF));
    }

    @Subcommand("add")
    @CommandPermission("group-whitelist.add")
    @Description("Adds a group to the whitelist")
    private void add(final CommandSender sender, final String groupName) {
        sender.sendMessage(locale.get(
                whitelistService.add(groupName) ? GROUP_ADDED : GROUP_ALREADY_ADDED,
                unparsed("group", groupName)
        ));
    }

    @Subcommand("rm")
    @CommandPermission("group-whitelist.remove")
    @Description("Removes a group from the whitelist")
    private void remove(final CommandSender sender, final String groupName) {
        LocaleKey feedbackKey = GROUP_ALREADY_REMOVED;
        if (whitelistService.remove(groupName)) {
            feedbackKey = GROUP_REMOVED;
            server.getPluginManager().callEvent(
                    new WhitelistGroupRemoveEvent(Set.of(groupName), whitelistConfig.whitelistGroups()));
        }
        sender.sendMessage(locale.get(feedbackKey, unparsed("group", groupName)));
    }

    @Subcommand("ls")
    @CommandPermission("group-whitelist.list")
    @Description("Gets a list of whitelisted groups")
    private void list(final CommandSender sender) {
        final Set<String> whitelistedGroups = whitelistConfig.whitelistGroups();
        final String groupsFormatted = whitelistedGroups.stream()
                .collect(Collectors.joining(locale.raw(GROUPS_DELIMITER)));
        sender.sendMessage(locale.get(
                GROUPS_LIST,
                unparsed("total", String.valueOf(whitelistedGroups.size())),
                parsed("groups", groupsFormatted)
        ));
    }
}
