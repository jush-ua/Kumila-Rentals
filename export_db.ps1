# Database Export Script
Write-Host "=== Database Export Options ===" -ForegroundColor Cyan

# Option 1: File Copy
if (Test-Path "cosplay.db") {
    $timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
    $backupFile = "cosplay_backup_$timestamp.db"
    Copy-Item "cosplay.db" $backupFile
    Write-Host "✓ Database copied to: $backupFile" -ForegroundColor Green
    Write-Host "  File size: $([math]::Round((Get-Item $backupFile).Length / 1MB, 2)) MB" -ForegroundColor Gray
} else {
    Write-Host "✗ cosplay.db not found!" -ForegroundColor Red
    exit 1
}

# Option 2: Export to SQL (using sqlite3 if available)
$sqliteCmd = Get-Command sqlite3 -ErrorAction SilentlyContinue
if ($sqliteCmd) {
    Write-Host "`n✓ SQLite command-line tool found" -ForegroundColor Green
    $sqlExport = "cosplay_export_$timestamp.sql"
    sqlite3 cosplay.db ".dump" | Out-File -Encoding UTF8 $sqlExport
    Write-Host "✓ SQL export created: $sqlExport" -ForegroundColor Green
    Write-Host "  File size: $([math]::Round((Get-Item $sqlExport).Length / 1MB, 2)) MB" -ForegroundColor Gray
} else {
    Write-Host "`nℹ SQLite CLI not found (optional)" -ForegroundColor Yellow
    Write-Host "  To install: winget install SQLite.SQLite" -ForegroundColor Gray
}

Write-Host "`n=== Export Complete ===" -ForegroundColor Cyan
Write-Host "Your database has been exported!" -ForegroundColor Green
