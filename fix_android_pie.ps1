$xmlPath = "D:/Development/Projects/studentlearnanalytics/app/src/main/java/com/social7/studentlearnanalytics/admin/AdminDashboardFragment.java"
$content = Get-Content $xmlPath -Raw -Encoding UTF8

$content = $content -replace '"Low Risk"', '"Low"'
$content = $content -replace '"Medium Risk"', '"Medium"'
$content = $content -replace '"High Risk"', '"High"'

Set-Content $xmlPath -Value $content -Encoding UTF8