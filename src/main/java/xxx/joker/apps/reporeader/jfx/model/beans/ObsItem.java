package xxx.joker.apps.reporeader.jfx.model.beans;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.libs.core.lambda.JkStreams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static xxx.joker.libs.core.util.JkStrings.strf;

public class ObsItem extends BooleanBinding {

    private static final Logger LOG = LoggerFactory.getLogger(ObsItem.class);

    private final SimpleListProperty<String> header;
    private final SimpleListProperty<ObsField> obsFields;

    public ObsItem(List<String> header, Collection<ObsField> obsFields) {
        this.header = new SimpleListProperty<>(FXCollections.observableArrayList(header));
        this.obsFields = new SimpleListProperty<>(FXCollections.observableArrayList(obsFields));
        this.obsFields.forEach(this::bind);
    }

    public List<ObsField> getObsFields() {
        return obsFields;
    }

    public void rollback() {
        obsFields.forEach(ObsField::rollback);
    }
    public void commit() {
        obsFields.forEach(ObsField::commit);
    }

    public List<String> getHeader() {
        return new ArrayList<>(header);
    }

    public List<String> strFields() {
        return JkStreams.map(obsFields, ObsField::getCurrentValue);
    }

    @Override
    public String toString() {
        List<String> elems = new ArrayList<>();
        for(int i = 0; i < header.size(); i++) {
            ObsField oof = getObsFields().get(i);
            String s = strf("{}: {}", getHeader().get(i), oof.toString());
            elems.add(s);
        }
        return strf("[{}]", JkStreams.join(elems, ", "));
    }
    // true == changed
    @Override
    protected boolean computeValue() {
        LOG.debug("obsItem changed = {},  {}", isChanged(), toString());
        return isChanged();
    }
    public boolean isChanged() {
        return JkStreams.count(obsFields, ObsField::isChanged) > 0;
    }
}