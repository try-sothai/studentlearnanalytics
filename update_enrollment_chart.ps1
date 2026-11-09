$javaPath = "D:/Development/Projects/studentlearnanalytics/app/src/main/java/com/social7/studentlearnanalytics/admin/AdminDashboardFragment.java"
$content = Get-Content $javaPath -Raw -Encoding UTF8

$oldBlock = @"
        LineDataSet set1 = new LineDataSet(totalStudentsList, "Total Students");
        set1.setColor(Color.parseColor("#10B981"));
        set1.setCircleColor(Color.parseColor("#10B981"));
        set1.setLineWidth(2f);
        set1.setDrawValues(false);

        LineDataSet set2 = new LineDataSet(activeLearnersList, "Active Learners");
        set2.setColor(Color.parseColor("#0EA5E9"));
        set2.setCircleColor(Color.parseColor("#0EA5E9"));
        set2.setLineWidth(2f);
        set2.setDrawValues(false);

        LineDataSet set3 = new LineDataSet(atRiskList, "At-Risk Students");
        set3.setColor(Color.parseColor("#EF4444"));
        set3.setCircleColor(Color.parseColor("#EF4444"));
        set3.setLineWidth(2f);
        set3.setDrawValues(false);

        LineData lineData = new LineData(set1, set2, set3);
        growthChart.setData(lineData);

        XAxis xAxis = growthChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        growthChart.getDescription().setEnabled(false);
        growthChart.getLegend().setEnabled(false); // Using custom legend
        growthChart.animateX(500);
        growthChart.invalidate();
"@

$newBlock = @"
        Log.d(TAG, "Enrollment Analytics API JSON: " + data.toString());
        Log.d(TAG, "Total Students values: " + totalStudentsList.toString());
        Log.d(TAG, "Active Learners values: " + activeLearnersList.toString());
        Log.d(TAG, "At-Risk Students values: " + atRiskList.toString());

        LineDataSet set1 = new LineDataSet(totalStudentsList, "Total Students");
        set1.setColor(Color.parseColor("#10B981"));
        set1.setCircleColor(Color.parseColor("#10B981"));
        set1.setLineWidth(2.5f);
        set1.setCircleRadius(4f);
        set1.setDrawCircles(true);
        set1.setDrawCircleHole(true);
        set1.setDrawValues(false);
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet set2 = new LineDataSet(activeLearnersList, "Active Learners");
        set2.setColor(Color.parseColor("#0EA5E9"));
        set2.setCircleColor(Color.parseColor("#0EA5E9"));
        set2.setLineWidth(2.5f);
        set2.setCircleRadius(4f);
        set2.setDrawCircles(true);
        set2.setDrawCircleHole(true);
        set2.setDrawValues(false);
        set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet set3 = new LineDataSet(atRiskList, "At-Risk Students");
        set3.setColor(Color.parseColor("#EF4444"));
        set3.setCircleColor(Color.parseColor("#EF4444"));
        set3.setLineWidth(2.5f);
        set3.setCircleRadius(4f);
        set3.setDrawCircles(true);
        set3.setDrawCircleHole(true);
        set3.setDrawValues(false);
        set3.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(set1, set2, set3);
        growthChart.setData(lineData);

        Log.d(TAG, "Number of entries - Total: " + set1.getEntryCount() + ", Active: " + set2.getEntryCount() + ", Risk: " + set3.getEntryCount());
        Log.d(TAG, "Colors assigned - Set1: #10B981, Set2: #0EA5E9, Set3: #EF4444");
        Log.d(TAG, "Final chart dataset count: " + lineData.getDataSetCount());

        XAxis xAxis = growthChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-20f);
        xAxis.setTextColor(Color.parseColor("#64748B"));

        growthChart.getAxisLeft().setAxisMinimum(0f);
        growthChart.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));
        growthChart.getAxisLeft().setTextColor(Color.parseColor("#64748B"));
        growthChart.getAxisRight().setEnabled(false);

        growthChart.getDescription().setEnabled(false);
        growthChart.getLegend().setEnabled(false);
        growthChart.animateX(600);
        growthChart.invalidate();
"@

$content = $content.Replace($oldBlock, $newBlock)
Set-Content $javaPath -Value $content -Encoding UTF8
