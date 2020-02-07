package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.localization.Messages
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.LocalizationService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.core.logic.business.extension.async
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.google.inject.Inject
import java.nio.file.Files
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Created by Shynixn 2020.
 * <p>
 * Version 1.5
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2020 by Shynixn
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
class LocalizationServiceImpl @Inject constructor(
    private val configurationService: ConfigurationService,
    private val loggingService: LoggingService,
    private val concurrencyService: ConcurrencyService
) :
    LocalizationService {
    private var properties = Properties()
    private val defaultLanguages = arrayListOf("en")

    /**
     * Reloads the localization.
     */
    init {
        reload()
    }

    /**
     * Checks if the given text contains the lang prefix and replaces it with the localized value.
     */
    override fun translate(text: String): String {
        var currentText = text

        for (item in properties.keys) {
            val fullKey = "\$lang.$item"
            currentText = currentText.replace(fullKey, properties.getProperty(item as String))
        }

        return currentText
    }

    /**
     * Reloads the localization of the messages.
     */
    override fun reload(): CompletableFuture<Void?> {
        val completableFuture = CompletableFuture<Void?>()

        async(concurrencyService) {
            properties = Properties()
            val langFileFolder = configurationService.applicationDir.resolve("lang")

            if (!Files.exists(langFileFolder)) {
                Files.createDirectory(langFileFolder)
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
                properties.load(stream)
            }

            for (field in Messages::class.java.declaredFields) {
                field.isAccessible = true

                if (field.type != String::class.java) {
                    continue
                }

                val fieldValue = field.get(null) as String

                if (!properties.containsKey(fieldValue)) {
                    loggingService.warn("Loaded language '$selectedLang' does not have definition for message '$fieldValue'!")
                    loggingService.warn("This message will not be available!")
                    continue
                }

                field.set(null, properties.getProperty(fieldValue).translateChatColors())
            }

            sync(concurrencyService) {
                completableFuture.complete(null)
            }
        }

        return completableFuture
    }
}