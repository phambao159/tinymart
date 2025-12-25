module project.group3.tinymart {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;

    opens main to javafx.fxml, javafx.controls; 


    exports main;
    

    opens util to javafx.fxml;
    opens controller.manager to javafx.fxml;
    opens model.manager to javafx.base;
    

}