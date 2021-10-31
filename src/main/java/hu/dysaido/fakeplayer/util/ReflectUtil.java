package hu.dysaido.fakeplayer.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public class ReflectUtil {

    private static final String PATH = Bukkit.getServer().getClass().getPackage().getName();

    private ReflectUtil() {
    }

    public static Class<?> getOBCClass(String name) {
        final String localPath = "org.bukkit.craftbukkit.";
        Class<?> clazz = null;
        try {
            clazz = Class.forName(localPath + PATH.split("\\.")[3] + "." + name);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning(localPath + PATH + name);
        }
        return clazz;
    }

    public static Class<?> getNMSClass(String name) {
        final String localPath = "net.minecraft.server.";
        Class<?> clazz = null;
        try {
            clazz = Class.forName(localPath + PATH.split("\\.")[3] + "." + name);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning(localPath + PATH + name);
        }
        return clazz;
    }


    public static Class<?> getOBCArrayClass(String name) {
        final String localPath = "[Lorg.bukkit.craftbukkit.";
        Class<?> clazz = null;
        try {
            clazz = Class.forName(localPath + PATH.split("\\.")[3] + "." + name + ";");
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning(localPath + PATH + name);
        }
        return clazz;
    }

    public static Class<?> getNMSArrayClass(String name) {
        final String localPath = "[Lnet.minecraft.server.";
        Class<?> clazz = null;
        try {
            clazz = Class.forName(localPath + PATH.split("\\.")[3] + "." + name + ";");
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning(localPath + PATH + name);
        }
        return clazz;
    }

    public static Object getField(final Object object, final String fieldName) {
        Object fieldValue = null;
        try {
            if (object == null) throw new IllegalAccessException("Tried to access field from a null object");
            final Field field = object.getClass().getField(fieldName);
            final boolean accessible = field.isAccessible();
            field.setAccessible(true);
            fieldValue = field.get(object);
            field.setAccessible(accessible);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fieldValue;
    }

    public static Object getDeclaredField(final Object object, final String fieldName) {
        Object fieldValue = null;
        try {
            if (object == null) throw new IllegalAccessException("Tried to access field from a null object");
            final Field field = object.getClass().getDeclaredField(fieldName);
            final boolean accessible = field.isAccessible();
            field.setAccessible(true);
            fieldValue = field.get(object);
            field.setAccessible(accessible);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fieldValue;
    }
}
