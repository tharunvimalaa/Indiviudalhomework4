package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The ReviewEditor class provides an interface for editing an existing review or creating a new one.
 * It handles the retrieval and update of review details and interacts with the database to save changes.
 */
public class ReviewEditor {
    private final Stage stage = new Stage();

    /**
     * Constructs a ReviewEditor with the specified database helper, username, answer identifier, and existing review.
     *
     * @param dbHelper the database helper used for database operations.
     * @param username the username of the reviewer editing the review.
     * @param answerId the identifier of the answer associated with the review.
     * @param existingReview the existing review to be edited; if null, a new review will be created.
     */
    public ReviewEditor(DatabaseHelper dbHelper, String username, int answerId, Review existingReview) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Write your review here...");    //you are able to write reviews, save them to update
        if (existingReview != null) {
            reviewArea.setText(existingReview.getText());
        }

        Button saveBtn = new Button("Save Review");
        saveBtn.setOnAction(e -> {
            String text = reviewArea.getText().trim();
            if (text.isEmpty()) {
                showAlert("Error", "Review cannot be empty.");
                return;
            }
            try {
                if (existingReview == null) {
                    dbHelper.createReview(answerId, username, text);
                    dbHelper.logAction(username, "Created a new review for answer ID " + answerId + ": \"" + text + "\"");
                } else {
                    dbHelper.updateReview(existingReview.getId(), text);
                    dbHelper.logAction(username, "Updated review ID " + existingReview.getId() + " for answer ID " + answerId);
                }
                showAlert("Success", "Review saved successfully.");
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Could not save review: " + ex.getMessage());
            }
        });
        
        

        root.getChildren().addAll(new Label("Your Review:"), reviewArea, saveBtn);
        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("Review Editor");
    }

    /**
     * Displays the review editor interface, allowing the user to edit the review details.
     */
    public void show() {
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); 
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
