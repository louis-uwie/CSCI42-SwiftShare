module com.csci42_2 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.csci42_2 to javafx.fxml;
    exports com.csci42_2;
}
