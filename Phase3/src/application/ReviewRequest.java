package application;

/*******
 * <p> Title: ReviewRequest Class. </p>
 * 
 * <p> Description: A Java class to host all review request objects </p>
 * 
 * @author Eliza Chook
 * 
 * @version 1.00	2025-03-31 Created the class and methods for review request
 * 
 */

public class ReviewRequest {
    private int id;
    private String studentUsername;
    private boolean isApproved;

    /**retrieve id, student, and approval for instructor to check
     * 
     * @param id
     * @param studentUsername
     * @param isApproved
     */
    public ReviewRequest(int id, String student, boolean isApproved) {
        this.id = id;
        this.studentUsername = student;
        this.isApproved = isApproved;
    }

    /**
     *  get student id
     * @return id
     */
    public int getId() {
        return id;
    }
    
    /**
     * set student id
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * get student's user name
     * @return studentUsername
     */
    public String getStudentUsername() {
        return studentUsername;
    }

    /**
     * show if request is approved
     * @return isApproved
     */
    public boolean isApproved() {
        return isApproved;
    }

    /**
     * set if request is approved/declined
     * @param isApproved
     */
    public void setApproved(boolean isApproved) {
        this.isApproved = isApproved;
    }
    
    @Override
    /**Return string of values obtained
     * @return "ReviewRequest{" +
                "id=" + id +
                ", studentUsername='" + studentUsername + '\'' +
                ", isApproved=" + isApproved +
                '}'
     */
    public String toString() {
        return "ReviewRequest{" +
                "id=" + id +
                ", studentUsername='" + studentUsername + '\'' +
                ", isApproved=" + isApproved +
                '}';
    }
}
