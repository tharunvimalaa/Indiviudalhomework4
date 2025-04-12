package application;
import java.util.regex.*;

/**
 * Utility class for validating email addresses.
 * <p>
 * This class provides a static method to check if an email address is valid based on a standard email format.
 * </p>
 */
public class EmailValidation {
	 /**
     * Private constructor to prevent instantiation of this utility class.
     */
	public EmailValidation() {}
	
	/**
     * Validates the given email address.
     *
     * @param email the email address to validate
     * @return true if the email address is valid, false otherwise
     */
	public static boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
	}
}