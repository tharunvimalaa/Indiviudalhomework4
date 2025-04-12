package databasePart1;

import application.Question;
import application.ReviewRequest;
import application.ReviewerProfile;
import application.Review;
import application.ActionLog;

//import application.ResultSet;
import application.Answer;
import application.Feedback;
//import application.PreparedStatement;
import application.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseHelper {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "org.h2.Driver";
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";

	// Database credentials
	static final String USER = "sa";
	static final String PASS = "";

	private Connection connection = null;
	private Statement statement = null;
	


	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement();

//			statement.execute("DROP ALL OBJECTS");
			
			createTables(); // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}
	
	
	/**
     * Fetches recent reviews created after a specific cutoff date by a list of specified reviewers.
     * Includes reviews linked via previousReviewId to trace updates.
     *
     * @param trustedReviewerUsernames List of usernames for trusted reviewers.
     * @param cutoffDate Timestamp defining the start date for "recent" reviews.
     * @return List of recent Review objects.
     * @throws SQLException
     */
    public List<Review> getRecentUpdatesByReviewers(List<String> trustedReviewerUsernames, Timestamp cutoffDate) throws SQLException {
        List<Review> recentUpdates = new ArrayList<>();
        if (trustedReviewerUsernames == null || trustedReviewerUsernames.isEmpty()) {
            return recentUpdates; // No trusted reviewers, no updates.
        }

        // Build the IN clause string (?, ?, ...)
        String placeholders = String.join(",", java.util.Collections.nCopies(trustedReviewerUsernames.size(), "?"));

        // Query for reviews created after the cutoff OR reviews whose *previous* review was created before the cutoff
        // This helps catch the *first* update after the cutoff even if the update itself was slightly before.
        // Ordering by creation date ensures we show the latest first.
         String sql = "SELECT * FROM reviews WHERE reviewerUsername IN (" + placeholders + ") " +
                      "AND createdAt >= ? " + // Select reviews created after the cutoff
                      "ORDER BY createdAt DESC"; // Show newest first


        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int index = 1;
            // Set the reviewer usernames in the prepared statement
            for (String username : trustedReviewerUsernames) {
                stmt.setString(index++, username);
            }
            // Set the cutoff date
            stmt.setTimestamp(index, cutoffDate);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recentUpdates.add(mapReview(rs)); // Use the existing mapReview helper
            }
        }
        return recentUpdates;
    }

	// --- PLACEHOLDER METHOD ---
	// TODO: Replace this with actual implementation fetching trusted reviewers for the specific student (Nathaniel's task)
	public List<String> getPlaceholderTrustedReviewers(String studentUsername) {
	    // For testing purposes, assume every student trusts 'reviewer_bella' and 'reviewer_zakia'
	    // In a real scenario, this would query a specific list associated with studentUsername.
	    System.out.println("WARNING: Using placeholder trusted reviewers for " + studentUsername);
	    // Add usernames of existing users who ARE reviewers based on your test data
	    return java.util.Arrays.asList("review");
	}
	
	// MODIFIED by Isabella Tunon-Robinson: searches through the question database for questions that contain the searched keyword.

	public List<Question> searchQuestionsByKeyword(String keyword) throws SQLException {
		List<Question> list = new ArrayList<>();

		// SQL query finds questions where the text contains the keyword
		String query = "SELECT * FROM Questions WHERE text LIKE ?";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			// (%) to match any text containing the keyword
			pstmt.setString(1, "%" + keyword + "%"); // searches for questions with the keyword

			ResultSet rs = pstmt.executeQuery(); // retrieve the results

			// create Question objects
			while (rs.next()) {
				int id = rs.getInt("id");
				String text = rs.getString("text");
				String userName = rs.getString("userName");

				// Checks if the question is a follow-up question by retrieving parentQuestionId

				Integer parentId = (rs.getObject("parentQuestionId") != null) ? rs.getInt("parentQuestionId") : null;

				// new Question object and add it to the list
				Question q = new Question(id, text, userName, parentId);
				list.add(q);
			}
		}
		return list;
	}

	
    private void createTables() throws SQLException {
        // MODIFIED: Increase the VARCHAR length for 'role' so that it can hold multiple
        // roles.
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, " + "password VARCHAR(255), " + "role VARCHAR(255), "
                + "email VARCHAR(255)" + ")";
        statement.execute(userTable);

        // Attempt to alter the column in case it was created with a smaller size
        // before.
        // This one is likely fine, or might give a harmless error if already altered.
        try {
            statement.execute("ALTER TABLE cse360users ALTER COLUMN role VARCHAR(255)");
        } catch (SQLException e) {
            System.out.println("Column 'role' alteration (if needed): " + e.getMessage());
        }

        // MODIFIED BY ISABELLA: creates the 'reviews' table if it doesn't exist
        // The definition correctly includes previousReviewId and createdAt
        String reviewsTable = "CREATE TABLE IF NOT EXISTS reviews (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "answerId INT NOT NULL, " +
                "reviewerUsername VARCHAR(255) NOT NULL, " +
                "text TEXT NOT NULL, " +
                "previousReviewId INT, " +  // Column is defined here
                "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + // Column is defined here
                "feedback TEXT" + 
                ")";


        statement.execute(reviewsTable); // Creates the table correctly now


        // Create the invitation codes table (modified code length for appended role
        // info)
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes (" + "code VARCHAR(50) PRIMARY KEY, "
                + "isUsed BOOLEAN DEFAULT FALSE)";
        statement.execute(invitationCodesTable);

        // --- NEW: Create Questions Table ---
        String questionsTable = "CREATE TABLE IF NOT EXISTS Questions (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "text VARCHAR(255) NOT NULL, " + "userName VARCHAR(255) NOT NULL, " + "parentQuestionId INT,"
                + "createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP" + ")";
        statement.execute(questionsTable);

        // --- NEW: Create Answers Table ---
        String answersTable = "CREATE TABLE IF NOT EXISTS Answers (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "questionId INT NOT NULL, " + "text VARCHAR(255) NOT NULL, " + "userName VARCHAR(255) NOT NULL, "
                + "createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + "isAccepted BOOLEAN DEFAULT FALSE, "
                + "isFavorite BOOLEAN DEFAULT FALSE, " + "isRead BOOLEAN DEFAULT FALSE" + ")";
        statement.execute(answersTable);

        // NEW: Create ReviewerProfile Table
        statement.execute("DROP TABLE IF EXISTS ReviewerProfiles");
        String reviewerProfileTable = "CREATE TABLE IF NOT EXISTS ReviewerProfiles ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, " + "reviewerUsername VARCHAR(255) UNIQUE NOT NULL, "
                + "experience TEXT, " + "createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + "weight INT DEFAULT 0" + ")";
        statement.execute(reviewerProfileTable);

        // NEW: Create RequestReviewRole Table (for student review role requests)
        String requestReviewRoleTable = "CREATE TABLE IF NOT EXISTS ReviewRequests ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, " + "studentUsername VARCHAR(255) UNIQUE NOT NULL, "
                + "isApproved BOOLEAN DEFAULT FALSE, " + "createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP" + ")";
        statement.execute(requestReviewRoleTable);
        
     // In DatabaseHelper.createTables() after your other table creation statements...
        String actionLogsTable = "CREATE TABLE IF NOT EXISTS ActionLogs ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "username VARCHAR(255), "
                + "actionDescription VARCHAR(1024), "
                + "logTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        statement.execute(actionLogsTable);
    }
	

	//MODIFIED BY ISABELLA: this method powers the feature where reviews are shown to everyone besides just reviewrs. returns the most recent review for an answer regardless of role. 
	public Review getLatestReviewForAnswer(int answerId) throws SQLException {
	    String query = "SELECT * FROM reviews WHERE answerId = ? ORDER BY createdAt DESC LIMIT 1";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setInt(1, answerId);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return new Review(
	                rs.getInt("id"),
	                rs.getInt("answerId"),
	                rs.getString("reviewerUsername"),
	                rs.getString("text"),
	                rs.getTimestamp("createdAt"),
	                rs.getObject("previousReviewId") != null ? rs.getInt("previousReviewId") : null
	            );
	        }
	    }
	    return null;
	}


	
	//MODIFIED BY ISABELLA: this returns feedback strings for a reviewer. it just helped the feedback button to work. the rest will be implemented by tharun 
	public List<String> getReviewFeedbacks(String reviewerUsername) throws SQLException {
	    List<String> feedbacks = new ArrayList<>();
	    String sql = "SELECT feedback FROM reviews WHERE reviewerUsername = ? AND feedback IS NOT NULL ORDER BY createdAt DESC";

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, reviewerUsername);
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                feedbacks.add(rs.getString("feedback"));
	            }
	        }
	    }
	    return feedbacks;
	}

	
	public boolean isPureReviewer(String username) throws SQLException { //MODIFIED BY ISABELLA: this method ensures that the user is JUST a reviewer and not any other roles so that only reviewers can see what reviewers are able to see
		List<String> roles = getUserRolesList(username);
	    return roles.contains("reviewer") && !roles.contains("student") && !roles.contains("admin");
	}

	public List<Review> getReviewerViewPage(String reviewerUsername) throws SQLException { //MODIFIED BY ISABELLA: this returns the list of reviews the reviewer has written. 
	    List<Review> reviews = new ArrayList<>();
	    String query = "SELECT * FROM reviews WHERE reviewerUsername = ?";
	    PreparedStatement stmt = connection.prepareStatement(query);
	    stmt.setString(1, reviewerUsername);
	    ResultSet rs = stmt.executeQuery();

	    while (rs.next()) {
	        Review r = new Review(
	            rs.getInt("id"),
	            rs.getInt("answerId"),
	            rs.getString("reviewerUsername"),
	            rs.getString("text"),
	            rs.getTimestamp("createdAt"),
	            rs.getInt("previousReviewId")  // Or handle nullable
	        );
	        reviews.add(r);
	    }

	    return reviews;
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, role, email) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			// Store roles as a comma-separated string
			pstmt.setString(3, String.join(",", user.getRoles())); // MODIFIED: Join roles list
			pstmt.setString(4, user.getEmail());
			pstmt.executeUpdate();
		}
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, String.join(",", user.getRoles())); // MODIFIED: Join roles list
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	public List<Answer> getAllAnswersFromDB() throws SQLException {
		List<Answer> list = new ArrayList<>();
		String query = "SELECT * FROM Answers";
		PreparedStatement pstmt = connection.prepareStatement(query);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
			int id = rs.getInt("id");
			int questionId = rs.getInt("questionId");
			String text = rs.getString("text");
			String userName = rs.getString("userName");
			boolean isAccepted = rs.getBoolean("isAccepted");
			boolean isFavorite = rs.getBoolean("isFavorite"); // Nathaniel: added favorite initialization
			Answer a = new Answer(id, questionId, text, userName);
			a.setAccepted(isAccepted);
			a.setAccepted(isFavorite); // Nathaniel
			list.add(a);
		}
		rs.close();
		pstmt.close();
		return list;
	}

	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
		String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				// If the count is greater than 0, the user exists
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false; // If an error occurs, assume user doesn't exist
	}

	// Retrieves the role string of a user from the database using their userName.
	// This returns a comma-separated string of roles.
	public String getUserRoles(String userName) {
		String query = "SELECT role FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString("role");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// NEW HELPER METHOD:
	// Converts the comma-separated roles string from the database into a List of
	// roles.
	public List<String> getUserRolesList(String userName) throws SQLException {
		String rolesStr = getUserRoles(userName);
		List<String> rolesList = new ArrayList<>();
		if (rolesStr != null && !rolesStr.trim().isEmpty()) {
			String[] rolesArray = rolesStr.split(",");
			for (String r : rolesArray) {
				rolesList.add(r.trim());
			}
		}
		return rolesList;
	}

	// MODIFIED METHOD:
	// Updated getAdminCount to work with comma-separated roles.
	public int getAdminCount() throws SQLException {
		// Using LIKE to check if 'admin' is a substring of the role column.
		// This works because roles are stored as comma-separated values.
		String query = "SELECT COUNT(*) as count FROM cse360users WHERE role LIKE '%admin%'";
		try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt("count");
			}
		}
		return 0;
	}

	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode(String[] roles) {
		String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code

		// Append role designations to the invite code
		for (int i = 0; i < roles.length; i++) {
			code += roles[i];
		}

		String query = "INSERT INTO InvitationCodes (code) VALUES (?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return code;
	}

	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
		String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				// Mark the code as used
				markInvitationCodeAsUsed(code);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
		String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// ----- QUESTION OPERATIONS -----
	public void addQuestionToDB(Question question) throws SQLException {
		String query = "INSERT INTO Questions (text, userName, parentQuestionId) VALUES (?, ?, ?)";
		PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, question.getText());
		pstmt.setString(2, question.getUserName());
		if (question.getParentQuestionId() == null) {
			pstmt.setNull(3, java.sql.Types.INTEGER);
		} else {
			pstmt.setInt(3, question.getParentQuestionId());
		}
		pstmt.executeUpdate();
		ResultSet rs = pstmt.getGeneratedKeys();
		if (rs.next()) {
			int generatedId = rs.getInt(1);
			question.setId(generatedId); // Ensure Question.java has a setter for id
		}
		rs.close();
		pstmt.close();
	}

	public List<Question> getAllQuestionsFromDB() throws SQLException {
		List<Question> list = new ArrayList<>();
		String query = "SELECT * FROM Questions";
		PreparedStatement pstmt = connection.prepareStatement(query);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
			int id = rs.getInt("id");
			String text = rs.getString("text");
			String userName = rs.getString("userName");
			// Get the parentQuestionId; it will be null if not set.
			Integer parentId = (rs.getObject("parentQuestionId") != null) ? rs.getInt("parentQuestionId") : null;
			Question q = new Question(id, text, userName, parentId);
			list.add(q);
		}
		rs.close();
		pstmt.close();
		return list;
	}

	public void updateQuestionInDB(int id, String newText) throws SQLException {
		String query = "UPDATE Questions SET text = ? WHERE id = ?";
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setString(1, newText);
		pstmt.setInt(2, id);
		pstmt.executeUpdate();
		pstmt.close();
	}

	public void deleteQuestionFromDB(int id) throws SQLException {
		// First, delete any answers related to this question.
		String deleteAnswers = "DELETE FROM Answers WHERE questionId = ?";
		PreparedStatement pstmtAnswers = connection.prepareStatement(deleteAnswers);
		pstmtAnswers.setInt(1, id);
		pstmtAnswers.executeUpdate();
		pstmtAnswers.close();

		// Then, delete any clarifications for this question.
		String deleteClarifications = "DELETE FROM Questions WHERE parentQuestionId = ?";
		PreparedStatement pstmtClarifications = connection.prepareStatement(deleteClarifications);
		pstmtClarifications.setInt(1, id);
		pstmtClarifications.executeUpdate();
		pstmtClarifications.close();

		// Finally, delete the original question.
		String query = "DELETE FROM Questions WHERE id = ?";
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setInt(1, id);
		pstmt.executeUpdate();
		pstmt.close();
	}

	// ----- ANSWER OPERATIONS -----
	public void addAnswerToDB(Answer answer) throws SQLException {
		String query = "INSERT INTO Answers (questionId, text, userName, isAccepted, isFavorite, isRead) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		pstmt.setInt(1, answer.getQuestionId());
		pstmt.setString(2, answer.getText());
		pstmt.setString(3, answer.getUserName());
		pstmt.setBoolean(4, answer.isAccepted());
		pstmt.setBoolean(5, answer.isFavorite()); // Nathaniel: entry for identifying favorites

		pstmt.setBoolean(6, false); // all new answers added are by default unread
		pstmt.executeUpdate();
		ResultSet rs = pstmt.getGeneratedKeys();
		if (rs.next()) {
			int generatedId = rs.getInt(1);
			answer.setId(generatedId); // Make sure to add a setter in Answer
		}
		rs.close();
		pstmt.close();
	}

	public List<Answer> getAnswersForQuestionFromDB(int questionId) throws SQLException {
		List<Answer> list = new ArrayList<>();
		String query = "SELECT * FROM Answers WHERE questionId = ?";
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setInt(1, questionId);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
			int id = rs.getInt("id");
			String text = rs.getString("text");
			String userName = rs.getString("userName");
			boolean isAccepted = rs.getBoolean("isAccepted");
			boolean isFavorite = rs.getBoolean("isFavorite");
			Answer a = new Answer(id, questionId, text, userName);
			a.setAccepted(isAccepted);
			a.setFavorite(isFavorite); // Nathaniel: initializing favorite status
			list.add(a); // Add favorite item
		}
		rs.close();
		pstmt.close();
		return list;
	}

	public void updateAnswerInDB(int id, String newText) throws SQLException {
		String query = "UPDATE Answers SET text = ? WHERE id = ?";
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setString(1, newText);
		pstmt.setInt(2, id);
		pstmt.executeUpdate();
		pstmt.close();
	}

	public void deleteAnswerFromDB(int id) throws SQLException {
		String query = "DELETE FROM Answers WHERE id = ?";
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setInt(1, id);
		pstmt.executeUpdate();
		pstmt.close();
	}

	// Nathaniel: toggle the favorite status for all answers of the user
	public void favoriteAnswerFromDB(String userName) throws SQLException {
		// Run two simultaneous queries.
		// 1: obtain all answers given by the user
		// 2: toggle favorite status of all answers given by the user
		String query1 = "SELECT * FROM Answers WHERE userName = ?";
		String query2 = "UPDATE Answers SET isFavorite = ? WHERE userName = ?";

		PreparedStatement pstmt1 = connection.prepareStatement(query1);
		PreparedStatement pstmt2 = connection.prepareStatement(query2);

		pstmt1.setString(1, userName);
		ResultSet rs = pstmt1.executeQuery();
		boolean isFavorite = false;
		while (rs.next()) {
			isFavorite = rs.getBoolean("isFavorite");
			if (isFavorite) { // Toggle the value of isFavorite
				pstmt2.setBoolean(1, false);
			} else {
				pstmt2.setBoolean(1, true);
			}
			pstmt2.setString(2, userName);
			pstmt2.executeUpdate();

		}
		rs.close();
		pstmt1.close();
		pstmt2.close();
	}

	// Zakia: lists all unread answers of a student
	public List<Answer> getUnreadAnsForStudent(String username) throws SQLException {
		List<Answer> unreadList = new ArrayList<>();
		String query = "SELECT a.* FROM Answers a JOIN Questions q ON a.questionId = q.id WHERE q.userName = ? AND a.isRead = false";

		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setString(1, username);
		ResultSet res = pstmt.executeQuery();
		while (res.next()) {
			Answer ans = new Answer(res.getInt("id"), res.getInt("questionId"), res.getString("text"),
					res.getString("username"));
			unreadList.add(ans);
		}
		res.close();
		pstmt.close();
		return unreadList;
	}

	// Zakia: update the isRead status for list of answer
	public void markAnswerRead(List<Integer> answerIds) throws SQLException {
		String query = "UPDATE Answers SET isRead = true WHERE id = ?";
		PreparedStatement pstmt = connection.prepareStatement(query);

		for (int i : answerIds) {
			pstmt.setInt(1, i);
			pstmt.addBatch();
		}

		pstmt.executeBatch();
		pstmt.close();
	}

	// Zakia: retrieves a student's all questions that are unresolved
	public List<Question> getUnresolvedQuestions(String username) throws SQLException {
		List<Question> questionsList = new ArrayList<>();

		String query = "SELECT * FROM Questions q WHERE q.userName = ? "
				+ "AND NOT EXISTS (SELECT 1 FROM Answers a WHERE a.questionId = q.id AND a.isFavorite = TRUE)";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			try (ResultSet res = pstmt.executeQuery()) {
				while (res.next()) {
					Integer parentId = (res.getObject("parentQuestionId") != null) ? res.getInt("parentQuestionId")
							: null;
					Question question = new Question(res.getInt("id"), res.getString("text"), res.getString("userName"),
							parentId);
					questionsList.add(question);
				}
			}
		}
		return questionsList;
	}

	// Zakia: retrieves a student's all questions that are resolved
	public List<Question> getResolvedQuestions(String username) throws SQLException {
		List<Question> resolved = new ArrayList<>();

		String query = "SELECT * FROM Questions q WHERE q.userName = ? "
				+ " AND EXISTS (SELECT 1 FROM Answers a WHERE a.questionId = q.id AND a.isAccepted = true)";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);

			try (ResultSet res = pstmt.executeQuery();) {

				while (res.next()) {
					int id = res.getInt("id");
					String text = res.getString("text");
					String user = res.getString("userName");

					Integer parentId = (res.getObject("parentQuestionId") != null) ? res.getInt("parentQuestionId")
							: null;
					Question question = new Question(id, text, user, parentId);
					resolved.add(question);

				}
			}
		}
		return resolved;
	}

	// Zakia: Method to retrieve all favorite(potential) answers for a question
	public List<Answer> getPotentialAns(int questionId) throws SQLException {
		String q = "SELECT * FROM Answers WHERE questionId = ? AND isFavorite = true";

		List<Answer> ans = new ArrayList<>();

		try (PreparedStatement pstmt = connection.prepareStatement(q)) {
			pstmt.setInt(1, questionId);

			try (ResultSet res = pstmt.executeQuery()) {

				while (res.next()) {
					Answer a = new Answer(res.getInt("id"), res.getInt("questionId"), res.getString("text"),
							res.getString("userName"));

					a.setAccepted(res.getBoolean("isAccepted"));
					a.setFavorite(res.getBoolean("isFavorite"));
					a.setread(res.getBoolean("isRead"));
					ans.add(a);
				}
			}
		}
		return ans;
	}

	public void markAnswerAsResolved(int answerId) throws SQLException {
		String query = "UPDATE Answers SET isFavorite = TRUE WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, answerId);
			pstmt.executeUpdate();
		}
	}

	public List<Answer> getSortedResolvedAnswers(int questionId) throws SQLException {
		List<Answer> list = new ArrayList<>();
		String query = "SELECT * FROM Answers WHERE questionId = ? ORDER BY isFavorite DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Answer answer = new Answer(rs.getInt("id"), rs.getInt("questionId"), rs.getString("text"),
						rs.getString("userName"));
				answer.setFavorite(rs.getBoolean("isFavorite")); // Mark as resolved
				answer.setAccepted(rs.getBoolean("isAccepted"));
				list.add(answer);
			}
		}
		return list;
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try {
			if (statement != null)
				statement.close();
		} catch (SQLException se2) {
			se2.printStackTrace();
		}
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}


	
	
	/**
	 * MODIFIED METHOD: Updated updateUserRole to toggle a role for a user, allowing
	 * admins to add or remove roles. Restrictions: - If the target user already has
	 * the role, the operation would remove it. - However, if the role is "admin",
	 * removal is disallowed—this prevents any admin from losing admin access
	 * (including self-removal or removal from another admin).
	 *
	 * @param adminUser    The username of the admin performing the update.
	 * @param targetUser   The username of the user whose roles are being updated.
	 * @param roleToToggle The role to add (if absent) or remove (if present).
	 * @return true if the update was successful, false if the operation is not
	 *         allowed.
	 */

	/**
	 * Adds a role to the target user. Returns true if the role was successfully
	 * added.
	 */
	public boolean addUserRole(String adminUser, String targetUser, String role) throws SQLException {
		List<String> targetRoles = getUserRolesList(targetUser);
		if (targetRoles.contains(role)) {
			return false; // Role is already assigned.
		}
		// Add the role
		targetRoles.add(role);
		String newRolesStr = String.join(",", targetRoles);
		String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newRolesStr);
			pstmt.setString(2, targetUser);
			pstmt.executeUpdate();
			return true;
		}
	}

	/**
	 * Removes a role from the target user. Returns true if the role was
	 * successfully removed. For the "admin" role, additional restrictions apply: -
	 * An admin can only remove their own admin role. - The admin role cannot be
	 * removed if this is the only admin.
	 */
	public boolean removeUserRole(String adminUser, String targetUser, String role) throws SQLException {
		List<String> targetRoles = getUserRolesList(targetUser);

		// Role must be assigned.
		if (!targetRoles.contains(role)) {
			return false;
		}
		// A user must have at least one role.
		if (targetRoles.size() <= 1) {
			return false; // Cannot remove the only role.
		}
		// If the role is "admin", enforce additional restrictions.
		if ("admin".equals(role)) {
			// An admin can only remove their own admin role.
			if (!targetUser.equals(adminUser)) {
				return false;
			}
			// There must be at least one admin remaining.
			int adminCount = getAdminCount();
			if (adminCount <= 1) {
				return false; // This is the only admin.
			}
		}
		// Remove the role.
		targetRoles.remove(role);
		String newRolesStr = String.join(",", targetRoles);
		String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newRolesStr);
			pstmt.setString(2, targetUser);
			pstmt.executeUpdate();
			return true;
		}
	}

	// Retrieves all usernames from the database.
	public List<String> getAllUsers() throws SQLException {
		List<String> users = new ArrayList<>();
		String query = "SELECT userName FROM cse360users";
		try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				users.add(rs.getString("userName"));
			}
		}
		return users;
	}

	public boolean updatePassword(String userName, String newPassword) throws SQLException {
		String query = "UPDATE cse360users SET password = ? WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newPassword);
			pstmt.setString(2, userName);
			int rowsAffected = pstmt.executeUpdate();
			return rowsAffected > 0;
		}
	}

	// get authors of answer (for filter)
	public List<String> fetchAuthors() throws SQLException {
		List<String> authors = new ArrayList<>();
		String query = "SELECT DISTINCT userName FROM Questions";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				authors.add(rs.getString("userName"));
			}
		}
		return authors;
	}

	public List<Answer> fetchAnswersByQuestionId(int questionId) throws SQLException {
		List<Answer> answers = new ArrayList<>();
		String query = "SELECT * FROM Answers WHERE questionId = ?";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("id");
				String text = rs.getString("text");
				String userName = rs.getString("userName");

				// Create Answer object and add to the list
				Answer answer = new Answer(id, questionId, text, userName);
				answers.add(answer);
			}
		}
		return answers;
	}

	/**
	 * Inserts a new reviewer's profile into the database.
	 *
	 * @param profile the reviewer profile to add
	 * @throws SQLException when a database error occurs
	 */
	public void addReviewerProfile(ReviewerProfile profile) throws SQLException {
		String query = "INSERT INTO ReviewerProfiles (reviewerUsername, experience, weight) VALUES (?, ?, ?)";
		PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, profile.getReviewerUsername());
		pstmt.setString(2, profile.getExperience());
		pstmt.setInt(3, profile.getWeight());
		pstmt.executeUpdate();
		ResultSet rs = pstmt.getGeneratedKeys();
		if (rs.next()) {
			profile.setId(rs.getInt(1));
		}
		rs.close();
		pstmt.close();
	}

	/**
	 * Retrieves the reviewer profile for the specified reviewer.
	 *
	 * @param reviewerUserName the username of the reviewer
	 * @return the reviewer profile or null if not found
	 * @throws SQLException when a database error occurs
	 */
	public ReviewerProfile getReviewerProfile(String reviewerUsername) throws SQLException {
		String query = "SELECT * FROM ReviewerProfiles WHERE reviewerUsername = ?";
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setString(1, reviewerUsername);
		ReviewerProfile profile = null;
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			profile = new ReviewerProfile(rs.getInt("id"), rs.getString("reviewerUsername"), rs.getString("experience"),
					rs.getTimestamp("createdDate"), rs.getInt("weight"));
		}
		rs.close();
		pstmt.close();
		return profile;
	}

	/**
	 * Updates an existing reviewer profile in the database.
	 *
	 * @param profile the reviewer profile with updated experience
	 * @throws SQLException
	 * @author zakia
	 */
	public void updateReviewerProfile(ReviewerProfile profile) throws SQLException {
		String query = "UPDATE ReviewerProfiles SET experience = ?, weight = ? WHERE reviewerUsername = ?";
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setString(1, profile.getExperience());
		pstmt.setInt(2, profile.getWeight());
		pstmt.setString(3, profile.getReviewerUsername());
		pstmt.executeUpdate();
		pstmt.close();
	}

	/**
	 * Retrieves all reviewer profiles from the ReviewerProfiles table.
	 *
	 * @return a list of ReviewerProfile objects
	 * @throws SQLException
	 * @author zakia
	 */
	
	public List<ReviewerProfile> listReviewers() throws SQLException {
		List<ReviewerProfile> list = new ArrayList<>();
		String query = "SELECT * FROM ReviewerProfiles ORDER BY weight DESC";
		PreparedStatement pstmt = connection.prepareStatement(query);
		ResultSet res = pstmt.executeQuery();

		while (res.next()) {

			ReviewerProfile prof = new ReviewerProfile(
					res.getInt("id"),
					res.getString("reviewerUsername"),
					res.getString("experience"),
					res.getTimestamp("createdDate"),
					res.getInt("weight"));
			list.add(prof);
		}
		res.close();
		pstmt.close();
		return list;
	}
	
	public List<ReviewerProfile> listTrustedReviewers() throws SQLException {
		List<ReviewerProfile> list = new ArrayList<>();
		String query = "SELECT * FROM ReviewerProfiles WHERE weight > 0 ORDER BY weight DESC";
		PreparedStatement pstmt = connection.prepareStatement(query);
		ResultSet res = pstmt.executeQuery();

		while (res.next()) {

			ReviewerProfile prof = new ReviewerProfile(
					res.getInt("id"),
					res.getString("reviewerUsername"),
					res.getString("experience"),
					res.getTimestamp("createdDate"),
					res.getInt("weight"));
			list.add(prof);
		}
		res.close();
		pstmt.close();
		return list;
	}
	
	public void showReviewerList() {
		try {
			
			List<ReviewerProfile> reviewers = listReviewers();

			ListView<ReviewerProfile> listV = new ListView<>();

			listV.getItems().addAll(reviewers);

			listV.setCellFactory(lv -> new ListCell<ReviewerProfile>() {
				@Override
				protected void updateItem(ReviewerProfile profile, boolean empty) {
					super.updateItem(profile, empty);
					if (empty || profile == null) {
						setText(null);
					} else {
						// Format the created date
						SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
						String dateStr = sdf.format(profile.getCreatedDate());
						setText(profile.getReviewerUsername() + " (Reviewer since: " + dateStr + ")");
					}
				}
			});

			// Create a dialog to display the ListView
			Stage dialog = new Stage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setTitle("All Reviewers");

			VBox vbox = new VBox(10, new Label("List of Reviewers:"), listV);
			vbox.setPadding(new Insets(10));
			vbox.setAlignment(Pos.CENTER);

			Scene scene = new Scene(vbox, 400, 300);
			dialog.setScene(scene);
			dialog.showAndWait();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void showTrustedReviewerList() {
		ObservableList<ReviewerProfile> reviewerObservableList;
		reviewerObservableList = FXCollections.observableArrayList();
		try {
			TextField reviewerWeightField;
			
			List<ReviewerProfile> reviewers = listTrustedReviewers();

			ListView<ReviewerProfile> listV = new ListView<>(reviewerObservableList);

			listV.getItems().addAll(reviewers);

			listV.setCellFactory(lv -> new ListCell<ReviewerProfile>() {
				@Override
				protected void updateItem(ReviewerProfile profile, boolean empty) {
					super.updateItem(profile, empty);
					if (empty || profile == null) {
						setText(null);
					} else {
						// Format the created date
						SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
						String dateStr = sdf.format(profile.getCreatedDate());
						setText(profile.getReviewerUsername() + " (Reviewer since: " + dateStr + ")" + "\t\t\t(Trust level: " + profile.getWeight() + ")");
					}
				}
			});
			
			reviewerWeightField = new TextField();
			reviewerWeightField.setPromptText("Assign a Weight");
			reviewerWeightField.setMaxWidth(100);
			reviewerWeightField.setMaxHeight(30);
			
			Button addWeightBtn = new Button("Add Weight");
			addWeightBtn.setOnAction(e -> updateWeight(listV, reviewerWeightField, reviewerObservableList));
			
			HBox hbox = new HBox(10);
			hbox.getChildren().addAll(reviewerWeightField, addWeightBtn);

			// Create a dialog to display the ListView
			Stage dialog = new Stage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setTitle("All Reviewers");

			VBox.setVgrow(reviewerWeightField, Priority.NEVER);
			VBox vbox = new VBox(10, new Label("List of Reviewers:"), listV, hbox);
			vbox.setPadding(new Insets(10));
			vbox.setAlignment(Pos.CENTER);

			Scene scene = new Scene(vbox, 400, 300);
			dialog.setScene(scene);
			dialog.showAndWait();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	
	private void updateWeight(ListView<ReviewerProfile> listV, TextField reviewerWeightField, ObservableList<ReviewerProfile> reviewerObservableList) {
		try {
			if (listV.getSelectionModel().getSelectedItem() == null
					|| reviewerWeightField.getText().trim().isEmpty()) {
				throw new IllegalArgumentException("No reviewer selected. Please select a reviewer to update.");
			} else {
				ReviewerProfile reviewer = listV.getSelectionModel().getSelectedItem();
				int weight = Integer.parseInt(reviewerWeightField.getText());
				if (weight > 10 || weight < 1) {
					throw new IllegalArgumentException("Invalid weight. Please select a value between 1-10.");
				}
				reviewer.setWeight(weight);
				updateReviewerProfile(reviewer);
				List<ReviewerProfile> reviewers = listReviewers();
				//Refresh
				reviewerObservableList.clear();
				reviewerObservableList.addAll(reviewers);
			}
		} catch (IllegalArgumentException ex) {
			showAlert("Error Adding Weight", ex.getMessage());
		} catch (Exception ex) {
			showAlert("Error Adding Weight", ex.getMessage());
		}
	}
	
	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
	
	/**
	 * get lists of requests for reviewer role
	 * 
	 * @return requests
	 * @throws SQLException
	 * @author eliza
	 */
	public List<ReviewRequest> getAllReviewRequests() throws SQLException {
		List<ReviewRequest> requests = new ArrayList<>();
		String sql = "SELECT * FROM ReviewRequests";
		try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				int id = rs.getInt("id");
				String studentUsername = rs.getString("studentUsername");
				boolean isApproved = rs.getBoolean("isApproved");
				requests.add(new ReviewRequest(id, studentUsername, isApproved));
			}
		}
		return requests;
	}

	/**
	 * Add requests for reviewer role
	 * 
	 * @param studentUsername
	 * @return
	 * @throws SQLException
	 * 
	 * @author eliza
	 */
	public boolean addReviewRequest(String studentUsername) throws SQLException {
	    // Check if a review request already exists for this student
	    String checkQuery = "SELECT COUNT(*) FROM ReviewRequests WHERE studentUsername = ? AND isApproved = false";
	    try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
	        checkStmt.setString(1, studentUsername);
	        ResultSet rs = checkStmt.executeQuery();
	        if (rs.next() && rs.getInt(1) > 0) {
	            // If a pending review request already exists, return false (or handle as needed)
	            return false;
	        }
	    }

	    // If no pending review request exists, proceed with the insert
	    String insertQuery = "INSERT INTO ReviewRequests (studentUsername, isApproved) VALUES (?, false)";
	    try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
	        pstmt.setString(1, studentUsername);
	        pstmt.executeUpdate();
	        return true; // Successfully added the review request
	    }
	}

	/**
	 * Accept reviewer role requests
	 * 
	 * @param requestId
	 * @param studentUsername
	 * @return
	 * @throws SQLException
	 * 
	 * @author eliza
	 */
	public boolean acceptReviewRequest(int requestId, String studentUsername) throws SQLException {
	    // Add the role for the user
	    String role = "reviewer"; // Example role, adjust as needed
	    if (addUserRole("instructorUsername", studentUsername, role)) { // Assuming the instructor is adding the role
	        // After adding the role, remove the review request from the database
	        removeReviewRequest(requestId);  // Make sure this properly deletes the request
	        return true;
	    }
	    return false;
	}

	/**
	 * Reject reviewer role requests
	 * 
	 * @param requestId
	 * @return
	 * @throws SQLException
	 * 
	 * @author eliza
	 */
	public boolean denyReviewRequest(int requestId) throws SQLException {
	    // Deny the review request and remove it from the database
	    removeReviewRequest(requestId); // Make sure this properly deletes the request
	    return true;
	}
	
	/**
	 * Removes requests from database once action is complete
	 * 
	 * @param requestId
	 * @throws SQLException
	 * 
	 * @author eliza
	 */
	private void removeReviewRequest(int requestId) throws SQLException {
	    String deleteQuery = "DELETE FROM ReviewRequests WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
	        pstmt.setInt(1, requestId);
	        pstmt.executeUpdate();  // Executes the deletion
	    }
	}
	
	/**
	 * Checks if request still exist for testing purposes
	 * 
	 * @param requestId
	 * @return
	 * @throws SQLException
	 * 
	 * @author eliza
	 */
	public boolean isRequestExists(int requestId) throws SQLException {
	    String query = "SELECT COUNT(*) FROM ReviewRequests WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setInt(1, requestId);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt(1) > 0; // If count > 0, request still exists
	        }
	    }
	    return false;
	}

	
	/**
	 * Provide controlled access to the connection for testing purposes
	 * 
	 * @return connection
	 * 
	 * @author eliza
	 */
    public Connection getConnection() {
        return connection;
    }
	
	// -------------- REVIEW FUNCTIONS --------------

	// MODIFIED BY ISABELLA:  Creates the 'reviews' table if dont exists
	public void createReviewTableIfNotExists() throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS reviews (" +
				"id INT AUTO_INCREMENT PRIMARY KEY," +
				"answerId INT," +
				"reviewerUsername VARCHAR(255)," +
				"text CLOB," +
				"createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
				"previousReviewId INT)";
		try (Statement stmt = connection.createStatement()) {
			stmt.execute(sql);
		}
	}

	// MODIFIED BY ISABELLA: create a new review (first review or updated review)
	public void createReview(int answerId, String reviewer, String text) throws SQLException {
		String sql = "INSERT INTO reviews (answerId, reviewerUsername, text) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, answerId);
			stmt.setString(2, reviewer);
			stmt.setString(3, text);
			stmt.executeUpdate();
		}
	}

	// MODIFIED BY ISABELLA: used to update a review and link it to the previous one via previousReviewID
	public void updateReview(int oldId, String newText) throws SQLException {
		Review old = getReviewById(oldId);
		if (old == null) return;

		createReview(old.getAnswerId(), old.getReviewerUsername(), newText);

		String updateSql = "UPDATE reviews SET previousReviewId = ? WHERE id = (SELECT MAX(id) FROM reviews)";
		try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
			stmt.setInt(1, oldId);
			stmt.executeUpdate();
		}
	}

	// MODIFIED BY ISABELLA: gets the latest review by a reviewer for a specific answer
	public Review getLatestReviewByReviewerForAnswer(String reviewer, int answerId) throws SQLException {
		String sql = "SELECT * FROM reviews WHERE reviewerUsername = ? AND answerId = ? ORDER BY createdAt DESC LIMIT 1";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, reviewer);
			stmt.setInt(2, answerId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return mapReview(rs);
			}
		}
		return null;
	}

	// MODIFIED BY ISABELLA: returns a specific review by ID which was used to retreieve previous reviews
	public Review getReviewById(int reviewId) throws SQLException {
		String sql = "SELECT * FROM reviews WHERE id = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, reviewId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return mapReview(rs);
			}
		}
		return null;
	}

	// MODIFIED BY ISABELLA: returns all reviews for an answer
	public List<Review> getReviewsForAnswer(int answerId) throws SQLException {
		List<Review> list = new ArrayList<>();
		String sql = "SELECT * FROM reviews WHERE answerId = ? ORDER BY createdAt DESC";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, answerId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(mapReview(rs));
			}	
		}
		return list;
	}

	// MODIFIED BY ISABELLA: converts result to Review object
	// In DatabaseHelper.java
	private Review mapReview(ResultSet rs) throws SQLException {
	    int id = rs.getInt("id");
	    int answerId = rs.getInt("answerId");
	    String reviewer = rs.getString("reviewerUsername");
	    String text = rs.getString("text");
	    Timestamp time = rs.getTimestamp("createdAt");
	    Integer prevId = null;
	    Object prevIdObj = rs.getObject("previousReviewId");
	    System.out.println("DEBUG mapReview: Review ID=" + id + ", prevIdObj type=" + (prevIdObj == null ? "NULL" : prevIdObj.getClass().getName()) + ", value=" + prevIdObj); // DEBUG LINE
	    if (prevIdObj != null) {
	         try {
	             prevId = rs.getInt("previousReviewId"); // Or potentially ((Number) prevIdObj).intValue(); if type is complex
	         } catch (SQLException e) {
	             System.err.println("DEBUG mapReview: Error getting int for previousReviewId for review ID=" + id);
	             e.printStackTrace();
	         }
	    }
	     System.out.println("DEBUG mapReview: Final prevId for Review ID=" + id + " is: " + prevId); // DEBUG LINE
	    return new Review(id, answerId, reviewer, text, time, prevId);
	}
	//Tharun Phase 3 
	
	/**
	 * Inserts a new feedback record into the Feedbacks table.
	 */
	public void addFeedback(Feedback feedback) throws SQLException {
	    String query = "INSERT INTO Feedbacks (reviewId, studentUsername, message) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
	        pstmt.setInt(1, feedback.getReviewId());
	        pstmt.setString(2, feedback.getStudentUsername());
	        pstmt.setString(3, feedback.getMessage());
	        pstmt.executeUpdate();
	        ResultSet rs = pstmt.getGeneratedKeys();
	        if (rs.next()) {
	            feedback.setFeedbackId(rs.getInt(1));
	        }
	    }
	}

	/**
	 * Retrieves all feedback messages for a given review.
	 */
	public List<Feedback> getFeedbacksForReview(int reviewId) throws SQLException {
	    List<Feedback> list = new ArrayList<>();
	    String query = "SELECT * FROM Feedbacks WHERE reviewId = ? ORDER BY createdAt DESC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, reviewId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Feedback fb = new Feedback(
	                rs.getInt("feedbackId"),
	                rs.getInt("reviewId"),
	                rs.getString("studentUsername"),
	                rs.getString("message"),
	                rs.getTimestamp("createdAt")
	            );
	            list.add(fb);
	        }
	    }
	    return list;
	}

	/**
	 * Retrieves the count of feedback messages for a given review.
	 */
	public int getFeedbackCountForReview(int reviewId) throws SQLException {
	    String query = "SELECT COUNT(*) AS count FROM Feedbacks WHERE reviewId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, reviewId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("count");
	        }
	    }
	    return 0;
	}
	public void logAction(String username, String actionDescription) throws SQLException {
	    String query = "INSERT INTO ActionLogs (username, actionDescription) VALUES (?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        pstmt.setString(2, actionDescription);
	        pstmt.executeUpdate();
	    }
	}
	public List<ActionLog> getActionLogs() throws SQLException {
	    List<ActionLog> logs = new ArrayList<>();
	    String query = "SELECT * FROM ActionLogs ORDER BY logTime DESC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	         while(rs.next()){
	             int id = rs.getInt("id");
	             String username = rs.getString("username");
	             String actionDesc = rs.getString("actionDescription");
	             Timestamp logTime = rs.getTimestamp("logTime");
	             logs.add(new ActionLog(id, username, actionDesc, logTime));
	         }
	    }
	    return logs;
	}
	


}
