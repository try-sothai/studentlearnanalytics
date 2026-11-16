# Student Learning Analytics System (SLAS)
## Teacher Module API Specification
**Specification Version:** 1.0.0  
**Phase:** Phase 0 – Project Audit & Architecture Mapping  
**Date:** November 11, 2026  
**Author:** Senior Backend & Security Architect  

---

## 1. Authentication & Security Architecture

### 1.1 Sanctum Bearer Authentication
All endpoints require a valid Sanctum bearer token in the `Authorization` header:
```http
Authorization: Bearer <sanctum_token>
Accept: application/json
```

### 1.2 Teacher Profile Resolution & Role Verification
1. User must possess the `teacher` or `admin` role (`EnsureUserHasRole` middleware).
2. Authenticated user's teacher record is resolved via `User::teacher` relationship.
3. If no active teacher profile exists for the user, return `404 Not Found` with message: `"Teacher profile not found."`
4. All database queries must be strictly scoped to offerings assigned to the authenticated teacher through active `course_offering_teachers` or active `courses.teacher_id` mappings.
5. If a teacher attempts to access an offering or class not assigned to them, return `403 Forbidden` with:
   ```json
   {
     "success": false,
     "message": "Unauthorized access to course offering records."
   }
   ```
6. **No Client Trust:** Any client-supplied `teacher_id`, `class_id`, or `course_offering_id` must be re-validated on the server.

---

## 2. API Endpoints

### 2.1 GET `/api/teacher/dashboard`
Fetches the four primary dashboard card metrics for the authenticated teacher.

**Query Parameters (Optional):**
- `academic_year_id` (integer)
- `semester_id` (integer)

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Teacher dashboard summary retrieved successfully.",
  "data": {
    "teacher": {
      "id": 1,
      "teacher_code": "TCH001",
      "name": "Dr. Sarah Jenkins",
      "department": "Computer Science",
      "email": "sarah.jenkins@slas.edu"
    },
    "summary": {
      "total_students": 50,
      "my_courses": 2,
      "at_risk_students": 6,
      "pending_scores": 8
    }
  }
}
```

#### Metrics Calculation Logic:
- `total_students`: Distinct count of active enrolled students across offerings assigned to the teacher within the selected academic scope.
- `my_courses`: Distinct count of courses taught by the teacher in the active/selected semester.
- `at_risk_students`: Distinct count of enrolled students in the teacher's offerings flagged with `risk_level IN ('medium', 'high')` and `status IN ('open', 'active')`.
- `pending_scores`: Total pending items awaiting grading (ungraded assignment submissions + open attendance sessions needing marking).

---

### 2.2 GET `/api/teacher/filters`
Returns dynamic, cascading filter options populated exclusively from the offerings assigned to the authenticated teacher.

**Query Parameters (Optional):**
- `academic_year_id` (integer)
- `semester_id` (integer)

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Teacher filters retrieved successfully.",
  "data": {
    "generations": [
      { "id": 10, "name": "Generation 10" },
      { "id": 11, "name": "Generation 11" }
    ],
    "programs": [
      { "id": 1, "code": "BSCS", "name": "Bachelor of Science in Computer Science" }
    ],
    "academic_years": [
      { "id": 2, "name": "2026-2027", "status": "active" }
    ],
    "study_years": [
      { "year": 1, "name": "Year 1" },
      { "year": 2, "name": "Year 2" }
    ],
    "semesters": [
      { "id": 3, "name": "Semester 1", "semester_number": 1, "academic_year_id": 2, "status": "active" }
    ],
    "courses": [
      { "id": 1, "code": "CS101", "name": "Database Systems" }
    ],
    "classes": [
      { "id": 1, "code": "CS-Y1-2026-2027", "name": "Class K" },
      { "id": 2, "code": "CS-Y1-B-2026-2027", "name": "Class A" }
    ]
  }
}
```

---

### 2.3 GET `/api/teacher/analytics`
Calculates real student learning engagement comparing the four learning activities: **Attendance**, **Assignment**, **Homework**, and **Quiz**.

**Query Parameters:**
- `academic_year_id` (optional, integer)
- `semester_id` (optional, integer)
- `course_id` (optional, integer)
- `class_id` (optional, integer)
- `program_id` (optional, integer)
- `study_year` (optional, integer)

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Teacher analytics retrieved successfully.",
  "data": {
    "scope": {
      "academic_year": "2026-2027",
      "semester": "Semester 1",
      "course": "Database Systems",
      "class": "Class K"
    },
    "summary": {
      "total_students": 25,
      "my_courses": 1,
      "at_risk_students": 3,
      "pending_scores": 4
    },
    "engagement": [
      {
        "activity": "Attendance",
        "completed": 92.5,
        "late": 3.0,
        "missed": 4.5,
        "total_events": 400
      },
      {
        "activity": "Assignment",
        "completed": 84.0,
        "late": 6.0,
        "missed": 10.0,
        "total_events": 100
      },
      {
        "activity": "Homework",
        "completed": 88.0,
        "late": 4.0,
        "missed": 8.0,
        "total_events": 50
      },
      {
        "activity": "Quiz",
        "completed": 95.0,
        "late": 0.0,
        "missed": 5.0,
        "total_events": 100
      }
    ]
  }
}
```

#### Engagement Calculation Formulas:
1. **Attendance:**
   - Completed: `(Present Details / Total Expected Details) * 100`
   - Late: `(Late Details / Total Expected Details) * 100`
   - Missed: `(Absent Details / Total Expected Details) * 100`
   *(Excused absences excluded from missed count denominator or tracked cleanly)*
2. **Assignment:**
   - Completed: `(On-time Submissions / Eligible Submissions) * 100`
   - Late: `(Late Submissions / Eligible Submissions) * 100`
   - Missed: `((Eligible Submissions - Submitted) / Eligible Submissions) * 100`
3. **Homework:**
   - Calculated from assignments where `assignment_type = 'homework'` or flagged as homework.
4. **Quiz:**
   - Completed: `(Completed Quiz Attempts / Expected Quiz Attempts) * 100`
   - Late: `0.0` (quizzes close at `end_at`)
   - Missed: `((Expected Quiz Attempts - Completed) / Expected Quiz Attempts) * 100`

---

### 2.4 Supporting Course Offering & Record Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/teacher/course-offerings` | List all course offerings assigned to the teacher. |
| `GET` | `/api/teacher/course-offerings/{id}` | Detail of a single authorized course offering. |
| `GET` | `/api/teacher/course-offerings/{id}/students` | List enrolled students in the offering. |
| `GET` | `/api/teacher/course-offerings/{id}/attendance` | Attendance sessions & details for the offering. |
| `GET` | `/api/teacher/course-offerings/{id}/assignments` | Assignments and submission status for the offering. |
| `GET` | `/api/teacher/course-offerings/{id}/quizzes` | Quizzes and attempt status for the offering. |
| `GET` | `/api/teacher/course-offerings/{id}/scores` | Gradebook and pending scores for the offering. |
| `POST`| `/api/teacher/course-offerings/{id}/attendance` | Record/mark attendance session. |
| `POST`| `/api/teacher/course-offerings/{id}/assignments` | Create course assignment. |
| `POST`| `/api/teacher/course-offerings/{id}/quizzes` | Create course quiz. |
| `POST`| `/api/teacher/submissions/{id}/grade` | Grade an assignment submission. |
