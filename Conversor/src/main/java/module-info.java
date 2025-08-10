module com.conversor.conversor {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.conversor.conversor to javafx.fxml;
    exports com.conversor.conversor;
}