package com.social7.studentlearnanalytics.localization;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalizationTest {

    private File resolveResDir() {
        File dir = new File("src/main/res");
        if (!dir.exists()) {
            dir = new File("app/src/main/res");
        }
        return dir;
    }

    @Test
    public void languageConstantsMatchSupportedLocaleTags() {
        assertEquals("en-US", LanguageConstants.LANG_ENGLISH);
        assertEquals("km-KH", LanguageConstants.LANG_KHMER);
    }

    @Test
    public void khmerFontResourcesExist() {
        File fontDir = new File(resolveResDir(), "font");
        assertTrue(new File(fontDir, "notosanskhmer.xml").exists());
        assertTrue(new File(fontDir, "notosanskhmer_regular.ttf").exists());
        assertTrue(new File(fontDir, "notosanskhmer_medium.ttf").exists());
        assertTrue(new File(fontDir, "notosanskhmer_bold.ttf").exists());
    }

    @Test
    public void everyEnglishStringResourceHasKhmerTranslationAndNoDuplicates() throws Exception {
        File resDir = resolveResDir();
        File enFile = new File(resDir, "values/strings.xml");
        File kmFile = new File(resDir, "values-km/strings.xml");

        assertTrue("English strings.xml must exist", enFile.exists());
        assertTrue("Khmer values-km/strings.xml must exist", kmFile.exists());

        Set<String> enDuplicates = new HashSet<>();
        Set<String> kmDuplicates = new HashSet<>();
        Map<String, String> enStrings = parseStrings(enFile, enDuplicates);
        Map<String, String> kmStrings = parseStrings(kmFile, kmDuplicates);

        assertTrue("Duplicate keys in values/strings.xml: " + enDuplicates, enDuplicates.isEmpty());
        assertTrue("Duplicate keys in values-km/strings.xml: " + kmDuplicates, kmDuplicates.isEmpty());
        assertFalse("English strings must not be empty", enStrings.isEmpty());

        for (Map.Entry<String, String> entry : enStrings.entrySet()) {
            String key = entry.getKey();
            assertTrue("Missing Khmer translation for key: " + key, kmStrings.containsKey(key));
            assertFalse("Blank Khmer translation for key: " + key, kmStrings.get(key).trim().isEmpty());
        }
    }

    @Test
    public void coreDrawerAndAppearanceKeysExistInBothLocales() throws Exception {
        File resDir = resolveResDir();
        Map<String, String> enStrings = parseStrings(new File(resDir, "values/strings.xml"), new HashSet<>());
        Map<String, String> kmStrings = parseStrings(new File(resDir, "values-km/strings.xml"), new HashSet<>());

        String[] requiredKeys = {
                "appearance_settings", "lang_english_us", "lang_khmer_kh",
                "dashboard", "user_management", "students_teachers_admins",
                "course_management", "manage_courses_and_curriculum",
                "class_management", "manage_classes_schedules_and_buildings",
                "reports_analytics", "view_system_reports_and_statistics",
                "nav_notifications", "view_and_manage_notifications",
                "nav_settings", "account_and_system_settings",
                "help_support", "get_help_and_contact_support",
                "about", "app_version_and_system_information",
                "status_present", "status_absent", "status_late", "status_excused",
                "risk_low", "risk_medium", "risk_high",
                "good_morning", "good_afternoon", "good_evening", "admin_slas",
                "annual_student_enrollment_engagement", "all_academic_years",
                "active_learners", "no_change_this_month", "no_comparison_data",
                "compare_years", "historical_reports"
        };

        for (String key : requiredKeys) {
            assertTrue("Missing EN key: " + key, enStrings.containsKey(key));
            assertTrue("Missing KM key: " + key, kmStrings.containsKey(key));
        }
    }

    private Map<String, String> parseStrings(File xmlFile, Set<String> duplicates) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        NodeList nodes = doc.getElementsByTagName("string");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String name = el.getAttribute("name");
            if (map.containsKey(name)) {
                duplicates.add(name);
            }
            map.put(name, el.getTextContent());
        }
        return map;
    }
}
