package com.github.shynixn.petblocks.business.logic.persistence.controller;

import com.github.shynixn.petblocks.api.entities.MoveType;
import com.github.shynixn.petblocks.api.entities.Movement;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PetData;
import com.github.shynixn.petblocks.lib.ExtensionHikariConnectionContext;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Shynixn
 */
public class PetDataRepository extends DataBaseRepository<PetMeta> implements PetMetaController {

    private ExtensionHikariConnectionContext dbContext;

    public PetDataRepository(ExtensionHikariConnectionContext connectionContext) {
        super();
        this.dbContext = connectionContext;
    }

    /**
     * Returns the petdata from the given player
     *
     * @param player player
     * @return petData
     */
    @Override
    public <T> PetMeta getByPlayer(T player) {
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("petblock/selectbyplayer", connection,
                    ((Player)player).getUniqueId().toString())) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return this.from(resultSet);
                    }
                }
            }
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
        return null;
    }

    /**
     * Checks if the player has got an entry in the database
     *
     * @param player player
     * @return hasEntry
     */
    @Override
    public <T> boolean hasEntry(T player) {
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("petblock/selectentrybyplayer", connection,
                    ((Player)player).getUniqueId().toString())) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
        return false;
    }

    /**
     * Returns the item of the given id
     *
     * @param id id
     * @return item
     */
    @Override
    public PetMeta getById(long id) {
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("petblock/selectbyid", connection,
                    id)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return this.from(resultSet);
                    }
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
    public boolean hasId(PetMeta item) {
        return item.getId() != 0;
    }

    /**
     * Selects all items from the database into the list
     *
     * @return listOfItems
     */
    @Override
    public List<PetMeta> select() {
        final List<PetMeta> petList = new ArrayList<>();
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("petblock/selectall", connection)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        final PetMeta petData = this.from(resultSet);
                        petList.add(petData);
                    }
                }
            }
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
        return petList;
    }

    /**
     * Updates the item inside of the database
     *
     * @param itemMeta item
     */
    @Override
    public void update(PetMeta itemMeta) {
        final PetData item = (PetData) itemMeta;
        if (item == null)
            throw new IllegalArgumentException("Meta has to be an instance of PetData");
        if (item.getType() == null)
            throw new IllegalArgumentException("PetType cannot be null!");
        if (item.getSkinMaterial() == null)
            throw new IllegalArgumentException("SkinMaterial cannot be null!");
        try (Connection connection = this.dbContext.getConnection()) {
            this.dbContext.executeStoredUpdate("petblock/update", connection,
                    item.getDisplayName(),
                    item.getType().name(),
                    item.getSkinMaterial().name(),
                    item.getSkinDurability(),
                    item.getSkin(),
                    item.isEnabled(),
                    item.getAgeInTicks(),
                    item.isUnbreakable(),
                    item.isSoundEnabled(),
                    item.getMoveType().name(),
                    item.getMovementType().name(),
                    item.getId()
            );
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
    public void delete(PetMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            this.dbContext.executeStoredUpdate("petblock/delete", connection,
                    item.getId());
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Inserts the item into the database and sets the id
     *
     * @param itemMeta item
     */
    @Override
    public void insert(PetMeta itemMeta) {
        final PetData item = (PetData) itemMeta;
        if (item == null)
            throw new IllegalArgumentException("Meta has to be an instance of PetData");
        if (item.getType() == null)
            throw new IllegalArgumentException("PetType cannot be null!");
        if (item.getSkinMaterial() == null)
            throw new IllegalArgumentException("SkinMaterial cannot be null!");
        try (Connection connection = this.dbContext.getConnection()) {
            final long id = this.dbContext.executeStoredInsert("petblock/insert", connection,
                    item.getPlayerId(),
                    item.getParticleId(),
                    item.getDisplayName(),
                    item.getType().name(),
                    item.getSkinMaterial().name(),
                    item.getSkinDurability(),
                    item.getSkin(),
                    item.isEnabled(),
                    item.getAgeInTicks(),
                    item.isUnbreakable(),
                    item.isSoundEnabled(),
                    item.getMoveType().name(),
                    item.getMovementType().name()
            );
            item.setId(id);
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
    public PetMeta from(ResultSet resultSet) throws SQLException {
        final PetData petMeta = new PetData();
        petMeta.setId(resultSet.getLong("id"));
        petMeta.setPlayerId(resultSet.getLong("shy_player_id"));
        petMeta.setParticleId(resultSet.getLong("shy_particle_effect_id"));
        petMeta.setDisplayName(resultSet.getString("name"));
        petMeta.setPetType(PetType.getPetTypeFromName(resultSet.getString("type")));
        petMeta.setSkin(Material.getMaterial(resultSet.getString("material")), (short) resultSet.getInt("data"), resultSet.getString("skull"));
        petMeta.setEnabled(resultSet.getBoolean("enabled"));
        petMeta.setAgeInTicks(resultSet.getInt("age"));
        petMeta.setUnbreakable(resultSet.getBoolean("unbreakable"));
        petMeta.setSoundEnabled(resultSet.getBoolean("play_sounds"));
        petMeta.setMoveType(MoveType.getMoveTypeFromName(resultSet.getString("moving_type")));
        petMeta.setMovementType(Movement.getMovementFromName(resultSet.getString("movement_type")));
        return petMeta;
    }

    /**
     * Returns the amount of items in the repository
     */
    @Override
    public int size() {
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("petblock/count", connection)) {
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
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        this.dbContext = null;
    }
}
