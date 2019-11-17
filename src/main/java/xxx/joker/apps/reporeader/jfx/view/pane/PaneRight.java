package xxx.joker.apps.reporeader.jfx.view.pane;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xxx.joker.apps.reporeader.jfx.model.GuiModel;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsObjField;
import xxx.joker.apps.reporeader.jfx.view.controls.GridPaneBuilder;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkStrings;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static xxx.joker.libs.core.util.JkStrings.safeTrim;
import static xxx.joker.libs.core.util.JkStrings.strf;

@Component
public class PaneRight extends VBox {

    @Autowired
    private GuiModel guiModel;

    @PostConstruct
    public void init() {
    // public PaneRight() {
        GridPaneBuilder gpBuilder = new GridPaneBuilder();

        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        guiModel.selTableItemProperty().addListener((obs, o, n) -> {
            double wbefore = getWidth();
            getChildren().clear();
            if(n != null) {
                List<String> jsonLines = new ArrayList<>();
                for(int i = 0; i < n.getHeader().size(); i++) {
                    gpBuilder.add(i, 0, n.getHeader().get(i));
                    gpBuilder.add(i, 1, createTextFieldVBox(n.getObsFields().get(i)));
                    jsonLines.add(strf("\"{}\": \"{}\"", n.getHeader().get(i), n.getObsFields().get(i).getCurrentValue()));
                }
                getChildren().add(gpBuilder.createGridPane());

                String jsonString = JkStreams.join(jsonLines, ",\n", s -> JkStrings.leftPadLines(s, " ", 4));
                TextArea textArea = new TextArea(strf("{\n{}\n}", jsonString));
//                getChildren().add(textArea);

            } else {
                setPrefWidth(wbefore);
            }
        });

        getStyleClass().addAll("rootRight");
    }

    private VBox createTextFieldVBox(ObsObjField obsField) {
        VBox vbox = new VBox();

        Label label = new Label(obsField.getOrigValue());
        label.setTextFill(Color.RED);
        obsField.addListener((obs,o,n) -> {
            if(n) {
                vbox.getChildren().add(0, label);
            } else {
                vbox.getChildren().remove(label);
            }
        });
        if(obsField.get()) {
            vbox.getChildren().add(label);
        }

        TextField tf = new TextField(obsField.getCurrentValue());
        tf.focusedProperty().addListener((obs,o,n) -> {
            if(!n)  {
                String trim = safeTrim(tf.getText());
                tf.setText(trim);
                obsField.setCurrentValue(trim);
            }
        });
        // in case of rollback
        obsField.addListener((obs,o,n) -> {
            if(!n && o) {
                tf.setText(obsField.getCurrentValue());
            }
        });
        vbox.getChildren().add(tf);

        return vbox;
    }

}
