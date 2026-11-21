package com.social7.studentlearnanalytics.student;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.AssessmentAdapter;
import com.social7.studentlearnanalytics.adapter.ExamAdapter;
import com.social7.studentlearnanalytics.model.Assessment;
import com.social7.studentlearnanalytics.model.Exam;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.student.assignment.StudentSubmissionAdapter;
import com.social7.studentlearnanalytics.student.assignment.StudentSubmissionDetailActivity;
import com.social7.studentlearnanalytics.teacher.assessment.SubmissionFileViewerActivity;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAssessmentFragment extends Fragment {

    private TextView tabStudentAssignments, tabStudentHomework, tabStudentQuizzes, tabStudentExams, tabStudentSubmissions;
    private ProgressBar pbLoading;
    private LinearLayout layoutState;
    private ImageView ivStateIcon;
    private TextView tvStateTitle, tvEmptyStudentAssessments;
    private MaterialButton btnRetry;
    private RecyclerView rvStudentAssessmentList;
    private EditText edtSearchStudentAssessments;
    private String selectedCategory = "Assignment";

    private final List<Assessment> allAssignments = new ArrayList<>();
    private final List<Assessment> allHomework = new ArrayList<>();
    private final List<Assessment> allQuizzes = new ArrayList<>();
    private final List<Exam> allExams = new ArrayList<>();
    private final List<StudentSubmissionAdapter.StudentSubmissionItem> allSubmissions = new ArrayList<>();

    private final List<Assessment> filteredAssessments = new ArrayList<>();
    private final List<Exam> filteredExams = new ArrayList<>();
    private final List<StudentSubmissionAdapter.StudentSubmissionItem> filteredSubmissions = new ArrayList<>();

    private String assignmentError = null;
    private String quizError = null;
    private String examError = null;
    private String submissionsError = null;

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
        tabStudentSubmissions = view.findViewById(R.id.tabStudentSubmissions);

        pbLoading = view.findViewById(R.id.pbStudentAssessmentsLoading);
        layoutState = view.findViewById(R.id.layoutStudentAssessmentsState);
        ivStateIcon = view.findViewById(R.id.ivStateIcon);
        tvStateTitle = view.findViewById(R.id.tvStateTitle);
        tvEmptyStudentAssessments = view.findViewById(R.id.tvEmptyStudentAssessments);
        btnRetry = view.findViewById(R.id.btnRetryStudentAssessments);

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> loadAssessmentsForCategory(selectedCategory));
        }

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
        if (tabStudentSubmissions != null) tabStudentSubmissions.setOnClickListener(v -> selectCategory("Submissions"));
    }

    private void selectCategory(String category) {
        this.selectedCategory = category;
        updateSubTabButtonStyles(category);
        loadAssessmentsForCategory(category);
    }

    private void updateSubTabButtonStyles(String activeCategory) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabStudentAssignments, tabStudentHomework, tabStudentQuizzes, tabStudentExams, tabStudentSubmissions};
        String[] categories = new String[]{"Assignment", "Homework", "Quiz", "Exam", "Submissions"};

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

    private void showLoading(boolean loading) {
        if (pbLoading != null) pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            if (layoutState != null) layoutState.setVisibility(View.GONE);
            if (rvStudentAssessmentList != null) rvStudentAssessmentList.setVisibility(View.GONE);
        }
    }

    private void loadAssessmentsForCategory(String category) {
        if (!isAdded() || getContext() == null) return;
        showLoading(true);

        if ("Submissions".equalsIgnoreCase(category)) {
            submissionsError = null;
            ApiClient.getApiService(getContext()).getStudentSubmissions().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded()) return;
                    showLoading(false);
                    allSubmissions.clear();

                    if (!response.isSuccessful() || response.body() == null) {
                        submissionsError = AssessmentUiHelper.extractErrorMessage(response, "Unable to load your submissions");
                        applyFilterAndBind();
                        return;
                    }

                    JsonArray arr = AssessmentUiHelper.extractDataArray(response.body(), "submissions");
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        StudentSubmissionAdapter.StudentSubmissionItem item = new StudentSubmissionAdapter.StudentSubmissionItem();
                        item.id = AssessmentUiHelper.optInt(obj, "id", 0);
                        item.assignmentId = AssessmentUiHelper.optInt(obj, "assignment_id", 0);
                        item.studentCode = AssessmentUiHelper.optString(obj, "student_code", "");
                        item.studentName = AssessmentUiHelper.optString(obj, "student_name", "");
                        item.assignmentTitle = AssessmentUiHelper.optString(obj, "assignment_title", "Assignment");
                        item.assessmentType = AssessmentUiHelper.optString(obj, "assessment_type", "Assignment");
                        item.courseCode = AssessmentUiHelper.optString(obj, "course_code", "");
                        item.courseName = AssessmentUiHelper.optString(obj, "course_name", "");
                        item.className = AssessmentUiHelper.optString(obj, "class_name", "");
                        item.submittedAt = AssessmentUiHelper.optString(obj, "submitted_at", "-");
                        item.submissionStatus = AssessmentUiHelper.optString(obj, "submission_status", "Submitted");
                        item.gradingStatus = AssessmentUiHelper.optString(obj, "grading_status", "Waiting for Teacher Grading");
                        item.isPublished = obj.has("is_published") && !obj.get("is_published").isJsonNull() && obj.get("is_published").getAsBoolean();
                        item.score = (item.isPublished && obj.has("score") && !obj.get("score").isJsonNull())
                                ? obj.get("score").getAsDouble() : null;
                        item.maxScore = AssessmentUiHelper.optDouble(obj, "max_score", 100.0);
                        item.feedback = item.isPublished ? AssessmentUiHelper.optString(obj, "feedback", "") : "";
                        item.publishedAt = AssessmentUiHelper.optString(obj, "published_at", "");
                        item.fileName = AssessmentUiHelper.optString(obj, "file_name", "");
                        item.fileType = AssessmentUiHelper.optString(obj, "file_type", "");
                        item.fileSizeLabel = AssessmentUiHelper.optString(obj, "file_size_label", "");
                        item.mimeType = AssessmentUiHelper.optString(obj, "mime_type", "");
                        item.previewType = AssessmentUiHelper.optString(obj, "preview_type", "");
                        item.hasFile = obj.has("has_file") && !obj.get("has_file").isJsonNull() && obj.get("has_file").getAsBoolean();
                        item.fileExists = obj.has("file_exists") && !obj.get("file_exists").isJsonNull() && obj.get("file_exists").getAsBoolean();
                        item.versionLabel = AssessmentUiHelper.optString(obj, "version_label", "Latest Submission");
                        allSubmissions.add(item);
                    }
                    applyFilterAndBind();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    showLoading(false);
                    allSubmissions.clear();
                    submissionsError = "Network error. Please check your connection and try again.";
                    applyFilterAndBind();
                }
            });
            return;
        }

        if ("Quiz".equalsIgnoreCase(category)) {
            quizError = null;
            ApiClient.getApiService(getContext()).getStudentQuizzes(null).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded()) return;
                    showLoading(false);
                    allQuizzes.clear();

                    if (!response.isSuccessful() || response.body() == null) {
                        quizError = AssessmentUiHelper.extractErrorMessage(response, "Unable to load quizzes");
                        applyFilterAndBind();
                        return;
                    }

                    JsonArray arr = AssessmentUiHelper.extractDataArray(response.body(), "quizzes");
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        String id = AssessmentUiHelper.optString(obj, "id", "");
                        String title = AssessmentUiHelper.optString(obj, "title", "Quiz");
                        String startAt = AssessmentUiHelper.optString(obj, "start_at", "");
                        String endAt = AssessmentUiHelper.optString(obj, "end_at", "");
                        String rawDate = !endAt.isEmpty() ? endAt : (!startAt.isEmpty() ? startAt : AssessmentUiHelper.optString(obj, "created_at", ""));
                        String dateStr = (!endAt.isEmpty() ? "Closes: " : "") + AssessmentUiHelper.formatDateShort(rawDate);

                        int totalScore = (int) Math.round(AssessmentUiHelper.optDouble(obj, "total_score", 100));
                        int questionsCount = AssessmentUiHelper.optInt(obj, "questions_count", 0);
                        int durationMins = AssessmentUiHelper.optInt(obj, "duration_minutes", 0);
                        String rawStatus = AssessmentUiHelper.optString(obj, "status", "published");

                        int score = 0;
                        boolean isSubmitted = false;
                        boolean isGraded = false;
                        boolean inProgress = false;
                        String attemptStatus = "";

                        if (obj.has("attempts") && obj.get("attempts").isJsonArray()) {
                            JsonArray atts = obj.getAsJsonArray("attempts");
                            if (atts.size() > 0 && atts.get(0).isJsonObject()) {
                                JsonObject att = atts.get(0).getAsJsonObject();
                                attemptStatus = AssessmentUiHelper.optString(att, "status", "");
                                if ("in_progress".equalsIgnoreCase(attemptStatus)) {
                                    inProgress = true;
                                } else {
                                    isSubmitted = true;
                                    if ("completed".equalsIgnoreCase(attemptStatus) && att.has("score") && !att.get("score").isJsonNull()) {
                                        score = Math.round(att.get("score").getAsFloat());
                                        isGraded = true;
                                    }
                                }
                            }
                        }

                        boolean isClosed = "closed".equalsIgnoreCase(rawStatus) || (!endAt.isEmpty() && AssessmentUiHelper.isPast(endAt));
                        boolean isUpcoming = !startAt.isEmpty() && AssessmentUiHelper.isFuture(startAt);

                        String statusLabel;
                        String actionLabel;
                        if (isGraded) {
                            statusLabel = "Completed";
                            actionLabel = isClosed ? "Review Answers" : "View Result";
                        } else if (isSubmitted) {
                            statusLabel = "Submitted";
                            actionLabel = "Pending Grading";
                        } else if (inProgress && !isClosed) {
                            statusLabel = "In Progress";
                            actionLabel = "Continue Quiz";
                        } else if (isClosed) {
                            statusLabel = "Closed";
                            actionLabel = "View Details";
                        } else if (isUpcoming) {
                            statusLabel = "Upcoming";
                            actionLabel = "View Details";
                        } else {
                            statusLabel = "Available";
                            actionLabel = "Start Quiz";
                        }

                        String[] courseInfo = extractCourseCodeAndName(obj);
                        String[] ctxInfo = extractAcademicContext(obj);

                        Assessment item = new Assessment(id, title, dateStr, score, totalScore, "quiz",
                                ctxInfo[1], courseInfo[1], isSubmitted, isGraded);
                        item.setCourseCode(courseInfo[0]);
                        item.setClassName(ctxInfo[0]);
                        item.setAcademicYearName(ctxInfo[2]);
                        item.setRawStatus(rawStatus);
                        item.setStatusLabel(statusLabel);
                        item.setActionLabel(actionLabel);
                        item.setQuestionsCount(questionsCount);
                        item.setDurationMinutes(durationMins);
                        item.setInProgress(inProgress);
                        allQuizzes.add(item);
                    }
                    applyFilterAndBind();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    showLoading(false);
                    allQuizzes.clear();
                    quizError = "Network error. Please check your connection and try again.";
                    applyFilterAndBind();
                }
            });
        } else if ("Exam".equalsIgnoreCase(category)) {
            examError = null;
            ApiClient.getApiService(getContext()).getStudentExams(null).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded()) return;
                    showLoading(false);
                    allExams.clear();

                    if (!response.isSuccessful() || response.body() == null) {
                        examError = AssessmentUiHelper.extractErrorMessage(response, "Unable to load exams");
                        applyFilterAndBind();
                        return;
                    }

                    JsonArray arr = AssessmentUiHelper.extractDataArray(response.body(), "exams");
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        String id = AssessmentUiHelper.optString(obj, "id", "");
                        String title = AssessmentUiHelper.optString(obj, "title", "Examination");
                        String[] courseInfo = extractCourseCodeAndName(obj);
                        String[] ctxInfo = extractAcademicContext(obj);
                        String courseDisplay = !courseInfo[0].isEmpty() ? (courseInfo[0] + " • " + courseInfo[1]) : courseInfo[1];
                        String displayTitle = (courseDisplay != null && !courseDisplay.isEmpty()) ? (courseDisplay + " – " + title) : title;

                        String examDate = AssessmentUiHelper.optString(obj, "exam_date", "");
                        String[] md = parseMonthDay(examDate);
                        int totalMarks = (int) Math.round(AssessmentUiHelper.optDouble(obj, "total_marks",
                                AssessmentUiHelper.optDouble(obj, "total_score", 100)));
                        int durationMins = AssessmentUiHelper.optInt(obj, "duration_minutes", 0);
                        String venue = AssessmentUiHelper.optString(obj, "venue", "");
                        String rawStatus = AssessmentUiHelper.optString(obj, "status", "scheduled");

                        boolean hasResult = false;
                        String scoreText = null;
                        String grade = null;
                        String remarks = null;
                        if (obj.has("results") && obj.get("results").isJsonArray()) {
                            JsonArray results = obj.getAsJsonArray("results");
                            if (results.size() > 0 && results.get(0).isJsonObject()) {
                                JsonObject r = results.get(0).getAsJsonObject();
                                if (r.has("score") && !r.get("score").isJsonNull()) {
                                    hasResult = true;
                                    scoreText = AssessmentUiHelper.formatMarks(r.get("score").getAsDouble()) + " / " + totalMarks;
                                    grade = AssessmentUiHelper.optString(r, "grade", null);
                                    remarks = AssessmentUiHelper.optString(r, "remarks", null);
                                }
                            }
                        }

                        boolean completed = hasResult || "completed".equalsIgnoreCase(rawStatus);
                        String statusLabel = completed ? "Completed"
                                : ("ongoing".equalsIgnoreCase(rawStatus) ? "Ongoing" : "Scheduled");

                        String rawExamType = AssessmentUiHelper.optString(obj, "exam_type", "exam");
                        String examCategory = rawExamType.isEmpty() ? "Exam"
                                : rawExamType.substring(0, 1).toUpperCase(Locale.US) + rawExamType.substring(1).toLowerCase(Locale.US);

                        StringBuilder details = new StringBuilder();
                        details.append(examCategory).append(" • ").append(statusLabel);
                        if (durationMins > 0) details.append(" • ").append(durationMins).append(" mins");
                        if (!venue.isEmpty()) details.append(" • ").append(venue);

                        Exam exam = new Exam(id, md[0], md[1], displayTitle, details.toString(), completed, scoreText);
                        exam.setTitle(title);
                        exam.setCourseCode(courseInfo[0]);
                        exam.setClassName(ctxInfo[0]);
                        exam.setSemesterName(ctxInfo[1]);
                        exam.setExamType(examCategory);
                        exam.setStatusLabel(statusLabel);
                        exam.setRawStatus(rawStatus);
                        exam.setVenue(venue);
                        if (durationMins > 0) exam.setDurationMinutes(durationMins);
                        exam.setExamDateFormatted(AssessmentUiHelper.formatDateShort(examDate));
                        exam.setDescription(AssessmentUiHelper.optString(obj, "description", ""));
                        exam.setGrade(grade);
                        exam.setRemarks(remarks);
                        allExams.add(exam);
                    }
                    applyFilterAndBind();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    showLoading(false);
                    allExams.clear();
                    examError = "Network error. Please check your connection and try again.";
                    applyFilterAndBind();
                }
            });
        } else {
            assignmentError = null;
            ApiClient.getApiService(getContext()).getStudentAssignments(null).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded()) return;
                    showLoading(false);
                    allAssignments.clear();
                    allHomework.clear();

                    if (!response.isSuccessful() || response.body() == null) {
                        assignmentError = AssessmentUiHelper.extractErrorMessage(response, "Unable to load assessments");
                        applyFilterAndBind();
                        return;
                    }

                    JsonArray arr = AssessmentUiHelper.extractDataArray(response.body(), "assignments");
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        String id = AssessmentUiHelper.optString(obj, "id", "");
                        String title = AssessmentUiHelper.optString(obj, "title", "Assignment");
                        String rawDue = AssessmentUiHelper.optString(obj, "due_date", AssessmentUiHelper.optString(obj, "created_at", ""));
                        String dateStr = "Due: " + AssessmentUiHelper.formatDateShort(rawDue);
                        int totalScore = (int) Math.round(AssessmentUiHelper.optDouble(obj, "total_score", 100));
                        String rawStatus = AssessmentUiHelper.optString(obj, "status", "published");

                        int score = 0;
                        boolean isSubmitted = false;
                        boolean isGraded = false;
                        String subStatus = "";

                        if (obj.has("submissions") && obj.get("submissions").isJsonArray()) {
                            JsonArray subs = obj.getAsJsonArray("submissions");
                            if (subs.size() > 0 && subs.get(0).isJsonObject()) {
                                JsonObject sub = subs.get(0).getAsJsonObject();
                                subStatus = AssessmentUiHelper.optString(sub, "status", "submitted");
                                isSubmitted = true;
                                boolean isPub = sub.has("is_published") && !sub.get("is_published").isJsonNull()
                                        ? sub.get("is_published").getAsBoolean()
                                        : "graded".equalsIgnoreCase(subStatus);
                                if (isPub && sub.has("score") && !sub.get("score").isJsonNull()) {
                                    score = Math.round(sub.get("score").getAsFloat());
                                    isGraded = true;
                                }
                            }
                        }

                        boolean isClosed = "closed".equalsIgnoreCase(rawStatus);
                        boolean isOverdue = !isSubmitted && !isClosed && !rawDue.isEmpty() && AssessmentUiHelper.isPast(rawDue);

                        String statusLabel;
                        String actionLabel;
                        if (isGraded) {
                            statusLabel = "Graded";
                            actionLabel = "View Grade";
                        } else if (isSubmitted) {
                            statusLabel = "late".equalsIgnoreCase(subStatus) ? "Submitted Late" : "Submitted";
                            actionLabel = isClosed ? "View Submission" : "Edit / View";
                        } else if (isClosed) {
                            statusLabel = "Closed";
                            actionLabel = "View Details";
                        } else if (isOverdue) {
                            statusLabel = "Overdue";
                            actionLabel = "Submit Late";
                        } else {
                            statusLabel = "Available";
                            actionLabel = "Submit";
                        }

                        String type = AssessmentUiHelper.optString(obj, "assignment_type", "assignment");
                        boolean isHw = "homework".equalsIgnoreCase(type);
                        String[] courseInfo = extractCourseCodeAndName(obj);
                        String[] ctxInfo = extractAcademicContext(obj);

                        Assessment item = new Assessment(id, title, dateStr, score, totalScore,
                                isHw ? "homework" : "assignment", ctxInfo[1], courseInfo[1], isSubmitted, isGraded);
                        item.setCourseCode(courseInfo[0]);
                        item.setClassName(ctxInfo[0]);
                        item.setAcademicYearName(ctxInfo[2]);
                        item.setRawStatus(rawStatus);
                        item.setStatusLabel(statusLabel);
                        item.setActionLabel(actionLabel);
                        item.setOverdue(isOverdue);

                        if (isHw) {
                            allHomework.add(item);
                        } else {
                            allAssignments.add(item);
                        }
                    }
                    applyFilterAndBind();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    showLoading(false);
                    allAssignments.clear();
                    allHomework.clear();
                    assignmentError = "Network error. Please check your connection and try again.";
                    applyFilterAndBind();
                }
            });
        }
    }

    private void applyFilterAndBind() {
        if (!isAdded() || rvStudentAssessmentList == null) return;
        String query = (edtSearchStudentAssessments != null && edtSearchStudentAssessments.getText() != null)
                ? edtSearchStudentAssessments.getText().toString().trim().toLowerCase(Locale.US) : "";

        if ("Submissions".equalsIgnoreCase(selectedCategory)) {
            if (submissionsError != null) {
                showState(true, "Could not load submissions", submissionsError, true);
                return;
            }
            filteredSubmissions.clear();
            for (StudentSubmissionAdapter.StudentSubmissionItem sub : allSubmissions) {
                if (query.isEmpty()
                        || contains(sub.assignmentTitle, query)
                        || contains(sub.courseName, query)
                        || contains(sub.courseCode, query)
                        || contains(sub.fileName, query)) {
                    filteredSubmissions.add(sub);
                }
            }
            rvStudentAssessmentList.setAdapter(new StudentSubmissionAdapter(requireContext(), filteredSubmissions,
                    new StudentSubmissionAdapter.OnStudentSubmissionClickListener() {
                        @Override
                        public void onViewSubmittedFile(StudentSubmissionAdapter.StudentSubmissionItem item) {
                            openStudentFileViewer(item);
                        }

                        @Override
                        public void onOpenDetail(StudentSubmissionAdapter.StudentSubmissionItem item) {
                            openStudentSubmissionDetail(item);
                        }
                    }));
            if (filteredSubmissions.isEmpty()) {
                if (!query.isEmpty() && !allSubmissions.isEmpty()) {
                    showState(true, "No matching submissions", "Try clearing or changing your search keyword.", false);
                } else {
                    showState(true, "No submissions yet.", "Submitted assignments and their uploaded files will appear here.", false);
                }
            } else {
                showState(false, "", "", false);
            }
            return;
        }

        if ("Exam".equalsIgnoreCase(selectedCategory)) {
            if (examError != null) {
                showState(true, "Could not load exams", examError, true);
                return;
            }
            filteredExams.clear();
            for (Exam e : allExams) {
                if (query.isEmpty()
                        || contains(e.getCourseName(), query)
                        || contains(e.getCourseCode(), query)
                        || contains(e.getTitle(), query)
                        || contains(e.getClassName(), query)
                        || contains(e.getDetails(), query)) {
                    filteredExams.add(e);
                }
            }
            rvStudentAssessmentList.setAdapter(new ExamAdapter(requireContext(), filteredExams));
            if (filteredExams.isEmpty()) {
                if (!query.isEmpty() && !allExams.isEmpty()) {
                    showState(true, "No matching exams", "Try clearing or changing your search keyword.", false);
                } else {
                    showState(true, "No exams scheduled yet.", "Your teachers have not published any exams for your enrolled courses yet.", false);
                }
            } else {
                showState(false, "", "", false);
            }
        } else {
            String err = "Quiz".equalsIgnoreCase(selectedCategory) ? quizError : assignmentError;
            if (err != null) {
                showState(true, "Could not load " + selectedCategory.toLowerCase(Locale.US), err, true);
                return;
            }

            List<Assessment> source = "Quiz".equalsIgnoreCase(selectedCategory)
                    ? allQuizzes
                    : ("Homework".equalsIgnoreCase(selectedCategory) ? allHomework : allAssignments);
            filteredAssessments.clear();
            for (Assessment a : source) {
                if (query.isEmpty()
                        || contains(a.getTitle(), query)
                        || contains(a.getCourseName(), query)
                        || contains(a.getCourseCode(), query)
                        || contains(a.getClassName(), query)) {
                    filteredAssessments.add(a);
                }
            }
            rvStudentAssessmentList.setAdapter(new AssessmentAdapter(filteredAssessments));

            if (filteredAssessments.isEmpty()) {
                if (!query.isEmpty() && !source.isEmpty()) {
                    showState(true, "No matching results", "No items match \"" + query + "\" in " + selectedCategory + ".", false);
                } else if ("Homework".equalsIgnoreCase(selectedCategory)) {
                    showState(true, "No homework available yet.", "Your teacher has not published any homework for your courses yet.", false);
                } else if ("Quiz".equalsIgnoreCase(selectedCategory)) {
                    showState(true, "No quizzes available yet.", "Published quizzes from your enrolled courses will appear here.", false);
                } else {
                    showState(true, "No assignments available yet.", "Published assignments from your enrolled courses will appear here.", false);
                }
            } else {
                showState(false, "", "", false);
            }
        }
    }

    private boolean contains(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase(Locale.US).contains(needle);
    }

    private void showState(boolean visible, String title, String msg, boolean allowRetry) {
        if (layoutState == null || rvStudentAssessmentList == null) return;
        if (visible) {
            layoutState.setVisibility(View.VISIBLE);
            rvStudentAssessmentList.setVisibility(View.GONE);
            if (tvStateTitle != null) tvStateTitle.setText(title);
            if (tvEmptyStudentAssessments != null) tvEmptyStudentAssessments.setText(msg);
            if (btnRetry != null) btnRetry.setVisibility(allowRetry ? View.VISIBLE : View.GONE);
        } else {
            layoutState.setVisibility(View.GONE);
            rvStudentAssessmentList.setVisibility(View.VISIBLE);
        }
    }

    private String[] extractCourseCodeAndName(JsonObject obj) {
        String code = "";
        String name = "";
        if (obj.has("course") && obj.get("course").isJsonObject()) {
            JsonObject c = obj.getAsJsonObject("course");
            code = AssessmentUiHelper.optString(c, "code", AssessmentUiHelper.optString(c, "course_code", ""));
            name = AssessmentUiHelper.optString(c, "name", AssessmentUiHelper.optString(c, "course_name", ""));
        }
        return new String[]{code, name};
    }

    private String[] extractAcademicContext(JsonObject obj) {
        String cls = "";
        String sem = "";
        String year = "";
        if (obj.has("academic_context") && obj.get("academic_context").isJsonObject()) {
            JsonObject ctx = obj.getAsJsonObject("academic_context");
            cls = AssessmentUiHelper.optString(ctx, "class_name", "");
            sem = AssessmentUiHelper.optString(ctx, "semester_name", "");
            year = AssessmentUiHelper.optString(ctx, "academic_year_name", "");
        }
        if (sem.isEmpty() && obj.has("semester") && obj.get("semester").isJsonObject()) {
            sem = AssessmentUiHelper.optString(obj.getAsJsonObject("semester"), "name", "");
        }
        return new String[]{cls, sem, year};
    }

    private String[] parseMonthDay(String rawDate) {
        Date d = AssessmentUiHelper.parseIsoDate(rawDate);
        if (d != null) {
            String m = new SimpleDateFormat("MMM", Locale.US).format(d).toUpperCase(Locale.US);
            String day = new SimpleDateFormat("dd", Locale.US).format(d);
            return new String[]{m, day};
        }
        return new String[]{"TBA", "--"};
    }

    private void openStudentFileViewer(StudentSubmissionAdapter.StudentSubmissionItem item) {
        if (!isAdded() || getContext() == null) return;
        android.content.Intent intent = new android.content.Intent(requireContext(), SubmissionFileViewerActivity.class);
        intent.putExtra("submission_id_int", item.id);
        intent.putExtra("is_student_mode", true);
        intent.putExtra("student_code", item.studentCode);
        intent.putExtra("student_name", item.studentName);
        intent.putExtra("assessment_title", item.assignmentTitle);
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

    private void openStudentSubmissionDetail(StudentSubmissionAdapter.StudentSubmissionItem item) {
        if (!isAdded() || getContext() == null) return;
        android.content.Intent intent = new android.content.Intent(requireContext(), StudentSubmissionDetailActivity.class);
        intent.putExtra("submission_id", item.id);
        intent.putExtra("student_code", item.studentCode);
        intent.putExtra("student_name", item.studentName);
        intent.putExtra("assignment_title", item.assignmentTitle);
        intent.putExtra("assessment_type", item.assessmentType);
        intent.putExtra("course_code", item.courseCode);
        intent.putExtra("course_name", item.courseName);
        intent.putExtra("submitted_at", item.submittedAt);
        intent.putExtra("submission_status", item.submissionStatus);
        intent.putExtra("grading_status", item.gradingStatus);
        intent.putExtra("is_published", item.isPublished);
        if (item.score != null) intent.putExtra("score", item.score);
        intent.putExtra("max_score", item.maxScore);
        intent.putExtra("feedback", item.feedback);
        intent.putExtra("published_at", item.publishedAt);
        intent.putExtra("file_name", item.fileName);
        intent.putExtra("file_type", item.fileType);
        intent.putExtra("file_size_label", item.fileSizeLabel);
        intent.putExtra("mime_type", item.mimeType);
        intent.putExtra("preview_type", item.previewType);
        intent.putExtra("has_file", item.hasFile);
        intent.putExtra("file_exists", item.fileExists);
        startActivity(intent);
    }
}
