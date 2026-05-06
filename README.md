# EduSmart - JavaFX Educational Platform

A comprehensive JavaFX project template for the **EduSmart** educational platform, designed for team collaboration.

## 🎨 Design Theme
- **Blue**: `#1E3A8A` (Primary brand color)
- **Purple**: `#7C3AED` (Accent color)
- **White / Light Gray**: Background and cards

---

## 📦 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── edusmart/
│   │           ├── Main.java                          ← Application entry point
│   │           ├── controller/
│   │           │   ├── auth/
│   │           │   │   ├── LoginController.java
│   │           │   │   └── SignUpController.java
│   │           │   ├── student/
│   │           │   │   ├── CoursesController.java
│   │           │   │   ├── ExamsController.java
│   │           │   │   ├── BulletinController.java
│   │           │   │   ├── CertificationController.java
│   │           │   │   └── ShopController.java
│   │           │   └── teacher/
│   │           │       ├── TeacherDashboardController.java
│   │           │       ├── ManageCoursesController.java
│   │           │       ├── ManageModulesController.java
│   │           │       ├── ManageExamsController.java
│   │           │       ├── ShopManagementController.java
│   │           │       ├── BulletinsController.java
│   │           │       ├── CertificationsController.java
│   │           │       ├── AnalysisAIController.java
│   │           │       └── StudentManagementController.java
│   │           ├── model/
│   │           │   ├── User.java
│   │           │   ├── Course.java
│   │           │   ├── Exam.java
│   │           │   ├── Module.java
│   │           │   ├── Grade.java
│   │           │   ├── Product.java
│   │           │   └── Certification.java
│   │           └── util/
│   │               └── SceneManager.java
│   └── resources/
│       ├── fxml/
│       │   ├── auth/
│       │   │   ├── login.fxml
│       │   │   └── signup.fxml
│       │   ├── student/
│       │   │   ├── courses.fxml
│       │   │   ├── exams.fxml
│       │   │   ├── bulletin.fxml
│       │   │   ├── certification.fxml
│       │   │   └── shop.fxml
│       │   └── teacher/
│       │       ├── dashboard.fxml
│       │       ├── manage-courses.fxml
│       │       ├── manage-modules.fxml
│       │       ├── manage-exams.fxml
│       │       ├── shop-management.fxml
│       │       ├── bulletins.fxml
│       │       ├── certifications.fxml
│       │       ├── analysis-ai.fxml
│       │       └── student-management.fxml
│       ├── css/
│       │   └── style.css
│       └── images/
├── pom.xml
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+** (LTS)
- **Maven 3.8+**
- **JavaFX 21**

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Rachid09-coder/javafx.git
   cd javafx
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

---

## 👥 Team Collaboration Guide

### How to contribute

Each team member should work on a specific feature branch:

```bash
# Create your feature branch
git checkout -b feature/login-screen
git checkout -b feature/student-courses
git checkout -b feature/teacher-dashboard
```

### Interface Assignment

| Interface | File | Status |
|-----------|------|--------|
| Login | `LoginController.java` + `login.fxml` | 🟡 Template ready |
| Sign Up | `SignUpController.java` + `signup.fxml` | 🟡 Template ready |
| Student Courses | `CoursesController.java` + `courses.fxml` | 🟡 Template ready |
| Student Exams | `ExamsController.java` + `exams.fxml` | 🟡 Template ready |
| Student Bulletin | `BulletinController.java` + `bulletin.fxml` | 🟡 Template ready |
| Student Certification | `CertificationController.java` + `certification.fxml` | 🟡 Template ready |
| Student Shop | `ShopController.java` + `shop.fxml` | 🟡 Template ready |
| Teacher Dashboard | `TeacherDashboardController.java` + `dashboard.fxml` | 🟡 Template ready |
| Manage Courses | `ManageCoursesController.java` + `manage-courses.fxml` | 🟡 Template ready |
| Manage Modules | `ManageModulesController.java` + `manage-modules.fxml` | 🟡 Template ready |
| Manage Exams | `ManageExamsController.java` + `manage-exams.fxml` | 🟡 Template ready |
| Shop Management | `ShopManagementController.java` + `shop-management.fxml` | 🟡 Template ready |
| Bulletins | `BulletinsController.java` + `bulletins.fxml` | 🟡 Template ready |
| Certifications | `CertificationsController.java` + `certifications.fxml` | 🟡 Template ready |
| AI Analysis | `AnalysisAIController.java` + `analysis-ai.fxml` | 🟡 Template ready |
| Student Management | `StudentManagementController.java` + `student-management.fxml` | 🟡 Template ready |

### Implementation Guide

Each controller has `TODO` comments indicating where to add:
- **Database queries** (replace demo data)
- **Service calls** (implement service layer)
- **Navigation logic** (use `SceneManager.getInstance().navigateTo(...)`)

### Navigation (SceneManager)

To navigate between screens, use:
```java
// Example: Navigate to teacher dashboard
SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD);

// Example: Navigate to student courses
SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES);
```

All available scenes:
- `LOGIN`, `SIGNUP`
- `STUDENT_COURSES`, `STUDENT_EXAMS`, `STUDENT_BULLETIN`, `STUDENT_CERTIFICATION`, `STUDENT_SHOP`
- `TEACHER_DASHBOARD`, `TEACHER_MANAGE_COURSES`, `TEACHER_MANAGE_MODULES`, `TEACHER_MANAGE_EXAMS`
- `TEACHER_SHOP_MANAGEMENT`, `TEACHER_BULLETINS`, `TEACHER_CERTIFICATIONS`
- `TEACHER_ANALYSIS_AI`, `TEACHER_STUDENT_MANAGEMENT`

---

## 🎯 Architecture

### MVC Pattern
- **Model** (`com.edusmart.model`) - Data classes (User, Course, Exam, etc.)
- **View** (`src/main/resources/fxml`) - FXML layout files
- **Controller** (`com.edusmart.controller`) - Business logic and event handling

### Key Classes
- **`Main.java`** - JavaFX Application entry point
- **`SceneManager`** - Centralized navigation manager (Singleton)
- **`style.css`** - Global EduSmart stylesheet

---

## 📋 TODO List for Implementation

- [ ] Implement database connection (JDBC / Hibernate)
- [ ] Create service layer classes (CourseService, ExamService, etc.)
- [ ] Implement SessionManager for current user state
- [ ] Connect controllers to real data sources
- [ ] Add input validation on all forms
- [ ] Implement PDF export for bulletins/certifications
- [ ] Add i18n support (French/Arabic/English)
- [ ] Write unit tests for service layer

---

## 📄 License

This project is created for educational purposes by **EduSmart Team**.
