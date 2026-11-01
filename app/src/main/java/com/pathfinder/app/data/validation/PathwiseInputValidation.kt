package com.pathfinder.app.data.validation

/** Shared, side-effect-free validation used by the forms and local unit tests. */
object PathwiseInputValidation {
    val registrationRoles = listOf("Student", "Counsellor", "Admin")

    private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun registrationError(
        name: String,
        email: String,
        password: String,
        confirmation: String,
        termsAccepted: Boolean
    ): String? = when {
        name.isBlank() || email.isBlank() || password.isBlank() || confirmation.isBlank() ->
            "Please fill all fields."
        !isValidEmail(email) -> "Enter a valid email address."
        password.length < 6 -> "Password must be at least 6 characters."
        password != confirmation -> "Passwords do not match."
        !termsAccepted -> "Please agree to the Terms of Service and Privacy Policy."
        else -> null
    }

    fun loginError(email: String, password: String): String? = when {
        !isValidEmail(email) || password.isBlank() -> "Enter a valid email address and password."
        else -> null
    }

    fun profileNameError(name: String): String? =
        if (name.isBlank()) "Please fill all fields." else null

    fun isValidEmail(email: String): Boolean = emailPattern.matches(email.trim())

    fun hasCompleteAssessment(answers: List<String>, questionCount: Int = 5): Boolean =
        answers.size == questionCount && answers.all(String::isNotBlank)
}
