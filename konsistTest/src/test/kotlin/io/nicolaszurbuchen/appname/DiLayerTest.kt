package io.nicolaszurbuchen.appname

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class DiLayerTest {
    companion object {
        private val scope = Konsist.scopeFromProduction(moduleName = "shared")

        private val projectPackagePrefix =
            scope.packages
                .map { it.name }
                .reduce { acc, name ->
                    acc.commonPrefixWith(name).trimEnd('.')
                }
    }

    // region Package conventions

    @Test
    fun `files in di package must be suffixed with Module`() {
        scope.files
            .withPackage("..di..")
            .filter { it.hasPackage("..feature..di..") || it.hasPackage("..common..di..") }
            .assertTrue { it.name.endsWith("Module") }
    }

    @Test
    fun `files suffixed with Module must reside in di package`() {
        scope.files
            .withNameEndingWith("Module")
            .filter { it.hasPackage("..feature..di..") || it.hasPackage("..common..di..") }
            .assertTrue { it.hasPackage("..di..") }
    }

    // endregion

    // region Dependency boundaries

    @Test
    fun `di modules must only import from their own subtree`() {
        scope.files
            .withPackage("..di..")
            .filter { it.hasPackage("..feature..di..") || it.hasPackage("..common..di..") }
            .assertTrue { file ->
                if (file.hasPackage("..infra.di.app..")) return@assertTrue true

                val ownSubtree =
                    projectSubtree(file.packagee?.name)
                        ?: return@assertTrue true

                file.imports.all { import ->
                    val importSubtree = projectSubtree(import.name)
                    importSubtree == null || importSubtree == ownSubtree
                }
            }
    }

    // endregion

    /**
     * The owning subtree of a project package:
     *   <prefix>.feature.vault.di -> "feature.vault"
     *   <prefix>.common.crypto.di -> "common.crypto"
     *   <prefix>.infra.ui         -> "infra"
     * Returns null for external (non-project) packages.
     */
    private fun projectSubtree(qualifiedName: String?): String? {
        if (qualifiedName == null || !qualifiedName.startsWith(projectPackagePrefix)) return null
        val segments =
            qualifiedName
                .removePrefix(projectPackagePrefix)
                .trimStart('.')
                .split('.')
        return when (segments.firstOrNull()) {
            "feature", "common" -> segments.take(2).joinToString(".")
            "infra" -> "infra"
            else -> null
        }
    }
}
