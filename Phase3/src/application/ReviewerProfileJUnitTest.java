package application;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;


/*******
 * <p>
 * Title: ReviewerProfileJUnitTest Class.
 * </p>
 * 
 * @author Zakia Mohammadi
 * 
 * @version 1.00 2025-04-02 Create 5 test cases to test the reviewer profile functions * 
 */

class ReviewerProfileJUnitTest {
	
	/*
	 * Tests the creation of a ReviewerProfile object with given
	 * @author zakia
	 */
	@Test
	public void testCreateReviewProfile() {
		Timestamp createdDate = new Timestamp(System.currentTimeMillis());
		ReviewerProfile profile = new ReviewerProfile(1, "reviewer1", "Experience1", createdDate, 0);
		
		assertEquals(1, profile.getId());
		assertEquals("reviewer1", profile.getReviewerUsername());
		assertEquals("Experience1", profile.getExperience());
		assertEquals(createdDate, profile.getCreatedDate());
	}
	
	/*
	 *  confirming that the role assignment logic works correctly and a reviewer is recognized by the system
	 *  @author zakia
	 */
	@Test
	public void testReviewerRoleAssignment () {
		List <String> roles = new ArrayList<>();
		roles.add("reviewer");
		User reviewer = new User ("reviewer2", "Password123!", roles, "rev2@gmail.com");
		
		assertTrue(reviewer.getRoles().contains("reviewer"));
	}

	
	/*
	 *  to validate that the creation date is correctly captured and stored.
	 *  @author zakia
	 */
	@Test
	public void testCreatedDate_NotNull () {
		Timestamp createdDate = new Timestamp(System.currentTimeMillis());
		ReviewerProfile profile = new ReviewerProfile(2, "reviewer3", "Experience3", createdDate, 0);
		
		assertNotNull(profile.getCreatedDate());
	}
	
	
	/*
	 * to ensure that the profile is linked to the correct user and there is no mismatch.
	 * @author zakia
	 */
	@Test
	public void testReviewerUserNameMatches () {
		ReviewerProfile profile = new ReviewerProfile(3, "reviewer4", "Experience4", new Timestamp(System.currentTimeMillis()), 0);
		
		assertEquals("reviewer4", profile.getReviewerUsername());
		
	}
	
	
	/*
	 * This is to verify that the roles list in User supports multiple roles and the reviewer role is working correctly
	 * @author zakia
	 */
	@Test
	public void testMultipleRolesInUser () {
		List <String> roles = new ArrayList<>();
		
		roles.add("student");
		roles.add("reviewer");
		
		User multiRoleUser = new User ("userxxx", "Pass@secure", roles, "userxxx@gmail.com");
		
		assertTrue(multiRoleUser.getRoles().contains("reviewer"));
		assertTrue(multiRoleUser.getRoles().contains("student"));
	}
	
}
