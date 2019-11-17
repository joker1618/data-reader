package xxx.joker.apps.reporeader.jfx.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.SepiaTone;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import xxx.joker.apps.reporeader.jfx.model.GenServ;
import xxx.joker.apps.reporeader.jfx.model.GuiModel;
import xxx.joker.apps.reporeader.jfx.model.ServT;
import xxx.joker.apps.reporeader.jfx.view.pane.PaneLeft;
import xxx.joker.libs.core.file.JkFiles;

import java.nio.file.Path;

@Controller
public class JfxRootController {

    private static final Logger LOG = LoggerFactory.getLogger(JfxRootController.class);

    @FXML public BorderPane rootPane;

    @Autowired
    private PaneLeft paneLeft;

    @Autowired
    private ServT servT;
    @Autowired
    private GenServ genServ;

    @FXML
    public void initialize() {
        LOG.debug("init");
        rootPane.setCenter(new Label("FEDE"));

//        rootPane.setLeft(new PaneLeft());
        rootPane.setLeft(paneLeft);

        servT.doSome();
        genServ.doSomeWork();
    }


    public void initApp(Path folder) {
        GuiModel.getModel().setCsvPaths(JkFiles.findFiles(folder, false));
    }
}