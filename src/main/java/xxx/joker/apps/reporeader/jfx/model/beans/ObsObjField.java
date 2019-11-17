package xxx.joker.apps.reporeader.jfx.model.beans;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;

public class ObsObjField extends BooleanBinding {
    private final SimpleStringProperty origValue;
    private final SimpleStringProperty currentValue;


    public ObsObjField(String value) {
        this.origValue = new SimpleStringProperty(value);
        this.currentValue = new SimpleStringProperty(value);
        bind(origValue, currentValue);
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

    // true == changed
    @Override
    protected boolean computeValue() {
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