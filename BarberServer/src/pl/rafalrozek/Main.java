package pl.rafalrozek;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import pl.rafalrozek.Barber.BarberServer;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainForm.fxml"));
        Parent root = loader.load();
        Controller ctrl = loader.getController();

        BarberServer bs = new BarberServer(ctrl);
        bs.start();

        primaryStage.setTitle("Braber");
        primaryStage.setScene(new Scene(root, 300, 300));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("icons/barber.png")));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
