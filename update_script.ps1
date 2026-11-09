$lines = Get-Content "D:/Development/Projects/studentlearnanalytics/app/src/main/java/com/social7/studentlearnanalytics/admin/AdminDashboardFragment.java" -Encoding UTF8
$scratch = Get-Content "scratch.txt" -Encoding UTF8
$out = @()

for ($i = 0; $i -lt $lines.Length; $i++) {
    $line = $lines[$i]

    if ($i -ge 392 -and $i -le 405) {
        if ($line -match "bindAnnualAnalytics") {
            $out += "        fetchAnalyticsData();"
            continue
        }
        if ($line -match "annual_analytics" -or $line -match "\} else \{" -or $line -match "fetchOverviewAndGrowthFallback\(\);") {
            continue
        }
    }

    if ($i -ge 406 -and $i -le 576) {
        continue
    }

    if ($i -eq 577) {
        $out += $scratch
    }

    $out += $line
}

$out | Set-Content "D:/Development/Projects/studentlearnanalytics/app/src/main/java/com/social7/studentlearnanalytics/admin/AdminDashboardFragment.java" -Encoding UTF8
Write-Output "Done"
