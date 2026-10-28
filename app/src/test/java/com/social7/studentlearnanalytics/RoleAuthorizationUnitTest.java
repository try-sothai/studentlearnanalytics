package com.social7.studentlearnanalytics;

import org.junit.Test;
import static org.junit.Assert.*;

public class RoleAuthorizationUnitTest {

    @Test
    public void testRoleComparisonCaseInsensitivity() {
        assertTrue("Student".equalsIgnoreCase("student"));
        assertTrue("Teacher".equalsIgnoreCase("TEACHER"));
        assertTrue("Admin".equalsIgnoreCase("admin"));
    }

    @Test
    public void testStudentRoleAuthorization() {
        String currentRole = "Student";
        assertTrue(isAuthorized(currentRole, "Student"));
        assertFalse(isAuthorized(currentRole, "Teacher"));
        assertFalse(isAuthorized(currentRole, "Admin"));
    }

    @Test
    public void testTeacherRoleAuthorization() {
        String currentRole = "teacher";
        assertFalse(isAuthorized(currentRole, "Student"));
        assertTrue(isAuthorized(currentRole, "Teacher"));
        assertFalse(isAuthorized(currentRole, "Admin"));
        assertTrue(isAuthorized(currentRole, "Teacher", "Admin"));
    }

    @Test
    public void testAdminRoleAuthorization() {
        String currentRole = "ADMIN";
        assertFalse(isAuthorized(currentRole, "Student"));
        assertFalse(isAuthorized(currentRole, "Teacher"));
        assertTrue(isAuthorized(currentRole, "Admin"));
    }

    @Test
    public void testUnknownRoleRejection() {
        String currentRole = "SuperUser";
        assertFalse(isAuthorized(currentRole, "Student"));
        assertFalse(isAuthorized(currentRole, "Teacher"));
        assertFalse(isAuthorized(currentRole, "Admin"));
    }

    @Test
    public void testNullOrEmptyRoleRejection() {
        assertFalse(isAuthorized(null, "Student", "Teacher", "Admin"));
        assertFalse(isAuthorized("", "Student", "Teacher", "Admin"));
    }

    private boolean isAuthorized(String currentRole, String... allowedRoles) {
        if (currentRole == null || currentRole.trim().isEmpty()) {
            return false;
        }
        for (String allowed : allowedRoles) {
            if (allowed != null && allowed.trim().equalsIgnoreCase(currentRole.trim())) {
                return true;
            }
        }
        return false;
    }
}
