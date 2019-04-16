package com.github.shynixn.petblockstools

import com.github.shynixn.petblockstools.logic.service.ConfigServiceImpl
import com.github.shynixn.petblockstools.logic.service.PublishPetBlocksSnapshotToDiscord
import java.nio.file.Paths

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

/**
 * This is a quick and dirty tool program for generating the config.yml.
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        return
    }

    if (args[0] == "--generate-config") {
        val sourceFile = Paths.get(Thread.currentThread().contextClassLoader.getResource("sourcefile.csv").toURI())
        val sourceFolder = sourceFile.parent

        val configService = ConfigServiceImpl()
        configService.generateFiles(sourceFile, sourceFolder)
        return
    }

    if (args[0] == "--snapshot") {
        if (args.size != 2) {
            throw IllegalArgumentException("--snapshot requires 1 additional arguments [WebHookUrl]!")
        }

        val webHookUrl = args[1]
        val publishService = PublishPetBlocksSnapshotToDiscord()

        publishService.publishSnapshotToDiscord(webHookUrl)

        return
    }
}

