package application;

import javafx.geometry.Insets;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;
import databasePart1.DatabaseHelper;

/**
 * AdminOTPGenerationPage provides an interface for the admin to generate a one-time password (OTP)
 * for a specific user. The admin selects a user from a list and clicks a button to generate the OTP.
 */
public class AdminOTPGenerationPage {

    private final DatabaseHelper databaseHelper;
    private final OneTimePassword otpManager;

    /**
     * Constructor.
     *
     * @param databaseHelper the database helper object.
     * @param otpManager.
     */
    public AdminOTPGenerationPage(DatabaseHelper databaseHelper, OneTimePassword otpManager) {
        this.databaseHelper = databaseHelper;
        this.otpManager = otpManager;
    }

    /**
     * Displays the OTP generation page.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
        List<String> users;
        try {
            // Retrieve all users from the database.
            users = databaseHelper.getAllUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Create a ListView to display the list of users.
        ListView<String> userListView = new ListView<>();
        userListView.getItems().addAll(users);
        userListView.setPrefHeight(200);

        // Button to generate OTP for the selected user.
        Button generateOTPButton = new Button("Generate OTP for Selected User");
        Label infoLabel = new Label();

        generateOTPButton.setOnAction(e -> {
            String selectedUser = userListView.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                infoLabel.setText("Please select a user!");
                return;
            }
            try {
                // Generate the OTP for the selected user using the new method.
                String otp = otpManager.generateOTPForUser(selectedUser);
                // Display the OTP in a pop-up dialog.
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("OTP Generated");
                alert.setHeaderText("One Time Password for " + selectedUser);
                alert.setContentText("OTP: " + otp);
                alert.showAndWait();
            } catch (SQLException ex) {
                ex.printStackTrace();
                infoLabel.setText("Error generating OTP for " + selectedUser);
            }
        });

        // Layout for the OTP generation page.
        VBox layout = new VBox(10, userListView, generateOTPButton, infoLabel);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 400, 300);

        // Create a new stage for the OTP generation page.
        Stage otpStage = new Stage();
        otpStage.setScene(scene);
        otpStage.setTitle("Admin - Generate OTP");
        otpStage.show();
    }
}
