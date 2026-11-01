# Pathfinder Android

A native Android app built with Kotlin and Jetpack Compose. The welcome, sign-in, and account creation screens lead into a Home dashboard and a Saved page. The bottom navigation switches between Home and Saved.

## Open in Android Studio

1. In Android Studio, choose **Open**.
2. Select this **PathfinderAndroid** folder (the folder containing `settings.gradle.kts`).
3. Allow Gradle sync to finish, then choose an emulator or connected Android device and press **Run**.

## Tests

In Android Studio's **Android** view, open **app → kotlin+java → com.pathfinder.app (androidTest)** for Compose/device tests and **com.pathfinder.app (test)** for JVM unit tests. The source files are in `app/src/androidTest/java/com/pathfinder/app` and `app/src/test/java/com/pathfinder/app`. See [testing/TESTING_INSTRUCTIONS.md](testing/TESTING_INSTRUCTIONS.md) for the class list, run tasks, PDF case traceability, and current results.

Use Android Studio's Gradle JDK 17. The project targets Android 15 (API 35) and compiles with API 36.

## Scope

The app uses Firebase Authentication for email/password accounts and password-reset emails. User profile, saved items, completed assessments, and recommendations persist in Cloud Firestore, with Room providing a local cache for supported legacy data. Registration offers Student, Counsellor, and Admin roles; account-role selection is open for this project prototype and must be restricted before production use.

## Firebase Authentication setup

The app is connected to the Firebase project configured in `app/google-services.json`. In the Firebase Console, enable **Authentication → Sign-in method → Email/Password** for that project. Firebase's default password-reset email template is used by **Forgot password?**; the user must enter their account email first. Email delivery requires internet access and a Firebase account. Older Room-only accounts can continue to sign in with their locally stored password; a successful online login attempts to create their Firebase account for future email resets.

When Firebase Authentication and Firestore are available, registration creates the Firebase account and Firestore profile before reporting cloud success. If Firebase authentication is temporarily unavailable or Firestore denies profile access, the app keeps a salted-password profile in Room and still opens the app; it clearly reports that cloud sync is pending. Cloud-only saves, assessment history, and profile updates require Firebase access and the deployed rules. Google/Apple and biometric buttons remain presentation-only entry points.

## Cloud Firestore records and security

Private data uses these UID-scoped paths:

- `users/{uid}` — full name, email, selected app role, and created/updated timestamps. Passwords and password hashes are never sent to Firestore.
- `users/{uid}/savedItems/{itemId}` — saved content ID, title, category, and saved timestamp. Removing a bookmark deletes only this document.
- `users/{uid}/assessments/{assessmentId}` — completed five-answer assessment, result summary, recommended career paths, and completion timestamp.
- `users/{uid}/recommendations/{assessmentId}` — career paths produced with the completed assessment.

The app retrieves profile data after Firebase sign-in, listens to that UID's saved items and latest assessment, writes profile edits and completed assessments to Firestore, and waits for Firestore write confirmation before showing success. Existing Room saved items and latest assessments are copied to missing cloud documents at sign-in without replacing documents already in Firestore. The local Room catalogue remains the source of the shared sample careers, scholarships, universities, training, and internships; users do not upload those catalogue records to Firestore.

Review `firestore.rules` before deploying it in Firebase Console → Firestore Database → Rules. Those rules restrict private paths to their owner and reserve shared-record writes for Firebase Admin SDK custom claims (`admin: true`). The registration form's Admin role is a profile/UI selection only; it does not grant the trusted server-side custom claim or permission to write shared records. Do not grant that claim from client code. Rules have not been deployed automatically. Firestore's Android offline persistence caches records locally and synchronizes confirmed writes when online.
