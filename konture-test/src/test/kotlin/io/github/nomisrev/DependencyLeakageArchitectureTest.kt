package io.github.nomisrev

import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class DependencyLeakageArchitectureTest {

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
        Konture.functions()
            .that()
            .satisfy { it.filePath.endsWith("Routes.kt") }
            .should()
            .satisfy { func ->
                val signatures = listOf(func.declaration.returnType) + func.declaration.parameters.map { it.type }
                signatures.none { it.contains("io.github.nomisrev.sqldelight") }
            }
            .check()
    }
}
