package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
created by Tomasz Bieniek
tel: 725505739
e-mail: tomabie433@student.polsl.pl
 */

public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("../resources/fxml/mainWindow.fxml"));
        primaryStage.setTitle("Solution");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
