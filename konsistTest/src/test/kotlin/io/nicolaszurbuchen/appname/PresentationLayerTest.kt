package io.nicolaszurbuchen.appname

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class PresentationLayerTest {
    companion object {
        private val scope = Konsist.scopeFromProduction(moduleName = "shared")
    }

    // region Name implies location

    @Test
    fun `files suffixed with Contract must reside in screen package`() {
        scope.files
            .withNameEndingWith("Contract")
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    @Test
    fun `files suffixed with Route must reside in screen package`() {
        scope.files
            .withNameEndingWith("Route")
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    @Test
    fun `files suffixed with Screen must reside in screen package`() {
        scope.files
            .withNameEndingWith("Screen")
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    @Test
    fun `files suffixed with Preview must reside in screen package`() {
        scope.files
            .withNameEndingWith("Preview")
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    @Test
    fun `files suffixed with StoreFactory must reside in screen package`() {
        scope.files
            .withNameEndingWith("StoreFactory")
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    @Test
    fun `files suffixed with ViewModel must reside in screen package`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    // endregion

    // region Location implies name

    @Test
    fun `files in screen packages must use an allowed suffix`() {
        val allowedSuffixes =
            setOf(
                "Contract",
                "Preview",
                "Reducer",
                "Route",
                "Screen",
                "StoreFactory",
                "UiMapper",
                "UiModel",
                "ViewModel",
            )

        scope.files
            .withPackage("..presentation.screen..")
            .filter { file -> !file.hasPackage("..component..") }
            .assertTrue { file -> allowedSuffixes.any { suffix -> file.name.endsWith(suffix) } }
    }

    // endregion

    // region Component package rules

    @Test
    fun `files in component packages must not use a screen file suffix`() {
        val screenSuffixes = listOf("Contract", "Preview", "Route", "Screen", "ViewModel", "Flow")

        scope.files
            .withPackage("..component..")
            .filter { file ->
                screenSuffixes.any { suffix -> file.name.endsWith(suffix) }
            }
            .assertEmpty()
    }

    @Test
    fun `files in component packages must contain at least one Composable-annotated function`() {
        scope.files
            .withPackage("..component..")
            .assertTrue { file ->
                file.functions().any { it.hasAnnotationWithName("Composable") }
            }
    }

    @Test
    fun `files in component packages must declare only functions`() {
        scope.files
            .withPackage("..component..")
            .filter { file ->
                file.classes().isNotEmpty() || file.interfaces().isNotEmpty() || file.objects().isNotEmpty()
            }
            .assertEmpty()
    }

    @Test
    fun `files in component packages must expose exactly one public declaration`() {
        val composableScopeReceivers = setOf("LazyListScope", "ColumnScope", "RowScope", "BoxScope")

        scope.files
            .withPackage("..component..")
            .assertTrue { file ->
                val publicFunctions = file.functions().filter { it.hasPublicOrDefaultModifier }
                if (publicFunctions.size != 1) return@assertTrue false

                val fn = publicFunctions.first()
                fn.hasAnnotationWithName("Composable") ||
                    fn.receiverType?.name in composableScopeReceivers
            }
    }

    @Test
    fun `files in component packages must not import from domain or data layers`() {
        scope.files
            .withPackage("..component..")
            .filter { file ->
                file.imports.any { import ->
                    import.name.contains(".domain.") || import.name.contains(".data.")
                }
            }
            .assertEmpty()
    }

    // endregion

    // region Screen subfolder rules

    @Test
    fun `screen folders must contain only allowed file names`() {
        val allowedSuffixes =
            setOf("Contract", "Preview", "Reducer", "Route", "Screen", "StoreFactory", "UiMapper", "UiModel", "ViewModel")

        scope.files
            .withPackage("..presentation.screen..")
            .filter { file -> !file.hasPackage("..component..") }
            .filter { file -> allowedSuffixes.none { suffix -> file.name.endsWith(suffix) } }
            .assertEmpty()
    }

    @Test
    fun `screen folders must contain Route and Screen`() {
        val screenPackages =
            scope.files
                .withPackage("..presentation.screen..")
                .filter { file -> !file.hasPackage("..component..") }
                .groupBy { it.packagee?.name }

        screenPackages.forEach { (packageName, files) ->
            val names = files.map { it.name }.toSet()
            assert(names.any { it.endsWith("Route") }) { "Missing Route in $packageName" }
            assert(names.any { it.endsWith("Screen") }) { "Missing Screen in $packageName" }
        }
    }

    @Test
    fun `stateful screen files must appear as a complete set`() {
        val statefulFiles = setOf("Contract", "StoreFactory", "ViewModel")

        val screenPackages =
            scope.files
                .withPackage("..presentation.screen..")
                .filter { file -> statefulFiles.any { suffix -> file.name.endsWith(suffix) } }
                .groupBy { it.packagee?.name }

        screenPackages.forEach { (packageName, files) ->
            val presentSuffixes =
                files.map { file ->
                    statefulFiles.first { suffix -> file.name.endsWith(suffix) }
                }.toSet()

            assert(presentSuffixes == statefulFiles) {
                "Incomplete stateful set in $packageName: found $presentSuffixes, expected $statefulFiles"
            }
        }
    }

    // endregion

    // region Route file rules

    @Test // ok
    fun `Route files must contain exactly one declaration and it must be a public function`() {
        scope.files
            .withNameEndingWith("Route")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val declarations = file.functions() + file.classes() + file.interfaces() + file.objects()
                declarations.size == 1 && file.functions().single().hasPublicOrDefaultModifier
            }
    }

    @Test // ok
    fun `Route files must contain a function matching the file name`() {
        scope.files
            .withNameEndingWith("Route")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions().any { it.name == file.name }
            }
    }

    @Test // ok
    fun `Route public function must be annotated with Composable`() {
        scope.files
            .withNameEndingWith("Route")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions().single { it.hasPublicOrDefaultModifier }.hasAnnotationWithName("Composable")
            }
    }

    @Test // ok
    fun `Route function parameters must only be lambdas, Modifier, or ViewModel`() {
        scope.files
            .withNameEndingWith("Route")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions()
                    .filter { it.hasPublicOrDefaultModifier }
                    .all { function ->
                        function.parameters.all { param ->
                            param.type.name == "Modifier" ||
                                param.type.name.endsWith("ViewModel") ||
                                param.type.isFunctionType
                        }
                    }
            }
    }

    @Test // ok
    fun `Route function ViewModel parameter must have a default value`() {
        scope.files
            .withNameEndingWith("Route")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions()
                    .filter { it.hasPublicOrDefaultModifier }
                    .all { function ->
                        val viewModelParam = function.parameters.firstOrNull { it.type.name.endsWith("ViewModel") }
                        viewModelParam?.hasDefaultValue() ?: true
                    }
            }
    }

    // endregion

    // region ViewModel file rules

    @Test // ok
    fun `ViewModel files must contain exactly one declaration and it must be a public class`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val declarations = file.classes() + file.interfaces() + file.objects() + file.functions().filter { it.isTopLevel }
                declarations.size == 1 && file.classes().single().hasPublicOrDefaultModifier
            }
    }

    @Test // ok
    fun `ViewModel classes must extend ViewModel`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.classes().single().hasParentWithName("ViewModel")
            }
    }

    @Test // ok
    fun `ViewModel classes must have a StoreFactory constructor parameter`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.classes().single().primaryConstructor?.parameters
                    ?.any { it.type.name.endsWith("StoreFactory") } ?: false
            }
    }

    @Test // ok
    fun `ViewModel state property must be a StateFlow of the matching State or UiModel type`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val className = file.classes().single().name
                val prefix = className.removeSuffix("ViewModel")
                val stateProperty =
                    file.classes().single()
                        .properties().firstOrNull { it.name == "state" }
                stateProperty == null ||
                    stateProperty.type?.name == "StateFlow<${prefix}State>" ||
                    stateProperty.type?.name == "StateFlow<${prefix}UiModel>"
            }
    }

    @Test // ok
    fun `ViewModel labels property must be a Flow of the matching Label type`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val className = file.classes().single().name
                val prefix = className.removeSuffix("ViewModel")
                val labelsProperty =
                    file.classes().single()
                        .properties().firstOrNull { it.name == "labels" }
                labelsProperty == null || labelsProperty.type?.name == "Flow<${prefix}Label>"
            }
    }

    // endregion

    // region Contract file rules

    @Test
    fun `Contract files must contain an Intent sealed interface`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.interfaces().any { it.name == "${prefix}Intent" && it.hasSealedModifier }
            }
    }

    @Test // ok
    fun `Contract files must contain a Label sealed interface`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.interfaces().any { it.name == "${prefix}Label" && it.hasSealedModifier }
            }
    }

    @Test
    fun `Contract files must contain an Action sealed interface`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.interfaces().any { it.name == "${prefix}Action" && it.hasSealedModifier }
            }
    }

    @Test // ok
    fun `Contract files must contain a Message sealed interface`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.interfaces().any { it.name == "${prefix}Message" && it.hasSealedModifier }
            }
    }

    @Test // ok
    fun `Contract files must contain a State data class`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.classes().any { it.name == "${prefix}State" && it.hasDataModifier }
            }
    }

    @Test // ok
    fun `Ui model classes in Contract files must be data classes`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.classes()
                    .filter { it.name.endsWith("Ui") }
                    .all { it.hasDataModifier }
            }
    }

    @Test // ok
    fun `top-level classes in Contract files must be State or Ui models`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.classes()
                    .filter { it.isTopLevel }
                    .all { cls ->
                        cls.name == "${prefix}State" || cls.name.endsWith("Ui")
                    }
            }
    }

    @Test // ok
    fun `top-level interfaces in Contract files must be the four MVI sealed interfaces`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                val allowedNames =
                    setOf(
                        "${prefix}Intent",
                        "${prefix}Label",
                        "${prefix}Action",
                        "${prefix}Message",
                    )
                file.interfaces()
                    .filter { it.isTopLevel }
                    .all { it.name in allowedNames }
            }
    }

    // endregion

    // region Screen file rules

    @Test // ok
    fun `Screen files must contain exactly one public function`() {
        scope.files
            .withNameEndingWith("Screen")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val topLevelDeclarations = file.classes() + file.interfaces() + file.objects()
                val publicFunctions =
                    file.functions()
                        .filter { it.isTopLevel && it.hasPublicOrDefaultModifier }
                topLevelDeclarations.isEmpty() && publicFunctions.size == 1
            }
    }

    @Test // ok
    fun `Screen public function must match the file name`() {
        scope.files
            .withNameEndingWith("Screen")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions()
                    .single { it.isTopLevel && it.hasPublicOrDefaultModifier }
                    .name == file.name
            }
    }

    @Test // ok
    fun `Screen public function must be annotated with Composable`() {
        scope.files
            .withNameEndingWith("Screen")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions()
                    .single { it.isTopLevel && it.hasPublicOrDefaultModifier }
                    .hasAnnotationWithName("Composable")
            }
    }

    @Test // ok
    fun `Screen public function parameters must only be Modifier, matching State or UiModel, or lambdas`() {
        scope.files
            .withNameEndingWith("Screen")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Screen")
                file.functions()
                    .single { it.isTopLevel && it.hasPublicOrDefaultModifier }
                    .parameters.all { param ->
                        param.type.name == "Modifier" ||
                            param.type.name == "${prefix}State" ||
                            param.type.name == "${prefix}UiModel" ||
                            param.type.isFunctionType
                    }
            }
    }

    @Test // ok
    fun `Screen State or UiModel parameter must not have a default value`() {
        scope.files
            .withNameEndingWith("Screen")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Screen")
                val stateParam =
                    file.functions()
                        .single { it.isTopLevel && it.hasPublicOrDefaultModifier }
                        .parameters.firstOrNull {
                            it.type.name == "${prefix}State" || it.type.name == "${prefix}UiModel"
                        }
                stateParam == null || !stateParam.hasDefaultValue()
            }
    }

    // endregion

    // region Dependency boundaries

    @Test
    fun `presentation files must not import from domain except StoreFactory, ViewModel, and Contract`() {
        scope.files
            .withPackage("..presentation..")
            .filter { file ->
                !file.name.endsWith("StoreFactory") &&
                    !file.name.endsWith("ViewModel") &&
                    !file.name.endsWith("Contract")
            }
            .filter { file ->
                file.imports.any { it.name.contains(".domain.") }
            }
            .assertEmpty()
    }

    @Test // ok
    fun `presentation layer must not import from data layer`() {
        scope.files
            .withPackage("..presentation..")
            .assertTrue { !it.hasImportWithName("..data..") }
    }

    // endregion
}
