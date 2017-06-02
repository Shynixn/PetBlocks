package com.github.shynixn.petblocks.lib;

import org.bukkit.plugin.java.JavaPlugin;

@Deprecated
public abstract class AsyncRunnable implements Runnable {
    @PluginLoader.PluginLoad
    private static JavaPlugin plugin;
    private boolean isSynchrone;
    private Object[] paramcache;

    private static boolean isPrimaryThread() {
        final Thread mainThread = (Thread) ReflectionLib.getValueFromField("primaryThread", ReflectionLib.invokeMethodByClazz(ReflectionLib.getClassFromName("net.minecraft.server.VERSION.MinecraftServer"), "getServer"));
        return Thread.currentThread() != mainThread;
    }

    public static void throwExceptionIfSynchroneThread() {
        if (isPrimaryThread())
            throw new RuntimeException("Cannot access data from primary thread!");
    }

    public static void throwExceptionIfAsnychroneThread() {
        if (!isPrimaryThread())
            throw new RuntimeException("Cannot access data from secondary thread!");
    }

    public <T> T getParam(int number) {
        if (this.paramcache.length > number && number >= 0)
            return (T) this.paramcache[number];
        return null;
    }

    public boolean isSynchrone() {
        return this.isSynchrone;
    }

    public static void toAsynchroneThread(final AsyncRunnable runnable, final Object... params) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            runnable.paramcache = params;
            runnable.isSynchrone = false;
            runnable.run();
        });
    }

    public static void toSynchroneThread(final AsyncRunnable runnable, final Object... params) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            runnable.paramcache = params;
            runnable.isSynchrone = true;
            runnable.run();
        });
    }
}
