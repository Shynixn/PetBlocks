package unittest

import com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_9_R2.CraftPetArmorstand
import com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_9_R2.NMSPetArmorstand
import org.bukkit.craftbukkit.v1_9_R2.CraftServer
import org.bukkit.entity.EntityType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class CraftPetArmorstandTest {
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
        val entityInsentient = Mockito.mock(NMSPetArmorstand::class.java)
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
        val entityInsentient = Mockito.mock(NMSPetArmorstand::class.java)
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
        Assertions.assertEquals("PetBlocks{ArmorstandEntity}", identifier)
    }

    companion object {
        fun createWithDependencies(entityInsentient: NMSPetArmorstand = Mockito.mock(NMSPetArmorstand::class.java)): CraftPetArmorstand {

            val server = Mockito.mock(CraftServer::class.java)

            return CraftPetArmorstand(server, entityInsentient)
        }
    }
}