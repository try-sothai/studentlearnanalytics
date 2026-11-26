package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.utils.RoleGuard;

public class TeacherCourseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE = "extra_course";
    public static final String EXTRA_ACTION = "extra_action";
    public static final String ACTION_VIEW_STUDENTS = "students";
    public static final String ACTION_VIEW_ANALYTICS = "analytics";

    private Course course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Teacher")) return;
        setContentView(R.layout.activity_teacher_course_detail);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_COURSE)) {
            Object extra = getIntent().getSerializableExtra(EXTRA_COURSE);
            if (extra instanceof Course) {
                course = (Course) extra;
            }
        }

        if (course == null) {
            Toast.makeText(this, "Course offering details unavailable.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView btnBack = findViewById(R.id.btnBackTeacherCourseDetail);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        TextView tvToolbar = findViewById(R.id.tvTeacherCourseDetailToolbarTitle);
        TextView tvCode = findViewById(R.id.tvTeacherCourseCode);
        TextView tvStudentsPill = findViewById(R.id.tvTeacherCourseStudentsPill);
        TextView tvName = findViewById(R.id.tvTeacherCourseName);
        TextView tvDept = findViewById(R.id.tvTeacherCourseDepartment);
        TextView tvSemester = findViewById(R.id.tvTeacherCourseSemester);
        TextView tvSchedule = findViewById(R.id.tvTeacherCourseSchedule);
        TextView tvRoom = findViewById(R.id.tvTeacherCourseRoom);

        String code = course.getCode().isEmpty() ? "COURSE" : course.getCode();
        String name = course.getName().isEmpty() ? "Untitled Course" : course.getName();

        if (tvToolbar != null) tvToolbar.setText(code + " Details");
        if (tvCode != null) tvCode.setText(code);
        if (tvName != null) tvName.setText(name);
        if (tvStudentsPill != null) tvStudentsPill.setText(course.getStudentCount() + " Students");
        if (tvDept != null) tvDept.setText(course.getDepartment().isEmpty() ? "—" : course.getDepartment());
        if (tvSemester != null) tvSemester.setText(course.getSemester().isEmpty() ? "—" : course.getSemester());
        if (tvSchedule != null) tvSchedule.setText(course.getSchedule().isEmpty() ? "TBA" : course.getSchedule());
        if (tvRoom != null) tvRoom.setText(course.getRoom().isEmpty() ? "TBA" : course.getRoom());

        MaterialButton btnStudents = findViewById(R.id.btnTeacherCourseViewStudents);
        MaterialButton btnAnalytics = findViewById(R.id.btnTeacherCourseAnalytics);

        if (btnStudents != null) {
            btnStudents.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra(EXTRA_ACTION, ACTION_VIEW_STUDENTS);
                result.putExtra(EXTRA_COURSE, course);
                setResult(RESULT_OK, result);
                finish();
            });
        }

        if (btnAnalytics != null) {
            btnAnalytics.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra(EXTRA_ACTION, ACTION_VIEW_ANALYTICS);
                result.putExtra(EXTRA_COURSE, course);
                setResult(RESULT_OK, result);
                finish();
            });
        }
    }
}
