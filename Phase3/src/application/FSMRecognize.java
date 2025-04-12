package application;

/**
 * The FSMRecognize class provides a method to validate if an input string meets
 * specific criteria for password strength. The criteria include the presence of at least:
 * <ul>
 *   <li>one uppercase letter,</li>
 *   <li>one lowercase letter,</li>
 *   <li>one numeric character, and</li>
 *   <li>one special character.</li>
 * </ul>
 */
public class FSMRecognize {


    /**
     * A string that holds an error message related to password validation.
     */
    public static String PWErrorMessage = "";
    private static final String specChar = "~`!@#$%^&*()_+-={}[]|:,.?/";     // Allowed special characters for validation

    /**
     * Default constructor for FSMRecognize.
     */
    public FSMRecognize() {
        // Default constructor
    }

    /**
     * Validates the input string based on specific password criteria.
     * <p>
     * The validation checks if the input contains at least:
     * </p>
     * <ul>
     *   <li>one uppercase letter,</li>
     *   <li>one lowercase letter,</li>
     *   <li>one numeric character, and</li>
     *   <li>one special character.</li>
     * </ul>
     * <p>
     * Example:
     * <pre>
     * if (upperCase &amp;&amp; lowerCase &amp;&amp; numericChar &amp;&amp; specialChar) {
     *     // The input is valid.
     * }
     * </pre>
     *
     * @param input the input string to be validated
     * @return true if the input meets all the required criteria, false otherwise
     */
    public static boolean validate(String input) {
    	int count = 0; //This is the character counter for the length of a userName
    	boolean upperCase = false; //upperCase boolean flag set to false
        boolean lowerCase = false; //lowerCase boolean flag set to false
        boolean numericChar = false; //numericChar boolean flag set to false
        boolean specialChar = false; //specialChar boolean flag set to false
        boolean otherChar = false; //otherChar boolean flag set to false
        
        for (char c : input.toCharArray()) {  // This goes through each character of the userName entered. 
            if (Character.isUpperCase(c)) upperCase = true; //If a character entered is an uppercase letter, the boolean flag becomes true, and this requirement is done because you only need "AT LEAST" 1 uppercase. 
            else if (Character.isLowerCase(c)) lowerCase = true; //If the character is a lowercase letter, the flag is true and the requirement is fulfilled.
            else if (Character.isDigit(c)) numericChar = true; //If the character is a number, the flag is true and the requirement is fulfilled.
            else if (specChar.indexOf(c) != -1) specialChar = true; //if the character is a special character or symbol, the requirement is fulfilled. 
            else {
            	otherChar = true; //Anything else is "otherChar". This flag checks if there are any invalid characters input into the code because it does NOT fall into the requirements category with the others.  
            	PWErrorMessage = "This username contains an invalid character!"; //Error message printed since an invalid character was entered.
            	return false; 
            }
            count++;
        }
        
        //Conditional statements checking the requirements
        if (!upperCase) {
        	PWErrorMessage = "This username is not valid because it does not contain at least one uppercase letter. Please try again!"; 
        	return false;
        }
        
        if (count < 8) {
            PWErrorMessage = "This username is not long enough! It must be at least 8 characters long.";
            return false;
        }
        
        if (!lowerCase) {
        	PWErrorMessage = "This username is not valid because it does not contain at least one lowercase letter. Please try again!"; 
        	return false;
        	
        }
        
        if (!numericChar) {
        	PWErrorMessage = "This username is not valid because it does not contain at least one number. Please try again!"; 
        	return false;
        	
        }
        
        if (!specialChar) {
        	PWErrorMessage = "This username is not valid because it does not contain at least one special character or symbol. Please try again!"; 
        	return false;
        	
        }
        
        PWErrorMessage = ""; 
        return true;
        
    }


    /**
     * Provides the error message for invalid input.
     *
     * @return the error message if validation fails, or an empty string if validation succeeds.
     */
   
    public static String getErrorMessage() {
        return PWErrorMessage;
    }

}
