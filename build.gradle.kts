import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdea("2025.2.6.2")

        // PSI for .json files (JsonFile/JsonProperty/JsonStringLiteral) --
        // this plugin builds go-to-definition on top of the bundled JSON
        // plugin's PSI, it does not implement its own JSON lexer/parser.
        bundledPlugin("com.intellij.modules.json")

        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            // 243 = 2024.3, so as not to exclude the real installed base.
            sinceBuild = "243"
            untilBuild = provider { null }
        }
    }

    // Same tooling bug as every other Gap Hunter Labs plugin (Gradle 9.5 +
    // IntelliJ Platform Gradle Plugin 2.16 + IDE 2025.2.6.2): the
    // bytecode instrumenter fails with "instrumentIdeaExtensions
    // doesn't support the nested element". Not required for
    // build/test/verifyPlugin.
    instrumentCode = false

    // Catch experimental/internal API usage locally, before Marketplace's
    // own verifier flags it post-upload. Never relax this list without a
    // documented exception (see AUTOMATION_PLAYBOOK.md SS1.5).
    pluginVerification {
        failureLevel = listOf(
            VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS,
            VerifyPluginTask.FailureLevel.INTERNAL_API_USAGES,
            VerifyPluginTask.FailureLevel.OVERRIDE_ONLY_API_USAGES,
            VerifyPluginTask.FailureLevel.EXPERIMENTAL_API_USAGES,
            VerifyPluginTask.FailureLevel.SCHEDULED_FOR_REMOVAL_API_USAGES,
        )
    }
}
