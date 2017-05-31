package com.github.shynixn.petblocks.business.logic.persistence.controller;

import com.github.shynixn.petblocks.api.persistence.controller.PlayerMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PlayerData;
import com.github.shynixn.petblocks.lib.DataBaseRepository;
import com.github.shynixn.petblocks.lib.ExtensionHikariConnectionContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Shynixn
 */
public class PlayerDataRepository extends DataBaseRepository<PlayerMeta> implements PlayerMetaController {

    private ExtensionHikariConnectionContext dbContext;

    public PlayerDataRepository(ExtensionHikariConnectionContext connectionContext) {
        super();
        this.dbContext = connectionContext;
    }


    /**
     * Creates a new playerData from the given player
     *
     * @param player player
     * @return playerData
     */
    @Override
    public PlayerMeta create(Player player) {
        return PlayerData.from(player);
    }

    /**
     * Returns the item of the given id
     *
     * @param id id
     * @return item
     */
    @Override
    public PlayerMeta getById(long id) {
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("player/selectbyid", connection,
                    id)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    return this.from(resultSet);
                }
            }
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
        return null;
    }

    /**
     * Checks if the item has got an valid databaseId
     *
     * @param item item
     * @return hasGivenId
     */
    @Override
    public boolean hasId(PlayerMeta item) {
        return item.getId() != 0;
    }

    /**
     * Selects all items from the database into the list
     *
     * @return listOfItems
     */
    @Override
    public List<PlayerMeta> select() {
        final List<PlayerMeta> playerList = new ArrayList<>();
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("player/selectall", connection)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        final PlayerData playerData = this.from(resultSet);
                        playerList.add(playerData);
                    }
                }
            }
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
        return playerList;
    }

    /**
     * Updates the item inside of the database
     *
     * @param item item
     */
    @Override
    public void update(PlayerMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            this.dbContext.executeStoredUpdate("player/update", connection,
                    item.getUUID().toString(),
                    item.getName(),
                    item.getId());
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Deletes the item from the database
     *
     * @param item item
     */
    @Override
    public void delete(PlayerMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            this.dbContext.executeStoredUpdate("player/delete", connection,
                    item.getId());
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Inserts the item into the database and sets the id
     *
     * @param item item
     */
    @Override
    public void insert(PlayerMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            if(item.getUUID() == null)
                throw new IllegalArgumentException("UUId cannot be null!");
            final long id = this.dbContext.executeStoredInsert("player/insert", connection,
                    item.getName(), item.getUUID().toString());
            ((PlayerData)item).setId(id);
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Generates the entity from the given resultSet
     *
     * @param resultSet resultSet
     * @return entity
     */
    @Override
    public PlayerData from(ResultSet resultSet) throws SQLException {
        final PlayerData playerStats = new PlayerData();
        playerStats.setId(resultSet.getLong("id"));
        playerStats.setName(resultSet.getString("name"));
        playerStats.setUuid(UUID.fromString(resultSet.getString("uuid")));
        return playerStats;
    }

    /**
     * Returns the amount of items in the repository
     */
    @Override
    public int size() {
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("player/count", connection)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            }
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
        return 0;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * <p>
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     * <p>
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     * <p>
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     * <p>
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        this.dbContext = null;
    }
}
