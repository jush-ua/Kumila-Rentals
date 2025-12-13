# Database Import Script
Write-Host "=== Database Import ===" -ForegroundColor Cyan

Write-Host "`nChoose import method:" -ForegroundColor Yellow
Write-Host "1. Import from seed_data.sql (from Git repository)" -ForegroundColor White
Write-Host "2. Import from backup file (.db file)" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Enter choice (1 or 2)"

if ($choice -eq "1") {
    # Import from SQL file
    Write-Host "`nImporting from seed_data.sql..." -ForegroundColor Cyan
    
    if (-not (Test-Path "src\main\resources\db\seed_data.sql")) {
        Write-Host "✗ seed_data.sql not found!" -ForegroundColor Red
        Write-Host "  Expected location: src\main\resources\db\seed_data.sql" -ForegroundColor Gray
        exit 1
    }
    
    Write-Host "Running DataImporter..." -ForegroundColor Gray
    mvn compile exec:java "-Dexec.mainClass=com.cosplay.util.DataImporter" "-Djava.awt.headless=true"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✓ Database imported successfully!" -ForegroundColor Green
    } else {
        Write-Host "`n✗ Import failed" -ForegroundColor Red
    }
    
} elseif ($choice -eq "2") {
    # Import from backup file
    Write-Host "`nAvailable backup files:" -ForegroundColor Cyan
    $backups = Get-ChildItem -Filter "*.db" | Where-Object { $_.Name -like "*backup*" -or $_.Name -eq "cosplay.db" }
    
    if ($backups.Count -eq 0) {
        Write-Host "✗ No backup files found!" -ForegroundColor Red
        Write-Host "  Place your backup .db file in this directory" -ForegroundColor Gray
        exit 1
    }
    
    $i = 1
    foreach ($backup in $backups) {
        Write-Host "$i. $($backup.Name) - $([math]::Round($backup.Length / 1MB, 2)) MB" -ForegroundColor White
        $i++
    }
    
    $fileChoice = Read-Host "`nEnter file number"
    $selectedFile = $backups[$fileChoice - 1]
    
    if ($selectedFile) {
        # Backup existing database if it exists
        if (Test-Path "cosplay.db") {
            $timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
            Copy-Item "cosplay.db" "cosplay_old_$timestamp.db"
            Write-Host "✓ Existing database backed up to: cosplay_old_$timestamp.db" -ForegroundColor Yellow
        }
        
        # Copy the selected backup
        Copy-Item $selectedFile.FullName "cosplay.db" -Force
        Write-Host "✓ Database imported from: $($selectedFile.Name)" -ForegroundColor Green
        Write-Host "  File size: $([math]::Round((Get-Item "cosplay.db").Length / 1MB, 2)) MB" -ForegroundColor Gray
    } else {
        Write-Host "✗ Invalid selection" -ForegroundColor Red
        exit 1
    }
    
} else {
    Write-Host "✗ Invalid choice" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== Import Complete ===" -ForegroundColor Cyan
