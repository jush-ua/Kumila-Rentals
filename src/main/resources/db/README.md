# Database Seed Data

This directory contains SQL scripts for initializing the database with sample data.

## How to Use

### Option 1: Automatic Loading (Recommended for Development)

Add this line to `Launcher.java` after `Database.init()`:

```java
Database.init();
Database.loadSeedData();  // Load sample data
```

### Option 2: Manual Loading

If you want to manually load seed data, you can call:
```java
Database.loadSeedData();
```

## Files

- **seed_data.sql** - Sample/test data that can be shared across devices via Git
  - Contains sample cosplays, users, featured items, etc.
  - Safe to commit to version control
  - Uses `INSERT OR IGNORE` to prevent duplicates

## Sharing Data Between Devices

1. **Edit seed_data.sql** - Add your cosplays and other data as INSERT statements
2. **Commit to Git** - Push the SQL file (not the .db file!)
3. **Pull on other device** - Other developers pull the changes
4. **Run loadSeedData()** - The new data will be inserted into their local database

## Adding New Seed Data

Edit `seed_data.sql` and add INSERT statements:

```sql
INSERT OR IGNORE INTO cosplays (name, category, series_name, size, description, image_path, rent_rate_1day, rent_rate_2days, rent_rate_3days) VALUES
('Your Cosplay Name', 'Category', 'Series', 'Size', 'Description', '/images/path.jpg', 500.00, 900.00, 1200.00);
```

The `OR IGNORE` clause prevents errors if the data already exists.

## Important Notes

- The actual database file (`cosplay.db`) should **NOT** be committed to Git
- Already configured in `.gitignore`
- Only SQL scripts should be version controlled
- Each developer has their own local database
- Seed data provides a consistent starting point
