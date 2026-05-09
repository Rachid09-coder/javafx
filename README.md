# EduSmart - JavaFX Educational Platform

EduSmart is a professional JavaFX-based desktop application for managing educational platforms, including courses, modules, exams, and grading.

## Features
- **Course & Module Management**: Manage subjects, courses, and educational materials.
- **Exam Management**: Create, schedule, and grade exams.
- **AI-Assisted Grading & Plagiarism Detection**: Integrates advanced features for modern educational needs.

## Tech Stack
- Java JDK 17
- JavaFX 17 (via OpenJFX)
- Maven
- MySQL

## Prerequisites
- JDK 17 installed
- Maven installed
- MySQL Server running

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/your-repo.git
   cd your-repo
   ```

2. Configure the database connection in your resources/properties file.

3. Run the application:
   ```bash
   mvn clean javafx:run
   ```

### Running in IDE (VS Code / IntelliJ)
If you encounter `NoClassDefFoundError: Stage`, run the project using the **`com.edusmart.AppLauncher`** class instead of `Main.java`. This bypasses JavaFX runtime component checks in non-modular IDE environments.


## Project Structure
- `src/main/java/` - Java source code
- `src/main/resources/` - FXML files, CSS styles, and images
- `pom.xml` - Maven configuration

## Contributing
Follow standard Git workflows. Create feature branches and submit pull requests.
