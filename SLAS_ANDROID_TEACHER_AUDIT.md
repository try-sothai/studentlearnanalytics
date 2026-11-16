# Student Learning Analytics System (SLAS)
## Android Teacher Module Architecture & Audit Report
**Document Version:** 1.0.0  
**Date:** November 11, 2026  
**Auditor:** Senior Android Java Engineer & System Architect  

---

## 1. Existing Android Project Architecture

The Android application (`D:\Development\Projects\studentlearnanalytics`) is a production native Android Java application configured with:
- **Build System:** Gradle 8.x, Compile SDK 36, Target SDK 36, Min SDK 27.
- **Language:** Java 17 (no Kotlin, no Jetpack Compose, preserving established architecture).
- **Networking:** Retrofit 2.11.0, OkHttp 4.12.0 with `HttpLoggingInterceptor`, Gson 2.12.1.
- **Security & Storage:** `androidx.security:security-crypto:1.1.0-alpha06` via `SessionManager.java` (`EncryptedSharedPreferences` with AES256_GCM).
- **Visualization:** `com.github.PhilJay:MPAndroidChart:v3.1.0`.
- **UI Framework:** Material Components 1.13.0, AndroidX Navigation, ConstraintLayout 2.2.1, RecyclerView 1.4.0.

---

## 2. Existing Screen Structure & Navigation

### 2.1 Teacher Entry Point & Flow
1. **Login & Session:**
   - User authenticates via `LoginActivity.java`.
   - On role `teacher` or `admin`, session credentials (JWT Bearer Token, Role, User ID, Name, Email) are stored in `SessionManager`.
   - App launches `TeacherMainActivity.java`.
   - `RoleGuard.checkAccess(this, "Teacher")` verifies teacher authorization before rendering.
2. **Container & Navigation:**
   - `TeacherMainActivity.java` controls a `DrawerLayout` (`teacher_drawer_layout`) and a `BottomNavigationView` (`teacher_bottom_navigation`).
   - Bottom navigation items: Home, Students, Courses, Reports, Profile.
   - Default fragment loaded on startup: `TeacherDashboardFragment.java` inside `R.id.teacher_fragment_container`.

---

## 3. Teacher Dashboard Audit: Working vs Missing

### 3.1 The Four Summary Cards
In [`fragment_teacher_dashboard.xml`](file:///D:/Development/Projects/studentlearnanalytics/app/src/main/res/layout/fragment_teacher_dashboard.xml):
1. **Total Students Card (`cardTotalStudents`):**
   - Click listener opens `StudentListFragment`.
   - Displays hardcoded static text `"156"`.
   - **Audit finding:** The `TextView` displaying the count has **no `android:id`**.
2. **My Courses Card (`cardMyCourses`):**
   - Click listener opens `CourseManagementFragment`.
   - Displays hardcoded static text `"5"`.
   - **Audit finding:** The `TextView` displaying the count has **no `android:id`**.
3. **At-Risk Students Card (`cardAtRisk`):**
   - Click listener opens `PredictionRiskFragment`.
   - Displays hardcoded static text `"18"`.
   - **Audit finding:** The `TextView` displaying the count has **no `android:id`**.
4. **Pending Scores Card (`cardPendingScores`):**
   - Click listener opens `ManageScoresFragment`.
   - Displays hardcoded static text `"12"`.
   - **Audit finding:** The `TextView` displaying the count has **no `android:id`**.

*Operational Status:* In `TeacherDashboardFragment.java`, `fetchTeacherDashboard()` triggers `ApiClient.getApiService(requireContext()).getTeacherDashboard()`. However, the response callback completely ignores card data and never updates the UI.

### 3.2 The Existing Chart
- A card titled `"Performance Overview"` with a decorative dropdown button `"This Semester ▾"`.
- Layout element: `<com.github.mikephil.charting.charts.LineChart android:id="@+id/lineChartPerformance" ... />`.
- Implementation in `TeacherDashboardFragment.java`:
  - `setupLineChart(chart)` creates hardcoded sample entries (Jan=72, Feb=78, Mar=81, Apr=75, May=84, Jun=88).
  - It does NOT reflect real student activity.
  - It does not compare Attendance, Assignment, Homework, and Quiz.
  - It does not provide dynamic filters.

---

## 4. Retrofit & API Integration Audit

### 4.1 `ApiClient.java`
- Base URL configured as: `http://192.168.1.41:8000/api/` (dynamically attachable to local backend).
- Interceptor automatically attaches:
  ```http
  Accept: application/json
  Authorization: Bearer <token>
  ```
- OkHttp client includes 30s timeouts and `HttpLoggingInterceptor`.

### 4.2 `ApiService.java`
- Currently contains:
  ```java
  @GET("dashboard/teacher")
  Call<JsonObject> getTeacherDashboard();
  ```
- **Audit finding:** Missing strongly-typed request/response models and endpoints for:
  - `GET api/teacher/dashboard` (summary data).
  - `GET api/teacher/filters` (dynamic authorized filters: generation, program, year, semester, course, class).
  - `GET api/teacher/analytics` (engagement overview comparison).

---

## 5. Exact Files to Modify & Create

### 5.1 Android Files to Modify
1. **`app/src/main/res/layout/fragment_teacher_dashboard.xml`**:
   - Add IDs to the 4 card count `TextView`s:
     - `android:id="@+id/tvTotalStudentsCount"`
     - `android:id="@+id/tvMyCoursesCount"`
     - `android:id="@+id/tvAtRiskCount"`
     - `android:id="@+id/tvPendingScoresCount"`
   - Maintain 100% visual layout, dimensions, icons, and colors of all 4 cards.
   - Replace the `Performance Overview` `LineChart` section with:
     - Compact horizontal scroll or two-row grid of filter selectors (`Spinner`s with subtle dropdown styling):
       * Generation, Major/Program, Academic Year, Study Year, Semester, Course, Class.
     - `com.github.mikephil.charting.charts.BarChart` with ID `barChartEngagement`.
     - Loading progress bar (`pbChartLoading`).
     - Error state layout with retry button (`layoutChartError`, `btnRetryChart`).
     - Empty state layout (`layoutChartEmpty`).
2. **`app/src/main/java/com/social7/studentlearnanalytics/teacher/TeacherDashboardFragment.java`**:
   - Bind card `TextView`s and populate dynamically from API.
   - Fetch `/api/teacher/filters` and populate Spinners dynamically based on teacher assignments.
   - Handle filter change listeners to reload `/api/teacher/analytics`.
   - Render MPAndroidChart Grouped Bar Chart comparing:
     * Attendance, Assignment, Homework, Quiz.
     * Three color-coded bars per activity: Completed (Green), Late (Orange), Missed (Red).
   - Display real percentage labels and formatted values.
   - Implement loading, error, retry, and empty states.
3. **`app/src/main/java/com/social7/studentlearnanalytics/network/ApiService.java`**:
   - Add typed API methods for teacher summary, filters, and analytics.

### 5.2 Android Files to Create
1. **`app/src/main/java/com/social7/studentlearnanalytics/model/TeacherDashboardSummaryResponse.java`**:
   - Typed model for `/api/teacher/dashboard`.
2. **`app/src/main/java/com/social7/studentlearnanalytics/model/TeacherFiltersResponse.java`**:
   - Typed model for `/api/teacher/filters` (`generations`, `programs`, `academic_years`, `study_years`, `semesters`, `courses`, `classes`).
3. **`app/src/main/java/com/social7/studentlearnanalytics/model/TeacherAnalyticsResponse.java`**:
   - Typed model for `/api/teacher/analytics` (`engagement` array containing `activity`, `completed`, `late`, `missed`, `total_events`).

---

## 6. Implementation Strategy & Compatibility Guardrails
- **Card Integrity:** Zero modifications to CardView cardElevation, strokeColor, cornerRadius, background, layout width/height, icons, or relative positions.
- **No Compose / No Kotlin:** 100% pure Android Java following established patterns.
- **Memory & Network Efficiency:** Avoid redundant calls when filter values have not changed.
- **Fail-Safe Rendering:** If an activity has no data or zero eligible events, display 0% or empty indicators without application crashes or chart rendering errors.
