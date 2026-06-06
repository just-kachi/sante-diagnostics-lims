package lims;

import javafx.application.Application;
import javafx.stage.Stage;
import lims.utils.SceneManager;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.setPrimaryStage(stage);
        SceneManager.switchTo("/views/login.fxml", "Sante Diagnostics LIMS");
    }

    public static void main(String[] args) {
        launch(args);
    }
}