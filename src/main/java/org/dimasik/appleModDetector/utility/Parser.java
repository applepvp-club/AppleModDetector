package org.dimasik.appleModDetector.utility;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Parser {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .build();
    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final LegacyComponentSerializer AMPERSAND = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final Pattern COLOR_TAG_PATTERN = Pattern.compile(
            "<(black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white|#[0-9a-fA-F]{6})>"
    );

    @Deprecated
    public static String legacy(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', convertHexToLegacy(message));
    }

    public static Component color(String message) {
        if (message == null) return Component.empty();
        message = SECTION.serialize(AMPERSAND.deserialize(convertHexToLegacy(message)));
        message = message.replaceAll(
                "§x§([0-9a-fA-F])§([0-9a-fA-F])§([0-9a-fA-F])§([0-9a-fA-F])§([0-9a-fA-F])§([0-9a-fA-F])",
                "<#$1$2$3$4$5$6>"
        );
        message = message
                .replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>")
                .replace("§n", "<underlined>")
                .replace("§m", "<strikethrough>>")
                .replace("§k", "<obfuscated>")
                .replace("§o", "<italic>")
                .replace("§l", "<bold>")
                .replace("§r", "<reset>");
        message = COLOR_TAG_PATTERN.matcher(message).replaceAll("<reset>$0");
        return MINI_MESSAGE.deserialize(message);
    }

    public static Component colorReset(String message) {
        return join(color(message));
    }

    public static String serialise(Component component) {
        return MINI_MESSAGE.serialize(component);
    }

    public static String legacy(Component component) {
        return SECTION.serialize(component);
    }

    public static Component join(Component... components) {
        if (components.length == 0) return Component.empty();

        Component reset = Component.text("").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, false)
                .decoration(TextDecoration.UNDERLINED, false)
                .decoration(TextDecoration.STRIKETHROUGH, false)
                .decoration(TextDecoration.OBFUSCATED, false);

        Component result = reset;
        Style lastStyle = reset.style();
        for (Component current : components) {
            if (current.style().isEmpty())
                current = current.style(lastStyle);
            else
                lastStyle = current.style();
            result = result.append(current);
        }

        return result;
    }

    public static Component empty() {
        return Component.empty();
    }

    public static String strip(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static String convertHexToLegacy(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public String convertHealthGray(double health, String decimalColor) {
        if (health <= 2) {
            String result = String.format("%.1f", health);
            return result.replaceAll("([.,]\\d+)", decimalColor + "$1");
        } else {
            return String.valueOf((int) health);
        }
    }
}