module org.example.pleaseworkconway {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.pleaseworkconway to javafx.fxml;
    exports org.example.pleaseworkconway;
}