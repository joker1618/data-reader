package xxx.joker.apps.datareader.jfx.view.beans;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xxx.joker.apps.datareader.jfx.model.beans.ObsField;
import xxx.joker.apps.datareader.jfx.model.beans.ObsItem;
import xxx.joker.libs.core.util.JkStrings;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static xxx.joker.libs.core.lambda.JkStreams.*;
import static xxx.joker.libs.core.util.JkStrings.safeTrim;

@Component
public class FilterObj extends ObjectBinding<Predicate<ObsItem>> {

    private Map<String, SimpleStringProperty> fieldMap = new HashMap<>();

    public void reset(Collection<String> fieldNames) {
        fieldMap.values().forEach(this::unbind);
        fieldMap.clear();
        fieldNames.forEach(fn -> fieldMap.put(fn, new SimpleStringProperty()));
        fieldMap.values().forEach(this::bind);
    }

    public void bindValue(String fname, StringBinding strBinding) {
        fieldMap.get(fname).bind(strBinding);
    }

    @Override
    protected Predicate<ObsItem> computeValue() {
        return this::testFilter;
    }

    private boolean testFilter(ObsItem obsItem) {
        Map<String, ObsField> fmap = obsItem.getObsFieldMap();
        int numWrong = count(fieldMap.entrySet(), e -> {
            ObsField of = fmap.get(e.getKey());
            String expected = e.getValue().getValueSafe();
            return StringUtils.isNotBlank(expected) && of != null
                    && !StringUtils.containsIgnoreCase(safeTrim(of.getCurrentValue()), expected);
        });
        return numWrong == 0;
    }

}