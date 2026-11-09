$xmlPath = "D:/Development/Projects/studentlearnanalytics/app/src/main/res/layout/fragment_admin_dashboard.xml"
$content = Get-Content $xmlPath -Raw -Encoding UTF8

$content = $content -replace 'android:layout_height="230dp"', 'android:layout_height="200dp"'
$content = $content -replace 'android:layout_height="220dp"', 'android:layout_height="200dp"'

Set-Content $xmlPath -Value $content -Encoding UTF8
