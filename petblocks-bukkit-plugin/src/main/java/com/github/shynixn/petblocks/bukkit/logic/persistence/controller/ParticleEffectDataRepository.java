package com.github.shynixn.petblocks.bukkit.logic.persistence.controller;

import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.ParticleEffectData;
import com.github.shynixn.petblocks.bukkit.lib.ExtensionHikariConnectionContext;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * DatabaseRepository for particleEffects.
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
public class ParticleEffectDataRepository extends DataBaseRepository<ParticleEffectMeta> implements ParticleEffectMetaController {

    /**
     * Initializes a new particleEffect repository.
     *
     * @param connectionContext connectionContext
     */
    public ParticleEffectDataRepository(ExtensionHikariConnectionContext connectionContext) {
        super(connectionContext);
    }

    /**
     * Checks if the item has got an valid databaseId.
     *
     * @param item item
     * @return hasGivenId
     */
    @Override
    protected boolean hasId(ParticleEffectMeta item) {
        return item.getId() != 0;
    }

    /**
     * Selects all items from the database into the list.
     *
     * @return listOfItems
     */
    @Override
    protected List<ParticleEffectMeta> select() {
        final List<ParticleEffectMeta> items = new ArrayList<>();
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("particle/selectall", connection);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                final ParticleEffectMeta data = this.from(resultSet);
                items.add(data);
            }
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
        return items;
    }

    /**
     * Updates the item inside of the database.
     *
     * @param item item
     */
    @Override
    protected void update(ParticleEffectMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            String materialName = null;
            if (item.getMaterial() != null)
                materialName = ((Material) item.getMaterial()).name();
            int data = -1;
            if (item.getData() != null)
                data = item.getData();
            this.dbContext.executeStoredUpdate("particle/update", connection,
                    item.getEffectName(),
                    item.getAmount(),
                    item.getSpeed(),
                    item.getOffsetX(),
                    item.getOffsetY(),
                    item.getOffsetZ(),
                    materialName,
                    data,
                    item.getId());
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Creates a new particleEffectMeta.
     *
     * @return meta
     */
    @Override
    public ParticleEffectMeta create() {
        return new ParticleEffectData();
    }

    /**
     * Returns the item of the given id.
     *
     * @param id id
     * @return item
     */
    @Override
    public Optional<ParticleEffectMeta> getFromId(long id) {
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("particle/selectbyid", connection, id);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return Optional.of(this.from(resultSet));
            }
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
        return Optional.empty();
    }

    /**
     * Deletes the item from the database.
     *
     * @param item item
     */
    @Override
    protected void delete(ParticleEffectMeta item) {
        if (item != null) {
            try (Connection connection = this.dbContext.getConnection()) {
                this.dbContext.executeStoredUpdate("particle/delete", connection,
                        item.getId());
            } catch (final SQLException e) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
            }
        }
    }

    /**
     * Inserts the item into the database and sets the id.
     *
     * @param item item
     */
    @Override
    protected void insert(ParticleEffectMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            String materialName = null;
            if (item.getMaterial() != null)
                materialName = ((Material) item.getMaterial()).name();
            final long id = this.dbContext.executeStoredInsert("particle/insert", connection,
                    item.getEffectName(),
                    item.getAmount(),
                    item.getSpeed(),
                    item.getOffsetX(),
                    item.getOffsetY(),
                    item.getOffsetZ(),
                    materialName,
                    item.getData());
            ((ParticleEffectData) item).setId(id);
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Returns the amount of items in the repository.
     */
    @Override
    public int size() {
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("particle/count", connection);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (final SQLException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Database error occurred.", e);
        }
        return 0;
    }

    /**
     * Generates the entity from the given resultSet.
     *
     * @param resultSet resultSet
     * @return entity
     */
    @Override
    protected ParticleEffectMeta from(ResultSet resultSet) throws SQLException {
        final ParticleEffectData particleEffectData = new ParticleEffectData();
        particleEffectData.setId(resultSet.getLong("id"));
        particleEffectData.setEffectName(resultSet.getString("name"));
        particleEffectData.setAmount(resultSet.getInt("amount"));
        particleEffectData.setSpeed(resultSet.getDouble("speed"));
        particleEffectData.setOffsetX((resultSet.getDouble("x")));
        particleEffectData.setOffsetY(resultSet.getDouble("y"));
        particleEffectData.setOffsetZ(resultSet.getDouble("z"));
        if (resultSet.getString("material") != null) {
            particleEffectData.setMaterial(Material.getMaterial(resultSet.getString("material")).getId());
        }
        if (resultSet.getInt("data") == -1) {
            particleEffectData.setData(null);
        } else {
            particleEffectData.setData((byte) resultSet.getInt("data"));
        }
        return particleEffectData;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     */
    @Override
    public void close() throws Exception {
        this.dbContext = null;
    }
}
