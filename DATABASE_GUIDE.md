# Database Import/Export Guide

This guide explains how to backup, export, and import your database when working across multiple devices.

## 📤 Exporting the Database

### Method 1: Quick Export (Recommended)
Run the export script:
```powershell
.\export_db.ps1
```

This creates a timestamped backup file like `cosplay_backup_20251213_031126.db`

### Method 2: Manual Copy
Simply copy the database file:
```powershell
Copy-Item cosplay.db cosplay_backup.db
```

### Method 3: Export to SQL (For Git)
Export to SQL format that can be committed to your repository:
```powershell
mvn compile exec:java -Dexec.mainClass="com.cosplay.util.DataExporter"
```

This creates/updates: `src/main/resources/db/seed_data.sql`

---

## 📥 Importing the Database

### When You Pull from Git (New Device)

**Option A: Interactive Script (Easiest)**
```powershell
.\import_db.ps1
```

Follow the prompts to choose:
1. Import from `seed_data.sql` (from Git)
2. Import from a `.db` backup file

**Option B: Quick SQL Import**
```powershell
mvn compile exec:java -Dexec.mainClass="com.cosplay.util.DataImporter"
```

This automatically:
- Creates the database structure
- Loads data from `src/main/resources/db/seed_data.sql`

**Option C: Direct File Copy**
If you have a `.db` backup file:
```powershell
Copy-Item cosplay_backup.db cosplay.db
```

---

## 🔄 Typical Workflow

### On Device A (your current device):
1. Make changes to your database
2. Export it:
   ```powershell
   .\export_db.ps1
   ```
3. Commit the `seed_data.sql` file (optional, if using SQL export):
   ```bash
   git add src/main/resources/db/seed_data.sql
   git commit -m "Update database with new items"
   git push
   ```

### On Device B (another device):
1. Pull the repository:
   ```bash
   git pull
   ```
2. Import the database:
   ```powershell
   .\import_db.ps1
   ```
3. Choose option 1 (import from seed_data.sql)

---

## 📋 Important Notes

- **File Format**: The database is a single SQLite file (`cosplay.db`)
- **Portability**: `.db` files work across all platforms (Windows, Mac, Linux)
- **Size**: Database files are usually small (under 50 MB)
- **Security**: Don't commit `.db` files to Git if they contain sensitive user data
- **SQL Files**: Safe to commit to Git - only contains catalog/items, not user accounts

---

## 🆘 Troubleshooting

**"Database file not found"**
- Make sure you're in the project root directory
- Run the import script to create a new database

**"Import failed"**
- Ensure Maven is installed: `mvn -version`
- Try compiling first: `mvn clean compile`

**"Backup file too large"**
- Use SQL export instead of file copy
- Compress the `.db` file before transferring

**Starting Fresh**
- Delete `cosplay.db`
- Run the app - it will create a new empty database
- Or run `.\import_db.ps1` to load seed data
