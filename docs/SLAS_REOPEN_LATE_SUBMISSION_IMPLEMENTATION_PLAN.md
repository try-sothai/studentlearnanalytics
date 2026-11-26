# Student Learning Analytics System (SLAS)
## Reopen & Late Submission Management - Android Implementation Plan
**Note:** This repository only contains the Android application (`studentlearnanalytics`). The Laravel backend code is not present in this directory. This plan covers the Android implementation. Backend API changes must be implemented in the respective repository.

---

## 1. Overview
The goal is to implement the Reopen & Late Submission Management System in the Android app for Assignments, Homework, and Quizzes, allowing Teachers to manage submissions after deadlines, and Students to submit based on granted permissions.

## 2. Open Questions & Missing Context
- **Backend Codebase:** The Laravel backend code is not in this project directory. I can only implement the Android application changes. Please confirm if you want me to proceed with just the Android app, or if you can provide access to the backend repository.
- **Backend API Endpoints:** We need to define the exact API endpoints and JSON payloads for the new Reopen/Late Submission features so I can add them to `ApiService.java`.

## 3. Proposed Changes (Android Side)

### 3.1 Network & Models (com.social7.studentlearnanalytics)
- **`model/Assessment.java` & `model/AssessmentStudent.java`**: Add fields for `reopenStatus`, `extendedDeadline`, `additionalAttempts`, `isLateSubmissionAllowed`.
- **`model/Submission.java`**: Add fields for `attemptNumber`, `isLate`, `isResubmission`.
- **`network/ApiService.java`**: Add endpoints:
  - `POST /api/teacher/assessments/{id}/reopen`
  - `POST /api/teacher/assessments/{id}/revoke-reopen`
  - `GET /api/teacher/assessments/{id}/students-status`

### 3.2 Teacher UI (com.social7.studentlearnanalytics.teacher)
- **`TeacherAssessmentFragment.java` & Adapter**: Add a "Manage Submissions/Reopen" action for Assessments.
- **`SubmissionAdapter.java`**: Update to show which students have reopen permissions, and display multiple attempts/submissions.
- **New `ReopenAssessmentDialog.java`**: A dialog to configure:
  - Select student(s)
  - New Deadline
  - Additional Attempts
  - Allow Late / Resubmission
  - Reason (Teacher Notes)

### 3.3 Student UI (com.social7.studentlearnanalytics.student)
- **`StudentAssessmentFragment.java`**: Update UI to reflect "Reopened" or "Extended Deadline" status.
- **`AssessmentDetailActivity.java` / `SubmitAssignmentActivity.java`**: 
  - Remove strict deadline locks if the student has reopen permissions.
  - Display "Late Submission" or "Attempt X of Y" if applicable.
  - Allow file uploads if `reopenStatus == true`.

## 4. Verification Plan
- **Mocking APIs**: Since the backend is not here, we can create mock responses or test the UI flows using dummy data.
- **UI Tests**: Verify the Reopen Settings dialog appears and captures the correct data.
- **Student Flow**: Verify that a past-deadline assessment becomes actionable again when the reopen flag is true.
