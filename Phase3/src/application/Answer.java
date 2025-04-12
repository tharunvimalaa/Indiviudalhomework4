package application;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Represents an answer to a question within the application.
 * This class encapsulates the details of an answer including its text, 
 * author, associated question, and various status flags.
 */
public class Answer {
    private int id;
    private int questionId;  // The ID of the question this answer is for
    private String text;
    private String userName;
    private Date createdDate;
    private boolean isAccepted;
    private boolean isFavorite; // Nathaniel: For marking trusted answers.
//    private boolean isPotential; //Zakia: for potential answers
    private boolean isRead; 	//Zakia: for read/unread answers
    /**
     * Constructs an Answer object.
     * @param id the unique identifier for the answer
     * @param questionId the id of the question this answer belongs to
     * @param text the text of the answer (cannot be null or empty)
     * @param userName the name of the user who provided the answer (cannot be null or empty)
     */
    public Answer(int id, int questionId, String text, String userName) {
        // Input validation:
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer text cannot be empty.");
        }
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        this.id = id;
        this.questionId = questionId;
        this.text = text;
        this.userName = userName;
        this.createdDate = new Date();
        this.isAccepted = false;
        this.isFavorite = false;
//        this.isPotential = false;
        this.isRead = false;
    }
    
    /**
     * Returns the unique identifier of this answer.
     *
     * @return the answer ID.
     */
    public int getId() { 
        return id; 
    }
    
    /**
     * Sets the unique identifier for this answer.
     *
     * @param id the new answer ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the identifier of the question associated with this answer.
     *
     * @return the question ID.
     */
    public int getQuestionId() { 
        return questionId; 
    }
    
    /**
     * Returns the text content of the answer.
     *
     * @return the answer text.
     */
    public String getText() { 
        return text; 
    }
    
    /**
     * Sets the text content of the answer.
     *
     * @param text the new text for the answer.
     */
    public void setText(String text) { 
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer text cannot be empty.");
        }
        this.text = text;
    }
    
    /**
     * Returns the username of the user who submitted this answer.
     *
     * @return the username of the answer's author.
     */
    public String getUserName() { 
        return userName; 
    }
    
    /**
     * Returns the date when this answer was created.
     *
     * @return the creation date of the answer.
     */
    public Date getCreatedDate() { 
        return createdDate; 
    }
    
    /**
     * Checks if this answer has been accepted.
     *
     * @return true if the answer is accepted, false otherwise.
     */
    public boolean isAccepted() { 
        return isAccepted; 
    }
    
    /**
     * Sets the accepted status for this answer.
     *
     * @param accepted true to mark the answer as accepted, false otherwise.
     */
    public void setAccepted(boolean accepted) { 
        this.isAccepted = accepted; 
    }
    
    /**
     * Checks if this answer is marked as a favorite.
     *
     * @return true if the answer is marked as favorite, false otherwise.
     */
    public boolean isFavorite() {
    	return isFavorite;
    }
    
    /**
     * Sets the favorite status for this answer.
     *
     * @param favorite true to mark the answer as favorite, false otherwise.
     */
    public void setFavorite(boolean favorite) {
    	isFavorite = favorite;
    }
    
    /**
     * Checks if this answer has been read.
     *
     * @return true if the answer has been read, false otherwise.
     */
    public boolean isRead() {
    	return isRead;
    }
    
    /**
     * Sets the read status for this answer.
     *
     * @param isRead true to mark the answer as read, false otherwise.
     */
    public void setread (boolean isRead) {
    	this.isRead = isRead;
    }
    
    /**
     * Marks this answer as read.
     */
    public void markRead() {
    	this.isRead = true;
    }
    
    @Override
    public String toString() {
    	SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        return String.format("Answer ID: %d\nAnswer: %s\nAnswered by: %s\nCreated: %s",
                             id, text, userName, sdf.format(createdDate));
    }
}
