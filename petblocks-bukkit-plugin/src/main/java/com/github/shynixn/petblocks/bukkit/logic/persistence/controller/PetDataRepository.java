package com.github.shynixn.petblocks.bukkit.logic.persistence.controller;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PlayerMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.Factory;
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.PetData;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.Config;
import com.github.shynixn.petblocks.bukkit.lib.ExtensionHikariConnectionContext;
import com.github.shynixn.petblocks.bukkit.nms.v1_12_R1.MaterialCompatibility12;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * DatabaseRepository for petData.
 * <p>
 * Version 1.1
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class PetDataRepository extends DataBaseRepository<PetMeta> implements PetMetaController {

    private final PlayerMetaController playerMetaController;
    private final ParticleEffectMetaController particleEffectMetaController;

    /**
     * Initializes a new petData repository.
     *
     * @param connectionContext connectionContext
     */
    public PetDataRepository(ExtensionHikariConnectionContext connectionContext) {
        super(connectionContext);
        this.playerMetaController = Factory.createPlayerDataController();
        this.particleEffectMetaController = Factory.createParticleEffectController();
    }

    /**
     * Stores a new a item in the repository.
     *
     * @param item item
     */
    @Override
    public void store(PetMeta item) {
        if (item == null)
            throw new IllegalArgumentException("PetMeta cannot be null!");
        if (item.getPlayerMeta() == null)
            throw new IllegalArgumentException("PlayerMeta cannot be null!");
        if (item.getParticleEffectMeta() == null)
            throw new IllegalArgumentException("ParticleMeta cannot be null!");
        if (item.getPlayerMeta().getPlayer() != null) {
            item.getPlayerMeta().setName(((Player) item.getPlayerMeta().getPlayer()).getName());
            if (item.getPlayerMeta().getId() == 0) {
                final PlayerMeta playerMeta;
                if ((playerMeta = this.playerMetaController.getByUUID(((Player) item.getPlayerMeta().getPlayer()).getUniqueId())) != null) {
                    ((PetData) item).setPlayerMeta(playerMeta);
                }
            }
        }
        this.playerMetaController.store(item.getPlayerMeta());
        this.particleEffectMetaController.store(item.getParticleEffectMeta());
        ((PetData) item).setParticleId(item.getParticleEffectMeta().getId());
        ((PetData) item).setPlayerId(item.getPlayerMeta().getId());
        super.store(item);
    }

    /**
     * Removes an item from the repository.
     *
     * @param item item
     */
    @Override
    public void remove(PetMeta item) {
        if (item == null)
            throw new IllegalArgumentException("PetMeta cannot be null!");
        super.remove(item);
        this.particleEffectMetaController.remove(item.getParticleEffectMeta());
    }

    /**
     * Creates a petMeta for the given player.
     *
     * @param player player
     * @return petMeta
     */
    @Override
    public PetMeta create(Object player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        final Optional<GUIItemContainer> containerOpt = Config.getInstance().getGuiItemsController().getGUIItemFromName("default-appearance");
        if (!containerOpt.isPresent())
            throw new IllegalArgumentException("Default appearance could not be loaded from the config.yml!");
        final PetData petData = new PetData((Player) player, Config.getInstance().getDefaultPetName());
        petData.setSkin(containerOpt.get().getItemId(), containerOpt.get().getItemDamage(), containerOpt.get().getSkin(), containerOpt.get().isItemUnbreakable());
        return petData;
    }

    /**
     * Returns the petData from the given player.
     *
     * @param player player
     * @return petData
     */
    @Override
    @Deprecated
    public <T> PetMeta getByPlayer(T player) {
        final Optional<PetMeta> tmp = this.getFromPlayer(player);
        return tmp.orElse(null);
    }

    /**
     * Returns the petData from the given player.
     *
     * @param player player
     * @return petData
     */
    @Override
    public <T> Optional<PetMeta> getFromPlayer(T player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        PetMeta petMeta = null;
        if (PetBlocksApi.getDefaultPetBlockController() != null && PetBlocksApi.getDefaultPetBlockController().getByPlayer(player) != null) {
            return Optional.ofNullable(PetBlocksApi.getDefaultPetBlockController().getByPlayer(player).getMeta());
        }
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("petblock/selectbyplayer", connection, ((Player) player).getUniqueId().toString());
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                petMeta = this.from(resultSet);
                if (petMeta == null)
                    return Optional.empty();
            } else {
                return Optional.empty();
            }
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
        petMeta.setEngine(Config.getInstance().getEngineController().getById(((PetData) petMeta).getEngineId()));
        ((PetData) petMeta).setParticleEffectMeta(this.particleEffectMetaController.getById(((PetData) petMeta).getParticleId()));
        ((PetData) petMeta).setPlayerMeta(this.playerMetaController.getById(((PetData) petMeta).getPlayerId()));
        return Optional.of(petMeta);
    }

    /**
     * Checks if the player has got an entry in the database.
     *
     * @param player player
     * @return hasEntry
     */
    @Override
    public <T> boolean hasEntry(T player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("petblock/selectentrybyplayer", connection,
                     ((Player) player).getUniqueId().toString())) {
            final ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
        return false;
    }

    /**
     * Removes the petMeta of the given player.
     *
     * @param player player
     */
    @Override
    public void removeByPlayer(Object player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        this.remove(this.getByPlayer(player));
    }

    /**
     * Returns the item of the given id.
     *
     * @param id id
     * @return item
     */
    @Override
    public Optional<PetMeta> getFromId(long id) {
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("petblock/selectbyid", connection,
                     id); ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return Optional.of(this.from(resultSet));
            }
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
        return Optional.empty();
    }

    /**
     * Checks if the item has got an valid databaseId.
     *
     * @param item item
     * @return hasGivenId
     */
    @Override
    protected boolean hasId(PetMeta item) {
        return item.getId() != 0;
    }

    /**
     * Selects all items from the database into the list.
     *
     * @return listOfItems
     */
    @Override
    protected List<PetMeta> select() {
        final List<PetMeta> petList = new ArrayList<>();
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("petblock/selectall", connection);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                final PetMeta petData = this.from(resultSet);
                petList.add(petData);
            }
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
        return petList;
    }

    /**
     * Updates the item inside of the database.
     *
     * @param itemMeta item
     */
    @Override
    protected void update(PetMeta itemMeta) {
        final PetData item = (PetData) itemMeta;
        if (item == null)
            throw new IllegalArgumentException("Meta has to be an instance of PetData");
        try (Connection connection = this.dbContext.getConnection()) {
            this.dbContext.executeStoredUpdate("petblock/update", connection,
                    item.getPetDisplayName(),
                    item.getEngine().getId(),
                    MaterialCompatibility12.getMaterialFromId(itemMeta.getItemId()).name(),
                    item.getItemDamage(),
                    item.getSkin(),
                    item.isEnabled(),
                    item.getAge(),
                    item.isItemStackUnbreakable(),
                    item.isSoundEnabled(),
                    item.getId()
            );
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Deletes the item from the database.
     *
     * @param item item
     */
    @Override
    protected void delete(PetMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            this.dbContext.executeStoredUpdate("petblock/delete", connection,
                    item.getId());
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Inserts the item into the database and sets the id.
     *
     * @param itemMeta item
     */
    @Override
    protected void insert(PetMeta itemMeta) {
        final PetData item = (PetData) itemMeta;
        if (item == null)
            throw new IllegalArgumentException("Meta has to be an instance of PetData");
        if (item.getEngine() == null)
            throw new IllegalArgumentException("Engine cannot be null!");
        try (Connection connection = this.dbContext.getConnection()) {
            final long id = this.dbContext.executeStoredInsert("petblock/insert", connection,
                    item.getPlayerId(),
                    item.getParticleId(),
                    item.getPetDisplayName(),
                    item.getEngine().getId(),
                    MaterialCompatibility12.getMaterialFromId(itemMeta.getItemId()).name(),
                    item.getItemDamage(),
                    item.getSkin(),
                    item.isEnabled(),
                    item.getAge(),
                    item.isItemStackUnbreakable(),
                    item.isSoundEnabled()
            );
            item.setId(id);
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Generates the entity from the given resultSet.
     *
     * @param resultSet resultSet
     * @return entity
     */
    @Override
    protected PetMeta from(ResultSet resultSet) throws SQLException {
        final PetData petMeta = new PetData();
        petMeta.setId(resultSet.getLong("id"));
        petMeta.setPlayerId(resultSet.getLong("shy_player_id"));
        petMeta.setParticleId(resultSet.getLong("shy_particle_effect_id"));
        try {
            petMeta.setPetDisplayName(resultSet.getString("name"));
        } catch (final Exception ex) {
            petMeta.setPetDisplayName(Config.getInstance().getDefaultPetName().replace(":player", "Player"));
        }
        petMeta.setEngineId(resultSet.getInt("engine"));
        petMeta.setSkin(Material.getMaterial(resultSet.getString("material")).getId(), resultSet.getInt("data"), resultSet.getString("skull"), resultSet.getBoolean("unbreakable"));
        petMeta.setEnabled(resultSet.getBoolean("enabled"));
        petMeta.setAge(resultSet.getInt("age"));
        petMeta.setSoundEnabled(resultSet.getBoolean("play_sounds"));
        return petMeta;
    }

    /**
     * Returns the amount of items in the repository.
     */
    @Override
    public int size() {
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("petblock/count", connection);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
        return 0;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        this.dbContext = null;
    }
}
