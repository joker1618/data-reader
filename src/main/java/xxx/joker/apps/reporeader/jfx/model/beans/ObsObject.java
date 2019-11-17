package xxx.joker.apps.reporeader.jfx.model.beans;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import xxx.joker.libs.core.lambda.JkStreams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObsObject extends BooleanBinding {
    private final SimpleListProperty<String> header;
    private final SimpleListProperty<ObsObjField> obsFields;

    public ObsObject(List<String> header, Collection<ObsObjField> obsFields) {
        this.header = new SimpleListProperty<>(FXCollections.observableArrayList(header));
        this.obsFields = new SimpleListProperty<>(FXCollections.observableArrayList(obsFields));
        this.obsFields.forEach(this::bind);
    }

    public List<ObsObjField> getObsFields() {
        return obsFields;
    }

    public void rollback() {
        obsFields.forEach(ObsObjField::rollback);
    }
    public void commit() {
        obsFields.forEach(ObsObjField::commit);
    }

    public List<String> getHeader() {
        return new ArrayList<>(header);
    }

    public List<String> strFields() {
        return JkStreams.map(obsFields, ObsObjField::getCurrentValue);
    }

    // true == changed
    @Override
    protected boolean computeValue() {
        return isChanged();
    }
    public boolean isChanged() {
        return JkStreams.count(obsFields, ObsObjField::isChanged) > 0;
    }
}