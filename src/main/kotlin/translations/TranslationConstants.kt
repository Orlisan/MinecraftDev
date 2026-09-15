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

package com.demonwav.mcdev.translations

object TranslationConstants {
    const val DEFAULT_LOCALE = "en_us"
    const val TRANSLATABLE_ANNOTATION = "com.demonwav.mcdev.annotations.Translatable"
    const val REQUIRED = "required"
    const val FOLD_METHOD = "foldMethod"
    const val ALLOW_ARBITRARY_ARGS = "allowArbitraryArgs"
    const val PREFIX = "prefix"
    const val SUFFIX = "suffix"
    val LANG_NAMES: List<Pair<String, String>> = listOf(
        "French" to "fr_fr",
        "Italian" to "it_it",
        "English" to "en_us",
        "Spanish" to "es_es",
        "Chinese" to "zh_cn",
        "Portuguese" to "pt_br",
        "Russian" to "ru_ru",
        "German" to "de_de",
        "Japanese" to "ja_jp",
        "Korean" to "ko_kr",
        "Indonesian" to "id_id",
        "Thai" to "th_th",
        "Turkish" to "tr_tr",
        "Polish" to "pl_pl",
        "Ukrainian" to "uk_ua",
        "Arabic" to "ar_sa",
        "Dutch" to "nl_nl",
        "Czech" to "cs_cz",
        "Swedish" to "sv_se",
        "Finnish" to "fi_fi",
        "Vietnamese" to "vi_vn",
        "Malay" to "ms_my",
        "Hungarian" to "hu_hu",
        "Romanian" to "ro_ro"
    )

}
