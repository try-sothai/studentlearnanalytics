package com.social7.studentlearnanalytics.admin.classes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.utils.UiUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Reusable full-screen searchable selection page for Classes, Students, Teachers,
 * Courses, Departments, Programs, Buildings, Floors, Academic Years, and Semesters.
 */
public class RecordSelectorActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "selector_type";
    public static final String EXTRA_MULTI_SELECT = "multi_select";
    public static final String EXTRA_FILTER_PARENT_ID = "filter_parent_id";
    public static final String EXTRA_PRELOADED_JSON = "preloaded_json";
    public static final String EXTRA_SELECTED_ID = "selected_id";

    public static final String RESULT_ID = "result_id";
    public static final String RESULT_CODE = "result_code";
    public static final String RESULT_TITLE = "result_title";
    public static final String RESULT_SUBTITLE = "result_subtitle";
    public static final String RESULT_RAW_JSON = "result_raw_json";
    public static final String RESULT_MULTI_JSON = "result_multi_json";

    public static final String TYPE_CLASS = "class";
    public static final String TYPE_STUDENT = "student";
    public static final String TYPE_TEACHER = "teacher";
    public static final String TYPE_COURSE = "course";
    public static final String TYPE_DEPARTMENT = "department";
    public static final String TYPE_PROGRAM = "program";
    public static final String TYPE_COURSE_CATEGORY = "course_category";
    public static final String TYPE_BUILDING = "building";
    public static final String TYPE_FLOOR = "floor";
    public static final String TYPE_ACADEMIC_YEAR = "academic_year";
    public static final String TYPE_SEMESTER = "semester";

    public static Intent createIntent(Context ctx, String type) {
        Intent intent = new Intent(ctx, RecordSelectorActivity.class);
        intent.putExtra(EXTRA_TYPE, type);
        return intent;
    }

    public static class SelectableItem {
        public String id;
        public String code;
        public String title;
        public String subtitle;
        public String badge;
        public String rawJson;
    }

    private String selectorType;
    private boolean multiSelect;
    private int filterParentId;
    private String preloadedJson;
    private String preselectedId;

    private TextView tvTitle, tvSubtitle, tvCount, tvEmpty;
    private TextInputEditText etSearch;
    private RecyclerView rvItems;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private MaterialButton btnRetry, btnCancel, btnConfirm;

    private final List<SelectableItem> masterList = new ArrayList<>();
    private final List<SelectableItem> filteredList = new ArrayList<>();
    private final Set<String> selectedIds = new LinkedHashSet<>();
    private SelectableAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_selector);

        selectorType = getIntent().getStringExtra(EXTRA_TYPE);
        if (selectorType == null) selectorType = TYPE_CLASS;
        multiSelect = getIntent().getBooleanExtra(EXTRA_MULTI_SELECT, false);
        filterParentId = getIntent().getIntExtra(EXTRA_FILTER_PARENT_ID, -1);
        preloadedJson = getIntent().getStringExtra(EXTRA_PRELOADED_JSON);
        preselectedId = getIntent().getStringExtra(EXTRA_SELECTED_ID);
        if (preselectedId != null && !preselectedId.isEmpty()) {
            for (String part : preselectedId.split(",")) {
                String trimmed = part != null ? part.trim() : "";
                if (!trimmed.isEmpty()) selectedIds.add(trimmed);
            }
        }

        bindViews();
        configureHeaderForType();
        loadData();
    }

    private void bindViews() {
        ImageButton btnBack = findViewById(R.id.btnSelectorBack);
        tvTitle = findViewById(R.id.tvSelectorTitle);
        tvSubtitle = findViewById(R.id.tvSelectorSubtitle);
        tvCount = findViewById(R.id.tvSelectorCount);
        tvEmpty = findViewById(R.id.tvSelectorEmpty);
        etSearch = findViewById(R.id.etSelectorSearch);
        rvItems = findViewById(R.id.rvSelectorItems);
        pbLoading = findViewById(R.id.pbSelectorLoading);
        layoutEmpty = findViewById(R.id.layoutSelectorEmpty);
        btnRetry = findViewById(R.id.btnSelectorRetry);
        btnCancel = findViewById(R.id.btnSelectorCancel);
        btnConfirm = findViewById(R.id.btnSelectorConfirm);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> finish());
        if (btnRetry != null) btnRetry.setOnClickListener(v -> loadData());
        if (btnConfirm != null) btnConfirm.setOnClickListener(v -> confirmAndReturn());

        adapter = new SelectableAdapter();
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterItems(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        updateSelectionState();
    }

    private void configureHeaderForType() {
        int titleRes = R.string.select_class_title;
        int hintRes = R.string.search_classes_hint;
        switch (selectorType) {
            case TYPE_STUDENT:
                titleRes = R.string.select_student_title;
                hintRes = R.string.search_eligible_students_hint;
                break;
            case TYPE_TEACHER:
                titleRes = R.string.select_teacher_title;
                hintRes = R.string.search_teachers;
                break;
            case TYPE_COURSE:
                titleRes = R.string.select_course_title;
                hintRes = R.string.search_courses_by_code_or_name;
                break;
            case TYPE_DEPARTMENT:
                titleRes = R.string.select_department_title;
                break;
            case TYPE_PROGRAM:
                titleRes = R.string.select_program_title;
                break;
            case TYPE_COURSE_CATEGORY:
                titleRes = R.string.course_categories;
                hintRes = R.string.search_categories;
                break;
            case TYPE_BUILDING:
                titleRes = R.string.select_building_title;
                hintRes = R.string.search_building_hint;
                break;
            case TYPE_FLOOR:
                titleRes = R.string.select_floor_title;
                hintRes = R.string.search_floor_hint;
                break;
            case TYPE_ACADEMIC_YEAR:
                titleRes = R.string.select_academic_year_title;
                break;
            case TYPE_SEMESTER:
                titleRes = R.string.select_semester_title;
                break;
            case TYPE_CLASS:
            default:
                titleRes = R.string.select_class_title;
                hintRes = R.string.search_classes_hint;
                break;
        }
        tvTitle.setText(titleRes);
        etSearch.setHint(hintRes);
    }

    private void loadData() {
        if (preloadedJson != null && !preloadedJson.trim().isEmpty()) {
            try {
                JsonArray arr = JsonParser.parseString(preloadedJson).getAsJsonArray();
                masterList.clear();
                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    SelectableItem item = parseGenericObject(el.getAsJsonObject());
                    if (item != null) masterList.add(item);
                }
                filterItems(etSearch.getText() != null ? etSearch.getText().toString() : "");
                return;
            } catch (Exception ignored) {
            }
        }

        showLoading(true);
        ApiService api = ApiClient.getApiService(this);

        if (TYPE_CLASS.equals(selectorType)
                || TYPE_DEPARTMENT.equals(selectorType)
                || TYPE_PROGRAM.equals(selectorType)
                || TYPE_ACADEMIC_YEAR.equals(selectorType)
                || TYPE_SEMESTER.equals(selectorType)) {
            api.getClassEnrollmentLookups().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                        JsonObject data = response.body().getAsJsonObject("data");
                        parseLookupsResponse(data);
                    } else {
                        showError(getString(R.string.network_error_server));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    showLoading(false);
                    showError(getString(R.string.network_error_server));
                }
            });
            return;
        }

        Call<JsonObject> call;
        if (TYPE_TEACHER.equals(selectorType)) {
            call = api.getTeachers();
        } else if (TYPE_COURSE.equals(selectorType)) {
            call = api.getCoursesCatalog();
        } else if (TYPE_STUDENT.equals(selectorType)) {
            call = api.getStudents();
        } else if (TYPE_BUILDING.equals(selectorType)) {
            call = api.getBuildings();
        } else if (TYPE_COURSE_CATEGORY.equals(selectorType)) {
            call = api.getCourseCategories();
        } else if (TYPE_FLOOR.equals(selectorType)) {
            call = api.getFloors(filterParentId > 0 ? filterParentId : null);
        } else {
            showLoading(false);
            filterItems("");
            return;
        }

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonElement dataEl = response.body().get("data");
                    if (dataEl.isJsonArray()) {
                        masterList.clear();
                        for (JsonElement el : dataEl.getAsJsonArray()) {
                            if (!el.isJsonObject()) continue;
                            SelectableItem item = parseEntityByType(el.getAsJsonObject());
                            if (item != null) masterList.add(item);
                        }
                        filterItems(etSearch.getText() != null ? etSearch.getText().toString() : "");
                        return;
                    }
                }
                showError(getString(R.string.network_error_server));
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                showLoading(false);
                showError(getString(R.string.network_error_server));
            }
        });
    }

    private void parseLookupsResponse(JsonObject data) {
        masterList.clear();
        String arrayKey = "classes";
        if (TYPE_DEPARTMENT.equals(selectorType)) arrayKey = "departments";
        else if (TYPE_PROGRAM.equals(selectorType)) arrayKey = "programs";
        else if (TYPE_ACADEMIC_YEAR.equals(selectorType)) arrayKey = "academic_years";
        else if (TYPE_SEMESTER.equals(selectorType)) arrayKey = "semesters";

        if (data.has(arrayKey) && data.get(arrayKey).isJsonArray()) {
            for (JsonElement el : data.getAsJsonArray(arrayKey)) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                if (TYPE_PROGRAM.equals(selectorType) && filterParentId > 0) {
                    int deptId = UiUtils.optInt(obj, "department_id", -1);
                    if (deptId != -1 && deptId != filterParentId) continue;
                }
                if (TYPE_SEMESTER.equals(selectorType) && filterParentId > 0) {
                    int ayId = UiUtils.optInt(obj, "academic_year_id", -1);
                    if (ayId != -1 && ayId != filterParentId) continue;
                }
                SelectableItem item = parseEntityByType(obj);
                if (item != null) masterList.add(item);
            }
        }
        filterItems(etSearch.getText() != null ? etSearch.getText().toString() : "");
    }

    private SelectableItem parseEntityByType(JsonObject obj) {
        SelectableItem item = new SelectableItem();
        item.rawJson = obj.toString();
        item.id = UiUtils.optString(obj, "id", "");

        switch (selectorType) {
            case TYPE_BUILDING: {
                String name = UiUtils.optString(obj, "name", "Building");
                String code = UiUtils.optString(obj, "code", "");
                int floorsCount = UiUtils.optInt(obj, "floors_count", 0);
                int classesCount = UiUtils.optInt(obj, "classes_count", 0);
                item.code = code.isEmpty() ? name : code;
                item.title = name;
                item.subtitle = floorsCount + " Floors • " + classesCount + " Classes";
                item.badge = UiUtils.optString(obj, "status", "active").toUpperCase(Locale.ROOT);
                return item;
            }
            case TYPE_FLOOR: {
                String name = UiUtils.optString(obj, "name", "Floor");
                int num = UiUtils.optInt(obj, "floor_number", 1);
                String bName = "";
                if (obj.has("building") && obj.get("building").isJsonObject()) {
                    bName = UiUtils.optString(obj.getAsJsonObject("building"), "name", "");
                }
                item.code = String.valueOf(num);
                item.title = name;
                item.subtitle = (bName.isEmpty() ? "" : bName + " • ") + UiUtils.optInt(obj, "classes_count", 0) + " Classes";
                item.badge = "F" + num;
                return item;
            }
            case TYPE_CLASS: {
                String name = UiUtils.optString(obj, "name", "Class");
                String code = UiUtils.optString(obj, "code", "");
                String dept = UiUtils.optString(obj, "department_name", "");
                String ay = UiUtils.optString(obj, "academic_year", "");
                String bName = UiUtils.optString(obj, "building_name", "");
                String fName = UiUtils.optString(obj, "floor_name", "");
                String loc = !bName.isEmpty() ? (bName + (fName.isEmpty() ? "" : " • " + fName)) : "";
                int count = UiUtils.optInt(obj, "students_count", 0);
                String status = UiUtils.optString(obj, "status", "active");
                item.code = code;
                item.title = code.isEmpty() ? name : (name + " (" + code + ")");
                item.subtitle = (loc.isEmpty() ? "" : loc + " • ")
                        + (dept.isEmpty() ? "" : dept + " • ")
                        + (ay.isEmpty() ? "" : ay + " • ")
                        + count + " students";
                item.badge = status.toUpperCase(Locale.ROOT);
                return item;
            }
            case TYPE_TEACHER: {
                String first = UiUtils.optString(obj, "first_name", "");
                String last = UiUtils.optString(obj, "last_name", "");
                String fullName = UiUtils.optString(obj, "name", (first + " " + last).trim());
                if (fullName.isEmpty()) fullName = UiUtils.optString(obj, "teacher_id", "Teacher");
                String tCode = UiUtils.optString(obj, "teacher_code", UiUtils.optString(obj, "teacher_id", ""));
                String pos = UiUtils.optString(obj, "position", "");
                String deptName = "";
                if (obj.has("department") && obj.get("department").isJsonObject()) {
                    deptName = UiUtils.optString(obj.getAsJsonObject("department"), "name", "");
                }
                item.code = tCode;
                item.title = fullName;
                item.subtitle = (tCode.isEmpty() ? "" : tCode + " • ")
                        + (deptName.isEmpty() ? "" : deptName + " • ")
                        + (pos.isEmpty() ? "Lecturer" : pos);
                item.badge = UiUtils.optString(obj, "work_type", "");
                return item;
            }
            case TYPE_COURSE: {
                String name = UiUtils.optString(obj, "name", UiUtils.optString(obj, "course_name", "Course"));
                String code = UiUtils.optString(obj, "code", UiUtils.optString(obj, "course_code", ""));
                int credits = UiUtils.optInt(obj, "credits", UiUtils.optInt(obj, "credit_hours", 3));
                String dept = "";
                if (obj.has("department") && obj.get("department").isJsonObject()) {
                    dept = UiUtils.optString(obj.getAsJsonObject("department"), "name", "");
                }
                item.code = code;
                item.title = code.isEmpty() ? name : (code + " — " + name);
                item.subtitle = (dept.isEmpty() ? "" : dept + " • ") + credits + " Credits";
                return item;
            }
            case TYPE_STUDENT: {
                String sCode = UiUtils.optString(obj, "student_code", UiUtils.optString(obj, "student_id", ""));
                String name = UiUtils.optString(obj, "english_name", "");
                if (name.isEmpty()) {
                    name = (UiUtils.optString(obj, "first_name", "") + " " + UiUtils.optString(obj, "last_name", "")).trim();
                }
                if (name.isEmpty()) name = UiUtils.optString(obj, "khmer_name", sCode);
                String dept = "";
                if (obj.has("department") && obj.get("department").isJsonObject()) {
                    dept = UiUtils.optString(obj.getAsJsonObject("department"), "name", "");
                }
                item.code = sCode;
                item.title = name;
                item.subtitle = sCode + (dept.isEmpty() ? "" : " • " + dept);
                item.badge = UiUtils.optString(obj, "status", "enrolled").toUpperCase(Locale.ROOT);
                return item;
            }
            case TYPE_DEPARTMENT:
            case TYPE_PROGRAM:
            case TYPE_COURSE_CATEGORY: {
                String code = UiUtils.optString(obj, "code", "");
                String name = UiUtils.optString(obj, "name", "");
                String desc = UiUtils.optString(obj, "description", "");
                item.code = code;
                item.title = name;
                item.subtitle = !desc.isEmpty() ? (code.isEmpty() ? desc : (code + " • " + desc)) : code;
                return item;
            }
            case TYPE_ACADEMIC_YEAR: {
                String name = UiUtils.optString(obj, "name", "");
                item.code = name;
                item.title = name;
                item.subtitle = UiUtils.optString(obj, "status", "active");
                return item;
            }
            case TYPE_SEMESTER: {
                String name = UiUtils.optString(obj, "name", "");
                int num = UiUtils.optInt(obj, "semester_number", 1);
                item.code = String.valueOf(num);
                item.title = name.isEmpty() ? ("Semester " + num) : name;
                item.subtitle = "Semester " + num;
                return item;
            }
            default:
                return parseGenericObject(obj);
        }
    }

    private SelectableItem parseGenericObject(JsonObject obj) {
        SelectableItem item = new SelectableItem();
        item.rawJson = obj.toString();
        item.id = UiUtils.optString(obj, "id", UiUtils.optString(obj, "code", ""));
        item.code = UiUtils.optString(obj, "code", item.id);
        item.title = UiUtils.optString(obj, "title", UiUtils.optString(obj, "name", item.code));
        item.subtitle = UiUtils.optString(obj, "subtitle", "");
        item.badge = UiUtils.optString(obj, "badge", "");
        if (item.id.isEmpty()) item.id = item.title;
        return item;
    }

    private void filterItems(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        filteredList.clear();
        for (SelectableItem item : masterList) {
            if (q.isEmpty()
                    || (item.title != null && item.title.toLowerCase(Locale.ROOT).contains(q))
                    || (item.code != null && item.code.toLowerCase(Locale.ROOT).contains(q))
                    || (item.subtitle != null && item.subtitle.toLowerCase(Locale.ROOT).contains(q))) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        boolean isEmpty = filteredList.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (isEmpty) {
            tvEmpty.setText(R.string.search_no_results);
            btnRetry.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean loading) {
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            layoutEmpty.setVisibility(View.GONE);
            rvItems.setVisibility(View.GONE);
        }
    }

    private void showError(String msg) {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvItems.setVisibility(View.GONE);
        tvEmpty.setText(msg);
        btnRetry.setVisibility(View.VISIBLE);
    }

    private void updateSelectionState() {
        int count = selectedIds.size();
        btnConfirm.setEnabled(count > 0);
        if (multiSelect && count > 0) {
            tvCount.setVisibility(View.VISIBLE);
            tvCount.setText(getString(R.string.selected_count_format, count));
        } else {
            tvCount.setVisibility(View.GONE);
        }
    }

    private void confirmAndReturn() {
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, R.string.search_no_results, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent result = new Intent();
        if (multiSelect) {
            List<SelectableItem> chosen = new ArrayList<>();
            for (SelectableItem item : masterList) {
                if (selectedIds.contains(item.id)) chosen.add(item);
            }
            result.putExtra(RESULT_MULTI_JSON, new Gson().toJson(chosen));
        } else {
            String targetId = selectedIds.iterator().next();
            for (SelectableItem item : masterList) {
                if (targetId.equals(item.id)) {
                    result.putExtra(RESULT_ID, item.id);
                    result.putExtra(RESULT_CODE, item.code);
                    result.putExtra(RESULT_TITLE, item.title);
                    result.putExtra(RESULT_SUBTITLE, item.subtitle);
                    result.putExtra(RESULT_RAW_JSON, item.rawJson);
                    break;
                }
            }
        }
        setResult(RESULT_OK, result);
        finish();
    }

    private class SelectableAdapter extends RecyclerView.Adapter<SelectableAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selectable_record, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SelectableItem item = filteredList.get(position);
            boolean isSelected = selectedIds.contains(item.id);

            holder.tvTitle.setText(item.title);
            if (item.subtitle != null && !item.subtitle.isEmpty()) {
                holder.tvSubtitle.setVisibility(View.VISIBLE);
                holder.tvSubtitle.setText(item.subtitle);
            } else {
                holder.tvSubtitle.setVisibility(View.GONE);
            }

            if (item.badge != null && !item.badge.isEmpty()) {
                holder.tvBadge.setVisibility(View.VISIBLE);
                holder.tvBadge.setText(item.badge);
            } else {
                holder.tvBadge.setVisibility(View.GONE);
            }

            holder.cb.setVisibility(multiSelect ? View.VISIBLE : View.GONE);
            holder.cb.setChecked(isSelected);
            holder.ivCheck.setVisibility(!multiSelect && isSelected ? View.VISIBLE : View.GONE);

            holder.card.setStrokeColor(Color.parseColor(isSelected ? "#3B49DF" : "#E2E8F0"));
            holder.card.setStrokeWidth(isSelected ? 4 : 2);
            holder.card.setCardBackgroundColor(Color.parseColor(isSelected ? "#EEF2FF" : "#FFFFFF"));

            holder.card.setOnClickListener(v -> {
                if (multiSelect) {
                    if (selectedIds.contains(item.id)) {
                        selectedIds.remove(item.id);
                    } else {
                        selectedIds.add(item.id);
                    }
                } else {
                    selectedIds.clear();
                    selectedIds.add(item.id);
                }
                notifyDataSetChanged();
                updateSelectionState();
            });
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final MaterialCardView card;
            final MaterialCheckBox cb;
            final TextView tvTitle, tvSubtitle, tvBadge;
            final ImageView ivCheck;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.cardSelectableRoot);
                cb = itemView.findViewById(R.id.cbSelectable);
                tvTitle = itemView.findViewById(R.id.tvSelectableTitle);
                tvSubtitle = itemView.findViewById(R.id.tvSelectableSubtitle);
                tvBadge = itemView.findViewById(R.id.tvSelectableBadge);
                ivCheck = itemView.findViewById(R.id.ivSelectedCheck);
            }
        }
    }
}
