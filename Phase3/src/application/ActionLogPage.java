package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class ActionLogPage {
    private DatabaseHelper databaseHelper;
    
    public ActionLogPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    public void show(Stage primaryStage) {
        TableView<ActionLog> tableView = new TableView<>();
        ObservableList<ActionLog> logList = FXCollections.observableArrayList();
        
        TableColumn<ActionLog, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<ActionLog, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        
        TableColumn<ActionLog, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("actionDescription"));
        
        TableColumn<ActionLog, String> timeCol = new TableColumn<>("Log Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("logTime"));
        
        tableView.getColumns().addAll(idCol, userCol, actionCol, timeCol);
        
        try {
            List<ActionLog> logs = databaseHelper.getActionLogs();
            logList.addAll(logs);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load action logs: " + e.getMessage());
        }
        tableView.setItems(logList);
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            primaryStage.setScene(primaryStage.getScene());
        });
        
        VBox layout = new VBox(10, new Label("Application Action Logs"), tableView, backButton);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 800, 600);
        Stage logStage = new Stage();
        logStage.setScene(scene);
        logStage.setTitle("Action Logs");
        logStage.show();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
