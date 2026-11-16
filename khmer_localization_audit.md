# SLAS Android Khmer Localization Audit

## Audit Summary
- **Java files scanned**: 209 (excluding `localization/` helpers)
- **XML files scanned**: 193 (layouts, menus, navigation/xml)
- **String resources**: `values/strings.xml` and `values-km/strings.xml` (100% key parity, 0 duplicates)
- **Remaining hardcoded strings**: 817 unique entries (588 XML, 248 Java — listed in `khmer_remaining_english.json`), primarily secondary/long-form descriptions, sample preview data, and concatenated strings.

## Key Source-Code Fixes Applied
| File | Class / Component | Fix | Status |
| --- | --- | --- | --- |
| `fragment_admin_dashboard.xml` | Admin Dashboard Layout | Replaced `Good Morning,`, `Admin SLAS`, `Annual Student Enrollment & Engagement`, `● Active Learners`, `No Data Available for Selected Year`, `Compare Years`, `Historical Reports`, `View Stats >`, `API Server`, `ML Service`, `Online` with `@string/...` | FIXED |
| `AdminDashboardFragment.java` | `AdminDashboardFragment` | Localized time-based greeting (`ApiLabelLocalizer.getTimeGreeting`), `Admin SLAS` display name, `All Academic Years` spinner item, `Active Learners` & `Avg GPA` chart datasets, `Present`/`Late`/`Absent` stack labels, `Low`/`Medium`/`High` pie slices, `Total Assessed`, `showYearComparisonDialog()`, `setTrendColor()` (`No change this month`, `No comparison data`), and `setServiceStatus()` (`Online`/`Offline`) | FIXED |
| `activity_fragment_student_dashboard.xml` & `StudentDashboardFragment.java` | Student Dashboard | Localized `Major information unavailable`, `Model Confidence`, `High risk! Action recommended.`, `Great job! Low risk.`, `No recent academic activities recorded yet.`, and card labels | FIXED |
| `fragment_teacher_dashboard.xml` & `TeacherDashboardFragment.java` | Teacher Dashboard | Localized `Learning Engagement Overview`, `Missed`, `No learning events recorded for this selection.`, `Failed to load engagement analytics` | FIXED |
| `AdminMainActivity.java` & `TeacherMainActivity.java` | Navigation Drawers | Replaced hardcoded `bindDrawerItem` titles and subtitles with `@StringRes` resource IDs | FIXED |
| `ApiLabelLocalizer.java` | Centralized API Label Mapper | Added `localizeTrendText`, `localizeSystemDisplayName`, and `getTimeGreeting` alongside attendance, risk, assignment, role, status, gender, and period mappings | FIXED |
| `ReportAdapter.java`, `StudentImportRowAdapter.java`, `ClassTeacherAssignmentAdapter.java`, `TeacherAssignmentReportAdapter.java`, `CurriculumAuditAdapter.java`, `CourseCategoryAdapter.java`, `TeacherReportAdapter.java`, `TeacherWorkloadAdapter.java`, `StudentListAdapter.java` | RecyclerView Adapters | Replaced remaining hardcoded prefixes/labels with `R.string.*` | FIXED |
