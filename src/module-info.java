module Finans_takip {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	requires javafx.graphics;
	requires com.google.gson;
	
	opens application to javafx.graphics, javafx.fxml;
}
