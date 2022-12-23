package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.legacy.business.annotation.Key
import com.github.shynixn.petblocks.api.legacy.business.localization.Messages
import com.github.shynixn.petblocks.api.legacy.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.legacy.business.service.LocalizationService
import com.github.shynixn.petblocks.api.legacy.business.service.LoggingService
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.google.inject.Inject
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.*

/**
 * Handles loading of localization files.
 */
class LocalizationServiceImpl @Inject constructor(
    private val configurationService: ConfigurationService,
    private val loggingService: LoggingService
) :
    LocalizationService {
    private var properties = Properties()
    private val defaultLanguages = arrayListOf("en_us", "zh_cn", "de_de", "es_la", "pt_br", "fr_eu")

    /**
     * Checks if the given text contains the lang prefix and replaces it with the localized value.
     */
    override fun translate(text: String): String {
        var currentText = text

        for (item in properties.keys) {
            val fullKey = "\$lang.$item"
            currentText = currentText.replace(fullKey, properties.getProperty(item as String).replace("\"", ""))
        }

        return currentText
    }

    /**
     * Reloads the localization of the messages.
     */
    override fun reload() {
        properties = Properties()
        val langFileFolder = configurationService.applicationDir.resolve("lang")

        if (!Files.exists(langFileFolder)) {
            Files.createDirectories(langFileFolder)
        }

        for (lang in defaultLanguages) {
            val targetFile = langFileFolder.resolve("$lang.lang")

            if (!Files.exists(targetFile)) {
                Files.copy(configurationService.openResource("assets/petblocks/lang/$lang.lang"), targetFile)
            }
        }

        val selectedLang = configurationService.findValue<String>("lang")
        val langFile = langFileFolder.resolve("$selectedLang.lang")

        if (!Files.exists(langFile)) {
            loggingService.warn("Selected language '$selectedLang' cannot be found in ${langFile.toFile().absolutePath}.")
            loggingService.warn("Messages will no longer be available!")
        }

        Files.newInputStream(langFile).use { stream ->
            properties.load(InputStreamReader(stream, Charset.forName("UTF-8")))
        }

        for (field in Messages::class.java.declaredFields) {
            field.isAccessible = true

            if (field.type != String::class.java) {
                continue
            }

            val fieldValue = field.getDeclaredAnnotation(Key::class.java).value

            if (!properties.containsKey(fieldValue)) {
                loggingService.warn("Loaded language '$selectedLang' does not have definition for message '$fieldValue'!")
                loggingService.warn("This message will not be available!")
                continue
            }

            field.set(null, properties.getProperty(fieldValue).replace("\"", "").translateChatColors())
        }
    }
}
