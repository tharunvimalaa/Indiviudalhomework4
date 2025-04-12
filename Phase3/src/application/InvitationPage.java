package application;


import databasePart1.*;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * InvitePage class represents the page where an admin can generate an invitation code.
 * The invitation code is displayed upon clicking a button.
 */

public class InvitationPage {
	
	/**
     * Default constructor for InvitationPage.
     * This constructor initializes the InvitationPage instance and sets up any required components.
     */
    public InvitationPage() {}

	/**
     * Displays the Invite Page in the provided primary stage.
     * 
     * @param databaseHelper An instance of DatabaseHelper to handle database operations.
     * @param primaryStage   The primary stage where the scene will be displayed.
     */
    public void show(DatabaseHelper databaseHelper,Stage primaryStage) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display the title of the page
	    Label userLabel = new Label("Create an invite for a...");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    layout.getChildren().add(userLabel);
	    
	    //--- MODIFIED: Check box to select user roles
	    String roles[] = {"admin", "student", "instructor", "staff", "reviewer"};
	    String[] selectedRoles = {"", "", "", "", ""}; //To be filled in by check box
	    
	    for (int i = 0; i < roles.length; i++) {
	    	//Create and add the CheckBox
	    	CheckBox roleOption = new CheckBox(roles[i]);
	    	layout.getChildren().add(roleOption);
	    	
    		final int idx = i; //Needed to use value of i within EventHandler
	    	EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() { 
                public void handle(ActionEvent e) 
                { 
                    if (roleOption.isSelected()) {
                    	//Add a '.' + the first and last char of the role to the selectedRoles array in the proper index
                    	selectedRoles[idx] = "." + roles[idx].substring(0,1) + roles[idx].substring(roles[idx].length()-1, roles[idx].length());
                    } else {
                    	//Clear the proper index of the selectedRoles array by setting it to an empty string
                    	selectedRoles[idx] = "";
                    }
                } 
            }; 
            roleOption.setOnAction(event);
	    }
	    Label blank = new Label(""); //Blank label for whitespace
	    //END---
	    
	    // Button to generate the invitation code
	    Button showCodeButton = new Button("Generate Invitation Code");
	    
	    // Label to display the generated invitation code
	    Label inviteCodeLabel = new Label(""); ;
        inviteCodeLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");
        
        
        showCodeButton.setOnAction(a -> {
        	boolean roleAssigned = false; 
        	for (int i = 0; i < selectedRoles.length; i++) {
        		if (selectedRoles[i] != "") {
        			roleAssigned = true;
        		}
        	}
        	if (roleAssigned) {
	        	// Generate the invitation code using the databaseHelper and set it to the label
	            String invitationCode = databaseHelper.generateInvitationCode(selectedRoles);
	            inviteCodeLabel.setText(invitationCode);
        	} else {
        		blank.setText("You must assign a role.");
        	}
        });
	    

        layout.getChildren().addAll(blank, showCodeButton, inviteCodeLabel);
	    Scene inviteScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(inviteScene);
	    primaryStage.setTitle("Invite Page");
    	
    }
}