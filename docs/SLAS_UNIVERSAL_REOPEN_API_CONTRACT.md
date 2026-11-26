# SLAS – Universal Reopen, Retake & Late Submission API Contract

**Project:** Student Learning Analytics System (SLAS)  
**Backend:** Laravel 12 (`D:\Development\Projects\student_learning_api`)  
**Client:** Android Application (`D:\Development\Projects\studentlearnanalytics`)  
**Database:** Existing SLAS MySQL Database  
**Status:** Verified Against Laravel Backend Inspection & Implemented

---

## Section G: Existing Laravel API Discovery Matrix (Verified)

> Inspected directly in `routes/api.php`, `routes/web.php`, `app/Http/Controllers/*`, `app/Services/*`, `app/Models/*`, and `database/migrations/*`.

### 1. Assignment Management

| HTTP Method | Actual URL | Controller & Method | Auth / Middleware | Request Parameters | Key Response Fields | DB Tables Involved | Reuse / Modify |
|---|---|---|---|---|---|---|---|
| `GET` | `/api/assignments` | `AssignmentController@index` | `auth:sanctum` (role-scoped via `ScopesAssessmentVisibility`) | Query: `course_id`, `teacher_id`, `semester_id` | `{ message, data: Assignment[] }` (`course`, `teacher`, `semester`) | `assignments`, `course_offerings`, `course_enrollments`, `course_offering_teachers` | **Reuse** |
| `GET` | `/api/assignments/{assignment}` | `AssignmentController@show` | `auth:sanctum` (role-scoped via `ScopesAssessmentVisibility`) | Path: `assignment` | `{ message, data: Assignment }` with student-filtered `submissions` (decorated by `SubmissionFileService`) | `assignments`, `assignment_submissions`, `courses`, `teachers`, `semesters` | **Modify** (attach server-side reopen/permission metadata & order `submissions` by `version_number DESC`) |
| `POST` | `/api/assignments` | `AssignmentController@store` | `auth:sanctum`, `role:admin` | `course_id`, `teacher_id`, `semester_id`, `title`, `description`, `total_score`, `due_date`, `status` | `{ message, data: Assignment }` (`201`) | `assignments` | **Reuse** |
| `PUT/PATCH` | `/api/assignments/{assignment}` | `AssignmentController@update` | `auth:sanctum`, `role:admin` | `title`, `description`, `total_score`, `due_date`, `status` | `{ message, data: Assignment }` | `assignments` | **Reuse** |
| `DELETE` | `/api/assignments/{assignment}` | `AssignmentController@destroy` | `auth:sanctum`, `role:admin` | Path: `assignment` | `{ message }` | `assignments`, `assignment_attempts`, `assignment_questions` | **Reuse** |
| `GET` | `/api/teacher/course-offerings/{id}/assignments` | `TeacherModuleController@courseOfferingAssignments` | `auth:sanctum`, `role:teacher,admin` + `isOfferingAuthorized` | Path: `id` (course offering) | `{ success, message, data: Assignment[] }` (`total_submissions`, `graded_submissions`) | `assignments`, `assignment_submissions`, `course_offerings` | **Modify** (filter `is_latest = true` on submission counts & expose reopen fields) |
| `POST` | `/api/teacher/course-offerings/{id}/assignments` | `TeacherModuleController@storeAssignment` | `auth:sanctum`, `role:teacher,admin` + `isOfferingAuthorized` | `title`, `description`, `assignment_type`, `total_score`, `due_date`, `status` | `{ success, message, data: Assignment }` (`201`) | `assignments`, `course_offerings` | **Modify** (support optional `allow_late_submission`, `allow_resubmission`, `max_attempts`, `allow_retakes`) |
| `PUT` | `/api/teacher/assignments/{assignmentId}` | `TeacherModuleController@updateAssignment` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | `title`, `description`, `total_score`, `due_date`, `status` | `{ success, message, data: Assignment }` | `assignments` | **Modify** (support optional `allow_late_submission`, `allow_resubmission`, `max_attempts`, `allow_retakes`) |
| `DELETE` | `/api/teacher/assignments/{assignmentId}` | `TeacherModuleController@deleteAssignment` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | Path: `assignmentId` | `{ success, message }` (`409` if submissions exist) | `assignments`, `assignment_submissions` | **Reuse** |
| `GET` / `POST` / `PUT` / `DELETE` | `/teacher/assignments*` | `routes/web.php` closures | `auth`, `role:teacher` | Web form fields (`course_id`, `title`, `starts_at`, `ends_at`, `assignment_type`, `allow_retakes`, `max_attempts`, `status`) | Blade HTML (`pages.teacher.assignments.*`) | `assignments`, `assignment_questions`, `assignment_attempts` | **Reuse** |

---

### 2. Homework Management

Homework is stored in the `assignments` table with `assignment_type = 'homework'` (distinct from `'assignment'`, `'file_upload'`, `'project'`, `'lab'`, `'true_false'`).

| HTTP Method | Actual URL | Controller & Method | Auth / Middleware | Request Parameters | Key Response Fields | DB Tables Involved | Reuse / Modify |
|---|---|---|---|---|---|---|---|
| `POST` | `/api/teacher/course-offerings/{id}/assignments` | `TeacherModuleController@storeAssignment` | `auth:sanctum`, `role:teacher,admin` | `assignment_type: 'homework'`, `title`, `description`, `total_score`, `due_date`, `status` | `{ success, message, data }` | `assignments`, `course_offerings` | **Modify** (same as Assignment; supports homework reopen policies) |
| `GET` | `/api/student/assignments` | `DashboardController@studentAssignments` | `auth:sanctum` (student profile) | Query: `semester_id`, `course_id` | `{ message, data: Assignment[] }` filtered on client by `assignment_type == 'homework'` | `assignments`, `assignment_submissions`, `assignment_attempts`, `course_offerings` | **Modify** (attach unified permissions/reopen status for both Assignment & Homework) |
| `GET` | `/api/teacher/analytics` | `TeacherModuleController@analytics` -> `TeacherAnalyticsService@calculateAssignmentEngagement` | `auth:sanctum`, `role:teacher,admin` | Query: `academic_year_id`, `semester_id`, `course_id`, `class_id`, `program_id`, `study_year`, `year_level` | `{ success, message, data: { engagement: [..., Homework, ...] } }` | `assignments` (`assignment_type = 'homework'`), `assignment_submissions` | **Modify** (filter `is_latest = true` on `assignment_submissions`) |

---

### 3. Quiz Management

| HTTP Method | Actual URL | Controller & Method | Auth / Middleware | Request Parameters | Key Response Fields | DB Tables Involved | Reuse / Modify |
|---|---|---|---|---|---|---|---|
| `GET` | `/api/quizzes` | `QuizController@index` | `auth:sanctum` (role-scoped via `ScopesAssessmentVisibility`) | Query: `course_id`, `teacher_id`, `semester_id` | `{ message, data: Quiz[] }` | `quizzes`, `courses`, `teachers`, `semesters` | **Reuse** |
| `GET` | `/api/quizzes/{quiz}` | `QuizController@show` | `auth:sanctum` | Path: `quiz` | `{ message, data: Quiz }` | `quizzes`, `quiz_questions` | **Reuse** |
| `POST` / `PUT` / `DELETE` | `/api/quizzes`, `/api/quizzes/{quiz}` | `QuizController@store`, `update`, `destroy` | `auth:sanctum`, `role:admin` | `course_id`, `teacher_id`, `semester_id`, `course_offering_id`, `title`, `total_score`, `duration_minutes`, `start_at`, `end_at`, `max_attempts`, `scoring_method`, `allow_late_submission`, `status` | `{ message, data: Quiz }` | `quizzes`, `quiz_attempts` | **Reuse** |
| `GET` / `POST` | `/api/teacher/course-offerings/{id}/quizzes` | `TeacherModuleController@courseOfferingQuizzes`, `storeQuiz` | `auth:sanctum`, `role:teacher,admin` | `title`, `description`, `duration_minutes`, `start_at`, `end_at`, `max_attempts`, `scoring_method`, `allow_late_submission` | `{ success, message, data }` | `quizzes`, `quiz_questions`, `quiz_attempts`, `course_offerings` | **Reuse** |
| `GET` / `PUT` / `DELETE` | `/api/teacher/quizzes/{quizId}` | `TeacherAssessmentController@showQuiz`, `updateQuiz`, `deleteQuiz` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | `title`, `description`, `duration_minutes`, `start_at`, `end_at`, `max_attempts`, `scoring_method`, `allow_late_submission`, `status` | `{ success, message, data }` (archives via soft delete if attempts exist) | `quizzes`, `quiz_questions`, `quiz_attempts`, `quiz_student_overrides` | **Reuse** |
| `PATCH` | `/api/teacher/quizzes/{quizId}/publish`, `/unpublish` | `TeacherAssessmentController@publishQuiz`, `unpublishQuiz` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | Path: `quizId` | `{ success, message, data }` | `quizzes`, `quiz_questions` | **Reuse** |
| `POST` | `/api/teacher/quizzes/{quizId}/reopen` | `TeacherAssessmentController@reopenQuiz` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | `end_at`, `reopened_until`, `allow_late_submission`, `reason` | `{ success, message, data }` | `quizzes`, `quiz_override_logs` | **Modify** (sync into `assessment_audit_logs`) |
| `GET` | `/api/teacher/quizzes/{quizId}/students` | `TeacherAssessmentController@quizStudents` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | Path: `quizId` | `{ success, data: [...] }` (`attempts_used`, `max_attempts`, `attempts_remaining`, `effective_score`, `has_override`, `override`) | `quizzes`, `students`, `course_enrollments`, `quiz_attempts`, `quiz_student_overrides` | **Reuse** |
| `GET` / `POST` / `DELETE` | `/api/teacher/quizzes/{quizId}/overrides`, `/overrides/{studentId}` | `TeacherAssessmentController@quizOverrides`, `storeQuizOverride`, `revokeQuizOverride` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | `student_id`, `grant_extra_attempts`, `extra_attempts`, `extended_end_at`, `allow_late_submission`, `reset_attempts`, `reason` | `{ success, message, data }` | `quiz_student_overrides`, `quiz_override_logs`, `quiz_attempts` | **Modify** (bidirectionally sync with `assessment_student_overrides` & `assessment_audit_logs`) |
| `POST` / `PUT` / `DELETE` | `/api/teacher/quizzes/{quizId}/questions`, `/quiz-questions/{questionId}*` | `TeacherAssessmentController@storeQuestion`, `updateQuestion`, `deleteQuestion`, `duplicateQuestion`, `reorderQuestions` | `auth:sanctum`, `role:teacher,admin` | Question payload / `order[]` | `{ success, message, data, quiz }` | `quiz_questions`, `quiz_attempts`, `quizzes` | **Reuse** |

---

### 4. Assignment & Homework Submissions

| HTTP Method | Actual URL | Controller & Method | Auth / Middleware | Request Parameters | Key Response Fields | DB Tables Involved | Reuse / Modify |
|---|---|---|---|---|---|---|---|
| `GET` | `/api/assignment-submissions` | `AssignmentSubmissionController@index` | `auth:sanctum` (student: own only; teacher: own offerings; admin: all) | Query: `assignment_id`, `student_id` | `{ message, data: AssignmentSubmission[] }` | `assignment_submissions`, `assignments`, `students` | **Modify** (order by `version_number DESC, id DESC`) |
| `POST` | `/api/assignment-submissions` | `AssignmentSubmissionController@store` | `auth:sanctum` (student only) | `assignment_id`, `file_path` (optional), `submission_text` (optional), `submission_file` (multipart) | `{ message, data: AssignmentSubmission }` (`201`) | `assignment_submissions`, `assignments`, `assessment_student_overrides` | **Modify** (enforce server deadline/reopen/attempt rules, create new `version_number` row, mark prior rows `is_latest = false`, duplicate guard) |
| `GET` / `PUT` / `DELETE` | `/api/assignment-submissions/{assignmentSubmission}` | `AssignmentSubmissionController@show`, `update`, `destroy` | `auth:sanctum` (`update`/`destroy` admin middleware) | `score`, `feedback`, `status` | `{ message, data }` | `assignment_submissions` | **Reuse** |
| `GET` | `/api/student/submissions` | `AssignmentSubmissionController@mySubmissions` | `auth:sanctum`, `role:student` | None (identity from token) | `{ success, message, data: [...] }` (`id`, `assignment_id`, `student_id`, `submission_status`, `grading_status`, `is_published`, `score`, `feedback`, file metadata, `version_label`) | `assignment_submissions`, `assignments`, `courses`, `course_offerings` | **Modify** (expose `submission_id`, `version_number`, `is_latest`, `is_late`, `uploaded_files`, server `can_resubmit`) |
| `GET` | `/api/student/submissions/{assignmentSubmission}` | `AssignmentSubmissionController@showStudentSubmission` | `auth:sanctum`, `role:student` + `authorizeStudentOwnership` | Path: `assignmentSubmission` | `{ success, message, data }` | `assignment_submissions`, `assignments` | **Modify** (include `version_number`, `is_late`, `is_latest`, `uploaded_files`) |
| `GET` | `/api/teacher/submissions/{submissionId}` | `TeacherModuleController@showSubmission` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | Path: `submissionId` | `{ success, message, data }` | `assignment_submissions`, `assignments`, `students` | **Modify** (include `version_number`, `is_late`, `is_latest`, `uploaded_files`) |
| `GET` / `POST` | `/student/assignments/{assignment}/submit` | `StudentAssignmentController@take`, `submit` | `auth`, `role:student` | `submission_file` or `answers[]` | Blade HTML / redirect | `assignments`, `assignment_attempts`, `assignment_questions`, `assignment_submissions` | **Modify** (evaluate access via `AssessmentReopenService` for Web/Android parity) |

---

### 5. Quiz Attempts

| HTTP Method | Actual URL | Controller & Method | Auth / Middleware | Request Parameters | Key Response Fields | DB Tables Involved | Reuse / Modify |
|---|---|---|---|---|---|---|---|
| `GET` | `/api/student/quizzes/{id}` | `StudentQuizController@show` | `auth:sanctum`, `role:student` | Path: `id` | `{ success, data: { ..., window_state, effective_end_at, max_attempts, base_max_attempts, extra_attempts, attempts_used, attempts_remaining, scoring_method, allow_late_submission, effective_score, can_start, can_continue, blocked_reason, override, attempt, attempts_history } }` | `quizzes`, `quiz_attempts`, `quiz_questions`, `quiz_student_overrides` | **Reuse** |
| `POST` | `/api/student/quizzes/{id}/start` | `StudentQuizController@start` | `auth:sanctum`, `role:student` | Path: `id` | `{ success, data: { attempt_id, attempt_number, started_at, expires_at, server_time, is_late, questions, answers } }` | `quizzes`, `quiz_attempts`, `quiz_questions`, `quiz_student_overrides` | **Modify** (`QuizAvailabilityService::attemptExpiresAt` handles reopened unfinished `in_progress` attempts) |
| `PUT` | `/api/student/quiz-attempts/{attemptId}/answers` | `StudentQuizController@saveAnswers` | `auth:sanctum`, `role:student` | `{ answers: [{ question_id, answer }] }` | `{ success, message, data: { answered, total_questions, unanswered_required_count, unanswered_question_numbers } }` | `quiz_attempts`, `quiz_answers` | **Reuse** |
| `POST` | `/api/student/quiz-attempts/{attemptId}/submit` | `StudentQuizController@submit` | `auth:sanctum`, `role:student` | `{ answers?: [...] }` | `{ success, message, data: resultPayload }` | `quiz_attempts`, `quiz_answers` | **Reuse** |
| `GET` | `/api/student/quiz-attempts/{attemptId}/review` | `StudentQuizController@review` | `auth:sanctum`, `role:student` | Path: `attemptId` | `{ success, data: { result, questions } }` | `quiz_attempts`, `quiz_answers`, `quiz_questions` | **Reuse** |
| `GET` | `/api/teacher/quizzes/{quizId}/attempts` | `TeacherAssessmentController@quizAttempts` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | Path: `quizId` | `{ success, data: [...] }` | `quiz_attempts`, `quiz_answers`, `students` | **Reuse** |
| `GET` | `/api/teacher/quiz-attempts/{attemptId}` | `TeacherAssessmentController@showQuizAttempt` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | Path: `attemptId` | `{ success, data: { ..., questions } }` | `quiz_attempts`, `quiz_answers`, `quiz_questions` | **Reuse** |
| `GET`..`DELETE` | `/api/quiz-attempts*` | `QuizAttemptController` | `auth:sanctum`, `role:admin` | Raw CRUD params | `{ message, data }` | `quiz_attempts`, `quiz_answers` | **Reuse** |

---

### 6. Student Assessment Access & 7. Teacher Assessment Management

| HTTP Method | Actual URL | Controller & Method | Auth / Middleware | Request Parameters | Key Response Fields | DB Tables Involved | Reuse / Modify |
|---|---|---|---|---|---|---|---|
| `GET` | `/api/student/assignments` | `DashboardController@studentAssignments` | `auth:sanctum` | Query: `semester_id`, `course_id` | `{ message, data: Assignment[] }` (`submissions`, `attempts`, `academic_context`) | `assignments`, `assignment_submissions`, `assignment_attempts`, `course_offerings`, `course_enrollments` | **Modify** (attach reopen/deadline/attempt permissions per student; order `submissions` by `version_number DESC`) |
| `GET` | `/api/student/quizzes` | `DashboardController@studentQuizzes` | `auth:sanctum` | Query: `semester_id`, `course_id` | `{ message, data: Quiz[] }` (`window_state`, `effective_end_at`, `max_attempts`, `attempts_used`, `attempts_remaining`, `allow_late_submission`, `can_start`, `can_continue`, `effective_score`, `attempts`) | `quizzes`, `quiz_attempts`, `quiz_student_overrides`, `course_offerings`, `course_enrollments` | **Modify** (include `reopen_status`, `original_deadline`, `extended_deadline`, `additional_attempts`, `blocked_reason` parity fields) |
| `GET` | `/api/student/exams` | `DashboardController@studentExams` | `auth:sanctum` | Query: `semester_id`, `course_id` | `{ message, data: Exam[] }` | `exams`, `exam_results`, `course_offerings`, `course_enrollments` | **Reuse** |
| `GET` | `/api/student/module/grades/courses/{id}` | `StudentModuleController@gradesCourse` | `auth:sanctum`, `role:student,admin` | Path: `id` (course offering) | `{ success, data: { breakdown, ... } }` | `assignments`, `assignment_submissions`, `quizzes`, `quiz_attempts`, `exams`, `exam_results`, `grades` | **Modify** (order `submissions` by `version_number DESC, id DESC`) |
| `GET` | `/api/teacher/assessments-overview` | `TeacherModuleController@assessmentsOverview` | `auth:sanctum`, `role:teacher,admin` | Query: `course_id` | `{ success, message, data: { courses, summary, assignments, quizzes, submissions, exams, gradebook, course_progress, pending_assessments } }` | `course_offerings`, `assignments`, `assignment_submissions`, `quizzes`, `quiz_attempts`, `exams`, `exam_results`, `grades` | **Modify** (filter `is_latest = true` on `assignment_submissions` counts/averages/feed; expose `allow_late_submission`, `allow_resubmission`, `reopened_until`, `version_number`, `is_late`) |
| `GET` | `/api/teacher/teaching-assignments` | `TeacherAssessmentController@teachingAssignments` | `auth:sanctum`, `role:teacher,admin` | None | `{ success, message, data: [...] }` | `course_offerings`, `course_offering_teachers`, `courses`, `classes`, `semesters`, `academic_years` | **Reuse** |

---

### 8. Score Management, 9. File Uploads & 10. Audit Logs

| HTTP Method | Actual URL | Controller & Method | Auth / Middleware | Request Parameters | Key Response Fields | DB Tables Involved | Reuse / Modify |
|---|---|---|---|---|---|---|---|
| `POST` | `/api/teacher/submissions/{submissionId}/grade` | `TeacherModuleController@gradeSubmission` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | `score`, `feedback`, `publish` | `{ success, message, data: presentSubmission }` | `assignment_submissions`, `assignments` | **Reuse** |
| `PATCH` | `/api/teacher/quiz-answers/{answerId}/grade` | `TeacherAssessmentController@gradeAnswer` | `auth:sanctum`, `role:teacher,admin` | `marks_obtained`, `feedback` / `teacher_feedback` | `{ success, message, data }` | `quiz_answers`, `quiz_attempts`, `quizzes` | **Reuse** |
| `POST` | `/api/teacher/quiz-attempts/{attemptId}/grade` | `TeacherAssessmentController@gradeAttemptAnswers` | `auth:sanctum`, `role:teacher,admin` | `grades: [{ answer_id, question_id, marks_obtained, feedback }]` | `{ success, data: showQuizAttempt }` | `quiz_answers`, `quiz_attempts`, `quizzes` | **Reuse** |
| `GET` / `POST` | `/api/teacher/course-offerings/{id}/scores` | `TeacherModuleController@courseOfferingScores`, `storeScore` | `auth:sanctum`, `role:teacher,admin` | `student_id`, `assignment_score`, `quiz_score`, `attendance_score`, `midterm_score`, `final_exam_score`, `total_score` | `{ success, message, data: Grade }` | `grades`, `course_offerings` | **Reuse** |
| `GET` / `POST` / `PUT` / `DELETE` | `/api/grades*` | `GradeController` | `auth:sanctum` (role-scoped) | Grade component scores & status | `{ message, data }` | `grades`, `courses`, `semesters`, `students` | **Reuse** |
| `GET` | `/api/student/submissions/{assignmentSubmission}/file` | `AssignmentSubmissionController@streamMySubmissionFile` | `auth:sanctum`, `role:student` + `authorizeStudentOwnership` | Query: `download` (`0\|1`) | Binary file stream (`Content-Type`, `Content-Disposition`) or `404` JSON | `assignment_submissions` + disk (`public`/`local`) | **Reuse** |
| `GET` | `/api/teacher/submissions/{submissionId}/file` | `TeacherModuleController@streamSubmissionFile` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | Query: `download` (`0\|1`) | Binary file stream or `404` JSON | `assignment_submissions` + disk (`public`/`local`) | **Reuse** |
| `GET` | `/api/teacher/quizzes/{quizId}/audit-logs` | `TeacherAssessmentController@quizAuditLogs` | `auth:sanctum`, `role:teacher,admin` + `teacherCanManage` | Path: `quizId` | `{ success, data: [...] }` | `quiz_override_logs`, `students`, `users` | **Reuse** (complemented by universal `/api/teacher/assessments/audit-logs`) |

---

## Universal Reopen, Retake & Late Submission Endpoints (New Unified Surface)

All endpoints reside under `/api/*`, require `auth:sanctum`, enforce role & offering/student ownership server-side, and return `{ "success": bool, "message": string, "data": ..., "errors"?: ... }`.

### Assessment Identification Rules
- `assessment_type`: `assignment` | `homework` | `quiz`
  - `assignment` -> `assignments` row where `assignment_type != 'homework'`
  - `homework` -> `assignments` row where `assignment_type == 'homework'`
  - `quiz` -> `quizzes` row
- `class_id`: derived from `$assessment->courseOffering?->class_id` (or `$student->class_id`).

### 1. Get Reopen Status
- **Teacher/Admin:** `GET /api/teacher/assessments/{assessmentType}/{assessmentId}/reopen-status?student_id={id?}`
- **Student (own only):** `GET /api/student/assessments/{assessmentType}/{assessmentId}/reopen-status`
- **Statuses distinguished (`reopen_status`):**
  `Not Submitted`, `In Progress`, `Submitted`, `Late Submitted`, `Reopened`, `Extended`, `Expired`
- **Core fields per student:** `assessment_type`, `assessment_id`, `class_id`, `student_id`, `original_deadline`, `extended_deadline`, `effective_deadline`, `student_submission_status`, `reopen_status`, `original_attempt_limit`, `additional_attempts`, `effective_attempt_limit`, `used_attempts`, `remaining_attempts`, `allow_submission`, `allow_late_submission`, `allow_resubmission`, `can_submit`, `can_resubmit`, `has_active_override`, `blocked_reason`, `override`.

### 2. Reopen Assessment
- **Endpoint:** `POST /api/teacher/assessments/reopen`
- **Request:** `{ assessment_type, assessment_id, student_ids?: int[], scope?: 'students'|'assessment', allow_submission?, allow_resubmission?, allow_late_submission?, additional_attempts?, reset_attempts?, new_deadline?, reason?, teacher_notes? }`
- **Behavior:** Supports never-submitted, already-submitted/graded, and `in_progress` students without altering unselected students.

### 3. Extend Deadline
- **Endpoint:** `POST /api/teacher/assessments/extend-deadline`
- **Request:** `{ assessment_type, assessment_id, student_ids: int[], new_deadline, reason?, teacher_notes? }`
- **Behavior:** Preserves original assessment deadline; stores student `extended_deadline` separately for selected `student_ids` only.

### 4. Grant Additional Attempts
- **Endpoint:** `POST /api/teacher/assessments/grant-attempts`
- **Request:** `{ assessment_type, assessment_id, student_id (or student_ids[]), additional_attempts: int(1..50), reason?, teacher_notes? }`
- **Behavior:** Increments `additional_attempts` cleanly for 0-attempt and limit-reached students while preserving existing submissions/attempts.

### 5. Revoke Reopen Permission
- **Endpoint:** `POST /api/teacher/assessments/revoke-reopen` (body: `{ assessment_type, assessment_id, student_ids: int[], reason? }`) and `DELETE /api/teacher/assessments/{assessmentType}/{assessmentId}/overrides/{studentId}`
- **Behavior:** Deactivates only selected students' active override (`is_active = false`), preserves all submissions/attempts/files, and enforces block immediately.

### 6. Get Submission History
- **Teacher/Admin:** `GET /api/teacher/assessments/{assessmentType}/{assessmentId}/submission-history?student_id={id?}`
- **Student (own only):** `GET /api/student/assessments/{assessmentType}/{assessmentId}/submission-history`
- **Response item fields:** `submission_id`, `assessment_type`, `assessment_id`, `student_id`, `submission_status`, `submitted_at`, `submitted_at_display`, `is_late`, `is_latest`, `version_number`, `uploaded_files` (`[{ file_name, file_extension, file_type, file_size, file_size_label, mime_type, preview_type, file_exists, view_url, download_url }]`), `score`, `max_score`, `feedback`, `is_published`, `submission_text`.

### 7. Get Quiz Attempt History
- **Teacher/Admin:** `GET /api/teacher/assessments/quiz/{assessmentId}/attempt-history?student_id={id?}`
- **Student (own only):** `GET /api/student/assessments/quiz/{assessmentId}/attempt-history`
- **Response fields:** `quiz_id`, `scoring_method` (`highest` | `latest` | `average`), `effective_score` (and per-student summary), `attempts: [{ attempt_id, quiz_id, student_id, attempt_number, started_at, submitted_at, score, total_marks, attempt_status, grading_status, is_late, is_reset, answers: [...] }]`.

### 8. Get Audit Logs
- **Endpoint:** `GET /api/teacher/assessments/audit-logs`
- **Filters:** `student_id`, `performed_by` (or `teacher_id`), `assessment_type`, `assessment_id`, `action_type`, `date_from`, `date_to`
- **Fields:** `audit_id`, `performed_by`, `performed_by_name`, `student_id`, `student_name`, `class_id`, `assessment_type`, `assessment_id`, `assessment_title`, `action_type`, `previous_deadline`, `new_deadline`, `previous_attempt_limit`, `new_attempt_limit`, `reason`, `teacher_notes`, `created_at`.
