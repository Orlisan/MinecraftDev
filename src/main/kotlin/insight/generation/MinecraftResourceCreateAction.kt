package com.demonwav.mcdev.insight.generation

import com.demonwav.mcdev.insight.mkdirIfAbsent
import com.demonwav.mcdev.asset.GeneralAssets
import com.demonwav.mcdev.asset.MCDevBundle
import com.demonwav.mcdev.asset.PlatformAssets
import com.demonwav.mcdev.facet.MinecraftFacet
import com.demonwav.mcdev.insight.generation.MinecraftClassCreateAction.ClassInputValidator
import com.demonwav.mcdev.insight.generation.ui.PotionGenerationDialog
import com.demonwav.mcdev.platform.fabric.FabricModuleType
import com.demonwav.mcdev.platform.forge.ForgeModuleType
import com.demonwav.mcdev.platform.mcp.McpModuleType
import com.demonwav.mcdev.platform.neoforge.NeoForgeModuleType
import com.demonwav.mcdev.util.MinecraftTemplates
import com.demonwav.mcdev.util.MinecraftVersions
import com.demonwav.mcdev.util.SemanticVersion
import com.demonwav.mcdev.util.findModule
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import java.util.*
import org.jetbrains.annotations.NonNls
import org.jetbrains.jps.model.java.JavaResourceRootType


class MinecraftResourceCreateAction : CreateFileFromTemplateAction(
    Const.CAPTION,
    MCDevBundle("generate.class.description"),
    GeneralAssets.MC_TEMPLATE
) {
    override fun isAvailable(context: DataContext): Boolean {
        val psi = context.getData(CommonDataKeys.PSI_ELEMENT)
        val module = psi?.findModule() ?: return false
        val dir: PsiDirectory?
        if (psi is PsiFile) {
            dir = psi.containingDirectory
            //   MinecraftFacet.getInstance(module)?.findFile(psi. SourceType.RESOURCE) ?: return false
        } else if (psi is PsiDirectory) {
            dir = psi
        } else {
            return false
        }

        val project = context.getData(CommonDataKeys.PROJECT) ?: return false
        val underSourceRootOfType = ProjectRootManager.getInstance(project).fileIndex.isUnderSourceRootOfType(
            dir.virtualFile,
            setOf(JavaResourceRootType.RESOURCE)
        )
        return underSourceRootOfType && findModid(dir, module) != null
    }

    private fun findModid(dir: PsiDirectory, module: Module, needsModidDirectory: Boolean = false): String? {
        val modids = MinecraftFacet.getInstance(module, ForgeModuleType)?.modIds ?: MinecraftFacet.getInstance(
            module,
            FabricModuleType
        )?.modIds ?: MinecraftFacet.getInstance(module, NeoForgeModuleType)?.modIds ?: return null
        val split = dir.virtualFile.path.split("/")
        return if (!needsModidDirectory) {
            modids.firstOrNull { split.contains(it) }
        } else {
            modids.firstOrNull { split[split.size - 1] == it }
        }
    }

    private fun isInAssets(dir: PsiDirectory): Boolean {
        return dir.virtualFile.path.split("/").contains("assets")
    }

    override fun buildDialog(
        project: Project,
        directory: PsiDirectory,
        builder: CreateFileFromTemplateDialog.Builder
    ) {
        builder.setTitle(Const.CAPTION)
        builder.setValidator(ClassInputValidator(project, directory))

        val module = directory.findModule() ?: return
        val mcVersion = MinecraftFacet.getInstance(module, McpModuleType)?.getSettings()
            ?.minecraftVersion?.let(SemanticVersion::parse)

        val icon = PlatformAssets.MINECRAFT_ICON

        if (findModid(directory, module, true) != null) {
            if (isInAssets(directory)) {
                builder.addKind("Block", icon, "BlockStructure")
                if (mcVersion == null || mcVersion >= MinecraftVersions.MC1_21_4) {
                    builder.addKind("Item", icon, "NewItemStructure")
                    builder.addKind("BlockItem", icon, "NewBlockItemStructure")
                } else {
                    builder.addKind("Item", icon, "OldItemStructure")
                    builder.addKind("BlockItem", icon, "OldBlockItemStructure")
                }
            } else {
            
                builder.addKind("Dimension", icon, "DimensionStructure")
                builder.addKind("Enchantment", icon, "EnchantmentStructure")
                
             //   if (mcVersion >= MinecraftVersions.MC26_3) {
                    builder.addKind("Potion recipes (26.3+)", icon, "Potion")
               // }
            }
        } else if (mcVersion != null && findModid(directory, module) != null) {
            if (!isInAssets(directory)) {
                if (mcVersion >= MinecraftVersions.MC1_21) {
                    builder.addKind("Enchantment", icon, MinecraftTemplates.JSON_ENCHANTMENT_TEMPLATE)
                }
                builder.addKind("Dimension", icon, MinecraftTemplates.JSON_DIMENSION_TEMPLATE)
                builder.addKind("Dimension type", icon, MinecraftTemplates.JSON_DIMENSION_TYPE_TEMPLATE)
            } else {
                if (mcVersion >= MinecraftVersions.MC1_21_4) {
                    builder.addKind("Item definition", icon, MinecraftTemplates.JSON_ITEM_DEFINITION_TEMPLATE)
                }
                builder.addKind("Item model", icon, MinecraftTemplates.JSON_ITEM_MODEL_TEMPLATE)
                builder.addKind("Block model", icon, MinecraftTemplates.JSON_BLOCK_MODEL_TEMPLATE)
                builder.addKind("Block state", icon, MinecraftTemplates.JSON_BLOCK_STATE_TEMPLATE)
                builder.addKind("Block item model", icon, MinecraftTemplates.JSON_BLOCK_ITEM_MODEL_TEMPLATE)
            }
        }
    }

    override fun getActionName(
        directory: PsiDirectory?,
        newName: @NonNls String,
        templateName: @NonNls String?
    ): @NlsContexts.Command String = Const.CAPTION


    override fun createFile(name: String?, templateName: String?, dir: PsiDirectory?): PsiFile? {
        val dir = dir ?: return null
        val module = dir.findModule() ?: return null
        val modid = findModid(dir, module) ?: return null

        when {
            templateName == "BlockStructure" -> {
                return WriteCommandAction.writeCommandAction(dir.project)
                    .withName(Const.CAPTION)
                    .compute<PsiFile?, Throwable> {
                        val blockstatesDir = dir.mkdirIfAbsent("blockstates")
                        val result = createFromTemplate(
                            name, blockstatesDir,
                            modid,
                            MinecraftTemplates.JSON_BLOCK_STATE_TEMPLATE
                        )
                        if (result != null) {
                            val modelDir = dir.mkdirIfAbsent("models").mkdirIfAbsent("block")
                            createFromTemplate(
                                name, modelDir,
                                modid,
                                MinecraftTemplates.JSON_BLOCK_MODEL_TEMPLATE
                            )
                        }
                        result
                    }
            }

            templateName?.contains("ItemStructure") == true -> {
                return WriteCommandAction.writeCommandAction(dir.project)
                    .withName(Const.CAPTION)
                    .compute<PsiFile?, Throwable> {
                        val modelDir = dir.mkdirIfAbsent("models").mkdirIfAbsent("item")
                        val result = if (!templateName.contains("Block")) {
                            createFromTemplate(
                                name, modelDir,
                                modid,
                                MinecraftTemplates.JSON_ITEM_MODEL_TEMPLATE
                            )
                        } else {
                            createFromTemplate(
                                name, modelDir,
                                modid,
                                MinecraftTemplates.JSON_BLOCK_ITEM_MODEL_TEMPLATE
                            )
                        }
                        if (templateName.contains("New") && result != null) {
                            val itemsDir = dir.mkdirIfAbsent("items")
                            createFromTemplate(
                                name, itemsDir,
                                modid,
                                MinecraftTemplates.JSON_ITEM_DEFINITION_TEMPLATE
                            )
                        }
                        result
                    }
            }

            templateName.equals("DimensionStructure") -> {
                return WriteCommandAction.writeCommandAction(dir.project)
                    .withName(Const.CAPTION)
                    .compute<PsiFile?, Throwable> {
                        val typeDir = dir.mkdirIfAbsent("dimension_type")
                        val result =
                            createFromTemplate(
                                name, typeDir, modid, MinecraftTemplates.JSON_DIMENSION_TYPE_TEMPLATE
                            )
                        if (result != null) {
                            val dimDir = dir.mkdirIfAbsent("dimension")
                            createFromTemplate(
                                name,
                                dimDir,
                                modid,
                                MinecraftTemplates.JSON_DIMENSION_TEMPLATE
                            )
                        }
                        result
                    }
            }

            templateName.equals("Potion") -> {
                return WriteCommandAction.writeCommandAction(dir.project)
                    .withName(Const.CAPTION)
                    .compute<PsiFile?, Throwable> {
                        val brewdir = dir.mkdirIfAbsent("recipe").mkdirIfAbsent("brewing")
                        val potionDialog = PotionGenerationDialog(dir.project)
                        if (potionDialog.showAndGet()) {
                            val reagent = potionDialog.reagent.text
                            val base = potionDialog.basePotion.text
                            val result = potionDialog.result.text
                            val p = "minecraft:potion"
                            val s = "minecraft:splash_potion"
                            val l = "minecraft:lingering_potion"
                            val result2 =   createPotionFromTemplate(name, brewdir, 
                                reagent, base, result, p,p )
                            if (potionDialog.splash) {
                                //SI
                                createPotionFromTemplate(name+"_splash_from_regular", brewdir, 
                                   "minecraft:gunpowder", result, result, p, s)
                                
                                createPotionFromTemplate(name+"_from_splash", brewdir,
                                reagent, base, result, s,s )
                            }
                            if (potionDialog.lingering) {
                                //SI
                                createPotionFromTemplate(name+"_lingering_from_splash", brewdir, 
                                    "minecraft:dragon_breath", result, result, s, l)
                                  createPotionFromTemplate(name+"_from_lingering", brewdir,
                                reagent, base, result, l,l)
                            }
                            if (potionDialog.long) {
                                //SI
                                createPotionFromTemplate("long_$name", brewdir,
                                    "minecraft:redstone", result, result.atStartPath("long_"), p, p)
                                if(potionDialog.splash) {
                                    //SI
                                    createPotionFromTemplate("long_splash_${name}_from_splash", brewdir,
                                        "minecraft:redstone", result, result.atStartPath("long_"), s, s)
                                    //SI
                                    createPotionFromTemplate("long_splash_${name}_from_long", brewdir,
                                        "minecraft:gunpowder", result.atStartPath("long_"), result.atStartPath("long_"), p, s)
                                }
                                if(potionDialog.lingering) {
                                   //SI
                                    createPotionFromTemplate("long_lingering_${name}_from_lingering", brewdir,
                                        "minecraft:redstone", result, result.atStartPath("long_"), l, l)
                                   //SI
                                    createPotionFromTemplate("long_lingering_${name}_from_long_splash", brewdir,
                                        "minecraft:dragon_breath", result.atStartPath("long_"), result.atStartPath("long_"), s, l)

                                }
                            }
                            if (potionDialog.powered) {
                                //SI
                                createPotionFromTemplate("strong_$name", brewdir, 
                                    "minecraft:glowstone_dust", result, result.atStartPath("strong_"), p, p)
                                if(potionDialog.splash) {
                                    //SI
                                    createPotionFromTemplate("strong_splash_${name}_from_splash", brewdir,
                                        "minecraft:glowstone_dust", result, result.atStartPath("strong_"), s, s)
                                    //SI
                                    createPotionFromTemplate("strong_splash_${name}_from_strong", brewdir,
                                        "minecraft:gunpowder", result.atStartPath("strong_"), result.atStartPath("strong_"), p, s)

                                }
                                if(potionDialog.lingering) {
                                    //SI
                                    createPotionFromTemplate("strong_lingering_${name}_from_lingering", brewdir,
                                        "minecraft:glowstone_dust", result, result.atStartPath("strong_"), l, l)
                                    //SI
                                    createPotionFromTemplate("strong_lingering_${name}_from_strong_splash", brewdir,
                                        "minecraft:dragon_breath", result.atStartPath("strong_"), result.atStartPath("strong_"), s, l)

                                }
                            }
                            return@compute result2
                        }
                        null
                    }
            }
            templateName.equals("EnchantmentStructure") -> {
                return WriteCommandAction.writeCommandAction(dir.project)
                    .withName(Const.CAPTION)
                    .compute<PsiFile?, Throwable> {
                        val enchDir = dir.mkdirIfAbsent("enchantment")
                        createFromTemplate(
                            name, enchDir, modid, MinecraftTemplates.JSON_ENCHANTMENT_TEMPLATE
                        )
                    }
            }

            else -> return createFromTemplate(name, dir, modid, templateName)
        }
    }

    fun createPotionFromTemplate(
        name: String?,
        dir: PsiDirectory,
        reagent: String,
        basePotion: String,
        resultPotion: String,
        inputItem: String,
        outputItem: String
    ): PsiFile? {
        val template = FileTemplateManager.getInstance(dir.project)
            .getInternalTemplate(MinecraftTemplates.JSON_POTION_RECIPE_TEMPLATE ?: return null)
        return createFileFromTemplate(
            name,
            template,
            dir,
            defaultTemplateProperty,
            true,
            Collections.emptyMap(),
            mapOf(
                "REAGENT" to reagent,
                "RESULT" to resultPotion,
                "BASEPOTION" to basePotion,
                "INPUT" to inputItem,
                "OUTPUT" to outputItem
            )
        )
    }

    fun String.atStartPath(value: String): String {
        val split = this.split(":")
        return split[0]+":"+value+split[1]
    }
    fun createFromTemplate(name: String?, dir: PsiDirectory, modid: String, templateName: String?): PsiFile? {
        val template = FileTemplateManager.getInstance(dir.project)
            .getInternalTemplate(templateName ?: return null)
        return createFileFromTemplate(
            name,
            template,
            dir,
            defaultTemplateProperty,
            true,
            Collections.emptyMap(),
            mapOf("MODID" to modid)
        )
    }

    private object Const {
        val CAPTION
            get() = MCDevBundle("generate.json.caption")
    }
}
