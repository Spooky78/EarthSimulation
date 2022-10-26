module com.example.earthsimulation {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.earthsimulation to javafx.fxml;
    exports com.example.earthsimulation;
}