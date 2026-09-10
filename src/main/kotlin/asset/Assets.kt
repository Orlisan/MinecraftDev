/*
 * Minecraft Development for IntelliJ
 *
 * https://mcdev.io/
 *
 * Copyright (C) 2025 minecraft-dev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, version 3.0 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.demonwav.mcdev.asset

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBLabel
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon

abstract class Assets protected constructor() {
    protected fun loadIcon(path: String): Icon {
        return IconLoader.getIcon(path, Assets::class.java)
    }
    protected fun loadFlagAtlas(path: String): List<ImageIcon> {
        val atlas = ImageIO.read(Assets::class.java.getResourceAsStream(path))
        val result: ArrayList<ImageIcon> = arrayListOf()
        for(flag in 0..<24) {
            val flagWidth = 60
            val flagHeight = 40
            val x = (flag%6)*flagWidth
            val y = (flag-flag%6)/6*flagHeight
            result.add(ImageIcon(atlas.getSubimage(x, y, flagWidth, flagHeight)))
        }
        return result
    }
}
