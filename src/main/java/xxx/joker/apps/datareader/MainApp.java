package xxx.joker.apps.datareader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.scenicview.ScenicView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import xxx.joker.apps.datareader.jfx.view.JfxRootController;
import xxx.joker.libs.core.runtime.JkEnvironment;
import xxx.joker.libs.core.util.JkConvert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static xxx.joker.libs.core.lambda.JkStreams.map;


@SpringBootApplication
public class MainApp extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MainApp.class);

    private ConfigurableApplicationContext context;
    private Parent rootNode;
    private JfxRootController rootController;

    public static void main(String[] args) {
        launch(MainApp.class, args);
    }

    @Override
    public void init() throws Exception {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(MainApp.class);
        context = builder.run(getParameters().getRaw().toArray(new String[0]));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        loader.setControllerFactory(context::getBean);
        rootNode = loader.load();
        rootController = loader.getController();
    }


    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(rootNode);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        Platform.setImplicitExit(false);
        stage.setOnCloseRequest(e -> {
            if(rootController.canCloseApp()) {
                LOG.debug("Platform shutdown");
                Platform.exit();
            } else {
                e.consume();
            }
        });

        List<String> params = new ArrayList<>(getParameters().getRaw());

        boolean scenicView = !params.isEmpty() && params.get(0).equals("-sv");
        if(scenicView)  params.remove(0);

        List<Path> paths = new ArrayList<>(map(params, par -> Paths.get(JkConvert.unixToWinPath(par))));

        if(paths.isEmpty()) {
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(JkEnvironment.getHomeFolder().resolve("Desktop").toFile());
            List<File> files = fc.showOpenMultipleDialog(stage);
            if(files != null)
                paths.addAll(map(files, File::toPath));
        }

        if(paths.isEmpty()) {
            Platform.exit();
        } else {
            stage.setTitle("DATA READER");
            rootController.initApp(paths);
            if (scenicView)
                ScenicView.show(scene);
        }
    }

    @Override
    public void stop() throws Exception {
        LOG.debug("Stop app");
        context.close();
    }
}
