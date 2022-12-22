package com.github.shynixn.petblocks.bukkit.impl.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.github.shynixn.mcutils.common.translateChatColors
import com.github.shynixn.petblocks.bukkit.contract.PetTemplateRepository
import com.github.shynixn.petblocks.bukkit.entity.PetTemplate
import kotlinx.coroutines.*
import org.bukkit.plugin.Plugin
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.collections.ArrayList

class PetTemplateRepositoryImpl(
    private val folder: Path, private val plugin: Plugin, private vararg val templateNames: String
) : PetTemplateRepository {
    private var cache: Deferred<List<PetTemplate>>? = null
    private val objectMapper: ObjectMapper =
        ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
    private var typeReference = object : TypeReference<PetTemplate>() {}

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Creates all tempaltes if they do not exist yet.
     */
    override suspend fun copyTemplatesIfNotExist() {
        withContext(Dispatchers.IO) {
            val templateFolder = plugin.dataFolder.resolve("template")

            if (!templateFolder.exists()) {
                templateFolder.mkdir()
            }

            for (name in templateNames) {
                val file = templateFolder.resolve(name)

                if (!file.exists()) {
                    val stream = plugin.getResource("template/$name")

                    stream.use {
                        Files.copy(it, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }
        }
    }

    /**
     * Gets all templates from the repository.
     */
    override suspend fun getAll(): List<PetTemplate> {
        if (cache == null) {
            coroutineScope {
                cache = async(Dispatchers.IO) {
                    val templates = ArrayList<PetTemplate>()

                    for (file in folder.toFile().listFiles()) {
                        if (file.endsWith(".yml")) {
                            val template = objectMapper.readValue<PetTemplate>(
                                file, typeReference
                            )

                            templates.add(template)
                        }
                    }

                    templates
                }
            }
        }

        return cache!!.await()
    }
}
