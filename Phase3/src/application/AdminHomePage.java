package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import databasePart1.DatabaseHelper;

/**
 * AdminHomePage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin and provides
 * navigation buttons to various admin functionalities.
 */
public class AdminHomePage {

    private final DatabaseHelper databaseHelper;
    private final String role;

    /**
     * Constructs a new AdminHomePage.
     * <p>
     * This constructor initializes the admin home page interface using the provided
     * database helper for managing database interactions and assigns the appropriate
     * role for access control.
     * </p>
     *
     * @param databaseHelper the helper object used to manage database connections and queries
     * @param role the role of the current user, which determines the access level to the admin functions
     */
    
    public AdminHomePage(DatabaseHelper databaseHelper, String role) {
        this.databaseHelper = databaseHelper;
        this.role = role;
    }

    /**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param adminUser The username of the currently logged-in admin.
     */
    public void show(Stage primaryStage, String adminUser) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Label to display the welcome message for the admin.
        Label adminLabel = new Label("Admin Dashboard");
        adminLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // *****************************************************************
        // MODIFICATION:
        // Removed outdated role update UI elements (userDropdown, roleDropdown, updateRoleButton).
        // Role management is now handled in a dedicated UI via AdminManageRolesPage.
        // *****************************************************************

        // Button to logout of the current user account.
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(a -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        // Button to view all user accounts.
        Button accountButton = new Button("View User Accounts");
        accountButton.setOnAction(a -> {
            try {
                new ViewAccountPage(databaseHelper).show(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Button to open the OTP generation page.
        Button generateOTPButton = new Button("Generate OTP for a User");
        generateOTPButton.setOnAction(e -> {
            // Create an instance of OneTimePassword and open the OTP generation page.
            OneTimePassword otpManager = new OneTimePassword(databaseHelper);
            new AdminOTPGenerationPage(databaseHelper, otpManager).show(primaryStage);
        });

        // Button to open the invitation page.
        Button inviteButton = new Button("Invite");
        inviteButton.setOnAction(a -> {
            // Call existing InvitationPage logic.
            new InvitationPage().show(databaseHelper, primaryStage);
        });

        // NEW BUTTON: Manage Roles.
        // Opens the dedicated role management UI (AdminManageRolesPage) that allows
        // an admin to add or remove roles according to the business rules.
        Button manageRolesButton = new Button("Manage Roles");
        manageRolesButton.setOnAction(e -> {
            new AdminManageRolesPage(databaseHelper, adminUser).show(primaryStage);
        });

        //Button to see reviewers list
        Button reviewerListBtn = new Button ("See Reviewers");
        Label l = new Label();
        l.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");
        reviewerListBtn.setOnAction(a -> {
        	databaseHelper.showReviewerList();
        });
        
        
        // Layout for the admin dashboard.
        layout.getChildren().addAll(adminLabel, inviteButton, accountButton, generateOTPButton,
                manageRolesButton, reviewerListBtn, logoutButton);

        Scene adminScene = new Scene(layout, 800, 400);

        // Set the scene to primary stage.
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Page");
        primaryStage.show();
    }
}
