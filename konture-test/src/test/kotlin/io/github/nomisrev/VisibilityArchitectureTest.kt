package io.github.nomisrev

import io.github.baole.konture.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class VisibilityArchitectureTest {

    @Test
    fun `persistence adapters should remain strictly internal`() {
        // Direct database queries must stay hidden within internal implementation files
        Konture.classes()
            .that().haveNameEndingWith("Persistence")
            .should().beInternal()
            .check()
    }
}
