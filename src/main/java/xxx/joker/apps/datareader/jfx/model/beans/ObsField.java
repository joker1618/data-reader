package xxx.joker.apps.datareader.jfx.model.beans;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static xxx.joker.libs.core.util.JkStrings.strf;

public class ObsField extends BooleanBinding {

    private static final Logger LOG = LoggerFactory.getLogger(ObsField.class);

    private final SimpleStringProperty origValue;
    private final SimpleStringProperty currentValue;

    public ObsField(String value) {
        this.origValue = new SimpleStringProperty(value);
        this.currentValue = new SimpleStringProperty(value);
        bind(origValue, currentValue);
    }

    @Override
    public String toString() {
        String s = strf("'{}'", getCurrentValue());
        if(isChanged()) s += strf(" ({})", getOrigValue());
        return s;
    }

    public String getOrigValue() {
        return origValue.get();
    }

    public SimpleStringProperty origValueProperty() {
        return origValue;
    }

    public void setOrigValue(String origValue) {
        this.origValue.set(origValue);
    }

    public String getCurrentValue() {
        return currentValue.get();
    }

    public SimpleStringProperty currentValueProperty() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue.set(currentValue);
    }

    @Override
    protected boolean computeValue() {
        LOG.trace("obsField changed = {},  {}", isChanged(), toString());
        return isChanged();
    }
    public boolean isChanged() {
        return !getOrigValue().equals(getCurrentValue());
    }

    public void rollback() {
        currentValue.set(origValue.getValue());
    }
    public void commit() {
        origValue.set(currentValue.getValue());
    }

}