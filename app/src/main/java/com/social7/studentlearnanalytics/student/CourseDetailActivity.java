package com.social7.studentlearnanalytics.student;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.AssessmentAdapter;
import com.social7.studentlearnanalytics.adapter.ExamAdapter;
import com.social7.studentlearnanalytics.model.Assessment;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.model.Exam;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseDetailActivity extends AppCompatActivity {

    private MaterialButton tabOverview, tabStudents, tabAssignments, tabQuizzes, tabExams;
    private LinearLayout layoutOverview, layoutSubContent;
    private TextView tvSubTitle, tvEmptySubContent;
    private RecyclerView recyclerCourseDetails;
    private Course currentCourse;
    private boolean isStudent = false;
    private Integer courseIdInt = null;

    private final List<Assessment> courseAssignments = new ArrayList<>();
    private final List<Assessment> courseQuizzes = new ArrayList<>();
    private final List<Exam> courseExams = new ArrayList<>();
    private int currentTabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!RoleGuard.checkAccess(this, "Student", "Teacher", "Admin")) {
            return;
        }

        String role = new SessionManager(this).getRole();
        isStudent = role != null && "student".equalsIgnoreCase(role.trim());

        setContentView(R.layout.activity_course_detail);

        initViews();
        setupCourseData();
        setupTabListeners();

        if (isStudent && courseIdInt != null && courseIdInt > 0) {
            loadStudentCourseData(courseIdInt);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!RoleGuard.checkAccess(this, "Student", "Teacher", "Admin")) {
            return;
        }
        if (isStudent && courseIdInt != null && courseIdInt > 0) {
            loadStudentCourseData(courseIdInt);
        }
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        ImageView btnMoreOptions = findViewById(R.id.btnMoreOptions);
        if (btnMoreOptions != null) btnMoreOptions.setOnClickListener(this::showMoreOptionsPopup);

        tabOverview = findViewById(R.id.tabOverview);
        tabStudents = findViewById(R.id.tabStudents);
        tabAssignments = findViewById(R.id.tabAssignments);
        tabQuizzes = findViewById(R.id.tabQuizzes);
        tabExams = findViewById(R.id.tabExams);

        layoutOverview = findViewById(R.id.layoutOverview);
        layoutSubContent = findViewById(R.id.layoutSubContent);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        tvEmptySubContent = findViewById(R.id.tvEmptySubContent);
        recyclerCourseDetails = findViewById(R.id.recyclerCourseDetails);
        if (recyclerCourseDetails != null) {
            recyclerCourseDetails.setLayoutManager(new LinearLayoutManager(this));
        }

        View cardStudentsInCourse = findViewById(R.id.cardStudentsInCourse);
        if (isStudent) {
            if (tabStudents != null) tabStudents.setVisibility(View.GONE);
            if (cardStudentsInCourse != null) cardStudentsInCourse.setVisibility(View.GONE);
        }

        TextView btnViewAllStudents = findViewById(R.id.btnViewAllStudents);
        if (btnViewAllStudents != null) {
            btnViewAllStudents.setOnClickListener(v -> {
                if (!isStudent) selectTab(1);
            });
        }
    }

    private void showMoreOptionsPopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "View Schedule");
        if (!isStudent) {
            popup.getMenu().add(0, 2, 1, "View Students");
        }
        popup.getMenu().add(0, 3, 2, "View Course Information");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                String scheduleText = (currentCourse != null && !currentCourse.getSchedule().trim().isEmpty())
                        ? "Schedule: " + currentCourse.getSchedule() + (!currentCourse.getRoom().trim().isEmpty() ? " (" + currentCourse.getRoom() + ")" : "")
                        : "Schedule: Mon, Wed 08:00–09:30";
                Toast.makeText(this, scheduleText, Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == 2 && !isStudent) {
                selectTab(1);
            } else if (item.getItemId() == 3) {
                String infoText;
                if (currentCourse != null) {
                    if (isStudent) {
                        infoText = currentCourse.getCode() + " – " + currentCourse.getName() + " • " + currentCourse.getCredits() + " Credits";
                    } else {
                        infoText = "Course Info: " + currentCourse.getCode() + " " + currentCourse.getName() + " • " + (currentCourse.getStudentCount() > 0 ? currentCourse.getStudentCount() : 35) + " Students";
                    }
                } else {
                    infoText = "Course Information";
                }
                Toast.makeText(this, infoText, Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    private void setupCourseData() {
        currentCourse = (Course) getIntent().getSerializableExtra("course");
        String extraCourseId = getIntent().getStringExtra("course_id");
        String extraCourseCode = getIntent().getStringExtra("course_code");
        String extraCourseTitle = getIntent().getStringExtra("course_title");
        String extraInstructor = getIntent().getStringExtra("instructor");
        String extraDept = getIntent().getStringExtra("course_department");
        String extraGrade = getIntent().getStringExtra("course_grade");

        String rawId = (currentCourse != null && !currentCourse.getId().isEmpty())
                ? currentCourse.getId() : extraCourseId;
        if (rawId != null) {
            try {
                courseIdInt = Integer.parseInt(rawId.trim());
            } catch (NumberFormatException ignored) {}
        }

        TextView tvCourseTitle = findViewById(R.id.tvCourseTitle);
        TextView tvCourseDept = findViewById(R.id.tvCourseDept);
        TextView tvCourseSubtext = findViewById(R.id.tvCourseSubtext);
        TextView tvStatusBadge = findViewById(R.id.tvStatusBadge);

        TextView tvChipStudentCount = findViewById(R.id.tvChipStudentCount);
        TextView tvChipSemester = findViewById(R.id.tvChipSemester);
        TextView tvChipRoom = findViewById(R.id.tvChipRoom);

        TextView tvPassRateLabel = findViewById(R.id.tvPassRateLabel);
        TextView tvClassAvgLabel = findViewById(R.id.tvClassAvgLabel);
        TextView tvPassRateVal = findViewById(R.id.tvPassRateVal);
        TextView tvPassRateBadge = findViewById(R.id.tvPassRateBadge);
        TextView tvClassAvgVal = findViewById(R.id.tvClassAvgVal);
        TextView tvClassAvgBadge = findViewById(R.id.tvClassAvgBadge);
        TextView tvTotalStudentsSubtext = findViewById(R.id.tvTotalStudentsSubtext);
        TextView tvLearningInsightsText = findViewById(R.id.tvLearningInsightsText);

        TextView tvSchedDay1 = findViewById(R.id.tvSchedDay1);
        TextView tvSchedTime1 = findViewById(R.id.tvSchedTime1);
        TextView tvSchedRoom1 = findViewById(R.id.tvSchedRoom1);
        TextView tvSchedRoom2 = findViewById(R.id.tvSchedRoom2);
        View rowSchedule2 = findViewById(R.id.rowSchedule2);

        if (isStudent) {
            if (tvPassRateLabel != null) tvPassRateLabel.setText(R.string.my_course_score);
            if (tvClassAvgLabel != null) tvClassAvgLabel.setText(R.string.course_attendance);
        }

        if (currentCourse != null) {
            String code = !currentCourse.getCode().isEmpty() ? currentCourse.getCode() : (extraCourseCode != null ? extraCourseCode : "");
            String name = !currentCourse.getName().isEmpty() ? currentCourse.getName() : (extraCourseTitle != null ? extraCourseTitle : "Course");
            if (tvCourseTitle != null) {
                tvCourseTitle.setText(!code.isEmpty() ? (code + " – " + name) : name);
            }

            String dept = !currentCourse.getDepartment().isEmpty()
                    ? currentCourse.getDepartment()
                    : (extraDept != null && !extraDept.isEmpty() ? extraDept : "University Course");
            if (tvCourseDept != null) tvCourseDept.setText(dept);

            String teacher = !currentCourse.getTeacher().isEmpty()
                    ? currentCourse.getTeacher()
                    : (extraInstructor != null && !extraInstructor.isEmpty() ? extraInstructor : "Faculty Lecturer");
            if (tvCourseSubtext != null) tvCourseSubtext.setText(tvCourseSubtext.getContext().getString(R.string.instructor) + teacher);

            if (tvStatusBadge != null) {
                tvStatusBadge.setText(!currentCourse.getStatus().isEmpty() ? currentCourse.getStatus() : "Active");
            }

            if (tvChipStudentCount != null) {
                if (isStudent) {
                    int credits = currentCourse.getCredits() > 0 ? currentCourse.getCredits() : 3;
                    tvChipStudentCount.setText(credits + " Credits");
                } else {
                    tvChipStudentCount.setText(currentCourse.getStudentCount() > 0 ? (currentCourse.getStudentCount() + " Students") : "Enrolled Roster");
                }
            }
            if (tvChipSemester != null) {
                tvChipSemester.setText(!currentCourse.getSemester().isEmpty() ? currentCourse.getSemester() : "Current Semester");
            }
            String room = !currentCourse.getRoom().isEmpty() ? currentCourse.getRoom() : "Room TBA";
            if (tvChipRoom != null) tvChipRoom.setText(room);
            if (tvSchedRoom1 != null) tvSchedRoom1.setText(room);
            if (tvSchedRoom2 != null) tvSchedRoom2.setText(room);

            String sched = currentCourse.getSchedule();
            if (sched != null && !sched.trim().isEmpty() && !sched.equalsIgnoreCase("Schedule TBA")) {
                String[] parts = sched.trim().split("\\s+", 2);
                if (tvSchedDay1 != null && parts.length >= 1) tvSchedDay1.setText(parts[0]);
                if (tvSchedTime1 != null && parts.length >= 2) tvSchedTime1.setText(parts[1]);
                if (rowSchedule2 != null) rowSchedule2.setVisibility(View.GONE);
            }

            if (tvTotalStudentsSubtext != null && !isStudent) {
                tvTotalStudentsSubtext.setText(currentCourse.getStudentCount() > 0
                        ? (tvTotalStudentsSubtext.getContext().getString(R.string.total_1fd328) + currentCourse.getStudentCount() + " Students")
                        : "Enrollment data unavailable");
            }

            float avg = currentCourse.getPerformance();
            if (isStudent) {
                if (tvPassRateVal != null) {
                    tvPassRateVal.setText(avg > 0 ? Math.round(avg) + "%" : "N/A");
                }
                if (tvPassRateBadge != null) {
                    if (extraGrade != null && !extraGrade.isEmpty()) {
                        tvPassRateBadge.setText(tvPassRateBadge.getContext().getString(R.string.grade_b6d890) + extraGrade);
                    } else {
                        tvPassRateBadge.setText(avg >= 80 ? "Good" : (avg >= 50 ? "Pass" : "In Progress"));
                    }
                }
                if (tvClassAvgVal != null) {
                    tvClassAvgVal.setText("N/A");
                }
                if (tvLearningInsightsText != null) {
                    if (avg >= 80) {
                        tvLearningInsightsText.setText("Your performance in " + code + " is strong (" + Math.round(avg) + "%). Keep up the great work!");
                    } else if (avg >= 50) {
                        tvLearningInsightsText.setText("Your current score in " + code + " is " + Math.round(avg) + "%. Review upcoming quizzes and assignments to boost your grade.");
                    } else {
                        tvLearningInsightsText.setText("Track your assignments, quizzes, and exam schedule in the tabs above to stay on top of this course.");
                    }
                }
            } else {
                if (tvClassAvgVal != null) tvClassAvgVal.setText(avg > 0 ? Math.round(avg) + "%" : "N/A");
                if (tvPassRateVal != null) tvPassRateVal.setText("N/A");
            }
        } else {
            if (tvCourseTitle != null) {
                String title = (extraCourseCode != null ? extraCourseCode + " – " : "") + (extraCourseTitle != null ? extraCourseTitle : "Course Overview");
                tvCourseTitle.setText(title);
            }
            if (tvCourseDept != null) tvCourseDept.setText(extraDept != null ? extraDept : "Academic Course Details");
            if (tvCourseSubtext != null) tvCourseSubtext.setText(tvCourseSubtext.getContext().getString(R.string.instructor) + (extraInstructor != null ? extraInstructor : "Assigned Instructor"));
            if (tvStatusBadge != null) tvStatusBadge.setText(R.string.status_active);
            if (tvChipStudentCount != null) tvChipStudentCount.setText(isStudent ? "-- Credits" : "Enrolled Roster");
            if (tvChipSemester != null) tvChipSemester.setText(R.string.current_semester);
            if (tvChipRoom != null) tvChipRoom.setText("Room TBA");
            if (tvPassRateVal != null) tvPassRateVal.setText("N/A");
            if (tvClassAvgVal != null) tvClassAvgVal.setText("N/A");
        }

        PieChart pieChart = findViewById(R.id.pieChartPerformanceSummary);
        if (pieChart != null) {
            float chartScore = (currentCourse != null && currentCourse.getPerformance() > 0)
                    ? currentCourse.getPerformance() : 0f;
            setupPieChart(pieChart, chartScore);
        }
    }

    private Double liveAssignmentAvg = null;
    private Double liveQuizAvg = null;
    private Double liveExamAvg = null;

    private void refreshStudentCoursePerformance() {
        double sum = 0.0;
        int count = 0;
        if (liveAssignmentAvg != null) { sum += liveAssignmentAvg; count++; }
        if (liveQuizAvg != null) { sum += liveQuizAvg; count++; }
        if (liveExamAvg != null) { sum += liveExamAvg; count++; }

        float score = 0f;
        if (count > 0) {
            score = (float) (sum / count);
        } else if (currentCourse != null && currentCourse.getPerformance() > 0) {
            score = currentCourse.getPerformance();
        }

        TextView tvPassRateVal = findViewById(R.id.tvPassRateVal);
        TextView tvPassRateBadge = findViewById(R.id.tvPassRateBadge);
        TextView tvLearningInsightsText = findViewById(R.id.tvLearningInsightsText);

        if (tvPassRateVal != null) {
            tvPassRateVal.setText(count > 0 || score > 0 ? Math.round(score) + "%" : "N/A");
        }
        if (tvPassRateBadge != null && (count > 0 || score > 0)) {
            tvPassRateBadge.setText(score >= 80 ? "Good" : (score >= 50 ? "Pass" : "Needs Focus"));
        }
        if (tvLearningInsightsText != null && (count > 0 || score > 0)) {
            String code = (currentCourse != null && !currentCourse.getCode().isEmpty())
                    ? currentCourse.getCode() : "this course";
            if (score >= 80) {
                tvLearningInsightsText.setText("Your performance in " + code + " is strong (" + Math.round(score) + "%). Keep up the great work!");
            } else if (score >= 50) {
                tvLearningInsightsText.setText("Your current score in " + code + " is " + Math.round(score) + "%. Review upcoming quizzes and assignments to boost your grade.");
            } else {
                tvLearningInsightsText.setText("Your current score in " + code + " is " + Math.round(score) + "%. Focus on upcoming coursework to raise your grade.");
            }
        }
        PieChart pieChart = findViewById(R.id.pieChartPerformanceSummary);
        if (pieChart != null) {
            setupPieChart(pieChart, score);
        }
    }

    private void loadStudentCourseData(int courseId) {
        long courseIdLong = (long) courseId;
        // 1. Course Attendance
        ApiClient.getApiService(this).getStudentAttendanceByCourse(null, courseIdLong)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject body = response.body();
                            JsonObject data = body.has("data") && body.get("data").isJsonObject()
                                    ? body.getAsJsonObject("data") : body;
                            JsonObject summary = data.has("summary") && data.get("summary").isJsonObject()
                                    ? data.getAsJsonObject("summary") : data;

                            int total = summary.has("total") && !summary.get("total").isJsonNull()
                                    ? summary.get("total").getAsInt() : 0;
                            int present = summary.has("present") && !summary.get("present").isJsonNull()
                                    ? summary.get("present").getAsInt() : 0;
                            Double rate = null;
                            if (summary.has("percentage") && !summary.get("percentage").isJsonNull()) {
                                rate = summary.get("percentage").getAsDouble();
                            } else if (summary.has("attendance_rate") && !summary.get("attendance_rate").isJsonNull()) {
                                rate = summary.get("attendance_rate").getAsDouble();
                            } else if (total > 0) {
                                rate = (present * 100.0) / total;
                            }

                            TextView tvClassAvgVal = findViewById(R.id.tvClassAvgVal);
                            TextView tvClassAvgBadge = findViewById(R.id.tvClassAvgBadge);
                            if (tvClassAvgVal != null) {
                                tvClassAvgVal.setText(total > 0 && rate != null ? String.format(Locale.US, "%.0f%%", rate) : "N/A");
                            }
                            if (tvClassAvgBadge != null) {
                                tvClassAvgBadge.setText(total > 0 ? (present + "/" + total) : "No sessions");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
                });

        // 2. Course Assignments
        ApiClient.getApiService(this).getStudentAssignmentsByCourse(null, courseIdLong)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            courseAssignments.clear();
                            double asnEarned = 0.0;
                            double asnMax = 0.0;
                            JsonObject body = response.body();
                            JsonArray arr = com.social7.studentlearnanalytics.utils.AssessmentUiHelper.extractDataArray(body, "assignments");
                            if (arr.size() > 0) {
                                for (JsonElement el : arr) {
                                    if (!el.isJsonObject()) continue;
                                    JsonObject obj = el.getAsJsonObject();
                                    String id = obj.has("id") ? obj.get("id").getAsString() : "";
                                    String title = obj.has("title") && !obj.get("title").isJsonNull()
                                            ? obj.get("title").getAsString() : "Assignment";
                                    String dueDate = obj.has("due_date") && !obj.get("due_date").isJsonNull()
                                            ? formatShortDate(obj.get("due_date").getAsString()) : "No due date";
                                    int maxScore = obj.has("total_score") && !obj.get("total_score").isJsonNull()
                                            ? Math.round(obj.get("total_score").getAsFloat()) : 100;

                                    boolean isSubmitted = false;
                                    boolean isGraded = false;
                                    int score = 0;
                                    if (obj.has("submissions") && obj.get("submissions").isJsonArray()) {
                                        JsonArray subs = obj.getAsJsonArray("submissions");
                                        if (subs.size() > 0 && subs.get(0).isJsonObject()) {
                                            isSubmitted = true;
                                            JsonObject sub = subs.get(0).getAsJsonObject();
                                            if (sub.has("score") && !sub.get("score").isJsonNull()) {
                                                isGraded = true;
                                                float rawSc = sub.get("score").getAsFloat();
                                                score = Math.round(rawSc);
                                                asnEarned += rawSc;
                                                asnMax += (maxScore > 0 ? maxScore : 100);
                                            }
                                        }
                                    }
                                    courseAssignments.add(new Assessment(id, title, dueDate, score, maxScore,
                                            "assignment", null, null, isSubmitted, isGraded));
                                }
                            }
                            liveAssignmentAvg = asnMax > 0 ? (asnEarned / asnMax) * 100.0 : null;
                            refreshStudentCoursePerformance();
                            if (tabAssignments != null) {
                                tabAssignments.setText(tabAssignments.getContext().getString(R.string.assignments_99fb8b) + courseAssignments.size() + ")");
                            }
                            if (currentTabIndex == 2) {
                                renderCurrentSubTab();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
                });

        // 3. Course Quizzes
        ApiClient.getApiService(this).getStudentQuizzesByCourse(null, courseIdLong)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            courseQuizzes.clear();
                            double qzEarned = 0.0;
                            double qzMax = 0.0;
                            JsonObject body = response.body();
                            JsonArray arr = com.social7.studentlearnanalytics.utils.AssessmentUiHelper.extractDataArray(body, "quizzes");
                            if (arr.size() > 0) {
                                for (JsonElement el : arr) {
                                    if (!el.isJsonObject()) continue;
                                    JsonObject obj = el.getAsJsonObject();
                                    String id = obj.has("id") ? obj.get("id").getAsString() : "";
                                    String title = obj.has("title") && !obj.get("title").isJsonNull()
                                            ? obj.get("title").getAsString() : "Quiz";
                                    String dateStr = obj.has("quiz_date") && !obj.get("quiz_date").isJsonNull()
                                            ? formatShortDate(obj.get("quiz_date").getAsString())
                                            : (obj.has("created_at") && !obj.get("created_at").isJsonNull()
                                            ? formatShortDate(obj.get("created_at").getAsString()) : "Scheduled");
                                    int maxScore = obj.has("total_score") && !obj.get("total_score").isJsonNull()
                                            ? Math.round(obj.get("total_score").getAsFloat()) : 100;

                                    boolean isSubmitted = false;
                                    boolean isGraded = false;
                                    int score = 0;
                                    if (obj.has("attempts") && obj.get("attempts").isJsonArray()) {
                                        JsonArray atts = obj.getAsJsonArray("attempts");
                                        if (atts.size() > 0 && atts.get(0).isJsonObject()) {
                                            isSubmitted = true;
                                            JsonObject att = atts.get(0).getAsJsonObject();
                                            if (att.has("score") && !att.get("score").isJsonNull()) {
                                                isGraded = true;
                                                float rawSc = att.get("score").getAsFloat();
                                                score = Math.round(rawSc);
                                                qzEarned += rawSc;
                                                qzMax += (maxScore > 0 ? maxScore : 100);
                                            }
                                        }
                                    }
                                    courseQuizzes.add(new Assessment(id, title, dateStr, score, maxScore,
                                            "quiz", null, null, isSubmitted, isGraded));
                                }
                            }
                            liveQuizAvg = qzMax > 0 ? (qzEarned / qzMax) * 100.0 : null;
                            refreshStudentCoursePerformance();
                            if (tabQuizzes != null) {
                                tabQuizzes.setText(tabQuizzes.getContext().getString(R.string.quizzes_8e16c4) + courseQuizzes.size() + ")");
                            }
                            if (currentTabIndex == 3) {
                                renderCurrentSubTab();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
                });

        // 4. Course Exams
        ApiClient.getApiService(this).getStudentExamsByCourse(null, courseIdLong)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            courseExams.clear();
                            double exEarned = 0.0;
                            double exMax = 0.0;
                            JsonObject body = response.body();
                            JsonArray arr = com.social7.studentlearnanalytics.utils.AssessmentUiHelper.extractDataArray(body, "exams");
                            if (arr.size() > 0) {
                                for (JsonElement el : arr) {
                                    if (!el.isJsonObject()) continue;
                                    JsonObject obj = el.getAsJsonObject();
                                    String id = obj.has("id") ? obj.get("id").getAsString() : "";
                                    String title = obj.has("title") && !obj.get("title").isJsonNull()
                                            ? obj.get("title").getAsString() : "Exam";
                                    String examType = obj.has("exam_type") && !obj.get("exam_type").isJsonNull()
                                            ? obj.get("exam_type").getAsString() : "Exam";
                                    String examDate = obj.has("exam_date") && !obj.get("exam_date").isJsonNull()
                                            ? obj.get("exam_date").getAsString() : "";
                                    String[] monthDay = parseMonthDay(examDate);
                                    int totalMarks = obj.has("total_marks") && !obj.get("total_marks").isJsonNull()
                                            ? Math.round(obj.get("total_marks").getAsFloat())
                                            : (obj.has("total_score") && !obj.get("total_score").isJsonNull()
                                            ? Math.round(obj.get("total_score").getAsFloat()) : 100);

                                    boolean completed = false;
                                    String scoreText = null;
                                    if (obj.has("results") && obj.get("results").isJsonArray()) {
                                        JsonArray res = obj.getAsJsonArray("results");
                                        if (res.size() > 0 && res.get(0).isJsonObject()) {
                                            JsonObject r = res.get(0).getAsJsonObject();
                                            if (r.has("score") && !r.get("score").isJsonNull()) {
                                                completed = true;
                                                float sc = r.get("score").getAsFloat();
                                                scoreText = Math.round(sc) + " / " + totalMarks;
                                                exEarned += sc;
                                                exMax += (totalMarks > 0 ? totalMarks : 100);
                                            }
                                        }
                                    }
                                    String details = capitalize(examType) + (completed ? " • Completed" : " • Upcoming");
                                    courseExams.add(new Exam(id, monthDay[0], monthDay[1], title, details, completed, scoreText));
                                }
                            }
                            liveExamAvg = exMax > 0 ? (exEarned / exMax) * 100.0 : null;
                            refreshStudentCoursePerformance();
                            if (tabExams != null) {
                                tabExams.setText(tabExams.getContext().getString(R.string.exams_9ccb3c) + courseExams.size() + ")");
                            }
                            if (currentTabIndex == 4) {
                                renderCurrentSubTab();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
                });
    }

    private void setupPieChart(PieChart chart, float avgScore) {
        float passed = Math.max(0f, Math.min(100f, avgScore));
        float remaining = Math.max(0f, 100f - passed);

        List<PieEntry> entries = new ArrayList<>();
        if (avgScore > 0) {
            entries.add(new PieEntry(passed, "Score"));
            if (remaining > 0) entries.add(new PieEntry(remaining, "Remaining"));
        } else {
            entries.add(new PieEntry(100f, "In Progress"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        if (avgScore > 0) {
            dataSet.setColors(new int[]{
                    Color.parseColor("#16B978"),
                    Color.parseColor("#E2E8F0")
            });
        } else {
            dataSet.setColors(new int[]{Color.parseColor("#CBD5E1")});
        }
        dataSet.setSliceSpace(2f);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(chart));

        chart.setUsePercentValues(true);
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(55f);
        chart.setTransparentCircleRadius(60f);
        chart.setHoleColor(Color.WHITE);

        chart.setCenterText(avgScore > 0 ? (Math.round(avgScore) + "%\nScore") : "In\nProgress");
        chart.setCenterTextColor(Color.parseColor("#111B67"));
        chart.setCenterTextSize(13f);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setData(pieData);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }

    private void setupTabListeners() {
        if (tabOverview != null) tabOverview.setOnClickListener(v -> selectTab(0));
        if (tabStudents != null) tabStudents.setOnClickListener(v -> selectTab(1));
        if (tabAssignments != null) tabAssignments.setOnClickListener(v -> selectTab(2));
        if (tabQuizzes != null) tabQuizzes.setOnClickListener(v -> selectTab(3));
        if (tabExams != null) tabExams.setOnClickListener(v -> selectTab(4));
    }

    private void selectTab(int index) {
        currentTabIndex = index;
        resetTabStyles();

        int primaryBlue = Color.parseColor("#2463FF");
        int white = Color.WHITE;

        int studentCount = (currentCourse != null && currentCourse.getStudentCount() > 0) ? currentCourse.getStudentCount() : 0;

        if (index == 0) {
            setButtonActive(tabOverview, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.VISIBLE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.GONE);
        } else if (index == 1) {
            setButtonActive(tabStudents, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.GONE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.VISIBLE);
            if (tvSubTitle != null) tvSubTitle.setText(tvSubTitle.getContext().getString(R.string.enrolled_students_bfac25) + studentCount + ")");
        } else if (index == 2) {
            setButtonActive(tabAssignments, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.GONE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.VISIBLE);
            renderCurrentSubTab();
        } else if (index == 3) {
            setButtonActive(tabQuizzes, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.GONE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.VISIBLE);
            renderCurrentSubTab();
        } else if (index == 4) {
            setButtonActive(tabExams, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.GONE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.VISIBLE);
            renderCurrentSubTab();
        }
    }

    private void renderCurrentSubTab() {
        if (recyclerCourseDetails == null) return;
        if (currentTabIndex == 2) {
            if (tvSubTitle != null) {
                tvSubTitle.setText(tvSubTitle.getContext().getString(R.string.course_assignments_cc7fae) + courseAssignments.size() + ")");
            }
            recyclerCourseDetails.setAdapter(new AssessmentAdapter(courseAssignments));
            updateEmptySubState(courseAssignments.isEmpty(), "No assignments found for this course.");
        } else if (currentTabIndex == 3) {
            if (tvSubTitle != null) {
                tvSubTitle.setText(tvSubTitle.getContext().getString(R.string.course_quizzes) + courseQuizzes.size() + ")");
            }
            recyclerCourseDetails.setAdapter(new AssessmentAdapter(courseQuizzes));
            updateEmptySubState(courseQuizzes.isEmpty(), "No quizzes found for this course.");
        } else if (currentTabIndex == 4) {
            if (tvSubTitle != null) {
                tvSubTitle.setText(tvSubTitle.getContext().getString(R.string.course_exams) + courseExams.size() + ")");
            }
            recyclerCourseDetails.setAdapter(new ExamAdapter(this, courseExams));
            updateEmptySubState(courseExams.isEmpty(), "No exams found for this course.");
        }
    }

    private void updateEmptySubState(boolean isEmpty, String message) {
        if (tvEmptySubContent == null || recyclerCourseDetails == null) return;
        if (isEmpty) {
            tvEmptySubContent.setText(message);
            tvEmptySubContent.setVisibility(View.VISIBLE);
            recyclerCourseDetails.setVisibility(View.GONE);
        } else {
            tvEmptySubContent.setVisibility(View.GONE);
            recyclerCourseDetails.setVisibility(View.VISIBLE);
        }
    }

    private String formatShortDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "Scheduled";
        try {
            String clean = rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;
            SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outFmt = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            Date d = inFmt.parse(clean);
            if (d != null) return outFmt.format(d);
        } catch (Exception ignored) {}
        return rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;
    }

    private String[] parseMonthDay(String rawDate) {
        if (rawDate != null && rawDate.length() >= 10) {
            try {
                String clean = rawDate.substring(0, 10);
                SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date d = inFmt.parse(clean);
                if (d != null) {
                    String m = new SimpleDateFormat("MMM", Locale.US).format(d).toUpperCase(Locale.US);
                    String day = new SimpleDateFormat("dd", Locale.US).format(d);
                    return new String[]{m, day};
                }
            } catch (Exception ignored) {}
        }
        return new String[]{"TBA", "--"};
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1);
    }

    private void setButtonActive(MaterialButton btn, int bgTint, int textColor) {
        if (btn == null) return;
        btn.setBackgroundColor(bgTint);
        btn.setTextColor(textColor);
        btn.setIconTintResource(R.color.white);
    }

    private void resetTabStyles() {
        int surfaceLight = Color.parseColor("#EAF2FF");
        int primaryBlue = Color.parseColor("#2463FF");

        setButtonInactive(tabOverview, surfaceLight, primaryBlue);
        setButtonInactive(tabStudents, surfaceLight, primaryBlue);
        setButtonInactive(tabAssignments, surfaceLight, primaryBlue);
        setButtonInactive(tabQuizzes, surfaceLight, primaryBlue);
        setButtonInactive(tabExams, surfaceLight, primaryBlue);
    }

    private void setButtonInactive(MaterialButton btn, int bgTint, int textColor) {
        if (btn == null) return;
        btn.setBackgroundColor(bgTint);
        btn.setTextColor(textColor);
        btn.setIconTintResource(R.color.teacher_primary_blue);
    }
}
