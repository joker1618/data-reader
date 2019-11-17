package xxx.joker.apps.reporeader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.scenicview.ScenicView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import xxx.joker.apps.reporeader.jfx.model.GuiModel;
import xxx.joker.apps.reporeader.jfx.view.JfxRootController;
import xxx.joker.libs.core.file.JkFiles;

import java.nio.file.Path;
import java.nio.file.Paths;


@SpringBootApplication
public class MainApp extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MainApp.class);

    private ConfigurableApplicationContext context;
    private Parent rootNode;
    private JfxRootController rootController;

    @Override
    public void init() throws Exception {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(MainApp.class);
        context = builder.run(getParameters().getRaw().toArray(new String[0]));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        loader.setControllerFactory(context::getBean);
        rootNode = loader.load();
        rootController = loader.getController();

//        rootNode = new MainController22();
    }


    @Override
    public void start(Stage stage) throws Exception {
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double width = visualBounds.getWidth();
        double height = visualBounds.getHeight();
        Scene scene = new Scene(rootNode, width, height);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
//        ScenicView.show(primaryStage.getScene());

//        rootNode.initi();
        Path folder = Paths.get("src\\test\\resources\\dbsimple");
        if (getParameters().getRaw().contains("-sv")) {
            ScenicView.show(scene);
        }

        Platform.setImplicitExit(false);
        stage.setOnCloseRequest(e -> {
            LOG.debug("Platform shutdown");
            Platform.exit();
        });

        rootController.initApp(folder);
    }

    @Override
    public void stop() throws Exception {
        context.close();
        System.out.println("END");
    }
}
