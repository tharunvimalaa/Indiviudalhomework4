package application;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * ReviewerProfilePage displays a reviewer's profile information, including their role
 * and the date since they became a reviewer, along with buttons to view their reviews,
 * feedback, and a list of all reviewers.
 */

public class ReviewerProfilePage {
	
	 private DatabaseHelper dbHelper;
	 private String reviewerUsername;
	 private String loggedInUsername;

	 private Label roleLabel;
	 private Label reviewerSinceLabel;
	 private Button viewReviewsBtn;
	 private Button viewFeedbacksBtn;

	  /**
	     * Constructs a ReviewerProfilePage with the specified database helper and reviewer username.
	     *
	     * @param dbHelper the DatabaseHelper instance used to manage database interactions.
	     * @param reviewerUsername the username of the reviewer whose profile is being displayed.
	     */
	 public ReviewerProfilePage(DatabaseHelper dbHelper, String reviewerUsername) {
		 this.dbHelper = dbHelper;
	     this.reviewerUsername = reviewerUsername;

	 }
	  
	 /**
	 * Displays the Reviewer Profile page.
	 *
	 * @param primaryStage.
	 */
	 
	 public void show(Stage primaryStage) {
		 VBox layout = new VBox(10);
		 layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
		 
		 Label userLabel = new Label ("Hello, " + reviewerUsername + " (Reviewer)!");
		 userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		 
		 
		 Button logoutBtn = new Button ("Logout");
		 logoutBtn.setOnAction(a -> {
			 new SetupLoginSelectionPage(dbHelper).show(primaryStage);
		 });
		 
	        Button reviewAnswersBtn = new Button("Review Answers");
	        reviewAnswersBtn.setOnAction(e -> {
	            try {
	            	Scene previousScene = primaryStage.getScene();
	                new QuestionsAnswersPage(dbHelper, reviewerUsername, reviewerUsername, previousScene).show(primaryStage);
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }
	        });

		 
		BorderPane root = new BorderPane();
	    root.setPadding(new Insets(15));
	        
	    VBox profileBox = new VBox(10);
	    profileBox.setPadding(new Insets(10));
	    profileBox.setAlignment(Pos.CENTER_LEFT);
	        
	     
	    roleLabel = new Label("Role: Reviewer");
	    reviewerSinceLabel = new Label("Reviewer since: Not available");

	    profileBox.getChildren().addAll(userLabel, roleLabel, reviewerSinceLabel);
	     
        HBox actionBtns = new HBox(20);
	    actionBtns.setPadding(new Insets(10));
	    actionBtns.setAlignment(Pos.CENTER);
	        
	    
	 // MODIFIED BY ISABELLA: implemented so that the buttons work now and are functional
	    
	    viewReviewsBtn = new Button("View My Reviews");
	    viewReviewsBtn.setOnAction(e -> {
	    	new ReviewerViewPage(dbHelper, reviewerUsername).show(primaryStage);
	    });

	    viewFeedbacksBtn = new Button("View My Feedbacks");
	    viewFeedbacksBtn.setOnAction(e -> {
	        new ReviewFeedbacks(dbHelper, reviewerUsername).show(primaryStage);
	    });
	        
	     //Button to see reviewers list
        Button reviewerListBtn = new Button ("See Reviewers");
        Label l = new Label();
        l.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");
        reviewerListBtn.setOnAction(a -> {
        	dbHelper.showReviewerList();
        });
        
        //Button to see trusted reviewers list
        Button trustedReviewerListBtn = new Button ("See Trusted Reviewers");
        Label tl = new Label();
        tl.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");
        trustedReviewerListBtn.setOnAction(a -> {
        	dbHelper.showTrustedReviewerList();
        });
        
	    
	    actionBtns.getChildren().addAll(reviewAnswersBtn, viewReviewsBtn, viewFeedbacksBtn, reviewerListBtn, trustedReviewerListBtn, logoutBtn);

        VBox mainContent = new VBox(20, profileBox, actionBtns);
        mainContent.setPadding(new Insets(15));
        mainContent.setAlignment(Pos.TOP_LEFT);
        
        root.setCenter(mainContent);
	        
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reviewer Profile");
        primaryStage.show();
	    
        loadProfile();
	    }
	    
	    /**
	     * Displays the experience and sets the reviewer since date, loads the reviewer profile from DB
	     */
	    private void loadProfile() {
	        try {
	            ReviewerProfile prof = dbHelper.getReviewerProfile(reviewerUsername);
	            if (prof == null) {
	            	
	            	prof = new ReviewerProfile();
	            	prof.setReviewerUsername(reviewerUsername);
	            	prof.setExperience("");
	            	dbHelper.addReviewerProfile(prof);
	            	
	            	prof = dbHelper.getReviewerProfile(reviewerUsername);
	            	
	            }
	            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
                String dateStr = sdf.format(prof.getCreatedDate());
                reviewerSinceLabel.setText("Reviewer since: " + dateStr);
                
	        } catch (SQLException e) {
	            showAlert("Error", "Failed to load profile: " + e.getMessage());
	        }
	    }

	    
	    /**
	     * Displays an alert with the specified title and message.
	     *
	     * @param title
	     * @param message
	     */
	    
	    private void showAlert(String t, String m) {
	        Alert alert = new Alert(Alert.AlertType.INFORMATION);
	        alert.setTitle(t);
	        alert.setHeaderText(null);
	        alert.setContentText(m);
	        alert.showAndWait();
	    }
	
}