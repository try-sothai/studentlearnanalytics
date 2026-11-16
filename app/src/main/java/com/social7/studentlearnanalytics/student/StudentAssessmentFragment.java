package com.social7.studentlearnanalytics.student;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.AssessmentAdapter;
import com.social7.studentlearnanalytics.adapter.ExamAdapter;
import com.social7.studentlearnanalytics.model.Assessment;
import com.social7.studentlearnanalytics.model.Exam;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAssessmentFragment extends Fragment {

    private TextView tabStudentAssignments, tabStudentHomework, tabStudentQuizzes, tabStudentExams;
    private TextView tvEmptyStudentAssessments;
    private RecyclerView rvStudentAssessmentList;
    private EditText edtSearchStudentAssessments;
    private String selectedCategory = "Assignment";

    private final List<Assessment> allAssignments = new ArrayList<>();
    private final List<Assessment> allHomework = new ArrayList<>();
    private final List<Assessment> allQuizzes = new ArrayList<>();
    private final List<Exam> allExams = new ArrayList<>();

    private final List<Assessment> filteredAssessments = new ArrayList<>();
    private final List<Exam> filteredExams = new ArrayList<>();

    public StudentAssessmentFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_student_assessment, container, false);

        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view, getString(R.string.my_assessments), getString(R.string.manage_assignments_quizzes_homework_and_ex)
            );
        }

        tabStudentAssignments = view.findViewById(R.id.tabStudentAssignments);
        tabStudentHomework = view.findViewById(R.id.tabStudentHomework);
        tabStudentQuizzes = view.findViewById(R.id.tabStudentQuizzes);
        tabStudentExams = view.findViewById(R.id.tabStudentExams);
        tvEmptyStudentAssessments = view.findViewById(R.id.tvEmptyStudentAssessments);

        setupSubTabListeners();

        edtSearchStudentAssessments = view.findViewById(R.id.edtSearchStudentAssessments);
        if (edtSearchStudentAssessments != null) {
            edtSearchStudentAssessments.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilterAndBind();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        rvStudentAssessmentList = view.findViewById(R.id.rvStudentAssessmentList);
        if (rvStudentAssessmentList != null) {
            rvStudentAssessmentList.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        selectCategory("Assignment");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAssessmentsForCategory(selectedCategory);
    }

    private void setupSubTabListeners() {
        if (tabStudentAssignments != null) tabStudentAssignments.setOnClickListener(v -> selectCategory("Assignment"));
        if (tabStudentHomework != null) tabStudentHomework.setOnClickListener(v -> selectCategory("Homework"));
        if (tabStudentQuizzes != null) tabStudentQuizzes.setOnClickListener(v -> selectCategory("Quiz"));
        if (tabStudentExams != null) tabStudentExams.setOnClickListener(v -> selectCategory("Exam"));
    }

    private void selectCategory(String category) {
        this.selectedCategory = category;
        updateSubTabButtonStyles(category);
        loadAssessmentsForCategory(category);
    }

    private void updateSubTabButtonStyles(String activeCategory) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabStudentAssignments, tabStudentHomework, tabStudentQuizzes, tabStudentExams};
        String[] categories = new String[]{"Assignment", "Homework", "Quiz", "Exam"};

        for (int i = 0; i < tabs.length; i++) {
            TextView tv = tabs[i];
            if (tv == null) continue;
            if (categories[i].equalsIgnoreCase(activeCategory)) {
                tv.setBackgroundResource(R.drawable.bg_notif_tab_active);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackground(null);
                tv.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
            }
        }
    }

    private void loadAssessmentsForCategory(String category) {
        if (!isAdded() || getContext() == null) return;

        if ("Quiz".equalsIgnoreCase(category)) {
            ApiClient.getApiService(getContext()).getStudentQuizzes(null).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded()) return;
                    allQuizzes.clear();
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject body = response.body();
                        JsonArray arr = body.has("data") && body.get("data").isJsonArray()
                                ? body.getAsJsonArray("data")
                                : (body.has("quizzes") && body.get("quizzes").isJsonArray()
                                ? body.getAsJsonArray("quizzes") : new JsonArray());
                        for (JsonElement el : arr) {
                            if (!el.isJsonObject()) continue;
                            JsonObject obj = el.getAsJsonObject();
                            String id = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsString() : "";
                            String title = obj.has("title") && !obj.get("title").isJsonNull() ? obj.get("title").getAsString() : "Quiz";
                            String rawDate = getFirstDate(obj, "start_at", "quiz_date", "end_at", "created_at");
                            String dateStr = formatDate(rawDate);
                            int totalScore = obj.has("total_score") && !obj.get("total_score").isJsonNull()
                                    ? Math.round(obj.get("total_score").getAsFloat()) : 100;
                            int score = 0;
                            boolean isSubmitted = false;
                            boolean isGraded = false;

                            if (obj.has("attempts") && obj.get("attempts").isJsonArray()) {
                                JsonArray atts = obj.getAsJsonArray("attempts");
                                if (atts.size() > 0 && atts.get(0).isJsonObject()) {
                                    JsonObject att = atts.get(0).getAsJsonObject();
                                    isSubmitted = true;
                                    if (att.has("score") && !att.get("score").isJsonNull()) {
                                        score = Math.round(att.get("score").getAsFloat());
                                        isGraded = true;
                                    }
                                }
                            }
                            String courseName = extractCourseName(obj);
                            allQuizzes.add(new Assessment(id, title, dateStr, score, totalScore, "quiz", null, courseName, isSubmitted, isGraded));
                        }
                    }
                    applyFilterAndBind();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    allQuizzes.clear();
                    applyFilterAndBind();
                }
            });
        } else if ("Exam".equalsIgnoreCase(category)) {
            ApiClient.getApiService(getContext()).getStudentExams(null).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded()) return;
                    allExams.clear();
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject body = response.body();
                        JsonArray arr = body.has("data") && body.get("data").isJsonArray()
                                ? body.getAsJsonArray("data")
                                : (body.has("exams") && body.get("exams").isJsonArray()
                                ? body.getAsJsonArray("exams") : new JsonArray());
                        for (JsonElement el : arr) {
                            if (!el.isJsonObject()) continue;
                            JsonObject obj = el.getAsJsonObject();
                            String id = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsString() : "";
                            String title = obj.has("title") && !obj.get("title").isJsonNull() ? obj.get("title").getAsString() : "Examination";
                            String courseName = extractCourseName(obj);
                            String displayTitle = (courseName != null && !courseName.isEmpty()) ? (courseName + " – " + title) : title;
                            String examDate = obj.has("exam_date") && !obj.get("exam_date").isJsonNull()
                                    ? obj.get("exam_date").getAsString() : "";
                            String[] md = parseMonthDay(examDate);
                            int totalMarks = obj.has("total_marks") && !obj.get("total_marks").isJsonNull()
                                    ? Math.round(obj.get("total_marks").getAsFloat())
                                    : (obj.has("total_score") && !obj.get("total_score").isJsonNull()
                                    ? Math.round(obj.get("total_score").getAsFloat()) : 100);

                            boolean completed = false;
                            String scoreText = null;
                            if (obj.has("results") && obj.get("results").isJsonArray()) {
                                JsonArray results = obj.getAsJsonArray("results");
                                if (results.size() > 0 && results.get(0).isJsonObject()) {
                                    JsonObject r = results.get(0).getAsJsonObject();
                                    if (r.has("score") && !r.get("score").isJsonNull()) {
                                        completed = true;
                                        scoreText = Math.round(r.get("score").getAsFloat()) + " / " + totalMarks;
                                    }
                                }
                            }
                            String examType = obj.has("exam_type") && !obj.get("exam_type").isJsonNull()
                                    ? obj.get("exam_type").getAsString() : "Exam";
                            String details = examType.substring(0, 1).toUpperCase(Locale.US) + examType.substring(1)
                                    + (completed ? " • Completed" : " • Scheduled");
                            allExams.add(new Exam(id, md[0], md[1], displayTitle, details, completed, scoreText));
                        }
                    }
                    applyFilterAndBind();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    allExams.clear();
                    applyFilterAndBind();
                }
            });
        } else {
            ApiClient.getApiService(getContext()).getStudentAssignments(null).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded()) return;
                    allAssignments.clear();
                    allHomework.clear();
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject body = response.body();
                        JsonArray arr = body.has("data") && body.get("data").isJsonArray()
                                ? body.getAsJsonArray("data")
                                : (body.has("assignments") && body.get("assignments").isJsonArray()
                                ? body.getAsJsonArray("assignments") : new JsonArray());
                        for (JsonElement el : arr) {
                            if (!el.isJsonObject()) continue;
                            JsonObject obj = el.getAsJsonObject();
                            String id = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsString() : "";
                            String title = obj.has("title") && !obj.get("title").isJsonNull() ? obj.get("title").getAsString() : "Assignment";
                            String rawDate = getFirstDate(obj, "due_date", "created_at");
                            String dateStr = formatDate(rawDate);
                            int totalScore = obj.has("total_score") && !obj.get("total_score").isJsonNull()
                                    ? Math.round(obj.get("total_score").getAsFloat()) : 100;
                            int score = 0;
                            boolean isSubmitted = false;
                            boolean isGraded = false;

                            if (obj.has("submissions") && obj.get("submissions").isJsonArray()) {
                                JsonArray subs = obj.getAsJsonArray("submissions");
                                if (subs.size() > 0 && subs.get(0).isJsonObject()) {
                                    JsonObject sub = subs.get(0).getAsJsonObject();
                                    isSubmitted = true;
                                    if (sub.has("score") && !sub.get("score").isJsonNull()) {
                                        score = Math.round(sub.get("score").getAsFloat());
                                        isGraded = true;
                                    }
                                }
                            }
                            String type = obj.has("assignment_type") && !obj.get("assignment_type").isJsonNull()
                                    ? obj.get("assignment_type").getAsString() : "assignment";
                            String courseName = extractCourseName(obj);
                            boolean isHw = "homework".equalsIgnoreCase(type) || title.toLowerCase(Locale.US).contains("homework");
                            Assessment item = new Assessment(id, title, dateStr, score, totalScore,
                                    isHw ? "homework" : "assignment", null, courseName, isSubmitted, isGraded);
                            if (isHw) {
                                allHomework.add(item);
                            } else {
                                allAssignments.add(item);
                            }
                        }
                    }
                    applyFilterAndBind();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    allAssignments.clear();
                    allHomework.clear();
                    applyFilterAndBind();
                }
            });
        }
    }

    private void applyFilterAndBind() {
        if (!isAdded() || rvStudentAssessmentList == null) return;
        String query = (edtSearchStudentAssessments != null && edtSearchStudentAssessments.getText() != null)
                ? edtSearchStudentAssessments.getText().toString().trim().toLowerCase(Locale.US) : "";

        if ("Exam".equalsIgnoreCase(selectedCategory)) {
            filteredExams.clear();
            for (Exam e : allExams) {
                if (query.isEmpty()
                        || (e.getCourseName() != null && e.getCourseName().toLowerCase(Locale.US).contains(query))
                        || (e.getDetails() != null && e.getDetails().toLowerCase(Locale.US).contains(query))) {
                    filteredExams.add(e);
                }
            }
            rvStudentAssessmentList.setAdapter(new ExamAdapter(requireContext(), filteredExams));
            updateEmptyState(filteredExams.isEmpty(), "No exams available yet.");
        } else {
            List<Assessment> source = "Quiz".equalsIgnoreCase(selectedCategory)
                    ? allQuizzes
                    : ("Homework".equalsIgnoreCase(selectedCategory) ? allHomework : allAssignments);
            filteredAssessments.clear();
            for (Assessment a : source) {
                if (query.isEmpty()
                        || (a.getTitle() != null && a.getTitle().toLowerCase(Locale.US).contains(query))
                        || (a.getCourseName() != null && a.getCourseName().toLowerCase(Locale.US).contains(query))) {
                    filteredAssessments.add(a);
                }
            }
            rvStudentAssessmentList.setAdapter(new AssessmentAdapter(filteredAssessments));
            updateEmptyState(filteredAssessments.isEmpty(), "No " + selectedCategory.toLowerCase(Locale.US) + " records available yet.");
        }
    }

    private void updateEmptyState(boolean empty, String msg) {
        if (tvEmptyStudentAssessments == null || rvStudentAssessmentList == null) return;
        if (empty) {
            tvEmptyStudentAssessments.setText(msg);
            tvEmptyStudentAssessments.setVisibility(View.VISIBLE);
            rvStudentAssessmentList.setVisibility(View.GONE);
        } else {
            tvEmptyStudentAssessments.setVisibility(View.GONE);
            rvStudentAssessmentList.setVisibility(View.VISIBLE);
        }
    }

    private String getFirstDate(JsonObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull()) {
                String val = obj.get(k).getAsString();
                if (val != null && !val.trim().isEmpty()) return val;
            }
        }
        return "";
    }

    private String extractCourseName(JsonObject obj) {
        if (obj.has("course") && obj.get("course").isJsonObject()) {
            JsonObject c = obj.getAsJsonObject("course");
            if (c.has("course_name") && !c.get("course_name").isJsonNull()) {
                return c.get("course_name").getAsString();
            }
            if (c.has("name") && !c.get("name").isJsonNull()) {
                return c.get("name").getAsString();
            }
        }
        return null;
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) return "Scheduled";
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
}
