package com.social7.studentlearnanalytics.admin.classes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.AdminMainActivity;
import com.social7.studentlearnanalytics.admin.CourseManagementFragment;

import java.util.ArrayList;
import java.util.List;

public class CourseCurriculumAuditFragment extends Fragment {

    private RecyclerView rvCurriculumAudit;
    private CurriculumAuditAdapter adapter;
    private final List<CurriculumAuditAdapter.CurriculumAuditItem> auditList = new ArrayList<>();

    public CourseCurriculumAuditFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_curriculum_audit, container, false);

        rvCurriculumAudit = view.findViewById(R.id.rvCurriculumAudit);
        EditText etSearch = view.findViewById(R.id.etSearchCurriculum);

        setupSearch(etSearch);
        loadCurriculumAuditData();

        return view;
    }

    private void setupSubNavigation(View view) {
    }

    private void setupSearch(EditText etSearch) {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) adapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadCurriculumAuditData() {
        auditList.clear();
        auditList.add(new CurriculumAuditAdapter.CurriculumAuditItem("CS101", "Data Structures & Algorithms", "BSCS 3-A", "Engr. John Reyes", 3, "Verified", "All course modules & syllabi verified for AY 2025-2026."));
        auditList.add(new CurriculumAuditAdapter.CurriculumAuditItem("CS102", "Database Management Systems", "BSIT 4-A", "Prof. Alan Santos", 3, "Verified", "Syllabus updated with relational design & indexing."));
        auditList.add(new CurriculumAuditAdapter.CurriculumAuditItem("IT201", "Web Systems & Technologies", "BSIT 4-B", "Unassigned", 3, "Missing Teacher", "Pending faculty assignment for upcoming semester."));
        auditList.add(new CurriculumAuditAdapter.CurriculumAuditItem("CS202", "Object-Oriented Programming", "BSCS 3-B", "Prof. Sarah Jenkins", 3, "Missing Syllabus", "Course syllabus pending department head sign-off."));
        auditList.add(new CurriculumAuditAdapter.CurriculumAuditItem("IS301", "Enterprise Systems Architecture", "BSIS 2-A", "Prof. Robert Lim", 3, "Verified", "Curriculum aligned with international IS standards."));
        auditList.add(new CurriculumAuditAdapter.CurriculumAuditItem("CS401", "Artificial Intelligence & ML", "BSCS 4-A", "Dr. Maria Cruz", 3, "Verified", "Prerequisites and credit distribution verified."));
        auditList.add(new CurriculumAuditAdapter.CurriculumAuditItem("IT404", "Cloud Computing & DevOps", "BSIT 4-A", "Unassigned", 3, "Missing Syllabus", "Syllabus draft under academic committee review."));

        if (getContext() != null && rvCurriculumAudit != null) {
            adapter = new CurriculumAuditAdapter(getContext(), auditList, this::showItemDetails);
            rvCurriculumAudit.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCurriculumAudit.setAdapter(adapter);
        }
    }

    private void showItemDetails(CurriculumAuditAdapter.CurriculumAuditItem item) {
        if (getContext() == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Curriculum Audit Detail")
                .setMessage("Course Code: " + item.code + "\n" +
                        "Course Name: " + item.name + "\n" +
                        "Associated Class: " + item.className + "\n" +
                        "Assigned Teacher: " + item.teacher + "\n" +
                        "Credits: " + item.credits + " Units\n" +
                        "Syllabus Audit Status: " + item.status + "\n\n" +
                        "Audit Findings & Notes:\n" + item.note)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
