package application;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import databasePart1.DatabaseHelper;


/**
 * The Answers class handles operations related to managing answers within the application.
 * It provides methods for adding, updating, deleting, and retrieving answers from the database.
 */
public class Answers {
    private DatabaseHelper dbHelper;
    
    /**
     * Constructs an Answers instance with the specified database helper.
     *
     * @param dbHelper the database helper used for database operations
     */
    public Answers(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Adds a new answer to the database.
     *
     * @param answer the Answer object to be added
     */
    public void addAnswer(Answer answer) {
        try {
            dbHelper.addAnswerToDB(answer);
        } catch (SQLException e) {
            throw new RuntimeException("Error adding answer: " + e.getMessage());
        }
    }

    
    /**
     * Retrieves all answers associated with a specific question.
     *
     * @param questionId the unique identifier of the question
     * @return a List of Answer objects for the specified question
     */
    public List<Answer> getAnswersForQuestion(int questionId) {
        try {
        	//Nathaniel: sort the answerList, if a favorite is found, move it to the top.
        	List<Answer> answerList = dbHelper.getAnswersForQuestionFromDB(questionId);
        	int j = 0;
        	for (int i = 0; i < answerList.size(); i++) {
        		if (answerList.get(i).isFavorite()) { //If favorite is found
        			Collections.swap(answerList, i, j); //Swap it with the top item
        			j++; //Make next item the top item
        		}
        	}
        	return answerList;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting answers for question: " + e.getMessage());
        }
    }


    /**
     * Retrieves a list of all answers from the database.
     *
     * @return a List of all Answer objects
     */
    public List<Answer> getAllAnswers() {
        try {
            return dbHelper.getAllAnswersFromDB();
        } catch (SQLException e) {
            throw new RuntimeException("Error getting all answers: " + e.getMessage());
        }
    }

    /**
     * Retrieves an answer by its unique identifier.
     *
     * @param id the unique identifier of the answer
     * @return the Answer object with the specified id, or null if not found
     */
    public Answer getAnswerById(int id) {
        List<Answer> allAnswers = getAllAnswers();
        for (Answer a : allAnswers) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    
    /**
     * Retrieves an answer submitted by the specified user.
     *
     * @param userName the username of the answer's author
     * @return the Answer object submitted by the specified user, or null if not found
     */
    public Answer getAnswerByUser(String userName) {
        List<Answer> allAnswers = getAllAnswers();
        for (Answer a : allAnswers) {
            if (a.getUserName().equals(userName)) {
                return a;
            }
        }
        return null;
    }


    /**
     * Updates an existing answer in the database.
     *
     * @param id the unique identifier of the answer to be updated
     * @param newText the new text content for the answer
     * @param currentUser the username of the user requesting the update
     */
    public void updateAnswer(int id, String newText, String currentUser) {
        Answer a = getAnswerById(id);
        if (a == null) {
            throw new RuntimeException("Answer not found.");
        }
        if (!a.getUserName().equals(currentUser)) {
            throw new RuntimeException("You are not authorized to update this answer.");
        }
        try {
            dbHelper.updateAnswerInDB(id, newText);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating answer: " + e.getMessage());
        }
    }

    /**
     * Deletes an answer from the database.
     *
     * @param id the unique identifier of the answer to be deleted
     * @param currentUser the username of the user requesting the deletion
     */
    public void deleteAnswer(int id, String currentUser) {
        Answer a = getAnswerById(id);
        if (a == null) {
            throw new RuntimeException("Answer not found.");
        }
        if (!a.getUserName().equals(currentUser)) {
            throw new RuntimeException("You are not authorized to delete this answer.");
        }
        try {
            dbHelper.deleteAnswerFromDB(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting answer: " + e.getMessage());
        }
    }
  
    /**
     * Marks an answer as a favorite.
     *
     * @param id the unique identifier of the answer to be marked as favorite
     */
    public void addFavorite(int id) {
    	Answer a = getAnswerById(id);
    	if (a == null) {
    		throw new RuntimeException("Answer not found.");
    	}
    	try {
            dbHelper.favoriteAnswerFromDB(a.getUserName()); //toggle the favorite status of the answer

        } catch (SQLException e) {
            throw new RuntimeException("Error favoriting answer: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves a list of unread answers for the specified user.
     *
     * @param username the username for which to retrieve unread answers
     * @return a List of unread Answer objects for the specified user
     */
    public List <Answer> getUnreadAnswerFromDB (String username) {
    	try {
    		return dbHelper.getUnreadAnsForStudent(username);
    	} catch (SQLException e) {
    		throw new RuntimeException("Error retrieving undread answers: " + e.getMessage());
    	}
    }
    

    /**
     * Marks the specified answers as read in the database.
     *
     * @param answerId a List of answer identifiers to be marked as read
     */
    public void markAnswerAsRead(List <Integer> answerId) {
    	try {
    		dbHelper.markAnswerRead(answerId);
    	} catch (SQLException e) {
    		throw new RuntimeException ("Error marking answers as read " + e.getMessage());
    	}
    }
    
    /**
     * Retrieves a list of favorite answers for a student for a specific question.
     *
     * @param questionId the unique identifier of the question
     * @return a List of favorite Answer objects for the specified question
     */
    public List<Answer> getFavAnswersForStudent (int questionId) {
    	try {
			return dbHelper.getPotentialAns(questionId);
		} catch (SQLException e) {
			throw new RuntimeException ("Error in retrieving favorite answers: " + e.getMessage());
		}
    }
    

}