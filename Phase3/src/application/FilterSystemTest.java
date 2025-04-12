package application;

import java.sql.SQLException;
import java.util.List;
import databasePart1.DatabaseHelper;
import application.Question;
import application.Questions;
import application.Answer;
import application.Answers;

/**
 * The FilterSystemTest class contains tests to validate the functionality of the filter system.
 * It includes a main method to run the tests and verify that the system behaves as expected.
 */
public class FilterSystemTest {
	  /**
     * Default constructor for FilterSystemTest.
     * <p>
     * This constructor initializes the FilterSystemTest instance. It can be used to set up any
     * necessary components or state required for the tests.
     * </p>
     */
	public FilterSystemTest() {}
		
	/**
     * The main method to execute the filter system tests.
     *
     * @param args the command-line arguments (not used)
     */
    public static void main(String[] args) {
        DatabaseHelper dbHelper = new DatabaseHelper();
        try {
            // Connect to the database.
            dbHelper.connectToDatabase();
            
            // Initialize managers.
            Answers answerManager = new Answers(dbHelper);
            Questions questionManager = new Questions(dbHelper, answerManager);
            
            // ---------- Test 1: Add Questions and Answers ----------
            System.out.println("=== Test 1: Add Questions and Answers ===");
            
            Question q1 = new Question(0, "What is Java?", "author1");
            Question q2 = new Question(0, "Explain OOP concepts.", "author2");
            Question q3 = new Question(0, "How does garbage collection work?", "author3");
            
            questionManager.addQuestion(q1);
            questionManager.addQuestion(q2);
            questionManager.addQuestion(q3);
            
            Answer a1 = new Answer(0, q1.getId(), "Java is a programming language.", "author4");
            Answer a2 = new Answer(0, q2.getId(), "OOP stands for Object-Oriented Programming.", "author5");
            
            answerManager.addAnswer(a1);
            answerManager.addAnswer(a2);
            
            System.out.println("Questions and answers added.");
            
            // ---------- Test 2: Fetch Answered and Unanswered Questions ----------
            System.out.println("\n=== Test 2: Fetch Answered and Unanswered Questions ===");
            List<Question> answeredQuestions = questionManager.getAllAnsweredQuestions();
            List<Question> unansweredQuestions = questionManager.getAllUnansweredQuestions();
            
            System.out.println("Answered Questions:");
            for (Question q : answeredQuestions) {
                System.out.println(q);
            }
            
            System.out.println("\nUnanswered Questions:");
            for (Question q : unansweredQuestions) {
                System.out.println(q);
            }
            
            // ---------- Test 3: Fetch Authors of Answered and Unanswered Questions ----------
            System.out.println("\n=== Test 3: Fetch Authors of Answered and Unanswered Questions ===");
            List<String> answeredAuthors = questionManager.getAuthorsForAnsweredQuestions();
            List<String> unansweredAuthors = questionManager.getAuthorsForUnansweredQuestions();
            
            System.out.println("Authors who answered questions:");
            for (String author : answeredAuthors) {
                System.out.println(author);
            }
            
            System.out.println("\nAuthors of unanswered questions:");
            for (String author : unansweredAuthors) {
                System.out.println(author);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnection();
        }
    }
}
