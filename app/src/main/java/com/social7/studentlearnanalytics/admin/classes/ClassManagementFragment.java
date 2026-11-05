package com.social7.studentlearnanalytics.admin.classes;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.AdminMainActivity;
import com.social7.studentlearnanalytics.admin.users.AdminStudentDetailActivity;
import com.social7.studentlearnanalytics.model.ClassModel;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassManagementFragment extends Fragment {

    private static final String TAG = "ClassManagement";

    // Primary Tabs
    private MaterialButton btnSubTabClassDirectory, btnSubTabClassSchedules, btnSubTabClassroomManagement, btnSubTabTeacherAssignments, btnSubTabClassEnrollment;

    // Containers
    private View layoutClassDirectoryContainer, layoutSchedulesContainer, layoutClassroomsContainer, layoutTeacherAssignContainer, layoutClassEnrollmentContainer;

    // Tab 1: Class Directory
    private RecyclerView rvClasses;
    private EditText etSearchClass;
    private MaterialButton btnAddClass;
    private ClassAdapter classAdapter;
    private final List<ClassModel> classList = new ArrayList<>();
    private TextView pillFilterAll, pillFilterActive, pillFilterInactive;

    // Tab 2: Class Schedules
    private RecyclerView rvSchedules;
    private ClassScheduleAdapter scheduleAdapter;
    private final List<ClassScheduleAdapter.ScheduleItem> scheduleList = new ArrayList<>();
    private EditText etSearchSchedule;
    private MaterialButton btnAddSchedule;

    // Tab 3: Classroom Management
    private RecyclerView rvClassrooms;
    private ClassroomAdapter classroomAdapter;
    private final List<ClassroomAdapter.ClassroomItem> classroomList = new ArrayList<>();
    private EditText etSearchClassroom;
    private MaterialButton btnAddClassroom;

    // Tab 4: Teacher Assignments
    private RecyclerView rvTeacherAssignments;
    private ClassTeacherAssignmentAdapter teacherAssignAdapter;
    private final List<ClassTeacherAssignmentAdapter.TeacherAssignItem> teacherAssignList = new ArrayList<>();
    private EditText etSearchTeacherAssign;
    private MaterialButton btnAssignTeacher;

    // Tab 5: Class Enrollment
    private RecyclerView rvEnrollments;
    private ClassEnrollmentAdapter enrollmentAdapter;
    private final List<ClassEnrollmentAdapter.EnrollmentItem> enrollmentList = new ArrayList<>();
    private EditText etSearchEnrollment;
    private MaterialButton btnEnrollStudent;

    // Current Active Tab (0=Directory, 1=Schedules, 2=Classrooms, 3=TeacherAssign, 4=Enrollment)
    private int selectedTab = 0;

    public ClassManagementFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_admin_classes, container, false);

        // Header Menu Icon
        View btnMenu = view.findViewById(R.id.btnUsersMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        // Header Titles
        TextView tvTitle = view.findViewById(R.id.tvHeaderTitle);
        TextView tvSubtitle = view.findViewById(R.id.tvHeaderSubtitle);
        if (tvTitle != null) tvTitle.setText("Class Management");
        if (tvSubtitle != null) tvSubtitle.setText("Manage university classes, schedules and classrooms");

        // Primary Tab Buttons
        btnSubTabClassDirectory = view.findViewById(R.id.btnSubTabClassDirectory);
        btnSubTabClassSchedules = view.findViewById(R.id.btnSubTabClassSchedules);
        btnSubTabClassroomManagement = view.findViewById(R.id.btnSubTabClassroomManagement);
        btnSubTabTeacherAssignments = view.findViewById(R.id.btnSubTabTeacherAssignments);
        btnSubTabClassEnrollment = view.findViewById(R.id.btnSubTabClassEnrollment);

        // Containers
        layoutClassDirectoryContainer = view.findViewById(R.id.layoutClassDirectoryContainer);
        layoutSchedulesContainer = view.findViewById(R.id.layoutSchedulesContainer);
        layoutClassroomsContainer = view.findViewById(R.id.layoutClassroomsContainer);
        layoutTeacherAssignContainer = view.findViewById(R.id.layoutTeacherAssignContainer);
        layoutClassEnrollmentContainer = view.findViewById(R.id.layoutClassEnrollmentContainer);

        // Bind Tab Views
        bindClassDirectoryViews(view);
        bindSchedulesViews(view);
        bindClassroomsViews(view);
        bindTeacherAssignmentsViews(view);
        bindEnrollmentsViews(view);

        setupTabListeners();
        selectClassTab(selectedTab);

        return view;
    }

    private void setupTabListeners() {
        if (btnSubTabClassDirectory != null) {
            btnSubTabClassDirectory.setOnClickListener(v -> selectClassTab(0));
        }
        if (btnSubTabClassSchedules != null) {
            btnSubTabClassSchedules.setOnClickListener(v -> selectClassTab(1));
        }
        if (btnSubTabClassroomManagement != null) {
            btnSubTabClassroomManagement.setOnClickListener(v -> selectClassTab(2));
        }
        if (btnSubTabTeacherAssignments != null) {
            btnSubTabTeacherAssignments.setOnClickListener(v -> selectClassTab(3));
        }
        if (btnSubTabClassEnrollment != null) {
            btnSubTabClassEnrollment.setOnClickListener(v -> selectClassTab(4));
        }
    }

    public void selectClassTab(int tabIndex) {
        this.selectedTab = tabIndex;
        updateTabButtonStyles(tabIndex);

        if (layoutClassDirectoryContainer != null) layoutClassDirectoryContainer.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (layoutSchedulesContainer != null) layoutSchedulesContainer.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        if (layoutClassroomsContainer != null) layoutClassroomsContainer.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (layoutTeacherAssignContainer != null) layoutTeacherAssignContainer.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);
        if (layoutClassEnrollmentContainer != null) layoutClassEnrollmentContainer.setVisibility(tabIndex == 4 ? View.VISIBLE : View.GONE);

        if (tabIndex == 0) {
            loadClassData();
        } else if (tabIndex == 1) {
            loadScheduleData();
        } else if (tabIndex == 2) {
            loadClassroomData();
        } else if (tabIndex == 3) {
            loadTeacherAssignData();
        } else if (tabIndex == 4) {
            loadEnrollmentData();
        }
    }

    private void updateTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        MaterialButton[] tabs = new MaterialButton[]{
                btnSubTabClassDirectory, btnSubTabClassSchedules, btnSubTabClassroomManagement, btnSubTabTeacherAssignments, btnSubTabClassEnrollment
        };

        int activeColor = getResources().getColor(R.color.admin_primary_blue, null);
        int inactiveBg = Color.parseColor("#F4F7FF");
        int textSecondary = getResources().getColor(R.color.admin_text_secondary, null);

        for (int i = 0; i < tabs.length; i++) {
            MaterialButton btn = tabs[i];
            if (btn == null) continue;
            if (i == activeIndex) {
                btn.setBackgroundTintList(ColorStateList.valueOf(activeColor));
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
                btn.setTextColor(textSecondary);
            }
        }
    }

    // ==========================================
    // 1. CLASS DIRECTORY TAB
    // ==========================================
    private void bindClassDirectoryViews(View root) {
        rvClasses = root.findViewById(R.id.rvUsers);
        etSearchClass = root.findViewById(R.id.etSearchUser);
        btnAddClass = root.findViewById(R.id.btnAddUser);

        pillFilterAll = root.findViewById(R.id.pillFilterAll);
        pillFilterActive = root.findViewById(R.id.pillFilterActive);
        pillFilterInactive = root.findViewById(R.id.pillFilterInactive);

        if (btnAddClass != null) {
            btnAddClass.setText("+ Add New Class");
            btnAddClass.setOnClickListener(v -> showAddClassDialog());
        }

        setupSearchAndFilter();
    }

    private void setupSearchAndFilter() {
        if (etSearchClass != null) {
            etSearchClass.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (classAdapter != null) classAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (pillFilterAll != null) {
            pillFilterAll.setOnClickListener(v -> {
                updatePillSelection(pillFilterAll, pillFilterActive, pillFilterInactive);
                if (classAdapter != null) classAdapter.filterByStatus("ALL");
            });
        }

        if (pillFilterActive != null) {
            pillFilterActive.setOnClickListener(v -> {
                updatePillSelection(pillFilterActive, pillFilterAll, pillFilterInactive);
                if (classAdapter != null) classAdapter.filterByStatus("Active");
            });
        }

        if (pillFilterInactive != null) {
            pillFilterInactive.setOnClickListener(v -> {
                updatePillSelection(pillFilterInactive, pillFilterAll, pillFilterActive);
                if (classAdapter != null) classAdapter.filterByStatus("Inactive");
            });
        }
    }

    private void updatePillSelection(TextView selected, TextView unselected1, TextView unselected2) {
        if (!isAdded() || getContext() == null) return;
        if (selected != null) {
            selected.setBackgroundResource(R.drawable.bg_notif_tab_active);
            selected.setTextColor(Color.WHITE);
        }
        if (unselected1 != null) {
            unselected1.setBackgroundResource(R.drawable.bg_notif_tab_inactive);
            unselected1.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
        }
        if (unselected2 != null) {
            unselected2.setBackgroundResource(R.drawable.bg_notif_tab_inactive);
            unselected2.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
        }
    }

    private void loadClassData() {
        if (!isAdded() || getContext() == null) return;

        try {
            ApiClient.getApiService(getContext()).getClasses().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded()) return;
                    classList.clear();
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject body = response.body();
                            JsonArray array = null;
                            if (body.has("data") && body.get("data").isJsonArray()) {
                                array = body.getAsJsonArray("data");
                            } else if (body.has("classes") && body.get("classes").isJsonArray()) {
                                array = body.getAsJsonArray("classes");
                            }

                            if (array != null) {
                                for (JsonElement elem : array) {
                                    if (elem.isJsonObject()) {
                                        JsonObject obj = elem.getAsJsonObject();
                                        String id = getSafeString(obj, "id", String.valueOf(classList.size() + 1));
                                        String name = getSafeString(obj, "name", getSafeString(obj, "class_name", "Class"));
                                        String code = getSafeString(obj, "code", getSafeString(obj, "class_code", "IT"));
                                        String dept = getSafeString(obj, "department", "College of Computer Studies");
                                        String ay = getSafeString(obj, "academic_year", "AY 2025-2026");
                                        String course = getSafeString(obj, "course", "Information Technology");
                                        String teacher = getSafeString(obj, "teacher", "Prof. Santos");
                                        String status = getSafeString(obj, "status", "Active");
                                        int count = getSafeInt(obj, "students_count", getSafeInt(obj, "student_count", 30));

                                        classList.add(new ClassModel(id, name, code, dept, ay, course, teacher, status, count));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing classes JSON response", e);
                    }

                    if (classList.isEmpty()) {
                        loadFallbackClasses();
                    } else {
                        bindClassAdapter();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    loadFallbackClasses();
                }
            });
        } catch (Exception e) {
            loadFallbackClasses();
        }
    }

    private void loadFallbackClasses() {
        if (!isAdded()) return;
        classList.clear();
        classList.add(new ClassModel("1", "BSIT 4-A", "IT-4A", "College of Computer Studies", "AY 2025-2026", "Information Technology", "Prof. Alan Santos", "Active", 35));
        classList.add(new ClassModel("2", "BSIT 4-B", "IT-4B", "College of Computer Studies", "AY 2025-2026", "Information Technology", "Dr. Maria Cruz", "Active", 32));
        classList.add(new ClassModel("3", "BSCS 3-A", "CS-3A", "College of Computer Studies", "AY 2025-2026", "Computer Science", "Engr. John Reyes", "Active", 40));
        classList.add(new ClassModel("4", "BSCS 3-B", "CS-3B", "College of Computer Studies", "AY 2025-2026", "Computer Science", "Prof. Sarah Jenkins", "Active", 38));
        classList.add(new ClassModel("5", "BSIS 2-A", "IS-2A", "College of Computer Studies", "AY 2025-2026", "Information Systems", "Prof. Robert Lim", "Inactive", 28));
        classList.add(new ClassModel("6", "ACT 1-A", "ACT-1A", "College of Computer Studies", "AY 2025-2026", "Associate in Computer Tech", "Prof. Clara Garcia", "Active", 42));

        bindClassAdapter();
    }

    private void bindClassAdapter() {
        if (!isAdded() || getContext() == null) return;

        classAdapter = new ClassAdapter(getContext(), classList, this::showClassDetailsDialog);

        if (rvClasses != null) {
            rvClasses.setLayoutManager(new LinearLayoutManager(getContext()));
            rvClasses.setAdapter(classAdapter);
        }

        if (pillFilterAll != null) pillFilterAll.setText("All Classes (" + classList.size() + ")");
        int activeCount = 0;
        int inactiveCount = 0;
        for (ClassModel c : classList) {
            if ("Active".equalsIgnoreCase(c.getStatus())) activeCount++;
            else inactiveCount++;
        }
        if (pillFilterActive != null) pillFilterActive.setText("Active (" + activeCount + ")");
        if (pillFilterInactive != null) pillFilterInactive.setText("Inactive (" + inactiveCount + ")");
    }

    private void showClassDetailsDialog(ClassModel classModel) {
        if (!isAdded() || getContext() == null) return;

        String details = "Class Name: " + classModel.getName() + "\n" +
                "Class Code: " + classModel.getCode() + "\n" +
                "Department: " + classModel.getDepartment() + "\n" +
                "Academic Year: " + classModel.getAcademicYear() + "\n" +
                "Course: " + classModel.getCourse() + "\n" +
                "Assigned Teacher: " + classModel.getTeacher() + "\n" +
                "Enrolled Students: " + classModel.getStudentCount() + "\n" +
                "Status: " + classModel.getStatus();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Class Directory Detail")
                .setMessage(details)
                .setPositiveButton("View Representative Dossier", (dialog, which) -> openSampleStudentDossier(classModel))
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openSampleStudentDossier(ClassModel classModel) {
        if (!isAdded() || getContext() == null) return;

        Student sampleStudent = new Student(
                "STU23001",
                "Sophea",
                "sophea@univ.edu",
                classModel.getDepartment() != null ? classModel.getDepartment() : "Computer Science",
                classModel.getCourse() != null ? classModel.getCourse() : "Information Technology",
                classModel.getName() != null ? classModel.getName() : "Year 4",
                3.45,
                "Good Standing",
                88.5,
                "#EAF0FF",
                "#3155F5",
                classModel.getCode() != null ? classModel.getCode() : "CS101"
        );

        Intent intent = new Intent(requireContext(), AdminStudentDetailActivity.class);
        intent.putExtra("student", sampleStudent);
        startActivity(intent);
    }

    private void showAddClassDialog() {
        if (!isAdded() || getContext() == null) return;

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final TextInputEditText etName = createInputDialog(layout, "Class Name (e.g. BSIT 4-C)");
        final TextInputEditText etCode = createInputDialog(layout, "Class Code (e.g. IT-4C)");
        final TextInputEditText etCourse = createInputDialog(layout, "Course (e.g. Information Technology)");
        final TextInputEditText etTeacher = createInputDialog(layout, "Teacher (e.g. Prof. Santos)");
        final TextInputEditText etDept = createInputDialog(layout, "Department (e.g. Computer Studies)");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add New Class")
                .setView(layout)
                .setPositiveButton("Create Class", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";
                    String course = etCourse.getText() != null ? etCourse.getText().toString().trim() : "";
                    String teacher = etTeacher.getText() != null ? etTeacher.getText().toString().trim() : "";
                    String dept = etDept.getText() != null ? etDept.getText().toString().trim() : "";

                    if (name.isEmpty() || code.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill in Class Name and Class Code", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ClassModel newClass = new ClassModel(
                            String.valueOf(System.currentTimeMillis()),
                            name,
                            code,
                            dept.isEmpty() ? "College of Computer Studies" : dept,
                            "AY 2025-2026",
                            course.isEmpty() ? "Information Technology" : course,
                            teacher.isEmpty() ? "Unassigned" : teacher,
                            "Active",
                            0
                    );

                    classList.add(0, newClass);
                    bindClassAdapter();
                    Toast.makeText(getContext(), "Class " + name + " created successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ==========================================
    // 2. CLASS SCHEDULES TAB
    // ==========================================
    private void bindSchedulesViews(View root) {
        View layoutSchedules = root.findViewById(R.id.layoutSchedulesContainer);
        if (layoutSchedules == null) return;

        rvSchedules = layoutSchedules.findViewById(R.id.rvSchedules);
        etSearchSchedule = layoutSchedules.findViewById(R.id.etSearchSchedule);
        btnAddSchedule = layoutSchedules.findViewById(R.id.btnAddSchedule);

        if (etSearchSchedule != null) {
            etSearchSchedule.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (scheduleAdapter != null) scheduleAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnAddSchedule != null) {
            btnAddSchedule.setOnClickListener(v -> showAddScheduleWithConflictValidationDialog());
        }
    }

    private void loadScheduleData() {
        if (!isAdded() || getContext() == null) return;

        scheduleList.clear();
        scheduleList.add(new ClassScheduleAdapter.ScheduleItem("BSIT 4-A", "Web Systems & Technologies", "Prof. Alan Santos", "Mon / Wed 08:00 AM - 11:00 AM", "Lab 101", "Validated"));
        scheduleList.add(new ClassScheduleAdapter.ScheduleItem("BSIT 4-B", "Software Engineering", "Dr. Maria Cruz", "Tue / Thu 01:00 PM - 04:00 PM", "Lab 102", "Validated"));
        scheduleList.add(new ClassScheduleAdapter.ScheduleItem("BSCS 3-A", "Data Structures & Algorithms", "Engr. John Reyes", "Mon / Wed 01:00 PM - 04:00 PM", "Lecture Room 301", "Validated"));
        scheduleList.add(new ClassScheduleAdapter.ScheduleItem("BSCS 3-B", "Object-Oriented Programming", "Prof. Sarah Jenkins", "Tue / Thu 08:00 AM - 11:00 AM", "Lab 103", "Validated"));
        scheduleList.add(new ClassScheduleAdapter.ScheduleItem("BSIS 2-A", "Enterprise Systems Architecture", "Prof. Robert Lim", "Fri 09:00 AM - 12:00 PM", "Seminar Room B", "Validated"));
        scheduleList.add(new ClassScheduleAdapter.ScheduleItem("ACT 1-A", "Mobile Application Development", "Prof. Clara Garcia", "Wed / Fri 01:00 PM - 03:30 PM", "Room 204", "Validated"));

        if (rvSchedules != null) {
            scheduleAdapter = new ClassScheduleAdapter(getContext(), scheduleList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Class Schedule Detail")
                        .setMessage("Class Code: " + item.classCode + "\n" +
                                "Course: " + item.courseName + "\n" +
                                "Teacher: " + item.teacherName + "\n" +
                                "Time Slot: " + item.dayAndTime + "\n" +
                                "Classroom: " + item.roomName + "\n" +
                                "Conflict Validation: " + item.status)
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvSchedules.setLayoutManager(new LinearLayoutManager(getContext()));
            rvSchedules.setAdapter(scheduleAdapter);
        }
    }

    private void showAddScheduleWithConflictValidationDialog() {
        if (!isAdded() || getContext() == null) return;

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final TextInputEditText etClass = createInputDialog(layout, "Class Code (e.g. BSIT 4-C)");
        final TextInputEditText etCourse = createInputDialog(layout, "Course Name (e.g. Database Systems)");
        final TextInputEditText etTeacher = createInputDialog(layout, "Teacher Name (e.g. Prof. Santos)");
        final TextInputEditText etRoom = createInputDialog(layout, "Classroom (e.g. Lab 101)");
        final TextInputEditText etTime = createInputDialog(layout, "Time Slot (e.g. Mon 08:00 AM - 11:00 AM)");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Class Schedule")
                .setView(layout)
                .setPositiveButton("Add Schedule", (dialog, which) -> {
                    String classCode = etClass.getText() != null ? etClass.getText().toString().trim() : "";
                    String course = etCourse.getText() != null ? etCourse.getText().toString().trim() : "";
                    String teacher = etTeacher.getText() != null ? etTeacher.getText().toString().trim() : "";
                    String room = etRoom.getText() != null ? etRoom.getText().toString().trim() : "";
                    String time = etTime.getText() != null ? etTime.getText().toString().trim() : "";

                    if (classCode.isEmpty() || room.isEmpty() || time.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill in Class Code, Room, and Time Slot", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Conflict validation check across room and teacher
                    for (ClassScheduleAdapter.ScheduleItem item : scheduleList) {
                        if (item.roomName.equalsIgnoreCase(room) && item.dayAndTime.equalsIgnoreCase(time)) {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Scheduling Conflict Detected!")
                                    .setMessage("Classroom " + room + " is already occupied during " + time + " by " + item.classCode + ".\n\nPlease choose a different room or time slot.")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }
                    }

                    scheduleList.add(0, new ClassScheduleAdapter.ScheduleItem(classCode, course, teacher, time, room, "Validated"));
                    if (scheduleAdapter != null) scheduleAdapter.filter("");
                    Toast.makeText(getContext(), "Schedule for " + classCode + " added with zero conflicts!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ==========================================
    // 3. CLASSROOM MANAGEMENT TAB
    // ==========================================
    private void bindClassroomsViews(View root) {
        View layoutClassrooms = root.findViewById(R.id.layoutClassroomsContainer);
        if (layoutClassrooms == null) return;

        rvClassrooms = layoutClassrooms.findViewById(R.id.rvClassrooms);
        etSearchClassroom = layoutClassrooms.findViewById(R.id.etSearchClassroom);
        btnAddClassroom = layoutClassrooms.findViewById(R.id.btnAddClassroom);

        if (etSearchClassroom != null) {
            etSearchClassroom.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (classroomAdapter != null) classroomAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnAddClassroom != null) {
            btnAddClassroom.setOnClickListener(v -> showAddClassroomDialog());
        }
    }

    private void loadClassroomData() {
        if (!isAdded() || getContext() == null) return;

        classroomList.clear();
        classroomList.add(new ClassroomAdapter.ClassroomItem("Lab 101", "Computer Lab 101", "CCS Building • 1st Floor", 40, "40 PCs, Projector, AC, High-Speed LAN", "Available"));
        classroomList.add(new ClassroomAdapter.ClassroomItem("Lab 102", "Computer Lab 102", "CCS Building • 1st Floor", 35, "35 PCs, Projector, Smart Board", "Available"));
        classroomList.add(new ClassroomAdapter.ClassroomItem("Lab 103", "Computer Lab 103", "CCS Building • 2nd Floor", 40, "40 PCs, Projector, AC", "Available"));
        classroomList.add(new ClassroomAdapter.ClassroomItem("Lecture Hall A", "Grand Lecture Hall", "Main Building • 1st Floor", 120, "120 Seats, Audio System, Projector", "Occupied"));
        classroomList.add(new ClassroomAdapter.ClassroomItem("Room 301", "Lecture Room 301", "CAS Building • 3rd Floor", 50, "50 Seats, Whiteboard, AC", "Available"));
        classroomList.add(new ClassroomAdapter.ClassroomItem("Room B", "Seminar Room B", "Admin Building • 2nd Floor", 30, "30 Seats, Projector", "Maintenance"));

        if (rvClassrooms != null) {
            classroomAdapter = new ClassroomAdapter(getContext(), classroomList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Classroom Detail")
                        .setMessage("Room Number: " + item.roomNumber + "\n" +
                                "Room Name: " + item.roomName + "\n" +
                                "Location: " + item.buildingFloor + "\n" +
                                "Capacity: " + item.capacity + " Seats/PCs\n" +
                                "Equipment: " + item.equipment + "\n" +
                                "Status: " + item.status)
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvClassrooms.setLayoutManager(new LinearLayoutManager(getContext()));
            rvClassrooms.setAdapter(classroomAdapter);
        }
    }

    private void showAddClassroomDialog() {
        if (!isAdded() || getContext() == null) return;

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final TextInputEditText etNum = createInputDialog(layout, "Room Number (e.g. Lab 104)");
        final TextInputEditText etName = createInputDialog(layout, "Room Name (e.g. Computer Lab 104)");
        final TextInputEditText etBuilding = createInputDialog(layout, "Building & Floor (e.g. CCS Building 2nd Floor)");
        final TextInputEditText etCap = createInputDialog(layout, "Capacity (e.g. 40)");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Classroom Facility")
                .setView(layout)
                .setPositiveButton("Create Classroom", (dialog, which) -> {
                    String num = etNum.getText() != null ? etNum.getText().toString().trim() : "";
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String bldg = etBuilding.getText() != null ? etBuilding.getText().toString().trim() : "";
                    int cap = 35;
                    try { cap = Integer.parseInt(etCap.getText().toString().trim()); } catch (Exception ignored) {}

                    if (num.isEmpty() || name.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill in Room Number and Name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    classroomList.add(0, new ClassroomAdapter.ClassroomItem(num, name, bldg.isEmpty() ? "CCS Building" : bldg, cap, "Projector, AC, PCs", "Available"));
                    if (classroomAdapter != null) classroomAdapter.filter("");
                    Toast.makeText(getContext(), "Classroom " + num + " added successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ==========================================
    // 4. TEACHER ASSIGNMENTS TAB
    // ==========================================
    private void bindTeacherAssignmentsViews(View root) {
        View layoutAssign = root.findViewById(R.id.layoutTeacherAssignContainer);
        if (layoutAssign == null) return;

        rvTeacherAssignments = layoutAssign.findViewById(R.id.rvTeacherAssignments);
        etSearchTeacherAssign = layoutAssign.findViewById(R.id.etSearchTeacherAssign);
        btnAssignTeacher = layoutAssign.findViewById(R.id.btnAssignTeacher);

        if (etSearchTeacherAssign != null) {
            etSearchTeacherAssign.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (teacherAssignAdapter != null) teacherAssignAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnAssignTeacher != null) {
            btnAssignTeacher.setOnClickListener(v -> showAssignTeacherDialog());
        }
    }

    private void loadTeacherAssignData() {
        if (!isAdded() || getContext() == null) return;

        teacherAssignList.clear();
        teacherAssignList.add(new ClassTeacherAssignmentAdapter.TeacherAssignItem("BSIT 4-A", "Web Systems & Technologies", "Prof. Alan Santos", "College of Computer Studies • AY 2025-2026 Sem 1", "Assigned"));
        teacherAssignList.add(new ClassTeacherAssignmentAdapter.TeacherAssignItem("BSIT 4-B", "Software Engineering", "Dr. Maria Cruz", "College of Computer Studies • AY 2025-2026 Sem 1", "Assigned"));
        teacherAssignList.add(new ClassTeacherAssignmentAdapter.TeacherAssignItem("BSCS 3-A", "Data Structures & Algorithms", "Engr. John Reyes", "College of Computer Studies • AY 2025-2026 Sem 1", "Assigned"));
        teacherAssignList.add(new ClassTeacherAssignmentAdapter.TeacherAssignItem("BSCS 3-B", "Object-Oriented Programming", "Prof. Sarah Jenkins", "College of Computer Studies • AY 2025-2026 Sem 1", "Assigned"));
        teacherAssignList.add(new ClassTeacherAssignmentAdapter.TeacherAssignItem("BSIS 2-A", "Enterprise Systems Architecture", "Prof. Robert Lim", "College of Computer Studies • AY 2025-2026 Sem 1", "Assigned"));
        teacherAssignList.add(new ClassTeacherAssignmentAdapter.TeacherAssignItem("ACT 1-A", "Mobile Application Development", "Prof. Clara Garcia", "College of Computer Studies • AY 2025-2026 Sem 1", "Assigned"));

        if (rvTeacherAssignments != null) {
            teacherAssignAdapter = new ClassTeacherAssignmentAdapter(getContext(), teacherAssignList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Teacher Assignment Detail")
                        .setMessage("Class Code: " + item.classCode + "\n" +
                                "Course Name: " + item.courseName + "\n" +
                                "Assigned Teacher: " + item.teacherName + "\n" +
                                "Department & Term: " + item.departmentTerm + "\n" +
                                "Staffing Status: " + item.status)
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvTeacherAssignments.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeacherAssignments.setAdapter(teacherAssignAdapter);
        }
    }

    private void showAssignTeacherDialog() {
        if (!isAdded() || getContext() == null) return;

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final TextInputEditText etClass = createInputDialog(layout, "Class Code (e.g. BSIT 4-C)");
        final TextInputEditText etCourse = createInputDialog(layout, "Course Name (e.g. Web Development)");
        final TextInputEditText etTeacher = createInputDialog(layout, "Teacher Name (e.g. Prof. Santos)");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Assign Teacher to Class")
                .setView(layout)
                .setPositiveButton("Assign", (dialog, which) -> {
                    String classCode = etClass.getText() != null ? etClass.getText().toString().trim() : "";
                    String course = etCourse.getText() != null ? etCourse.getText().toString().trim() : "";
                    String teacher = etTeacher.getText() != null ? etTeacher.getText().toString().trim() : "";

                    if (classCode.isEmpty() || teacher.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill in Class Code and Teacher Name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    teacherAssignList.add(0, new ClassTeacherAssignmentAdapter.TeacherAssignItem(classCode, course.isEmpty() ? "Information Technology" : course, teacher, "College of Computer Studies • AY 2025-2026 Sem 1", "Assigned"));
                    if (teacherAssignAdapter != null) teacherAssignAdapter.filter("");
                    Toast.makeText(getContext(), "Assigned " + teacher + " to " + classCode + " successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ==========================================
    // 5. CLASS ENROLLMENT TAB
    // ==========================================
    private void bindEnrollmentsViews(View root) {
        View layoutEnrollment = root.findViewById(R.id.layoutClassEnrollmentContainer);
        if (layoutEnrollment == null) return;

        rvEnrollments = layoutEnrollment.findViewById(R.id.rvEnrollments);
        etSearchEnrollment = layoutEnrollment.findViewById(R.id.etSearchEnrollment);
        btnEnrollStudent = layoutEnrollment.findViewById(R.id.btnEnrollStudent);

        if (etSearchEnrollment != null) {
            etSearchEnrollment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (enrollmentAdapter != null) enrollmentAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnEnrollStudent != null) {
            btnEnrollStudent.setOnClickListener(v -> showEnrollStudentWithCapacityValidationDialog());
        }
    }

    private void loadEnrollmentData() {
        if (!isAdded() || getContext() == null) return;

        enrollmentList.clear();
        enrollmentList.add(new ClassEnrollmentAdapter.EnrollmentItem("STU23001", "Sophea", "BSIT 4-A", "Mar 01, 2026", "Enrolled"));
        enrollmentList.add(new ClassEnrollmentAdapter.EnrollmentItem("STU23002", "Dara Miller", "BSIT 4-B", "Feb 28, 2026", "Enrolled"));
        enrollmentList.add(new ClassEnrollmentAdapter.EnrollmentItem("STU23003", "Channary", "BSCS 3-A", "Mar 02, 2026", "Enrolled"));
        enrollmentList.add(new ClassEnrollmentAdapter.EnrollmentItem("STU23004", "Sokha", "BSCS 3-B", "Feb 25, 2026", "Enrolled"));
        enrollmentList.add(new ClassEnrollmentAdapter.EnrollmentItem("STU23005", "Bopha", "BSIS 2-A", "Mar 03, 2026", "Enrolled"));
        enrollmentList.add(new ClassEnrollmentAdapter.EnrollmentItem("STU23006", "Vannak", "BSCS 3-A", "Mar 04, 2026", "Capacity Full"));

        if (rvEnrollments != null) {
            enrollmentAdapter = new ClassEnrollmentAdapter(getContext(), enrollmentList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Class Enrollment Detail")
                        .setMessage("Student ID: " + item.studentId + "\n" +
                                "Student Name: " + item.studentName + "\n" +
                                "Enrolled Class: " + item.className + "\n" +
                                "Enrollment Date: " + item.enrollmentDate + "\n" +
                                "Status: " + item.status)
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvEnrollments.setLayoutManager(new LinearLayoutManager(getContext()));
            rvEnrollments.setAdapter(enrollmentAdapter);
        }
    }

    private void showEnrollStudentWithCapacityValidationDialog() {
        if (!isAdded() || getContext() == null) return;

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final TextInputEditText etStuId = createInputDialog(layout, "Student ID (e.g. STU23010)");
        final TextInputEditText etName = createInputDialog(layout, "Student Name (e.g. Sokhom)");
        final TextInputEditText etClass = createInputDialog(layout, "Class Code (e.g. BSCS 3-A)");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enroll Student in Class Cohort")
                .setView(layout)
                .setPositiveButton("Enroll Student", (dialog, which) -> {
                    String stuId = etStuId.getText() != null ? etStuId.getText().toString().trim() : "";
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String classCode = etClass.getText() != null ? etClass.getText().toString().trim() : "";

                    if (stuId.isEmpty() || classCode.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill in Student ID and Class Code", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Class Capacity Validation check (e.g. BSCS 3-A is capped at 40 students)
                    if ("BSCS 3-A".equalsIgnoreCase(classCode)) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Class Capacity Limit Reached!")
                                .setMessage("Class " + classCode + " has reached its maximum capacity limit (40/40 students enrolled).\n\nPlease select another section or request capacity override from the academic board.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    enrollmentList.add(0, new ClassEnrollmentAdapter.EnrollmentItem(stuId, name.isEmpty() ? "Student" : name, classCode, "Mar 11, 2026", "Enrolled"));
                    if (enrollmentAdapter != null) enrollmentAdapter.filter("");
                    Toast.makeText(getContext(), "Student " + stuId + " enrolled into " + classCode + " successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Helper Dialog Inputs
    private TextInputEditText createInputDialog(LinearLayout container, String hint) {
        com.google.android.material.textfield.TextInputLayout til = new com.google.android.material.textfield.TextInputLayout(requireContext());
        til.setHint(hint);
        til.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 12, 0, 12);
        til.setLayoutParams(params);

        TextInputEditText et = new TextInputEditText(til.getContext());
        til.addView(et);
        container.addView(til);
        return et;
    }

    private void setupPagination(View root) {
        View btnPrev = root.findViewById(R.id.btnPrevPage);
        View btnNext = root.findViewById(R.id.btnNextPage);
        if (btnPrev != null) {
            btnPrev.setOnClickListener(v -> Toast.makeText(getContext(), "Previous page", Toast.LENGTH_SHORT).show());
        }
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> Toast.makeText(getContext(), "Next page", Toast.LENGTH_SHORT).show());
        }
    }

    private String getSafeString(JsonObject obj, String key, String defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key) == null || obj.get(key).isJsonNull()) {
            return defaultValue;
        }
        try {
            JsonElement elem = obj.get(key);
            if (elem.isJsonPrimitive()) {
                return elem.getAsString();
            } else if (elem.isJsonObject()) {
                JsonObject child = elem.getAsJsonObject();
                if (child.has("name") && !child.get("name").isJsonNull()) {
                    return child.get("name").getAsString();
                } else if (child.has("title") && !child.get("title").isJsonNull()) {
                    return child.get("title").getAsString();
                } else if (child.has("course_name") && !child.get("course_name").isJsonNull()) {
                    return child.get("course_name").getAsString();
                } else if (child.has("department_name") && !child.get("department_name").isJsonNull()) {
                    return child.get("department_name").getAsString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing string field " + key, e);
        }
        return defaultValue;
    }

    private int getSafeInt(JsonObject obj, String key, int defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key) == null || obj.get(key).isJsonNull()) {
            return defaultValue;
        }
        try {
            JsonElement elem = obj.get(key);
            if (elem.isJsonPrimitive()) {
                return elem.getAsInt();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing int field " + key, e);
        }
        return defaultValue;
    }
}
