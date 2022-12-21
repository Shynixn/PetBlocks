package com.github.shynixn.petblocks.bukkit.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.github.shynixn.petblocks.bukkit.contract.PetTemplateRepository
import com.github.shynixn.petblocks.bukkit.entity.PetTemplate
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.nio.file.Path

class PetTemplateRepositoryImpl(private val folder: Path) : PetTemplateRepository {
    private var cache: Deferred<List<PetTemplate>>? = null
    private val objectMapper: ObjectMapper =
        ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
    private var typeReference = object : TypeReference<PetTemplate>() {}

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
