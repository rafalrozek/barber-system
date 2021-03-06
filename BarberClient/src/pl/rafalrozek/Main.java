package pl.rafalrozek;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import pl.rafalrozek.BarberClient.BarberClient;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainForm.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Braber Client");
        primaryStage.setScene(new Scene(root, 300, 300));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("icons/barber.png")));
        primaryStage.show();

        Controller ctrl = loader.getController();

        BarberClient fc = new BarberClient("localhost", 1337, ctrl);
        fc.start();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
