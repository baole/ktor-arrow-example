package testreport

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.kotlin.dsl.withType

/**
 * Wires every `Test` task with a listener that prints the full name of each failing test as soon
 * as the task's root suite finishes. Gradle's own `-q` summary ("15 tests completed, 3 failed")
 * doesn't say which tests failed, so this fills that gap without needing `--info`/`--scan` or a
 * separate report-parsing step.
 *
 * Follow up with `./gradlew inspectTest --name "<test name>"` to see the stack trace.
 */
internal object TestFailureListener {
  /** Wires every `Test` task in the whole build, not just [project], so it also covers subprojects. */
  fun applyTo(project: Project) {
    project.rootProject.allprojects { tasks.withType<Test>().configureEach { registerOn(this) } }
  }

  private fun registerOn(task: Test) {
    val failures = mutableListOf<String>()

    task.addTestListener(
      object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) = Unit

        override fun beforeTest(testDescriptor: TestDescriptor) = Unit

        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
          if (result.resultType == TestResult.ResultType.FAILURE) {
            val className = testDescriptor.className
            failures += if (className.isNullOrBlank()) {
              testDescriptor.name
            } else {
              "$className > ${testDescriptor.name}"
            }
          }
        }

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
          // The root suite has no parent; report once per task, not per nested suite.
          if (suite.parent != null || failures.isEmpty()) return

          task.logger.quiet("\nFailed tests in ${task.path}:")
          failures.sorted().distinct().forEach { task.logger.quiet("  - $it") }
          task.logger.quiet(
            "\nRun ./gradlew -q inspectTest --name \"<test name>\" to see the stack trace.",
          )
        }
      },
    )
  }
}
