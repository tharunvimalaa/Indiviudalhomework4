package application;

import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import databasePart1.DatabaseHelper;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, email, password, and a valid invitation code to register.
 */
public class SetupAccountPage {
    
    private final DatabaseHelper databaseHelper;

    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
        // Input field for userName
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        // New input field for email
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");
        emailField.setMaxWidth(250);

        // Input field for password
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Input field for invitation code
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        // Labels to display password requirements
        Label labelRequiresWhat = new Label("While creating a suitable password, check you are adhering to the following requirements:");
        Label labelUppercase = new Label("At least one uppercase letter");
        Label labelLowercase = new Label("At least one lowercase letter");
        Label labelNum = new Label("At least one number");
        Label labelLongEnough = new Label("At least eight characters in length");
        Label labelSpecial = new Label("At least one special character or symbol");

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
            // Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String email = emailField.getText();
            String code = inviteCodeField.getText();
            List<String> role = new ArrayList<>();
            
            // Validate the email format
            if (!EmailValidation.isValidEmail(email)) {
                errorLabel.setText("Please enter a valid email address.");
                return;
            }
            
            try {
                // Check if the user already exists
                if (!databaseHelper.doesUserExist(userName)) {
                    // Validate the invitation code
                    if (databaseHelper.validateInvitationCode(code)) {
                        // Loop to check the invite code for any appended roles.
                        for (int i = 4; i < code.length(); i += 3) {
                            switch (code.substring(i, i + 3)) {
                                case ".an":
                                    role.add("admin");
                                    break;
                                case ".st":
                                    role.add("student");
                                    break;
                                case ".ir":
                                    role.add("instructor");
                                    break;
                                case ".sf":
                                    role.add("staff");
                                    break;
                                case ".rr":
                                    role.add("reviewer");
                                    break;
                                default:
                                    break;
                            }
                        }
                        // Create a new user (including email) and register them in the database
                        User user = new User(userName, password, role, email);
                        databaseHelper.register(user);
                        
                        // Navigate to the Welcome Login Page
                        new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                        new UserLoginPage(databaseHelper).show(primaryStage); 
                    } else {
                        errorLabel.setText("Please enter a valid invitation code");
                    }
                } else {
                    errorLabel.setText("This userName is taken!!.. Please use another to setup an account");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });
   
        // Listener to update password requirements in real time
        passwordField.textProperty().addListener((observe, oldVal, newVal) ->
            passwordGUI(newVal, labelUppercase, labelLowercase, labelNum, labelSpecial, labelLongEnough)
        );

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, emailField, passwordField, inviteCodeField, setupButton, errorLabel,
                labelRequiresWhat, labelUppercase, labelLowercase, labelNum, labelSpecial, labelLongEnough);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
        
    private void passwordGUI(String password, Label labelUppercase, Label labelLowercase, Label labelNum, Label labelSpecial, Label labelLongEnough) {
        PasswordEvaluator.evaluatePassword(password);
        labelUppercase.setTextFill(PasswordEvaluator.foundUpperCase ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
        labelLowercase.setTextFill(PasswordEvaluator.foundLowerCase ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
        labelNum.setTextFill(PasswordEvaluator.foundNumericDigit ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
        labelSpecial.setTextFill(PasswordEvaluator.foundSpecialChar ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
        labelLongEnough.setTextFill(PasswordEvaluator.foundLongEnough ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
        
        if (password.isEmpty()) {
            labelUppercase.setTextFill(javafx.scene.paint.Color.BLACK);
            labelLowercase.setTextFill(javafx.scene.paint.Color.BLACK);
            labelNum.setTextFill(javafx.scene.paint.Color.BLACK);
            labelSpecial.setTextFill(javafx.scene.paint.Color.BLACK);
            labelLongEnough.setTextFill(javafx.scene.paint.Color.BLACK);
            return;
        }
    }
}
