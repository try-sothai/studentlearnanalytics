package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentCoursesFragment extends Fragment {

    private static final String TAG = "SLAS_COURSES";

    private TextView tvSemesterText;
    private TextView tvStatCourseCount;
    private TextView tvStatGpa;
    private TextView tvStatAverage;
    private TextView tvStatCompleted;
    private LinearLayout containerCourseList;

    private List<DashboardResponse.EnrollmentInfo> allEnrollments = new ArrayList<>();
    private Map<String, List<DashboardResponse.EnrollmentInfo>> groupedBySemester = new LinkedHashMap<>();
    private String currentSelectedSemester = "";
    private DashboardResponse.DashboardData dashboardData;

    public StudentCoursesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_student_courses,
                container,
                false
        );

        View headerContainer = view.findViewById(R.id.headerContainer);
        if (headerContainer != null) {
            int initialTopPadding = headerContainer.getPaddingTop();
            ViewCompat.setOnApplyWindowInsetsListener(headerContainer, (v, insets) -> {
                Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.setPadding(
                        v.getPaddingLeft(),
                        statusBarInsets.top + initialTopPadding,
                        v.getPaddingRight(),
                        v.getPaddingBottom()
                );
                return insets;
            });
        }

        tvSemesterText = view.findViewById(R.id.tvSemesterText);
        tvStatCourseCount = view.findViewById(R.id.tvStatCourseCount);
        tvStatGpa = view.findViewById(R.id.tvStatGpa);
        tvStatAverage = view.findViewById(R.id.tvStatAverage);
        tvStatCompleted = view.findViewById(R.id.tvStatCompleted);
        containerCourseList = view.findViewById(R.id.containerCourseList);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof StudentMainActivity) {
                    BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                    if (nav != null) {
                        nav.setSelectedItemId(R.id.nav_home);
                    }
                } else if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        View btnSearch = view.findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> showSearchDialog());
        }

        View cardSemesterSelector = view.findViewById(R.id.cardSemesterSelector);
        if (cardSemesterSelector != null) {
            cardSemesterSelector.setOnClickListener(v -> showSemesterDialog());
        }

        fetchStudentCourses();

        return view;
    }

    private void fetchStudentCourses() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && isAdded()) {
                    dashboardData = response.body().getData();
                    List<DashboardResponse.EnrollmentInfo> enrollments = dashboardData.getEnrollments();
                    if (enrollments != null && !enrollments.isEmpty()) {
                        allEnrollments = enrollments;
                        groupEnrollmentsBySemester(enrollments);
                        renderSelectedSemester();
                    } else {
                        renderEmptyCourses("No enrolled courses available.");
                    }
                } else {
                    Log.d(TAG, "Courses response code: " + response.code());
                    renderEmptyCourses("Unable to load courses. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                Log.d(TAG, "Courses API failed: " + t.getMessage());
                renderEmptyCourses("Network error. Please check your connection.");
            }
        });
    }

    private void groupEnrollmentsBySemester(List<DashboardResponse.EnrollmentInfo> enrollments) {
        groupedBySemester.clear();
        for (DashboardResponse.EnrollmentInfo info : enrollments) {
            if (info == null) continue;
            String semKey = "Semester 1";
            if (info.semester != null && info.semester.name != null && !info.semester.name.trim().isEmpty()) {
                String semName = info.semester.name.trim();
                String yearId = info.semester.academicYearId != null ? String.valueOf(info.semester.academicYearId).trim() : "";
                semKey = semName + (!yearId.isEmpty() ? " (Year " + yearId + ")" : "");
            }
            if (!groupedBySemester.containsKey(semKey)) {
                groupedBySemester.put(semKey, new ArrayList<>());
            }

            List<DashboardResponse.EnrollmentInfo> list = groupedBySemester.get(semKey);
            if (list != null) {
                boolean exists = false;
                if (info.course != null && info.course.id != null) {
                    for (DashboardResponse.EnrollmentInfo existing : list) {
                        if (existing != null && existing.course != null && info.course.id.equals(existing.course.id)) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    list.add(info);
                }
            }
        }

        if (!groupedBySemester.isEmpty() && currentSelectedSemester.isEmpty()) {
            currentSelectedSemester = groupedBySemester.keySet().iterator().next();
        }
    }

    private void renderSelectedSemester() {
        if (currentSelectedSemester.isEmpty() && !groupedBySemester.isEmpty()) {
            currentSelectedSemester = groupedBySemester.keySet().iterator().next();
        }

        if (tvSemesterText != null) {
            tvSemesterText.setText(currentSelectedSemester);
        }

        List<DashboardResponse.EnrollmentInfo> currentList = groupedBySemester.get(currentSelectedSemester);
        if (currentList == null) {
            currentList = allEnrollments;
        }

        int totalCourses = currentList.size();
        float scoreSum = 0f;
        int completedCount = 0;

        for (DashboardResponse.EnrollmentInfo info : currentList) {
            float score = info.finalScore != null ? info.finalScore : 0f;
            scoreSum += score;
            if (info.status != null && ("completed".equalsIgnoreCase(info.status) || "enrolled".equalsIgnoreCase(info.status))) {
                completedCount++;
            }
        }

        float avgScore = totalCourses > 0 ? scoreSum / totalCourses : 0f;

        if (tvStatCourseCount != null) tvStatCourseCount.setText(String.valueOf(totalCourses));
        if (tvStatGpa != null) {
            String gpaStr = dashboardData != null ? formatGpa(dashboardData.getGpaValue()) : null;
            tvStatGpa.setText(gpaStr != null ? gpaStr : "--");
        }
        if (tvStatAverage != null) tvStatAverage.setText(String.format(Locale.US, "%.1f%%", avgScore));
        if (tvStatCompleted != null) tvStatCompleted.setText(String.valueOf(completedCount));

        renderCourseCards(currentList);
    }

    private void renderCourseCards(List<DashboardResponse.EnrollmentInfo> enrollments) {
        if (containerCourseList == null) return;
        containerCourseList.removeAllViews();

        if (enrollments == null || enrollments.isEmpty()) {
            renderEmptyCourses("No courses found for " + currentSelectedSemester);
            return;
        }

        for (DashboardResponse.EnrollmentInfo info : enrollments) {
            if (info == null || info.course == null) continue;

            DashboardResponse.CourseModelInfo course = info.course;
            String cName = course.courseName != null ? course.courseName.trim() : "Course";
            String cCode = course.courseCode != null ? course.courseCode.trim() : "CS";
            String teacherName = course.teacher != null && course.teacher.user != null && course.teacher.user.name != null
                    ? course.teacher.user.name : "Lecturer";
            float finalScore = info.finalScore != null ? info.finalScore : 0f;

            CardView card = new CardView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, dp(16));
            card.setLayoutParams(lp);
            card.setRadius(dp(22));
            card.setCardElevation(dp(3));
            card.setCardBackgroundColor(Color.WHITE);

            LinearLayout inner = new LinearLayout(requireContext());
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(dp(16), dp(16), dp(16), dp(16));

            // Top Row: Info Container + Chevron
            LinearLayout topRow = new LinearLayout(requireContext());
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // Icon Frame
            View iconFrame = new View(requireContext());
            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(48), dp(48));
            iconFrame.setLayoutParams(iconLp);
            iconFrame.setBackgroundResource(R.drawable.bg_course_icon_blue);

            // Text Info Container
            LinearLayout infoCol = new LinearLayout(requireContext());
            infoCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            infoLp.setMargins(dp(12), 0, dp(8), 0);
            infoCol.setLayoutParams(infoLp);

            TextView tvName = new TextView(requireContext());
            tvName.setText(cName);
            tvName.setTextColor(Color.parseColor("#111B6B"));
            tvName.setTextSize(16);
            tvName.setTypeface(null, Typeface.BOLD);
            infoCol.addView(tvName);

            TextView tvSub = new TextView(requireContext());
            tvSub.setText(cCode + " • " + teacherName);
            tvSub.setTextColor(Color.parseColor("#7A7D8F"));
            tvSub.setTextSize(12);
            tvSub.setPadding(0, dp(2), 0, 0);
            infoCol.addView(tvSub);

            // Badges Row
            LinearLayout badgesRow = new LinearLayout(requireContext());
            badgesRow.setOrientation(LinearLayout.HORIZONTAL);
            badgesRow.setPadding(0, dp(6), 0, 0);

            TextView badgeType = new TextView(requireContext());
            badgeType.setText("Core");
            badgeType.setTextColor(Color.parseColor("#315CFF"));
            badgeType.setTextSize(11);
            badgeType.setTypeface(null, Typeface.BOLD);
            badgeType.setPadding(dp(8), dp(3), dp(8), dp(3));
            badgeType.setBackgroundResource(R.drawable.bg_badge_core);
            badgesRow.addView(badgeType);

            TextView badgeCredit = new TextView(requireContext());
            float cr = course.credit != null ? course.credit : 3.0f;
            badgeCredit.setText(String.format(Locale.US, "%.0f Credits", cr));
            badgeCredit.setTextColor(Color.parseColor("#64748B"));
            badgeCredit.setTextSize(11);
            badgeCredit.setPadding(dp(8), dp(3), dp(8), dp(3));
            badgeCredit.setBackgroundResource(R.drawable.bg_badge_credit);
            LinearLayout.LayoutParams crLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            crLp.setMargins(dp(8), 0, 0, 0);
            badgeCredit.setLayoutParams(crLp);
            badgesRow.addView(badgeCredit);

            infoCol.addView(badgesRow);

            // Right Chevron
            ImageView btnChevron = new ImageView(requireContext());
            btnChevron.setImageResource(R.drawable.ic_chevron_right_purple);
            btnChevron.setColorFilter(Color.parseColor("#315CFF"));
            LinearLayout.LayoutParams chLp = new LinearLayout.LayoutParams(dp(18), dp(18));
            btnChevron.setLayoutParams(chLp);

            topRow.addView(iconFrame);
            topRow.addView(infoCol);
            topRow.addView(btnChevron);

            inner.addView(topRow);

            // Progress Bar Row
            LinearLayout progressRow = new LinearLayout(requireContext());
            progressRow.setOrientation(LinearLayout.HORIZONTAL);
            progressRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
            progressRow.setPadding(0, dp(12), 0, 0);

            ProgressBar progressBar = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(0, dp(8), 1f);
            progressBar.setLayoutParams(pbLp);
            progressBar.setMax(100);
            progressBar.setProgress((int) finalScore);
            progressBar.setProgressDrawable(requireContext().getDrawable(R.drawable.progress_bar_blue));

            TextView tvScore = new TextView(requireContext());
            tvScore.setText(String.format(Locale.US, "%.1f%%", finalScore));
            tvScore.setTextColor(Color.parseColor("#315CFF"));
            tvScore.setTextSize(13);
            tvScore.setTypeface(null, Typeface.BOLD);
            tvScore.setPadding(dp(12), 0, 0, 0);

            progressRow.addView(progressBar);
            progressRow.addView(tvScore);

            inner.addView(progressRow);

            card.addView(inner);

            // Click listener opens CourseDetailActivity
            Course cObj = new Course(
                    course.id != null ? course.id : "1",
                    cCode,
                    cName,
                    teacherName,
                    (int) finalScore,
                    course.credit != null ? course.credit.intValue() : 3,
                    "Core",
                    88, 85, 78, 76, "Computer Science"
            );
            card.setOnClickListener(v -> openCourseDetail(cObj));

            containerCourseList.addView(card);
        }
    }

    private void renderEmptyCourses(String message) {
        if (containerCourseList == null) return;
        containerCourseList.removeAllViews();

        TextView tvEmpty = new TextView(requireContext());
        tvEmpty.setText(message);
        tvEmpty.setTextColor(Color.parseColor("#64748B"));
        tvEmpty.setTextSize(14);
        tvEmpty.setPadding(0, dp(20), 0, dp(20));
        containerCourseList.addView(tvEmpty);
    }

    private void openCourseDetail(Course course) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), CourseDetailActivity.class);
            intent.putExtra("course", course);
            startActivity(intent);
        }
    }

    private void showSearchDialog() {
        if (getContext() == null) return;

        final EditText etSearch = new EditText(getContext());
        etSearch.setHint("Type course name or code...");
        etSearch.setPadding(40, 30, 40, 30);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Search Courses")
                .setView(etSearch)
                .setPositiveButton("Close", null)
                .create();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCoursesBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void filterCoursesBySearch(String query) {
        if (query.trim().isEmpty()) {
            renderSelectedSemester();
            return;
        }

        List<DashboardResponse.EnrollmentInfo> filtered = new ArrayList<>();
        List<DashboardResponse.EnrollmentInfo> currentList = groupedBySemester.get(currentSelectedSemester);
        if (currentList == null) currentList = allEnrollments;

        String q = query.toLowerCase().trim();
        for (DashboardResponse.EnrollmentInfo info : currentList) {
            if (info != null && info.course != null) {
                String name = info.course.courseName != null ? info.course.courseName.toLowerCase() : "";
                String code = info.course.courseCode != null ? info.course.courseCode.toLowerCase() : "";
                if (name.contains(q) || code.contains(q)) {
                    filtered.add(info);
                }
            }
        }

        renderCourseCards(filtered);
    }

    private void showSemesterDialog() {
        if (getContext() == null) return;

        List<String> semestersList = new ArrayList<>(groupedBySemester.keySet());
        if (semestersList.isEmpty()) {
            semestersList.add("Semester 1, 2025-2026");
            semestersList.add("Semester 2, 2025-2026");
            semestersList.add("Semester 1, 2026-2027");
            semestersList.add("Semester 2, 2026-2027");
        }

        String[] semestersArray = semestersList.toArray(new String[0]);

        new AlertDialog.Builder(getContext())
                .setTitle("Select Semester")
                .setItems(semestersArray, (dialog, which) -> {
                    currentSelectedSemester = semestersArray[which];
                    renderSelectedSemester();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int dp(int value) {
        if (getContext() == null) return value;
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String formatGpa(Object value) {
        if (value == null) return null;
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty() || "null".equalsIgnoreCase(raw)) return null;
        try {
            double gpa = Double.parseDouble(raw);
            if (Double.isNaN(gpa) || Double.isInfinite(gpa) || gpa < 0 || gpa > 4) return null;
            return String.format(Locale.US, "%.2f", gpa);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
