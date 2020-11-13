package pl.rafalrozek;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class Controller {
    @FXML
    public ListView visitsList;


    public void visitsListRemoveItem(String s) {
        Platform.runLater( () ->{
            this.visitsList.getItems().remove(s);
        });

    }

    public void visitsListAddItem(String s) {
        Platform.runLater( () ->{
            this.visitsList.getItems().add(s);
        });


    }
}
