package application;

import java.sql.SQLException;
import java.util.List;
import databasePart1.DatabaseHelper;
import application.Question;
import application.Questions;
import application.Answer;
import application.Answers;

/**
 * The Phase2Test class is used to execute tests related to Phase 2 of the project.
 * It contains a main method that serves as the entry point for running the tests.
 */
public class Phase2Test {
	  /**
     * Default constructor for Phase2Test.
     * This constructor creates an instance of the test class.
     */
    public Phase2Test() {}
    
    /**
     * The main method serves as the entry point for running Phase 2 tests.
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
            
            // ---------- Test 1: Add Original Question and Clarification ----------
            System.out.println("=== Test 1: Add Original and Clarification ===");
            // Create an original question.
            Question original = new Question(0, "Breakfast?", "student1");
            questionManager.addQuestion(original);
            System.out.println("Original question added:\n" + original);
            
            // Create a clarification (follow-up) question for the original.
            Question clarification = new Question(0, "Java?", "student2", original.getId());
            questionManager.addQuestion(clarification);
            System.out.println("Clarification added:\n" + clarification);
            
            // List all questions.
            List<Question> allQuestions = questionManager.getAllQuestions();
            System.out.println("\nAll questions in DB:");
            for (Question q : allQuestions) {
                System.out.println(q);
            }
            
            // ---------- Test 2: Answer Mapping for Clarification ----------
            System.out.println("\n=== Test 2: Answer Mapping ===");
            // When a student selects a clarification, the answer is mapped to the original question.
            // Here, we simulate by adding an answer with the original question's id.
            Answer answer = new Answer(0, original.getId(), "Whatsup?.", "student3");
            answerManager.addAnswer(answer);
            System.out.println("Answer added:\n" + answer);
            
            // Retrieve and display answers for the original question.
            List<Answer> answersForOriginal = answerManager.getAnswersForQuestion(original.getId());
            System.out.println("\nAnswers for Original Question (ID " + original.getId() + "):");
            for (Answer a : answersForOriginal) {
                System.out.println(a);
            }
            
            // ---------- Test 3: Cascading Deletion ----------
         // Deleting the original question should also delete its clarification.
            System.out.println("\n=== Test 3: Cascading Deletion ===");
            System.out.println("Deleting original question with ID: " + original.getId());
            
            questionManager.deleteQuestion(original.getId(), original.getUserName());
            
            List<Question> questionsAfterDeletion = questionManager.getAllQuestions();
            System.out.println("\nQuestions in DB after deletion:");
            for (Question q : questionsAfterDeletion) {
                System.out.println(q);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnection();
        }
    }
}
