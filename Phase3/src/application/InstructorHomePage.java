package application;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * The InstructorHomePage class represents the main interface for an instructor.
 * It is responsible for initializing and displaying the instructor's home page.
 * @author eliza
 */
public class InstructorHomePage {
	private final DatabaseHelper databaseHelper;
	private final String currentUser;
	
	 /**
     * Constructs an InstructorHomePage with the specified database helper and current user.
     *
     * @param databaseHelper the helper object used to manage database operations.
     * @param currentUser the username of the currently logged in instructor.
     */
	public InstructorHomePage(DatabaseHelper databaseHelper, String currentUser) {
		this.databaseHelper = databaseHelper;
		this.currentUser = currentUser;
	}

	 /**
     * Displays the instructor home page on the provided stage.
     *
     * @param primaryStage the primary stage where the instructor home page will be displayed.
     */
	public void show(Stage primaryStage) {
		VBox layout = new VBox(10);
		layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

		Label userLabel = new Label("Hello, " + currentUser + " (Instructor)!");
		userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	     //Button to see reviewers list
        Button reviewerListBtn = new Button ("See Reviewers");
        Label l = new Label();
        l.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");
        reviewerListBtn.setOnAction(a -> {
        	databaseHelper.showReviewerList();
        });
        
		
		// Logout Button
		Button logoutButton = new Button("Logout");
		logoutButton.setOnAction(a -> {
			new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
		});
		
		//view reviews in QnA Page
		Button manageQAButton = new Button("View Q&A");
        // Non-admin user; pass 'false' for the isAdmin flag.
        manageQAButton.setOnAction(a -> {
            try {
            	Scene previousScene = primaryStage.getScene();
            	new QuestionsAnswersPage(databaseHelper, currentUser, currentUser, previousScene).show(primaryStage);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        });
		
		// View Review Requests Button
		Button viewReviewRequestsButton = new Button("View Review Requests");
		viewReviewRequestsButton.setOnAction(a -> {
			try {
				showReviewRequests(primaryStage);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		
		Button viewLogsButton = new Button("View Action Logs");
		viewLogsButton.setOnAction(e -> {
		    try {
		        databaseHelper.logAction(currentUser, "Instructor viewed action logs.");
		    } catch (SQLException ex) {
		        ex.printStackTrace();
		    }
		    new ActionLogPage(databaseHelper).show(primaryStage);
		});


		layout.getChildren().addAll(userLabel, logoutButton, manageQAButton, viewReviewRequestsButton, reviewerListBtn, viewLogsButton);
		Scene userScene = new Scene(layout, 800, 400);
		primaryStage.setScene(userScene);
		primaryStage.setTitle("Instructor Home");
		primaryStage.show();
	}

	// Show Review Requests in a TableView
	/**
	 * Manages layout of the page that allows instructors to view/accept/deny
	 * requests for reviewer role
	 * 
	 * @param primaryStage
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	private void showReviewRequests(Stage primaryStage) throws SQLException {
		// Create a TableView to show review requests
		TableView<ReviewRequest> tableView = new TableView<>();

		// Convert List<ReviewRequest> to ObservableList<ReviewRequest>
		ObservableList<ReviewRequest> observableReviewRequests = FXCollections
				.observableArrayList(databaseHelper.getAllReviewRequests());

		// Set the ObservableList to the TableView
		tableView.setItems(observableReviewRequests);

		// Define TableColumns for ReviewRequest fields
		TableColumn<ReviewRequest, String> studentColumn = new TableColumn<>("Student Username");
		studentColumn.setCellValueFactory(
				data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStudentUsername()));

		TableColumn<ReviewRequest, String> questionsColumn = new TableColumn<>("Questions & Answers");
		questionsColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("List of Q&A")); // Placeholder

		TableColumn<ReviewRequest, String> actionsColumn = new TableColumn<>("Actions");
		actionsColumn.setCellFactory(col -> new TableCell<ReviewRequest, String>() {

			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setText(null);
					setGraphic(null);
				} else {
					Button acceptButton = new Button("Accept");
					Button denyButton = new Button("Deny");

					acceptButton.setOnAction(e -> {
						try {
							int requestId = getTableRow().getItem().getId(); // Get the request ID
							String studentUsername = getTableRow().getItem().getStudentUsername(); // Get the student
																									// username
							boolean success = databaseHelper.acceptReviewRequest(requestId, studentUsername);
							if (success) {
								getTableView().getItems().remove(getTableRow().getItem()); // Remove the item from the
																							// table
							} else {
								// Handle error (e.g., role assignment failure)
							}
						} catch (SQLException ex) {
							ex.printStackTrace();
						}
					});

					denyButton.setOnAction(e -> {
						try {
							int requestId = getTableRow().getItem().getId(); // Get the request ID
							databaseHelper.denyReviewRequest(requestId);
							getTableView().getItems().remove(getTableRow().getItem()); // Remove the item from the table
						} catch (SQLException ex) {
							ex.printStackTrace();
						}
					});

					HBox actionsBox = new HBox(10, acceptButton, denyButton);
					setGraphic(actionsBox);
					setText(null);
				}
			}
		});

		// Create the 'Return to Instructor Home Page' button
		Button returnButton = new Button("Return to Home Page");
		returnButton.setOnAction(e -> {
			try {
				// Code to switch to InstructorHomePage (you can define the scene here)
				new InstructorHomePage(databaseHelper, "instructor").show(primaryStage);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

		tableView.getColumns().addAll(studentColumn, questionsColumn, actionsColumn);
		VBox layout = new VBox(10, tableView, returnButton);
		Scene scene = new Scene(layout, 800, 400);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Review Requests");
		primaryStage.show();
	}
}
