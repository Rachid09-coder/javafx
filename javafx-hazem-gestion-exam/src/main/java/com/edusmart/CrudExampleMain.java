package com.edusmart;

import com.edusmart.dao.CourseDao;
import com.edusmart.dao.ModuleDao;
import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.dao.jdbc.JdbcModuleDao;
import com.edusmart.model.Course;
import com.edusmart.model.Module;
import com.edusmart.service.CourseService;
import com.edusmart.service.ModuleService;
import com.edusmart.service.impl.CourseServiceImpl;
import com.edusmart.service.impl.ModuleServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Simple console example for CRUD operations.
 * Run this class directly from IntelliJ to test DB operations.
 */
public class CrudExampleMain {

    public static void main(String[] args) {
        CourseDao courseDao = new JdbcCourseDao();
        ModuleDao moduleDao = new JdbcModuleDao();

        CourseService courseService = new CourseServiceImpl(courseDao);
        ModuleService moduleService = new ModuleServiceImpl(moduleDao);

        runCourseCrudDemo(courseService);
        runModuleCrudDemo(moduleService);
    }

    private static void runCourseCrudDemo(CourseService courseService) {
        System.out.println("=== COURSE CRUD ===");

        Course course = new Course();
        course.setId(1001);
        course.setTitle("JDBC Fundamentals");
        course.setDescription("Course created from JDBC CRUD demo.");
        course.setPrice(149.99);
        course.setStatusValue("ACTIVE");
        course.setCreatedAt(LocalDateTime.now());
        course.setThumbnailPath("images/jdbc-course.png");
        course.setPdfPath("pdf/jdbc-course.pdf");
        course.setGeneratedContent("Auto-generated content placeholder");
        course.setCoefficient(1.5);

        // Create
        System.out.println("Create: " + courseService.createCourse(course));

        // Read all
        System.out.println("Find all:");
        courseService.getAllCourses().forEach(System.out::println);

        // Read by id
        Optional<Course> found = courseService.getCourseById(1001);
        System.out.println("Find by id(1001): " + found.orElse(null));

        // Update
        if (found.isPresent()) {
            Course toUpdate = found.get();
            toUpdate.setPrice(199.99);
            toUpdate.setStatusValue("INACTIVE");
            System.out.println("Update: " + courseService.updateCourse(toUpdate));
        }

        // Delete
        System.out.println("Delete: " + courseService.deleteCourse(1001));
        System.out.println();
    }

    private static void runModuleCrudDemo(ModuleService moduleService) {
        System.out.println("=== MODULE CRUD ===");

        List<Module> before = moduleService.getAllModules();
        Module module = new Module();
        module.setTitle("Intro to JDBC API");
        module.setDescription("Module created from JDBC CRUD demo.");
        module.setThumbnail("images/module-jdbc.png");
        module.setCreatedAt(LocalDateTime.now());

        System.out.println("Create: " + moduleService.createModule(module));

        Module created = moduleService.getAllModules().stream()
                .filter(m -> before.stream().noneMatch(b -> b.getId() == m.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Created module not found"));
        int newId = created.getId();

        System.out.println("Find all:");
        moduleService.getAllModules().forEach(System.out::println);

        Optional<Module> found = moduleService.getModuleById(newId);
        System.out.println("Find by id(" + newId + "): " + found.orElse(null));

        if (found.isPresent()) {
            Module toUpdate = found.get();
            toUpdate.setTitle("JDBC API Essentials");
            System.out.println("Update: " + moduleService.updateModule(toUpdate));
        }

        System.out.println("Delete: " + moduleService.deleteModule(newId));
        System.out.println();
    }
}
