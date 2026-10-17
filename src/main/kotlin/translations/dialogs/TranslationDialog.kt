package com.demonwav.mcdev.translations.dialogs

import com.demonwav.mcdev.asset.GeneralAssets
import com.demonwav.mcdev.translations.TranslationConstants
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import java.awt.Component
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.OverlayLayout

typealias Translation = ArrayList<Pair<String, String>>

class TranslationDialog(
    private val project: Project,
    val translationKey: String = "",
    val remembedApiKey: String = ""
) : DialogWrapper(project) {
    var rememberKey = remembedApiKey.isNotBlank()

    lateinit var langsButton: JButton
    override fun createCenterPanel(): JComponent = panel {
        row("Api Key: ") {
            apiKeyField = textField().component
            checkBox("Remember").bindSelected(::rememberKey)
            cell(
                ContextHelpLabel.create(
                    "Create translations", "<html>Insert your Groq Api Key\n avalaible " +
                        "on https://console.groq.com/keys " +
                        "\n and insert that here for\n automatic translate your mod\n in 24+ languages" +
                        "<b>Warning: Translations uses AI models that might hallucinate.</b></html>"
                )
            )
            if (remembedApiKey.isNotBlank()) {
                apiKeyField.text = remembedApiKey; rememberKey = true
            }
            langsButton = button("Languages") {
                val langDialog = LanguagesDialog(project)
                if (langDialog.showAndGet()) {
                    langs = langDialog.selectedLangs
                }
            }.component
        }
        row {
            translationsPanel = buildTranslPanel()
            translationScPanel = JBScrollPane(translationsPanel)
            translationScPanel.preferredSize = Dimension(translationScPanel.preferredSize.width, 120)
            cell(translationScPanel)
        }
        row {
            button("Add Translation") {
                numberOfTranslations++
                refreshTransl()
            }
            button("Remove Translation") {
                if (numberOfTranslations > 1) {
                    numberOfTranslations--
                    translPairs.removeLast()
                    refreshTransl()
                }
            }
        }
    }

    override fun continuousValidation(): Boolean = contValidation
    override fun doValidate(): ValidationInfo? {
        if (apiKeyField.text.isNullOrBlank()) return validationInfo("Api Key cannot be empty", apiKeyField)
        for ((keyField, valueField) in translPairs) {
            if (keyField.text.isEmpty()) return validationInfo("This key cannot be empty", keyField)
            if (valueField.text.isEmpty()) return validationInfo("This value cannot be empty", valueField)
        }
        if (langs.isEmpty()) return validationInfo("No languages selected", langsButton)
        return super.doValidate()
    }

    var contValidation = false
    fun validationInfo(text: String, component: JComponent): ValidationInfo {
        contValidation = true
        return ValidationInfo(text, component)
    }

    val translPairs: ArrayList<Pair<JBTextField, JBTextField>> = arrayListOf()
    fun buildTranslPanel(): DialogPanel = panel {
        val translsPairCopy = translPairs.toMutableList()
        translPairs.clear()
        for (x in 1..numberOfTranslations) {
            row("Key:") {
                val areaKey = textField()
                if (x <= translsPairCopy.size) areaKey.component.text = translsPairCopy[x - 1].first.text
                label("Transl:")
                val areaTranslation = textField()
                if (x <= translsPairCopy.size) areaTranslation.component.text = translsPairCopy[x - 1].second.text
                if (translationKey.isNotBlank() && x == 1 &&
                    (areaKey.component.text == null || areaKey.component.text == "")
                ) areaKey.component.text =
                    translationKey
                translPairs.add(areaKey.component to areaTranslation.component)
            }
        }
    }

    fun refreshTransl() {
        val newTransl = buildTranslPanel()

        translationScPanel.setViewportView(newTransl)

        translationScPanel.revalidate()
        translationScPanel.repaint()
    }

    var numberOfTranslations: Int = 1
    private lateinit var translationScPanel: JBScrollPane
    private lateinit var translationsPanel: DialogPanel
    private lateinit var apiKeyField: JBTextField
    private val returnValue: Translation = arrayListOf()
    private var langs: ArrayList<String> = arrayListOf()
    fun getApiKey(): String {
        return if (!::apiKeyField.isInitialized || apiKeyField.text == null) "" else apiKeyField.getText()
    }

    init {
        title = "Create Translations"
        init()
    }

    override fun doOKAction() {
        translPairs.forEach {
            returnValue.add(it.first.text to it.second.text)
        }
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
                            val flag = JBLabel(flag)
                            val tick = JBLabel(GeneralAssets.TICK)
                            tick.isVisible = false
                            val overlay = JPanel()
                            overlay.layout = OverlayLayout(overlay)
                            flag.alignmentX = Component.CENTER_ALIGNMENT
                            flag.alignmentY = Component.CENTER_ALIGNMENT
                            tick.alignmentX = Component.CENTER_ALIGNMENT
                            tick.alignmentY = Component.CENTER_ALIGNMENT
                            overlay.add(tick)
                            overlay.add(flag)
                            cell(overlay)
                            val realIndex = index + (colonna * 6)
                            button(TranslationConstants.LANG_NAMES[realIndex].first) { event ->
                                val btn = event.source as JButton
                                if (selectedButtons.contains(btn)) {
                                    selectedLangs.remove(TranslationConstants.LANG_NAMES[realIndex].second);
                                    tick.isVisible = false;
                                    selectedButtons.remove(btn)
                                } else {
                                    selectedLangs.add(TranslationConstants.LANG_NAMES[realIndex].second);
                                    tick.isVisible = true
                                    selectedButtons.add(btn)
                                }
                            }
                        }
                    }
                }
                row("Other languages: ") {
                    otherTextField = textField().component
                    cell(
                        ContextHelpLabel.create(
                            "Insert more languages", "Insert, with the minecraft xy_za langs codes (ex. en_us, " +
                                "\n, unexistent codes will be ignored). the others languages that not are above that you want translate in" +
                                "\nsepare the codes with ',' spaces are allowed"
                        )
                    )
                }
            }
        lateinit var otherTextField: JBTextField
        override fun doOKAction() {
            selectedLangs.addAll(otherTextField.text.split(",").map{it.trim()}.filter {it.isNotBlank()})
            super.doOKAction()
        }
    }
}
