package com.social7.studentlearnanalytics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.model.Assessment;
import com.social7.studentlearnanalytics.model.Exam;
import com.social7.studentlearnanalytics.model.QuizQuestionItem;
import com.social7.studentlearnanalytics.model.TeachingAssignment;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AssessmentIntegrationModelTest {

    @Test
    public void teachingAssignmentParsesAcademicContextCorrectly() {
        String json = "{"
                + "\"course_offering_id\": 12,"
                + "\"academic_year_id\": 1,"
                + "\"academic_year_name\": \"2025-2026\","
                + "\"semester_id\": 2,"
                + "\"semester_name\": \"Semester 1\","
                + "\"class_id\": 4,"
                + "\"class_name\": \"CS Year 2 - Section A\","
                + "\"class_code\": \"CS-Y2-A\","
                + "\"course_id\": 9,"
                + "\"course_name\": \"Data Structures\","
                + "\"course_code\": \"CS201\","
                + "\"student_count\": 28,"
                + "\"status\": \"active\""
                + "}";
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        TeachingAssignment ta = TeachingAssignment.fromJson(obj);

        assertEquals(12, ta.getCourseOfferingId());
        assertEquals(1, ta.getAcademicYearId());
        assertEquals("2025-2026", ta.getAcademicYearName());
        assertEquals(2, ta.getSemesterId());
        assertEquals("Semester 1", ta.getSemesterName());
        assertEquals(4, ta.getClassId());
        assertEquals("CS Year 2 - Section A", ta.getClassName());
        assertEquals(9, ta.getCourseId());
        assertEquals("CS201 – Data Structures", ta.getCourseDisplayLabel());
        assertEquals(28, ta.getStudentCount());
    }

    @Test
    public void quizQuestionItemParsesAllSupportedTypes() {
        JsonObject mc = JsonParser.parseString("{"
                + "\"id\": 5, \"quiz_id\": 2, \"question_order\": 1,"
                + "\"question_text\": \"What is O(1)?\", \"question_type\": \"multiple_choice\","
                + "\"marks\": 2.5, \"option_a\": \"Constant\", \"option_b\": \"Linear\","
                + "\"option_c\": null, \"option_d\": null, \"correct_answer\": \"A\""
                + "}").getAsJsonObject();
        QuizQuestionItem mcItem = QuizQuestionItem.fromJson(mc);
        assertEquals("Multiple Choice", mcItem.getTypeLabel());
        assertEquals(2, mcItem.optionCount);
        assertEquals(2.5, mcItem.marks, 0.001);
        assertEquals("A", mcItem.correctAnswer);

        JsonObject tf = JsonParser.parseString("{"
                + "\"id\": 6, \"quiz_id\": 2, \"question_order\": 2,"
                + "\"question_text\": \"Java is statically typed.\", \"question_type\": \"true_false\","
                + "\"marks\": 1, \"correct_answer\": \"true\""
                + "}").getAsJsonObject();
        QuizQuestionItem tfItem = QuizQuestionItem.fromJson(tf);
        assertEquals("True / False", tfItem.getTypeLabel());
        assertEquals(0, tfItem.optionCount);

        JsonObject sa = JsonParser.parseString("{"
                + "\"id\": 7, \"quiz_id\": 2, \"question_order\": 3,"
                + "\"question_text\": \"Explain polymorphism.\", \"question_type\": \"short_answer\","
                + "\"marks\": 5, \"correct_answer\": null"
                + "}").getAsJsonObject();
        QuizQuestionItem saItem = QuizQuestionItem.fromJson(sa);
        assertEquals("Short Answer", saItem.getTypeLabel());
        assertNull(saItem.correctAnswer);
    }

    @Test
    public void assessmentAndExamStatusFormattingRespectsBackendState() {
        Assessment graded = new Assessment("1", "HW 1", "Due: Oct 10", 18, 20, "homework", "Semester 1", "CS101", true, true);
        graded.setStatusLabel("Graded");
        assertEquals("18 / 20", graded.getFormattedScore());

        Assessment inProg = new Assessment("2", "Quiz 1", "Oct 12", 0, 20, "quiz", "Semester 1", "CS101", false, false);
        inProg.setInProgress(true);
        inProg.setStatusLabel("In Progress");
        assertEquals("In Progress", inProg.getFormattedScore());

        Assessment overdue = new Assessment("3", "Lab 2", "Oct 01", 0, 50, "assignment", "Semester 1", "CS101", false, false);
        overdue.setOverdue(true);
        assertEquals("Overdue", overdue.getFormattedScore());

        Exam exam = new Exam("9", "NOV", "15", "CS201 – Midterm", "Midterm • Scheduled", false, null);
        exam.setVenue("Room 301");
        exam.setDurationMinutes(90);
        exam.setStatusLabel("Scheduled");
        assertFalse(exam.isCompleted());
        assertEquals("Room 301", exam.getVenue());
        assertEquals(Integer.valueOf(90), exam.getDurationMinutes());
    }

    @Test
    public void helperParsesIsoDatesAndMarksCleanly() {
        assertNotNull(AssessmentUiHelper.parseIsoDate("2026-11-21T00:00:00.000000Z"));
        assertNotNull(AssessmentUiHelper.parseIsoDate("2026-12-31 23:59:59"));
        assertEquals("10", AssessmentUiHelper.formatMarks(10.0));
        assertEquals("2.5", AssessmentUiHelper.formatMarks(2.5));
        assertTrue(AssessmentUiHelper.isPast("2020-01-01T00:00:00Z"));
        assertFalse(AssessmentUiHelper.isFuture("2020-01-01T00:00:00Z"));
    }
}
