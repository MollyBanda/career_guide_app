# Pathwise tests in Android Studio

## Where the tests are

The test folders use the same two-source-set layout shown in the supplied Android Studio screenshots. In **Android** view expand **app → kotlin+java → com.pathfinder.app (androidTest)** for device/emulator UI tests and **com.pathfinder.app (test)** for local JVM unit tests. In **Project** view, the same files are under `app/src/androidTest/java/com/pathfinder/app` and `app/src/test/java/com/pathfinder/app`.

Recorded baseline from an earlier project revision: **50 instrumentation tests, 0 failures, 1 skipped** and **20 JVM unit tests passed**. The current catalogue changes add tests that have not yet executed. The latest attempt could not start Gradle because Windows denied access to the Gradle `native-platform.dll` lock under the user Gradle cache. The baseline therefore does not certify this revision or live Firestore persistence.

### Instrumented UI tests (`androidTest`)

- `ExampleInstrumentedTest.kt` — Android instrumentation/package smoke check.
- `WelcomeScreenTest.kt` — welcome screen content and actions.
- `HomeScreenUiTest.kt` — home categories, career card, and responsive **See all** navigation.
- `SignUpUiTest.kt` — Counsellor/Admin role selection and required consent.
- `LoginScreenUiTest.kt` — selected login role, navigation, and password-reset callback.
- `AuthFallbackJourneyTest.kt` — full signup → logout → login flow when Firebase setup is unavailable.
- `AuthScreensTest.kt` — original PDF registration/login cases and form feedback.
- `BrowseAndCareerTests.kt` — career details/search, assessment/recommendations, and saved items.
- `EducationOpportunityTests.kt` — universities, programme requirements, scholarships, training, and internships.
- `UniversityProgrammeNavigationTest.kt` — university list → institution profile → only that institution's programme → programme details.
- `ProfileScreenTests.kt` — profile display/edit validation and logout action.
- `LogoutBackNavigationTest.kt` — creates or resumes a test account, then checks logout and Android Back navigation.
- `ScenarioCatalogue.kt` — isolated catalogue fixtures used by screen tests; it is test data, not a test class.

### Local JVM tests (`test`)

- `AdminRoleTests.kt` — supported account role choices.
- `AppUnitTest.kt` — bundled catalogue identity and uniqueness.
- `AuthInputValidationTests.kt` — registration, email, password, consent, and login validation.
- `CareerCatalogueTests.kt` — career detail data and the Software Developer scenario.
- `AssessmentValidationTests.kt` — exactly five nonblank answers are required.
- `UniversityCatalogueTests.kt` — university and programme requirement data.
- `ExpandedCatalogueTests.kt` — requested catalogue counts, source dates, and application-cycle status.
- `ScholarshipCatalogueTests.kt` — provider, eligibility, deadline, and application source data.
- `TrainingInternshipCatalogueTests.kt` — training/internship content and requirements.
- `ProfileValidationTests.kt` — profile name validation.
- `SavedItemsModelTests.kt` — saved-item identity is scoped to user and opportunity.

Use the green run icon beside any test class or method. Run the complete local unit suite with `:app:testDebugUnitTest`; run the device/emulator suite with `:app:connectedDebugAndroidTest` after selecting a connected device or emulator. The project does not include `gradlew.bat`, so these tasks are also available from Android Studio's Gradle tool window.

## Traceability to the submitted PDF

The 36 cases actually present in the PDF retain their original IDs in the instrumented tests. PDF case numbers **16** and **35** are absent in the source document; no replacement cases have been invented. The original login case uses `Career`, while registration case 1 uses `Career123`; this credential inconsistency is retained and documented in the execution CSV rather than silently corrected.

Some requirements differ from the app's current behavior. For example, career/university catalogues are seeded locally and remain available offline, the assessment shows its result in-place, the Home screen shows a preset career match before assessment, and blank opportunity sections are omitted instead of showing a “Not available” placeholder. Comments beside the corresponding tests and the execution report call these out. A screen/component test is not evidence of Firebase account creation, Firestore persistence, or the full signed-in journey.

## Device launch evidence

Run `testing/automation/run-device-smoke.ps1` from the project root in PowerShell to launch the already-installed app, inspect its activity and UI hierarchy, and save evidence under `testing/evidence/`. It does not install the debug APK or clear app data. A physical-device end-to-end scenario requires USB debugging input and install/update access to be allowed on that device.
