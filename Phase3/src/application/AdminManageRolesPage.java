package application;

import databasePart1.DatabaseHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The AdminManageRolesPage class provides the UI for administrators to manage user roles.
 * It handles the display and interactions for assigning and removing roles from users.
 */
public class AdminManageRolesPage {

    private final DatabaseHelper databaseHelper;
    private final String adminUser; // The admin performing these changes.

    /**
     * Constructs a new AdminManageRolesPage.
     *
     * @param databaseHelper the database helper object used to interact with the underlying database.
     * @param adminUser the username of the admin user managing roles.
     */
    public AdminManageRolesPage(DatabaseHelper databaseHelper, String adminUser) {
        this.databaseHelper = databaseHelper;
        this.adminUser = adminUser;
    }


    /**
     * Displays the Admin Manage Roles Page on the provided stage.
     *
     * @param primaryStage the primary stage where the page will be displayed.
     */
    public void show(Stage primaryStage) {
        Stage stage = new Stage();
        stage.setTitle("Manage User Roles");

        // Create the table.
        TableView<UserRoleRow> tableView = new TableView<>();
        tableView.setPrefWidth(700);
        tableView.setPrefHeight(400);

        // --- Column for Username ---
        TableColumn<UserRoleRow, String> usernameCol = new TableColumn<>("User");
        usernameCol.setMinWidth(150);
        usernameCol.setCellValueFactory(cell -> cell.getValue().usernameProperty());

        // --- Column for Assigned Roles (comma-separated) ---
        TableColumn<UserRoleRow, String> rolesCol = new TableColumn<>("Assigned Roles");
        rolesCol.setMinWidth(200);
        rolesCol.setCellValueFactory(cell -> cell.getValue().assignedRolesProperty());

        // --- Column for Role Selection (ComboBox) ---
        TableColumn<UserRoleRow, String> selectorCol = new TableColumn<>("Select Role");
        selectorCol.setMinWidth(150);
        selectorCol.setCellFactory(col -> new TableCell<UserRoleRow, String>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            {
                // Fixed list of roles.
                comboBox.getItems().addAll("admin", "student", "instructor", "staff", "reviewer");
                comboBox.setPrefWidth(140);
                // Set prompt text so nothing is selected by default.
                comboBox.setPromptText("Select role");
                // When selection changes, update the row’s selectedRole property.
                comboBox.setOnAction(e -> {
                    UserRoleRow row = getTableView().getItems().get(getIndex());
                    String selected = comboBox.getSelectionModel().getSelectedItem();
                    row.setSelectedRole(selected);
                    // Refresh the table to update button states.
                    getTableView().refresh();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserRoleRow row = getTableView().getItems().get(getIndex());
                    // Instead of auto-selecting a value from the list, clear the selection
                    // so the prompt text is visible until the admin explicitly selects a role.
                    if (row.getSelectedRole() != null) {
                        comboBox.setValue(row.getSelectedRole());
                    } else {
                        comboBox.getSelectionModel().clearSelection();
                    }
                    setGraphic(comboBox);
                }
            }
        });

        // --- Column for Add Button ---
        TableColumn<UserRoleRow, Void> addCol = new TableColumn<>("Add Role");
        addCol.setMinWidth(100);
        addCol.setCellFactory(col -> new TableCell<UserRoleRow, Void>() {
            private final Button addButton = new Button("Add");

            {
                addButton.setOnAction(e -> {
                    UserRoleRow row = getTableView().getItems().get(getIndex());
                    String selectedRole = row.getSelectedRole();
                    if (selectedRole != null && !row.hasRole(selectedRole)) {
                        try {
                            // Attempt to add the selected role.
                            boolean success = databaseHelper.addUserRole(adminUser, row.getUsername(), selectedRole);
                            if (success) {
                                // Refresh the row's roles from the database.
                                String newRoles = databaseHelper.getUserRoles(row.getUsername());
                                row.setAssignedRoles(newRoles);
                                row.setSelectedRole(null); // Clear selection after action.
                                getTableView().refresh();
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserRoleRow row = getTableView().getItems().get(getIndex());
                    // Enable Add only if a role is selected and not already assigned.
                    boolean enable = row.getSelectedRole() != null && !row.hasRole(row.getSelectedRole());
                    addButton.setDisable(!enable);
                    setGraphic(addButton);
                }
            }
        });

        // --- Column for Remove Button ---
        TableColumn<UserRoleRow, Void> removeCol = new TableColumn<>("Remove Role");
        removeCol.setMinWidth(100);
        removeCol.setCellFactory(col -> new TableCell<UserRoleRow, Void>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setOnAction(e -> {
                    UserRoleRow row = getTableView().getItems().get(getIndex());
                    String selectedRole = row.getSelectedRole();
                    if (selectedRole != null && row.hasRole(selectedRole)) {
                        try {
                            // Attempt to remove the selected role.
                            boolean success = databaseHelper.removeUserRole(adminUser, row.getUsername(), selectedRole);
                            if (success) {
                                String newRoles = databaseHelper.getUserRoles(row.getUsername());
                                row.setAssignedRoles(newRoles);
                                row.setSelectedRole(null); // Clear selection after action.
                                getTableView().refresh();
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserRoleRow row = getTableView().getItems().get(getIndex());
                    boolean enable = false;
                    String selectedRole = row.getSelectedRole();
                    if (selectedRole != null && row.hasRole(selectedRole)) {
                        // Disallow removal if it would leave the user with no roles.
                        if (row.getAssignedRolesList().size() <= 1) {
                            enable = false;
                        }
                        // For the "admin" role: allow removal only if it is the admin's own role
                        // and at least one other admin remains.
                        else if ("admin".equals(selectedRole)) {
                            if (row.getUsername().equals(adminUser)) {
                                try {
                                    int adminCount = databaseHelper.getAdminCount();
                                    enable = adminCount > 1;
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                enable = false; // Cannot remove another admin's admin role.
                            }
                        } else {
                            enable = true;
                        }
                    }
                    removeButton.setDisable(!enable);
                    setGraphic(removeButton);
                }
            }
        });

        // Add all columns to the table.
        tableView.getColumns().addAll(usernameCol, rolesCol, selectorCol, addCol, removeCol);

        // --- Populate the table with data from the database ---
        try {
            for (String username : databaseHelper.getAllUsers()) {
                String roles = databaseHelper.getUserRoles(username);
                tableView.getItems().add(new UserRoleRow(username, roles));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // --- Layout ---
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().add(tableView);
        Scene scene = new Scene(vbox, 700, 400);
        stage.setScene(scene);
        stage.initOwner(primaryStage);
        stage.show();
    }

    /**
     * Inner class representing a row in the table.
     */
    public static class UserRoleRow {
        private final SimpleStringProperty username;
        private final SimpleStringProperty assignedRoles;
        private final SimpleStringProperty selectedRole;

        /**
         * Constructs a new UserRoleRow with the specified username and assigned roles.
         *
         * @param username the username associated with this row.
         * @param assignedRoles a string representation of the assigned roles for the user.
         */
        
        public UserRoleRow(String username, String assignedRoles) {
            this.username = new SimpleStringProperty(username);
            // Ensure that the roles string is not null.
            this.assignedRoles = new SimpleStringProperty(assignedRoles != null ? assignedRoles : "");
            this.selectedRole = new SimpleStringProperty();
        }

        public String getUsername() {
            return username.get();
        }

        /**
         * Returns the username property for binding UI elements.
         *
         * @return a SimpleStringProperty containing the username.
         */
        public SimpleStringProperty usernameProperty() {
            return username;
        }

        public String getAssignedRoles() {
            return assignedRoles.get();
        }

        public void setAssignedRoles(String roles) {
            assignedRoles.set(roles);
        }

        /**
         * Returns the property representing the assigned roles.
         *
         * @return a SimpleStringProperty containing the assigned roles.
         */
        public SimpleStringProperty assignedRolesProperty() {
            return assignedRoles;
        }

        public String getSelectedRole() {
            return selectedRole.get();
        }

        public void setSelectedRole(String role) {
            selectedRole.set(role);
        }

        /**
         * Returns the property representing the currently selected role.
         *
         * @return a SimpleStringProperty containing the selected role.
         */
        public SimpleStringProperty selectedRoleProperty() {
            return selectedRole;
        }

        /**
         * Retrieves a list of assigned roles for the admin user.
         *
         * @return a list of strings representing the assigned roles.
         */
        public List<String> getAssignedRolesList() {
            List<String> list = new ArrayList<>();
            String rolesStr = getAssignedRoles();
            if (rolesStr != null && !rolesStr.trim().isEmpty()) {
                String[] parts = rolesStr.split(",");
                for (String part : parts) {
                    list.add(part.trim());
                }
            }
            return list;
        }

        /**
         * Checks if the given role is assigned to the admin user.
         *
         * @param role the role to check for.
         * @return true if the admin user has the specified role, false otherwise.
         */
        public boolean hasRole(String role) {
            return getAssignedRolesList().contains(role);
        }
    }
}
