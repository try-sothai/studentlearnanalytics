package com.social7.studentlearnanalytics.admin.users;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentExportDialog extends BottomSheetDialog {

    private final List<AddStudentActivity.IdNameItem> deptList = new ArrayList<>();
    private final List<AddStudentActivity.IdNameItem> progList = new ArrayList<>();
    private final List<AddStudentActivity.IdNameItem> yearList = new ArrayList<>();

    public StudentExportDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_student_export, null);
        setContentView(view);

        RadioGroup rgFormat = view.findViewById(R.id.rgExportFormat);
        Spinner spDept = view.findViewById(R.id.spExportDepartment);
        Spinner spProg = view.findViewById(R.id.spExportProgram);
        Spinner spYearLevel = view.findViewById(R.id.spExportYearLevel);
        Spinner spAcademicYear = view.findViewById(R.id.spExportAcademicYear);
        MaterialButton btnExport = view.findViewById(R.id.btnDoExport);

        ArrayAdapter<String> ylAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{"All Year Levels", "Year 1", "Year 2", "Year 3", "Year 4"});
        ylAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYearLevel.setAdapter(ylAdapter);

        // Fetch lookups
        ApiClient.getApiService(getContext()).getStudentLookups().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject d = response.body().has("data") ? response.body().getAsJsonObject("data") : null;
                    if (d == null) return;

                    deptList.clear();
                    deptList.add(new AddStudentActivity.IdNameItem(0, "All Departments", "", 0));
                    if (d.has("departments")) {
                        for (JsonElement el : d.getAsJsonArray("departments")) {
                            JsonObject o = el.getAsJsonObject();
                            deptList.add(new AddStudentActivity.IdNameItem(o.get("id").getAsInt(), o.get("name").getAsString(), "", 0));
                        }
                    }
                    setSpinner(spDept, deptList);

                    progList.clear();
                    progList.add(new AddStudentActivity.IdNameItem(0, "All Programs", "", 0));
                    if (d.has("programs")) {
                        for (JsonElement el : d.getAsJsonArray("programs")) {
                            JsonObject o = el.getAsJsonObject();
                            progList.add(new AddStudentActivity.IdNameItem(o.get("id").getAsInt(), o.get("name").getAsString(), "", 0));
                        }
                    }
                    setSpinner(spProg, progList);

                    yearList.clear();
                    yearList.add(new AddStudentActivity.IdNameItem(0, "All Academic Years", "", 0));
                    if (d.has("academic_years")) {
                        for (JsonElement el : d.getAsJsonArray("academic_years")) {
                            JsonObject o = el.getAsJsonObject();
                            yearList.add(new AddStudentActivity.IdNameItem(o.get("id").getAsInt(), o.get("name").getAsString(), "", 0));
                        }
                    }
                    setSpinner(spAcademicYear, yearList);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });

        btnExport.setOnClickListener(v -> {
            boolean isCsv = rgFormat.getCheckedRadioButtonId() == R.id.rbFormatCsv;
            String format = isCsv ? "csv" : "xlsx";

            JsonObject body = new JsonObject();
            body.addProperty("format", format);

            AddStudentActivity.IdNameItem dept = (AddStudentActivity.IdNameItem) spDept.getSelectedItem();
            if (dept != null && dept.id > 0) body.addProperty("department_id", dept.id);

            AddStudentActivity.IdNameItem prog = (AddStudentActivity.IdNameItem) spProg.getSelectedItem();
            if (prog != null && prog.id > 0) body.addProperty("program_id", prog.id);

            int yl = spYearLevel.getSelectedItemPosition();
            if (yl > 0) body.addProperty("year_level", yl);

            AddStudentActivity.IdNameItem ay = (AddStudentActivity.IdNameItem) spAcademicYear.getSelectedItem();
            if (ay != null && ay.id > 0) body.addProperty("academic_year_id", ay.id);

            Toast.makeText(getContext(), "Exporting students...", Toast.LENGTH_SHORT).show();
            dismiss();

            ApiClient.getApiService(getContext()).exportStudents(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        saveExportFile(response.body(), "students_export." + format);
                    } else {
                        Toast.makeText(getContext(), "Export failed (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Network error exporting: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setSpinner(Spinner sp, List<AddStudentActivity.IdNameItem> items) {
        ArrayAdapter<AddStudentActivity.IdNameItem> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }

    private void saveExportFile(ResponseBody body, String filename) {
        new Thread(() -> {
            try {
                File dir = getContext().getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS);
                if (dir == null) dir = getContext().getFilesDir();
                File outFile = new File(dir, filename);

                try (InputStream is = body.byteStream();
                     FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }
}
