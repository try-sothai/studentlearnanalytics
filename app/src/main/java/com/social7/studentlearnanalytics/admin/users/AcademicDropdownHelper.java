package com.social7.studentlearnanalytics.admin.users;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Pure-Java helper for parsing SLAS academic lookup payloads and building
 * dependent dropdown options (Faculty -> Major, Academic Year -> Semester).
 */
public final class AcademicDropdownHelper {

    private static final Set<String> COMPUTING_PROGRAM_CODES = new HashSet<>(
            Arrays.asList("CS", "BSCS", "SE", "BSE", "IT", "BSIT")
    );

    private AcademicDropdownHelper() {
    }

    public static class LookupData {
        public final List<AddStudentActivity.IdNameItem> faculties;
        public final List<AddStudentActivity.IdNameItem> majors;
        public final List<AddStudentActivity.IdNameItem> academicYears;
        public final List<AddStudentActivity.IdNameItem> semesters;

        public LookupData(
                List<AddStudentActivity.IdNameItem> faculties,
                List<AddStudentActivity.IdNameItem> majors,
                List<AddStudentActivity.IdNameItem> academicYears,
                List<AddStudentActivity.IdNameItem> semesters
        ) {
            this.faculties = faculties != null ? faculties : Collections.emptyList();
            this.majors = majors != null ? majors : Collections.emptyList();
            this.academicYears = academicYears != null ? academicYears : Collections.emptyList();
            this.semesters = semesters != null ? semesters : Collections.emptyList();
        }
    }

    public static class MajorOptionsResult {
        public final List<AddStudentActivity.IdNameItem> items;
        public final boolean enabled;

        public MajorOptionsResult(List<AddStudentActivity.IdNameItem> items, boolean enabled) {
            this.items = items != null ? items : Collections.emptyList();
            this.enabled = enabled;
        }
    }

    private static class AcademicYearEntry {
        final AddStudentActivity.IdNameItem item;
        final boolean active;

        AcademicYearEntry(AddStudentActivity.IdNameItem item, boolean active) {
            this.item = item;
            this.active = active;
        }
    }

    private static class SemesterEntry {
        final AddStudentActivity.IdNameItem item;
        final int semesterNumber;

        SemesterEntry(AddStudentActivity.IdNameItem item, int semesterNumber) {
            this.item = item;
            this.semesterNumber = semesterNumber;
        }
    }

    public static LookupData parseLookups(JsonObject responseBody) {
        if (responseBody == null) {
            return new LookupData(Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList());
        }

        JsonObject data = responseBody.has("data") && responseBody.get("data").isJsonObject()
                ? responseBody.getAsJsonObject("data")
                : responseBody;

        List<AddStudentActivity.IdNameItem> faculties = parseFaculties(data);
        List<AddStudentActivity.IdNameItem> majors = parseMajors(data);
        List<AddStudentActivity.IdNameItem> academicYears = parseAcademicYears(data);
        List<AddStudentActivity.IdNameItem> semesters = parseSemesters(data);

        return new LookupData(faculties, majors, academicYears, semesters);
    }

    public static List<AddStudentActivity.IdNameItem> parseMajorsResponse(JsonObject responseBody) {
        if (responseBody == null) {
            return Collections.emptyList();
        }
        if (responseBody.has("data")) {
            JsonElement dataEl = responseBody.get("data");
            if (dataEl.isJsonArray()) {
                return parseMajorArray(dataEl.getAsJsonArray());
            }
            if (dataEl.isJsonObject()) {
                return parseMajors(dataEl.getAsJsonObject());
            }
        }
        return parseMajors(responseBody);
    }

    private static List<AddStudentActivity.IdNameItem> parseFaculties(JsonObject data) {
        JsonArray arr = getArray(data, "departments", "faculties");
        List<AddStudentActivity.IdNameItem> result = new ArrayList<>();
        if (arr == null) {
            return result;
        }

        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            int id = optInt(o, "id", 0);
            if (id <= 0 || isInactiveStatus(optString(o, "status", ""))) {
                continue;
            }
            String name = optString(o, "name", "");
            String code = optString(o, "code", "");
            result.add(new AddStudentActivity.IdNameItem(id, name, code, 0));
        }

        result.sort(Comparator.comparing(item -> item.name.toLowerCase(Locale.ROOT)));
        return result;
    }

    private static List<AddStudentActivity.IdNameItem> parseMajors(JsonObject data) {
        return parseMajorArray(getArray(data, "programs", "majors"));
    }

    private static List<AddStudentActivity.IdNameItem> parseMajorArray(JsonArray arr) {
        List<AddStudentActivity.IdNameItem> result = new ArrayList<>();
        if (arr == null) {
            return result;
        }

        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            int id = optInt(o, "id", 0);
            if (id <= 0 || isInactiveStatus(optString(o, "status", ""))) {
                continue;
            }
            String name = optString(o, "name", "");
            String code = optString(o, "code", "");
            int deptId = optInt(o, "department_id", 0);
            if (deptId <= 0) {
                deptId = optInt(o, "faculty_id", 0);
            }
            result.add(new AddStudentActivity.IdNameItem(id, name, code, deptId));
        }

        result.sort(Comparator.comparing(item -> item.name.toLowerCase(Locale.ROOT)));
        return result;
    }

    private static List<AddStudentActivity.IdNameItem> parseAcademicYears(JsonObject data) {
        JsonArray arr = getArray(data, "academic_years", null);
        List<AcademicYearEntry> entries = new ArrayList<>();
        if (arr == null) {
            return new ArrayList<>();
        }

        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            int id = optInt(o, "id", 0);
            if (id <= 0) {
                continue;
            }
            String name = optString(o, "name", "");
            String status = optString(o, "status", "").trim();
            boolean active = "active".equalsIgnoreCase(status);
            entries.add(new AcademicYearEntry(new AddStudentActivity.IdNameItem(id, name, "", 0), active));
        }

        entries.sort((a, b) -> {
            if (a.active != b.active) {
                return a.active ? -1 : 1;
            }
            int nameCmp = b.item.name.compareToIgnoreCase(a.item.name);
            if (nameCmp != 0) {
                return nameCmp;
            }
            return Integer.compare(b.item.id, a.item.id);
        });

        List<AddStudentActivity.IdNameItem> result = new ArrayList<>(entries.size());
        for (AcademicYearEntry entry : entries) {
            result.add(entry.item);
        }
        return result;
    }

    private static List<AddStudentActivity.IdNameItem> parseSemesters(JsonObject data) {
        JsonArray arr = getArray(data, "semesters", null);
        List<SemesterEntry> entries = new ArrayList<>();
        if (arr == null) {
            return new ArrayList<>();
        }

        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            int id = optInt(o, "id", 0);
            if (id <= 0) {
                continue;
            }
            int semNum = optInt(o, "semester_number", 0);
            String name = optString(o, "name", "").trim();
            if (name.isEmpty()) {
                name = semNum > 0 ? ("Semester " + semNum) : ("Semester " + id);
            }
            int ayId = optInt(o, "academic_year_id", 0);
            entries.add(new SemesterEntry(new AddStudentActivity.IdNameItem(id, name, "", ayId), semNum));
        }

        entries.sort((a, b) -> {
            int ayCmp = Integer.compare(a.item.parentId, b.item.parentId);
            if (ayCmp != 0) {
                return ayCmp;
            }
            int numCmp = Integer.compare(a.semesterNumber, b.semesterNumber);
            if (numCmp != 0) {
                return numCmp;
            }
            return Integer.compare(a.item.id, b.item.id);
        });

        List<AddStudentActivity.IdNameItem> result = new ArrayList<>(entries.size());
        for (SemesterEntry entry : entries) {
            result.add(entry.item);
        }
        return result;
    }

    public static List<AddStudentActivity.IdNameItem> buildFacultyOptions(
            List<AddStudentActivity.IdNameItem> faculties,
            String selectFacultyLabel
    ) {
        List<AddStudentActivity.IdNameItem> options = new ArrayList<>();
        options.add(new AddStudentActivity.IdNameItem(0, selectFacultyLabel, "", 0));
        if (faculties != null) {
            for (AddStudentActivity.IdNameItem item : faculties) {
                if (item != null && item.id > 0) {
                    options.add(item);
                }
            }
        }
        return options;
    }

    public static MajorOptionsResult buildMajorOptions(
            AddStudentActivity.IdNameItem selectedFaculty,
            List<AddStudentActivity.IdNameItem> allMajors,
            String selectFacultyFirstLabel,
            String selectMajorLabel,
            String noMajorsLabel
    ) {
        if (selectedFaculty == null || selectedFaculty.id <= 0) {
            List<AddStudentActivity.IdNameItem> disabled = new ArrayList<>();
            disabled.add(new AddStudentActivity.IdNameItem(0, selectFacultyFirstLabel, "", 0));
            return new MajorOptionsResult(disabled, false);
        }

        List<AddStudentActivity.IdNameItem> matching = new ArrayList<>();
        if (allMajors != null) {
            for (AddStudentActivity.IdNameItem major : allMajors) {
                if (major != null && major.id > 0 && isMajorCompatibleWithFaculty(selectedFaculty, major)) {
                    matching.add(major);
                }
            }
        }

        if (matching.isEmpty()) {
            List<AddStudentActivity.IdNameItem> empty = new ArrayList<>();
            empty.add(new AddStudentActivity.IdNameItem(0, noMajorsLabel, "", 0));
            return new MajorOptionsResult(empty, false);
        }

        List<AddStudentActivity.IdNameItem> options = new ArrayList<>(matching.size() + 1);
        options.add(new AddStudentActivity.IdNameItem(0, selectMajorLabel, "", 0));
        options.addAll(matching);
        return new MajorOptionsResult(options, true);
    }

    public static MajorOptionsResult buildMajorOptionsFromApiResult(
            AddStudentActivity.IdNameItem selectedFaculty,
            List<AddStudentActivity.IdNameItem> apiMajors,
            String selectFacultyFirstLabel,
            String selectMajorLabel,
            String noMajorsLabel
    ) {
        if (selectedFaculty == null || selectedFaculty.id <= 0) {
            List<AddStudentActivity.IdNameItem> disabled = new ArrayList<>();
            disabled.add(new AddStudentActivity.IdNameItem(0, selectFacultyFirstLabel, "", 0));
            return new MajorOptionsResult(disabled, false);
        }

        List<AddStudentActivity.IdNameItem> valid = new ArrayList<>();
        if (apiMajors != null) {
            for (AddStudentActivity.IdNameItem major : apiMajors) {
                if (major != null && major.id > 0) {
                    valid.add(major);
                }
            }
        }

        if (valid.isEmpty()) {
            List<AddStudentActivity.IdNameItem> empty = new ArrayList<>();
            empty.add(new AddStudentActivity.IdNameItem(0, noMajorsLabel, "", 0));
            return new MajorOptionsResult(empty, false);
        }

        List<AddStudentActivity.IdNameItem> options = new ArrayList<>(valid.size() + 1);
        options.add(new AddStudentActivity.IdNameItem(0, selectMajorLabel, "", 0));
        options.addAll(valid);
        return new MajorOptionsResult(options, true);
    }

    public static boolean isMajorCompatibleWithFaculty(
            AddStudentActivity.IdNameItem faculty,
            AddStudentActivity.IdNameItem major
    ) {
        if (faculty == null || major == null || faculty.id <= 0 || major.id <= 0) {
            return false;
        }
        if (major.parentId == faculty.id) {
            return true;
        }
        String deptCode = faculty.code != null ? faculty.code.trim().toUpperCase(Locale.ROOT) : "";
        String progCode = major.code != null ? major.code.trim().toUpperCase(Locale.ROOT) : "";
        return ("CS".equals(deptCode) || "IT".equals(deptCode)) && COMPUTING_PROGRAM_CODES.contains(progCode);
    }

    public static List<AddStudentActivity.IdNameItem> buildAcademicYearOptions(
            List<AddStudentActivity.IdNameItem> academicYears,
            String selectAcademicYearLabel
    ) {
        List<AddStudentActivity.IdNameItem> options = new ArrayList<>();
        options.add(new AddStudentActivity.IdNameItem(0, selectAcademicYearLabel, "", 0));
        if (academicYears != null) {
            for (AddStudentActivity.IdNameItem item : academicYears) {
                if (item != null && item.id > 0) {
                    options.add(item);
                }
            }
        }
        return options;
    }

    public static List<AddStudentActivity.IdNameItem> buildSemesterOptions(
            AddStudentActivity.IdNameItem selectedAcademicYear,
            List<AddStudentActivity.IdNameItem> allSemesters,
            String noneLabel
    ) {
        List<AddStudentActivity.IdNameItem> options = new ArrayList<>();
        options.add(new AddStudentActivity.IdNameItem(0, noneLabel, "", 0));
        if (selectedAcademicYear != null && selectedAcademicYear.id > 0 && allSemesters != null) {
            for (AddStudentActivity.IdNameItem sem : allSemesters) {
                if (sem != null && sem.id > 0 && (sem.parentId == 0 || sem.parentId == selectedAcademicYear.id)) {
                    options.add(sem);
                }
            }
        }
        return options;
    }

    public static String validateAcademicSelection(
            AddStudentActivity.IdNameItem dept,
            AddStudentActivity.IdNameItem prog,
            Integer yearLevel,
            AddStudentActivity.IdNameItem academicYear
    ) {
        if (dept == null || dept.id <= 0) {
            return "faculty";
        }
        if (prog == null || prog.id <= 0) {
            return "major";
        }
        if (yearLevel == null || yearLevel < 1 || yearLevel > 4) {
            return "year_level";
        }
        if (academicYear == null || academicYear.id <= 0) {
            return "academic_year";
        }
        return null;
    }

    private static JsonArray getArray(JsonObject data, String primaryKey, String fallbackKey) {
        if (data == null) return null;
        if (primaryKey != null && data.has(primaryKey) && data.get(primaryKey).isJsonArray()) {
            return data.getAsJsonArray(primaryKey);
        }
        if (fallbackKey != null && data.has(fallbackKey) && data.get(fallbackKey).isJsonArray()) {
            return data.getAsJsonArray(fallbackKey);
        }
        return null;
    }

    private static boolean isInactiveStatus(String status) {
        if (status == null) return false;
        String s = status.trim().toLowerCase(Locale.ROOT);
        return "inactive".equals(s) || "disabled".equals(s);
    }

    private static int optInt(JsonObject o, String key, int fallback) {
        if (o == null || key == null || !o.has(key) || o.get(key).isJsonNull()) {
            return fallback;
        }
        JsonElement el = o.get(key);
        if (!el.isJsonPrimitive()) {
            return fallback;
        }
        JsonPrimitive prim = el.getAsJsonPrimitive();
        try {
            if (prim.isNumber()) {
                return prim.getAsInt();
            }
            if (prim.isString()) {
                String s = prim.getAsString().trim();
                return s.isEmpty() ? fallback : Integer.parseInt(s);
            }
        } catch (NumberFormatException ignored) {
        }
        return fallback;
    }

    private static String optString(JsonObject o, String key, String fallback) {
        if (o == null || key == null || !o.has(key) || o.get(key).isJsonNull()) {
            return fallback;
        }
        JsonElement el = o.get(key);
        if (!el.isJsonPrimitive()) {
            return fallback;
        }
        try {
            String val = el.getAsString();
            return val != null ? val : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
