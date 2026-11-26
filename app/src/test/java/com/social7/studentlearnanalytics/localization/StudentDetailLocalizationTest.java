package com.social7.studentlearnanalytics.localization;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StudentDetailLocalizationTest {

    private File resolveResDir() {
        File dir = new File("src/main/res");
        if (!dir.exists()) {
            dir = new File("app/src/main/res");
        }
        return dir;
    }

    @Test
    public void studentDetailTabsAndSectionsAreLocalizedInKhmer() throws Exception {
        File resDir = resolveResDir();
        Map<String, String> en = parseStrings(new File(resDir, "values/strings.xml"));
        Map<String, String> km = parseStrings(new File(resDir, "values-km/strings.xml"));

        String[] requiredKeys = {
                "overview", "tab_study", "attendance", "notes",
                "academic_information", "address_information", "family_emergency_contact",
                "department", "program_95c50c", "year_level_6a4901", "academic_year_cecc30",
                "assigned_class", "cumulative_gpa", "enrolled_courses",
                "place_of_birth", "current_address",
                "father_name", "father_phone", "mother_name", "mother_phone",
                "emergency_contact", "emergency_phone",
                "attendance_summary", "overall_attendance_rate", "total_classes_recorded",
                "attendance_present", "attendance_late", "attendance_excused", "attendance_absent",
                "attendance_history", "attendance_no_records",
                "admin_advisor_notes", "add_note", "edit_note", "delete_note",
                "note_author", "note_created_at", "note_updated_at", "note_content",
                "no_notes_yet", "note_saved_success", "note_deleted_success",
                "view_details", "edit_student", "deactivate_account", "activate_account", "delete_user"
        };

        for (String key : requiredKeys) {
            assertTrue("Missing English key: " + key, en.containsKey(key));
            assertTrue("Missing Khmer key: " + key, km.containsKey(key));
            assertFalse("Blank Khmer value for key: " + key, km.get(key).trim().isEmpty());
        }

        assertEquals("ទិដ្ឋភាពទូទៅ", km.get("overview"));
        assertEquals("សិក្សា", km.get("tab_study"));
        assertEquals("វត្តមាន", km.get("attendance"));
        assertEquals("កំណត់ចំណាំ", km.get("notes"));
        assertEquals("ព័ត៌មានសិក្សា", km.get("academic_information"));
        assertEquals("សង្ខេបវត្តមាន", km.get("attendance_summary"));
        assertEquals("អត្រាវត្តមានសរុប", km.get("overall_attendance_rate"));
        assertEquals("កំណត់ចំណាំពីអ្នកគ្រប់គ្រង និងទីប្រឹក្សា", km.get("admin_advisor_notes"));

        assertEquals("Faculty", en.get("department"));
        assertEquals("Faculty *", en.get("department_0e3338"));
        assertEquals("Major *", en.get("program"));
        assertEquals("Major", en.get("program_95c50c"));
        assertEquals("Faculties", en.get("departments"));
        assertEquals("Select Faculty", en.get("select_department_title"));
        assertEquals("Select Major", en.get("select_program_title"));
        assertEquals("Please select a faculty", en.get("please_select_a_department"));
        assertEquals("Please select a major", en.get("please_select_a_program"));
        assertEquals("No majors available for this faculty", en.get("no_majors_for_faculty"));

        assertEquals("មហាវិទ្យាល័យ", km.get("department"));
        assertEquals("មហាវិទ្យាល័យ *", km.get("department_0e3338"));
        assertEquals("ជំនាញ *", km.get("program"));
        assertEquals("ជំនាញ", km.get("program_95c50c"));
        assertEquals("មហាវិទ្យាល័យ", km.get("departments"));
        assertEquals("ជ្រើសរើសមហាវិទ្យាល័យ", km.get("select_department_title"));
        assertEquals("ជ្រើសរើសជំនាញ", km.get("select_program_title"));
        assertEquals("សូមជ្រើសរើសមហាវិទ្យាល័យ", km.get("please_select_a_department"));
        assertEquals("សូមជ្រើសរើសជំនាញ", km.get("please_select_a_program"));
        assertEquals("មិនមានជំនាញសម្រាប់មហាវិទ្យាល័យនេះទេ", km.get("no_majors_for_faculty"));
    }

    private Map<String, String> parseStrings(File file) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        NodeList nodes = doc.getElementsByTagName("string");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            map.put(el.getAttribute("name"), el.getTextContent());
        }
        return map;
    }
}
