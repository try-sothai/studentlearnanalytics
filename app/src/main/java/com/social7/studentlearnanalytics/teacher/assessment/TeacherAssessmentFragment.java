package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.TeacherMainActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherAssessmentFragment extends Fragment {

    private TextView tabAssessDashboard, tabAssessAssignments, tabAssessQuizzes, tabAssessExams, tabAssessSubmissions, tabAssessGrading;
    private View secAssessDashboardContainer, secAssessAssignmentsContainer, secAssessQuizzesContainer, secAssessExamsContainer, secAssessSubmissionsContainer, secAssessGradingContainer;

    private TextView tvMetricTotalAssignments, tvMetricTotalQuizzes, tvMetricTotalExams, tvMetricPublished, tvMetricDrafts, tvMetricPendingGrading, tvCourseProgressPlaceholder;
    private LinearLayout layoutCourseProgressRows;

    private MaterialButton btnCreateAssessment;

    private RecyclerView rvAssignmentsList;
    private AssignmentAdapter assignmentAdapter;
    private final List<AssignmentAdapter.AssignmentCardItem> assignmentList = new ArrayList<>();
    private EditText edtSearchAssignments;

    private RecyclerView rvQuizzesList;
    private QuizAdapter quizAdapter;
    private final List<QuizAdapter.QuizCardItem> quizList = new ArrayList<>();
    private EditText edtSearchQuizzes;

    private RecyclerView rvExamsList;
    private TeacherExamAdapter examAdapter;
    private final List<TeacherExamAdapter.ExamCardItem> examList = new ArrayList<>();
    private EditText edtSearchExams;

    private RecyclerView rvSubmissionsList;
    private SubmissionAdapter submissionAdapter;
    private final List<SubmissionAdapter.SubmissionCardItem> submissionList = new ArrayList<>();
    private EditText edtSearchSubmissions;

    private RecyclerView rvGradingList;
    private GradingAdapter gradingAdapter;
    private final List<GradingAdapter.GradingCardItem> gradingList = new ArrayList<>();
    private EditText edtSearchGrading;

    private int selectedSubTab = 0;

    public TeacherAssessmentFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_teacher_assessment, container, false);

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view, getString(R.string.assessment_management), "Manage assignments, quizzes, student submissions, and grading"
            );
        }

        tabAssessDashboard = view.findViewById(R.id.tabAssessDashboard);
        tabAssessAssignments = view.findViewById(R.id.tabAssessAssignments);
        tabAssessQuizzes = view.findViewById(R.id.tabAssessQuizzes);
        tabAssessExams = view.findViewById(R.id.tabAssessExams);
        tabAssessSubmissions = view.findViewById(R.id.tabAssessSubmissions);
        tabAssessGrading = view.findViewById(R.id.tabAssessGrading);

        secAssessDashboardContainer = view.findViewById(R.id.secAssessDashboardContainer);
        secAssessAssignmentsContainer = view.findViewById(R.id.secAssessAssignmentsContainer);
        secAssessQuizzesContainer = view.findViewById(R.id.secAssessQuizzesContainer);
        secAssessExamsContainer = view.findViewById(R.id.secAssessExamsContainer);
        secAssessSubmissionsContainer = view.findViewById(R.id.secAssessSubmissionsContainer);
        secAssessGradingContainer = view.findViewById(R.id.secAssessGradingContainer);

        bindDashboardViews(view);
        bindAssignmentsViews(view);
        bindQuizzesViews(view);
        bindExamsViews(view);
        bindSubmissionsViews(view);
        bindGradingViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);
        fetchAssessmentsOverview();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && getContext() != null) {
            fetchAssessmentsOverview();
        }
    }

    private void setupSubTabListeners() {
        if (tabAssessDashboard != null) tabAssessDashboard.setOnClickListener(v -> selectSubTab(0));
        if (tabAssessAssignments != null) tabAssessAssignments.setOnClickListener(v -> selectSubTab(1));
        if (tabAssessQuizzes != null) tabAssessQuizzes.setOnClickListener(v -> selectSubTab(2));
        if (tabAssessExams != null) tabAssessExams.setOnClickListener(v -> selectSubTab(3));
        if (tabAssessSubmissions != null) tabAssessSubmissions.setOnClickListener(v -> selectSubTab(4));
        if (tabAssessGrading != null) tabAssessGrading.setOnClickListener(v -> selectSubTab(5));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secAssessDashboardContainer != null) secAssessDashboardContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secAssessAssignmentsContainer != null) secAssessAssignmentsContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secAssessQuizzesContainer != null) secAssessQuizzesContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secAssessExamsContainer != null) secAssessExamsContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);
        if (secAssessSubmissionsContainer != null) secAssessSubmissionsContainer.setVisibility(subIndex == 4 ? View.VISIBLE : View.GONE);
        if (secAssessGradingContainer != null) secAssessGradingContainer.setVisibility(subIndex == 5 ? View.VISIBLE : View.GONE);
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabAssessDashboard, tabAssessAssignments, tabAssessQuizzes, tabAssessExams, tabAssessSubmissions, tabAssessGrading};
        for (int i = 0; i < tabs.length; i++) {
            TextView tv = tabs[i];
            if (tv == null) continue;
            if (i == activeIndex) {
                tv.setBackgroundResource(R.drawable.bg_notif_tab_active);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackground(null);
                tv.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
            }
        }
    }

    private void fetchAssessmentsOverview() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getTeacherAssessmentsOverview(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    JsonArray assignmentsArr = data.has("assignments") && data.get("assignments").isJsonArray()
                            ? data.getAsJsonArray("assignments") : null;
                    JsonArray quizzesArr = data.has("quizzes") && data.get("quizzes").isJsonArray()
                            ? data.getAsJsonArray("quizzes") : null;
                    JsonArray examsArr = data.has("exams") && data.get("exams").isJsonArray()
                            ? data.getAsJsonArray("exams") : null;
                    JsonArray submissionsArr = data.has("submissions") && data.get("submissions").isJsonArray()
                            ? data.getAsJsonArray("submissions") : null;

                    bindDashboardMetrics(data, assignmentsArr, quizzesArr, examsArr);
                    bindAssignmentsData(assignmentsArr);
                    bindQuizzesData(quizzesArr);
                    bindExamsData(examsArr);
                    bindSubmissionsAndGradingData(submissionsArr);
                } else {
                    Toast.makeText(getContext(), com.social7.studentlearnanalytics.utils.AssessmentUiHelper.extractErrorMessage(response, "Unable to load assessments"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error loading assessments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindDashboardViews(View root) {
        tvMetricTotalAssignments = root.findViewById(R.id.tvMetricTotalAssignments);
        tvMetricTotalQuizzes = root.findViewById(R.id.tvMetricTotalQuizzes);
        tvMetricTotalExams = root.findViewById(R.id.tvMetricTotalExams);
        tvMetricPublished = root.findViewById(R.id.tvMetricPublished);
        tvMetricDrafts = root.findViewById(R.id.tvMetricDrafts);
        tvMetricPendingGrading = root.findViewById(R.id.tvMetricPendingGrading);
        tvCourseProgressPlaceholder = root.findViewById(R.id.tvCourseProgressPlaceholder);
        layoutCourseProgressRows = root.findViewById(R.id.layoutCourseProgressRows);

        btnCreateAssessment = root.findViewById(R.id.btnCreateAssessment);
        if (btnCreateAssessment != null) {
            btnCreateAssessment.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), AddAssessmentActivity.class);
                startActivity(intent);
            });
        }
    }

    private void bindDashboardMetrics(JsonObject data, JsonArray assignmentsArr, JsonArray quizzesArr, JsonArray examsArr) {
        JsonObject summary = data.has("summary") && data.get("summary").isJsonObject()
                ? data.getAsJsonObject("summary") : new JsonObject();
        int totalAsn = getInt(summary, "total_assignments");
        int totalQz = getInt(summary, "total_quizzes");
        int totalEx = getInt(summary, "total_exams");
        int pendingSubs = getInt(summary, "pending_submissions");

        int publishedCount = 0;
        int draftCount = 0;
        if (assignmentsArr != null) {
            for (JsonElement el : assignmentsArr) {
                if (!el.isJsonObject()) continue;
                String st = getStr(el.getAsJsonObject(), "raw_status", getStr(el.getAsJsonObject(), "status", ""));
                if ("draft".equalsIgnoreCase(st)) draftCount++;
                else if ("published".equalsIgnoreCase(st)) publishedCount++;
            }
        }
        if (quizzesArr != null) {
            for (JsonElement el : quizzesArr) {
                if (!el.isJsonObject()) continue;
                String st = getStr(el.getAsJsonObject(), "raw_status", getStr(el.getAsJsonObject(), "status", ""));
                if ("draft".equalsIgnoreCase(st)) draftCount++;
                else if ("published".equalsIgnoreCase(st) || "open".equalsIgnoreCase(st)) publishedCount++;
            }
        }
        if (examsArr != null) {
            for (JsonElement el : examsArr) {
                if (!el.isJsonObject()) continue;
                String st = getStr(el.getAsJsonObject(), "raw_status", "");
                if ("draft".equalsIgnoreCase(st)) draftCount++;
                else if ("scheduled".equalsIgnoreCase(st) || "ongoing".equalsIgnoreCase(st)) publishedCount++;
            }
        }

        if (tvMetricTotalAssignments != null) tvMetricTotalAssignments.setText(String.valueOf(totalAsn));
        if (tvMetricTotalQuizzes != null) tvMetricTotalQuizzes.setText(String.valueOf(totalQz));
        if (tvMetricTotalExams != null) tvMetricTotalExams.setText(String.valueOf(totalEx));
        if (tvMetricPublished != null) tvMetricPublished.setText(String.valueOf(publishedCount));
        if (tvMetricDrafts != null) tvMetricDrafts.setText(String.valueOf(draftCount));
        if (tvMetricPendingGrading != null) tvMetricPendingGrading.setText(String.valueOf(pendingSubs));

        if (layoutCourseProgressRows != null && getContext() != null) {
            // Keep first 2 children (header + divider) and rebuild course rows dynamically from real data
            while (layoutCourseProgressRows.getChildCount() > 2) {
                layoutCourseProgressRows.removeViewAt(2);
            }
            java.util.Map<String, int[]> perCourse = new java.util.LinkedHashMap<>();
            if (assignmentsArr != null) {
                for (JsonElement el : assignmentsArr) {
                    if (!el.isJsonObject()) continue;
                    JsonObject o = el.getAsJsonObject();
                    String key = getStr(o, "course_code", "COURSE") + " — " + getStr(o, "course_name", "Course");
                    int[] counts = perCourse.computeIfAbsent(key, k -> new int[2]);
                    counts[0] += getInt(o, "graded_submissions");
                    counts[1] += getInt(o, "total_submissions");
                }
            }
            if (perCourse.isEmpty()) {
                TextView emptyTv = new TextView(requireContext());
                emptyTv.setText("No assigned course submissions yet.");
                emptyTv.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
                emptyTv.setTextSize(12f);
                layoutCourseProgressRows.addView(emptyTv);
            } else {
                for (java.util.Map.Entry<String, int[]> e : perCourse.entrySet()) {
                    int graded = e.getValue()[0];
                    int total = e.getValue()[1];
                    int pct = total > 0 ? Math.round((graded * 100f) / total) : 0;

                    LinearLayout row = new LinearLayout(requireContext());
                    row.setOrientation(LinearLayout.VERTICAL);
                    row.setPadding(0, 6, 0, 10);

                    LinearLayout top = new LinearLayout(requireContext());
                    top.setOrientation(LinearLayout.HORIZONTAL);
                    TextView nameTv = new TextView(requireContext());
                    nameTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    nameTv.setText(e.getKey());
                    nameTv.setTextColor(getResources().getColor(R.color.admin_text_primary, null));
                    nameTv.setTextSize(12f);
                    nameTv.setTypeface(null, android.graphics.Typeface.BOLD);

                    TextView pctTv = new TextView(requireContext());
                    pctTv.setText(total > 0 ? (pct + "% graded (" + graded + "/" + total + ")") : "No submissions");
                    pctTv.setTextColor(getResources().getColor(R.color.admin_primary_blue, null));
                    pctTv.setTextSize(11.5f);
                    pctTv.setTypeface(null, android.graphics.Typeface.BOLD);

                    top.addView(nameTv);
                    top.addView(pctTv);
                    row.addView(top);

                    android.widget.ProgressBar pb = new android.widget.ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
                    LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 18);
                    pbParams.topMargin = 6;
                    pb.setLayoutParams(pbParams);
                    pb.setMax(100);
                    pb.setProgress(pct);
                    row.addView(pb);

                    layoutCourseProgressRows.addView(row);
                }
            }
        }
    }

    private void bindAssignmentsViews(View root) {
        rvAssignmentsList = root.findViewById(R.id.rvAssignmentsList);
        edtSearchAssignments = root.findViewById(R.id.edtSearchAssignments);

        if (rvAssignmentsList != null && getContext() != null) {
            assignmentAdapter = new AssignmentAdapter(requireContext(), assignmentList, new AssignmentAdapter.OnAssignmentActionClickListener() {
                @Override
                public void onGradeClick(AssignmentAdapter.AssignmentCardItem item) {
                    Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
                    intent.putExtra("assessment_id", item.id);
                    intent.putExtra("assessment_title", item.title);
                    intent.putExtra("course_code", item.courseCode);
                    startActivity(intent);
                }

                @Override
                public void onDeleteClick(AssignmentAdapter.AssignmentCardItem item) {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.confirm_delete_assignment)
                            .setMessage("Are you sure you want to delete '" + item.title + "'?")
                            .setPositiveButton(R.string.delete, (dialog, which) -> deleteAssignmentOnApi(item))
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }
            });
            rvAssignmentsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvAssignmentsList.setAdapter(assignmentAdapter);
        }

        if (edtSearchAssignments != null) {
            edtSearchAssignments.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (assignmentAdapter != null) {
                        assignmentAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void bindAssignmentsData(JsonArray arr) {
        assignmentList.clear();
        if (arr != null) {
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String id = String.valueOf(getInt(obj, "id"));
                String code = getStr(obj, "course_code", "COURSE");
                String title = getStr(obj, "title", "Assignment");
                String courseName = getStr(obj, "course_name", "Course");
                String due = getStr(obj, "due_date", "-");
                int marks = (int) getDouble(obj, "total_score");
                int subs = getInt(obj, "total_submissions");
                int graded = getInt(obj, "graded_submissions");
                String status = getStr(obj, "status", "Published");
                String cls = getStr(obj, "class_name", "");
                String asnType = getStr(obj, "assignment_type", "assignment");
                String typeTag = "homework".equalsIgnoreCase(asnType) ? "Homework • " : "";
                String subtitle = cls.isEmpty() ? (typeTag + courseName) : (typeTag + courseName + " • " + cls);
                String details = "Due Date: " + due + " • Marks: " + marks + " pts • Submissions: " + subs + " (" + graded + " graded)";
                assignmentList.add(new AssignmentAdapter.AssignmentCardItem(id, code, title, subtitle, details, status));
            }
        }
        if (assignmentAdapter != null) {
            assignmentAdapter.filter("");
        }
    }

    private void deleteAssignmentOnApi(AssignmentAdapter.AssignmentCardItem item) {
        if (getContext() == null) return;
        try {
            int asnId = Integer.parseInt(item.id.replaceAll("[^0-9]", ""));
            ApiClient.getApiService(requireContext()).deleteTeacherAssignment(asnId).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded() || getContext() == null) return;
                    if (response.isSuccessful()) {
                        assignmentList.remove(item);
                        if (assignmentAdapter != null) assignmentAdapter.filter("");
                        Toast.makeText(requireContext(), "Assignment deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete assignment (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(requireContext(), "Network error deleting assignment", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException ignored) {
        }
    }

    private void bindQuizzesViews(View root) {
        rvQuizzesList = root.findViewById(R.id.rvQuizzesList);
        edtSearchQuizzes = root.findViewById(R.id.edtSearchQuizzes);

        if (rvQuizzesList != null && getContext() != null) {
            quizAdapter = new QuizAdapter(requireContext(), quizList, new QuizAdapter.OnQuizActionClickListener() {
                @Override
                public void onBuilderClick(QuizAdapter.QuizCardItem item) {
                    openQuizBuilder(item);
                }

                @Override
                public void onPublishClick(QuizAdapter.QuizCardItem item) {
                    publishQuizOnApi(item);
                }
            });
            rvQuizzesList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvQuizzesList.setAdapter(quizAdapter);
        }

        if (edtSearchQuizzes != null) {
            edtSearchQuizzes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (quizAdapter != null) {
                        quizAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void bindQuizzesData(JsonArray arr) {
        quizList.clear();
        if (arr != null) {
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String id = String.valueOf(getInt(obj, "id"));
                String code = getStr(obj, "course_code", "COURSE");
                String title = getStr(obj, "title", "Quiz");
                String cName = getStr(obj, "course_name", "Course");
                int qCount = getInt(obj, "questions_count");
                int attCount = getInt(obj, "attempts_count");
                int dur = getInt(obj, "duration_minutes");
                double totalMarks = getDouble(obj, "total_marks");
                if (totalMarks <= 0) totalMarks = getDouble(obj, "total_score");
                String cls = getStr(obj, "class_name", "");
                String status = getStr(obj, "status", "Published");
                String cohortLine = cName + (cls.isEmpty() ? "" : " • " + cls) + " • " + qCount + " Questions";
                quizList.add(new QuizAdapter.QuizCardItem(
                        id,
                        code,
                        title,
                        cohortLine,
                        "Duration: " + dur + " Mins • Marks: " + com.social7.studentlearnanalytics.utils.AssessmentUiHelper.formatMarks(totalMarks) + " pts • Attempts: " + attCount,
                        status
                ));
            }
        }
        if (quizAdapter != null) {
            quizAdapter.filter("");
        }
    }

    private void publishQuizOnApi(QuizAdapter.QuizCardItem item) {
        if (getContext() == null) return;
        try {
            int qzId = Integer.parseInt(item.id.replaceAll("[^0-9]", ""));
            ApiClient.getApiService(requireContext()).publishTeacherQuiz(qzId).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded() || getContext() == null) return;
                    if (response.isSuccessful()) {
                        item.status = "Published";
                        if (quizAdapter != null) quizAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), R.string.quiz_published_to_students, Toast.LENGTH_SHORT).show();
                        fetchAssessmentsOverview();
                    } else {
                        String msg = com.social7.studentlearnanalytics.utils.AssessmentUiHelper.extractErrorMessage(response, "Failed to publish quiz");
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Cannot Publish Quiz")
                                .setMessage(msg)
                                .setPositiveButton("Open Quiz Builder", (d, w) -> openQuizBuilder(item))
                                .setNegativeButton("Close", null)
                                .show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(requireContext(), "Network error publishing quiz", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException ignored) {
        }
    }

    private void openQuizBuilder(QuizAdapter.QuizCardItem quiz) {
        if (getContext() == null) return;
        try {
            int qzId = Integer.parseInt(quiz.id.replaceAll("[^0-9]", ""));
            Intent intent = new Intent(requireContext(), QuizBuilderActivity.class);
            intent.putExtra("quiz_id", qzId);
            startActivity(intent);
        } catch (NumberFormatException ignored) {}
    }

    private void bindExamsViews(View root) {
        rvExamsList = root.findViewById(R.id.rvExamsList);
        edtSearchExams = root.findViewById(R.id.edtSearchExams);

        if (rvExamsList != null && getContext() != null) {
            examAdapter = new TeacherExamAdapter(requireContext(), examList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Exam?")
                        .setMessage("Are you sure you want to delete '" + item.title + "'?")
                        .setPositiveButton(R.string.delete, (d, w) -> deleteExamOnApi(item))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            });
            rvExamsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvExamsList.setAdapter(examAdapter);
        }

        if (edtSearchExams != null) {
            edtSearchExams.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (examAdapter != null) examAdapter.filter(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void bindExamsData(JsonArray arr) {
        examList.clear();
        if (arr != null) {
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                int id = getInt(obj, "id");
                String code = getStr(obj, "course_code", "COURSE");
                String title = getStr(obj, "title", "Exam");
                String cName = getStr(obj, "course_name", "Course");
                String cls = getStr(obj, "class_name", "");
                String exType = getStr(obj, "exam_type", "Exam");
                String exDate = getStr(obj, "exam_date", "-");
                String venue = getStr(obj, "venue", "");
                int dur = getInt(obj, "duration_minutes");
                int marks = (int) getDouble(obj, "total_score");
                int resCount = getInt(obj, "results_count");
                String rawSt = getStr(obj, "raw_status", "scheduled");
                String status = rawSt.substring(0, 1).toUpperCase(java.util.Locale.US) + rawSt.substring(1);

                String classAndType = exType + " • " + cName + (cls.isEmpty() ? "" : " • " + cls);
                String details = "Date: " + exDate
                        + (dur > 0 ? " • " + dur + " mins" : "")
                        + (!venue.isEmpty() ? " • " + venue : "")
                        + " • Marks: " + marks + " pts • Results: " + resCount;
                examList.add(new TeacherExamAdapter.ExamCardItem(id, code, title, classAndType, details, status));
            }
        }
        if (examAdapter != null) examAdapter.filter("");
    }

    private void deleteExamOnApi(TeacherExamAdapter.ExamCardItem item) {
        if (getContext() == null) return;
        ApiClient.getApiService(requireContext()).deleteTeacherExam(item.id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful()) {
                    examList.remove(item);
                    if (examAdapter != null) examAdapter.filter("");
                    Toast.makeText(requireContext(), "Exam deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), com.social7.studentlearnanalytics.utils.AssessmentUiHelper.extractErrorMessage(response, "Cannot delete exam"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(requireContext(), "Network error deleting exam", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindSubmissionsViews(View root) {
        rvSubmissionsList = root.findViewById(R.id.rvSubmissionsList);
        edtSearchSubmissions = root.findViewById(R.id.edtSearchSubmissions);

        if (rvSubmissionsList != null && getContext() != null) {
            submissionAdapter = new SubmissionAdapter(requireContext(), submissionList, new SubmissionAdapter.OnSubmissionActionClickListener() {
                @Override
                public void onViewFileClick(SubmissionAdapter.SubmissionCardItem item) {
                    openSubmissionFileViewer(item);
                }

                @Override
                public void onDetailClick(SubmissionAdapter.SubmissionCardItem item) {
                    showSubmissionDetailDialog(item);
                }

                @Override
                public void onGradeClick(SubmissionAdapter.SubmissionCardItem item) {
                    openSubmissionGrading(item);
                }
            });
            rvSubmissionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvSubmissionsList.setAdapter(submissionAdapter);
        }

        if (edtSearchSubmissions != null) {
            edtSearchSubmissions.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (submissionAdapter != null) {
                        submissionAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void bindGradingViews(View root) {
        rvGradingList = root.findViewById(R.id.rvGradingList);
        edtSearchGrading = root.findViewById(R.id.edtSearchGrading);

        if (rvGradingList != null && getContext() != null) {
            gradingAdapter = new GradingAdapter(requireContext(), gradingList, this::openGradingScreenFromGradingCard);
            rvGradingList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvGradingList.setAdapter(gradingAdapter);
        }

        if (edtSearchGrading != null) {
            edtSearchGrading.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (gradingAdapter != null) {
                        gradingAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void bindSubmissionsAndGradingData(JsonArray arr) {
        submissionList.clear();
        gradingList.clear();
        if (arr != null) {
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String subId = String.valueOf(getInt(obj, "id"));
                String stCode = getStr(obj, "student_code", "STU");
                String stName = getStr(obj, "student_name", "Student");
                String asnTitle = getStr(obj, "assignment_title", "Assignment");
                String asnType = getStr(obj, "assessment_type", "Assignment");
                String courseCode = getStr(obj, "course_code", "");
                String courseName = getStr(obj, "course_name", "");
                String subAt = getStr(obj, "submitted_at", "-");
                String status = getStr(obj, "status", "Submitted");
                String fb = getStr(obj, "feedback", "");

                boolean hasFile = obj.has("has_file") && !obj.get("has_file").isJsonNull() && obj.get("has_file").getAsBoolean();
                boolean fileExists = obj.has("file_exists") && !obj.get("file_exists").isJsonNull() && obj.get("file_exists").getAsBoolean();
                String fileName = getStr(obj, "file_name", "");
                String fileType = getStr(obj, "file_type", "");
                String fileSizeLabel = getStr(obj, "file_size_label", "");
                String mimeType = getStr(obj, "mime_type", "");
                String previewType = getStr(obj, "preview_type", "");

                if (fileName.isEmpty()) {
                    fileName = hasFile ? "Submission attachment" : "Submitted online (no file)";
                }

                double maxMarksDouble = getDouble(obj, "max_score");
                if (maxMarksDouble <= 0) maxMarksDouble = getDouble(obj, "total_score");
                if (maxMarksDouble <= 0) maxMarksDouble = 100.0;
                int maxMarks = (int) Math.round(maxMarksDouble);

                Double scoreVal = (obj.has("score") && !obj.get("score").isJsonNull())
                        ? obj.get("score").getAsDouble() : null;
                int score = scoreVal != null ? (int) Math.round(scoreVal) : 0;

                String attachmentSummary;
                if (!hasFile) {
                    attachmentSummary = "Online submission (no attachment)";
                } else if (!fileExists) {
                    attachmentSummary = "File: " + fileName + " (Missing on server)";
                } else {
                    String meta = fileType.isEmpty() ? "" : fileType;
                    if (!fileSizeLabel.isEmpty()) {
                        meta = meta.isEmpty() ? fileSizeLabel : (meta + " • " + fileSizeLabel);
                    }
                    attachmentSummary = meta.isEmpty()
                            ? ("File: " + fileName)
                            : ("File: " + fileName + " (" + meta + ")");
                }

                SubmissionAdapter.SubmissionCardItem card = new SubmissionAdapter.SubmissionCardItem(
                        subId,
                        stCode,
                        stName,
                        "Assessment: " + asnTitle,
                        "Submitted: " + subAt + "\n" + attachmentSummary,
                        status
                );
                card.rawAssessmentTitle = asnTitle;
                card.assessmentType = asnType;
                card.courseCode = courseCode;
                card.courseName = courseName;
                card.submittedAt = subAt;
                card.maxScore = maxMarksDouble;
                card.score = scoreVal;
                card.feedback = fb;
                card.fileName = fileName;
                card.fileType = fileType;
                card.fileSizeLabel = fileSizeLabel;
                card.mimeType = mimeType;
                card.previewType = previewType;
                card.hasFile = hasFile;
                card.fileExists = fileExists;
                submissionList.add(card);

                gradingList.add(new GradingAdapter.GradingCardItem(
                        subId,
                        stCode,
                        stName,
                        "Assessment: " + asnTitle,
                        "Status: " + status + (hasFile ? (" • File: " + fileName) : "") + (fb.isEmpty() ? "" : " • Feedback: " + fb),
                        scoreVal != null ? (score + " / " + maxMarks + " pts") : ("Pending / " + maxMarks + " pts"),
                        maxMarks,
                        score
                ));
            }
        }
        if (submissionAdapter != null) submissionAdapter.filter("");
        if (gradingAdapter != null) gradingAdapter.filter("");
    }

    private void showSubmissionDetailDialog(SubmissionAdapter.SubmissionCardItem item) {
        if (!isAdded() || getContext() == null) return;
        StringBuilder msg = new StringBuilder();
        msg.append("Student: ").append(item.studentName).append(" (").append(item.studentId).append(")\n");
        msg.append("Assessment: ").append(item.rawAssessmentTitle).append("\n");
        if (!item.courseCode.isEmpty()) {
            msg.append("Course: ").append(item.courseCode);
            if (!item.courseName.isEmpty()) msg.append(" - ").append(item.courseName);
            msg.append("\n");
        }
        msg.append("Submitted: ").append(item.submittedAt.isEmpty() ? "-" : item.submittedAt).append("\n");
        msg.append("File Name: ").append(item.fileName.isEmpty() ? "No attachment" : item.fileName).append("\n");
        if (!item.fileType.isEmpty()) {
            msg.append("File Type: ").append(item.fileType);
            if (!item.fileSizeLabel.isEmpty()) msg.append(" (").append(item.fileSizeLabel).append(")");
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.student_submission_file_detail)
                .setMessage(msg.toString())
                .setPositiveButton(R.string.view_file, (dialog, which) -> openSubmissionFileViewer(item))
                .setNeutralButton(R.string.grade_submission, (dialog, which) -> openSubmissionGrading(item))
                .setNegativeButton(R.string.close, null);
        builder.show();
    }

    private void openSubmissionFileViewer(SubmissionAdapter.SubmissionCardItem item) {
        if (!isAdded() || getContext() == null) return;
        Intent intent = new Intent(requireContext(), SubmissionFileViewerActivity.class);
        intent.putExtra("submission_id", item.id);
        intent.putExtra("student_code", item.studentId);
        intent.putExtra("student_name", item.studentName);
        intent.putExtra("assessment_title", item.rawAssessmentTitle);
        intent.putExtra("course_code", item.courseCode);
        intent.putExtra("course_name", item.courseName);
        intent.putExtra("submitted_at", item.submittedAt);
        intent.putExtra("file_name", item.fileName);
        intent.putExtra("file_type", item.fileType);
        intent.putExtra("file_size_label", item.fileSizeLabel);
        intent.putExtra("mime_type", item.mimeType);
        intent.putExtra("preview_type", item.previewType);
        intent.putExtra("has_file", item.hasFile);
        intent.putExtra("file_exists", item.fileExists);
        startActivity(intent);
    }

    private void openSubmissionGrading(SubmissionAdapter.SubmissionCardItem item) {
        if (!isAdded() || getContext() == null) return;
        Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
        intent.putExtra("submission_id", item.id);
        intent.putExtra("assessment_title", item.rawAssessmentTitle);
        intent.putExtra("assessment_type", item.assessmentType);
        intent.putExtra("course_code", item.courseCode);
        intent.putExtra("course_name", item.courseName);
        intent.putExtra("student_name", item.studentName);
        intent.putExtra("student_code", item.studentId);
        intent.putExtra("submitted_at", item.submittedAt);
        intent.putExtra("status", item.status);
        intent.putExtra("max_score", item.maxScore);
        if (item.score != null) {
            intent.putExtra("existing_score", item.score);
        }
        intent.putExtra("existing_feedback", item.feedback);
        intent.putExtra("file_name", item.fileName);
        intent.putExtra("file_type", item.fileType);
        intent.putExtra("file_size_label", item.fileSizeLabel);
        intent.putExtra("mime_type", item.mimeType);
        intent.putExtra("preview_type", item.previewType);
        intent.putExtra("has_file", item.hasFile);
        intent.putExtra("file_exists", item.fileExists);
        startActivity(intent);
    }

    private void openGradingScreenFromGradingCard(GradingAdapter.GradingCardItem item) {
        if (!isAdded() || getContext() == null) return;
        for (SubmissionAdapter.SubmissionCardItem sub : submissionList) {
            if (sub.id != null && sub.id.equals(item.id)) {
                openSubmissionGrading(sub);
                return;
            }
        }
        Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
        intent.putExtra("submission_id", item.id);
        intent.putExtra("assessment_title", item.assessmentTitle);
        intent.putExtra("student_name", item.studentName);
        intent.putExtra("student_code", item.studentId);
        intent.putExtra("max_score", (double) item.maxMarks);
        startActivity(intent);
    }

    private void showGradingModalDialog(GradingAdapter.GradingCardItem item) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        TextInputLayout tilScore = new TextInputLayout(requireContext());
        tilScore.setHint("Awarded Score (Max: " + item.maxMarks + " pts)");
        TextInputEditText edtScore = new TextInputEditText(tilScore.getContext());
        edtScore.setText(String.valueOf(item.currentScore));
        edtScore.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tilScore.addView(edtScore);
        container.addView(tilScore);

        TextInputLayout tilFeedback = new TextInputLayout(requireContext());
        tilFeedback.setHint(R.string.teacher_feedback_comments);
        TextInputEditText edtFeedback = new TextInputEditText(tilFeedback.getContext());
        tilFeedback.addView(edtFeedback);
        container.addView(tilFeedback);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Grade Student Submission — " + item.studentName)
                .setView(container)
                .setPositiveButton(R.string.publish_grade, (dialog, which) -> {
                    String scoreStr = edtScore.getText() != null ? edtScore.getText().toString().trim() : "0";
                    double score;
                    try {
                        score = Double.parseDouble(scoreStr.isEmpty() ? "0" : scoreStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Invalid score value", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (score < 0 || score > item.maxMarks) {
                        Toast.makeText(requireContext(), "Awarded score must be between 0 and " + item.maxMarks + " pts!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String feedback = edtFeedback.getText() != null ? edtFeedback.getText().toString().trim() : "";
                    int subId;
                    try {
                        subId = Integer.parseInt(item.id.replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException e) {
                        return;
                    }
                    JsonObject body = new JsonObject();
                    body.addProperty("score", score);
                    if (!feedback.isEmpty()) body.addProperty("feedback", feedback);

                    ApiClient.getApiService(requireContext()).gradeTeacherSubmission(subId, body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (!isAdded() || getContext() == null) return;
                            if (response.isSuccessful()) {
                                item.currentScore = (int) Math.round(score);
                                item.scoreBadge = item.currentScore + " / " + item.maxMarks + " pts";
                                if (gradingAdapter != null) gradingAdapter.notifyDataSetChanged();
                                Toast.makeText(requireContext(), "Grade published successfully for " + item.studentName + "!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to save grade (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            if (!isAdded() || getContext() == null) return;
                            Toast.makeText(requireContext(), "Network error saving grade", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String getStr(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            String v = obj.get(key).getAsString();
            return v != null && !v.isEmpty() ? v : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private int getInt(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return 0;
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }

    private double getDouble(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return 0.0;
        try {
            return obj.get(key).getAsDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }
}
