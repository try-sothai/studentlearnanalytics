package com.social7.studentlearnanalytics.teacher.assessment;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.social7.studentlearnanalytics.R;

public class AssessmentListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_detail);
        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }
}
