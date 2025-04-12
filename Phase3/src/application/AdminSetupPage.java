
package application;

import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import databasePart1.*;

/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;

    /**
     * Constructor.
     *
     * @param databaseHelper.
     */
    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    /**
     * Displays the Admin Setup Page on the given stage.
     *
     * @param primaryStage the primary stage where the admin setup interface will be shown.
     */
    public void show(Stage primaryStage) {
    	// Input fields for userName and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);
        Label  errorLabel = new Label();
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");
        emailField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String email = emailField.getText();
            String usernameValidation = UserNameRecognizer.checkForValidUserName(userName);
            String passwordValidation = PasswordEvaluator.evaluatePassword(password);
            
            if(!usernameValidation.isEmpty()) {
            	errorLabel.setText(usernameValidation);
            	return;
            }
            if(!passwordValidation.isEmpty()) {
            	errorLabel.setText(passwordValidation);
            	return;
            }
            if (!EmailValidation.isValidEmail(email)) {
                errorLabel.setText("Please enter a valid email address.");
                return;
            }


            try {
            	List<String> adminRole = new ArrayList<>(Arrays.asList("admin"));
            	// Create a new User object with admin role and register in the database
            	User user=new User(userName, password, adminRole, email);
                databaseHelper.register(user);
                System.out.println("Administrator setup completed.");
                
                // Navigate to the Welcome Login Page
                new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });


        VBox layout = new VBox(10, userNameField, emailField, passwordField, setupButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
