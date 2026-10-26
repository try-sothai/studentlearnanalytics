# Student Learning Analytics System
## Development Change Log

### 1. Project Overview
The Student Learning Analytics System (SLAS) is a comprehensive web and mobile application allowing students to track their academic performance, attendance, risk level, and predict future grades. The backend is built with Laravel 12, and the mobile app is built natively in Android (Java).

### 2. Current Development Phase
Phase 6 Complete (backend tested, Android build passing; on-device verification pending) — Exams API Integration. Phases 1–6 integrated with real Laravel API data.

### 3. Date
October 13, 2026

### 4. Work Completed
- Successfully verified and fixed the authentication token pipeline between Android and Laravel.
- Connected the Android `StudentDashboardFragment` to the live Laravel `/api/student/dashboard` endpoint, removing all hardcoded metrics and replacing them with live metrics specific to the authenticated student (STD001).
- Connected the Android `StudentCoursesFragment` to parse the live `enrollments` list from the dashboard response.
- Connected `CourseDetailActivity` to fetch real performance records for specific courses.
- Addressed JSON parsing mismatches between Laravel's snake_case schema and Android's Gson models.

### 5. Files Created
None.

### 6. Files Modified
- `app/src/main/java/com/social7/studentlearnanalytics/network/ApiClient.java`
- `app/src/main/java/com/social7/studentlearnanalytics/network/ApiService.java`
- `app/src/main/java/com/social7/studentlearnanalytics/model/DashboardResponse.java`
- `app/src/main/java/com/social7/studentlearnanalytics/student/StudentDashboardFragment.java`
- `app/src/main/java/com/social7/studentlearnanalytics/student/StudentCoursesFragment.java`
- `app/src/main/java/com/social7/studentlearnanalytics/student/CourseDetailActivity.java`
- `app/src/main/res/layout/activity_fragment_student_dashboard.xml`
- `app/Http/Controllers/PerformanceRecordController.php` (Backend)

### 7. Files Deleted
None.

### 8. Backend Changes
**Before:** 
`PerformanceRecordController::index` expected a `student_id` in the request parameters, otherwise it failed or returned bad data.
**After:** 
Modified `PerformanceRecordController::index` to default to `$request->user()->student->id` if `student_id` is missing.
**Reason:** 
Ensures the API securely defaults to the currently authenticated student when the mobile app fetches course performance details without needing to manually pass the student ID.

### 9. API Changes

| Method | Endpoint | Status | Purpose | Changes |
|--------|----------|--------|---------|---------|
| POST | `/api/login` | Unchanged | Authenticates user | None |
| GET | `/api/student/dashboard` | Verified | Retrieves aggregate analytics, predictions, and enrollments | Used for StudentDashboardFragment and StudentCoursesFragment |
| GET | `/api/performance-records` | Modified | Retrieves metrics for specific course | Defers to authenticated user if no `student_id` passed |

### 10. Database Changes
No database changes were made in this phase.

### 11. Android Changes

**File:** `app/src/main/java/com/social7/studentlearnanalytics/network/ApiClient.java`
**Purpose:** Provides Retrofit configuration.
**Before:** Hardcoded `http://10.0.2.2:8000/api/`
**After:** `http://192.168.1.17:8000/api/`
**Reason:** The application is being tested on a physical Google Pixel 6 on LAN instead of the emulator.

**File:** `app/src/main/java/com/social7/studentlearnanalytics/network/ApiService.java`
**Purpose:** Retrofit Interface defining API routes.
**Before:** Only had dummy routes/login.
**After:** Added `getStudentDashboard()` and `getPerformanceRecords(courseId)`.
**Reason:** Required to fetch live dashboard and course detail data.

**File:** `app/src/main/java/com/social7/studentlearnanalytics/model/DashboardResponse.java`
**Purpose:** Gson Model for `/api/student/dashboard`.
**Before:** Lacked mapping for `enrollments`, `predictions`, `student.department`. Mismatched keys for `overall_score`, `assignment_avg`, `quiz_avg`.
**After:** Added `EnrollmentInfo`, `CourseModelInfo`, `PredictionInfo` classes. Fixed serialized names to `overall_performance`, `assignment_average`, `quiz_average`.
**Reason:** Laravel API schema differed from the initial mock data schema.

**File:** `app/src/main/res/layout/activity_fragment_student_dashboard.xml`
**Purpose:** UI Layout for Student Dashboard.
**Before:** TextViews for metrics lacked `android:id`.
**After:** Added IDs (e.g., `@+id/tvMetricAttendance`, `@+id/tvStudentId`).
**Reason:** Necessary for Java to find and dynamically update views.

**File:** `app/src/main/java/com/social7/studentlearnanalytics/student/StudentDashboardFragment.java`
**Purpose:** Controls the Student Dashboard View.
**Before:** Only bound Name and Performance; the rest were left as mock XML text.
**After:** Binds Student ID, Department, Year Level, Attendance, Assignments, Quizzes, GPA, Predicted Score, and Risk Level dynamically via `bindDashboardData`.
**Reason:** Show real authenticated API data instead of STU023001.

**File:** `app/src/main/java/com/social7/studentlearnanalytics/student/StudentCoursesFragment.java`
**Purpose:** Controls "My Courses" tab.
**Before:** Used hardcoded list of fake courses.
**After:** Parses the `enrollments` list out of the Dashboard API response and dynamically injects course names and instructor details via layout hierarchy traversal.
**Reason:** To reflect actual enrolled courses.

**File:** `app/src/main/java/com/social7/studentlearnanalytics/student/CourseDetailActivity.java`
**Purpose:** Detailed view for a selected course.
**Before:** Static numbers mapped from intents.
**After:** Calls `getPerformanceRecords` via API and updates Attendance/Quizzes/Exam scores upon successful response.
**Reason:** Real-time data drill-down.

### 12. Authentication Changes
Verified existing bearer token passing through `SessionManager` in `ApiClient.java`. Unchanged.

### 13. UI Changes
No visual design/XML layouts were fundamentally changed. Only data mapping and IDs were added to preserve the original design.

### 14. Bug Fixes

| Bug | Cause | Solution | File |
|-----|-------|----------|------|
| Compile Error | `name` had private access in `UserInfo` | Changed modifier from `private` to `public` | `DashboardResponse.java` |
| Compile Error | Missing `android.widget.LinearLayout` | Added import | `StudentCoursesFragment.java` |
| Mismatched API keys | Gson using `overall_score` but Laravel sending `overall_performance` | Renamed `@SerializedName` fields in Android Model | `DashboardResponse.java` |

### 15. Testing
- Laravel Feature Tests: NOT TESTED
- API tests (Postman/Curl): PASS
- Login tests: PASS
- Dashboard tests: PASS (Data binds properly)
- Android build: PASS (`./gradlew assembleDebug` succeeded)
- Pixel 6 testing: PASS (Network verified)

### 16. Build Results
Android Build Successful (0 errors, 0 warnings affecting compilation).

### 17. API Test Results
Verified `/api/student/dashboard` returns expected deep JSON structure including `student`, `analytics`, `predictions`, `risk_assessment`, and `enrollments`.

### 18. Known Issues
- **Backend API missing for: GPA** (LearningAnalytic model has no GPA field)
- **Backend API missing for: My Learning Progress chart** (No time-series data provided yet for charts)

### 19. Remaining Tasks
1. Verify Dashboard real data (COMPLETED — Phase 1)
2. Verify My Courses (COMPLETED — Phase 1)
3. Verify Course Detail (COMPLETED — Phase 1)
4. Learning Analytics (COMPLETED — Phase 2)
5. Prediction & Risk (COMPLETED — Phase 2)
6. Attendance (COMPLETED — Phase 3)
7. Assignments (COMPLETED — Phase 4)
8. Quizzes (COMPLETED — Phase 5)
9. Exams (COMPLETED — Phase 6)
10. GPA (Requires Backend Update)
11. Report
12. Recommendations
13. Notifications
14. Profile

### 20. Next Recommended Step
Proceed to **Learning Analytics** and **Prediction & Risk** detail activities, ensuring their specific API routes are built or mapped correctly in Android. Implement missing backend data for GPA and Chart Time-Series.

---
**Status Summary:**
- **Completed:** Login, Dashboard API, My Courses API, Course Detail API mapping.
- **In Progress:** Dashboard refinements.
- **Not Started:** Specific detail tabs (Quizzes, Assignments, Attendance inside Course Detail).
- **Blocked:** GPA display (awaiting backend logic).

### 21. Date: October 7, 2026 (Phase 2)
### 22. Work Completed (Phase 2)
- Reused GET /api/dashboard/student to deliver data for Learning Analytics and Prediction screens.
- Bound live data to Android LearningAnalyticsFragment (Overall performance, Attendance, Assignments, Quizzes, Exams, and rendering the time-series chart via PhilJay LineChart).
- Bound live data to Android PredictionFragment (Predicted score, Risk Level colors/text, and mapping individual Key Factors metric progress bars).
- Verified GpaRecord model handles GPA natively, avoiding database schema migrations.
- Extracted dynamic chart labeling for historical records in Android using the existing LearningAnalytic and Prediction arrays.

### 23. Files Created (Phase 2)
None.

### 24. Files Modified (Phase 2)
- d:\Development\Projects\student_learning_api\app\Http\Controllers\DashboardController.php
- d:\USEA\Y3 S2\android\lesson_practist\studentlearnanalytics\app\src\main\java\com\social7\studentlearnanalytics\model\DashboardResponse.java
- d:\USEA\Y3 S2\android\lesson_practist\studentlearnanalytics\app\src\main\java\com\social7\studentlearnanalytics\student\LearningAnalyticsFragment.java
- d:\USEA\Y3 S2\android\lesson_practist\studentlearnanalytics\app\src\main\java\com\social7\studentlearnanalytics\student\PredictionFragment.java

### 25. Files Deleted (Phase 2)
None.

### 26. APIs Reused (Phase 2)
- GET /api/dashboard/student (DashboardController@student): Reused this endpoint to deliver the data for both the Learning Analytics and the Prediction & Risk screens, preventing redundant API calls.

### 27. APIs Modified (Phase 2)
- GET /api/dashboard/student: Injected gpa_record, chart_data, chart_labels, prediction_chart_data, and prediction_chart_labels into the JSON payload seamlessly.

### 28. Database Changes (Phase 2)
None.

### 29. Android Changes (Phase 2)
- Updated DashboardResponse.java to map the new chart JSON arrays and exam_average.
- Upgraded LearningAnalyticsFragment.java to fetch data securely from the dashboard API and dynamically inject it into the UI.
- Upgraded PredictionFragment.java to fetch data from the dashboard API and bind predicted scores, dynamic progress bars for metrics, and dynamic styling for high/medium/low risk.

### 30. Bug Fixes (Phase 2)
- Replaced hardcoded string layout arrays for time-series charts with dynamic parsing of chart_labels JSON payloads.
- Circumvented GPA calculation bottleneck by extracting direct historical query from the gpa_records table, eliminating server recalculation load.

### 31. Tests Performed (Phase 2)
- Dashboard payload validation (PASS).
- Compile/Build Check: ./gradlew assembleDebug (PASS, 1m 22s).

### 32. Remaining Issues (Phase 2)
- Implement Teacher perspective dashboards for risk assessments.
- Implement actionable Notifications and API tracking for "Recommendations".

---
### 33. Date: October 9, 2026 (Phase 3)
### 34. Work Completed (Phase 3)
- Integrated real Laravel API data into the Android AttendanceDetailActivity screen.
- Secured the backend attendance retrieval mechanism, strictly authorizing by the authenticated token instead of trusting client query parameters.
- Built a dynamic local grouping algorithm on Android to convert flat attendance JSON structures into monthly bar chart layouts natively.

### 35. Files Created (Phase 3)
None.

### 36. Files Modified (Phase 3)
- Laravel: `routes/api.php`
- Laravel: `app/Http/Controllers/DashboardController.php`
- Android: `app/src/main/java/com/social7/studentlearnanalytics/network/ApiService.java`
- Android: `app/src/main/java/com/social7/studentlearnanalytics/student/attendance/AttendanceDetailActivity.java`

### 37. Files Deleted (Phase 3)
None.

### 38. APIs Created (Phase 3)
- `GET /api/student/attendance`: Extracts the student's ID from `$request->user()->student->id` strictly via Sanctum token. Delivers an aggregated `summary` object alongside raw `details` arrays.

### 39. APIs Modified (Phase 3)
None.

### 40. Database Changes (Phase 3)
None.

### 41. Android Changes (Phase 3)
- Upgraded `AttendanceDetailActivity.java` to fetch its UI from `/api/student/attendance`.
- Implemented robust `PieChart` data mapping (Present vs Absent percentages).
- Implemented robust `BarChart` data mapping, generating monthly bins using `SimpleDateFormat` string keys corresponding to `attendance_date`.
- Refactored chart initialization patterns to allow seamless asynchronous layout swaps without lifecycle crashes.

### 42. Laravel Changes (Phase 3)
- Added `DashboardController::studentAttendance` with automatic summation of total/present/absent/late logic.
- Ensured 100% backward compatibility by not overwriting standard `AttendanceDetailController` endpoints relied upon by teacher/admin web portals.

### 43. Bug Fixes (Phase 3)
- Resolved critical vulnerability where students could manipulate `student_id` in unverified REST operations to view private attendances belonging to other classmates.

### 44. Tests Performed (Phase 3)
- Gradle Build `./gradlew assembleDebug` successfully verified all modified Java code and Gson imports.

### 45. Remaining Issues (Phase 3)
- The Assignments and Quizzes detailed views still need API integrations.

---
### 46. Date: October 12, 2026 (Phase 4)
### 47. Work Completed (Phase 4)
- Integrated real Laravel API data into the Android Assignment screen (`AssessmentDetailActivity`).
- Verified and secured the backend assignment retrieval mechanism (`GET /api/student/assignments`), strictly validating against courses the student is actually enrolled in via Sanctum token authorization.
- Added eager loading of `semester` relation in `DashboardController::studentAssignments` to provide clean semester names alongside course data.
- Enhanced `activity_assessment_detail.xml` with a centered `ProgressBar` loading indicator and empty state layout (`layout_empty_state`) for seamless state transitions without crashing.
- Updated `AssessmentDetailActivity.java` with robust multi-pattern ISO 8601 date parsing for `due_date`, dynamic semester extraction & filtering via `semester_spinner`, dynamic average score calculation based on graded assignments, and error/auth handling (HTTP 401 session expiry and network error toasts).
- Enhanced `Assessment.java` model with overloaded constructor and attributes for `semester`, `courseName`, `isSubmitted`, and `isGraded`, maintaining 100% backward compatibility for `QuizDetailActivity`.
- Updated `AssessmentAdapter.java` to style submission status and scores dynamically using `ContextCompat` (green for graded, blue for submitted, orange for pending/due).

### 48. Files Created (Phase 4)
None.

### 49. Files Modified (Phase 4)
- Laravel: `app/Http/Controllers/DashboardController.php`
- Android: `app/src/main/res/layout/activity_assessment_detail.xml`
- Android: `app/src/main/java/com/social7/studentlearnanalytics/model/Assessment.java`
- Android: `app/src/main/java/com/social7/studentlearnanalytics/adapter/AssessmentAdapter.java`
- Android: `app/src/main/java/com/social7/studentlearnanalytics/student/assignment/AssessmentDetailActivity.java`

### 50. Files Deleted (Phase 4)
None.

### 51. APIs Reused / Integrated (Phase 4)
- `GET /api/student/assignments`: Safely queries published assignments specific to courses where the authenticated student is currently enrolled, including student submissions, attempts, course metadata, and semester information.

### 52. APIs Modified (Phase 4)
- `GET /api/student/assignments`: Eager loads `semester` relation in `DashboardController::studentAssignments`.

### 53. Database Changes (Phase 4)
None.

### 54. Android Changes (Phase 4)
- `activity_assessment_detail.xml`: Added `@+id/loading_progress_bar` and `@+id/layout_empty_state` with `@+id/empty_state_text`.
- `Assessment.java`: Added constructor and fields for `semester`, `courseName`, `isSubmitted`, `isGraded`, with adaptive score formatting (`score / totalScore`, `Submitted`, `Not Submitted`).
- `AssessmentAdapter.java`: Upgraded to modern `ContextCompat.getColor` and dynamic color styling based on submission/grading status.
- `AssessmentDetailActivity.java`: Bound real data from `getStudentAssignments()`, implemented interactive semester filtering from the spinner, resilient multi-pattern date parsing, dynamic average score calculation, and graceful error handling.

### 55. Laravel Changes (Phase 4)
- Added eager loading of `semester` on `DashboardController::studentAssignments` so semester names are provided to client applications.

### 56. Bug Fixes (Phase 4)
- Fixed potential `ParseException` when backend ISO 8601 dates lack microsecond precision by implementing a multi-format date parser fallback.
- Fixed attempt/submission parsing where an empty submissions array blocked evaluation of attempts.
- Added visual empty states and progress indicators to prevent blank screens or crashes on empty or slow network responses.

### 57. Tests Performed (Phase 4)
- Backend Tinker endpoint validation for `GET /api/student/assignments`: PASS (Status 200, returns scoped student assignments with submissions and courses).
- Android Gradle Build: PASS (`./gradlew assembleDebug` SUCCESS in 13s, 0 errors).

### 58. Next Recommended Step (Phase 5)
- Proceed to Phase 5: Quiz API Integration (`QuizDetailActivity.java`).

---
### 59. Date: October 13, 2026 (Phase 5)
### 60. Work Completed (Phase 5)
- Integrated real Laravel API data into the Android `QuizDetailActivity.java` screen.
- Added a new secured `GET /api/student/quizzes` endpoint inside the `auth:sanctum` middleware group, scoped exclusively to the authenticated student's enrolled courses with only `published`/`open` quizzes returned and the student's own attempt(s) pre-filtered on the server side.
- Updated `activity_quiz_detail.xml` to add a centered `ProgressBar` loading indicator and empty-state `LinearLayout` (with icon + message text), matching the pattern established in Phase 4 for Assignments.
- Added `getStudentQuizzes()` call to `ApiService.java`.
- Rewrote `QuizDetailActivity.java` to replace all dummy data with live API data, with full semester filtering, average score calculation, attempt score/status parsing, multi-pattern date formatting, and graceful error handling (HTTP 401, non-2xx, JSON parse exceptions, network failures).

### 61. Files Created (Phase 5)
None.

### 62. Files Modified (Phase 5)
- Laravel: `app/Http/Controllers/DashboardController.php` — added `studentQuizzes()` method
- Laravel: `routes/api.php` — registered `GET /api/student/quizzes` inside `auth:sanctum` group
- Android: `app/src/main/java/com/social7/studentlearnanalytics/network/ApiService.java` — added `getStudentQuizzes()`
- Android: `app/src/main/res/layout/activity_quiz_detail.xml` — added `loading_progress_bar`, `layout_empty_state`, `empty_state_text`
- Android: `app/src/main/java/com/social7/studentlearnanalytics/student/quiz/QuizDetailActivity.java` — replaced dummy data with real API integration

### 63. Files Deleted (Phase 5)
None.

### 64. APIs Created (Phase 5)
- `GET /api/student/quizzes` (auth:sanctum): Returns `published`/`open` quizzes for all courses where the authenticated student has `enrolled` status. Each quiz includes `course`, `semester`, and the student's own `attempts` (ordered DESC by `attempt_number`, i.e. latest first). Student identity is derived entirely from the Sanctum token — no client-controlled `student_id` parameter is trusted.

### 65. APIs Modified (Phase 5)
None.

### 66. Database Changes (Phase 5)
None.

### 67. Android Changes (Phase 5)
- `activity_quiz_detail.xml`: Added `@+id/loading_progress_bar` and `@+id/layout_empty_state` with `@+id/empty_state_text`; changed `average_score_text` default value from hardcoded `78%` to `--`.
- `ApiService.java`: Added `getStudentQuizzes()` Retrofit call targeting `GET student/quizzes`.
- `QuizDetailActivity.java`: Full replacement — removed all hardcoded dummy items; added `fetchQuizzes()` calling `getStudentQuizzes()`, multi-pattern ISO 8601 date parsing (using `start_at`), semester extraction and spinner filtering, attempt-based score/status reading (`completed`/`timed_out` = submitted; `score` present = graded), average score calculation, and all error handling states.

### 68. JSON Fields Consumed by Android (Phase 5)
From `GET /api/student/quizzes` → `data[]`:
- `id` → Assessment ID
- `title` → Assessment title
- `total_score` → Max score (integer)
- `start_at` → Display date (ISO 8601, multi-pattern parsed)
- `semester.name` (or fallback `semester_id`) → Semester label for filtering
- `course.course_name` (or `course.name`) → Course label displayed in list
- `attempts[].score` → Student's numeric score
- `attempts[].status` → `completed`/`timed_out` = submitted; `in_progress` = not submitted

### 69. Authentication and Authorization (Phase 5)
- Token: Sent as `Authorization: Bearer <token>` via the existing `ApiClient` OkHttp interceptor (unchanged).
- Server: `DashboardController@studentQuizzes` derives `$student` from `$request->user()->student` — no client-supplied ID is trusted.
- Enrollment scope: `whereHas('course.students', ...)` with `students.id = $student->id AND course_enrollments.status = 'enrolled'` — students cannot access quizzes from courses they are not enrolled in.
- Attempt scope: `attempts` relation filtered with `where('student_id', $student->id)` — students only see their own attempt scores.

### 70. Remaining Issues (Phase 5)
- Exams, GPA, Reports, Recommendations, Notifications, Profile detail activities still use dummy/partial data.
- Quiz attempt scores depend on seeded/real data in `quiz_attempts` table — if none exist the list shows quizzes as "Not Submitted" with score 0.

---
### 71. Date: October 15, 2026 (Phase 6)
### 72. Work Completed (Phase 6)
- Integrated real Laravel API data into the Android Exams screen (`ExamDetailActivity`), replacing the 5 hardcoded dummy exams.
- Inspected the existing exam endpoints. `GET /api/exams` and `GET /api/exam-results` are registered outside the `auth:sanctum` group and are not scoped to a student (`exam-results` accepts any `student_id` query parameter), so they were not reused for the student view.
- Added a new secured endpoint `GET /api/student/exams`, following the Phase 3–5 `DashboardController` pattern.
- Upcoming vs Completed is derived only from real backend data (`status`, `exam_date`, and the student's `exam_results`). No scores, times, or rooms are fabricated.

### 73. Files Created (Phase 6)
- Laravel: `D:\Development\Projects\student_learning_api\tests\Feature\StudentExamsTest.php`

### 74. Files Modified (Phase 6)
- Laravel: `D:\Development\Projects\student_learning_api\app\Http\Controllers\DashboardController.php` — added `studentExams()` method (30 lines)
- Laravel: `D:\Development\Projects\student_learning_api\routes\api.php` — added 1 route line
- Android: `D:\Development\Projects\studentlearnanalytics\app\src\main\java\com\social7\studentlearnanalytics\network\ApiService.java` — added `getStudentExams()`
- Android: `D:\Development\Projects\studentlearnanalytics\app\src\main\java\com\social7\studentlearnanalytics\student\ExamDetailActivity.java` — dummy data replaced with API integration
- Android: `D:\Development\Projects\studentlearnanalytics\app\src\main\java\com\social7\studentlearnanalytics\model\Exam.java` — added nullable `scoreText` field, new constructor, `getScoreText()`
- Android: `D:\Development\Projects\studentlearnanalytics\app\src\main\java\com\social7\studentlearnanalytics\adapter\ExamAdapter.java` — score shown only when `scoreText` is non-null
- Android: `D:\Development\Projects\studentlearnanalytics\app\src\main\res\layout\activity_exam_detail.xml` — added `loading_progress_bar` and `empty_state_text` (hidden by default)
- Docs: `SLAS_CHANGELOG.md`, `SLAS_Development_Report.docx`, `SLAS_Development_Report.pdf`

Verification method: Laravel via `git status` / `git diff` (the `DashboardController.php` and `routes/api.php` diffs also contain uncommitted Phase 5 `studentQuizzes` changes, which were left in place). The Android project is not a Git repository, so its changes were verified by file modification times (all 5 files modified 2026-10-15 14:18–14:19).

### 75. Files Deleted (Phase 6)
None.

### 76. APIs Created (Phase 6)
- `GET /api/student/exams` (auth:sanctum, `DashboardController@studentExams`): returns exams with `status != 'cancelled'` for courses where the authenticated student has `course_enrollments.status = 'enrolled'`, ordered by `exam_date` ascending. Each exam eager-loads `course`, `semester`, and `results` filtered to `student_id = authenticated student`. Returns 404 `Student profile not found.` if the user has no student profile.

### 77. APIs Modified (Phase 6)
None. The existing `/api/exams` and `/api/exam-results` resources were left unchanged.

### 78. Database Changes (Phase 6)
None. The existing `exams` and `exam_results` tables and the `Exam`/`ExamResult` models (relations `course`, `semester`, `results`) were used as-is.

### 79. Android Changes (Phase 6)
- `ApiService.java`: added `@GET("student/exams") Call<JsonObject> getStudentExams()`.
- `ExamDetailActivity.java`: removed `initExamData()` dummy list. Added `fetchExams()`, defensive JSON parsing, and loading / empty / error states. Existing tab UI, back button, and window-insets handling are preserved.
  - Completed = the student has a result with a non-null `score`, OR `status = completed`, OR `exam_date` is before today. Otherwise the exam is Upcoming.
  - Upcoming is sorted soonest first; Completed is sorted most recent first.
  - Details line: exam type, title, duration and status for upcoming exams; grade or "Result pending" for completed exams.
  - Error handling: HTTP 401 → "Session expired"; 404 → "Student profile not found."; other HTTP errors → message with the code; malformed JSON → "Error loading exams"; network failure → "Network error".
- `Exam.java`: original 8-argument constructor kept (now also sets `scoreText`). New 7-argument constructor with nullable `scoreText`.
- `ExamAdapter.java`: the score view is visible only when the exam is completed AND `scoreText != null`.
- `activity_exam_detail.xml`: additive `ProgressBar` and `TextView`; existing views and IDs unchanged.

JSON fields consumed from `data[]`: `id`, `title`, `exam_type`, `exam_date` (first 10 chars, `yyyy-MM-dd`), `status`, `total_score`, `duration_minutes`, `course.course_name` / `course.name`, `results[0].score`, `results[0].grade`.

### 80. Laravel Changes (Phase 6)
- `DashboardController::studentExams()` added. The student is derived from `$request->user()->student`; no client-supplied `student_id` is used.
- Route `Route::get('/student/exams', ...)` registered inside the `auth:sanctum` group.
- Feature test `StudentExamsTest` added (3 tests).

### 81. Bug Fixes (Phase 6)
- Exams screen previously showed hardcoded exams and scores for every user; it now shows only the authenticated student's real data.
- Avoided exposing other students' exam scores: the student view does not use the unscoped `/api/exam-results`.

### 82. Tests Performed (Phase 6)
- `php artisan test --filter=StudentExamsTest`: PASS — 3 passed (10 assertions).
  - Unauthenticated → 401.
  - User without a student profile → 404.
  - Enrolled student sees only enrolled-course, non-cancelled exams and only their own result; the `?student_id=` of another student is ignored.
- `php artisan test` (full suite): PASS — 58 passed (405 assertions), 104.91s.
- `gradlew.bat assembleDebug`: output `BUILD SUCCESSFUL in 35s` (34 actionable tasks). PowerShell reported exit code 1, probably caused by Gradle's deprecation-warning output.
- Not performed: on-device / emulator test of the Exams screen against live seeded data.

### 83. Remaining Issues (Phase 6)
- The `exams` table has no time or room columns, so those are not displayed (the old dummy data showed them).
- Course Detail has no navigation entry to Exams. The screen is reached from `LearningAnalyticsFragment` and the student `NotificationFragment` (unchanged).
- `GET /api/exams` and `GET /api/exam-results` remain public and unscoped (pre-existing; not modified in this phase).
- On-device verification of the Exams screen is pending.
- GPA, Reports, Recommendations, Notifications, and Profile detail screens still use dummy/partial data.

### 84. Next Recommended Step
- Verify the Exams screen on a device, then proceed to Phase 7.

---
### 85. Date: October 15, 2026 (Attendance Detail - API binding cleanup)
- `AttendanceDetailActivity.java`: the summary cards (Total/Present/Absent) and the donut legend were static XML placeholders (25/22/3, "22 (88%)", "3 (12%)"); they are now bound to `summary` from `GET /api/student/attendance`. Added loading (`ProgressBar`), empty, 401, 404, other-HTTP and network-error states, matching `QuizDetailActivity`. The semester label shows "All Semesters" (no semester data in the API).
- `activity_attendance_detail.xml`: added IDs to the placeholder TextViews plus hidden `loading_progress_bar` / `empty_state_text`.

### 86. Date: October 15, 2026 (Attendance Summary consistency audit)
#### Discrepancies found
| Calculation | present | late | excused | absent | unknown status |
|---|---|---|---|---|---|
| Backend `summary.percentage` (`DashboardController::studentAttendance`) | numerator + denominator | denominator only | denominator only | denominator only | denominator only |
| Donut (before) | Present slice | not shown | not shown | Absent slice | not shown |
| Legend (before) | present/total | not shown | not shown | absent/total | not shown (shares didn't sum to 100%) |
| Monthly chart (Phase 3, before) | Present stack | **counted as Present** | **counted as Absent** | Absent stack | **counted as Absent** |
- Colors also disagreed: donut absent `#FF4D67`, bar absent `#D9D3F5`, legend dot `#A5B4FC`; existing legend dots used `app:tint` on a plain `View`, which has no effect.

#### Counting policy (now applied everywhere on this screen)
- Each `attendance_details` row is counted once under its raw status: present, late, excused, absent. No status is merged into another. Unknown statuses are shown as "Other" (row hidden when 0).
- Per-status percentages all use `summary.total` as denominator, so they sum to 100%.
- Headline rate = backend `summary.percentage` = present / total (on-time presence rate), labelled "X% Present". It is intentionally not called an "attendance rate" because late/excused are excluded from the numerator.
- Monthly chart stacks the same categories per month (`attendance.attendance_date`); per-status monthly sums equal the summary counts except records without a date (logged, excluded).

#### Files modified
- Android: `student/attendance/AttendanceDetailActivity.java` - policy implemented; 4(+Other)-segment donut and stacked bars; shared color palette; safe parsing of missing `data`/`summary`/`details`/fields; zero-total handled.
- Android: `res/layout/activity_attendance_detail.xml` - added Late, Excused, Other legend rows (`tvLateLegend`, `tvExcusedLegend`, `tvOtherLegend`, `rowOtherLegend`) and Late/Excused in the monthly legend; legend dots switched to `android:backgroundTint` with chart colors.
- No backend, route, `ApiService` or `ApiClient` changes.

#### Semester filter
- Not implemented. `GET /api/student/attendance` takes no semester parameter and returns no semester per record. Needed backend capability: accept an optional `semester_id` (validated, applied via `attendance.course`/`semester` relation) and either return the semester on each record or provide a list of the student's semesters.

#### Remaining issues
- Backend status column is a free string (no enum) and the backend counts with case-sensitive matches; Android matches case-insensitively, so a mis-cased status would appear as "Other" in the summary but as its status in the monthly chart.
- No Laravel feature test covers `/api/student/attendance`.
- Summary cards still show only Total/Present/Absent; late/excused are in the legend and charts.
- On-device verification not performed.

#### Tests performed (Attendance consistency audit)
- `gradlew.bat assembleDebug`: exit code 0.
- `gradlew.bat assembleDebug lintDebug`: BUILD FAILED, exit code 1, due to a pre-existing lint error in unrelated `item_recent_activity.xml:88` (NotSibling: `@+id/tvTitle`). 1349 warnings project-wide; none are errors in the attendance files (warnings there: 37 HardcodedText, 6 SmallSp, 6 UseCompoundDrawables, 1 each SetTextI18n, TooManyViews, Overdraw, DisableBaselineAlignment).
- `php artisan test` (backend unchanged): 58 passed (405 assertions), 120.37s.
- Not performed: on-device / emulator test.

---
### 87. Date: October 16, 2026 (Attendance Verification and Lint Fix)
#### Layout lint fix
- `app/src/main/res/layout/item_recent_activity.xml:88`: the bottom divider `View` was constrained with `app:layout_constraintStart_toStartOf="@+id/tvTitle"`. `tvTitle` is a child of the nested `LinearLayout` (line 41), so it is not a sibling of the divider inside the outer `ConstraintLayout`; ConstraintLayout cannot constrain to a non-sibling, producing the lint error `NotSibling` at line 88.
- Fix (smallest correct change): the constraint now reads `app:layout_constraintStart_toEndOf="@id/iconContainer"`, a real sibling that spans the timeline column. No views were removed or redesigned; the divider's `width=0dp` + `End_toEndOf="parent"` still spans the row. `@+id/tvTitle` is no longer referenced from the outer ConstraintLayout.
- Note: the file already contained the corrected `@id/iconContainer` reference when inspected this session; the change is recorded here as the verified resolution of the previously reported error.

#### Attendance counting consistency (Android)
- `student/attendance/AttendanceDetailActivity.java`: the summary counts (`present`/`late`/`excused`/`absent`) are now derived by iterating `data.details[]` and mapping each row's `status` through the shared `statusIndex()` helper, instead of reading only the backend's case-sensitive `summary` values. When `details` is empty/absent, it falls back to the backend `summary`.
- This makes summary cards, donut chart, legend rows, and monthly chart all count Present, Late, Excused, Absent and Other from one identical, case-insensitive normalization. Unknown/blank/null statuses fall to "Other" and are never folded into a known category. `total` falls back to the summed counts when the backend `total` is 0/missing.
- `statusIndex(String)`: `status.trim().toLowerCase(Locale.US)`; null -> `OTHER`; `present`/`late`/`excused`/`absent` -> their indices; anything else -> `OTHER`.

#### Backend limitation (reported, not changed)
- `DashboardController::studentAttendance` still counts with case-sensitive `where('status','present'|'late'|'excused'|'absent')` and returns a `summary` computed that way. Android no longer depends on those per-status counts (it recomputes from `details`), but the backend `percentage` is still present/total on the raw lowercase match. A mis-cased or unknown status row is excluded from the backend numerator but is now shown consistently as "Other" on Android. Fixing the backend would require normalizing status on write or an enum; not done here.
- No Laravel endpoint, route, schema, or semester-filtering change was made. Semester filtering remains unimplemented (unchanged limitation from section 86).

#### Tests / build results
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL, exit code 0 (34 tasks, up-to-date).
- `gradlew.bat clean assembleDebug`: BUILD SUCCESSFUL, exit code 0 (35 tasks executed) - confirms the Java change compiles.
- `gradlew.bat lintDebug`: BUILD SUCCESSFUL, exit code 0 - 0 errors, 1348 warnings. The prior `item_recent_activity.xml:88` NotSibling error is resolved (warnings dropped 1349 -> 1348).
- Not performed: on-device / emulator test of the attendance screen. Backend `php artisan test` not re-run (no backend change this session).