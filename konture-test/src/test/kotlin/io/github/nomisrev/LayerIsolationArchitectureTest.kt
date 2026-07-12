package io.github.nomisrev

import io.github.baole.konture.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class LayerIsolationArchitectureTest {

    @Test
    fun `routes should not bypass services`() {
        // Strict Layer Isolation: route files must not reference DB persistence adapters or SQLDelight generated APIs directly
        Konture.files()
            .that { declaration.name.endsWith("Routes.kt") }
            .should {
                val forbiddenImports = imports.filter {
                    it.endsWith("Persistence") ||
                        it.startsWith("io.github.nomisrev.sqldelight")
                }

                if (forbiddenImports.isNotEmpty()) {
                    addViolation(
                        "Route file $filePath bypasses the service or persistence boundary: " +
                            forbiddenImports.joinToString(),
                    )
                }
            }
            .check()
    }
}
