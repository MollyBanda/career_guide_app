# Pathfinder Android - Lab Test Run Sheet

**Tester:** Molly Banda  
**Student ID:** BSC-COM-34-23  
**App:** Mobile Career Guide Application  
**Platform:** Android phone/emulator (record device + Android version below)  
**Planned run:** 26 October 2026  
**Build/version:** __________  **Device/emulator:** __________  **Android version:** __________

## How to run the lab test

1. Install and launch the same app build you will demonstrate. Record the build and device details above.
2. For a clean account, use a unique email you control or the supplied test email if it is not already registered. Do not use a real password you use elsewhere.
3. Run the test steps in the supplied test-case PDF. This sheet tracks results; it does not replace the detailed steps and prerequisites in that PDF.
4. Mark Pass only when the observed result matches the expected result; mark Fail when it does not. Mark Blocked when setup or a missing service prevents the test. Do not mark a case Pass before running it.
5. For every Fail, record the case ID, exact steps, expected vs actual result, and a screenshot or short screen recording if allowed. Then continue with the next case.
6. Re-run failed cases after a fix and record the re-test result separately.

**Useful test data from the supplied cases:** Chikondi Phiri; chikondi@example.com; Career123 (test registration); WrongPass (incorrect-login case); unknown@example.com; keyword engineer; no-match keyword xyzqwerty; scholarship Commonwealth; career Software Developer; institution University of Malawi; programme BSc Computer Science. Use fake/sample data only.

**App-specific note:** The current prototype requires the Terms of Service checkbox during registration. Tick it for successful registration. The app uses a local demo account for this build, not a production authentication server.

## Test execution log

Use P/F/B for Pass/Fail/Blocked. Write the actual observed result and any defect number in the last two columns. All rows are intentionally Not run until executed in the lab.

| ID | Test case | Type / priority | Test data / key action | Expected result (summary) | Result | Actual result / defect ID |
|---|---|---|---|---|---|---|
| CG_REG_001 | Successful registration | Positive / High | Chikondi Phiri; chikondi@example.com; Career123; confirm Career123 | Account created; Home or Login shown; account details retained | Not run | |
| CG_REG_002 | Already registered email | Negative / High | Register chikondi@example.com a second time | “Email already registered”; remain on Create Account; no duplicate | Not run | |
| CG_REG_003 | Empty registration fields | Negative / Medium | Leave required fields empty and submit | “Please fill all fields”; no account created | Not run | |
| CG_REG_004 | Invalid email format | Negative / Medium | chikondi.example.com; other fields valid | “Enter a valid email address”; stay on registration | Not run | |
| CG_REG_005 | Password below minimum | Boundary / Medium | Ab123 in password and confirmation | “Password must be at least 6 characters”; no account | Not run | |
| CG_REG_006 | Passwords do not match | Negative / Medium | Career123 / Career321 | “Passwords do not match”; stay on registration | Not run | |
| CG_LOGIN_001 | Successful login | Positive / High | Registered email + Career123 | Home opens; registered user's name is shown | Not run | |
| CG_LOGIN_002 | Incorrect password | Negative / High | Registered email + WrongPass | “Invalid email or password”; no session | Not run | |
| CG_LOGIN_003 | Unregistered email | Negative / High | unknown@example.com + any password | “Invalid email or password”; no session | Not run | |
| CG_PROFILE_001 | View profile | Positive / Medium | Log in, open Profile | Profile shows registered name and email | Not run | |
| CG_PROFILE_002 | Update profile name | Positive / Medium | Change to Chikondi Banda; Save | Success message; new name displayed | Not run | |
| CG_PROFILE_003 | Save profile with empty name | Negative / Medium | Clear name; Save | “Please fill all fields”; original name remains | Not run | |
| CG_CAREER_001 | View career information | Positive / High | Careers → Software Developer | Detail includes overview, qualifications, skills, courses, pathways | Not run | |
| CG_CAREER_002 | View careers offline | Negative / Medium | Disable Wi-Fi/mobile data; open Careers | Per source case: connection message; app must not crash | Not run | |
| CG_SEARCH_001 | Search careers by keyword | Positive / High | Search engineer | Matching engineer roles appear | Not run | |
| CG_SEARCH_002 | Filter careers by category | Positive / Medium | Select Health filter | Only Health-category careers appear | Not run | |
| CG_SEARCH_003 | Search with no match | Negative / Medium | xyzqwerty | “No careers found”; app does not crash | Not run | |
| CG_ASSESS_001 | Complete self-assessment | Positive / High | Answer every question; submit | Responses accepted; career recommendations shown | Not run | |
| CG_ASSESS_002 | Submit incomplete assessment | Negative / High | Leave one question unanswered; submit | “Please answer all questions”; remain on assessment | Not run | |
| CG_RECOM_001 | View recommendations after assessment | Positive / High | Complete science/technology-oriented answers | Relevant recommendations appear and open details | Not run | |
| CG_RECOM_002 | Recommendations before assessment | Negative / Medium | New account, no assessment | Prompt to take assessment; button opens it | Not run | |
| CG_UNI_001 | View universities and colleges | Positive / High | Universities → University of Malawi | Institution details and programmes displayed | Not run | |
| CG_UNI_002 | View institutions offline | Negative / Medium | Disable Wi-Fi/mobile data; open Universities | Per source case: connection message; app must not crash | Not run | |
| CG_ENTRY_001 | View programme entry requirements | Positive / High | University of Malawi → BSc Computer Science | Entry requirements and required subjects displayed | Not run | |
| CG_ENTRY_002 | Entry requirements unavailable | Negative / Low | Open a programme with no requirements data | “Entry requirements not available”; no crash | Not run | |
| CG_SCHSEARCH_001 | Search scholarships by criteria | Positive / High | Search Commonwealth | Matching Commonwealth scholarship appears | Not run | |
| CG_SCHSEARCH_002 | Search scholarships with no match | Negative / Medium | Search zzzzzz | “No scholarships found”; no crash | Not run | |
| CG_SCHDET_001 | View scholarship details | Positive / High | Open Commonwealth Master’s Scholarship | Eligibility, deadline/cycle, and application method shown | Not run | |
| CG_SCHDET_002 | Scholarship missing details | Negative / Low | Open record without a deadline | Missing data says “Not available”; no crash | Not run | |
| CG_TRAIN_001 | View training opportunity | Positive / Medium | Training → open a programme | Description, duration, and provider displayed | Not run | |
| CG_TRAIN_002 | No training available | Negative / Low | Test empty training catalogue | “No training opportunities available”; no crash | Not run | |
| CG_INTERN_001 | View internship opportunity | Positive / Medium | Internships → open a placement | Description and application requirements shown | Not run | |
| CG_INTERN_002 | No internships available | Negative / Low | Test empty internship catalogue | “No internships available”; no crash | Not run | |
| CG_SAVE_001 | Save a career | Positive / Medium | Open Software Developer; tap bookmark | Saved state appears; item appears in Saved | Not run | |
| CG_SAVE_002 | Tap bookmark on an already-saved item | Negative / Low | Tap bookmark again | Item toggles off or remains single; no duplicate | Not run | |
| CG_SAVE_003 | View saved items | Positive / Medium | Save a career and scholarship; open Saved | Both items listed; tapping opens details | Not run | |
| CG_LOGOUT_001 | Successful logout | Positive / High | Profile → Log out | User returns to Login; session ends | Not run | |
| CG_LOGOUT_002 | Back button after logout | Negative / High | Log out, press Android Back | Protected Home screen does not reappear | Not run | |
| CG_UI_001 | Responsive layout | Usability / Medium | Open Welcome, Login, Home, Careers, Details, Profile on available screen sizes; scroll | Text, fields, buttons, and bottom navigation remain visible and tappable; no clipping/crash | Not run | |
| CG_NAV_001 | Navigation and save toggling | Functional / Medium | Open a career from search; save/unsave; use Back and bottom tabs | Correct detail opens; save state updates; Back returns to prior page | Not run | |

## Defect notes

| Defect ID | Test case ID | Steps to reproduce | Expected | Actual | Severity | Screenshot/file |
|---|---|---|---|---|---|---|
| | | | | | | |
| | | | | | | |
| | | | | | | |

## End-of-session summary

**Passed:** ____  **Failed:** ____  **Blocked:** ____  **Not run:** ____  
**Most serious defect:** ______________________________  
**Retest needed:** ______________________________
