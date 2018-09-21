package com.github.shynixn.petblocks.core.logic.persistence.controller;

import com.github.shynixn.petblocks.api.business.service.LoggingService;
import com.github.shynixn.petblocks.api.persistence.controller.PlayerMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import com.github.shynixn.petblocks.core.logic.business.entity.DbContext;
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerData;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DatabaseRepository for playerData.
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
public abstract class PlayerDataRepository<Player> extends DataBaseRepository<PlayerMeta> implements PlayerMetaController<Player> {

    /**
     * Initializes a new databaseRepository with the given connectionContext.
     *
     * @param connectionContext connectionContext
     * @param logger            logger
     */
    public PlayerDataRepository(DbContext connectionContext, LoggingService logger) {
        super(connectionContext, logger);
    }

    /**
     * Creates a new Player Data object.
     * @return playerData.
     */
    public abstract PlayerData create();

    /**
     * Returns the playerMeta of the given uuid.
     *
     * @param uuid uuid
     * @return playerMeta
     */
    @Override
    public Optional<PlayerMeta> getFromUUID(UUID uuid) {
        if (uuid == null)
            throw new IllegalArgumentException("UUID cannot be null!");
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("player/selectbyuuid", connection,
                     uuid.toString()); ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return Optional.of(this.from(resultSet));
            }
        } catch (final SQLException e) {
            this.logger.error("Database error occurred.", e);
        }
        return Optional.empty();
    }

    /**
     * Returns the item of the given id.
     *
     * @param id id
     * @return item
     */
    @Override
    public Optional<PlayerMeta> getFromId(long id) {
        try (Connection connection = this.dbContext.getConnection(); PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("player/selectbyid", connection,
                id); ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return Optional.of(this.from(resultSet));
            }
        } catch (final SQLException e) {
            this.logger.error("Database error occurred.", e);
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
    protected boolean hasId(PlayerMeta item) {
        return item.getId() != 0;
    }

    /**
     * Selects all items from the database into the list.
     *
     * @return listOfItems
     */
    @Override
    protected List<PlayerMeta> select() {
        final List<PlayerMeta> playerList = new ArrayList<>();
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("player/selectall", connection);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                final PlayerData playerData = this.from(resultSet);
                playerList.add(playerData);
            }
        } catch (final SQLException e) {
            this.logger.error("Database error occurred.", e);
        }
        return playerList;
    }

    /**
     * Updates the item inside of the database.
     *
     * @param item item
     */
    @Override
    protected void update(PlayerMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            this.dbContext.executeStoredUpdate("player/update", connection,
                    item.getUUID().toString(),
                    item.getName(),
                    item.getId());
        } catch (final SQLException e) {
            this.logger.error("Database error occurred.", e);
        }
    }

    /**
     * Deletes the item from the database.
     *
     * @param item item
     */
    @Override
    protected void delete(PlayerMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            this.dbContext.executeStoredUpdate("player/delete", connection,
                    item.getId());
        } catch (final SQLException e) {
            this.logger.error("Database error occurred.", e);
        }
    }

    /**
     * Inserts the item into the database and sets the id.
     *
     * @param item item
     */
    @Override
    protected void insert(PlayerMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            if (item.getUUID() == null)
                throw new IllegalArgumentException("UUId cannot be null!");
            final long id = this.dbContext.executeStoredInsert("player/insert", connection,
                    item.getName(), item.getUUID().toString());
            ((PlayerData) item).setId(id);
        } catch (final SQLException e) {
            this.logger.error("Database error occurred.", e);
        }
    }

    /**
     * Generates the entity from the given resultSet.
     *
     * @param resultSet resultSet
     * @return entity
     */
    @Override
    protected PlayerData from(ResultSet resultSet) throws SQLException {
        final PlayerData playerStats = this.create();
        playerStats.setId(resultSet.getLong("id"));
        playerStats.setName(resultSet.getString("name"));
        playerStats.setUuid(UUID.fromString(resultSet.getString("uuid")));
        return playerStats;
    }

    /**
     * Returns the amount of items in the repository.
     */
    @Override
    public int size() {
        try (Connection connection = this.dbContext.getConnection();
             PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("player/count", connection);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (final SQLException e) {
            this.logger.error("Database error occurred.", e);
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
