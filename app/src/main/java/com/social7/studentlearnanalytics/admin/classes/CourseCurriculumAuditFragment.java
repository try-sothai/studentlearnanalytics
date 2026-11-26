package com.social7.studentlearnanalytics.admin.classes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.courses.AdminCourseDetailActivity;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseCurriculumAuditFragment extends Fragment {

    private RecyclerView rvCurriculumAudit;
    private CurriculumAuditAdapter adapter;
    private final List<CurriculumAuditAdapter.CurriculumAuditItem> auditList = new ArrayList<>();
    private final List<Course> auditCourses = new ArrayList<>();
    private TextView tvReviewed, tvAssigned, tvMissing, tvIssues;

    private final ActivityResultLauncher<Intent> courseDetailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadCurriculumAuditData();
                }
            }
    );

    public CourseCurriculumAuditFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_curriculum_audit, container, false);

        rvCurriculumAudit = view.findViewById(R.id.rvCurriculumAudit);
        tvReviewed = view.findViewById(R.id.tvAuditReviewed);
        tvAssigned = view.findViewById(R.id.tvAuditAssigned);
        tvMissing = view.findViewById(R.id.tvAuditMissing);
        tvIssues = view.findViewById(R.id.tvAuditIssues);
        EditText etSearch = view.findViewById(R.id.etSearchCurriculum);

        if (rvCurriculumAudit != null && getContext() != null) {
            rvCurriculumAudit.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        setupSearch(etSearch);
        loadCurriculumAuditData();

        return view;
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
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(requireContext()).getCoursesCatalog()
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded() || getContext() == null) return;
                        auditList.clear();
                        auditCourses.clear();
                        int assignedCount = 0;
                        int missingCount = 0;
                        int issuesCount = 0;

                        if (response.isSuccessful() && response.body() != null) {
                            JsonArray arr = null;
                            if (response.body().has("data") && response.body().get("data").isJsonArray()) {
                                arr = response.body().getAsJsonArray("data");
                            } else if (response.body().has("courses") && response.body().get("courses").isJsonArray()) {
                                arr = response.body().getAsJsonArray("courses");
                            }
                            if (arr != null) {
                                for (JsonElement el : arr) {
                                    if (!el.isJsonObject()) continue;
                                    Course c = Course.fromJson(el.getAsJsonObject());
                                    auditCourses.add(c);
                                    String code = !c.getCode().isEmpty() ? c.getCode() : "COURSE";
                                    String name = !c.getName().isEmpty() ? c.getName() : "Untitled Course";
                                    String dept = !c.getDepartment().isEmpty() ? c.getDepartment() : "General";
                                    String teacher = !c.getTeacher().isEmpty() ? c.getTeacher() : "Unassigned";
                                    int credits = c.getCredits();
                                    boolean unassigned = "Unassigned".equalsIgnoreCase(teacher) || teacher.trim().isEmpty();
                                    boolean missingSyllabus = c.getDescription().trim().isEmpty();
                                    if (unassigned) {
                                        missingCount++;
                                        issuesCount++;
                                    } else {
                                        assignedCount++;
                                        if (missingSyllabus) issuesCount++;
                                    }
                                    String status = unassigned ? "Missing Teacher" : (missingSyllabus ? "Missing Syllabus" : "Verified");
                                    String note = !c.getDescription().isEmpty()
                                            ? c.getDescription()
                                            : (unassigned ? "Pending faculty assignment." : "Course syllabus verified in database.");
                                    auditList.add(new CurriculumAuditAdapter.CurriculumAuditItem(
                                            code, name, dept, teacher, credits, status, note));
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to load curriculum audit.", Toast.LENGTH_SHORT).show();
                        }

                        if (tvReviewed != null) tvReviewed.setText(String.valueOf(auditList.size()));
                        if (tvAssigned != null) tvAssigned.setText(String.valueOf(assignedCount));
                        if (tvMissing != null) tvMissing.setText(String.valueOf(missingCount));
                        if (tvIssues != null) tvIssues.setText(String.valueOf(issuesCount));

                        if (rvCurriculumAudit != null && getContext() != null) {
                            adapter = new CurriculumAuditAdapter(getContext(), auditList, item -> openCourseDetail(item));
                            rvCurriculumAudit.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openCourseDetail(CurriculumAuditAdapter.CurriculumAuditItem item) {
        if (!isAdded() || getContext() == null) return;
        Course match = null;
        for (Course c : auditCourses) {
            if (c.getCode().equalsIgnoreCase(item.code) && c.getName().equalsIgnoreCase(item.name)) {
                match = c;
                break;
            }
        }
        if (match == null) {
            match = new Course(item.code, item.name, item.credits, "Active");
            match.setDepartment(item.className);
            match.setTeacher(item.teacher);
            match.setDescription(item.note);
        }
        Intent intent = new Intent(requireContext(), AdminCourseDetailActivity.class);
        intent.putExtra(AdminCourseDetailActivity.EXTRA_COURSE, match);
        courseDetailLauncher.launch(intent);
    }
}
