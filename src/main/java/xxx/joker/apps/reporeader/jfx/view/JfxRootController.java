package xxx.joker.apps.reporeader.jfx.view;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import xxx.joker.apps.reporeader.jfx.model.GuiModel;
import xxx.joker.apps.reporeader.jfx.view.pane.PaneCenter;
import xxx.joker.apps.reporeader.jfx.view.pane.PaneLeft;
import xxx.joker.apps.reporeader.jfx.view.pane.PaneRight;
import xxx.joker.apps.reporeader.jfx.view.util.DragResizer;
import xxx.joker.libs.core.file.JkFiles;

import java.nio.file.Path;
import java.util.Optional;

@Controller
public class JfxRootController {

    private static final Logger LOG = LoggerFactory.getLogger(JfxRootController.class);

    @FXML public BorderPane rootPane;

    @Autowired
    private PaneLeft paneLeft;
    @Autowired
    private PaneCenter paneCenter;
    @Autowired
    private PaneRight paneRight;

    @Autowired
    private GuiModel guiModel;

    @FXML
    public void initialize() {
        LOG.debug("init");

        rootPane.getStylesheets().add(getClass().getResource("/css/root.css").toExternalForm());
        rootPane.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        rootPane.setLeft(paneLeft);
        rootPane.setCenter(paneCenter);
        rootPane.setRight(paneRight);

        DragResizer.makeResizable(paneLeft, Side.RIGHT);
    }


    public void initApp(Path folder) {
        guiModel.setCsvPaths(JkFiles.findFiles(folder, false));
    }

    public boolean canCloseApp() {
        if(guiModel.getChangedObsObjects().isEmpty()) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(guiModel.getChangedObsObjects().size() + " uncommitted changes will be lost. Close anyway?");
        Optional<ButtonType> resp = alert.showAndWait();
        return resp.isPresent() && resp.get() == ButtonType.OK;
    }
}