package application;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import databasePart1.DatabaseHelper;

/**
 * The OneTimePassword class handles OTP generation and password reset functionality.
 * It uses a database update (instead of registration) to temporarily set a one-time password.
 */
public class OneTimePassword {
    
    private DatabaseHelper DBHelper;        
    private Random rand = new Random();
    
    // --- UPDATED --- Make OTPUsers static so that the same list is shared
    private static List<String> OTPUsers = new ArrayList<>();
    
    /**
     * Constructs a OneTimePassword instance with the specified DatabaseHelper.
     *
     * @param DBHelper the database helper used for database operations related to OTPs.
     */
    public OneTimePassword(DatabaseHelper DBHelper) {
        this.DBHelper = DBHelper;
    }
    
    /**
     * Generates a new one-time password (OTP) and stores it temporarily in the database.
     *
     * @throws SQLException if an error occurs during database operations.
     */
    public void generateOTP() throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the username to reset password: ");
        String username = sc.nextLine();
        
        if (!DBHelper.doesUserExist(username)) {
            System.out.println("Invalid username!");
            return;
        }
        
        // --- UPDATED --- Now generate a 6-digit numeric OTP
        String oneTimePass = generateOTP(6);
        String userRoleString = DBHelper.getUserRoles(username);
        // Decode the userRoleString into a List<String>
        List<String> userRoles = (userRoleString != null) ? Arrays.asList(userRoleString.split(",")) : new ArrayList<>();
        
        // Instead of registering a new user, update the existing password with the OTP.
        if (DBHelper.updatePassword(username, oneTimePass)) {
            OTPUsers.add(username);
            System.out.println("The One Time password for the user " + username + " is: " + oneTimePass);
        } else {
            System.out.println("Failed to update password for user " + username);
        }
        // Note: Do not close the Scanner here if it will be reused.
    }
    
    /**
     * NEW METHOD:
     * Generates an OTP for a specific user.
     * This is intended to be used by the admin interface.
     * @param username The username for which to generate the OTP.
     * @return The generated OTP string.
     * @throws SQLException if a database error occurs.
     */
    public String generateOTPForUser(String username) throws SQLException {
        if (!DBHelper.doesUserExist(username)) {
            throw new SQLException("Invalid username!");
        }
        
        // Generate a 6-digit numeric OTP.
        String oneTimePass = generateOTP(6);
        String userRoleString = DBHelper.getUserRoles(username);
        List<String> userRoles = (userRoleString != null) ? Arrays.asList(userRoleString.split(",")) : new ArrayList<>();
        
        // Update the user's password with the generated OTP.
        if (DBHelper.updatePassword(username, oneTimePass)) {
            OTPUsers.add(username);
            return oneTimePass;
        } else {
            throw new SQLException("OTP generation failed for " + username);
        }
    }
    
    /**
     * Handles the login process using a one-time password (OTP). This method validates the OTP
     * and performs any necessary authentication steps.
     *
     * @throws SQLException if an error occurs during database operations.
     */
    public void handleLogin() throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter the username: ");
        String username = sc.nextLine();
        System.out.println("Please enter the password (OTP): ");
        String pass = sc.nextLine();
        
        if (!DBHelper.doesUserExist(username)) {
            System.out.println("Invalid User!");
            return;
        }
        
        String userRoleString = DBHelper.getUserRoles(username);
        List<String> userRoles = (userRoleString != null) ? Arrays.asList(userRoleString.split(",")) : new ArrayList<>();
        User nUser = new User(username, pass, userRoles, userRoleString);
        
        // Verify that the OTP (currently stored as password) is correct.
        if (DBHelper.login(nUser)) {
            System.out.println("OTP verified. Create a new password: ");
            String newPass;
            String isValid;
            do {
                System.out.println("Set your new password: ");
                newPass = sc.nextLine();
                isValid = PasswordEvaluator.evaluatePassword(newPass);
                if (!isValid.isEmpty()) {
                    System.out.println("Error occurred! " + isValid);
                }
            } while (!isValid.isEmpty());
            
            // Update the user's password with the new password.
            if (DBHelper.updatePassword(username, newPass)) {
                clearTempPass(username);
                System.out.println("Your password has been updated. Please log in again.");
            } else {
                System.out.println("Failed to update your password.");
            }
        } else {
            System.out.println("Your password doesn't satisfy the conditions or OTP is incorrect!");
        }
        sc.close();
    }
    

    /**
     * Checks if a temporary password exists for the specified user.
     *
     * @param user the username for which to check the temporary password.
     * @return true if a temporary password exists for the user, false otherwise.
     */
    public static boolean hasTempPass(String user) {
        return OTPUsers.contains(user);
    }
    
    /**
     * Clears the temporary password for the specified user.
     *
     * @param user the username for which the temporary password will be cleared.
     */
    public static void clearTempPass(String user) {
        OTPUsers.remove(user);
    }
    
    /**
     * PRIVATE HELPER METHOD:
     * Generates an OTP string of the given length.
     * For a 6-digit numeric OTP, call generateOTP(6).
     */
    private String generateOTP(int len) {
        // Calculate the maximum value (e.g., 10^6 for a 6-digit code).
        int max = (int) Math.pow(10, len);
        // Generate a random integer between 0 (inclusive) and max (exclusive).
        int otp = rand.nextInt(max);
        // Format the OTP with leading zeros if necessary.
        return String.format("%0" + len + "d", otp);
    }
    
    // The following shuffled() method is no longer needed when generating a numeric OTP.
    // You may remove it or leave it for future use.
    private String shuffled(String s) {
        char[] arr = s.toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int idx = rand.nextInt(i + 1);
            char t = arr[i];
            arr[i] = arr[idx];
            arr[idx] = t;
        }
        return new String(arr);
    }
}
