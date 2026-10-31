# Pathfinder Android

A native Android app built with Kotlin and Jetpack Compose. The welcome, sign-in, and account creation screens lead into a Home dashboard and a Saved page. The bottom navigation switches between Home and Saved.

## Open in Android Studio

1. In Android Studio, choose **Open**.
2. Select this **PathfinderAndroid** folder (the folder containing `settings.gradle.kts`).
3. Allow Gradle sync to finish, then choose an emulator or connected Android device and press **Run**.

Use Android Studio's Gradle JDK 17. The project targets Android 15 (API 35) and compiles with API 36.

## Scope

The screens include the screenshot copy, password visibility, password strength feedback, education-level selection, consent and scholarship-alert checkboxes, remember-me, password-reset feedback, biometric sign-in messaging, and Google/Apple buttons. Authentication, social sign-in, password reset, and biometrics are UI entry points and need a backend or identity provider before they can authenticate real users.
