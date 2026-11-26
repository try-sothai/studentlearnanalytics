package com.social7.studentlearnanalytics.admin.users;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AcademicDropdownHelperTest {

    private static final String REAL_LOOKUPS_JSON = "{\n"
            + "  \"data\": {\n"
            + "    \"departments\": [\n"
            + "      {\"id\": 3, \"code\": \"BA\", \"name\": \"Business Administration\"},\n"
            + "      {\"id\": 1, \"code\": \"CS\", \"name\": \"Computer Science\"},\n"
            + "      {\"id\": 4, \"code\": \"DS\", \"name\": \"Data Science\"},\n"
            + "      {\"id\": 14, \"code\": \"FOSAT\", \"name\": \"Faculty Of Science And Technology\"},\n"
            + "      {\"id\": 2, \"code\": \"IT\", \"name\": \"Information Technology\"}\n"
            + "    ],\n"
            + "    \"programs\": [\n"
            + "      {\"id\": 4, \"department_id\": 3, \"code\": \"BBA\", \"name\": \"Bachelor of Business Administration\", \"duration_years\": 4},\n"
            + "      {\"id\": 1, \"department_id\": 1, \"code\": \"BSCS\", \"name\": \"Bachelor of Science in Computer Science\", \"duration_years\": 4},\n"
            + "      {\"id\": 3, \"department_id\": 2, \"code\": \"BSIT\", \"name\": \"Bachelor of Science in Information Technology\", \"duration_years\": 4},\n"
            + "      {\"id\": 2, \"department_id\": 1, \"code\": \"BSE\", \"name\": \"Bachelor of Software Engineering\", \"duration_years\": 4},\n"
            + "      {\"id\": 8, \"department_id\": 3, \"code\": \"BA\", \"name\": \"Business Administration\", \"duration_years\": 4},\n"
            + "      {\"id\": 5, \"department_id\": 1, \"code\": \"CS\", \"name\": \"Computer Science\", \"duration_years\": 4},\n"
            + "      {\"id\": 7, \"department_id\": 2, \"code\": \"IT\", \"name\": \"Information Technology\", \"duration_years\": 4},\n"
            + "      {\"id\": 6, \"department_id\": 1, \"code\": \"SE\", \"name\": \"Software Engineering\", \"duration_years\": 4}\n"
            + "    ],\n"
            + "    \"academic_years\": [\n"
            + "      {\"id\": 5, \"name\": \"2027-2028\", \"status\": \"inactive\"},\n"
            + "      {\"id\": 4, \"name\": \"2024-2025\", \"status\": \"completed\"},\n"
            + "      {\"id\": 3, \"name\": \"2023-2024\", \"status\": \"completed\"},\n"
            + "      {\"id\": 2, \"name\": \"2026-2027\", \"status\": \"active\"},\n"
            + "      {\"id\": 1, \"name\": \"2025-2026\", \"status\": \"completed\"}\n"
            + "    ],\n"
            + "    \"semesters\": [\n"
            + "      {\"id\": 1, \"academic_year_id\": 1, \"name\": \"Semester 1\", \"semester_number\": 1},\n"
            + "      {\"id\": 2, \"academic_year_id\": 1, \"name\": \"Semester 2\", \"semester_number\": 2},\n"
            + "      {\"id\": 3, \"academic_year_id\": 2, \"name\": \"Semester 1\", \"semester_number\": 1},\n"
            + "      {\"id\": 4, \"academic_year_id\": 2, \"name\": \"Semester 2\", \"semester_number\": 2},\n"
            + "      {\"id\": 5, \"academic_year_id\": 3, \"name\": \"Semester 1\", \"semester_number\": 1},\n"
            + "      {\"id\": 6, \"academic_year_id\": 3, \"name\": \"Semester 2\", \"semester_number\": 2},\n"
            + "      {\"id\": 7, \"academic_year_id\": 4, \"name\": \"Semester 1\", \"semester_number\": 1},\n"
            + "      {\"id\": 8, \"academic_year_id\": 4, \"name\": \"Semester 2\", \"semester_number\": 2},\n"
            + "      {\"id\": 9, \"academic_year_id\": 5, \"name\": \"Semester 1\", \"semester_number\": 1},\n"
            + "      {\"id\": 10, \"academic_year_id\": 5, \"name\": \"Semester 2\", \"semester_number\": 2}\n"
            + "    ]\n"
            + "  }\n"
            + "}";

    @Test
    public void parseLookups_realMySqlPayload_parsesAllCollectionsAndOrdersActiveAcademicYearFirst() {
        JsonObject json = JsonParser.parseString(REAL_LOOKUPS_JSON).getAsJsonObject();
        AcademicDropdownHelper.LookupData data = AcademicDropdownHelper.parseLookups(json);

        assertEquals(5, data.faculties.size());
        assertEquals(8, data.majors.size());
        assertEquals(5, data.academicYears.size());
        assertEquals(10, data.semesters.size());

        // Active 2026-2027 (id=2) must be ordered first
        assertEquals(2, data.academicYears.get(0).id);
        assertEquals("2026-2027", data.academicYears.get(0).name);
    }

    @Test
    public void parseLookups_fallbackAliasesNullFieldsAndInactiveStatus_handledSafely() {
        String aliasPayload = "{\n"
                + "  \"data\": {\n"
                + "    \"faculties\": [\n"
                + "      {\"id\": 10, \"name\": \"Active Faculty\", \"code\": \"AF\", \"status\": \"active\"},\n"
                + "      {\"id\": 11, \"name\": \"Inactive Faculty\", \"code\": \"IF\", \"status\": \"inactive\"}\n"
                + "    ],\n"
                + "    \"majors\": [\n"
                + "      {\"id\": 20, \"faculty_id\": 10, \"name\": \"Valid Major\", \"code\": null, \"status\": \"active\"},\n"
                + "      {\"id\": 21, \"faculty_id\": 10, \"name\": \"Disabled Major\", \"code\": \"DM\", \"status\": \"disabled\"}\n"
                + "    ],\n"
                + "    \"academic_years\": [\n"
                + "      {\"id\": 1, \"name\": \"2026-2027\", \"status\": \"active\"}\n"
                + "    ],\n"
                + "    \"semesters\": [\n"
                + "      {\"id\": 30, \"academic_year_id\": 1, \"name\": null, \"semester_number\": null}\n"
                + "    ]\n"
                + "  }\n"
                + "}";

        JsonObject json = JsonParser.parseString(aliasPayload).getAsJsonObject();
        AcademicDropdownHelper.LookupData data = AcademicDropdownHelper.parseLookups(json);

        assertEquals(1, data.faculties.size());
        assertEquals(10, data.faculties.get(0).id);

        assertEquals(1, data.majors.size());
        assertEquals(20, data.majors.get(0).id);
        assertEquals(10, data.majors.get(0).parentId);
        assertEquals("", data.majors.get(0).code);

        assertEquals(1, data.semesters.size());
        assertEquals("Semester 30", data.semesters.get(0).name);
    }

    @Test
    public void buildFacultyOptions_prependsSelectFacultyPlaceholder() {
        JsonObject json = JsonParser.parseString(REAL_LOOKUPS_JSON).getAsJsonObject();
        AcademicDropdownHelper.LookupData data = AcademicDropdownHelper.parseLookups(json);

        List<AddStudentActivity.IdNameItem> options =
                AcademicDropdownHelper.buildFacultyOptions(data.faculties, "Select Faculty");

        assertEquals(6, options.size());
        assertEquals(0, options.get(0).id);
        assertEquals("Select Faculty", options.get(0).toString());
        assertEquals("Business Administration", options.get(1).name);
    }

    @Test
    public void buildMajorOptions_disabledWhenNoFacultySelected() {
        JsonObject json = JsonParser.parseString(REAL_LOOKUPS_JSON).getAsJsonObject();
        AcademicDropdownHelper.LookupData data = AcademicDropdownHelper.parseLookups(json);

        AcademicDropdownHelper.MajorOptionsResult nullResult = AcademicDropdownHelper.buildMajorOptions(
                null, data.majors, "Select a faculty first", "Select Major", "No majors available");
        assertFalse(nullResult.enabled);
        assertEquals(1, nullResult.items.size());
        assertEquals(0, nullResult.items.get(0).id);
        assertEquals("Select a faculty first", nullResult.items.get(0).name);

        AcademicDropdownHelper.MajorOptionsResult placeholderResult = AcademicDropdownHelper.buildMajorOptions(
                new AddStudentActivity.IdNameItem(0, "Select Faculty", "", 0),
                data.majors, "Select a faculty first", "Select Major", "No majors available");
        assertFalse(placeholderResult.enabled);
        assertEquals(1, placeholderResult.items.size());
        assertEquals("Select a faculty first", placeholderResult.items.get(0).name);
    }

    @Test
    public void buildMajorOptions_filtersByFacultyAndHandlesFacultyChangeAndEmptyFaculties() {
        JsonObject json = JsonParser.parseString(REAL_LOOKUPS_JSON).getAsJsonObject();
        AcademicDropdownHelper.LookupData data = AcademicDropdownHelper.parseLookups(json);

        // Business Administration (id=3) -> BBA & BA only
        AddStudentActivity.IdNameItem ba = new AddStudentActivity.IdNameItem(3, "Business Administration", "BA", 0);
        AcademicDropdownHelper.MajorOptionsResult baResult = AcademicDropdownHelper.buildMajorOptions(
                ba, data.majors, "Select a faculty first", "Select Major", "No majors available");
        assertTrue(baResult.enabled);
        assertEquals(3, baResult.items.size()); // placeholder + 2 majors
        assertEquals(0, baResult.items.get(0).id);
        assertEquals("Select Major", baResult.items.get(0).name);
        assertTrue(baResult.items.stream().anyMatch(m -> m.id == 4 && "BBA".equals(m.code)));
        assertTrue(baResult.items.stream().anyMatch(m -> m.id == 8 && "BA".equals(m.code)));

        // Switching to Computer Science (id=1) reloads computing majors and resets selection to index 0 placeholder
        AddStudentActivity.IdNameItem cs = new AddStudentActivity.IdNameItem(1, "Computer Science", "CS", 0);
        AcademicDropdownHelper.MajorOptionsResult csResult = AcademicDropdownHelper.buildMajorOptions(
                cs, data.majors, "Select a faculty first", "Select Major", "No majors available");
        assertTrue(csResult.enabled);
        assertEquals(0, csResult.items.get(0).id);
        assertEquals("Select Major", csResult.items.get(0).name);
        assertEquals(7, csResult.items.size()); // placeholder + 6 computing majors (BSCS, BSE, CS, SE, BSIT, IT)

        // Faculties without majors (DS id=4, FOSAT id=14) disable Major spinner cleanly
        AddStudentActivity.IdNameItem ds = new AddStudentActivity.IdNameItem(4, "Data Science", "DS", 0);
        AcademicDropdownHelper.MajorOptionsResult dsResult = AcademicDropdownHelper.buildMajorOptions(
                ds, data.majors, "Select a faculty first", "Select Major", "No majors available");
        assertFalse(dsResult.enabled);
        assertEquals(1, dsResult.items.size());
        assertEquals(0, dsResult.items.get(0).id);
        assertEquals("No majors available", dsResult.items.get(0).name);

        AddStudentActivity.IdNameItem fosat = new AddStudentActivity.IdNameItem(14, "Faculty Of Science And Technology", "FOSAT", 0);
        AcademicDropdownHelper.MajorOptionsResult fosatResult = AcademicDropdownHelper.buildMajorOptions(
                fosat, data.majors, "Select a faculty first", "Select Major", "No majors available");
        assertFalse(fosatResult.enabled);
        assertEquals(1, fosatResult.items.size());
        assertEquals("No majors available", fosatResult.items.get(0).name);
    }

    @Test
    public void buildAcademicYearOptions_prependsPlaceholderAndPreservesActiveFirst() {
        JsonObject json = JsonParser.parseString(REAL_LOOKUPS_JSON).getAsJsonObject();
        AcademicDropdownHelper.LookupData data = AcademicDropdownHelper.parseLookups(json);

        List<AddStudentActivity.IdNameItem> options =
                AcademicDropdownHelper.buildAcademicYearOptions(data.academicYears, "Select Academic Year");

        assertEquals(6, options.size());
        assertEquals(0, options.get(0).id);
        assertEquals("Select Academic Year", options.get(0).name);
        assertEquals(2, options.get(1).id);
        assertEquals("2026-2027", options.get(1).name);
    }

    @Test
    public void buildSemesterOptions_filtersByAcademicYearAndAlwaysAllowsEmptySelection() {
        JsonObject json = JsonParser.parseString(REAL_LOOKUPS_JSON).getAsJsonObject();
        AcademicDropdownHelper.LookupData data = AcademicDropdownHelper.parseLookups(json);

        // No academic year selected -> only None (Optional)
        List<AddStudentActivity.IdNameItem> noneSelected = AcademicDropdownHelper.buildSemesterOptions(
                null, data.semesters, "None (Optional)");
        assertEquals(1, noneSelected.size());
        assertEquals(0, noneSelected.get(0).id);
        assertEquals("None (Optional)", noneSelected.get(0).name);

        // Academic Year 2026-2027 (id=2) -> None (Optional), Semester 1 (id=3), Semester 2 (id=4)
        AddStudentActivity.IdNameItem ay2026 = new AddStudentActivity.IdNameItem(2, "2026-2027", "", 0);
        List<AddStudentActivity.IdNameItem> ay2Options = AcademicDropdownHelper.buildSemesterOptions(
                ay2026, data.semesters, "None (Optional)");
        assertEquals(3, ay2Options.size());
        assertEquals(0, ay2Options.get(0).id);
        assertEquals(3, ay2Options.get(1).id);
        assertEquals(4, ay2Options.get(2).id);

        // Switching to Academic Year 2025-2026 (id=1) -> None (Optional), Semester 1 (id=1), Semester 2 (id=2)
        AddStudentActivity.IdNameItem ay2025 = new AddStudentActivity.IdNameItem(1, "2025-2026", "", 0);
        List<AddStudentActivity.IdNameItem> ay1Options = AcademicDropdownHelper.buildSemesterOptions(
                ay2025, data.semesters, "None (Optional)");
        assertEquals(3, ay1Options.size());
        assertEquals(0, ay1Options.get(0).id);
        assertEquals(1, ay1Options.get(1).id);
        assertEquals(2, ay1Options.get(2).id);
    }

    @Test
    public void validateAcademicSelection_requiresFacultyMajorYearLevelAcademicYear_allowsEmptySemester() {
        AddStudentActivity.IdNameItem validDept = new AddStudentActivity.IdNameItem(1, "Computer Science", "CS", 0);
        AddStudentActivity.IdNameItem placeholderDept = new AddStudentActivity.IdNameItem(0, "Select Faculty", "", 0);
        AddStudentActivity.IdNameItem validProg = new AddStudentActivity.IdNameItem(1, "BSCS", "BSCS", 1);
        AddStudentActivity.IdNameItem placeholderProg = new AddStudentActivity.IdNameItem(0, "Select Major", "", 0);
        AddStudentActivity.IdNameItem validAy = new AddStudentActivity.IdNameItem(2, "2026-2027", "", 0);
        AddStudentActivity.IdNameItem placeholderAy = new AddStudentActivity.IdNameItem(0, "Select Academic Year", "", 0);

        assertEquals("faculty", AcademicDropdownHelper.validateAcademicSelection(placeholderDept, validProg, 1, validAy));
        assertEquals("major", AcademicDropdownHelper.validateAcademicSelection(validDept, placeholderProg, 1, validAy));
        assertEquals("year_level", AcademicDropdownHelper.validateAcademicSelection(validDept, validProg, null, validAy));
        assertEquals("year_level", AcademicDropdownHelper.validateAcademicSelection(validDept, validProg, 5, validAy));
        assertEquals("academic_year", AcademicDropdownHelper.validateAcademicSelection(validDept, validProg, 1, placeholderAy));

        assertNull(AcademicDropdownHelper.validateAcademicSelection(validDept, validProg, 1, validAy));
        assertNotNull(AcademicDropdownHelper.buildSemesterOptions(validAy, Collections.emptyList(), "None (Optional)").get(0));
    }

    @Test
    public void parseMajorsResponse_andBuildMajorOptionsFromApiResult_handleEmptyAndPopulatedResponses() {
        // Empty response from GET /api/departments/14/programs
        String emptyJson = "{\"message\":\"Majors retrieved successfully.\",\"data\":[],\"meta\":{\"department_id\":14,\"faculty_id\":14,\"faculty_code\":\"FOSAT\",\"count\":0}}";
        List<AddStudentActivity.IdNameItem> emptyParsed = AcademicDropdownHelper.parseMajorsResponse(
                JsonParser.parseString(emptyJson).getAsJsonObject());
        assertTrue(emptyParsed.isEmpty());

        AddStudentActivity.IdNameItem fosat = new AddStudentActivity.IdNameItem(14, "Faculty Of Science And Technology", "FOSAT", 0);
        AcademicDropdownHelper.MajorOptionsResult emptyResult = AcademicDropdownHelper.buildMajorOptionsFromApiResult(
                fosat, emptyParsed, "Select a faculty first", "Select Major", "No majors available for this faculty");
        assertFalse(emptyResult.enabled);
        assertEquals(1, emptyResult.items.size());
        assertEquals("No majors available for this faculty", emptyResult.items.get(0).name);

        // Populated response from GET /api/departments/{id}/programs (filtering inactive + null safe)
        String populatedJson = "{\n"
                + "  \"message\": \"Majors retrieved successfully.\",\n"
                + "  \"data\": [\n"
                + "    {\"id\": 9, \"department_id\": 14, \"faculty_id\": 14, \"code\": \"FOSAT-CS\", \"name\": \"Computer Science\", \"status\": \"active\"},\n"
                + "    {\"id\": 10, \"department_id\": 14, \"faculty_id\": 14, \"code\": \"AP\", \"name\": \"Applied Physics\", \"status\": \"active\"},\n"
                + "    {\"id\": 11, \"department_id\": 14, \"faculty_id\": 14, \"code\": \"OLD\", \"name\": \"Inactive Major\", \"status\": \"inactive\"}\n"
                + "  ]\n"
                + "}";
        List<AddStudentActivity.IdNameItem> parsed = AcademicDropdownHelper.parseMajorsResponse(
                JsonParser.parseString(populatedJson).getAsJsonObject());
        assertEquals(2, parsed.size());
        assertEquals("Applied Physics", parsed.get(0).name);
        assertEquals("Computer Science", parsed.get(1).name);

        AcademicDropdownHelper.MajorOptionsResult populatedResult = AcademicDropdownHelper.buildMajorOptionsFromApiResult(
                fosat, parsed, "Select a faculty first", "Select Major", "No majors available for this faculty");
        assertTrue(populatedResult.enabled);
        assertEquals(3, populatedResult.items.size());
        assertEquals(0, populatedResult.items.get(0).id);
        assertEquals("Select Major", populatedResult.items.get(0).name);
        assertEquals(10, populatedResult.items.get(1).id);
        assertEquals(9, populatedResult.items.get(2).id);

        // Null faculty -> disabled with "Select a faculty first"
        AcademicDropdownHelper.MajorOptionsResult noFaculty = AcademicDropdownHelper.buildMajorOptionsFromApiResult(
                null, parsed, "Select a faculty first", "Select Major", "No majors available for this faculty");
        assertFalse(noFaculty.enabled);
        assertEquals("Select a faculty first", noFaculty.items.get(0).name);
    }
}
