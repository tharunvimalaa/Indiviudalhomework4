package application;
import javafx.beans.property.SimpleStringProperty;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import databasePart1.*;

/**
 * Page to view all users as well as delete user accounts (add on additional actions related to editing user info here if needed)
 * 
 * Made by: Eliza Chook
 */
public class ViewAccountPage {
	
	private final DatabaseHelper databaseHelper;

	public ViewAccountPage(DatabaseHelper databaseHelper) {
	    this.databaseHelper = databaseHelper;
	}

    private TableView<User> tableView;//for making table
    private ObservableList<User> userList;//for list
    //for getting database (idk why just using DatabaseHelper isn't working)
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  
    static final String USER = "sa"; 
    static final String PASS = ""; 
    
    //got rid of the datahelper since didn't use due to issues with connection - Eliza
    
    @SuppressWarnings({ "unchecked" })
    public void show(Stage primaryStage) throws Exception {
        VBox layout = new VBox(5);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // TableView setup (username in first column, role, in 2nd, and delete in 3rd)
        //add on more columns for more actions if needed
        tableView = new TableView<>();
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.join(", ", cellData.getValue().getRoles()))
        );
        
        TableColumn<User, Void> actionCol = new TableColumn<>("Delete Account");
        actionCol.setCellFactory(a -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(a -> {
                    User user = getTableView().getItems().get(getIndex());
                    confirmAndDeleteUser(user.getUserName());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
        usernameCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3)); // 30%
        emailCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.4)); // 40%
        roleCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.2)); // 20%
        actionCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.2)); // 20%

        tableView.getColumns().addAll(usernameCol, emailCol, roleCol, actionCol);
        //usernameCol.prefWidthProperty().bind(tableView.widthProperty().subtract(roleCol.getPrefWidth()).subtract(actionCol.getPrefWidth())); //resize 1st column to fit table
        loadUsers();
        
        // Button to return to admin home page
        Button homeButton = new Button("Return to Home");
        homeButton.setOnAction(a -> {
            String adminUser = "admin"; // Replace with actual admin username if available
            new AdminHomePage(databaseHelper, adminUser).show(primaryStage, adminUser);
        });
        
        layout.getChildren().addAll(homeButton, tableView);
        Scene welcomeScene = new Scene(layout, 800, 400);
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("View User Accounts");
    }
    
    //connect to database and collect information on users of the system
    private void loadUsers() {
        userList = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        		PreparedStatement stmt = conn.prepareStatement("SELECT userName, password, role, email FROM cse360users");

             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
				String userRoleString = rs.getString("role");
				//MODIFICATION: decodes the userRoleString which has all roles joined together and stores it in a List<String>
				List<String> userRoles = new ArrayList<>(Arrays.asList(userRoleString.split(",")));
				userList.add(new User(
					    rs.getString("userName"),
					    rs.getString("password"),
					    new ArrayList<>(Arrays.asList(rs.getString("role").split(","))),
					    rs.getString("email")
					));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tableView.setItems(userList);
    }
    
    //creates pop-up to confirm the deletion of user account
    private void confirmAndDeleteUser(String username) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete " + username + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                deleteUser(username);
            }
        });
    }
    
    
    //delete user account from database
    private void deleteUser(String username) {
        String deleteQuery = "DELETE FROM cse360users WHERE userName = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            loadUsers(); // Refresh table after deletion
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}