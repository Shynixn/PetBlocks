package com.github.shynixn.petblocks.core.logic.persistence.controller;

import com.github.shynixn.petblocks.api.business.enumeration.ParticleType;
import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.Particle;
import com.github.shynixn.petblocks.core.logic.business.entity.DbContext;
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity;
import com.google.inject.Inject;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
public class ParticleEffectDataRepository extends DataBaseRepository<Particle> implements ParticleEffectMetaController {
    /**
     * Initializes a new databaseRepository with the given connectionContext.
     *
     * @param connectionContext connectionContext
     * @param logger            logger
     */
    @Inject
    public ParticleEffectDataRepository(DbContext connectionContext, Logger logger) {
        super(connectionContext, logger);
    }

    /**
     * Checks if the item has got an valid databaseId.
     *
     * @param item item
     * @return hasGivenId
     */
    @Override
    protected boolean hasId(Particle item) {
        return ((ParticleEntity) item).getId() != 0;
    }

    /**
     * Selects all items from the database into the list.
     *
     * @return listOfItems
     */
    @Override
    protected List<Particle> select() {
        final List<Particle> items = new ArrayList<>();
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("particle/selectall", connection);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                final ParticleEntity data = this.from(resultSet);
                items.add(data);
            }
        } catch (final SQLException e) {
            this.logger.error("Database error occurred.", e);
        }
        return items;
    }

    /**
     * Updates the item inside of the database.
     *
     * @param item item
     */
    @Override
    protected void update(Particle item) {
        try (final Connection connection = this.dbContext.getConnection()) {
            String materialName = null;
            if (item.getMaterialName() != null)
                materialName = item.getMaterialName();
            int data = -1;
            if (item.getData() != 0)
                data = item.getData();
            this.dbContext.executeStoredUpdate("particle/update", connection,
                    item.getType().getGameId_113(),
                    item.getAmount(),
                    item.getSpeed(),
                    item.getOffSetX(),
                    item.getOffSetY(),
                    item.getOffSetZ(),
                    materialName,
                    data,
                    ((ParticleEntity) item).getId());
        } catch (final SQLException e) {
            this.logger.error("Database error occurred.", e);
        }
    }

    /**
     * Returns the item of the given id.
     *
     * @param id id
     * @return item
     */
    @Override
    public Optional<Particle> getFromId(long id) {
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("particle/selectbyid", connection, id);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return Optional.of(this.from(resultSet));
            }
        } catch (final SQLException e) {
            this.logger.error("Database error occurred.", e);
        }
        return Optional.empty();
    }

    /**
     * Deletes the item from the database.
     *
     * @param item item
     */
    @Override
    protected void delete(Particle item) {
        if (item != null) {
            try (Connection connection = this.dbContext.getConnection()) {
                this.dbContext.executeStoredUpdate("particle/delete", connection,
                        ((ParticleEntity) item).getId());
            } catch (final SQLException e) {
                this.logger.error("Database error occurred.", e);
            }
        }
    }

    /**
     * Inserts the item into the database and sets the id.
     *
     * @param item item
     */
    @Override
    protected void insert(Particle item) {
        try (Connection connection = this.dbContext.getConnection()) {
            String materialName = null;
            if (item.getMaterialName() != null)
                materialName = item.getMaterialName();
            final long id = this.dbContext.executeStoredInsert("particle/insert", connection,
                    item.getType().getGameId_113(),
                    item.getAmount(),
                    item.getSpeed(),
                    item.getOffSetX(),
                    item.getOffSetY(),
                    item.getOffSetZ(),
                    materialName,
                    item.getData());
            ((ParticleEntity) item).setId(id);
        } catch (final SQLException e) {
            this.logger.error("Database error occurred.", e);
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
            this.logger.error("Database error occurred.", e);
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
    protected ParticleEntity from(ResultSet resultSet) throws SQLException {
        final ParticleEntity particleEffectData = (ParticleEntity) this.create();
        particleEffectData.setId(resultSet.getLong("id"));
        particleEffectData.setType(this.findParticleTypeFromName(resultSet.getString("name")));
        particleEffectData.setAmount(resultSet.getInt("amount"));
        particleEffectData.setSpeed(resultSet.getDouble("speed"));
        particleEffectData.setOffSetX((resultSet.getDouble("x")));
        particleEffectData.setOffSetY(resultSet.getDouble("y"));
        particleEffectData.setOffSetZ(resultSet.getDouble("z"));
        if (resultSet.getString("material") != null) {
            particleEffectData.setMaterialName(resultSet.getString("material"));
        }
        if (resultSet.getInt("data") == -1) {
            particleEffectData.setData(0);
        } else {
            particleEffectData.setData((byte) resultSet.getInt("data"));
        }
        return particleEffectData;
    }

    private ParticleType findParticleTypeFromName(String name) {
        for (final ParticleType p : ParticleType.values()) {
            if (p.getGameId_18().equalsIgnoreCase(name) || p.getGameId_113().equalsIgnoreCase(name) || p.name().equalsIgnoreCase(name)) {
                return p;
            }
        }

        throw new RuntimeException("Failed to find Particle.");
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     */
    @Override
    public void close() {
        this.dbContext = null;
    }

    /**
     * Creates a new particleEffectMeta.
     *
     * @return meta
     */
    @Override
    public Particle create() {
        return new ParticleEntity(ParticleType.NONE);
    }
}
