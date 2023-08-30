package voidpointer.spigot.groupwhitelist.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public interface Locale {
    String raw(LocaleKey localeKey);

    Component get(LocaleKey localeKey);

    Component get(LocaleKey localeKey, TagResolver... resolvers);
}
