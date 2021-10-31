package hu.dysaido.fakeplayer.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public class LocationUtil {

    public static String key(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static void setLocation(Location location, ConfigurationSection configuration) {
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(configuration, "Section cannot be null");
        World w = location.getWorld();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        configuration.set("world", w.getName());
        configuration.set("x", x);
        configuration.set("y", y);
        configuration.set("z", z);
        configuration.set("yaw", location.getYaw());
        configuration.set("pitch", location.getPitch());
    }

    public static Location getLocation(ConfigurationSection configuration) {
        Objects.requireNonNull(configuration, "Section cannot be null");
        World w = Bukkit.getServer().getWorld(configuration.getString("world"));
        double x = configuration.getDouble("x");
        double y = configuration.getDouble("y");
        double z = configuration.getDouble("z");
        double yaw = configuration.getDouble("yaw");
        double pitch = configuration.getDouble("pitch");
        return new Location(w, x, y, z, (float) yaw, (float) pitch);
    }

}
