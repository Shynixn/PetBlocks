package unittest

import com.github.shynixn.petblocks.api.business.service.EntityService
import com.github.shynixn.petblocks.sponge.logic.business.service.EntityServiceImpl
import helper.LoggingHelper
import ninja.leaping.configurate.objectmapping.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.spongepowered.api.CatalogType
import org.spongepowered.api.GameRegistry
import org.spongepowered.api.Sponge
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.data.value.ValueFactory
import org.spongepowered.api.entity.EntityType
import org.spongepowered.api.entity.ai.task.AITaskType
import org.spongepowered.api.entity.ai.task.AbstractAITask
import org.spongepowered.api.entity.living.Agent
import org.spongepowered.api.item.ItemType
import org.spongepowered.api.item.merchant.VillagerRegistry
import org.spongepowered.api.item.recipe.crafting.CraftingRecipeRegistry
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipeRegistry
import org.spongepowered.api.network.status.Favicon
import org.spongepowered.api.registry.CatalogRegistryModule
import org.spongepowered.api.registry.RegistryModule
import org.spongepowered.api.resourcepack.ResourcePack
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot
import org.spongepowered.api.statistic.BlockStatistic
import org.spongepowered.api.statistic.EntityStatistic
import org.spongepowered.api.statistic.ItemStatistic
import org.spongepowered.api.statistic.StatisticType
import org.spongepowered.api.text.format.TextColor
import org.spongepowered.api.text.selector.SelectorFactory
import org.spongepowered.api.text.serializer.TextSerializerFactory
import org.spongepowered.api.text.translation.Translation
import org.spongepowered.api.util.ResettableBuilder
import org.spongepowered.api.util.rotation.Rotation
import org.spongepowered.api.world.extent.ExtentBufferFactory
import org.spongepowered.common.SpongeImpl
import org.spongepowered.common.config.SpongeConfig
import org.spongepowered.common.config.type.ConfigBase
import org.spongepowered.common.config.type.TrackerConfig
import java.awt.image.BufferedImage
import java.io.InputStream
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.function.Supplier
import java.util.logging.Logger
import kotlin.collections.ArrayList

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
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
class EntityServiceTest {
    /**
     * Given
     *   a valid setup and no registered entities
     * When
     *    registerEntitiesOnServer is called
     * Then
     *   true should be returned.
     */
    @Test
    fun registerEntitiesOnServer_ValidState_ShouldReturnTrue() {
        // Arrange
        val classUnderTest = createWithDependencies()

        // Act
        val success = classUnderTest.registerEntitiesOnServer()

        // Assert
        Assertions.assertTrue(success)
    }

    /**
     * Given
     *   a valid setup and registered entities
     * When
     *    registerEntitiesOnServer is called
     * Then
     *   true should be returned.
     */
    @Test
    fun registerEntitiesOnServer_AlreadyRegisteredState_ShouldReturnTrue() {
        // Arrange
        val classUnderTest = createWithDependencies()

        // Act
        classUnderTest.registerEntitiesOnServer()
        val success = classUnderTest.registerEntitiesOnServer()

        // Assert
        Assertions.assertTrue(success)
    }

    companion object {
        fun createWithDependencies(): EntityService {
            val tracker = SpongeConfig<TrackerConfig>(SpongeConfig.Type.TRACKER, null, null, null)

            val field = Sponge::class.java.getDeclaredField("registry")
            field.isAccessible = true
            field.set(null, MockedGameRegistry())

            val secondField = SpongeImpl::class.java.getDeclaredField("trackerConfig")
            secondField.isAccessible = true
            secondField.set(null, tracker)

            val thirdField = SpongeConfig::class.java.getDeclaredField("configMapper")
            thirdField.isAccessible = true
            thirdField.set(tracker, ObjectMapper.forObject(TrackerConfig()))

            return EntityServiceImpl(LoggingHelper())
        }
    }

    private class MockedGameRegistry : GameRegistry {
        override fun <T : CatalogType?> registerModule(catalogClass: Class<T>?, registryModule: CatalogRegistryModule<T>?): GameRegistry {
            throw IllegalArgumentException()
        }

        override fun registerModule(module: RegistryModule?): GameRegistry {
            throw IllegalArgumentException()
        }

        override fun getCraftingRecipeRegistry(): CraftingRecipeRegistry {
            throw IllegalArgumentException()
        }

        override fun getEntityStatistic(statType: StatisticType?, entityType: EntityType?): Optional<EntityStatistic> {
            throw IllegalArgumentException()
        }

        override fun getResourcePackById(id: String?): Optional<ResourcePack> {
            throw IllegalArgumentException()
        }

        override fun getSelectorFactory(): SelectorFactory {
            throw IllegalArgumentException()
        }

        override fun getLocale(locale: String?): Locale {
            throw IllegalArgumentException()
        }

        override fun <T : CatalogType?> getAllFor(pluginId: String?, typeClass: Class<T>?): MutableCollection<T> {
            throw IllegalArgumentException()
        }

        override fun getItemStatistic(statType: StatisticType?, itemType: ItemType?): Optional<ItemStatistic> {
            throw IllegalArgumentException()
        }

        override fun <T : CatalogType?> register(type: Class<T>?, obj: T): T {
            throw IllegalArgumentException()
        }

        override fun getRotationFromDegree(degrees: Int): Optional<Rotation> {
            throw IllegalArgumentException()
        }

        override fun getSmeltingRecipeRegistry(): SmeltingRecipeRegistry {
            throw IllegalArgumentException()
        }

        override fun <T : ResettableBuilder<*, in T>?> createBuilder(builderClass: Class<T>?): T {
            throw IllegalArgumentException()
        }

        override fun getBlockStatistic(statType: StatisticType?, blockType: BlockType?): Optional<BlockStatistic> {
            throw IllegalArgumentException()
        }

        override fun <T : CatalogType?> getAllOf(typeClass: Class<T>?): MutableCollection<T> {
            return mutableListOf()
        }

        override fun getValueFactory(): ValueFactory {
            throw IllegalArgumentException()
        }

        override fun getVillagerRegistry(): VillagerRegistry {
            throw IllegalArgumentException()
        }

        override fun <T : CatalogType?> getType(typeClass: Class<T>?, id: String?): Optional<T> {
            throw IllegalArgumentException()
        }

        override fun getTranslationById(id: String?): Optional<Translation> {
            throw IllegalArgumentException()
        }

        override fun getTextSerializerFactory(): TextSerializerFactory {
            throw IllegalArgumentException()
        }

        override fun registerAITaskType(plugin: Any?, id: String?, name: String?, aiClass: Class<out AbstractAITask<out Agent>>?): AITaskType {
            throw IllegalArgumentException()
        }

        override fun getExtentBufferFactory(): ExtentBufferFactory {
            throw IllegalArgumentException()
        }

        override fun <T : Any?> registerBuilderSupplier(builderClass: Class<T>?, supplier: Supplier<out T>?): GameRegistry {
            throw IllegalArgumentException()
        }

        override fun loadFavicon(raw: String?): Favicon {
            throw IllegalArgumentException()
        }

        override fun loadFavicon(path: Path?): Favicon {
            throw IllegalArgumentException()
        }

        override fun loadFavicon(url: URL?): Favicon {
            throw IllegalArgumentException()
        }

        override fun loadFavicon(`in`: InputStream?): Favicon {
            throw IllegalArgumentException()
        }

        override fun loadFavicon(image: BufferedImage?): Favicon {
            throw IllegalArgumentException()
        }

        override fun getDisplaySlotForColor(color: TextColor?): Optional<DisplaySlot> {
            throw IllegalArgumentException()
        }

        override fun getDefaultGameRules(): MutableCollection<String> {
            throw IllegalArgumentException()
        }
    }
}