package io.github.nomisrev.articles

import arrow.core.raise.either
import io.github.nomisrev.CannotGenerateSlug
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import de.infix.testBalloon.framework.core.testSuite
import kotlin.random.Random

@Suppress("RETURN_VALUE_NOT_USED_COERCION")
val SlugGeneratorSuite by testSuite {
    val seed = Random(42)

    test("should generate a slug from a title") {
        val slugGenerator = slugifyGenerator(seed)

        val title = "Test Title"
        val slug = either { slugGenerator.generateSlug(title) { true } }.shouldBeRight()

        slug.value shouldContain "test_title"
        slug.value shouldNotContain " "
    }

    test("should add a random suffix when the first attempt is not unique") {
        val slugGenerator = slugifyGenerator(seed)

        val title = "Test Title"

        val slug = either {
            slugGenerator.generateSlug(title) { slug ->
                slug.value == title.lowercase().replace(' ', '_')
            }
        }.shouldBeRight()

        slug.value shouldContain "test_title"
        slug.value shouldContain "_"
    }

    test("should return CannotGenerateSlug when all attempts fail") {
        val slugGenerator = slugifyGenerator(seed, defaultMaxAttempts = 3)

        val title = "Test Title"

        either { slugGenerator.generateSlug(title) { false } }
            .shouldBeLeft(CannotGenerateSlug("Failed to generate unique slug from $title"))
    }

    test("should handle special characters in title") {
        val slugGenerator = slugifyGenerator(seed)

        val title = "Special @#$%^&*() Title"
        val result = either { slugGenerator.generateSlug(title) { true } }

        val slug = result.shouldBeRight()
        slug.value shouldContain "special_title"
        slug.value shouldNotContain "@"
        slug.value shouldNotContain "#"
        slug.value shouldNotContain "$"
    }

    test("should handle empty title") {
        val slugGenerator = slugifyGenerator(seed)

        either { slugGenerator.generateSlug("") { true } }.shouldBeRight(Slug(""))
    }

    test("should handle very long title") {
        val slugGenerator = slugifyGenerator(seed)

        val title = "Very Long Title " + "x".repeat(200)
        val result = either { slugGenerator.generateSlug(title) { true } }

        val slug = result.shouldBeRight()
        slug.value shouldContain "very_long_title"
    }
}
