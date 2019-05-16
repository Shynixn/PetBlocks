package unittest

import com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_11_R1.CraftPet
import net.minecraft.server.v1_11_R1.EntityInsentient
import org.bukkit.craftbukkit.v1_11_R1.CraftServer
import org.bukkit.entity.EntityType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class CraftPetTest {
    /**
     * Given
     *    a pet
     * When
     *    deleteFromWorld is called.
     * Then
     *    pet should die.
     */
    @Test
    fun deleteFromWorld_CraftPet_ShouldExecuteDie() {
        // Arrange
        val entityInsentient = Mockito.mock(EntityInsentient::class.java)
        val classUnderTest = createWithDependencies(entityInsentient)

        var called = false

        Mockito.`when`(entityInsentient.die()).then {
            called = true
            Unit
        }

        // Act
        classUnderTest.deleteFromWorld()

        // Assert
        Assertions.assertTrue(called)
    }

    /**
     * Given
     *    a pet
     * When
     *    remove is called.
     * Then
     *    pet should not die.
     */
    @Test
    fun remove_CraftPet_ShouldNotExecuteDie() {
        // Arrange
        val entityInsentient = Mockito.mock(EntityInsentient::class.java)
        val classUnderTest = createWithDependencies(entityInsentient)

        var called = false

        Mockito.`when`(entityInsentient.die()).then {
            called = true
            Unit
        }

        // Act
        classUnderTest.remove()

        // Assert
        Assertions.assertFalse(called)
    }

    /**
     * Given
     *    a pet
     * When
     *    getType is called.
     * Then
     *    rabbit type should always be returned.
     */
    @Test
    fun getType_CraftPet_ShouldReturnRabbitType() {
        // Arrange
        val classUnderTest = createWithDependencies()

        // Act
        val entityType = classUnderTest.type

        // Assert
        Assertions.assertEquals(EntityType.RABBIT, entityType)
    }

    /**
     * Given
     *    a pet
     * When
     *    toString is called.
     * Then
     *    Pet identifier should be returned.
     */
    @Test
    fun toString_CraftPet_ShouldReturnIdentifier() {
        // Arrange
        val classUnderTest = createWithDependencies()

        // Act
        val identifier = classUnderTest.toString()

        // Assert
        Assertions.assertEquals("PetBlocks{Entity}", identifier)
    }

    companion object {
        fun createWithDependencies(entityInsentient: EntityInsentient = Mockito.mock(EntityInsentient::class.java)): CraftPet {

            val server = Mockito.mock(CraftServer::class.java)

            return CraftPet(server, entityInsentient)
        }
    }
}