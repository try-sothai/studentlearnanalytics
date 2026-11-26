package com.social7.studentlearnanalytics;

import com.social7.studentlearnanalytics.model.Exam;
import com.social7.studentlearnanalytics.teacher.assessment.ExamStudentScoreAdapter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ExamModuleUnitTest {

    @Test
    public void testExamModelInitializationAndGetters() {
        Exam exam = new Exam("1", "NOV", "25", "Physics", "Midterm • Room 301", false, null);
        exam.setTitle("Physics Midterm");
        exam.setCourseCode("PHY101");
        exam.setClassName("Class A");
        exam.setSemesterName("Fall 2026");
        exam.setExamType("Midterm");
        exam.setStatusLabel("Scheduled");
        exam.setRawStatus("scheduled");
        exam.setStartTime("09:00");
        exam.setEndTime("10:30");
        exam.setVenue("Room 301");
        exam.setDurationMinutes(90);
        exam.setExamDateFormatted("2026-11-25");
        exam.setDescription("Closed-book classroom paper examination.");
        exam.setMaxScore(100);
        exam.setPublished(false);

        assertEquals("1", exam.getId());
        assertEquals("NOV", exam.getDateMonth());
        assertEquals("25", exam.getDateDay());
        assertEquals("Physics", exam.getCourseName());
        assertEquals("Physics Midterm", exam.getTitle());
        assertEquals("PHY101", exam.getCourseCode());
        assertEquals("Class A", exam.getClassName());
        assertEquals("Fall 2026", exam.getSemesterName());
        assertEquals("Midterm", exam.getExamType());
        assertEquals("Scheduled", exam.getStatusLabel());
        assertEquals("09:00", exam.getStartTime());
        assertEquals("10:30", exam.getEndTime());
        assertEquals("Room 301", exam.getVenue());
        assertEquals(Integer.valueOf(90), exam.getDurationMinutes());
        assertEquals("2026-11-25", exam.getExamDateFormatted());
        assertEquals(100, exam.getMaxScore());
        assertNull(exam.getScoreText());
        assertFalse(exam.isCompleted());
        assertFalse(exam.isPublished());
    }

    @Test
    public void testExamCalculateTimeRangeAndAddMinutes() {
        assertEquals("09:00 – 10:30", Exam.calculateTimeRange("09:00", "10:30", 90));
        assertEquals("09:00 – 10:30", Exam.calculateTimeRange("09:00:00", "", 90));
        assertEquals("14:15", Exam.calculateTimeRange("14:15", null, null));
        assertEquals("", Exam.calculateTimeRange("", null, 90));
        assertEquals("15:30", Exam.addMinutesToTime("13:30", 120));
    }

    @Test
    public void testStudentPublishedScoreDisplay() {
        Exam publishedExam = new Exam("2", "DEC", "15", "Math", "Final • Room 204", true, "88.5 / 100");
        publishedExam.setPublished(true);
        publishedExam.setRawScore(88.5);
        publishedExam.setPercentage(88.5);
        publishedExam.setGrade("A-");
        publishedExam.setResultStatus("Pass");
        publishedExam.setRemarks("Well done");

        assertTrue(publishedExam.isCompleted());
        assertTrue(publishedExam.isPublished());
        assertEquals("88.5 / 100", publishedExam.getScoreText());
        assertEquals(Double.valueOf(88.5), publishedExam.getRawScore());
        assertEquals(Double.valueOf(88.5), publishedExam.getPercentage());
        assertEquals("A-", publishedExam.getGrade());
        assertEquals("Pass", publishedExam.getResultStatus());
        assertEquals("Well done", publishedExam.getRemarks());
    }

    @Test
    public void testStudentUnpublishedExamHidesScore() {
        Exam scheduledExam = new Exam("3", "NOV", "30", "Chemistry", "Midterm • Room 101", false, null);
        scheduledExam.setPublished(false);

        assertFalse(scheduledExam.isCompleted());
        assertFalse(scheduledExam.isPublished());
        assertNull(scheduledExam.getScoreText());
    }

    @Test
    public void testStudentScoreAdapterItemAndDraftValidation() {
        ExamStudentScoreAdapter.StudentScoreItem gradedItem = new ExamStudentScoreAdapter.StudentScoreItem(
                101, "STU001", "Alice Wonder", "alice@test.com", "Class A",
                true, 92.5, "A", "Outstanding", "2026-11-23T12:00:00Z"
        );

        assertEquals("92.5", gradedItem.draftScoreInput);
        assertFalse(gradedItem.isDirty);
        assertFalse(gradedItem.hasValidationError);

        // Same score string -> not dirty
        assertNotNull(gradedItem.applyDraftInput("92.5", 100.0));
        assertFalse(gradedItem.isDirty);
        assertFalse(gradedItem.hasValidationError);

        // Changed valid score -> dirty, valid
        Double updatedVal = gradedItem.applyDraftInput("88", 100.0);
        assertEquals(Double.valueOf(88.0), updatedVal);
        assertTrue(gradedItem.isDirty);
        assertFalse(gradedItem.hasValidationError);

        // Exceeding max marks -> invalid
        assertNull(gradedItem.applyDraftInput("105", 100.0));
        assertTrue(gradedItem.hasValidationError);

        // Negative score -> invalid
        assertNull(gradedItem.applyDraftInput("-2", 100.0));
        assertTrue(gradedItem.hasValidationError);

        // Clearing an already-graded student -> invalid
        assertNull(gradedItem.applyDraftInput("", 100.0));
        assertTrue(gradedItem.hasValidationError);

        // Pending student item
        ExamStudentScoreAdapter.StudentScoreItem pendingItem = new ExamStudentScoreAdapter.StudentScoreItem(
                102, "STU002", "Bob Builder", "bob@test.com", "Class A",
                false, null, null, null, null
        );
        assertEquals("", pendingItem.draftScoreInput);
        assertNull(pendingItem.applyDraftInput("", 100.0));
        assertFalse(pendingItem.isDirty);
        assertFalse(pendingItem.hasValidationError);

        assertEquals(Double.valueOf(76.0), pendingItem.applyDraftInput("76", 100.0));
        assertTrue(pendingItem.isDirty);
        assertFalse(pendingItem.hasValidationError);
    }

    @Test
    public void testGradeCalculationScale() {
        double totalMarks = 100.0;
        assertEquals("A", ExamStudentScoreAdapter.StudentScoreItem.calculateGrade(95.0, totalMarks));
        assertEquals("A-", ExamStudentScoreAdapter.StudentScoreItem.calculateGrade(88.0, totalMarks));
        assertEquals("B+", ExamStudentScoreAdapter.StudentScoreItem.calculateGrade(82.0, totalMarks));
        assertEquals("B", ExamStudentScoreAdapter.StudentScoreItem.calculateGrade(76.0, totalMarks));
        assertEquals("B-", ExamStudentScoreAdapter.StudentScoreItem.calculateGrade(71.0, totalMarks));
        assertEquals("C+", ExamStudentScoreAdapter.StudentScoreItem.calculateGrade(67.0, totalMarks));
        assertEquals("C", ExamStudentScoreAdapter.StudentScoreItem.calculateGrade(61.0, totalMarks));
        assertEquals("D", ExamStudentScoreAdapter.StudentScoreItem.calculateGrade(52.0, totalMarks));
        assertEquals("F", ExamStudentScoreAdapter.StudentScoreItem.calculateGrade(45.0, totalMarks));
    }
}
