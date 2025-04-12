package application;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import databasePart1.*;

/**
 * The Questions class manages operations related to handling questions in the application.
 * It provides methods for adding, updating, deleting, and retrieving questions from the database,
 * as well as methods to retrieve questions based on their answer status.
 */
public class Questions {
	private DatabaseHelper dbHelper;
	
	private Answers answerHandler; // Reference to Answers class


    /**
     * Constructs a Questions instance with the specified database helper and answer handler.
     *
     * @param dbHelper the database helper used for database operations.
     * @param answerHandler the answer handler used to manage answers associated with questions.
     */
    public Questions(DatabaseHelper dbHelper, Answers answerHandler) {
        this.dbHelper = dbHelper;
        this.answerHandler = answerHandler;
    }
	// CREATE
    /**
     * Adds a new question to the database.
     *
     * @param question the question object to be added.
     */
	public void addQuestion(Question question) {
		try {
			dbHelper.addQuestionToDB(question);
		} catch (SQLException e) {
			throw new RuntimeException("Error adding question: " + e.getMessage());
		}
	}

	// READ
	 /**
     * Retrieves all questions from the database.
     *
     * @return a list of all questions.
     */
	public List<Question> getAllQuestions() {
		try {
			return dbHelper.getAllQuestionsFromDB();
		} catch (SQLException e) {
			throw new RuntimeException("Error getting questions: " + e.getMessage());
		}
	}

	  /**
     * Retrieves a question by its unique identifier.
     *
     * @param id the unique identifier of the question.
     * @return the question object with the specified id.
     */
	public Question getQuestionById(int id) {
		List<Question> list = getAllQuestions();
		for (Question q : list) {
			if (q.getId() == id) {
				return q;
			}
		}
		return null;
	}

	// UPDATE

    /**
     * Updates the text of an existing question.
     *
     * @param id the unique identifier of the question to update.
     * @param newText the new text for the question.
     * @param currentUser the username of the user performing the update.
     */
	public void updateQuestion(int id, String newText, String currentUser) {
		Question q = getQuestionById(id);
		if (q == null) {
			throw new RuntimeException("Question not found.");
		}
		// Only allow the owner to update
		if (!q.getUserName().equals(currentUser)) {
			throw new RuntimeException("You are not authorized to update this question.");
		}
		try {
			dbHelper.updateQuestionInDB(id, newText);
		} catch (SQLException e) {
			throw new RuntimeException("Error updating question: " + e.getMessage());
		}
	}

	// DELETE
	   /**
     * Deletes a question from the database.
     *
     * @param id the unique identifier of the question to be deleted.
     * @param currentUser the username of the user requesting the deletion.
     */
	public void deleteQuestion(int id, String currentUser) {
		Question q = getQuestionById(id);
		if (q == null) {
			throw new RuntimeException("Question not found.");
		}
		if (!q.getUserName().equals(currentUser)) {
			throw new RuntimeException("You are not authorized to delete this question.");
		}
		try {
			dbHelper.deleteQuestionFromDB(id);
		} catch (SQLException e) {
			throw new RuntimeException("Error deleting question: " + e.getMessage());
		}
	}

	// Zakia: retreive all resolved/unresolved questions of a student
	 /**
     * Retrieves all resolved questions for a specific student.
     *
     * @param username the username of the student.
     * @return a list of resolved questions for the specified student.
     */
	public List<Question> getResolvedQuestionsForStudent(String username) {
		try {
			return dbHelper.getResolvedQuestions(username);
		} catch (SQLException e) {
			throw new RuntimeException("Error retrieving uesolved questions: " + e.getMessage());
		}
	}


    /**
     * Retrieves all unresolved questions for a specific student.
     *
     * @param username the username of the student.
     * @return a list of unresolved questions for the specified student.
     * @throws SQLException if a database access error occurs.
     */
	public List<Question> getUnresolvedQuestionsForStudent(String username) throws SQLException {
		try {
			return dbHelper.getUnresolvedQuestions(username);
		} catch (SQLException e) {
			throw new RuntimeException("Error retrieving unresolved questions: " + e.getMessage());
		}

	}

	// Eliza: get specific lists for filter
	// NEW: Fetch all answered questions
	 /**
     * Retrieves all answered questions from the database.
     *
     * @return a list of answered questions.
     * @throws SQLException if a database access error occurs.
     */
    public List<Question> getAllAnsweredQuestions() throws SQLException {
        List<Question> allQuestions = getAllQuestions();
		List<Question> answeredQuestions = new ArrayList<>();
		for (Question q : allQuestions) {
		    if (hasAnswer(q.getId())) {
		        answeredQuestions.add(q);
		    }
		}
		return answeredQuestions;
    }

    // NEW: Fetch all unanswered questions
    /**
     * Retrieves all unanswered questions from the database.
     *
     * @return a list of unanswered questions.
     * @throws SQLException if a database access error occurs.
     */
    public List<Question> getAllUnansweredQuestions() throws SQLException {
        List<Question> allQuestions = getAllQuestions();
		List<Question> unansweredQuestions = new ArrayList<>();
		for (Question q : allQuestions) {
		    if (!hasAnswer(q.getId())) {
		        unansweredQuestions.add(q);
		    }
		}
		return unansweredQuestions;
    }

    /**
     * Checks if a question has an answer.
     *
     * @param questionId the unique identifier of the question.
     * @return true if the question has an answer, false otherwise.
     */
    public boolean hasAnswer(int questionId) {
        List<Answer> answers = answerHandler.getAnswersForQuestion(questionId);
        return !answers.isEmpty();
    }

    /**
     * Retrieves a list of authors for answered questions.
     *
     * @return a list of usernames who authored answered questions.
     * @throws SQLException if a database access error occurs.
     */
    public List<String> getAuthorsForAnsweredQuestions() throws SQLException {
        List<Question> answeredQuestions = getAllAnsweredQuestions();
        List<String> authors = new ArrayList<>();
        for (Question q : answeredQuestions) {
            List<Answer> answers = answerHandler.getAnswersForQuestion(q.getId());
            for (Answer a : answers) {
                if (!authors.contains(a.getUserName())) {
                    authors.add(a.getUserName()); // Add unique authors
                }
            }
        }
        return authors;
    }

    /**
     * Retrieves a list of authors for unanswered questions.
    *
    * @return a list of usernames who authored unanswered questions.
    */
    public List<String> getAuthorsForUnansweredQuestions() {
        List<Question> unansweredQuestions = null;
		try {
			unansweredQuestions = getAllUnansweredQuestions();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List<String> authors = new ArrayList<>();
        for (Question q : unansweredQuestions) {
            if (!authors.contains(q.getUserName())) {
                authors.add(q.getUserName()); // Add unique authors
            }
        }
        return authors;
    }

}