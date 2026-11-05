package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class StudentImportItem implements Serializable {
    @SerializedName("id")
    public int id;

    @SerializedName("original_filename")
    public String originalFilename;

    @SerializedName("status")
    public String status;

    @SerializedName("imported_by")
    public String importedBy;

    @SerializedName("total_rows")
    public int totalRows;

    @SerializedName("valid_rows")
    public int validRows;

    @SerializedName("invalid_rows")
    public int invalidRows;

    @SerializedName("duplicate_rows")
    public int duplicateRows;

    @SerializedName("processed_rows")
    public int processedRows;

    @SerializedName("success_rows")
    public int successRows;

    @SerializedName("failed_rows")
    public int failedRows;

    @SerializedName("skipped_rows")
    public int skippedRows;

    @SerializedName("progress_percent")
    public int progressPercent;

    @SerializedName("duration_seconds")
    public Integer durationSeconds;

    @SerializedName("failure_message")
    public String failureMessage;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("preview_rows")
    public List<RowItem> previewRows;

    public static class RowItem implements Serializable {
        @SerializedName("row_number")
        public int rowNumber;

        @SerializedName("student_identifier")
        public String studentIdentifier;

        @SerializedName("english_name")
        public String englishName;

        @SerializedName("khmer_name")
        public String khmerName;

        @SerializedName("department_code")
        public String departmentCode;

        @SerializedName("program_code")
        public String programCode;

        @SerializedName("status")
        public String status;

        @SerializedName("errors")
        public List<String> errors;
    }
}
