package com.demonwav.mcdev.translations.dialogs

import com.demonwav.mcdev.asset.GeneralAssets
import com.demonwav.mcdev.translations.TranslationConstants
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import javax.swing.JButton
import javax.swing.JComponent

typealias Translation = List<Pair<String?, String?>>

class TranslationDialog(private val project: Project, translationKey: String = "") : DialogWrapper(project) {

    override fun createCenterPanel(): JComponent = panel {
        row("Gemini Api Key: ") {
            apiKeyField = textField().component
            button("Languages") {
                val langDialog = LanguagesDialog(project)
                if (langDialog.showAndGet()) {
                    langs = langDialog.selectedLangs
                }
            }
        }
    }

    private lateinit var dialogPanel: DialogPanel
    private lateinit var apiKeyField: JBTextField
    private val onlyATranslation = translationKey.isNotBlank();
    private lateinit var returnValue: Translation
    private var langs: ArrayList<String> = arrayListOf()
    fun getApiKey(): String {
        return if (!::apiKeyField.isInitialized || apiKeyField.text == null) "" else apiKeyField.getText()
    }

    init {
        title = if (onlyATranslation) "Create Translation" else "Create Translations"
        init()
    }

    override fun doOKAction() {
        returnValue = listOf(null to null)
        //TODO: Molte cose
        super.doOKAction()
    }

    fun returnedLangs(): ArrayList<String> {
        return langs;
    }

    fun returnedCouples(): Translation {
        return returnValue;
    }

    private class LanguagesDialog(project: Project) : DialogWrapper(project) {
        val selectedLangs: ArrayList<String> = arrayListOf()
        val selectedButtons: MutableSet<JButton> = mutableSetOf()
        init {
            title = "Select Languages"
            init()
        }
        override fun createCenterPanel(): JComponent =
            panel {
                for (colonna in 0..<4) {
                    row {
                        for ((index, flag) in GeneralAssets.FLAGS.filterIndexed { indice, _ -> (indice - indice % 6) / 6 == colonna }
                            .withIndex()) {
                            cell<JBLabel>(JBLabel(flag))
                            val label: Cell<JBLabel> = cell(JBLabel(GeneralAssets.TICK))
                            label.component.isVisible = false
                            val realIndex = index + (colonna * 6)
                            button(TranslationConstants.LANG_NAMES[realIndex].first) { event ->
                                val btn = event.source as JButton
                                if (selectedButtons.contains(btn)) {
                                    selectedLangs.remove(TranslationConstants.LANG_NAMES[realIndex].second);
                                    label.component.isVisible = false;
                                    selectedButtons.remove(btn)
                                } else {
                                    selectedLangs.add(TranslationConstants.LANG_NAMES[realIndex].second);
                                    label.component.isVisible = true
                                    selectedButtons.add(btn)
                                }
                            }
                        }
                    }
                }
            }
    }

}

