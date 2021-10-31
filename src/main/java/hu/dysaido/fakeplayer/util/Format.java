package hu.dysaido.fakeplayer.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Format {

    private Format() {
    }

    public static List<String> strings(String[] messages) {
        return strings(Arrays.asList(messages));
    }

    public static List<String> strings(List<String> messages) {
        return messages.stream().map(Format::color).collect(Collectors.toList());
    }

    public static void broadcast(String message) {
        for (Player user : Bukkit.getOnlinePlayers()) {
            user.sendMessage(color(message));
        }
        Bukkit.getServer().getConsoleSender().sendMessage(color(message));
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
