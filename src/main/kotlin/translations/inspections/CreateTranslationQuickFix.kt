package com.demonwav.mcdev.translations.inspections

import com.demonwav.mcdev.facet.MinecraftFacet
import com.demonwav.mcdev.insight.mkdirIfAbsent
import com.demonwav.mcdev.platform.fabric.FabricModuleType
import com.demonwav.mcdev.platform.forge.ForgeModuleType
import com.demonwav.mcdev.platform.neoforge.NeoForgeModuleType
import com.demonwav.mcdev.translations.TranslationFiles
import com.demonwav.mcdev.translations.dialogs.TranslationDialog
import com.demonwav.mcdev.translations.identification.TranslationIdentifier
import com.demonwav.mcdev.util.findModule
import com.google.gson.JsonParser
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.util.IncorrectOperationException
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.chat.completions.ChatCompletionCreateParams
import java.time.Duration
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.toUElementOfType

class CreateTranslationQuickFix : LocalQuickFix {
    override fun getName() = "Create translation"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        try {
            val element = descriptor.psiElement
            val literal = element.toUElementOfType<ULiteralExpression>() ?: return
            val translation = TranslationIdentifier.identify(literal)
            val literalValue = literal.value as String
            val key = translation?.key?.copy(infix = literalValue)?.full ?: literalValue
            createTranslationsWithAI(project, key, element)

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

    companion object {
        val attributes = CredentialAttributes(generateServiceName("MinecraftDev", "userApiKey"))
        fun saveApiKey(key: String?) {
            PasswordSafe.instance.set(attributes, Credentials("userApiKey", key))
        }

        fun takeApiKey(): String? = PasswordSafe.instance.getPassword(attributes)
        fun deleteApiKey() = saveApiKey(null)
        fun createTranslationsWithAI(project: Project, key: String = "", element: PsiElement) {
            val remembedApiKey = if (takeApiKey() != null) takeApiKey()!! else ""
            val dialog = if (key.isNotBlank()) {
                TranslationDialog(project, key, remembedApiKey)
            } else TranslationDialog(project, translationKey = "", remembedApiKey = remembedApiKey)
            if (dialog.showAndGet()) {
                val apiKey = dialog.getApiKey()
                if (remembedApiKey.isNotBlank() && !dialog.rememberKey) {
                    deleteApiKey()
                } else if (remembedApiKey != apiKey && dialog.rememberKey) {
                    saveApiKey(apiKey)
                }
                val langs = dialog.returnedLangs()
                val returnedCouples = dialog.returnedCouples()
                val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())


                val client = initClient(apiKey)
                val module =
                    element.findModule() ?: return
                val modId =
                    MinecraftFacet.getInstance(module, FabricModuleType, NeoForgeModuleType, ForgeModuleType)?.modIds[0]
                        ?:return
                val langDir = WriteAction.compute<PsiDirectory, Throwable> {
                    val resourcesRoot = ModuleRootManager.getInstance(module)
                        .getSourceRoots(JavaModuleSourceRootTypes.RESOURCES).firstOrNull()
                        ?: error("No Resources Root Founded")
                    val rDir = PsiManager.getInstance(module.project).findDirectory(resourcesRoot)
                        ?: error("No Resources Root Found")
                    rDir.mkdirIfAbsent("assets").mkdirIfAbsent(modId).mkdirIfAbsent("lang")
                }
                println("LINGUE SELEZIONATE: $langs")
                scope.launch {

                    for ((first, second) in returnedCouples) {
                        val translations = askGemini(second, langs, client)
                        println("RISULTATO: $translations")
                        if (translations.isEmpty()) continue
                        runOnEdtAndWait(module.project) action@{
                            for (lang in translations.map {it.first}) {
                                var file = langDir.findFile("$lang.json")
                                if (file == null || file.isDirectory) {
                                    val realfile = langDir.createFile("$lang.json")
                                    val documentManager = PsiDocumentManager.getInstance(langDir.project)
                                    val document =
                                        documentManager.getDocument(realfile) ?: return@action
                                    document.setText("{}")
                                    FileDocumentManager.getInstance().saveDocument(document)
                                    documentManager.commitDocument(document)
                                    file = realfile
                                }
                                TranslationFiles.changeLocale(lang)
                                TranslationFiles.add(
                                    file,
                                    first,
                                    translations.firstOrNull { it.first == lang }?.second ?: continue
                                )
                            }

                        }

                    }
                    TranslationFiles.changeLocale("en_us")

                }
            }
        }

        suspend fun runOnEdtAndWait(project: Project, action: () -> Unit) = suspendCancellableCoroutine<Unit> { cont ->
            ApplicationManager.getApplication().invokeLater({
                WriteCommandAction.runWriteCommandAction(project) {
                    action()
                }
                cont.resume(Unit)
            }, ModalityState.defaultModalityState())
        }

        fun initClient(apiKey: String): OpenAIClient = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .baseUrl("https://api.groq.com/openai/v1/")
            .timeout(Duration.ofSeconds(30))
            .build()

        suspend fun askGemini(
            trad: String,
            langs: List<String>,
            client: OpenAIClient
        ): List<Pair<String, String>> {
            try {
                val prompt = "You must faithfully translate " +
                    "the string specified below, deducing " +
                    "the source language, into the languages " +
                    ",specified as xy_za or xyz_ab strings, " +
                    "following Minecraft's translation file naming conventions" +
                    " (e.g., en_us = American English, lmo_it = Lombard Dialect)." +
                    "Answer this message only a json format exatly, without any kind of format type like \"```\", like:" +
                    "\"la_ng1\":\"Translation1\"," +
                    "\"la_ng2\":\"Translation2\"" +
                    ",It leaves the Minecraft placeholders unchanged, " +
                    "placing them in the same context as before; Do not translate into" +
                    " the language in which the user" +
                    " wrote the initial translation by simply using that text as the translation." +
                    " Langs: $langs , Strding to translate: $trad"
                val params = ChatCompletionCreateParams.builder()
                    .model("openai/gpt-oss-120b")
                    .addUserMessage(prompt)
                    .build()

                val chatCompletion = client.chat().completions().create(params)
                val text = chatCompletion.choices().first().message().content().orElse("{}")
                val jsonObject = JsonParser.parseString(text).asJsonObject
                val result: ArrayList<Pair<String, String>> = arrayListOf()
                jsonObject.entrySet().forEach { entry ->
                    result.add(entry.key.toString() to entry.value.asString)
                }
                return result.filter {it.first.matches("^[a-z]{2,3}_[a-z]{2}$".toRegex())}
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Messages.showErrorDialog("Error in Translations Generation: ${e.message}", "Translation Error")
                }
            }
            return emptyList()
        }
    }

    override fun startInWriteAction() = false

    override fun getFamilyName() = name
}
