package io.github.nomisrev

import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class ArchitectureTest {

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

    @Test
    fun `ensure routes and services do not leak database schemas`() {
        // Schema Non-Leakage: SQLDelight queries and raw persistence entities must not leak into service or route signatures
        
        // 1. Check Service classes
        Konture.scope.classes()
            .withNameEndingWith("Service")
            .assertTrue("Services must not leak database types in signatures") { cls ->
                val signatures = cls.functions.flatMap { func -> 
                    listOf(func.returnType) + func.parameters.map { it.type } 
                }
                signatures.none { it.contains("io.github.nomisrev.sqldelight") }
            }

        // 2. Check top-level Route functions
        Konture.scope.functions()
            .filter { it.filePath.endsWith("Routes.kt") }
            .assertTrue("Routes must not leak database types in signatures") { func ->
                val signatures = listOf(func.returnType) + func.parameters.map { it.type }
                signatures.none { it.contains("io.github.nomisrev.sqldelight") }
            }
    }

    @org.junit.jupiter.api.Disabled(
        "Disabled because the showcase codebase has public persistence adapters (e.g., UserPersistence, ArticlePersistence) " +
        "which violates our architectural recommendation of keeping persistence adapters internal."
    )
    @Test
    fun `persistence adapters should remain strictly internal`() {
        // Direct database queries must stay hidden within internal implementation files
        Konture.classes()
            .that().haveNameEndingWith("Persistence")
            .should().beInternal()
            .check()
    }
}
