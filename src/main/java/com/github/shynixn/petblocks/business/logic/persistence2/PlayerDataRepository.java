package com.github.shynixn.petblocks.business.logic.persistence2;

import com.github.shynixn.petblocks.lib.util.DataBaseRepository;
import com.github.shynixn.petblocks.lib.util.DbConnectionContext;
import com.github.shynixn.petblocks.lib.util.SQLProvider;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shynixn
 */
public class PlayerDataRepository extends DataBaseRepository<PlayerData>{

    private DbConnectionContext dbContext;
    private SQLProvider sqlProvider;

    PlayerDataRepository(DbConnectionContext dbContext, SQLProvider sqlProvider) {
        super();
        this.dbContext = dbContext;
        this.sqlProvider = sqlProvider;
    }

    /**
     * Returns the item of the given id
     *
     * @param id id
     * @return item
     */
    @Override
    public PlayerData getById(long id) {
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeQuery(this.sqlProvider.getString("selectbyid"), connection,
                    id)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        return PlayerData.from(resultSet);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
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
    public boolean hasId(PlayerData item) {
        return item.getId() != 0;
    }

    /**
     * Selects all items from the database into the list
     *
     * @return listOfItems
     */
    @Override
    public List<PlayerData> select() {
        final List<PlayerData> playerList = new ArrayList<>();
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeQuery(this.sqlProvider.getString("player/selectall"), connection)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        playerList.add(PlayerData.from(resultSet));
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return playerList;
    }

    /**
     * Updates the item inside of the database
     *
     * @param item item
     */
    @Override
    public void update(PlayerData item) {
        try (Connection connection = this.dbContext.getConnection()) {
            this.dbContext.executeUpdate(this.sqlProvider.getString("player/update"), connection,
                    item.getName(),
                    item.getId());
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the item from the database
     *
     * @param item item
     */
    @Override
    public void delete(PlayerData item) {
        try (Connection connection = this.dbContext.getConnection()) {
            this.dbContext.executeUpdate(this.sqlProvider.getString("player/delete"), connection,
                    item.getId());
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts the item into the database and sets the id
     *
     * @param item item
     */
    @Override
    public void insert(PlayerData item) {
        try (Connection connection = this.dbContext.getConnection()) {
            final long id = this.dbContext.executeInsert(this.sqlProvider.getString("player/insert"), connection,
                    item.getName(), item.getUUID().toString());
            item.setId(id);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the amount of items in the repository
     */
    @Override
    public int size() {
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeQuery(this.sqlProvider.getString("player/count"), connection)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
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
        this.sqlProvider = null;
    }
}
