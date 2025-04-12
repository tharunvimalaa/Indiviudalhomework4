package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewerViewPage {

    private DatabaseHelper dbHelper;
    private String reviewerUsername;

    public ReviewerViewPage(DatabaseHelper dbHelper, String reviewerUsername) {
        this.dbHelper = dbHelper;
        this.reviewerUsername = reviewerUsername;
    }

    public void show(Stage stage) {
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10;");

        Label title = new Label("My Reviews"); //shows the reviews by the reviewer, only reviewers can see this page
        ListView<String> reviewListView = new ListView<>();
//MODIFICATION BY ISABELLA
        try {
            List<Review> reviewList = dbHelper.getReviewerViewPage(reviewerUsername);

            if (reviewList.isEmpty()) {
                reviewListView.setPlaceholder(new Label("No reviews found."));
            } else {

                List<String> displayList = reviewList.stream()
                        .map(r -> String.format("Review ID: %d\nAnswer ID: %d\nReview: %s\nDate: %s",
                                r.getId(), r.getAnswerId(), r.getText(), r.getCreatedAt().toString()))
                        .collect(Collectors.toList());

                reviewListView.setItems(FXCollections.observableArrayList(displayList));
            }
        } catch (Exception e) {
            showAlert("Error", "Could not load reviews: " + e.getMessage());
            e.printStackTrace();
        }

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> {
            new ReviewerProfilePage(dbHelper, reviewerUsername).show(stage);
        });

        root.getChildren().addAll(title, reviewListView, backBtn);
        stage.setScene(new Scene(root, 500, 400));
        stage.setTitle("My Reviews");
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
