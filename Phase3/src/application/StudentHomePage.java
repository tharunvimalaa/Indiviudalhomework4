package application;

import java.sql.SQLException;
import java.sql.SQLException;
import java.sql.Timestamp; // Import Timestamp
import java.time.LocalDateTime; // Import LocalDateTime
import java.time.format.DateTimeFormatter; // Import Formatter
import java.util.List; // Import List
import application.Review; // Import Review class
import javafx.scene.control.ListView; // Import ListView
import javafx.collections.FXCollections; // Import FXCollections

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StudentHomePage {
	private final DatabaseHelper databaseHelper;
	private final String currentUser;

	public StudentHomePage(DatabaseHelper databaseHelper, String currentUser) {
		this.databaseHelper = databaseHelper;
		this.currentUser = currentUser;
	}

	public void show(Stage primaryStage) {
		VBox layout = new VBox(10);
		layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

		Label userLabel = new Label("Hello, " + currentUser + " (Student)!");
		userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		// Logout Button
		Button logoutButton = new Button("Logout");
		logoutButton.setOnAction(a -> {
			new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
		});

		// QA Button
		Button manageQAButton = new Button("View Q&A");
		manageQAButton.setOnAction(a -> {
			try {
				Scene previousScene = primaryStage.getScene();
				new QuestionsAnswersPage(databaseHelper, currentUser, currentUser, previousScene).show(primaryStage);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		// Button to see reviewers list
		Button reviewerListBtn = new Button("See Reviewers");
		Label l = new Label();
		l.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");
		reviewerListBtn.setOnAction(a -> {
			databaseHelper.showReviewerList();
		});

		// Button to see trusted reviewers list
		Button trustedReviewerListBtn = new Button("See Trusted Reviewers");
		Label tl = new Label();
		tl.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");
		trustedReviewerListBtn.setOnAction(a -> {
			databaseHelper.showTrustedReviewerList();
		});

		/**
		 * Button for students to request for reviewer role from instructor
		 */
		Button requestReviewerButton = new Button("Request Reviewer Role");
		Label request = new Label();
		request.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");
		requestReviewerButton.setOnAction(a -> {
			try {
				databaseHelper.addReviewRequest(currentUser); // Request review role
				request.setText("Request to become a reviewer has been sent.");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		Label updatesTitle = new Label("Recent Updates from Trusted Reviewers:");
		updatesTitle.setStyle("-fx-font-weight: bold;");
		ListView<String> updatesListView = new ListView<>();
		updatesListView.setPlaceholder(new Label("No recent updates found.")); // Message if list is empty
		updatesListView.setPrefHeight(150); // Give it some default height

		try {
			// 1. Get placeholder trusted reviewers
			List<String> trusted = databaseHelper.getPlaceholderTrustedReviewers(currentUser);

			// 2. Define "recent" (e.g., last 7 days)
			Timestamp cutoff = Timestamp.valueOf(LocalDateTime.now().minusDays(365));

			// 3. Fetch recent updates
			List<Review> recentUpdates = databaseHelper.getRecentUpdatesByReviewers(trusted, cutoff);

			// 4. Populate the ListView
			if (!recentUpdates.isEmpty()) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				List<String> updateStrings = new java.util.ArrayList<>();
				for (Review review : recentUpdates) {
					String formattedDate = review.getCreatedAt().toLocalDateTime().format(formatter);
					String updateText = String.format("Update by %s (Answer ID: %d) on %s: %s",
							review.getReviewerUsername(), review.getAnswerId(), formattedDate,
							// Show a snippet of the review text if it's long
							review.getText().length() > 50 ? review.getText().substring(0, 50) + "..."
									: review.getText());
					// Indicate if it's an update to a previous review
					if (review.getPreviousReviewId() != null && review.getPreviousReviewId() > 0) {
						updateText += " (Updated)";
					}
					updateStrings.add(updateText);
				}
				updatesListView.setItems(FXCollections.observableArrayList(updateStrings));
			}

		} catch (SQLException e) {
			e.printStackTrace();
			// Optionally show an error message in the UI
			updatesListView.setPlaceholder(new Label("Error loading updates."));
		}

		layout.getChildren().addAll(userLabel, logoutButton, manageQAButton, requestReviewerButton, request,
				reviewerListBtn, trustedReviewerListBtn);
		Scene userScene = new Scene(layout, 800, 400);
		primaryStage.setScene(userScene);
		primaryStage.setTitle("Student Home");
		primaryStage.show();
	}

}
