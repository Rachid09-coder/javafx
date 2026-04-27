package com.edusmart.util;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * Factory for creating modern action button cells in TableViews.
 * Provides a reusable, styled actions column with Edit and Delete buttons.
 * 
 * <p>Usage Example:</p>
 * <pre>{@code
 * TableColumn<User, Void> actionsColumn = new TableColumn<>("Actions");
 * actionsColumn.setCellFactory(col -> new ModernActionCell<>(
 *     user -> handleEdit(user),
 *     user -> handleDelete(user)
 * ));
 * }</pre>
 */
public class ModernActionCell<T> extends TableCell<T, Void> {
    
    private final Button editBtn;
    private final Button deleteBtn;
    private final HBox container;
    @SuppressWarnings("unused")
    private final ActionCallback<T> editCallback;
    @SuppressWarnings("unused")
    private final ActionCallback<T> deleteCallback;

    /**
     * Creates a modern action cell with Edit and Delete buttons.
     * 
     * @param editCallback   Callback when Edit button is clicked
     * @param deleteCallback Callback when Delete button is clicked
     */
    public ModernActionCell(ActionCallback<T> editCallback, ActionCallback<T> deleteCallback) {
        this.editCallback = editCallback;
        this.deleteCallback = deleteCallback;

        // Create Edit Button (Light Blue)
        editBtn = new Button("✏");
        editBtn.getStyleClass().add("btn-action-edit");
        editBtn.setStyle("-fx-font-size: 14px; -fx-padding: 6 10;");
        editBtn.setTooltip(new Tooltip("Modifier"));
        editBtn.setOnAction(e -> {
            T item = getTableView().getItems().get(getIndex());
            if (item != null && editCallback != null) {
                editCallback.handle(item);
            }
        });

        // Create Delete Button (Light Red)
        deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("btn-action-delete");
        deleteBtn.setStyle("-fx-font-size: 14px; -fx-padding: 6 10;");
        deleteBtn.setTooltip(new Tooltip("Supprimer"));
        deleteBtn.setOnAction(e -> {
            T item = getTableView().getItems().get(getIndex());
            if (item != null && deleteCallback != null) {
                deleteCallback.handle(item);
            }
        });

        // Container for both buttons
        container = new HBox(8);
        container.setAlignment(Pos.CENTER);
        container.setPrefWidth(USE_COMPUTED_SIZE);
        container.getChildren().addAll(editBtn, deleteBtn);
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || getTableView().getItems().isEmpty()) {
            setGraphic(null);
        } else {
            T data = getTableView().getItems().get(getIndex());
            // Disable buttons if item is null
            editBtn.setDisable(data == null);
            deleteBtn.setDisable(data == null);
            setGraphic(container);
        }
    }

    /**
     * Functional interface for handling action button clicks.
     */
    @FunctionalInterface
    public interface ActionCallback<T> {
        void handle(T item);
    }

    // Builder Pattern for better usability
    public static class Builder<T> {
        private ActionCallback<T> editCallback;
        private ActionCallback<T> deleteCallback;
        private String editTooltip = "Modifier";
        private String deleteTooltip = "Supprimer";

        public Builder<T> onEdit(ActionCallback<T> callback) {
            this.editCallback = callback;
            return this;
        }

        public Builder<T> onDelete(ActionCallback<T> callback) {
            this.deleteCallback = callback;
            return this;
        }

        public Builder<T> editTooltip(String tooltip) {
            this.editTooltip = tooltip;
            return this;
        }

        public Builder<T> deleteTooltip(String tooltip) {
            this.deleteTooltip = tooltip;
            return this;
        }

        public ModernActionCell<T> build() {
            ModernActionCell<T> cell = new ModernActionCell<>(editCallback, deleteCallback);
            cell.editBtn.setTooltip(new Tooltip(editTooltip));
            cell.deleteBtn.setTooltip(new Tooltip(deleteTooltip));
            return cell;
        }
    }
}
