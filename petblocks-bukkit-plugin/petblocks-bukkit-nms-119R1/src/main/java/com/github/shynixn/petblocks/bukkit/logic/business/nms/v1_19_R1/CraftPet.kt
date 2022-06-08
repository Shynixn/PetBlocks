@file:Suppress("KDocMissingDocumentation")

package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_19_R1

import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import net.minecraft.world.entity.EntityInsentient
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.loot.LootTable
import java.util.*

/**
 * CraftBukkit Wrapper of the pet.
 */
class CraftPet(server: CraftServer, nmsPet: EntityInsentient) : CraftLivingEntity(server, nmsPet), EntityPetProxy, Pig,
    Rabbit, Bat {
    /**
     * Removes this entity.
     */
    override fun deleteFromWorld() {
        super.remove()
    }

    override fun isAware(): Boolean {
        return false
    }

    override fun isBreedItem(p0: ItemStack): Boolean {
        return false
    }

    override fun isBreedItem(p0: Material): Boolean {
        return false
    }

    override fun setAware(p0: Boolean) {
    }

    /**
     * Hides the true type of the pet from everyone else.
     */
    override fun getType(): EntityType {
        return EntityType.PIG
    }

    override fun getSteerMaterial(): Material {
        return Material.AIR
    }

    /**
     * Ignore all other plugins trying to remove this entity. This is the entity of PetBlocks,
     * no one else is allowed to modify this!
     */
    override fun remove() {
    }

    /**
     * Pet should never be persistent.
     */
    override fun isPersistent(): Boolean {
        return false
    }

    /**
     * Pet should never be persistent.
     */
    override fun setPersistent(b: Boolean) {}

    /**
     * Custom type.
     */
    override fun toString(): String {
        return "PetBlocks{Entity}"
    }

    override fun getLootTable(): LootTable? {
        return null
    }

    override fun setTarget(p0: LivingEntity?) {
    }

    override fun getTarget(): LivingEntity? {
        return null
    }

    override fun setLootTable(p0: LootTable?) {
    }

    override fun setSeed(p0: Long) {
    }

    override fun getSeed(): Long {
        return 0L
    }

    override fun setAdult() {
    }

    override fun setLoveModeTicks(p0: Int) {
    }

    override fun setBaby() {
    }

    override fun setBoostTicks(p0: Int) {
    }

    override fun setAge(p0: Int) {
    }

    override fun getLoveModeTicks(): Int {
        return 0
    }

    override fun getAge(): Int {
        return 2
    }

    override fun getCurrentBoostTicks(): Int {
        return 0
    }

    override fun canBreed(): Boolean {
        return false
    }

    override fun setSaddle(p0: Boolean) {
    }

    override fun getBoostTicks(): Int {
        return 0
    }

    override fun isAdult(): Boolean {
        return false
    }

    override fun hasSaddle(): Boolean {
        return false
    }

    override fun setCurrentBoostTicks(p0: Int) {
    }

    override fun getAgeLock(): Boolean {
        return false
    }

    override fun getBreedCause(): UUID? {
        return UUID.randomUUID()
    }

    override fun setBreedCause(p0: UUID?) {
    }

    override fun setAgeLock(p0: Boolean) {
    }

    override fun isLoveMode(): Boolean {
        return false
    }

    override fun setBreed(p0: Boolean) {
    }

    override fun setRabbitType(p0: Rabbit.Type) {
    }

    override fun isAwake(): Boolean {
        return true
    }

    override fun setAwake(p0: Boolean) {
    }

    override fun getRabbitType(): Rabbit.Type {
        return Rabbit.Type.BLACK
    }
}
