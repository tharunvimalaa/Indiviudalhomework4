package application;

import java.sql.Timestamp;

/**
 * The ReviewerProfile class represents a user entity in the system.
 * It contains the reviewer's details such as id, username, experience, and a timestamp.
 */
public class ReviewerProfile {
	private int id;
	 private String reviewerUsername;
	    private String exp;
	    private Timestamp createdDate;
	    private int weight;

	    /**
	     * Default Constructor
	     */
	    public ReviewerProfile() {}

	    

	    /**
	     * Constructs a new ReviewerProfile with specified values.
	     *
	     * @param id the ID of the profile
	     * @param reviewerUserName the username of the reviewer
	     * @param exp the experience description
	     * @param createdDate the timestamp when the profile was created
	     * @param weight measure
	     */	    
	    public ReviewerProfile(int id, String reviewerUserName, String exp, Timestamp createdDate, int weight) {
	        this.id = id;
	        this.reviewerUsername = reviewerUserName;
	        this.exp = exp;
	        this.createdDate = createdDate;
	        this.weight = weight;
	    }

	    // Getters and setters
	    
	    /**
	     * Returns the reviewer's id
	     * 
	     * @return the reviewer's id
	     */
	    public int getId() {
	        return id;
	    }
	    
	    /**
	     * Sets the ID of the reviewer profile.
	     *
	     * @param id the ID to assign to the reviewer
	     */
	    public void setId(int id) {
	        this.id = id;
	    }
	    
	    
	    /**
	     * Returns the reviewer username
	     *
	     * @return the reviewer's username
	     */
	    public String getReviewerUsername() {
	        return reviewerUsername;
	    }
	    
	    /**
	     * Sets the username of the reviewer.
	     *
	     * @param reviewerUsername the reviewer's username
	     */
	    public void setReviewerUsername(String reviewerUsername) {
	        this.reviewerUsername = reviewerUsername;
	    }
	    
	    /**
	     * get experience method.
	     *
	     * @return experience
	     */
	    public String getExperience() {
	        return exp;
	    }
	    
	    /**
	     * Sets the experience description of the reviewer.
	     *
	     * @param experience the experience string to set
	     */
	    public void setExperience(String experience) {
	        this.exp = experience;
	    }
	    
	    /**
	     * Returns the creation date of the reviewer profile.
	     *
	     * @return the created date timestamp
	     */
	    public Timestamp getCreatedDate() {
	        return createdDate;
	    }
	    
	    /**
	     * Sets the creation date of the reviewer profile.
	     *
	     * @param createdDate the created date timestamp to set
	     */
	    public void setCreatedDate(Timestamp createdDate) {
	        this.createdDate = createdDate;
	    }
	    
	    /**
	     * Gets the weight for a reviewer
	     *
	     * @return the weight
	     */
	    public int getWeight() {
	    	return weight;
	    }
	    /**
	     * Sets the trust weight assigned to this reviewer by a student.
	     *
	     * @param weight the numerical weight representing trust level
	     */
	    public void setWeight(int weight) {
	    	this.weight = weight;
	    }
}