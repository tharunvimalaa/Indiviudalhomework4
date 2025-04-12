package application;
import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * The Question class represents a question submitted in the application.
 * It includes the question's text, the user who submitted it, and additional metadata such as creation date
 * and a reference to a parent question if it exists.
 */
public class Question {
    private int id;
    private String text;
    private String userName;    // The user who asked the question
    private Date createdDate;
    private Integer parentQuestionId;
    
    /**
     * Constructs a Question object.
     * @param id the unique identifier for the question
     * @param text the text of the question (cannot be null or empty)
     * @param userName the name of the user who asked the question (cannot be null or empty)
     */
    public Question(int id, String text, String userName) {
        // Input validation:
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Question text cannot be empty.");
        }
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        this.id = id;
        this.text = text;
        this.userName = userName;
        this.createdDate = new Date(); // Automatically assign current date/time
    }
    
    /**
     * Constructs a Question with the specified id, text, user name, and parent question id.
     *
     * @param id the unique identifier for the question.
     * @param text the content of the question.
     * @param userName the username of the user who submitted the question.
     * @param parentQuestionId the identifier of the parent question, or null if this is not a reply.
     */
    public Question(int id, String text, String userName, Integer parentQuestionId) {
        this(id, text, userName); // Calls the existing constructor (which handles validation and sets createdDate)
        this.parentQuestionId = parentQuestionId;
    }
    
    /**
     * Returns the unique identifier of the question.
     *
     * @return the question id.
     */
    public int getId() { 
        return id; 
    }
    
    /**
     * Sets the unique identifier for the question.
     *
     * @param id the new question id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the identifier of the parent question.
     *
     * @return the parent question id, or null if there is no parent question.
     */
    public Integer getParentQuestionId() {
        return parentQuestionId;
    }
    
    
    /**
     * Sets the identifier of the parent question.
     *
     * @param parentQuestionId the new parent question id.
     */
    public void setParentQuestionId(Integer parentQuestionId) {
        this.parentQuestionId = parentQuestionId;
    }
    

    /**
     * Returns the text content of the question.
     *
     * @return the question text.
     */
    public String getText() { 
        return text; 
    }
    
    /**
     * Sets the text content of the question.
     *
     * @param text the new question text.
     */
    public void setText(String text) { 
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Question text cannot be empty.");
        }
        this.text = text;
    }
    
    /**
     * Returns the username of the user who submitted the question.
     *
     * @return the username.
     */
    public String getUserName() { 
        return userName; 
    }
    
    /**
     * Returns the creation date of the question.
     *
     * @return the date the question was created.
     */
    public Date getCreatedDate() { 
        return createdDate; 
    }
    
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        return String.format("Question ID: %d\nQuestion: %s\nAsked by: %s\nCreated: %s",
                id, text, userName, sdf.format(createdDate));
    }
}
