package io.nicolaszurbuchen.appname

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import kotlin.test.Test

class LayerBoundariesTest {
    @Test
    fun `feature layers should not depend on other features internal implementation`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.hasPackage("..feature..") }
            .assertFalse { file ->
                val packageName = file.packagee?.name ?: ""
                val currentFeature = packageName.substringAfter("feature.").substringBefore(".")

                file.imports.any { import ->
                    val importName = import.name
                    if (importName.contains("feature.")) {
                        val importedFeature = importName.substringAfter("feature.").substringBefore(".")
                        val isInternal = importName.contains(".presentation") || importName.contains(".data")

                        importedFeature != currentFeature && isInternal
                    } else {
                        false
                    }
                }
            }
    }

    @Test
    fun `infra should not depend on features`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.hasPackage("..infra..") }
            .assertFalse {
                it.imports.any { import -> import.name.contains(".feature.") }
            }
    }
}
