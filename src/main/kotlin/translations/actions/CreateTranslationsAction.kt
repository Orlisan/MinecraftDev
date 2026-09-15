package com.demonwav.mcdev.translations.actions

import com.demonwav.mcdev.asset.GeneralAssets
import com.demonwav.mcdev.translations.inspections.CreateTranslationQuickFix
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.psi.PsiManager


class CreateTranslationsAction :
    AnAction("Minecraft Translations", "Create custom translation using Groq API", GeneralAssets.TRANSLATIONS_ICON) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val view = e.getData(LangDataKeys.IDE_VIEW)
        val directory = view?.orChooseDirectory ?: return

        CreateTranslationQuickFix.createTranslationsWithAI(project, element = directory)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val project = e.project
        val data = e.getData(LangDataKeys.IDE_VIEW)
        e.presentation.isEnabledAndVisible = project != null && data != null
    }
}
