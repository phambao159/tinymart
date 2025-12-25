package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("inventory","login"), 1024, 768);
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String path,String fxml) throws IOException {
        scene.setRoot(loadFXML(path,fxml));
    }


    private static Parent loadFXML(String path,String fxml) throws IOException {

        String resourcePath = path+ "/" + fxml + ".fxml";

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getClassLoader().getResource(resourcePath));

        if (fxmlLoader.getLocation() == null) {
            throw new IOException("FXML file not found at: " + resourcePath);
        }

        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
