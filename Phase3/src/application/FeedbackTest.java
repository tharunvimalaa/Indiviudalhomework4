package application;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import databasePart1.DatabaseHelper;

/**
 * JUnit tests for the Feedback functionality.
 */
public class FeedbackTest {

    private static DatabaseHelper dbHelper;

    /**
     * Default constructor for FeedbackTest.
     * This constructor initializes the FeedbackTest instance.
     */
    public FeedbackTest() {}
    
    /**
     * Sets up the test database before any tests are run.
     * This static method configures the test database and initializes necessary resources.
     *
     * @throws SQLException if a database access error occurs
     */
    @BeforeAll
    public static void setupDatabase() throws SQLException {
        dbHelper = new DatabaseHelper();
        dbHelper.connectToDatabase();
        // Clear the Feedbacks table before tests.
        dbHelper.getConnection().prepareStatement("DELETE FROM Feedbacks").executeUpdate();
    }
    
    /**
     * Executed before each test to set up the test environment.
     * This method prepares the necessary preconditions for each test.
     *
     * @throws SQLException if a database access error occurs
     */
    @BeforeEach
    public void beforeEachTest() throws SQLException {
        // Clear feedback records before each test.
        dbHelper.getConnection().prepareStatement("DELETE FROM Feedbacks").executeUpdate();
    }

    /**
     * Tears down the test environment after each test.
     * This method cleans up resources and resets the state for subsequent tests.
     *
     * @throws SQLException if a database access error occurs during cleanup
     */
    @AfterEach
    public void tearDown() throws SQLException {
        // Optional cleanup after each test.
        dbHelper.getConnection().prepareStatement("DELETE FROM Feedbacks").executeUpdate();
    }

    /**
     * Tests that adding valid feedback is successful.
     *
     * @throws SQLException if a database access error occurs during the test
     */
    @Test
    public void testAddFeedbackValid() throws SQLException {
        // Assume reviewId 1 exists and "studentUser" is providing feedback.
        Feedback feedback = new Feedback(1, "studentUser", "Great review, very detailed!");
        dbHelper.addFeedback(feedback);
        
        // Feedback ID should be auto-generated.
        assertTrue(feedback.getFeedbackId() > 0, "Feedback ID should be generated and > 0.");

        // Retrieve feedback for review 1.
        List<Feedback> feedbacks = dbHelper.getFeedbacksForReview(1);
        assertFalse(feedbacks.isEmpty(), "Feedback list should not be empty.");
        boolean found = feedbacks.stream().anyMatch(fb -> fb.getFeedbackId() == feedback.getFeedbackId());
        assertTrue(found, "Inserted feedback should be present in the database.");
    }

    /**
     * Tests that the feedback count is accurately retrieved.
     *
     * @throws SQLException if a database access error occurs during the test
     */
    @Test
    public void testGetFeedbackCount() throws SQLException {
        // Insert multiple feedback entries for reviewId 2.
        dbHelper.addFeedback(new Feedback(2, "studentA", "First feedback."));
        dbHelper.addFeedback(new Feedback(2, "studentB", "Second feedback."));
        dbHelper.addFeedback(new Feedback(2, "studentC", "Third feedback."));
        
        int count = dbHelper.getFeedbackCountForReview(2);
        assertEquals(3, count, "Feedback count should be 3.");
    }

    /**
     * Tests retrieving feedback for a specific review.
     *
     * @throws SQLException if a database access error occurs during the test
     */
    @Test
    public void testRetrieveFeedbacksForReview() throws SQLException {
        // Insert a feedback for reviewId 3.
        Feedback feedback = new Feedback(3, "studentX", "Insightful comment.");
        dbHelper.addFeedback(feedback);
        
        List<Feedback> feedbacks = dbHelper.getFeedbacksForReview(3);
        assertNotNull(feedbacks, "Feedback list should not be null.");
        assertTrue(feedbacks.size() >= 1, "Feedback list should have at least one entry.");
        assertEquals("Insightful comment.", feedbacks.get(0).getMessage());
    }
}
