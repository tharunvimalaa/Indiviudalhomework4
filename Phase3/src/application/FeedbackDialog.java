//Tharun
package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;

import databasePart1.DatabaseHelper;

/**
 * The FeedbackDialog class is responsible for displaying a dialog window
 * that allows users to submit feedback. This dialog may include fields for
 * entering a feedback message and additional information.
 */
public class FeedbackDialog {
	
	 /**
     * Constructs a new FeedbackDialog.
     * <p>
     * This constructor initializes the feedback dialog and sets up the necessary
     * UI components.
     * </p>
     */
	public FeedbackDialog() {}
	
    
    /**
     * Opens a dialog where a student can enter feedback.
     *
     * @param dbHelper        DatabaseHelper for database operations.
     * @param reviewId        The review ID receiving feedback.
     * @param studentUsername The student’s username.
     */
    public static void showFeedbackDialog(DatabaseHelper dbHelper, int reviewId, String studentUsername) {
        Stage stage = new Stage();
        stage.setTitle("Send Feedback");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        
        Label prompt = new Label("Enter your private feedback:");
        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("Type your feedback here...");
        
        Button sendBtn = new Button("Send Feedback");
        sendBtn.setOnAction(e -> {
            String message = feedbackArea.getText().trim();
            if (message.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Feedback cannot be empty.");
                alert.showAndWait();
                return;
            }
            Feedback feedback = new Feedback(reviewId, studentUsername, message);
            try {
                dbHelper.addFeedback(feedback);
                dbHelper.logAction(studentUsername, "Submitted feedback for review ID " + reviewId + ": \"" + message + "\"");
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Feedback sent successfully!");
                alert.showAndWait();
                stage.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error sending feedback: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        layout.getChildren().addAll(prompt, feedbackArea, sendBtn);
        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.show();
    }
}
