package com.demonwav.mcdev.translations.inspections

import com.demonwav.mcdev.translations.dialogs.TranslationDialog
import com.demonwav.mcdev.translations.identification.TranslationIdentifier
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.util.IncorrectOperationException
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.toUElementOfType

class CreateTranslationQuickFix(private val fromNothing: Boolean = true) : LocalQuickFix {
    override fun getName() = "Create translation"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        try {
            val element = descriptor.psiElement
            val literal = element.toUElementOfType<ULiteralExpression>() ?: return
            val translation = TranslationIdentifier.identify(literal)
            val literalValue = literal.value as String
            val key = translation?.key?.copy(infix = literalValue)?.full ?: literalValue
//                val result = Messages.showInputDialog(
//                    project,
//                    "Enter default value for \"$key\":",
//                    "Create Translation",
//                    Messages.getQuestionIcon(),
//                )
//                if (result != null) {
//                    TranslationFiles.add(literal.sourcePsi!!, key, result).onFailure {
//                        return showBalloon(project, null, element, it.message)
//                    }
//                }
            val module = ModuleUtilCore.findModuleForPsiElement(element)
            if(module != null) {
                val dialog = TranslationDialog(project, literalValue)
                if(dialog.showAndGet()) {
                    println("Aperto: Api Key: ${dialog.getApiKey()}, lingue selezionate: ${dialog.returnedLangs()}")
                }
            }

        } catch (_: IncorrectOperationException) {
        } catch (e: Exception) {
            Notification(
                "Translation support error",
                "Error while adding translation",
                e.message ?: e.stackTraceToString(),
                NotificationType.WARNING,
            ).notify(project)
        }
    }

    override fun startInWriteAction() = false

    override fun getFamilyName() = name
}
