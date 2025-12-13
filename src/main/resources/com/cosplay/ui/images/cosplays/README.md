# Cosplay Images

Store your cosplay images here. These images will be included in the project resources and sync via Git.

## Directory Structure

```
cosplays/
  ├── naruto.jpg
  ├── sailormoon.jpg
  ├── spiderman.jpg
  └── ... (your cosplay images)
```

## How to Use

### Adding Images

1. Place image files in this directory
2. When adding/editing cosplays in the admin panel, use the path format:
   ```
   /com/cosplay/ui/images/cosplays/filename.jpg
   ```

### Supported Formats

- `.jpg` / `.jpeg`
- `.png`
- `.gif`

### Recommended Specifications

- **Width:** 800-1200px
- **Height:** 1000-1500px (portrait orientation works best)
- **File Size:** Under 500KB each (compress if needed)

## Image Path Examples

In the admin panel, enter image paths like this:

```
/com/cosplay/ui/images/cosplays/naruto.jpg
/com/cosplay/ui/images/cosplays/sailor-moon.png
/com/cosplay/ui/images/cosplays/spider-man-suit.jpg
```

## Alternative: Use URLs

You can also use external URLs instead:

```
https://i.imgur.com/abc123.jpg
https://example.com/images/cosplay.png
```

URLs don't need to be in this folder and won't bloat your Git repository.

## Sharing Images

When you:
1. Add images to this folder
2. Update paths in database to use `/com/cosplay/ui/images/cosplays/...`
3. Export data with `DataExporter`
4. Commit and push to Git

Other developers will:
1. Pull the repository (images included!)
2. Run `Database.loadSeedData()`
3. See the same cosplays with working images
