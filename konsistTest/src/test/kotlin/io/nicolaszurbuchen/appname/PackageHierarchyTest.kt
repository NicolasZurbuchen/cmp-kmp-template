package io.nicolaszurbuchen.appname

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class PackageHierarchyTest {
    companion object {
        private val scope = Konsist.scopeFromProduction(moduleName = "shared")
    }

    @Test
    fun `Direct children of feature or core must be in allowed list`() {
        val allowed = listOf("presentation", "domain", "data", "di")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of presentation must be in allowed list`() {
        val allowed = listOf("screen", "component", "navigation", "uimodel")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.presentation\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of domain must be in allowed list`() {
        val allowed = listOf("model", "repository", "usecase")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.domain\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of data must be in allowed list`() {
        val allowed = listOf("repository", "datasource")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.data\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of data datasource must be in allowed list`() {
        val allowed = listOf("remote", "local")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.data\\.datasource\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of data datasource remote must be in allowed list`() {
        val allowed = listOf("api", "dto", "mapper")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.data\\.datasource\\.remote\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of data datasource local must be in allowed list`() {
        val allowed = listOf("mapper")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.data\\.datasource\\.local\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Top level packages must be in allowed list`() {
        val allowed = listOf("app", "core", "design", "feature", "infra")

        val allPackages = Konsist.scopeFromDirectory("shared/src/commonMain/kotlin").packages
        val allPackageNames = allPackages.map { it.name }

        if (allPackageNames.isEmpty()) return

        val rootPrefix =
            allPackageNames
                .reduce { acc, name -> acc.commonPrefixWith(name).trimEnd('.') }

        allPackages
            .assertTrue { pkg ->
                val relative = pkg.name.removePrefix("$rootPrefix.").trimStart('.')
                if (relative.isEmpty()) {
                    true
                } else {
                    relative.split(".").first() in allowed
                }
            }
    }

    @Test
    fun `Leaf packages must not have child packages`() {
        val leafPackageNames =
            setOf("api", "component", "di", "dto", "mapper", "model", "navigation", "repository", "usecase", "uimodel")

        val allPackages = Konsist.scopeFromProject().packages
        val allPackageNames = allPackages.map { it.name }.toSet()

        allPackages
            .filter { pkg ->
                pkg.name.contains(Regex("\\.(feature|core)\\.")) &&
                    pkg.name.split(".").last() in leafPackageNames
            }
            .assertTrue { pkg ->
                allPackageNames.none { it.startsWith("${pkg.name}.") }
            }
    }

    @Test
    fun `Screen name packages must not have child packages other than component`() {
        scope.files
            .filter { file ->
                file.packagee?.name?.contains(".presentation.screen.") == true
            }
            .assertTrue { file ->
                file.packagee?.name
                    ?.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.presentation\\.screen\\.[^.]+(\\.component)?$")) == true
            }
    }
}
