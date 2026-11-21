package com.social7.studentlearnanalytics.admin.classes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.AdminMainActivity;
import com.social7.studentlearnanalytics.admin.ReportsFragment;
import com.social7.studentlearnanalytics.admin.users.AdminStudentDetailActivity;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidentResolutionWorkspaceFragment extends Fragment {

    private RecyclerView rvIncidentWorkspace;
    private IncidentWorkspaceAdapter adapter;
    private final List<IncidentWorkspaceAdapter.IncidentItem> incidentList = new ArrayList<>();
    private TextView tvOpen, tvHighPriority, tvReview, tvResolved;

    public IncidentResolutionWorkspaceFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_incident_workspace, container, false);

        rvIncidentWorkspace = view.findViewById(R.id.rvIncidentWorkspace);
        tvOpen = view.findViewById(R.id.tvIncidentOpen);
        tvHighPriority = view.findViewById(R.id.tvIncidentHighPriority);
        tvReview = view.findViewById(R.id.tvIncidentReview);
        tvResolved = view.findViewById(R.id.tvIncidentResolved);

        EditText etSearch = view.findViewById(R.id.etSearchIncident);
        View btnMenu = view.findViewById(R.id.btnIncidentMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        if (rvIncidentWorkspace != null && getContext() != null) {
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
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getIncidents(null, null, null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    bindSummary(body.has("summary") && body.get("summary").isJsonObject()
                            ? body.getAsJsonObject("summary") : null);

                    incidentList.clear();
                    JsonArray arr = body.has("data") && body.get("data").isJsonArray()
                            ? body.getAsJsonArray("data") : new JsonArray();
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        IncidentWorkspaceAdapter.IncidentItem item = new IncidentWorkspaceAdapter.IncidentItem(
                                getStr(obj, "incident_code", "-"),
                                getStr(obj, "title", "Incident"),
                                getStr(obj, "student_name", "Unassigned Student"),
                                getStr(obj, "student_code", "-"),
                                getStr(obj, "class_name", "N/A"),
                                getStr(obj, "category", "General"),
                                getStr(obj, "priority", "Medium Priority"),
                                getStr(obj, "status", "Open"),
                                getStr(obj, "logged_date", "-"),
                                getStr(obj, "assigned_staff", "Unassigned")
                        );
                        item.dbId = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsLong() : 0L;
                        item.studentDbId = obj.has("student_id") && !obj.get("student_id").isJsonNull() ? obj.get("student_id").getAsLong() : 0L;
                        item.studentUserId = obj.has("student_user_id") && !obj.get("student_user_id").isJsonNull() ? obj.get("student_user_id").getAsLong() : 0L;
                        item.studentEmail = getStr(obj, "student_email", "");
                        item.description = getStr(obj, "description", "");
                        item.resolutionNotes = getStr(obj, "resolution_notes", "");
                        incidentList.add(item);
                    }

                    if (adapter != null) {
                        adapter.filter("");
                    }
                    if (incidentList.isEmpty()) {
                        Toast.makeText(getContext(), "No incidents recorded yet.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Unable to load incidents (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error loading incidents.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindSummary(JsonObject summary) {
        if (tvOpen != null) tvOpen.setText(String.valueOf(getInt(summary, "open")));
        if (tvHighPriority != null) tvHighPriority.setText(String.valueOf(getInt(summary, "high_priority")));
        if (tvReview != null) tvReview.setText(String.valueOf(getInt(summary, "in_progress")));
        if (tvResolved != null) tvResolved.setText(String.valueOf(getInt(summary, "resolved")));
    }

    private void showIncidentDetails(IncidentWorkspaceAdapter.IncidentItem item) {
        if (getContext() == null) return;

        StringBuilder msg = new StringBuilder();
        msg.append(getString(R.string.incident_id)).append(item.id).append("\n")
           .append("Summary: ").append(item.title).append("\n")
           .append("Student: ").append(item.studentName).append(" (").append(item.studentId).append(")\n")
           .append("Class: ").append(item.className).append("\n")
           .append("Category: ").append(item.category).append("\n")
           .append("Priority: ").append(item.priority).append("\n")
           .append("Status: ").append(item.status).append("\n")
           .append("Logged Date: ").append(item.date).append("\n")
           .append("Assigned Staff: ").append(item.assignedStaff);
        if (item.description != null && !item.description.isEmpty()) {
            msg.append("\nDescription: ").append(item.description);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Incident Workspace Detail")
                .setMessage(msg.toString())
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss());

        if (item.dbId > 0) {
            builder.setNeutralButton("Update Status", (dialog, which) -> showUpdateStatusDialog(item));
        }
        if (item.studentDbId > 0) {
            builder.setPositiveButton("Open Student Dossier", (dialog, which) -> openStudentDossier(item));
        }
        builder.show();
    }

    private void showUpdateStatusDialog(IncidentWorkspaceAdapter.IncidentItem item) {
        if (getContext() == null) return;

        android.widget.LinearLayout box = new android.widget.LinearLayout(requireContext());
        box.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);

        String[] statuses = new String[]{"Open", "In Progress", "Under Review", "Resolved", "Closed"};
        Spinner spStatus = new Spinner(requireContext());
        spStatus.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, statuses));
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(item.status)) {
                spStatus.setSelection(i);
                break;
            }
        }
        box.addView(spStatus);

        EditText etNotes = new EditText(requireContext());
        etNotes.setHint("Resolution notes (optional)");
        if (item.resolutionNotes != null) etNotes.setText(item.resolutionNotes);
        box.addView(etNotes);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Update Incident " + item.id)
                .setView(box)
                .setPositiveButton(R.string.submit, (d, w) -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("status", (String) spStatus.getSelectedItem());
                    String notes = etNotes.getText().toString().trim();
                    if (!notes.isEmpty()) body.addProperty("resolution_notes", notes);
                    ApiClient.getApiService(requireContext()).updateIncident(item.dbId, body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (!isAdded() || getContext() == null) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Incident updated.", Toast.LENGTH_SHORT).show();
                                loadIncidentData();
                            } else {
                                Toast.makeText(getContext(), "Update failed (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            if (!isAdded() || getContext() == null) return;
                            Toast.makeText(getContext(), "Network error updating incident.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openStudentDossier(IncidentWorkspaceAdapter.IncidentItem item) {
        if (getContext() == null) return;
        if (item.studentDbId <= 0) {
            Toast.makeText(getContext(), "No student linked to this incident.", Toast.LENGTH_SHORT).show();
            return;
        }

        Student student = new Student(
                item.studentId,
                item.studentName,
                item.studentEmail != null ? item.studentEmail : "",
                "",
                "",
                "",
                0.0,
                "",
                0.0,
                "#FFEBEF",
                "#FF4055",
                item.className != null ? item.className : ""
        );
        student.setDbId(item.studentDbId);
        if (item.studentUserId > 0) {
            student.setUserId(item.studentUserId);
        }

        Intent intent = new Intent(requireContext(), AdminStudentDetailActivity.class);
        intent.putExtra("student", student);
        startActivity(intent);
    }

    private String getStr(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            String val = obj.get(key).getAsString();
            return val != null && !val.isEmpty() ? val : fallback;
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
}
