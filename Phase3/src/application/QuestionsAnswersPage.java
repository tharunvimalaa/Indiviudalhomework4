package application;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.VBox;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * The QuestionsAnswersPage class provides the user interface for displaying questions and their
 * corresponding answers within the application. It allows users to view, interact with, and navigate
 * between questions and answers.
 */
public class QuestionsAnswersPage {
	private DatabaseHelper dbHelper;
	private Questions questionManager;
	private Answers answerManager;
	private Scene previousScene;

	private ListView<Question> questionListView;
	private ObservableList<Question> questionObservableList;

	private ListView<Answer> answerListView;
	private ObservableList<Answer> answerObservableList;

	// UI fields for questions
	private TextField questionIdField;
	private TextField questionTextField;

	// UI fields for searching keyword for questions
	private TextField searchBar;
	private Button searchButton;

	// UI fields for answers
	private TextField answerIdField;
	private TextField answerTextField;

	// Logged-in username and admin flag
	private String loggedInUsername;

	// listView for unresolved questions
	private ListView<Question> unresolvedList;
	
	// Class-level variables to store filter selections
	private List<String> selectedAuthors = new ArrayList<>();
	private boolean filterAnswered = false;
	private boolean filterUnanswered = false;
	
	private String currentUser;


	 /**
     * Constructs a QuestionsAnswersPage with the specified database helper, logged-in username,
     * current user, and a reference to the previous scene.
     *
     * @param dbHelper the database helper used for database operations.
     * @param loggedInUsername the username of the currently logged-in user.
     * @param currentUser the username of the user interacting with the page.
     * @param previousScene the previous scene to allow navigation back.
     */
	public QuestionsAnswersPage(DatabaseHelper dbHelper, String loggedInUsername, String currentUser, Scene previousScene) {
		this.dbHelper = dbHelper;
		this.loggedInUsername = loggedInUsername;
	    this.currentUser = currentUser;
	    this.previousScene = previousScene;

		questionManager = new Questions(dbHelper, answerManager);
		answerManager = new Answers(dbHelper);
		questionObservableList = FXCollections.observableArrayList();
		answerObservableList = FXCollections.observableArrayList();
	}


    /**
     * Displays the QuestionsAnswersPage on the provided primary stage.
     * This method initializes the UI components, retrieves necessary data, and handles any database operations
     * required for displaying the questions and answers.
     *
     * @param primaryStage the primary stage where the page will be displayed.
     * @throws SQLException if a database access error occurs during data retrieval or UI initialization.
     */
	public void show(Stage primaryStage) throws SQLException {
		// Load questions from the database
		try {
			List<Question> allQuestions = questionManager.getAllQuestions();
			questionObservableList.clear();
			questionObservableList.addAll(allQuestions);
		} catch (Exception e) {
			showAlert("Error", "Failed to load questions: " + e.getMessage());
		}
		// ---------------------------
		// Create Questions Section
		// ---------------------------
		Label questionsLabel = new Label("Questions");

		// Add search bar and button
		searchBar = new TextField();
		searchBar.setPromptText("Search for related questions...");
		searchButton = new Button("Search");
		searchButton.setOnAction(e -> searchRelatedQuestions());

		// Eliza: Added filter drop down (with check box)
		MenuButton menu = new MenuButton("Filter");
		Menu filter = new Menu("Options");

		// CheckMenuItems for question status
		CheckMenuItem answered = new CheckMenuItem("Answered Question");
		CheckMenuItem unanswered = new CheckMenuItem("Unanswered Question");

		// List of authors to filter from the database
		List<String> authors = dbHelper.fetchAuthors();

		// Create CheckMenuItem for each author and add to the filter menu
		for (String author : authors) {
		    CheckMenuItem authorItem = new CheckMenuItem(author);
		    authorItem.setOnAction(event -> {
		        // Update selected authors
		        updateSelectedAuthors(filter);
		        updateQuestionList(); // Refresh the question list
		    });
		    filter.getItems().add(authorItem);
		}

		// Add the answered and unanswered status filters
		answered.setOnAction(event -> {
		    filterAnswered = answered.isSelected();
		    updateQuestionList(); // Refresh the question list
		});
		unanswered.setOnAction(event -> {
		    filterUnanswered = unanswered.isSelected();
		    updateQuestionList(); // Refresh the question list
		});

		// Add all the filters to the menu
		filter.getItems().addAll(answered, unanswered);
		menu.getItems().add(filter);

		HBox searchBox = new HBox(10, searchBar, searchButton, menu);
		searchBox.setAlignment(Pos.CENTER);
		searchBox.setPadding(new Insets(10));

		questionListView = new ListView<>(questionObservableList);
		questionListView.setPrefHeight(150);
		questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				populateQuestionFields(newSelection);
				// If the selected question is a clarification, use the parent's id to load
				// answers.
				int id = (newSelection.getParentQuestionId() != null) ? newSelection.getParentQuestionId()
						: newSelection.getId();
				loadAnswersForQuestion(id);
			}
		});

		questionListView.setCellFactory(lv -> new ListCell<Question>() {
			@Override
			protected void updateItem(Question item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setStyle("");
				} else {
					// Check if this is a clarifying (follow-up) question
					if (item.getParentQuestionId() != null) {
						// Prepend a label and change the background color for clarification questions
						setText("Follow-up for ID " + item.getParentQuestionId() + ": " + item.getText());
						setStyle("-fx-background-color: lightblue;");
					} else {
						// Original questions
						setText(item.toString());
						setStyle("");
					}
				}
			}
		});

		questionIdField = new TextField();
		questionIdField.setPromptText("Question ID (auto)");
		questionIdField.setEditable(false);
		questionTextField = new TextField();
		questionTextField.setPromptText("Question Text");

		Button addQuestionBtn = new Button("Add Question");
		addQuestionBtn.setOnAction(e -> addQuestion());
		Button updateQuestionBtn = new Button("Update Question");
		updateQuestionBtn.setOnAction(e -> updateQuestion());
		Button deleteQuestionBtn = new Button("Delete Question");
		deleteQuestionBtn.setOnAction(e -> deleteQuestion());
		Button followUpQuestionBtn = new Button("Follow-Up Question");
		followUpQuestionBtn.setOnAction(e -> followUpQuestion());

		HBox questionButtons = new HBox(10, addQuestionBtn, updateQuestionBtn, deleteQuestionBtn, followUpQuestionBtn);
		VBox questionForm = new VBox(10, new Label("Manage Questions"), questionIdField, questionTextField,
				questionButtons);
		questionForm.setPadding(new Insets(10));
		questionForm.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5;");

		VBox questionsSection = new VBox(10, questionsLabel, searchBox, questionListView, questionForm);

		// ---------------------------
		// Create Answers Section
		// ---------------------------
		Label answerLabel = new Label("Answers for Selected Question");
		answerListView = new ListView<>(answerObservableList);

		answerListView.setCellFactory(lv -> new ListCell<Answer>() {
		    @Override
		    protected void updateItem(Answer item, boolean empty) {
		        super.updateItem(item, empty);
		        if (empty || item == null) {
		            setText(null);
		            setGraphic(null);
		        } else {
		            VBox vbox = new VBox(5);
		            // Display answer information (you may already have this part)
		            Label answerLabel = new Label(item.toString());
		            vbox.getChildren().add(answerLabel);

		            try {
		                // Retrieve the latest review for this answer
		                Review latestReview = dbHelper.getLatestReviewForAnswer(item.getId());
		                if (latestReview != null) {
		                    // Display the review details
		                    Label reviewLabel = new Label("Review by " + latestReview.getReviewerUsername() + ": " + latestReview.getText());
		                    reviewLabel.setWrapText(true);
		                    vbox.getChildren().add(reviewLabel);

		                    // Optional: If there is a previous review, add a button to view it.
		                    if (latestReview.getPreviousReviewId() != null) {
		                        Button viewOldReviewBtn = new Button("View Previous Review");
		                        viewOldReviewBtn.setOnAction(e -> {
		                            try {
		                                Review oldReview = dbHelper.getReviewById(latestReview.getPreviousReviewId());
		                                Alert dialog = new Alert(Alert.AlertType.INFORMATION);
		                                dialog.setTitle("Previous Review");
		                                dialog.setHeaderText("Original Review by " + oldReview.getReviewerUsername());
		                                dialog.setContentText(oldReview.getText());
		                                dialog.showAndWait();
		                            } catch (Exception ex) {
		                                ex.printStackTrace();
		                            }
		                        });
		                        vbox.getChildren().add(viewOldReviewBtn);
		                    }

		                    // If the current user is a student, add a "Send Feedback" button.
		                    if (dbHelper.getUserRolesList(currentUser).contains("student")) {
		                        Button sendFeedbackBtn = new Button("Send Feedback");
		                        sendFeedbackBtn.setOnAction(e -> {
		                            // Opens the FeedbackDialog, passing the review ID and current student's username.
		                            FeedbackDialog.showFeedbackDialog(dbHelper, latestReview.getId(), currentUser);
		                        });
		                        vbox.getChildren().add(sendFeedbackBtn);
		                    }

		                    // If the current user is a reviewer, add a "View Feedback" button.
		                    if (dbHelper.getUserRolesList(currentUser).contains("reviewer")) {
		                        Button viewFeedbackBtn = new Button("View Feedback");
		                        viewFeedbackBtn.setOnAction(e -> {
		                            // Opens the ReviewFeedbacks view for the reviewer's feedback.
		                            new ReviewFeedbacks(dbHelper, latestReview.getReviewerUsername()).show(primaryStage);
		                        });
		                        vbox.getChildren().add(viewFeedbackBtn);
		                    }
		                }
		            } catch (Exception ex) {
		                ex.printStackTrace();
		            }

		            setGraphic(vbox);
		        }
		    }

		});
		
		



		answerListView.setPrefHeight(150);
		answerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				populateAnswerFields(newSelection);
			}
		});

		answerIdField = new TextField();
		answerIdField.setPromptText("Answer ID (auto)");
		answerIdField.setEditable(false);
		answerTextField = new TextField();
		answerTextField.setPromptText("Answer Text");

		Button addAnswerBtn = new Button("Add Answer");
		addAnswerBtn.setOnAction(e -> addAnswer());
		Button updateAnswerBtn = new Button("Update Answer");
		updateAnswerBtn.setOnAction(e -> updateAnswer());
		Button deleteAnswerBtn = new Button("Delete Answer");
		deleteAnswerBtn.setOnAction(e -> deleteAnswer());

		Button resolveAnswerBtn = new Button("Mark as Resolved");
		resolveAnswerBtn.setOnAction(e -> {
			Answer selectedAnswer = answerListView.getSelectionModel().getSelectedItem();
			if (selectedAnswer == null) {
				showAlert("No Answer Selected", "Please select an answer to mark as resolved.");
				return;
			}
			try {
				dbHelper.markAnswerAsResolved(selectedAnswer.getId());
				dbHelper.logAction(loggedInUsername, "Marked answer ID " + selectedAnswer.getId() + " as resolved.");
				showAlert("Success", "Answer marked as resolved.");
				loadAnswersForQuestion(selectedAnswer.getQuestionId()); // Refresh
			} catch (SQLException ex) {
				showAlert("Error", "Failed to mark answer as resolved.");
			}
		});

		Button sortByResolvedBtn = new Button("Sort by Resolved %");
		sortByResolvedBtn.setOnAction(e -> {
			try {
				int questionId = questionListView.getSelectionModel().getSelectedItem().getId();
				List<Answer> sortedAnswers = dbHelper.getSortedResolvedAnswers(questionId);
				answerObservableList.clear();
				answerObservableList.addAll(sortedAnswers);
			} catch (Exception ex) {
				showAlert("Error", "Failed to sort answers by resolve percentage.");
			}
		});

		HBox answerButtons = new HBox(10, addAnswerBtn, updateAnswerBtn, deleteAnswerBtn);
		
		Button favoriteAnswerBtn = new Button("Favorite Answer"); // Nathaniel: favorite answer button
		favoriteAnswerBtn.setOnAction(e -> favoriteAnswer());
		Button trustAnswerBtn = new Button("Trust Reviewer"); // Nathaniel: favorite answer button
		trustAnswerBtn.setOnAction(e -> trustAnswer());
		HBox sortButtons = new HBox(10, resolveAnswerBtn, sortByResolvedBtn, favoriteAnswerBtn, trustAnswerBtn);

		VBox answerForm = new VBox(10, new Label("Manage Answers"), answerIdField, answerTextField, answerButtons, sortButtons);
		answerForm.setPadding(new Insets(10));
		answerForm.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5;");

		VBox answersSection = new VBox(10, answerLabel, answerListView, answerForm);

		// ---------------------------
		// Main Layout & Navigation
		// ---------------------------

		// Zakia: list of unread answers
		Button unreadBtn = new Button("Show Unread Answers");
		unreadBtn.setOnAction(e -> {
			List<Answer> unread = answerManager.getUnreadAnswerFromDB(loggedInUsername);

			if (unread.isEmpty()) {
				Alert al = new Alert(Alert.AlertType.INFORMATION);
				al.setTitle("Unread Answers");
				al.setHeaderText(null);
				al.setContentText("You don't have any unread answers.");
				al.showAndWait();
			} else {

				StringBuilder sb = new StringBuilder();
				sb.append("You have ").append(unread.size()).append(" unread answers: \n\n");
				List<Integer> unreadIds = new ArrayList<>();

				for (Answer a : unread) {
					sb.append("ID ").append(a.getId()).append(": ").append(a.getText()).append("\n");
					unreadIds.add(a.getId());
				}

				Alert al = new Alert(AlertType.INFORMATION);
				al.setTitle("Unread Answers");
				al.setHeaderText(null);
				al.setContentText(sb.length() > 0 ? sb.toString() : "No unread answers found");
				al.showAndWait();

				answerManager.markAnswerAsRead(unreadIds);
			}
		});

		// Zakia: button for showing unresolved questions
		Button unresolvedBtn = new Button("Show Unresolved Questions");
		unresolvedBtn.setOnAction(e -> {
			List<Question> unresolved = null;
			try {
				unresolved = questionManager.getUnresolvedQuestionsForStudent(loggedInUsername);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			StringBuilder sb = new StringBuilder();

			for (Question q : unresolved) {
				sb.append(q.getText()).append("\n");
			}

			Alert al = new Alert(AlertType.INFORMATION);
			al.setTitle("Unresolved Questions");
			al.setHeaderText(null);
			al.setContentText(sb.length() > 0 ? sb.toString() : "No unresolved question found");
			al.showAndWait();

			unresolvedList.getItems().clear();
			unresolvedList.getItems().addAll(unresolved);

		});
		unresolvedList = new ListView<>();
		unresolvedList.setPrefHeight(150);
		unresolvedList.setPlaceholder(new javafx.scene.control.Label("No Unresolved Questions"));

		// Button to show potential answers for a slected question
		Button potentialBtn = new Button("Show Potential Answers");
		potentialBtn.setOnAction(e -> {
			Question selectedQ = unresolvedList.getSelectionModel().getSelectedItem();

			if (selectedQ != null) {
				List<Answer> potentialAns = new ArrayList<>();

				try {
					potentialAns = dbHelper.getPotentialAns(selectedQ.getId());

				} catch (Exception e1) {
					showAlert("Error", "Failed to load potential answers: " + e1.getMessage());
				}

				StringBuilder sb = new StringBuilder();

				for (Answer a : potentialAns) {
					sb.append(a.getText()).append("\n");
				}

				Alert al = new Alert(AlertType.INFORMATION);
				al.setTitle("Potential Answers");
				al.setHeaderText("For Question: " + selectedQ.getText());
				al.setContentText(sb.length() > 0 ? sb.toString() : "No potential answers found");
				al.showAndWait();
			} else {
				showAlert("No Selection made", "Please selected an question first");
			}

		});

		HBox btnsBox = new HBox(10, unreadBtn, unresolvedBtn, potentialBtn);
		btnsBox.setAlignment(Pos.CENTER);
		btnsBox.setPadding(new Insets(10));

		VBox content = new VBox(10);
		content.setPadding(new Insets(10));
		Label unresolvedLbl = new Label("Select an unresovled question to see potential answers: ");
		content.getChildren().addAll(btnsBox, unresolvedLbl, unresolvedList);

		Button backButton = new Button("Back to Home");
		backButton.setOnAction(e -> {
			primaryStage.setScene(previousScene);
		});

		HBox mainContent = new HBox(20, questionsSection, answersSection);
		mainContent.setPadding(new Insets(10));

		BorderPane root = new BorderPane();

		root.setTop(content);

		root.setCenter(mainContent);
		root.setBottom(backButton);
		BorderPane.setMargin(backButton, new Insets(10));
		BorderPane.setAlignment(backButton, Pos.CENTER);

		Scene scene = new Scene(root, 900, 600);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Manage Questions and Answers");
		primaryStage.show();
	}

	// Zakia: mothod dahsboardPanel to show unread, unresolved answers and questions
	private VBox createDashboard() throws SQLException {
		VBox dashPanel = new VBox(10);
		dashPanel.setSpacing(10);
		dashPanel.setPadding(new Insets(10));
		dashPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: gray; -fx-border-width: 1;");

		Label summary;
		VBox questionContainer = new VBox(5);

		try {
			Questions qm = new Questions(dbHelper, answerManager);
			List<Question> unresolved = qm.getUnresolvedQuestionsForStudent(loggedInUsername);
			int totalUnreadQ = 0;

			for (Question q : unresolved) {

				List<Answer> ansForQ = dbHelper.getAnswersForQuestionFromDB(q.getId());
				List<Answer> potentialAnsForQ = new ArrayList<>();

				for (Answer a : ansForQ) {
					if (a.isFavorite()) {
						potentialAnsForQ.add(a);

						if (!a.isRead()) {
							totalUnreadQ++;
						}
					}
				}

				if (!potentialAnsForQ.isEmpty()) {

					Label ql = new Label("Questions: " + q.getId());
					ql.setStyle("-fx-font-weight: bold;");
					questionContainer.getChildren().add(ql);

					for (Answer a : potentialAnsForQ) {
						String status = a.isRead() ? "read" : "unread";
						Label al = new Label("\tPotential Answer (" + status + "): " + a.getText());
						questionContainer.getChildren().add(al);
					}
				}
			}
			summary = new Label("Dashboard: You have " + totalUnreadQ + " unread answers");
		} catch (Exception e) {
			summary = new Label("Error loading the dashboard: " + e.getMessage());
		}

		dashPanel.getChildren().addAll(summary, questionContainer);
		return dashPanel;
	}

	// ----- Helper Methods -----
	private void populateQuestionFields(Question q) {
		questionIdField.setText(String.valueOf(q.getId()));
		questionTextField.setText(q.getText());
	}

	private void populateAnswerFields(Answer a) {
		answerIdField.setText(String.valueOf(a.getId()));
		answerTextField.setText(a.getText());
	}

	private void loadAnswersForQuestion(int questionId) {
		try {
			List<Answer> answersForQuestion = answerManager.getAnswersForQuestion(questionId);
			answerObservableList.clear();
			answerObservableList.addAll(answersForQuestion);
		} catch (Exception e) {
			showAlert("Error", "Failed to load answers: " + e.getMessage());
		}
	}

	// MODIFIED by Isabella Tunon-Robinson: Searches for questions in the database
	// that contain the keyword entered by the user

	private void searchRelatedQuestions() {
		// get the keyword from the search bar and trim any spaces
		String keyword = searchBar.getText().trim();

		if (keyword.isEmpty()) { // if empty, shows an error
			showAlert("Search Error", "Please enter a keyword to search for related questions.");
			return;
		}

		try {
			// Search for questions containing the keyword using the DatabaseHelper
			List<Question> relatedQuestions = dbHelper.searchQuestionsByKeyword(keyword);
			if (relatedQuestions.isEmpty()) {
				showAlert("No Results", "No related questions found for the keyword: " + keyword);
			} else {
				// Display the related questions in gui
				showRelatedQuestionsDialog(relatedQuestions);
			}
		} catch (SQLException ex) {
			showAlert("Database Error", "Failed to search for related questions: " + ex.getMessage());
		}
	}

	// Displays a dialog window showing the list of related questions.

	private void showRelatedQuestionsDialog(List<Question> relatedQuestions) {

		// makes new dialog window
		Stage dialogStage = new Stage();
		dialogStage.setTitle("Related Questions");

		// ListView displays related questions
		ListView<Question> relatedQuestionsList = new ListView<>(FXCollections.observableArrayList(relatedQuestions));

		// ListView displays the question text
		relatedQuestionsList.setCellFactory(lv -> new ListCell<Question>() {
			@Override
			protected void updateItem(Question item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.getText());
				}
			}
		});

		VBox dialogVBox = new VBox(10, new Label("Related Questions:"), relatedQuestionsList);
		dialogVBox.setPadding(new Insets(10));

		Scene dialogScene = new Scene(dialogVBox, 400, 300);
		dialogStage.setScene(dialogScene);
		dialogStage.show();
	}

	// ----- CRUD Operations for Questions -----
	private void addQuestion() {
		try {
			String text = questionTextField.getText();
			if (text == null || text.trim().isEmpty()) {
				throw new IllegalArgumentException("Question text cannot be empty.");
			}
			// Automatically use the logged-in username.
			Question q = new Question(0, text, loggedInUsername);
			questionManager.addQuestion(q);
			questionObservableList.add(q);
			dbHelper.logAction(loggedInUsername, "Added new question: \"" + text + "\"");
			clearQuestionFields();
		} catch (IllegalArgumentException ex) {
			showAlert("Error Adding Question", ex.getMessage());
		} catch (Exception ex) {
			showAlert("Error Adding Question", ex.getMessage());
		}
	}

	private void updateQuestion() {
		try {
			// Check if a question is selected
			if (questionListView.getSelectionModel().getSelectedItem() == null
					|| questionIdField.getText().trim().isEmpty()) {
				throw new IllegalArgumentException("No question selected. Please select a question to update.");
			}
			int id = Integer.parseInt(questionIdField.getText());
			String newText = questionTextField.getText();
			if (newText == null || newText.trim().isEmpty()) {
				throw new IllegalArgumentException("Question text cannot be empty.");
			}
			questionManager.updateQuestion(id, newText, loggedInUsername);
			// Refresh the list
			questionObservableList.clear();
			questionObservableList.addAll(questionManager.getAllQuestions());
			dbHelper.logAction(loggedInUsername, "Updated question ID " + id + " to new text: \"" + newText + "\"");
			clearQuestionFields();
		} catch (Exception ex) {
			showAlert("Error Updating Question", ex.getMessage());
		}
	}

	private void deleteQuestion() {
		try {
			// Check if a question is selected
			if (questionListView.getSelectionModel().getSelectedItem() == null
					|| questionIdField.getText().trim().isEmpty()) {
				throw new IllegalArgumentException("No question selected. Please select a question to delete.");
			}
			int id = Integer.parseInt(questionIdField.getText());
			questionManager.deleteQuestion(id, loggedInUsername);
			questionObservableList.clear();
			questionObservableList.addAll(questionManager.getAllQuestions());
			dbHelper.logAction(loggedInUsername, "Deleted question ID " + id);
			answerObservableList.clear();
			clearQuestionFields();
		} catch (Exception ex) {
			showAlert("Error Deleting Question", ex.getMessage());
		}
	}

	private void followUpQuestion() {
		// Ensure a question is selected to follow up on
		if (questionListView.getSelectionModel().getSelectedItem() == null) {
			showAlert("Error", "No question selected. Please select a question to follow up.");
			return;
		}
		Question parentQuestion = questionListView.getSelectionModel().getSelectedItem();

		// If the selected question is already a clarification, do not allow another
		// clarification.
		if (parentQuestion.getParentQuestionId() != null) {
			showAlert("Error", "You cannot ask a clarification for a clarifying question.");
			return;
		}

		// Open a dialog for the student to type their clarifying (follow-up) question
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Clarification Question");
		dialog.setHeaderText("Follow up on the selected question:");
		dialog.setContentText("Your clarifying question:");
		dialog.getEditor().setPromptText("Enter your clarifying question here...");

		dialog.showAndWait().ifPresent(followUpText -> {
			if (followUpText.trim().isEmpty()) {
				showAlert("Error", "Clarification question text cannot be empty.");
				return;
			}
			// Create a new Question object with parentQuestionId set to the selected
			// question's id
			Question followUpQuestion = new Question(0, followUpText, loggedInUsername, parentQuestion.getId());
			questionManager.addQuestion(followUpQuestion);
			questionObservableList.add(followUpQuestion);
		});
	}

	// ----- CRUD Operations for Answers -----
	private void addAnswer() {
		try {
			// Ensure a question is selected before adding an answer
			if (questionListView.getSelectionModel().getSelectedItem() == null
					|| questionIdField.getText().trim().isEmpty()) {
				throw new IllegalArgumentException(
						"No question selected. Please select a question before adding an answer.");
			}

			// Retrieve the selected question
			Question selectedQuestion = questionListView.getSelectionModel().getSelectedItem();

			// If the selected question is a follow-up (clarification), use its parent's id
			// for answering.
			int questionId;
			if (selectedQuestion.getParentQuestionId() != null) {
				// Answer the original question instead of the clarification
				questionId = selectedQuestion.getParentQuestionId();
			} else {
				questionId = selectedQuestion.getId();
			}

			String text = answerTextField.getText();
			if (text == null || text.trim().isEmpty()) {
				throw new IllegalArgumentException("Answer text cannot be empty.");
			}

			Answer a = new Answer(0, questionId, text, loggedInUsername);
			answerManager.addAnswer(a);
			dbHelper.logAction(loggedInUsername, "Added answer to question ID " + questionId + ": \"" + text + "\"");
			answerObservableList.add(a);
			clearAnswerFields();
		} catch (IllegalArgumentException ex) {
			showAlert("Error Adding Answer", ex.getMessage());
		} catch (Exception ex) {
			showAlert("Error Adding Answer", ex.getMessage());
		}
	}

	private void updateAnswer() {
		try {
			// Check if an answer is selected
			if (answerListView.getSelectionModel().getSelectedItem() == null
					|| answerIdField.getText().trim().isEmpty()) {
				throw new IllegalArgumentException("No answer selected. Please select an answer to update.");
			}
			int id = Integer.parseInt(answerIdField.getText());
			String newText = answerTextField.getText();
			if (newText == null || newText.trim().isEmpty()) {
				throw new IllegalArgumentException("Answer text cannot be empty.");
			}
			answerManager.updateAnswer(id, newText, loggedInUsername);
			// Refresh the answer list
			int questionId = Integer.parseInt(questionIdField.getText());
			answerObservableList.clear();
			answerObservableList.addAll(answerManager.getAnswersForQuestion(questionId));
			dbHelper.logAction(loggedInUsername, "Updated answer ID " + id + " to: \"" + newText + "\"");
			clearAnswerFields();
		} catch (Exception ex) {
			showAlert("Error Updating Answer", ex.getMessage());
		}
	}

	private void deleteAnswer() {
		try {
			// Check if an answer is selected
			if (answerListView.getSelectionModel().getSelectedItem() == null
					|| answerIdField.getText().trim().isEmpty()) {
				throw new IllegalArgumentException("No answer selected. Please select an answer to delete.");
			}
			int id = Integer.parseInt(answerIdField.getText());
			answerManager.deleteAnswer(id, loggedInUsername);
			int questionId = Integer.parseInt(questionIdField.getText());
			answerObservableList.clear();
			answerObservableList.addAll(answerManager.getAnswersForQuestion(questionId));
			dbHelper.logAction(loggedInUsername, "Deleted answer ID " + id);
			clearAnswerFields();
		} catch (Exception ex) {
			showAlert("Error Deleting Answer", ex.getMessage());
		}
	}

	// Nathaniel: for handling favorites
	private void favoriteAnswer() {
		try {
			// Check if an answer is selected
			if (answerListView.getSelectionModel().getSelectedItem() == null
					|| answerIdField.getText().trim().isEmpty()) {
				throw new IllegalArgumentException("No answer selected. Please select an answer to favorite.");
			}
			int id = Integer.parseInt(answerIdField.getText());
			answerManager.addFavorite(id);
			// Refresh the answer list
			int questionId = Integer.parseInt(questionIdField.getText());
			answerObservableList.clear();
			answerObservableList.addAll(answerManager.getAnswersForQuestion(questionId));
			clearAnswerFields();
		} catch (Exception ex) {
			showAlert("Error Deleting Answer", ex.getMessage());
		}
	}
	
	private void trustAnswer() {
		try {
			// Check if an answer is selected
			if (answerListView.getSelectionModel().getSelectedItem() == null
					|| answerIdField.getText().trim().isEmpty()) {
				throw new IllegalArgumentException("No answer selected. Please select a review to trust.");
			}
			int id = answerListView.getSelectionModel().getSelectedItem().getId();
			List<Review> reviewList = dbHelper.getReviewsForAnswer(id);
			if (!reviewList.isEmpty()) {
				Review review = reviewList.get(0);
				System.out.println(review.getReviewerUsername());
				ReviewerProfile reviewer = dbHelper.getReviewerProfile(review.getReviewerUsername());
				if (reviewer != null) {
					reviewer.setWeight(1);
					dbHelper.updateReviewerProfile(reviewer);
					System.out.println(reviewer.getWeight());
				} else {
					System.out.println("No reviewer with that username");
				}
			} else {
				System.out.println("No reviewers in list");
			}
			// Refresh the answer list
			int answerId = Integer.parseInt(questionIdField.getText());
			answerObservableList.clear();
			answerObservableList.addAll(answerManager.getAnswersForQuestion(answerId));
			clearAnswerFields();
		} catch (Exception ex) {
			showAlert("Error Deleting Answer", ex.getMessage());
		}
	}

	// ----- Utility Methods -----
	private void clearQuestionFields() {
		questionIdField.clear();
		questionTextField.clear();
	}

	private void clearAnswerFields() {
		answerIdField.clear();
		answerTextField.clear();
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void updateQuestionList() {
		// Fetch all questions from the database
		List<Question> allQuestions = questionManager.getAllQuestions();
		List<Question> filtered = allQuestions.stream()
		        .filter(q -> {
		            // Check if the question matches any of the selected authors
		            boolean matchesAuthor = selectedAuthors.isEmpty() || selectedAuthors.contains(q.getUserName());

		            // Check if the question is answered or unanswered based on the status filters
		            boolean matchesStatus = (filterAnswered && hasAnswerForQuestion(q.getId())) || 
		                                    (filterUnanswered && !hasAnswerForQuestion(q.getId()));

		            return matchesAuthor && matchesStatus;
		        })
		        .collect(Collectors.toList());

		// Update the observable list
		questionObservableList.setAll(filtered);
	}

	// Helper method to check if the question has answers
	private boolean hasAnswerForQuestion(int questionId) {
		try {
	        // Fetch answers for the given question ID from the database
	        List<Answer> answers = dbHelper.fetchAnswersByQuestionId(questionId);
	        return !answers.isEmpty();
	    } catch (SQLException e) {
	        showAlert("Error", "Failed to fetch answers: " + e.getMessage());
	        return false;
	    }
	}
	
	private void updateSelectedAuthors(Menu filter) {
	    selectedAuthors.clear(); // Clear existing selections

	    // Add all selected authors to the list
	    for (MenuItem item : filter.getItems()) {
	        if (item instanceof CheckMenuItem) {
	            CheckMenuItem checkMenuItem = (CheckMenuItem) item;

	            // Check if the item is selected and it is not a status filter (answered/unanswered)
	            if (checkMenuItem.isSelected() && !checkMenuItem.getText().equals("Answered Question") && !checkMenuItem.getText().equals("Unanswered Question")) {
	                selectedAuthors.add(checkMenuItem.getText()); // Add the selected author
	            }
	        }
	    }
	}


}
