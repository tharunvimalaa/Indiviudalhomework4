package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import databasePart1.DatabaseHelper;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 * Additionally, it provides a "Forgot Password" option for OTP-based password reset.
 */
public class UserLoginPage {

    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Input field for the user's username and password.
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter username");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        // Label to display error messages.
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Login button for normal login.
        Button loginButton = new Button("Login");
        loginButton.setOnAction(a -> {
            String userName = userNameField.getText();
            String password = passwordField.getText();

            try {
                // Retrieve the user's roles from the database (e.g., "admin,student").
                String roleString = databaseHelper.getUserRoles(userName);
                if (roleString == null) {
                    errorLabel.setText("User account doesn't exist");
                    return;
                }
                // Convert the comma-separated roles string into a list.
                List<String> roles = Arrays.asList(roleString.split(","));
                User user = new User(userName, password, roles, roleString);

                // Validate login credentials.
                if (!databaseHelper.login(user)) {
                    errorLabel.setText("Invalid username or password");
                    return;
                }

                // Navigate based on the user's role(s).
             // ...
                if (roles.size() == 1) {
                    String role = roles.get(0).trim();
                    switch (role) {
                        case "admin":
                            new AdminHomePage(databaseHelper, userName).show(primaryStage, userName);
                            break;
                        case "student":
                            new StudentHomePage(databaseHelper, userName).show(primaryStage);
                            break;
                        case "instructor"://Added instructor home page option
                        	new InstructorHomePage(databaseHelper, userName).show(primaryStage);
                            break;
                        case "staff":
                        case "reviewer":
                            new ReviewerProfilePage(databaseHelper, userName).show(primaryStage);
                            break;
                        default:
                            System.out.println("Unknown role: " + role);
                    }
                    
                } else {
                    new RoleSelectionPage(databaseHelper).show(primaryStage, user);
                }
                // ...

            } catch (SQLException ex) {
                errorLabel.setText("Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // --- NEW: Forgot Password Button ---
        Button forgotPasswordButton = new Button("Forgot Password");
        forgotPasswordButton.setOnAction(e -> {
            // Create a new window (stage) for OTP-based password reset.
            Stage forgotStage = new Stage();
            forgotStage.setTitle("Reset Password Using OTP");

            // Fields for username, OTP, and new password.
            TextField forgotUsernameField = new TextField();
            forgotUsernameField.setPromptText("Enter your username");
            forgotUsernameField.setMaxWidth(250);

            TextField otpField = new TextField();
            otpField.setPromptText("Enter OTP");
            otpField.setMaxWidth(250);

            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setPromptText("Enter new password");
            newPasswordField.setMaxWidth(250);

            Label forgotMessageLabel = new Label();
            forgotMessageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

            Button resetPasswordButton = new Button("Reset Password");
            resetPasswordButton.setOnAction(ev -> {
                String username = forgotUsernameField.getText();
                String otp = otpField.getText();
                String newPassword = newPasswordField.getText();

                try {
                    if (!databaseHelper.doesUserExist(username)) {
                        forgotMessageLabel.setText("Username does not exist.");
                        return;
                    }
                    // Retrieve the user's roles.
                    String roleString = databaseHelper.getUserRoles(username);
                    List<String> roles = (roleString != null) ? Arrays.asList(roleString.split(",")) : null;

                    // Create a temporary User object using the OTP as the password.
                    User tempUser = new User(username, otp, roles, roleString);

                    // Verify that the OTP is correct and active.
                    if (!databaseHelper.login(tempUser) || !OneTimePassword.hasTempPass(username)) {
                        forgotMessageLabel.setText("Invalid OTP.");
                        return;
                    }

                    // Validate the new password using PasswordEvaluator.
                    String validationMessage = PasswordEvaluator.evaluatePassword(newPassword);
                    if (!validationMessage.isEmpty()) {
                        forgotMessageLabel.setText("New password invalid: " + validationMessage);
                        return;
                    }

                    // Update the user's password with the new password.
                    if (databaseHelper.updatePassword(username, newPassword)) {
                        OneTimePassword.clearTempPass(username);
                        forgotMessageLabel.setText("Password updated successfully. Please log in.");
                        // Optionally, close the forgot-password window after success.
                        forgotStage.close();
                    } else {
                        forgotMessageLabel.setText("Failed to update password.");
                    }
                } catch (SQLException ex) {
                    forgotMessageLabel.setText("Database error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            VBox forgotLayout = new VBox(10, forgotUsernameField, otpField, newPasswordField, resetPasswordButton, forgotMessageLabel);
            forgotLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");
            Scene forgotScene = new Scene(forgotLayout, 400, 300);
            forgotStage.setScene(forgotScene);
            forgotStage.show();
        });
        // --- End Forgot Password Button ---

        VBox layout = new VBox(10, userNameField, passwordField, loginButton, forgotPasswordButton, errorLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
}
