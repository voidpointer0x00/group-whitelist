package voidpointer.spigot.groupwhitelist.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import voidpointer.spigot.groupwhitelist.locale.Locale;
import voidpointer.spigot.groupwhitelist.locale.LocaleKey;
import voidpointer.spigot.groupwhitelist.locale.LocaleKeys;

import java.util.HashMap;
import java.util.Map;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

@ConfigSerializable
public final class LocaleConfig implements Locale {
    private Map<String, String> messages;

    public LocaleConfig() {
        LocaleKeys[] defaultKeys = LocaleKeys.values();
        messages = new HashMap<>(defaultKeys.length);
        for (final LocaleKey defaultKey : defaultKeys)
            messages.putIfAbsent(defaultKey.key(), defaultKey.value());
    }

    @Override public String raw(final LocaleKey localeKey) {
        return messages.getOrDefault(localeKey.key(), localeKey.value());
    }

    @Override public Component get(final LocaleKey localeKey) {
        return miniMessage().deserialize(messages.getOrDefault(localeKey.key(), localeKey.value()));
    }

    @Override public Component get(final LocaleKey localeKey, final TagResolver... resolvers) {
        return miniMessage().deserialize(messages.getOrDefault(localeKey.key(), localeKey.value()), resolvers);
    }
}
