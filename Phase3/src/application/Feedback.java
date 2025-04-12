//Tharun
package application;

import java.sql.Timestamp;

/**
 * The Feedback class represents the feedback provided by a student on a review.
 * It contains details such as the feedback ID, associated review ID, the student's username,
 * the feedback message, and the timestamp when the feedback was created.
 */
public class Feedback {
    private int feedbackId;
    private int reviewId;
    private String studentUsername;
    private String message;
    private Timestamp createdAt;


    /**
     * Constructs a Feedback object with all details specified.
     *
     * @param feedbackId the unique identifier for this feedback.
     * @param reviewId the unique identifier of the review associated with this feedback.
     * @param studentUsername the username of the student who provided the feedback.
     * @param message the content of the feedback.
     * @param createdAt the timestamp indicating when the feedback was created.
     */
    public Feedback(int feedbackId, int reviewId, String studentUsername, String message, Timestamp createdAt) {
        this.feedbackId = feedbackId;
        this.reviewId = reviewId;
        this.studentUsername = studentUsername;
        this.message = message;
        this.createdAt = createdAt;
    }

    /**
     * Constructs a Feedback object without specifying the feedback ID and creation timestamp.
     * These values may be set automatically or later.
     *
     * @param reviewId the unique identifier of the review associated with this feedback.
     * @param studentUsername the username of the student who provided the feedback.
     * @param message the content of the feedback.
     */
    public Feedback(int reviewId, String studentUsername, String message) {
        this(0, reviewId, studentUsername, message, new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Returns the unique identifier for this feedback.
     *
     * @return the feedback ID.
     */
    public int getFeedbackId() {
        return feedbackId;
    }
    
    /**
     * Sets the unique identifier for this feedback.
     *
     * @param feedbackId the new feedback ID.
     */
    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }
    
    /**
     * Returns the unique identifier of the review associated with this feedback.
     *
     * @return the review ID.
     */
    public int getReviewId() {
        return reviewId;
    }
    
    /**
     * Returns the username of the student who provided the feedback.
     *
     * @return the student's username.
     */
    public String getStudentUsername() {
        return studentUsername;
    }
    
    /**
     * Returns the feedback message.
     *
     * @return the content of the feedback.
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Returns the timestamp when this feedback was created.
     *
     * @return the creation timestamp.
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Feedback from " + studentUsername + " at " + createdAt + ": " + message;
    }
}
