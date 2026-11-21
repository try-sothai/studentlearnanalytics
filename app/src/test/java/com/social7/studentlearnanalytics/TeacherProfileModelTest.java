package com.social7.studentlearnanalytics;

import com.social7.studentlearnanalytics.model.Teacher;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TeacherProfileModelTest {

    @Test
    public void teacherOptionalFieldsDoNotInjectFakeEditDefaults() {
        Teacher teacher = new Teacher("TCH999", "Sokha Dara", "sokha@slas.edu", "Computer Science", "Lecturer");

        assertEquals("", teacher.getRawOfficeLocation());
        assertEquals("", teacher.getRawBio());
        assertEquals("Not set", teacher.getOfficeLocation());
        assertEquals("Not set", teacher.getBio());
        assertEquals("Not set", teacher.getSpecializationDisplay());
        assertTrue(teacher.getSpecializations().isEmpty());
    }

    @Test
    public void teacherSupportsMultipleSpecializationsAndUnicodeBiography() {
        Teacher teacher = new Teacher("TCH998", "ចាន់ សុភា", "sophea@slas.edu", "Computer Science", "Senior Lecturer");
        teacher.setOfficeLocationId(3);
        teacher.setOfficeLocation("Building A, Room 103");
        teacher.setSpecializations(Arrays.asList("Data Structures", "Algorithms", "វិស្វកម្មសូហ្វវែរ"));
        String bio = "សាស្ត្រាចារ្យជំនាញវិទ្យាសាស្ត្រកុំព្យូទ័រ។\nSpecializes in Distributed Systems.";
        teacher.setBio(bio);

        assertEquals(Integer.valueOf(3), teacher.getOfficeLocationId());
        assertEquals("Building A, Room 103", teacher.getRawOfficeLocation());
        assertEquals(3, teacher.getSpecializations().size());
        assertEquals("Data Structures, Algorithms, វិស្វកម្មសូហ្វវែរ", teacher.getSpecializationDisplay());
        assertEquals(bio, teacher.getRawBio());

        teacher.setSpecializations(Collections.emptyList());
        assertTrue(teacher.getSpecializations().isEmpty());
        assertEquals("Not set", teacher.getSpecializationDisplay());
    }
}
