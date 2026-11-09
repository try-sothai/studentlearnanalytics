package com.social7.studentlearnanalytics.admin.users;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentExportDialog extends BottomSheetDialog {

    private final List<AddStudentActivity.IdNameItem> deptList = new ArrayList<>();
    private final List<AddStudentActivity.IdNameItem> allProgList = new ArrayList<>();
    private final List<AddStudentActivity.IdNameItem> filteredProgList = new ArrayList<>();
    private final List<AddStudentActivity.IdNameItem> yearList = new ArrayList<>();

    public StudentExportDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_student_export, null);
        setContentView(view);

        final Context appContext = getContext().getApplicationContext();

        RadioGroup rgFormat = view.findViewById(R.id.rgExportFormat);
        Spinner spDept = view.findViewById(R.id.spExportDepartment);
        Spinner spProg = view.findViewById(R.id.spExportProgram);
        Spinner spYearLevel = view.findViewById(R.id.spExportYearLevel);
        Spinner spAcademicYear = view.findViewById(R.id.spExportAcademicYear);
        MaterialButton btnExport = view.findViewById(R.id.btnDoExport);

        ArrayAdapter<String> ylAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{"All Year Levels", "Year 1", "Year 2", "Year 3", "Year 4", "Year 5"});
        ylAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYearLevel.setAdapter(ylAdapter);

        spDept.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                AddStudentActivity.IdNameItem dept = position >= 0 && position < deptList.size() ? deptList.get(position) : null;
                filteredProgList.clear();
                filteredProgList.add(new AddStudentActivity.IdNameItem(0, "All Programs", "", 0));
                for (AddStudentActivity.IdNameItem p : allProgList) {
                    if (p.id == 0) continue;
                    if (dept == null || dept.id == 0 || p.parentId == dept.id) {
                        filteredProgList.add(p);
                    }
                }
                setSpinner(spProg, filteredProgList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Fetch lookups
        ApiClient.getApiService(appContext).getStudentLookups().enqueue(new Callback<JsonObject>() {
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
                            deptList.add(new AddStudentActivity.IdNameItem(
                                    o.get("id").getAsInt(),
                                    o.get("name").getAsString(),
                                    o.has("code") && !o.get("code").isJsonNull() ? o.get("code").getAsString() : "",
                                    0
                            ));
                        }
                    }
                    setSpinner(spDept, deptList);

                    allProgList.clear();
                    allProgList.add(new AddStudentActivity.IdNameItem(0, "All Programs", "", 0));
                    if (d.has("programs")) {
                        for (JsonElement el : d.getAsJsonArray("programs")) {
                            JsonObject o = el.getAsJsonObject();
                            allProgList.add(new AddStudentActivity.IdNameItem(
                                    o.get("id").getAsInt(),
                                    o.get("name").getAsString(),
                                    o.has("code") && !o.get("code").isJsonNull() ? o.get("code").getAsString() : "",
                                    o.has("department_id") && !o.get("department_id").isJsonNull() ? o.get("department_id").getAsInt() : 0
                            ));
                        }
                    }
                    filteredProgList.clear();
                    filteredProgList.addAll(allProgList);
                    setSpinner(spProg, filteredProgList);

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

            Toast.makeText(appContext, "Exporting students...", Toast.LENGTH_SHORT).show();
            dismiss();

            ApiClient.getApiService(appContext).exportStudents(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String filename = "SLAS_Students_Export_" + System.currentTimeMillis() + "." + format;
                        saveExportFile(appContext, response.body(), filename, isCsv ? "text/csv" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    } else {
                        Toast.makeText(appContext, "Export failed (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(appContext, "Network error exporting: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

    public static void saveExportFile(Context appContext, ResponseBody body, String filename, String mimeType) {
        new Thread(() -> {
            String savedLocation = null;
            try {
                byte[] bytes = body.bytes();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                    values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    Uri uri = appContext.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        try (OutputStream os = appContext.getContentResolver().openOutputStream(uri)) {
                            if (os != null) {
                                os.write(bytes);
                                savedLocation = "Downloads/" + filename;
                            }
                        }
                    }
                }

                File dir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (dir == null) dir = appContext.getFilesDir();
                File outFile = new File(dir, filename);
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(bytes);
                    if (savedLocation == null) {
                        savedLocation = outFile.getAbsolutePath();
                    }
                }
            } catch (Exception e) {
                final String errMsg = e.getMessage();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(appContext, "Failed to save file: " + errMsg, Toast.LENGTH_LONG).show()
                );
                return;
            }

            final String finalLoc = savedLocation;
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(appContext, "Saved to " + finalLoc, Toast.LENGTH_LONG).show()
            );
        }).start();
    }
}
