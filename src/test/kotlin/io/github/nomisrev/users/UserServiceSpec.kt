package io.github.nomisrev.users

import arrow.core.nonEmptyListOf
import de.infix.testBalloon.framework.core.testSuite
import io.github.nomisrev.EmailAlreadyExists
import io.github.nomisrev.EmptyUpdate
import io.github.nomisrev.InvalidEmail
import io.github.nomisrev.InvalidPassword
import io.github.nomisrev.InvalidUsername
import io.github.nomisrev.PasswordNotMatched
import io.github.nomisrev.UsernameAlreadyExists
import io.github.nomisrev.assertRaised
import io.github.nomisrev.dependencies
import io.github.nomisrev.incorrectInput
import io.github.nomisrev.registerUser
import io.github.nomisrev.testDependencies
import io.github.nomisrev.userFixture
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

@Suppress("RETURN_VALUE_NOT_USED_COERCION")
val UserServiceSuite by testSuite {
    val validPw = "123456789"

    testDependencies("username cannot be empty") {
        val validEmail = "valid@domain.com"
        val errors = nonEmptyListOf("Cannot be blank", "is too short (minimum is 1 characters)")
        assertRaised {
            dependencies.userService.register(RegisterUser("", validEmail, validPw))
        } shouldBe incorrectInput(InvalidUsername(errors))
    }

    testDependencies("username longer than 25 chars") {
        val validEmail = "valid@domain.com"
        val name = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val errors = nonEmptyListOf("is too long (maximum is 25 characters)")
        assertRaised {
            dependencies.userService.register(RegisterUser(name, validEmail, validPw))
        } shouldBe incorrectInput(InvalidUsername(errors))
    }

    testDependencies("email cannot be empty") {
        val validUsername = userFixture().username
        val errors = nonEmptyListOf("Cannot be blank", "'' is invalid email")
        assertRaised {
            dependencies.userService.register(RegisterUser(validUsername, "", validPw))
        } shouldBe incorrectInput(InvalidEmail(errors))
    }

    testDependencies("email too long") {
        val validUsername = userFixture().username
        val email = "${(0..340).joinToString("") { "A" }}@domain.com"
        val errors = nonEmptyListOf("is too long (maximum is 350 characters)")
        assertRaised {
            dependencies.userService.register(RegisterUser(validUsername, email, validPw))
        } shouldBe incorrectInput(InvalidEmail(errors))
    }

    testDependencies("email is not valid") {
        val validUsername = userFixture().username
        val email = "AAAA"
        val errors = nonEmptyListOf("'$email' is invalid email")
        assertRaised {
            dependencies.userService.register(RegisterUser(validUsername, email, validPw))
        } shouldBe incorrectInput(InvalidEmail(errors))
    }

    testDependencies("password cannot be empty") {
        val validUsername = userFixture().username
        val validEmail = "valid@domain.com"
        val errors = nonEmptyListOf("Cannot be blank", "is too short (minimum is 8 characters)")
        assertRaised {
            dependencies.userService.register(RegisterUser(validUsername, validEmail, ""))
        } shouldBe incorrectInput(InvalidPassword(errors))
    }

    testDependencies("password can be max 100") {
        val validUsername = userFixture().username
        val validEmail = "valid@domain.com"
        val password = (0..100).joinToString("") { "A" }
        val errors = nonEmptyListOf("is too long (maximum is 100 characters)")
        assertRaised {
            dependencies.userService.register(RegisterUser(validUsername, validEmail, password))
        } shouldBe incorrectInput(InvalidPassword(errors))
    }

    testDependencies("all valid returns a token") {
        val user = userFixture(password = validPw)
        dependencies.userService.register(RegisterUser(user.username, user.email, user.password))
    }

    testDependencies("register with duplicate username results in") {
        val first = userFixture(password = validPw)
        val second = userFixture(password = validPw)
        dependencies.userService.register(RegisterUser(first.username, first.email, first.password))

        assertRaised {
            dependencies.userService.register(
                RegisterUser(first.username, second.email, second.password)
            )
        } shouldBe UsernameAlreadyExists(first.username)
    }

    testDependencies("register with duplicate email results in") {
        val first = userFixture(password = validPw)
        val second = userFixture(password = validPw)
        dependencies.userService.register(RegisterUser(first.username, first.email, first.password))

        assertRaised {
            dependencies.userService.register(
                RegisterUser(second.username, first.email, second.password)
            )
        } shouldBe EmailAlreadyExists(first.email)
    }

    testDependencies("email cannot be empty on login") {
        val errors = nonEmptyListOf("Cannot be blank", "'' is invalid email")
        assertRaised {
            dependencies.userService.login(Login("", validPw))
        } shouldBe incorrectInput(InvalidEmail(errors))
    }

    testDependencies("email too long on login") {
        val email = "${(0..340).joinToString("") { "A" }}@domain.com"
        val errors = nonEmptyListOf("is too long (maximum is 350 characters)")
        assertRaised {
            dependencies.userService.login(Login(email, validPw))
        } shouldBe incorrectInput(InvalidEmail(errors))
    }

    testDependencies("email is not valid on login") {
        val email = "AAAA"
        val errors = nonEmptyListOf("'$email' is invalid email")
        assertRaised {
            dependencies.userService.login(Login(email, validPw))
        } shouldBe incorrectInput(InvalidEmail(errors))
    }

    testDependencies("password cannot be empty on login") {
        val validEmail = "valid@domain.com"
        val errors = nonEmptyListOf("Cannot be blank", "is too short (minimum is 8 characters)")
        assertRaised {
            dependencies.userService.login(Login(validEmail, ""))
        } shouldBe incorrectInput(InvalidPassword(errors))
    }

    testDependencies("password can be max 100 on login") {
        val validEmail = "valid@domain.com"
        val password = (0..100).joinToString("") { "A" }
        val errors = nonEmptyListOf("is too long (maximum is 100 characters)")
        assertRaised {
            dependencies.userService.login(Login(validEmail, password))
        } shouldBe incorrectInput(InvalidPassword(errors))
    }

    testDependencies("all valid login returns a token") {
        val user = userFixture(password = validPw)
        dependencies.userService.register(RegisterUser(user.username, user.email, user.password))
        val (token) = dependencies.userService.login(Login(user.email, user.password))
        token.value.shouldNotBeBlank()
    }

    testDependencies("update with all null") {
        val (userId) = registerUser(userFixture(password = validPw))
        assertRaised {
            dependencies.userService.update(Update(userId, null, null, null, null, null))
        } shouldBe EmptyUpdate("Cannot update user with $userId with only null values")
    }

    testDependencies("update password rotates credentials and keeps public profile data") {
        val (user, userId) = registerUser(userFixture(password = validPw))
        val newPassword = "987654321"

        val updated =
            dependencies.userService.update(Update(userId, null, null, newPassword, null, null))

        assert(updated.email == user.email)
        assert(updated.username == user.username)
        assert(updated.bio == "")
        assert(updated.image == "")

        assertRaised {
            dependencies.userService.login(Login(user.email, user.password))
        } shouldBe PasswordNotMatched

        dependencies.userService.login(Login(user.email, newPassword))
    }
}
