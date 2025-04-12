package application;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Displays a welcome message and navigation options.
 */
public class UserHomePage {

    private final DatabaseHelper databaseHelper;
    private final String currentUser;

    public UserHomePage(DatabaseHelper databaseHelper, String currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label userLabel = new Label("Hello, " + currentUser + "!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(a -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        Button manageQAButton = new Button("Manage Q&A");
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

        //Button to see reviewers list
        Button reviewerListBtn = new Button ("See Reviewers");
        Label l = new Label();
        l.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");
        reviewerListBtn.setOnAction(a -> {
        	databaseHelper.showReviewerList();
        });

        //Button to see trusted reviewers list
        Button trustedReviewerListBtn = new Button ("See Trusted Reviewers");
        Label tl = new Label();
        tl.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");
        trustedReviewerListBtn.setOnAction(a -> {
        	databaseHelper.showTrustedReviewerList();
        });
        
        layout.getChildren().addAll(userLabel, logoutButton, manageQAButton, reviewerListBtn, trustedReviewerListBtn);
        Scene userScene = new Scene(layout, 800, 600);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
        primaryStage.show();
    }
}
