package xxx.joker.apps.datareader.jfx.view.beans;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xxx.joker.apps.datareader.jfx.model.beans.ObsField;
import xxx.joker.apps.datareader.jfx.model.beans.ObsItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static xxx.joker.libs.core.lambda.JkStreams.*;

@Component
public class FilterObj extends ObjectBinding<Predicate<ObsItem>> {

    private Map<String, SimpleStringProperty> fieldMap = new HashMap<>();

    public void reset(Collection<String> fieldNames) {
        fieldMap.values().forEach(this::unbind);
        fieldMap.clear();
        fieldNames.forEach(fn -> fieldMap.put(fn, new SimpleStringProperty()));
        fieldMap.values().forEach(this::bind);
    }

    public void bindValue(String fname, StringProperty textProp) {
        fieldMap.get(fname).bind(textProp);
    }

    @Override
    protected Predicate<ObsItem> computeValue() {
        return this::testFilter;
    }

    private boolean testFilter(ObsItem obsItem) {
        Map<String, ObsField> fmap = obsItem.getObsFieldMap();
        int numWrong = count(fieldMap.entrySet(), e -> {
            ObsField of = fmap.get(e.getKey());
            String expected = e.getValue().get();
            return StringUtils.isNotBlank(expected)
                    && !StringUtils.containsIgnoreCase(of.getCurrentValue().trim(), expected);
        });
        return numWrong == 0;
    }
}
