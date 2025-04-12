package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//MODIFIED BY ISABELLA: created so that the feedback button in the profile works. not implemented yet

import java.util.List;

public class ReviewFeedbacks {

    private DatabaseHelper dbHelper;
    private String reviewerUsername;

    public ReviewFeedbacks(DatabaseHelper dbHelper, String reviewerUsername) {
        this.dbHelper = dbHelper;
        this.reviewerUsername = reviewerUsername;
    }

    public void show(Stage stage) {
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10;");

        Label title = new Label("Feedback Received");
        ListView<String> feedbackListView = new ListView<>();

        try {
            List<String> feedbackList = dbHelper.getReviewFeedbacks(reviewerUsername);
            if (feedbackList.isEmpty()) {
                feedbackListView.setPlaceholder(new Label("No feedback received yet."));
            } else {
                feedbackListView.setItems(FXCollections.observableArrayList(feedbackList));
            }
        } catch (Exception e) {
            showAlert("Error", "Could not load feedback: " + e.getMessage());
            e.printStackTrace();
        }

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> {
            new ReviewerProfilePage(dbHelper, reviewerUsername).show(stage);
        });

        root.getChildren().addAll(title, feedbackListView, backBtn);
        stage.setScene(new Scene(root, 500, 400));
        stage.setTitle("My Feedback");
        stage.show();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
