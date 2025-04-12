package application;

import java.sql.Timestamp;

/**
 * The Review class represents a review submitted within the application.
 * It contains details such as the review's unique identifier, the associated answer's ID,
 * the timestamp when the review was created, a reference to a previous review (if any),
 * the reviewer's username, and the text content of the review.
 */
public class Review {
    private int id;
    private int answerId;
    private String reviewerUsername;
    private String text;
    private Timestamp createdAt;
    private Integer previousReviewId;

    /**
     * Constructs a Review object with the specified details.
     *
     * @param id the unique identifier for the review.
     * @param answerId the identifier of the associated answer.
     * @param reviewerUsername the username of the reviewer who submitted the review.
     * @param text the text content of the review.
     * @param createdAt the timestamp when the review was created.
     * @param previousReviewId the identifier of the previous review, or null if there is none.
     */
    public Review(int id, int answerId, String reviewerUsername, String text, Timestamp createdAt, Integer previousReviewId) {
        this.id = id;
        this.answerId = answerId;
        this.reviewerUsername = reviewerUsername;
        this.text = text;
        this.createdAt = createdAt;
        this.previousReviewId = previousReviewId;
    }

    //this is what is taken in when each review is done. 
    
    /**
     * Returns the unique identifier of this review.
     *
     * @return the review id.
     */
    public int getId() { return id; }
    
    /**
     * Returns the identifier of the answer associated with this review.
     *
     * @return the answer id.
     */
    public int getAnswerId() { return answerId; }
    
    /**
     * Returns the username of the reviewer who submitted this review.
     *
     * @return the reviewer's username.
     */
    public String getReviewerUsername() { return reviewerUsername; }
    
    /**
     * Returns the text content of this review.
     *
     * @return the review text.
     */
    public String getText() { return text; }
    

    /**
     * Returns the timestamp indicating when this review was created.
     *
     * @return the creation timestamp.
     */
    public Timestamp getCreatedAt() { return createdAt; }
    
    /**
     * Returns the identifier of the previous review, if available.
     *
     * @return the previous review id, or null if there is no previous review.
     */
    public Integer getPreviousReviewId() { return previousReviewId; }
}
