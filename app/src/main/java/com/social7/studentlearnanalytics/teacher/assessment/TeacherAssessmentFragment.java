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
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.teacher.TeacherMainActivity;

import java.util.ArrayList;
import java.util.List;

public class TeacherAssessmentFragment extends Fragment {

    // Sub-Tab Buttons
    private TextView tabAssessDashboard, tabAssessAssignments, tabAssessQuizzes, tabAssessSubmissions, tabAssessGrading;

    // Sub-Tab Containers
    private View secAssessDashboardContainer, secAssessAssignmentsContainer, secAssessQuizzesContainer, secAssessSubmissionsContainer, secAssessGradingContainer;

    // Sub-Tab 0: Dashboard
    private MaterialButton btnCreateAssessment;

    // Sub-Tab 1: Assignments
    private RecyclerView rvAssignmentsList;
    private AssignmentAdapter assignmentAdapter;
    private final List<AssignmentAdapter.AssignmentCardItem> assignmentList = new ArrayList<>();
    private EditText edtSearchAssignments;

    // Sub-Tab 2: Quizzes
    private RecyclerView rvQuizzesList;
    private QuizAdapter quizAdapter;
    private final List<QuizAdapter.QuizCardItem> quizList = new ArrayList<>();
    private EditText edtSearchQuizzes;

    // Sub-Tab 3: Submissions
    private RecyclerView rvSubmissionsList;
    private SubmissionAdapter submissionAdapter;
    private final List<SubmissionAdapter.SubmissionCardItem> submissionList = new ArrayList<>();
    private EditText edtSearchSubmissions;

    // Sub-Tab 4: Grading
    private RecyclerView rvGradingList;
    private GradingAdapter gradingAdapter;
    private final List<GradingAdapter.GradingCardItem> gradingList = new ArrayList<>();
    private EditText edtSearchGrading;

    // Selected Sub-Tab Flag (0=Dashboard, 1=Assignments, 2=Quizzes, 3=Submissions, 4=Grading)
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

        // Header Integration
        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view,
                    "Assessment Management",
                    "Manage assignments, quizzes, student submissions, and grading"
            );
        }

        // Sub-Tab Buttons
        tabAssessDashboard = view.findViewById(R.id.tabAssessDashboard);
        tabAssessAssignments = view.findViewById(R.id.tabAssessAssignments);
        tabAssessQuizzes = view.findViewById(R.id.tabAssessQuizzes);
        tabAssessSubmissions = view.findViewById(R.id.tabAssessSubmissions);
        tabAssessGrading = view.findViewById(R.id.tabAssessGrading);

        // Containers
        secAssessDashboardContainer = view.findViewById(R.id.secAssessDashboardContainer);
        secAssessAssignmentsContainer = view.findViewById(R.id.secAssessAssignmentsContainer);
        secAssessQuizzesContainer = view.findViewById(R.id.secAssessQuizzesContainer);
        secAssessSubmissionsContainer = view.findViewById(R.id.secAssessSubmissionsContainer);
        secAssessGradingContainer = view.findViewById(R.id.secAssessGradingContainer);

        // Bind Sub-views
        bindDashboardViews(view);
        bindAssignmentsViews(view);
        bindQuizzesViews(view);
        bindSubmissionsViews(view);
        bindGradingViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);

        return view;
    }

    private void setupSubTabListeners() {
        if (tabAssessDashboard != null) tabAssessDashboard.setOnClickListener(v -> selectSubTab(0));
        if (tabAssessAssignments != null) tabAssessAssignments.setOnClickListener(v -> selectSubTab(1));
        if (tabAssessQuizzes != null) tabAssessQuizzes.setOnClickListener(v -> selectSubTab(2));
        if (tabAssessSubmissions != null) tabAssessSubmissions.setOnClickListener(v -> selectSubTab(3));
        if (tabAssessGrading != null) tabAssessGrading.setOnClickListener(v -> selectSubTab(4));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secAssessDashboardContainer != null) secAssessDashboardContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secAssessAssignmentsContainer != null) secAssessAssignmentsContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secAssessQuizzesContainer != null) secAssessQuizzesContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secAssessSubmissionsContainer != null) secAssessSubmissionsContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);
        if (secAssessGradingContainer != null) secAssessGradingContainer.setVisibility(subIndex == 4 ? View.VISIBLE : View.GONE);

        if (subIndex == 1) {
            setupAssignmentsList();
        } else if (subIndex == 2) {
            setupQuizzesList();
        } else if (subIndex == 3) {
            setupSubmissionsList();
        } else if (subIndex == 4) {
            setupGradingList();
        }
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabAssessDashboard, tabAssessAssignments, tabAssessQuizzes, tabAssessSubmissions, tabAssessGrading};
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

    // ==========================================
    // 0. DASHBOARD SUB-TAB
    // ==========================================
    private void bindDashboardViews(View root) {
        btnCreateAssessment = root.findViewById(R.id.btnCreateAssessment);
        if (btnCreateAssessment != null) {
            btnCreateAssessment.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), AddAssessmentActivity.class);
                startActivity(intent);
            });
        }
    }

    // ==========================================
    // 1. ASSIGNMENTS SUB-TAB
    // ==========================================
    private void bindAssignmentsViews(View root) {
        rvAssignmentsList = root.findViewById(R.id.rvAssignmentsList);
        edtSearchAssignments = root.findViewById(R.id.edtSearchAssignments);

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

    private void setupAssignmentsList() {
        if (rvAssignmentsList == null || assignmentAdapter != null) return;

        assignmentList.clear();
        assignmentList.add(new AssignmentAdapter.AssignmentCardItem("A101", "CS101", "Assignment 1: Linked Lists & Pointers", "Class: BSIT 4-A • Computer Science Dept", "Due Date: Oct 20, 2024 • Marks: 100 pts • Submissions: 28/35", "Published"));
        assignmentList.add(new AssignmentAdapter.AssignmentCardItem("A102", "CS101", "Assignment 2: Binary Search Trees & Heaps", "Class: BSIT 4-B • Computer Science Dept", "Due Date: Oct 28, 2024 • Marks: 100 pts • Submissions: 18/35", "Published"));
        assignmentList.add(new AssignmentAdapter.AssignmentCardItem("A103", "IT402", "Assignment 3: REST API Design & Node.js", "Class: BSCS 3-A • Information Tech Dept", "Due Date: Nov 05, 2024 • Marks: 100 pts • Submissions: 0/32", "Draft"));

        assignmentAdapter = new AssignmentAdapter(requireContext(), assignmentList, new AssignmentAdapter.OnAssignmentActionClickListener() {
            @Override
            public void onGradeClick(AssignmentAdapter.AssignmentCardItem item) {
                Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
                intent.putExtra("assessment_id", item.id);
                intent.putExtra("assessment_title", item.title);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(AssignmentAdapter.AssignmentCardItem item) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Confirm Delete Assignment")
                        .setMessage("Are you sure you want to delete '" + item.title + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            assignmentList.remove(item);
                            if (assignmentAdapter != null) assignmentAdapter.filter("");
                            Toast.makeText(requireContext(), "Assignment deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        rvAssignmentsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAssignmentsList.setAdapter(assignmentAdapter);
    }

    // ==========================================
    // 2. QUIZZES SUB-TAB
    // ==========================================
    private void bindQuizzesViews(View root) {
        rvQuizzesList = root.findViewById(R.id.rvQuizzesList);
        edtSearchQuizzes = root.findViewById(R.id.edtSearchQuizzes);

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

    private void setupQuizzesList() {
        if (rvQuizzesList == null || quizAdapter != null) return;

        quizList.clear();
        quizList.add(new QuizAdapter.QuizCardItem("Q101", "CS101", "Quiz 1: Arrays & Big-O Complexity", "Class: BSCS 3-A • 10 Multiple Choice Qs", "Duration: 30 Mins • Marks: 50 pts • Completed: 30/35", "Active"));
        quizList.add(new QuizAdapter.QuizCardItem("Q102", "IT402", "Quiz 2: Web Security & JWT Tokens", "Class: BSIT 4-B • 12 Multiple Choice Qs", "Duration: 25 Mins • Marks: 50 pts • Completed: 22/32", "Active"));

        quizAdapter = new QuizAdapter(requireContext(), quizList, new QuizAdapter.OnQuizActionClickListener() {
            @Override
            public void onBuilderClick(QuizAdapter.QuizCardItem item) {
                showQuizBuilderDialog(item);
            }

            @Override
            public void onPublishClick(QuizAdapter.QuizCardItem item) {
                item.status = "Published";
                if (quizAdapter != null) quizAdapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Quiz published to students!", Toast.LENGTH_SHORT).show();
            }
        });

        rvQuizzesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvQuizzesList.setAdapter(quizAdapter);
    }

    private void showQuizBuilderDialog(QuizAdapter.QuizCardItem quiz) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        TextInputLayout tilQ = new TextInputLayout(requireContext());
        tilQ.setHint("Question Title (e.g., What is Big-O of Array Access?)");
        TextInputEditText edtQ = new TextInputEditText(tilQ.getContext());
        tilQ.addView(edtQ);
        container.addView(tilQ);

        TextInputLayout tilAns = new TextInputLayout(requireContext());
        tilAns.setHint("Correct Option (e.g., Option A: O(1))");
        TextInputEditText edtAns = new TextInputEditText(tilAns.getContext());
        tilAns.addView(edtAns);
        container.addView(tilAns);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Quiz Question Builder — " + quiz.title)
                .setView(container)
                .setPositiveButton("Add Question", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Question added to quiz successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ==========================================
    // 3. SUBMISSIONS SUB-TAB
    // ==========================================
    private void bindSubmissionsViews(View root) {
        rvSubmissionsList = root.findViewById(R.id.rvSubmissionsList);
        edtSearchSubmissions = root.findViewById(R.id.edtSearchSubmissions);

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

    private void setupSubmissionsList() {
        if (rvSubmissionsList == null || submissionAdapter != null) return;

        submissionList.clear();
        submissionList.add(new SubmissionAdapter.SubmissionCardItem("S101", "STU23001", "Sophea", "Assessment: Assignment 1 - Linked Lists & Pointers", "Submitted: Oct 10, 2024 02:30 PM • Attachment: solution_file_v1.pdf", "Submitted On Time"));
        submissionList.add(new SubmissionAdapter.SubmissionCardItem("S102", "STU23002", "Dara", "Assessment: Assignment 2 - Binary Search Trees", "Submitted: Oct 22, 2024 08:15 AM • Attachment: bst_dara_code.zip", "Late (+2 Days)"));
        submissionList.add(new SubmissionAdapter.SubmissionCardItem("S103", "STU23003", "Rathana", "Assessment: Quiz 1 - Big-O Complexity", "Submitted: Oct 15, 2024 11:00 AM • Auto-Graded Quiz Attempt", "Submitted On Time"));

        submissionAdapter = new SubmissionAdapter(requireContext(), submissionList, new SubmissionAdapter.OnSubmissionActionClickListener() {
            @Override
            public void onViewFileClick(SubmissionAdapter.SubmissionCardItem item) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Student Submission File Detail")
                        .setMessage("Student: " + item.studentName + " (" + item.studentId + ")\n" +
                                item.assessmentTitle + "\n\n" +
                                item.details + "\n\n" +
                                "Student Text Submission:\n" +
                                "\"Implemented double-linked list with tail pointer insertion and O(1) delete operations.\"")
                        .setPositiveButton("Grade Answer", (dialog, which) -> {
                            Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
                            intent.putExtra("assessment_id", item.id);
                            intent.putExtra("assessment_title", item.assessmentTitle);
                            startActivity(intent);
                        })
                        .setNegativeButton("Close", null)
                        .show();
            }

            @Override
            public void onGradeClick(SubmissionAdapter.SubmissionCardItem item) {
                Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
                intent.putExtra("assessment_id", item.id);
                intent.putExtra("assessment_title", item.assessmentTitle);
                startActivity(intent);
            }
        });

        rvSubmissionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSubmissionsList.setAdapter(submissionAdapter);
    }

    // ==========================================
    // 4. GRADING SUB-TAB
    // ==========================================
    private void bindGradingViews(View root) {
        rvGradingList = root.findViewById(R.id.rvGradingList);
        edtSearchGrading = root.findViewById(R.id.edtSearchGrading);

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

    private void setupGradingList() {
        if (rvGradingList == null || gradingAdapter != null) return;

        gradingList.clear();
        gradingList.add(new GradingAdapter.GradingCardItem("G101", "STU23001", "Sophea", "Assessment: Assignment 1 - Linked Lists", "Status: Graded & Published • Feedback: Excellent implementation!", "88 / 100 pts", 100, 88));
        gradingList.add(new GradingAdapter.GradingCardItem("G102", "STU23002", "Dara", "Assessment: Assignment 2 - Search Trees", "Status: Pending Review • Submitted Late", "58 / 100 pts", 100, 58));
        gradingList.add(new GradingAdapter.GradingCardItem("G103", "STU23003", "Rathana", "Assessment: Quiz 1 - Big-O Complexity", "Status: Graded & Published • Feedback: Good effort", "65 / 100 pts", 100, 65));

        gradingAdapter = new GradingAdapter(requireContext(), gradingList, item -> {
            showGradingModalDialog(item);
        });

        rvGradingList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGradingList.setAdapter(gradingAdapter);
    }

    private void showGradingModalDialog(GradingAdapter.GradingCardItem item) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        TextInputLayout tilScore = new TextInputLayout(requireContext());
        tilScore.setHint("Awarded Score (Max: " + item.maxMarks + " pts)");
        TextInputEditText edtScore = new TextInputEditText(tilScore.getContext());
        edtScore.setText(String.valueOf(item.currentScore));
        edtScore.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        tilScore.addView(edtScore);
        container.addView(tilScore);

        TextInputLayout tilFeedback = new TextInputLayout(requireContext());
        tilFeedback.setHint("Teacher Feedback & Comments");
        TextInputEditText edtFeedback = new TextInputEditText(tilFeedback.getContext());
        edtFeedback.setText("Great effort! Clean code structure.");
        tilFeedback.addView(edtFeedback);
        container.addView(tilFeedback);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Grade Student Submission — " + item.studentName)
                .setView(container)
                .setPositiveButton("Publish Grade", (dialog, which) -> {
                    String scoreStr = edtScore.getText() != null ? edtScore.getText().toString().trim() : "0";
                    int score = Integer.parseInt(scoreStr.isEmpty() ? "0" : scoreStr);
                    if (score > item.maxMarks) {
                        Toast.makeText(requireContext(), "Awarded score cannot exceed " + item.maxMarks + " pts!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    item.currentScore = score;
                    item.scoreBadge = score + " / " + item.maxMarks + " pts";
                    if (gradingAdapter != null) gradingAdapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "Grade published successfully for " + item.studentName + "!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
