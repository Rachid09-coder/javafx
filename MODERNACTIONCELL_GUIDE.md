# ModernActionCell - Reusable Implementation Guide

## Quick Start

The `ModernActionCell` utility class provides a reusable solution for modern action buttons across all TableViews in your application.

## Basic Implementation

### Method 1: Simple Constructor (Current Implementation)

```java
// In your controller's setupTable() method
if (actionsColumn != null) {
    actionsColumn.setCellFactory(col -> new ModernActionCell<>(
        user -> handleEditRow(user),
        user -> handleDeleteRow(user)
    ));
}
```

### Method 2: Builder Pattern (Recommended)

```java
if (actionsColumn != null) {
    actionsColumn.setCellFactory(col -> 
        new ModernActionCell.Builder<User>()
            .onEdit(this::handleEditRow)
            .onDelete(this::handleDeleteRow)
            .editTooltip("Modifier l'utilisateur")
            .deleteTooltip("Supprimer l'utilisateur")
            .build()
    );
}
```

## Complete Example

```java
package com.edusmart.controller.teacher;

import com.edusmart.model.User;
import com.edusmart.util.ModernActionCell;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentManagementController {
    
    @FXML private TableColumn<User, Void> actionsColumn;
    
    private void setupTable() {
        // Set up action buttons using ModernActionCell
        if (actionsColumn != null) {
            actionsColumn.setCellFactory(col -> 
                new ModernActionCell.Builder<User>()
                    .onEdit(this::handleEditRow)
                    .onDelete(this::handleDeleteRow)
                    .build()
            );
        }
    }
    
    private void handleEditRow(User user) {
        // Your edit logic
    }
    
    private void handleDeleteRow(User user) {
        // Your delete logic
    }
}
```

## Available CSS Classes

The ModernActionCell uses the following CSS classes (already defined in style.css):

### Edit Button
- **Class**: `btn-action-edit`
- **Colors**: Light Blue (#E3F2FD) background, Medium Blue (#1E88E5) text
- **Hover**: Darker blue with drop shadow

### Delete Button
- **Class**: `btn-action-delete`
- **Colors**: Light Red (#FFEBEE) background, Medium Red (#E53935) text
- **Hover**: Darker red with drop shadow

## Customization Options

### Custom Tooltips
```java
new ModernActionCell.Builder<User>()
    .onEdit(this::handleEdit)
    .onDelete(this::handleDelete)
    .editTooltip("Edit this record")
    .deleteTooltip("Remove this record")
    .build()
```

### Custom Styling (via CSS)
Add custom styles in your CSS stylesheet:

```css
.btn-action-edit {
    -fx-background-color: #YOUR_COLOR;
    -fx-text-fill: #YOUR_TEXT_COLOR;
}

.btn-action-delete {
    -fx-background-color: #YOUR_COLOR;
    -fx-text-fill: #YOUR_TEXT_COLOR;
}
```

## Supported Generic Types

The `ModernActionCell` works with any type of object:

```java
// For Course objects
TableColumn<Course, Void> courseActions = new TableColumn<>();
courseActions.setCellFactory(col -> new ModernActionCell<>(
    course -> handleEditCourse(course),
    course -> handleDeleteCourse(course)
));

// For Exam objects
TableColumn<Exam, Void> examActions = new TableColumn<>();
examActions.setCellFactory(col -> new ModernActionCell<>(
    exam -> handleEditExam(exam),
    exam -> handleDeleteExam(exam)
));
```

## Integration Checklist

To integrate ModernActionCell in other controllers:

- [ ] Import: `import com.edusmart.util.ModernActionCell;`
- [ ] Ensure `style.css` is loaded (it already includes the action button styles)
- [ ] Replace existing cell factory in `setupTable()` method
- [ ] Define `handleEditRow()` and `handleDeleteRow()` methods
- [ ] Test button actions and styling

## Benefits

✅ **DRY (Don't Repeat Yourself)**: Single source of truth for action buttons
✅ **Consistency**: Same appearance across all tables
✅ **Maintainability**: Update once, affects all tables
✅ **Accessibility**: Tooltips and keyboard navigation included
✅ **Type-Safe**: Generic implementation works with any model class
✅ **Flexible**: Builder pattern allows customization

## Future Enhancements

Consider these improvements:

- [ ] Add FontAwesomeFX for true SVG icons
- [ ] Add animation library for press effects
- [ ] Add keyboard shortcuts (Alt+E, Alt+D)
- [ ] Add row selection highlighting
- [ ] Add multi-select with batch actions

## Troubleshooting

### Buttons not appearing
- Ensure `style.css` is properly loaded in your FXML
- Check that the table column width is sufficient (recommend 120px minimum)

### Tooltips not showing
- Verify `javafx.scene.control.Tooltip` import is correct
- Check Tooltip text is being set

### Buttons disabled
- This is intentional behavior when row is null
- The cell checks if `getTableView().getItems().get(getIndex())` is valid

### CSS not applied
- Clear and rebuild project: `mvn clean compile`
- Ensure CSS classes are in lowercase: `btn-action-edit`, `btn-action-delete`
