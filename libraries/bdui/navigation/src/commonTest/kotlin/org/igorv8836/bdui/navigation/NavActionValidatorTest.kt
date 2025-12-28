package org.igorv8836.bdui.navigation

import org.igorv8836.bdui.navigation.validation.NavActionValidationConfig
import org.igorv8836.bdui.navigation.validation.NavActionValidator
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class NavActionValidatorTest {

    @Test
    fun acceptsAllowedSchemeAndLength() {
        val validator = NavActionValidator(
            NavActionValidationConfig(
                allowedSchemes = setOf("app", "https"),
                maxPathLength = 32,
            ),
        )

        val result = validator.validateForwardPath("https://example.com/home")

        assertTrue(result.isSuccess)
    }

    @Test
    fun rejectsUnknownScheme() {
        val validator = NavActionValidator(
            NavActionValidationConfig(allowedSchemes = setOf("https")),
        )

        val result = validator.validateForwardPath("custom://route")

        assertTrue(result.isFailure)
    }

    @Test
    fun rejectsTooLongPath() {
        val validator = NavActionValidator(
            NavActionValidationConfig(maxPathLength = 5),
        )
        val longPath = "https://very-long-path"

        val result = validator.validateForwardPath(longPath)

        if (result.isSuccess) fail("Expected failure for long path")
    }
}
