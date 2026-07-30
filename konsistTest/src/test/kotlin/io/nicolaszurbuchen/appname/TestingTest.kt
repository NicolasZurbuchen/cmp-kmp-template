package io.nicolaszurbuchen.appname

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.ext.list.withSourceSet
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class TestingTest {
    companion object {
        private val scope = Konsist.scopeFromModule("shared")

        private fun hasCorrespondingTestFile(file: KoFileDeclaration): Boolean = scope.files.any { it.name == "${file.name}Test" }
    }

    // region mapper coverage

    @Test
    fun `every mapper file has a corresponding test file`() {
        scope.files
            .filter { it.resideInPath("..mapper..") && it.name.endsWith("Mapper") }
            .assertTrue { mapperFile -> hasCorrespondingTestFile(mapperFile) }
    }

    @Test
    fun `every RepositoryImpl and DataSourceImpl file has a corresponding test file`() {
        scope.files
            .filter { it.name.endsWith("RepositoryImpl") || it.name.endsWith("DataSourceImpl") }
            .assertTrue { implFile -> hasCorrespondingTestFile(implFile) }
    }

    // endregion

    // region test file location

    @Test
    fun `test files reside in commonTest or androidHostTest mirroring their subject's package`() {
        scope.files
            .filter { it.name.endsWith("Test") }
            .assertTrue { testFile ->
                (testFile.resideInSourceSet("commonTest") || testFile.resideInSourceSet("androidHostTest")) &&
                    scope.files.any {
                        it.resideInSourceSet("commonMain") &&
                            it.packagee?.name == testFile.packagee?.name
                    }
            }
    }

    @Test
    fun `every file in androidHostTest ends with Test`() {
        scope.files
            .withSourceSet("androidHostTest")
            .assertTrue { it.name.endsWith("Test") }
    }

    // endregion

    // region fakes

    @Test
    fun `top-level classes prefixed Fake implement an interface and reside in a domain fake package`() {
        scope.classes()
            .filter { it.name.startsWith("Fake") && it.isTopLevel }
            .assertTrue { fakeClass ->
                fakeClass.parents().isNotEmpty() && fakeClass.resideInPackage("..domain.fake")
            }
    }

    // endregion
}
