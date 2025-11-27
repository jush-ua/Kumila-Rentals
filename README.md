# Kumila Rentals – Desktop app + backend

This repository contains:
- A simple desktop app (JavaFX) for previewing the UI and basic flows
- The backend data model and a lightweight SQLite persistence layer

The desktop UI is intentionally lightweight so a frontend developer can own the look and feel (FXML + CSS) while using the existing navigation, controllers, and DAOs.

## Setup

### Google OAuth Configuration (Required for Google Login)
1. Create OAuth credentials at [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Set environment variables:
   ```bash
   # Windows PowerShell
   $env:GOOGLE_CLIENT_ID="your-client-id.apps.googleusercontent.com"
   $env:GOOGLE_CLIENT_SECRET="your-client-secret"
   
   # Windows CMD
   set GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
   set GOOGLE_CLIENT_SECRET=your-client-secret
   
   # Linux/Mac
   export GOOGLE_CLIENT_ID="your-client-id.apps.googleusercontent.com"
   export GOOGLE_CLIENT_SECRET="your-client-secret"
   ```
3. Or add them to your IDE's run configuration environment variables

See `.env.example` for reference.

## What's in here
- Backend core (safe to build on):
  - `src/main/java/com/cosplay/model` – POJOs for `User`, `Costume`, `Rental`
  - `src/main/java/com/cosplay/dao` – Data access using SQLite
  - `src/main/java/com/cosplay/util/Database.java` – DB connection and table creation
  - `src/main/java/com/cosplay/AppMain.java` – Console runner for quick DB testing (note: not wired to authentication/session)
- Desktop prototype (optional):
  - `src/main/java/com/cosplay/Launcher.java` – JavaFX entrypoint
  - `src/main/resources/com/cosplay/ui/views/LoginView.fxml` – Login screen FXML
  - `src/main/java/com/cosplay/ui/controllers` – JavaFX controllers

Notes:
- Canonical FXML lives under `src/main/resources/...`. Java sources under `src/main/java` should not contain FXML files.
- The desktop entrypoint is `com.cosplay.Launcher`.

## For frontend developers (JavaFX UI ownership)

You own the UI markup (FXML) and styling (CSS). The app already has routing, a reusable navbar, basic auth, and admin tooling for “featured” items.

- Where to edit UI:
  - FXML views: `src/main/resources/com/cosplay/ui/views/`
    - Home, Catalog, Terms, About, Login, Register, Admin
  - Reusable components: `src/main/resources/com/cosplay/ui/views/components/` (see `NavBar.fxml`)
  - Styles: `src/main/resources/com/cosplay/ui/styles/app.css` (applied globally)
  - Controllers (Java): `src/main/java/com/cosplay/ui/controllers/`
- Navigation:
  - Use `Views` enum (`com.cosplay.ui.Views`) to add screens.
  - Switch screens via `SceneNavigator.navigate(Views.X)`.
  - Global CSS is applied in `SceneNavigator`.
- Reusable NavBar component:
  - Included in each screen via `<fx:include ... NavBar.fxml />`.
  - Nav highlights the active screen and shows an “Admin” link only for admin users.
  - A “Logout” button clears the session and returns to the Login screen.
- Admin account & permissions:
  - A default admin is seeded on first run: username `admin`, password `admin`.
  - Only admin users may add costumes (enforced in `CostumeDAO.addCostume`).
  - Admins manage the 4 “Featured” slots from the Admin screen by selecting existing costumes.
- Featured images model:
  - Featured slots now reference a `costume_id` and an optional title override.
  - The Home screen shows the costume’s image and either the override title or the costume name.
- Images & paths:
  - Costume images are read from `Costume.imagePath`.
  - Supported forms:
    - Web URL: `https://...`
    - File URI: `file:///C:/path/to/image.jpg` (Windows) or `file:///Users/me/image.jpg` (macOS)
  - Tip (Windows): a local path like `C:\images\pic.jpg` should be converted to a File URI `file:///C:/images/pic.jpg` for JavaFX Image.

### Typical workflow (UI)
1) Run the app and log in as admin (`admin` / `admin`).
2) Add some costumes (admin-only – see “Add costumes” below).
3) Go to Admin and select which costumes fill the 4 featured slots; optionally set titles.
4) Tweak home/other screens’ FXML and `app.css` to match your design.
5) Add new screens as needed by creating an FXML + controller and registering in `Views`.

### Add costumes (admin-only)
Right now there is no visual form in the Admin screen to add costumes (only to select featured). You have two options:

- Quick option (DB insert): use a SQLite browser (e.g., DB Browser for SQLite) to insert into `costumes`:
  - Columns: `name`, `category`, `size`, `description`, `image_path`
  - After adding, the costume becomes selectable in the Admin screen’s featured slots.
- Or ask to add an “Add Costume” section to the Admin UI and we’ll wire a form that writes to `CostumeDAO.addCostume` (admin-protected).

### Add a new screen
1) Create `YourView.fxml` under `resources/com/cosplay/ui/views/`, with `fx:controller="com.cosplay.ui.controllers.YourController"`.
2) Create `YourController.java` under `ui/controllers/`.
3) Add a constant in `com.cosplay.ui.Views`.
4) From any controller, navigate with `SceneNavigator.navigate(Views.YOUR_VIEW)`.
5) If you need the top nav, include it with `<fx:include source="/com/cosplay/ui/views/components/NavBar.fxml" fx:id="navBar" />` and inject the included controller as `@FXML private NavController navBarController;` then call `navBarController.setActive(Views.YOUR_VIEW);` in `initialize()`.

### Styling tips
- Global styles live in `app.css`. Add classes and reference them from FXML via `styleClass`.
- The nav uses `.nav-button` and `.nav-button.active` for the active tab.
- Prefer CSS classes over inline styles to keep FXML clean.

### Auth & session
- Login stores the current user in `com.cosplay.util.Session`.
- NavBar shows Admin/Logout only when logged in (Admin only visible to admins).
- Logout clears the session and navigates to `Views.LOGIN`.

## Run options
- Console backend (quick DB exercise):
  - In your IDE, run `com.cosplay.AppMain`. Note: not linked to session; admin checks in DAOs may block some actions here.
- Desktop demo UI (JavaFX):
  - Requires JDK 21.
  - Maven: `mvn -DskipTests=true javafx:run`

## Desktop UI development guide (JavaFX)
- App entrypoint: `com.cosplay.Launcher`
- Navigator: `com.cosplay.ui.SceneNavigator` + `com.cosplay.ui.Views`
- See “For frontend developers” section for a practical workflow

## Build
- JDK 21 is required (enforced by Maven Enforcer).
- Clean build: `mvn -DskipTests=true clean package`

## Housekeeping
- Build outputs are ignored via `.gitignore`.
- SQLite DB file `cosplay.db` is also ignored so you can reset freely.
