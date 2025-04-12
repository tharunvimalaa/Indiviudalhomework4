package application;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import databasePart1.DatabaseHelper;

import java.sql.*;

/*******
 * <p>
 * Title: ReviewRequestJunitTest Class.
 * </p>
 * 
 * <p>
 * Description: Java file to test if user can add request, instructor can accept or deny request, and management of the request list. You must delete the database before starting running this test
 * </p>
 * 
 * @author Eliza Chook
 * 
 * @version 1.00 2025-04-02 Create 5 test cases to test the review request functions * 
 */
public class ReviewRequestJunitTest {
    public static DatabaseHelper databaseHelper;
    public static Connection testConnection;
    
    /**
     * Connect to database before start of tests
     * 
     * @throws SQLException
     * 
     * @author eliza
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
            stmt.execute("CREATE TABLE IF NOT EXISTS ReviewRequests (id INT AUTO_INCREMENT PRIMARY KEY, studentUsername VARCHAR(255) UNIQUE, isApproved BOOLEAN);");
        }
    }
    
    /**
     * Clear request lists before each test to prevent unwanted variables from affecting each test
     * 
     * @throws SQLException
     * 
     * @author eliza
     */
    @BeforeEach
    public void resetDatabase() throws SQLException {
        try (PreparedStatement pstmt = testConnection.prepareStatement("DELETE FROM ReviewRequests WHERE studentUsername = ?")) {
            pstmt.setString(1, "testStudent");
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Test if user can request for the reviewer role
     * 
     * @throws SQLException
     * 
     * @author eliza
     */
    @Test
    public void testAddUserRole() throws SQLException {
        // Insert a user
        try (PreparedStatement pstmt = testConnection.prepareStatement("INSERT INTO cse360users (userName, role) VALUES (?, ?)")) {
            pstmt.setString(1, "testUser");
            pstmt.setString(2, "Student");
            pstmt.executeUpdate();
        }

        // Add a new role
        boolean result = databaseHelper.addUserRole("admin", "testUser", "Reviewer");

        // Verify role is updated
        try (PreparedStatement pstmt = testConnection.prepareStatement("SELECT role FROM cse360users WHERE userName = ?")) {
            pstmt.setString(1, "testUser");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String roles = rs.getString("role");
                assertTrue(roles.contains("Reviewer"));
            } else {
                fail("User not found in database");
            }
        }

        assertTrue(result);
    }
    
    /**
     * Test if instructor can accept requests
     * 
     * @throws SQLException
     * 
     * @author eliza
     */
    @Test
    public void testAcceptReviewRequest() throws SQLException {
        // Insert a test review request
        try (PreparedStatement pstmt = testConnection.prepareStatement("INSERT INTO ReviewRequests (id, studentUsername, isApproved) VALUES (?, ?, ?)")) {
            pstmt.setInt(1, 1);
            pstmt.setString(2, "testStudent");
            pstmt.setBoolean(3, false);
            pstmt.executeUpdate();
        }

        // Accept the review request
        boolean result = databaseHelper.acceptReviewRequest(1, "testStudent");

        // Ensure the request is removed
        assertFalse(databaseHelper.isRequestExists(1), "Review request should be removed after acceptance");
        assertTrue(result);
    }
    
    /**
     * Test if instructor can reject requests
     * 
     * @throws SQLException
     * 
     * @author eliza
     */
    @Test
    public void testDenyReviewRequest() throws SQLException {
        // Insert a test review request
        try (PreparedStatement pstmt = testConnection.prepareStatement("INSERT INTO ReviewRequests (id, studentUsername, isApproved) VALUES (?, ?, ?)")) {
            pstmt.setInt(1, 1);
            pstmt.setString(2, "testStudent");
            pstmt.setBoolean(3, false);
            pstmt.executeUpdate();
        }

        // Deny the review request
        boolean result = databaseHelper.denyReviewRequest(1);

        // Ensure the request is removed
        assertFalse(databaseHelper.isRequestExists(1), "Review request should be removed after denial");
        assertTrue(result);
    }

    /**
     * Test if requests are removed after instructor accepts or denies request
     * 
     * @throws SQLException
     * 
     * @author eliza
     */
    @Test
    public void testReviewRequestRemovedAfterAction() throws SQLException {
        // Insert a review request
        try (PreparedStatement pstmt = testConnection.prepareStatement("INSERT INTO ReviewRequests (studentUsername, isApproved) VALUES (?, ?)")) {
            pstmt.setString(1, "testStudent");
            pstmt.setBoolean(2, false);
            pstmt.executeUpdate();
        }

        // Accept request
        databaseHelper.acceptReviewRequest(1, "testStudent");

        // Verify request is removed
        try (PreparedStatement pstmt = testConnection.prepareStatement("SELECT COUNT(*) FROM ReviewRequests WHERE studentUsername = ?")) {
            pstmt.setString(1, "testStudent");
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            assertEquals(0, rs.getInt(1), "Review request should be removed after action");
        }
    }
    
    /**
     * Test to make sure that user connot have duplicate requests
     * 
     * @throws SQLException
     * 
     * @author eliza
     */
    @Test
    public void testNoDuplicateReviewRequests() throws SQLException {
        // Add a review request
        boolean firstRequest = databaseHelper.addReviewRequest("testStudent");
        
        // Attempt to add a duplicate request
        boolean secondRequest = databaseHelper.addReviewRequest("testStudent");

        // The first request should be added successfully
        assertTrue(firstRequest, "First review request should be accepted.");
        
        // The second request should be rejected
        assertFalse(secondRequest, "Duplicate review request should be rejected.");
    }
    
    /**
     * Close database after tests 
     * 
     * @throws SQLException
     * 
     * @author eliza
     */
    @AfterAll
    public static void closeDatabase() throws SQLException {
        // Close the connection from DatabaseHelper
        if (databaseHelper.getConnection() != null) {
            databaseHelper.getConnection().close();
        }
    }
}
