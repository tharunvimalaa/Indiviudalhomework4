package application;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import databasePart1.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*******
 * <p>
 * Title: ReviewRequestJunitTest Class.
 * </p>
 * 
 * <p>
 * Description: Java file to test if user can add request, instructor can accept or deny request, and management of the request list. You must delete the database before starting running this test
 * </p>
 * 
 * @author Nathaniel Teo
 * 
 * @version 1.00 2025-04-02 Create 5 test cases to test the review request functions * 
 */
public class ReviewerTrustedJUnitTest {
    public static DatabaseHelper databaseHelper;
    public static Connection testConnection;
    
    /**
     * Constructor
     */
    public ReviewerTrustedJUnitTest() {}
    
    /**
     * Connect to database before start of tests
     * 
     * @throws SQLException
     * 
     * @author Nathaniel
     */
    @BeforeAll
    public static void setupDatabase() throws SQLException {
        // Initialize DatabaseHelper and connect to the database
        databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase(); // Use connectToDatabase to establish the connection
        
        // Now, access the connection using the getConnection() method
        testConnection = databaseHelper.getConnection(); // Get the connection from DatabaseHelper

        // Create necessary tables for testing
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS cse360users (userName VARCHAR(255) PRIMARY KEY, role VARCHAR(255));");
            stmt.execute("CREATE TABLE IF NOT EXISTS ReviewerProfiles (id INT AUTO_INCREMENT PRIMARY KEY, reviewerUsername VARCHAR(255) UNIQUE NOT NULL, experience TEXT, createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, weight INT DEFAULT 0)");
        }
    }
    
    /**
     * Clear request lists before each test to prevent unwanted variables from affecting each test
     * 
     * @throws SQLException
     * 
     * @author Nathaniel
     */
    @BeforeEach
    public void resetDatabase() throws SQLException {
        try (PreparedStatement pstmt = testConnection.prepareStatement("DELETE FROM cse360users WHERE studentUsername = ?")) {
            pstmt.setString(1, "testStudent");
            pstmt.executeUpdate();
        }
        try (PreparedStatement pstmt = testConnection.prepareStatement("DELETE FROM ReviewerProfiles WHERE studentUsername = ?")) {
            pstmt.setString(1, "testReviewer");
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Test if user can add a reviewer as trusted
     * 
     * @throws SQLException
     * 
     * @author Nathaniel
     */
    @Test
    public void testAddUserRole() throws SQLException {
        // Insert a user
        try (PreparedStatement pstmt = testConnection.prepareStatement("INSERT INTO cse360users (userName, role) VALUES (?, ?)")) {
            pstmt.setString(1, "testUser");
            pstmt.setString(2, "Student");
            pstmt.executeUpdate();
        }
        
        // Register a reviewer
        try (PreparedStatement pstmt = testConnection.prepareStatement("INSERT INTO ReviewerProfiles (id, reviewerUsername) VALUES (?, ?)")) {
            pstmt.setInt(1, 0);
            pstmt.setString(2, "testReviewer");
            pstmt.executeUpdate();
        }

        // Verify role is updated
        try (PreparedStatement pstmt = testConnection.prepareStatement("UPDATE ReviewerProfiles SET weight = ? WHERE reviewerUsername = ?")) {
            pstmt.setInt(1, 1);
            pstmt.setString(2, "testReviewer");
            pstmt.executeUpdate();
            List<ReviewerProfile> list = databaseHelper.listTrustedReviewers();
            if (list.getFirst() != null) {
            	assertTrue(list.getFirst() != null);
            } else {
            	fail("Reviewer not added to the list");
            }
        }
    }
    
    /**
     * Test if user can modify reviewer weight - success
     * 
     * @throws SQLException
     * 
     * @author Nathaniel
     */
    @Test
    public void testModifyReviewerWeightSuccess() throws SQLException {
        // Insert a test review request
        try (PreparedStatement pstmt = testConnection.prepareStatement("UPDATE ReviewerProfiles SET weight = ? WHERE reviewerUsername = ?")) {
            pstmt.setInt(1, 10);
            pstmt.setString(2, "testReviewer");
            pstmt.executeUpdate();
            List<ReviewerProfile> list = databaseHelper.listTrustedReviewers();
            if (list.getFirst() != null) {
            	assertTrue(list.getFirst().getWeight() == 10);
            } else {
            	fail("Reviewer not added to the list");
            }
        }
    }
    
    /**
     * Test if user can modify reviewer weight - positive fail
     * 
     * @throws SQLException
     * 
     * @author Nathaniel
     */
    @Test
    public void testModifyReviewerWeightPositiveFail() throws SQLException {
        // Insert a test review request
        try (PreparedStatement pstmt = testConnection.prepareStatement("UPDATE ReviewerProfiles SET weight = ? WHERE reviewerUsername = ?")) {
            pstmt.setInt(1, 11);
            pstmt.setString(2, "testReviewer");
            pstmt.executeUpdate();
            List<ReviewerProfile> list = databaseHelper.listTrustedReviewers();
            if (list.getFirst() != null) {
            	assertFalse(list.getFirst().getWeight() == 11);
            } else {
            	fail("Reviewer not added to the list");
            }
        }
    }

    /**
     * Test if user can modify reviewer weight - negative fail
     * 
     * @throws SQLException
     * 
     * @author Nathaniel
     */
    @Test
    public void testModifyReviewerWeightNegativeFail() throws SQLException {
        // Insert a test review request
        try (PreparedStatement pstmt = testConnection.prepareStatement("UPDATE ReviewerProfiles SET weight = ? WHERE reviewerUsername = ?")) {
            pstmt.setInt(1, -1);
            pstmt.setString(2, "testReviewer");
            pstmt.executeUpdate();
            List<ReviewerProfile> list = databaseHelper.listTrustedReviewers();
            if (list.getFirst() != null) {
            	assertFalse(list.getFirst().getWeight() == -1);
            } else {
            	fail("Reviewer not added to the list");
            }
        }
    }
    
    /**
     * Test if user can remove reviewer from trusted list
     * 
     * @throws SQLException
     * 
     * @author Nathaniel
     */
    @Test
    public void testRemoveReviewer() throws SQLException {
        // Insert a test review request
        try (PreparedStatement pstmt = testConnection.prepareStatement("UPDATE ReviewerProfiles SET weight = ? WHERE reviewerUsername = ?")) {
            pstmt.setInt(1, 0);
            pstmt.setString(2, "testReviewer");
            pstmt.executeUpdate();
            List<ReviewerProfile> list = databaseHelper.listTrustedReviewers();
            if (list.getFirst() == null) {
            	assertTrue(list.getFirst() == null);
            } else {
            	fail("Reviewer not added to the list");
            }
        }
    }
    
    /**
     * Close database after tests 
     * 
     * @throws SQLException
     * 
     */
    @AfterAll
    public static void closeDatabase() throws SQLException {
        // Close the connection from DatabaseHelper
        if (databaseHelper.getConnection() != null) {
            databaseHelper.getConnection().close();
        }
    }
}