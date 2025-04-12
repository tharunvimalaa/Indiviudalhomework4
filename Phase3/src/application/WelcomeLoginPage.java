package application;

import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.util.List;

import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    public void show( Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label welcomeLabel = new Label("Welcome!!");
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // Button to navigate to the user's respective page based on their role
        Button continueButton = new Button("Continue to your Page");
        continueButton.setOnAction(a -> {
            List<String> roles = user.getRoles();
            if (roles.contains("admin")) {
                new AdminHomePage(databaseHelper, "admin").show(primaryStage, user.getUserName());
            } else if (!roles.contains("admin")) {
                new UserHomePage(databaseHelper, null).show(primaryStage);
            }
        });
	    
	    // Button to logout of current role (similar to quit)
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(a -> {
            databaseHelper.closeConnection(); // Close any session if applicable
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage); // Redirect to login page
        });

	    // Button to quit the application
	    Button quitButton = new Button("Quit");
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });
	    
	    // "Invite" button for admin to generate invitation codes
	    if (user.getRoles().contains("admin")) {
            Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage);
            });
            layout.getChildren().add(inviteButton);
        }

        layout.getChildren().addAll(welcomeLabel, continueButton, logoutButton, quitButton);
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
    }
}