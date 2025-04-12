package application;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;
import databasePart1.*;

/**
 * RoleSelectionPage allows users with multiple roles to select which role to assume after login.
 * MODIFICATION: New file added to handle role selection UI.
 */ 
public class RoleSelectionPage {

    private final DatabaseHelper databaseHelper;

    public RoleSelectionPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the role selection UI for the user.
     * @param primaryStage The main application window
     * @param user The logged-in user with multiple roles
     */ 
    public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        Label selectRoleLabel = new Label("Select your role:");
        layout.getChildren().add(selectRoleLabel);

        // MODIFICATION: Generate a button for each role the user has
        for (String role : user.getRoles()) {
            Button roleButton = new Button(role);
            roleButton.setOnAction(e -> navigateToHomePage(role, primaryStage, user)); // Navigate based on selection
            layout.getChildren().add(roleButton);
        }

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Select Role");
        primaryStage.show();
    }

    /**
     * MODIFICATION: Redirect user to the correct home page based on their selected role.
     */ 
    private void navigateToHomePage(String role, Stage primaryStage, User user) {
        switch (role) {
            case "admin":
            	new AdminHomePage(databaseHelper, user.getUserName()).show(primaryStage, user.getUserName());
                break;
            case "student":
            	new StudentHomePage(databaseHelper, user.getUserName()).show(primaryStage);
                break;
            case "instructor":
            	new InstructorHomePage(databaseHelper, user.getUserName()).show(primaryStage);
                break;
            case "staff"://remember to add one for staff later on for upcoming phases
            case "reviewer":
            	new ReviewerProfilePage(databaseHelper, user.getUserName()).show(primaryStage);
                break;
            default:
                System.out.println("Unknown role: " + role);
        }
    }
}
