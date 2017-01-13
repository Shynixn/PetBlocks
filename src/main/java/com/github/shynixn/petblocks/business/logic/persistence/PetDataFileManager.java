package com.github.shynixn.petblocks.business.logic.persistence;

import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.github.shynixn.petblocks.api.entities.MoveType;
import com.github.shynixn.petblocks.api.entities.Movement;
import com.github.shynixn.petblocks.api.entities.Particle;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.lib.AsyncRunnable;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.ParticleBuilder;
import com.github.shynixn.petblocks.lib.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.PersistenceException;
import java.io.File;
import java.util.logging.Level;

class PetDataFileManager {
    private final JavaPlugin plugin;

    PetDataFileManager(JavaPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    private File getFolder() {
        final File file = new File(this.plugin.getDataFolder(), "Pets");
        if (!file.exists() && !file.mkdir())
            Bukkit.getLogger().log(Level.WARNING, "Cannot create resource folder.");
        return file;
    }

    private boolean isSqlEnabled() {
        return this.plugin.getConfig().getBoolean("sql-enabled");
    }

    void connectToDataBase() {
        AsyncRunnable.toAsynchroneThread(new AsyncRunnable() {
            @Override
            public void run() {
                if (PetDataFileManager.this.isSqlEnabled()) {
                    BukkitUtilities.sendColorMessage("Connecting to database...'", ChatColor.GREEN, PetBlocksPlugin.PREFIX_CONSOLE);
                    try {
                        PetDataFileManager.this.plugin.getDatabase().find(PetData.class).findRowCount();
                        final SqlQuery query = PetDataFileManager.this.plugin.getDatabase().createSqlQuery("DESCRIBE `petblock`");
                        boolean found = false;
                        boolean foundbreak = false;
                        boolean foundSound = false;
                        for (SqlRow row : query.findList()) {
                            if (row.getString("field").equals("movement")) {
                                found = true;
                            }
                            if (row.getString("field").equals("sounds")) {
                                foundSound = true;
                            }
                            if (row.getString("field").equals("unbreakable")) {
                                foundbreak = true;
                            }
                        }
                        query.cancel();
                        if (!found || !foundSound || !foundbreak) {
                            BukkitUtilities.sendColorMessage("Outdated petblock database! Removing database...'", ChatColor.YELLOW, PetBlocksPlugin.PREFIX_CONSOLE);
                            PetDataFileManager.this.plugin.getDatabase().createSqlUpdate("drop table petblock").execute();
                            BukkitUtilities.sendColorMessage("Finished removing database!", ChatColor.YELLOW, PetBlocksPlugin.PREFIX_CONSOLE);
                            throw new PersistenceException("Install");
                        }
                        BukkitUtilities.sendColorMessage("Successfully connected to your database!", ChatColor.GREEN, PetBlocksPlugin.PREFIX_CONSOLE);
                    } catch (final PersistenceException ex) {
                        BukkitUtilities.sendColorMessage("Cannot find petblocks resources! Installing database...'", ChatColor.GREEN, PetBlocksPlugin.PREFIX_CONSOLE);
                        final PetBlocksPlugin plugin2 = (PetBlocksPlugin) PetDataFileManager.this.plugin;
                        plugin2.setupDatabase();
                        BukkitUtilities.sendColorMessage("Successfully connected to your database!", ChatColor.GREEN, PetBlocksPlugin.PREFIX_CONSOLE);
                    } catch (final Exception ex) {
                       Bukkit.getLogger().log(Level.WARNING,"Cannot connect to your database!" , ex);
                    }
                }
            }
        });
    }

    private synchronized void saveDataBase(Player player, PetData petData) {
        try {
            if (this.plugin.getDatabase().find(PetData.class).where().ieq("uuid", player.getUniqueId().toString()).findUnique() == null) {
                this.plugin.getDatabase().save(petData);
            } else {
                this.plugin.getDatabase().update(petData);
            }
        } catch (final Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Cannot save petData from player.", e);
        }
    }

    private PetData loadDataBase(Player player) {
        try {
            PetData data = this.plugin.getDatabase().find(PetData.class).where().ieq("uuid", player.getUniqueId().toString()).findUnique();
            if (data != null) {
                data = data.copy();
                data.setOwner(player);
                data.setIsBuild(true);
            }
            return data;
        } catch (final Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Cannot load petData from player.", e);
        }
        return null;
    }

    void save(final Player player, final PetData petData, boolean instant) {
        if (!instant) {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {

                @Override
                public void run() {
                    try {
                        if (PetDataFileManager.this.isSqlEnabled())
                            PetDataFileManager.this.saveDataBase(player, petData);
                        else
                            PetDataFileManager.this.saveDefault(player, petData);
                    } catch (final Exception ex) {
                        Bukkit.getLogger().log(Level.WARNING, "Cannot save petData from player.", ex);
                    }
                }
            });
        } else {
            if (this.isSqlEnabled())
                this.saveDataBase(player, petData);
            else
                this.saveDefault(player, petData);
        }
    }

    private void saveDefault(Player player, PetData petData) {
        try {
            final FileConfiguration dataConfig = new YamlConfiguration();
            final File file = BukkitUtilities.createFile(new File(this.getFolder(), player.getUniqueId().toString() + ".yml"));
            dataConfig.load(file);
            dataConfig.set("pet.petname", petData.getDisplayName());
            dataConfig.set("pet.type", petData.getPetType().name().toUpperCase());
            dataConfig.set("pet.id", petData.getSkinMaterial().getId());
            dataConfig.set("pet.durability", petData.getSkinDurability());
            dataConfig.set("pet.skullname", petData.getSkin());
            dataConfig.set("pet.enabled", petData.isEnabled());
            dataConfig.set("pet.age", petData.getAgeInTicks());
            dataConfig.set("pet.unbreakable", petData.isUnbreakable());
            dataConfig.set("pet.sounds", petData.isSoundsEnabled());
            dataConfig.set("pet.moving", petData.getMoveType().name().toUpperCase());
            dataConfig.set("pet.movement", petData.getMovementType().name().toUpperCase());
            if (petData.getParticleEffect() != null) {
                dataConfig.set("pet.particle.name", petData.getParticleEffect().getEffect().getName());
                dataConfig.set("pet.particle.x", petData.getParticleEffect().getX());
                dataConfig.set("pet.particle.y", petData.getParticleEffect().getY());
                dataConfig.set("pet.particle.z", petData.getParticleEffect().getZ());
                dataConfig.set("pet.particle.speed", petData.getParticleEffect().getSpeed());
                dataConfig.set("pet.particle.amount", petData.getParticleEffect().getAmount());
                dataConfig.set("pet.particle.block.id", petData.getParticleEffect().getMaterialId());
                dataConfig.set("pet.particle.block.damage", petData.getParticleEffect().getData());
            }
            dataConfig.save(file);
        } catch (final Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Cannot save petData from player.", e);
        }
    }

    private PetData loadDefault(Player player) {
        try {
            final FileConfiguration dataConfig = new YamlConfiguration();
            final File file = new File(this.getFolder(), player.getUniqueId().toString() + ".yml");
            if (file.exists()) {
                dataConfig.load(file);
                final PetData petData = new PetData(player, PetType.getPetTypeFromName(dataConfig.getString("pet.type")));
                petData.setSkin(Material.getMaterial(dataConfig.getInt("pet.id")), (short) dataConfig.getInt("pet.durability"), dataConfig.getString("pet.skullname"));
                petData.setDisplayName(dataConfig.getString("pet.petname"));
                petData.setEnabled(dataConfig.getBoolean("pet.enabled"));
                petData.setAgeInTicks(dataConfig.getInt("pet.age"));
                if (dataConfig.contains("pet.unbreakable"))
                    petData.setUnbreakable(dataConfig.getBoolean("pet.unbreakable"));
                petData.setSoundsEnabled(dataConfig.getBoolean("pet.sounds"));
                petData.setMovementType(Movement.getMovementFromName(dataConfig.getString("pet.movement")));
                if (MoveType.getMoveTypeFromName(dataConfig.getString("pet.moving")) == null)
                    petData.setMoveType(MoveType.WALKING);
                else
                    petData.setMoveType(MoveType.getMoveTypeFromName(dataConfig.getString("pet.moving")));
                if (ParticleEffect.getParticleEffectFromName(dataConfig.getString("pet.particle.name")) != null) {
                    final Particle particle = new ParticleBuilder()
                            .setEffect(ParticleEffect.getParticleEffectFromName(dataConfig.getString("pet.particle.name")))
                            .setOffset(dataConfig.getDouble("pet.particle.x"), dataConfig.getDouble("pet.particle.y"), dataConfig.getDouble("pet.particle.z"))
                            .setSpeed(dataConfig.getDouble("pet.particle.speed"))
                            .setAmount(dataConfig.getInt("pet.particle.amount"))
                            .setMaterialId(dataConfig.getInt("pet.particle.block.id"))
                            .setData((byte) dataConfig.getInt("pet.particle.block.damage")).build();
                    petData.setParticleEffect(particle);
                }
                return petData;
            }
        } catch (final Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Cannot load petData from player.", e);
        }
        return null;
    }

    public PetData load(Player player) {
        if (this.isSqlEnabled())
            return this.loadDataBase(player);
        else
            return this.loadDefault(player);
    }
}
