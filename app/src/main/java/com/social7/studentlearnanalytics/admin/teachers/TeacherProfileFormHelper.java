package com.social7.studentlearnanalytics.admin.teachers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shared UI helper for Admin Add/Edit Teacher profile fields:
 * - Searchable Office Location selector grouped by building (plus authorized Admin creation)
 * - Multi-select Specialization catalog with removable Material Chips (plus Admin creation)
 * - Optional department synchronization from backend catalog
 */
public class TeacherProfileFormHelper {

    public static class OfficeRoomItem {
        public final Integer id;
        public final String building;
        public final String roomNumber;
        public final String displayName;

        public OfficeRoomItem(@Nullable Integer id, String building, String roomNumber, String displayName) {
            this.id = id;
            this.building = building;
            this.roomNumber = roomNumber;
            this.displayName = displayName;
        }
    }

    private static class GroupedDialogRow {
        final boolean isHeader;
        final String title;
        final OfficeRoomItem room;

        GroupedDialogRow(String buildingTitle) {
            this.isHeader = true;
            this.title = buildingTitle;
            this.room = null;
        }

        GroupedDialogRow(OfficeRoomItem room) {
            this.isHeader = false;
            this.title = room.roomNumber;
            this.room = room;
        }
    }

    private final Context context;
    private final EditText etOfficeSelector;
    private final ChipGroup chipGroupSpecializations;
    private final TextView tvSpecEmptyHint;

    private final List<OfficeRoomItem> officeCatalog = new ArrayList<>();
    private final List<String> specializationCatalog = new ArrayList<>();
    private final List<String> selectedSpecializations = new ArrayList<>();
    private final List<String> departmentCatalog = new ArrayList<>();

    private Integer selectedOfficeId = null;
    private String selectedOfficeLabel = "";

    public TeacherProfileFormHelper(
            @NonNull Context context,
            @Nullable EditText etOfficeSelector,
            @Nullable ChipGroup chipGroupSpecializations,
            @Nullable TextView tvSpecEmptyHint
    ) {
        this.context = context;
        this.etOfficeSelector = etOfficeSelector;
        this.chipGroupSpecializations = chipGroupSpecializations;
        this.tvSpecEmptyHint = tvSpecEmptyHint;
        seedFallbackCatalogs();
    }

    private void seedFallbackCatalogs() {
        officeCatalog.clear();
        String[][] defaults = {
                {"Building A", "Room 101"}, {"Building A", "Room 102"}, {"Building A", "Room 103"},
                {"Building A", "Room 201"}, {"Building A", "Room 202"}, {"Building A", "Room 203"},
                {"Building B", "Room 101"}, {"Building B", "Room 102"}, {"Building B", "Room 201"}, {"Building B", "Room 202"},
                {"Building C", "Room 101"}, {"Building C", "Room 102"},
                {"Faculty Center", "Office 101"}, {"Faculty Center", "Office 102"}
        };
        for (String[] pair : defaults) {
            officeCatalog.add(new OfficeRoomItem(null, pair[0], pair[1], pair[0] + ", " + pair[1]));
        }

        specializationCatalog.clear();
        specializationCatalog.addAll(Arrays.asList(
                "Algorithms", "Artificial Intelligence", "Business Analytics", "Cloud Computing",
                "Cybersecurity", "Data Science", "Data Structures", "Database Systems",
                "Databases", "Machine Learning", "Mobile Development", "Networking",
                "Operating Systems", "Software Engineering", "Web Development"
        ));
    }

    public boolean isAdminUser() {
        String role = new SessionManager(context).getRole();
        return "Admin".equalsIgnoreCase(role);
    }

    public void setInitialValues(@Nullable Integer officeId, @Nullable String officeLabel, @Nullable List<String> specs) {
        this.selectedOfficeId = officeId;
        this.selectedOfficeLabel = officeLabel != null ? officeLabel.trim() : "";
        if (etOfficeSelector != null) {
            etOfficeSelector.setText(this.selectedOfficeLabel);
        }

        selectedSpecializations.clear();
        if (specs != null) {
            for (String s : specs) {
                addSelectedSpecializationInternal(s);
            }
        }
        renderSpecializationChips();
    }

    public Integer getSelectedOfficeId() {
        return selectedOfficeId;
    }

    public String getSelectedOfficeLabel() {
        return selectedOfficeLabel;
    }

    public List<String> getSelectedSpecializations() {
        return new ArrayList<>(selectedSpecializations);
    }

    public JsonArray getSelectedSpecializationsJson() {
        JsonArray arr = new JsonArray();
        for (String s : selectedSpecializations) {
            arr.add(s);
        }
        return arr;
    }

    public void loadLookups(@Nullable Spinner deptSpinner, @Nullable String preselectedDept) {
        ApiClient.getApiService(context).getTeacherLookups(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) {
                    return;
                }
                JsonObject d = response.body().getAsJsonObject("data");

                if (d.has("office_locations") && d.get("office_locations").isJsonArray()) {
                    List<OfficeRoomItem> loadedOffices = new ArrayList<>();
                    for (JsonElement el : d.getAsJsonArray("office_locations")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        Integer id = o.has("id") && !o.get("id").isJsonNull() ? o.get("id").getAsInt() : null;
                        String b = o.has("building") && !o.get("building").isJsonNull() ? o.get("building").getAsString() : "";
                        String r = o.has("room_number") && !o.get("room_number").isJsonNull() ? o.get("room_number").getAsString() : "";
                        String label = o.has("display_name") && !o.get("display_name").isJsonNull() ? o.get("display_name").getAsString() : (b + ", " + r);
                        if (!label.trim().isEmpty()) {
                            loadedOffices.add(new OfficeRoomItem(id, b, r, label));
                        }
                    }
                    if (!loadedOffices.isEmpty()) {
                        officeCatalog.clear();
                        officeCatalog.addAll(loadedOffices);
                    }
                }

                if (d.has("specializations") && d.get("specializations").isJsonArray()) {
                    List<String> loadedSpecs = new ArrayList<>();
                    for (JsonElement el : d.getAsJsonArray("specializations")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject s = el.getAsJsonObject();
                        if (s.has("name") && !s.get("name").isJsonNull()) {
                            String name = s.get("name").getAsString().trim();
                            if (!name.isEmpty()) loadedSpecs.add(name);
                        }
                    }
                    if (!loadedSpecs.isEmpty()) {
                        specializationCatalog.clear();
                        specializationCatalog.addAll(loadedSpecs);
                    }
                }

                if (deptSpinner != null && d.has("departments") && d.get("departments").isJsonArray()) {
                    departmentCatalog.clear();
                    for (JsonElement el : d.getAsJsonArray("departments")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject dep = el.getAsJsonObject();
                        if (dep.has("name") && !dep.get("name").isJsonNull()) {
                            departmentCatalog.add(dep.get("name").getAsString().trim());
                        }
                    }
                    if (!departmentCatalog.isEmpty()) {
                        applyDepartmentsToSpinner(deptSpinner, preselectedDept);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                // Keep offline fallback catalogs cleanly.
            }
        });
    }

    private void applyDepartmentsToSpinner(Spinner spinner, @Nullable String currentValue) {
        List<String> items = new ArrayList<>();
        String first = spinner.getCount() > 0 ? String.valueOf(spinner.getItemAtPosition(0)) : "";
        boolean hasPrompt = first.toLowerCase(Locale.ROOT).startsWith("select");
        if (hasPrompt) {
            items.add(first);
        }
        items.addAll(departmentCatalog);
        if (currentValue != null && !currentValue.trim().isEmpty()) {
            boolean exists = false;
            for (String item : items) {
                if (item.equalsIgnoreCase(currentValue.trim())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                items.add(currentValue.trim());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (currentValue != null && !currentValue.trim().isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).equalsIgnoreCase(currentValue.trim())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    public void showOfficeLocationPickerDialog() {
        int pad = dp(16);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, dp(10), pad, 0);

        EditText searchInput = new EditText(context);
        searchInput.setHint(R.string.search_office_hint);
        searchInput.setSingleLine(true);
        searchInput.setBackgroundResource(R.drawable.bg_spinner_border);
        searchInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        searchInput.setTextSize(14f);
        root.addView(searchInput);

        ListView listView = new ListView(context);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(300));
        listParams.topMargin = dp(8);
        listView.setLayoutParams(listParams);
        listView.setDivider(null);
        root.addView(listView);

        TextView emptyView = new TextView(context);
        emptyView.setText(R.string.no_matching_locations);
        emptyView.setPadding(dp(8), dp(16), dp(8), dp(16));
        emptyView.setTextColor(Color.parseColor("#64748B"));
        emptyView.setVisibility(View.GONE);
        root.addView(emptyView);

        List<GroupedDialogRow> visibleRows = new ArrayList<>();
        ArrayAdapter<GroupedDialogRow> adapter = new ArrayAdapter<GroupedDialogRow>(context, android.R.layout.simple_list_item_1, visibleRows) {
            @Override
            public boolean isEnabled(int position) {
                return position >= 0 && position < visibleRows.size() && !visibleRows.get(position).isHeader;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                GroupedDialogRow row = visibleRows.get(position);
                TextView tv = new TextView(context);
                if (row.isHeader) {
                    tv.setText(row.title);
                    tv.setTextSize(12f);
                    tv.setTypeface(null, Typeface.BOLD);
                    tv.setTextColor(Color.parseColor("#2563FF"));
                    tv.setBackgroundColor(Color.parseColor("#F1F5F9"));
                    tv.setPadding(dp(12), dp(8), dp(12), dp(6));
                } else {
                    boolean isSelected = row.room != null && row.room.displayName.equalsIgnoreCase(selectedOfficeLabel);
                    tv.setText((isSelected ? "✓  " : "•  ") + row.room.roomNumber + "   (" + row.room.building + ")");
                    tv.setTextSize(14f);
                    tv.setTextColor(Color.parseColor(isSelected ? "#2563FF" : "#0F172A"));
                    tv.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
                    tv.setPadding(dp(22), dp(10), dp(12), dp(10));
                }
                return tv;
            }
        };
        listView.setAdapter(adapter);

        Runnable rebuildRows = () -> {
            String term = searchInput.getText() != null ? searchInput.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
            visibleRows.clear();
            Map<String, List<OfficeRoomItem>> grouped = new LinkedHashMap<>();
            for (OfficeRoomItem item : officeCatalog) {
                String searchable = (item.building + " " + item.roomNumber + " " + item.displayName).toLowerCase(Locale.ROOT);
                if (term.isEmpty() || searchable.contains(term)) {
                    grouped.computeIfAbsent(item.building, k -> new ArrayList<>()).add(item);
                }
            }
            for (Map.Entry<String, List<OfficeRoomItem>> e : grouped.entrySet()) {
                visibleRows.add(new GroupedDialogRow(e.getKey()));
                for (OfficeRoomItem room : e.getValue()) {
                    visibleRows.add(new GroupedDialogRow(room));
                }
            }
            emptyView.setVisibility(visibleRows.isEmpty() ? View.VISIBLE : View.GONE);
            listView.setVisibility(visibleRows.isEmpty() ? View.GONE : View.VISIBLE);
            adapter.notifyDataSetChanged();
        };
        rebuildRows.run();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { rebuildRows.run(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.select_office_location)
                .setView(root)
                .setNegativeButton(R.string.close, null)
                .setNeutralButton(R.string.clear_optional, (d, w) -> {
                    selectedOfficeId = null;
                    selectedOfficeLabel = "";
                    if (etOfficeSelector != null) etOfficeSelector.setText("");
                });

        if (isAdminUser()) {
            builder.setPositiveButton(R.string.add_new_location, null);
        }

        AlertDialog dialog = builder.create();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            GroupedDialogRow row = visibleRows.get(position);
            if (!row.isHeader && row.room != null) {
                selectedOfficeId = row.room.id;
                selectedOfficeLabel = row.room.displayName;
                if (etOfficeSelector != null) {
                    etOfficeSelector.setText(selectedOfficeLabel);
                }
                dialog.dismiss();
            }
        });

        dialog.setOnShowListener(d -> {
            if (isAdminUser()) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> showAddOfficeLocationDialog(dialog));
            }
        });
        dialog.show();
    }

    private void showAddOfficeLocationDialog(AlertDialog parentDialog) {
        int pad = dp(16);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(pad, dp(8), pad, 0);

        EditText etBuilding = new EditText(context);
        etBuilding.setHint("Building Name (e.g. Building A)");
        etBuilding.setSingleLine(true);
        layout.addView(etBuilding);

        EditText etRoom = new EditText(context);
        etRoom.setHint("Room Number (e.g. Room 305)");
        etRoom.setSingleLine(true);
        layout.addView(etRoom);

        AlertDialog addDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.add_new_location)
                .setView(layout)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        addDialog.setOnShowListener(d -> addDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String building = etBuilding.getText() != null ? etBuilding.getText().toString().trim() : "";
            String room = etRoom.getText() != null ? etRoom.getText().toString().trim() : "";
            if (building.isEmpty()) {
                etBuilding.setError("Building name is required");
                return;
            }
            if (room.isEmpty()) {
                etRoom.setError("Room number is required");
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("building", building);
            body.addProperty("room_number", room);
            addDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            ApiClient.getApiService(context).createOfficeLocation(body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    addDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                        JsonObject loc = response.body().getAsJsonObject("data");
                        Integer id = loc.has("id") && !loc.get("id").isJsonNull() ? loc.get("id").getAsInt() : null;
                        String b = loc.has("building") ? loc.get("building").getAsString() : building;
                        String r = loc.has("room_number") ? loc.get("room_number").getAsString() : room;
                        String label = loc.has("display_name") ? loc.get("display_name").getAsString() : (b + ", " + r);
                        upsertOfficeCatalogItem(new OfficeRoomItem(id, b, r, label));
                        selectedOfficeId = id;
                        selectedOfficeLabel = label;
                        if (etOfficeSelector != null) etOfficeSelector.setText(selectedOfficeLabel);
                        addDialog.dismiss();
                        parentDialog.dismiss();
                        Toast.makeText(context, "Office location added: " + label, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to save location (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    addDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Toast.makeText(context, "Network error saving office location", Toast.LENGTH_SHORT).show();
                }
            });
        }));
        addDialog.show();
    }

    private void upsertOfficeCatalogItem(OfficeRoomItem newItem) {
        for (int i = 0; i < officeCatalog.size(); i++) {
            if (officeCatalog.get(i).displayName.equalsIgnoreCase(newItem.displayName)) {
                officeCatalog.set(i, newItem);
                return;
            }
        }
        officeCatalog.add(newItem);
    }

    public void showSpecializationPickerDialog() {
        int pad = dp(16);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, dp(10), pad, 0);

        EditText searchInput = new EditText(context);
        searchInput.setHint(R.string.search_specialization_hint);
        searchInput.setSingleLine(true);
        searchInput.setBackgroundResource(R.drawable.bg_spinner_border);
        searchInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        searchInput.setTextSize(14f);
        root.addView(searchInput);

        ListView listView = new ListView(context);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(280));
        listParams.topMargin = dp(8);
        listView.setLayoutParams(listParams);
        root.addView(listView);

        List<String> filtered = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice, filtered) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                String name = filtered.get(position);
                listView.setItemChecked(position, containsIgnoreCase(selectedSpecializations, name));
                return view;
            }
        };
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);

        Runnable rebuild = () -> {
            String term = searchInput.getText() != null ? searchInput.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
            filtered.clear();
            for (String item : specializationCatalog) {
                if (term.isEmpty() || item.toLowerCase(Locale.ROOT).contains(term)) {
                    filtered.add(item);
                }
            }
            for (String sel : selectedSpecializations) {
                if (!containsIgnoreCase(filtered, sel) && (term.isEmpty() || sel.toLowerCase(Locale.ROOT).contains(term))) {
                    filtered.add(sel);
                }
            }
            adapter.notifyDataSetChanged();
            for (int i = 0; i < filtered.size(); i++) {
                listView.setItemChecked(i, containsIgnoreCase(selectedSpecializations, filtered.get(i)));
            }
        };
        rebuild.run();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String name = filtered.get(position);
            if (containsIgnoreCase(selectedSpecializations, name)) {
                removeSelectedSpecializationInternal(name);
            } else {
                addSelectedSpecializationInternal(name);
            }
            renderSpecializationChips();
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { rebuild.run(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.select_specializations)
                .setView(root)
                .setPositiveButton(R.string.done, null)
                .setNegativeButton(R.string.clear_optional, (d, w) -> {
                    selectedSpecializations.clear();
                    renderSpecializationChips();
                });

        if (isAdminUser()) {
            builder.setNeutralButton(R.string.add_new_specialization, null);
        }

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            if (isAdminUser()) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> showAddSpecializationDialog(rebuild));
            }
        });
        dialog.show();
    }

    private void showAddSpecializationDialog(Runnable onAdded) {
        int pad = dp(16);
        EditText input = new EditText(context);
        input.setHint("e.g. Cloud Security / សន្តិសុខព័ត៌មាន");
        input.setSingleLine(true);

        LinearLayout wrap = new LinearLayout(context);
        wrap.setPadding(pad, dp(8), pad, 0);
        wrap.addView(input, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        AlertDialog addDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.add_new_specialization)
                .setView(wrap)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        addDialog.setOnShowListener(d -> addDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String rawName = input.getText() != null ? input.getText().toString().trim() : "";
            if (rawName.isEmpty()) {
                input.setError("Specialization name is required");
                return;
            }
            JsonObject body = new JsonObject();
            body.addProperty("name", rawName);
            addDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            ApiClient.getApiService(context).createSpecialization(body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    addDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    String savedName = rawName;
                    if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                        JsonObject spec = response.body().getAsJsonObject("data");
                        if (spec.has("name") && !spec.get("name").isJsonNull()) {
                            savedName = spec.get("name").getAsString().trim();
                        }
                    }
                    if (!containsIgnoreCase(specializationCatalog, savedName)) {
                        specializationCatalog.add(savedName);
                    }
                    addSelectedSpecializationInternal(savedName);
                    renderSpecializationChips();
                    onAdded.run();
                    addDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    addDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Toast.makeText(context, "Network error saving specialization", Toast.LENGTH_SHORT).show();
                }
            });
        }));
        addDialog.show();
    }

    public void renderSpecializationChips() {
        if (chipGroupSpecializations == null) return;
        chipGroupSpecializations.removeAllViews();

        boolean empty = selectedSpecializations.isEmpty();
        if (tvSpecEmptyHint != null) {
            tvSpecEmptyHint.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        chipGroupSpecializations.setVisibility(empty ? View.GONE : View.VISIBLE);

        for (String name : selectedSpecializations) {
            Chip chip = new Chip(context);
            chip.setText(name);
            chip.setCloseIconVisible(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#EEF2FF")));
            chip.setTextColor(Color.parseColor("#1D4ED8"));
            chip.setCloseIconTint(ColorStateList.valueOf(Color.parseColor("#1D4ED8")));
            chip.setChipStrokeWidth(1f);
            chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#C7D2FE")));
            chip.setOnCloseIconClickListener(v -> {
                removeSelectedSpecializationInternal(name);
                renderSpecializationChips();
            });
            chipGroupSpecializations.addView(chip);
        }
    }

    private void addSelectedSpecializationInternal(@Nullable String value) {
        if (value == null) return;
        String clean = value.trim();
        if (clean.isEmpty() || containsIgnoreCase(selectedSpecializations, clean)) return;
        selectedSpecializations.add(clean);
    }

    private void removeSelectedSpecializationInternal(String value) {
        selectedSpecializations.removeIf(item -> item.equalsIgnoreCase(value));
    }

    private boolean containsIgnoreCase(List<String> list, String target) {
        for (String s : list) {
            if (s.equalsIgnoreCase(target)) return true;
        }
        return false;
    }

    private int dp(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
