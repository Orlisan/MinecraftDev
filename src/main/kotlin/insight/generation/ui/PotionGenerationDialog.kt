package com.demonwav.mcdev.insight.generation.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class PotionGenerationDialog(project: Project): DialogWrapper(project) {
    lateinit var basePotion: JBTextField
    lateinit var reagent: JBTextField
    lateinit var result: JBTextField

    var splash: Boolean = true
    var lingering: Boolean = true
    var long: Boolean = true
    var powered: Boolean = true

    lateinit var splashBox: JBCheckBox
    lateinit var lingeringBox: JBCheckBox
    lateinit var longBox: JBCheckBox
    lateinit var poweredBox: JBCheckBox
    init {
        title = "Potion Generation"
        init()
    }
     override fun continuousValidation(): Boolean = contValidation
     var contValidation = false
    fun validationInfo(text: String, component: JComponent): ValidationInfo {
        contValidation = true
        return ValidationInfo(text, component)
    }
    
    override fun createCenterPanel(): JComponent = panel {
        row("Potion: ") {
            basePotion = textField().component
            basePotion.emptyText.text = "Base potion"
            basePotion.text = "minecraft:awkward_potion"
            reagent = textField().component
            reagent.emptyText.text = "Reagent"
            result = textField().component
            result.emptyText.text = "New potion"
        }
        row("Variants: ") {
            splashBox = checkBox("Splash").component
            splashBox.isSelected = splash
            lingeringBox = checkBox("Lingering").component
            lingeringBox.isSelected = lingering
            longBox = checkBox("Long").component
            longBox.isSelected = long
            poweredBox = checkBox("Power").component
            poweredBox.isSelected = powered
        }
    }
    override fun doValidate(): ValidationInfo? {
        if(reagent.text.isBlank()) return validationInfo("Reagent cannot be empty", reagent)
        if(basePotion.text.isBlank()) return validationInfo("Start potion cannot be empty", basePotion)
        if(result.text.isBlank()) return validationInfo("Result potion cannot be empty", result)
        if(!reagent.text.contains(":")) return validationInfo("Reagent field not contains a valid minecraft id", reagent)
        if(!basePotion.text.contains(":")) return validationInfo("Start field not contains a valid minecraft id", basePotion)
        if(!result.text.contains(":")) return validationInfo("Result potion field not contains a valid minecraft id", result)

        if(lingeringBox.isSelected && !splashBox.isSelected) {
            return validationInfo("Cannot creating lingering potion without splash potion", splashBox)
        }
        
        return null
    }
    override fun doOKAction() {
        powered = poweredBox.isSelected
        splash = splashBox.isSelected
        long = longBox.isSelected
        lingering = lingeringBox.isSelected
        super.doOKAction()
    }
}
