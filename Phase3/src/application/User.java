package application;

import java.util.List;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 */
public class User {
    private String userName;
    private String password;
    private List<String> role; //Modified to fit Anas's declaration
    private String email;
    
    // Constructor to initialize a new User object with userName, password, and role.
    public User( String userName, String password, List<String> role, String email) { //MODIFIED TYPE FOR COMPATABILITY
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.email = email;

    }
    
    
    public boolean isPasswordValid() {
        return FSMRecognize.validate(password);
    }
    
    // Sets the role of the user. MODIFIED TYPE FOR COMPATABILITY
    public void setRole(List<String> role) {
    	this.role = role;
    }

    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public List<String> getRoles() { return role; } //MODIFIED TYPE FOR COMPATABILITY
    public String getEmail() { return email; }
}