module FoundationCode {
    requires javafx.controls;
    requires java.sql;
    requires javafx.base;
	requires javafx.graphics;
    requires javafx.fxml;


    opens application to javafx.graphics, javafx.fxml, javafx.base;
}
