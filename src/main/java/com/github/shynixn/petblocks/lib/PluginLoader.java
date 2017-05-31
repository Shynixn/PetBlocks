package com.github.shynixn.petblocks.lib;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

public final class PluginLoader {
    private PluginLoader() {
        super();
    }

    public static void load(JavaPlugin plugin, Class<?>... classes) {
        for (Class<?> tClass : classes) {
            do {
                try {
                    for (final Field field : tClass.getDeclaredFields()) {
                        if (field.isAnnotationPresent(PluginLoad.class)) {
                            field.setAccessible(true);
                            field.set(null, plugin);
                        }
                    }
                    for (final Method method : tClass.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(PluginLoad.class)) {
                            method.setAccessible(true);
                            method.invoke(null, plugin);
                        }
                    }
                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to load plugin loader.", e);
                }
                tClass = tClass.getSuperclass();
            } while (tClass != null);
        }
    }

    public static void unload(JavaPlugin plugin, Class<?>... classes) {
        for (Class<?> tClass : classes) {
            do {
                try {
                    for (final Method method : tClass.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(PluginUnload.class)) {
                            method.setAccessible(true);
                            method.invoke(null, plugin);
                        }
                    }
                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to disable pluginloader.", e);
                }
                tClass = tClass.getSuperclass();
            } while (tClass != null);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({CONSTRUCTOR, FIELD, METHOD})
    public @interface PluginLoad {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(METHOD)
    public @interface PluginUnload {
    }
}
