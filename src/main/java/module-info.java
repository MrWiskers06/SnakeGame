module com.snakegame.snakegame {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.snakegame.snakegame to javafx.fxml;
    exports com.snakegame.snakegame;
}