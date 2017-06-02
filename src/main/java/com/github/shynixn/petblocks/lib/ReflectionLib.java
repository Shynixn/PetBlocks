package com.github.shynixn.petblocks.lib;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Created by Shynixn
 */

@Deprecated
public final class ReflectionLib {

    public static Object invokeConstructor(Class<?> clazz, Object... params) {
        do {
            for (final Constructor constructor : clazz.getDeclaredConstructors()) {
                try {
                    constructor.setAccessible(true);
                    return constructor.newInstance(params);
                } catch (final Exception ignored) {

                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        throw new RuntimeException("Cannot find correct constructor.");
    }

    public static Object invokeMethodByObject(Object object, String name, Object... params) {
        Class<?> clazz = object.getClass();
        do {
            for (final Method method : clazz.getDeclaredMethods()) {
                try {
                    if (method.getName().equalsIgnoreCase(name)) {
                        method.setAccessible(true);
                        return method.invoke(object, params);
                    }
                } catch (final Exception ignored) {

                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        clazz = object.getClass();
        do {
            for (final Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equalsIgnoreCase(name)) {
                    Bukkit.getLogger().log(Level.WARNING, "Unknown: [" + method.getName() + "] " + method.getParameterTypes().length);
                    Bukkit.getLogger().log(Level.WARNING, "Calling params: ");
                    for (final Object param : params) {
                        Bukkit.getLogger().log(Level.WARNING, param.getClass().getName());
                    }
                    Bukkit.getLogger().log(Level.WARNING, "Method params: ");
                    for (final Class<?> cl : method.getParameterTypes()) {
                        Bukkit.getLogger().log(Level.WARNING, cl.getName());
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        throw new RuntimeException("Cannot find correct method.");
    }

    public static Object getValueFromField(String fieldName, Object object) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            for (final Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    field.setAccessible(true);
                    try {
                        return field.get(object);
                    } catch (final IllegalAccessException e) {
                        Bukkit.getLogger().log(Level.WARNING, "Access violation.", e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static Object invokeMethodByClazz(Class<?> clazz, String name, Object... params) {
        do {
            for (final Method method : clazz.getDeclaredMethods()) {
                try {
                    if (method.getName().equalsIgnoreCase(name)) {
                        method.setAccessible(true);
                        return method.invoke(null, params);
                    }
                } catch (final Exception ignored) {
                    ignored.printStackTrace();
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        throw new RuntimeException("Cannot find correct method.");
    }

    public static Class<?> getClassFromName(String name) {
        try {
            return Class.forName(name.replace("VERSION", BukkitUtilities.getServerVersion()));
        } catch (final Exception e) {
            throw new RuntimeException("Cannot find correct class [" + name + "] " + e.getMessage());
        }
    }
}

