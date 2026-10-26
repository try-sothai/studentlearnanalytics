# 🎓 SLAS Student Prototype Builder — Figma Plugin

This is a local Figma Plugin created specifically for the **LearnAnalytics — Student Learning Analytics System (SLAS)** project.

It automatically inspects your Figma document, identifies existing Student frames and components, shows an interactive preview of proposed source-to-destination prototype connections, and wires the entire Student prototype flow with a single click.

---

## 🔒 Security & Privacy Guarantees
- **100% Local**: Runs entirely inside Figma's sandboxed environment.
- **No Token Required**: Operates using Figma's native Plugin API session.
- **Zero External Requests**: No design data or network requests leave Figma.
- **Safe Execution**: Preserves Admin/Teacher screens, shows a preview, and only applies connections after your explicit confirmation.

---

## 📁 Plugin Project Files

This plugin directory contains:
- `manifest.json`: Figma Plugin Manifest configuration.
- `code.js`: Main plugin code (inspects frames, searches elements, configures reactions via `setReactionsAsync`, and configures `flowStartingPoints`).
- `ui.html`: Interactive preview interface with connection list, missing screen warnings, confirmation button, and log console.

---

## 🚀 How to Run the Plugin in Figma (3 Easy Steps)

### Step 1: Import the Local Plugin into Figma
1. Open Figma (Desktop App or Web Browser) and open your design file:
   `https://www.figma.com/design/iGHJ3LpjTXZDcncJgfxafN/UI`
2. Click the **Figma Menu** (top-left logo icon) $\rightarrow$ **Plugins** $\rightarrow$ **Development** $\rightarrow$ **Import plugin from manifest...**.
3. Select the `manifest.json` file located in this folder:
   `D:\Development\Projects\studentlearnanalytics\figma_plugin_student_prototype\manifest.json`

### Step 2: Run the Plugin
1. In Figma, right-click anywhere on the canvas or open the menu:
   **Plugins** $\rightarrow$ **Development** $\rightarrow$ **SLAS Student Prototype Builder**.
2. The plugin window will open and automatically scan your document for Student screens.

### Step 3: Review Preview & Apply Connections
1. The plugin will display:
   - **Matched Student Frames** (Login, Dashboard, Courses, Analytics, Performance Details, Prediction & Risk, GPA, Attendance, Notifications, Profile, Drawer, etc.).
   - **Missing Destinations** (if any key screens are missing, highlighted in yellow/red).
   - **Proposed Prototype Connections** (Bottom navigation items, Quick Action cards, Analytics links, Drawer links).
2. Uncheck any connections you do not wish to create.
3. Click **⚡ Confirm & Apply Prototype Connections**.
4. The plugin will wire all selected connections asynchronously and set the flow starting point **"LearnAnalytics — Student Prototype"** at your Login or Dashboard screen!

---

## ⚙️ Wired Prototype Navigation Map

| Source Component | Destination Screen | Trigger / Action |
| :--- | :--- | :--- |
| **Login Button** | Student Dashboard | On Click $\rightarrow$ Navigate |
| **Bottom Nav: Home** | Student Dashboard | On Click $\rightarrow$ Navigate |
| **Bottom Nav: Courses** | My Courses | On Click $\rightarrow$ Navigate |
| **Bottom Nav: Analytics** | Learning Analytics Overview | On Click $\rightarrow$ Navigate |
| **Bottom Nav: Notifications** | Notifications | On Click $\rightarrow$ Navigate |
| **Bottom Nav: Profile** | Profile | On Click $\rightarrow$ Navigate |
| **Overall Performance Card** | Performance Details | On Click $\rightarrow$ Navigate |
| **Predicted Exam Score Card** | Prediction & Risk | On Click $\rightarrow$ Navigate |
| **Attendance Action** | Attendance Details | On Click $\rightarrow$ Navigate |
| **Assignments Action** | Assignments | On Click $\rightarrow$ Navigate |
| **Quizzes Action** | Quizzes | On Click $\rightarrow$ Navigate |
| **Exams Action** | Exams | On Click $\rightarrow$ Navigate |
| **GPA Card** | GPA / Academic Progress | On Click $\rightarrow$ Navigate |
| **Recommendations Card** | Recommendations | On Click $\rightarrow$ Navigate |
| **Analytics $\rightarrow$ Prediction Link** | Prediction & Risk | On Click $\rightarrow$ Navigate |
| **Analytics $\rightarrow$ Performance Link** | Performance Details | On Click $\rightarrow$ Navigate |
| **Navigation Drawer Items** | Corresponding Student Screens | On Click $\rightarrow$ Navigate |

---

## 🛠️ API & Technical Notes
- **Figma API Method**: Uses `setReactionsAsync()` / `reactions` to attach `ON_CLICK` triggers to `NODE` actions.
- **Prototype Flow**: Uses `figma.currentPage.flowStartingPoints` to register `"LearnAnalytics — Student Prototype"`.
- **Admin/Teacher Safety**: Strictly skips frames matching `/admin/i` or `/teacher/i`.
