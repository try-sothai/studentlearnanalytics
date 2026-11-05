package com.social7.studentlearnanalytics.admin.classes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.AdminMainActivity;
import com.social7.studentlearnanalytics.admin.ReportsFragment;
import com.social7.studentlearnanalytics.admin.users.AdminStudentDetailActivity;
import com.social7.studentlearnanalytics.model.Student;

import java.util.ArrayList;
import java.util.List;

public class IncidentResolutionWorkspaceFragment extends Fragment {

    private RecyclerView rvIncidentWorkspace;
    private IncidentWorkspaceAdapter adapter;
    private final List<IncidentWorkspaceAdapter.IncidentItem> incidentList = new ArrayList<>();

    public IncidentResolutionWorkspaceFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_incident_workspace, container, false);

        rvIncidentWorkspace = view.findViewById(R.id.rvIncidentWorkspace);
        EditText etSearch = view.findViewById(R.id.etSearchIncident);
        View btnMenu = view.findViewById(R.id.btnIncidentMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        setupSubNavigation(view);
        setupSearch(etSearch);
        loadIncidentData();

        return view;
    }

    private void setupSubNavigation(View view) {
        MaterialButton btnReportsOverview = view.findViewById(R.id.btnSubTabReportsOverview);

        if (btnReportsOverview != null) {
            btnReportsOverview.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new ReportsFragment());
                }
            });
        }
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

    private void loadIncidentData() {
        incidentList.clear();
        incidentList.add(new IncidentWorkspaceAdapter.IncidentItem("INC-2026-001", "Low Attendance Alert (Below 75%)", "Sophea", "STU23001", "BSIT 4-A", "Attendance", "High Priority", "Open", "Mar 10, 2026", "Advisor Santos"));
        incidentList.add(new IncidentWorkspaceAdapter.IncidentItem("INC-2026-002", "GPA Risk Assessment Failure", "David Miller", "STU23002", "BSIT 4-B", "Academic Risk", "High Priority", "Under Review", "Mar 08, 2026", "Dr. Maria Cruz"));
        incidentList.add(new IncidentWorkspaceAdapter.IncidentItem("INC-2026-003", "Missing Midterm Exam", "Channary", "STU23003", "BSCS 3-A", "Assessment", "Medium Priority", "Open", "Mar 05, 2026", "Engr. John Reyes"));
        incidentList.add(new IncidentWorkspaceAdapter.IncidentItem("INC-2026-004", "Course Prerequisite Waiver Request", "Sokha", "STU23004", "BSIS 2-A", "Administrative", "Low Priority", "Resolved", "Feb 28, 2026", "Prof. Robert Lim"));

        if (getContext() != null && rvIncidentWorkspace != null) {
            adapter = new IncidentWorkspaceAdapter(getContext(), incidentList, new IncidentWorkspaceAdapter.OnIncidentClickListener() {
                @Override
                public void onIncidentClick(IncidentWorkspaceAdapter.IncidentItem item) {
                    showIncidentDetails(item);
                }

                @Override
                public void onStudentClick(IncidentWorkspaceAdapter.IncidentItem item) {
                    openStudentDossier(item);
                }
            });
            rvIncidentWorkspace.setLayoutManager(new LinearLayoutManager(getContext()));
            rvIncidentWorkspace.setAdapter(adapter);
        }
    }

    private void showIncidentDetails(IncidentWorkspaceAdapter.IncidentItem item) {
        if (getContext() == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Incident Workspace Detail")
                .setMessage("Incident ID: " + item.id + "\n" +
                        "Summary: " + item.title + "\n" +
                        "Student: " + item.studentName + " (" + item.studentId + ")\n" +
                        "Class: " + item.className + "\n" +
                        "Category: " + item.category + "\n" +
                        "Priority: " + item.priority + "\n" +
                        "Status: " + item.status + "\n" +
                        "Logged Date: " + item.date + "\n" +
                        "Assigned Staff: " + item.assignedStaff)
                .setPositiveButton("Open Student Dossier", (dialog, which) -> openStudentDossier(item))
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openStudentDossier(IncidentWorkspaceAdapter.IncidentItem item) {
        if (getContext() == null) return;

        Student student = new Student(
                item.studentId,
                item.studentName,
                item.studentName.toLowerCase().replace(" ", "") + "@univ.edu",
                "Computer Science",
                "Bachelor of Science",
                "Year 3",
                3.15,
                "At Risk",
                72.0,
                "#FFEBEF",
                "#FF4055",
                "CS101"
        );

        Intent intent = new Intent(requireContext(), AdminStudentDetailActivity.class);
        intent.putExtra("student", student);
        startActivity(intent);
    }
}
