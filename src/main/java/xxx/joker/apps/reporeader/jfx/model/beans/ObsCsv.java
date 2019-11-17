package xxx.joker.apps.reporeader.jfx.model.beans;

import xxx.joker.libs.core.format.JkCsv;
import xxx.joker.libs.core.lambda.JkStreams;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static xxx.joker.libs.core.lambda.JkStreams.map;

public class ObsCsv {

    private final Path csvPath;
    private final List<String> header;
    private final List<ObsObject> dataList;

    public ObsCsv(JkCsv csv) {
        this.csvPath = csv.getCsvPath();
        this.header = new ArrayList<>(csv.getHeader());
        this.dataList = map(csv.getCurrentData(true), row -> new ObsObject(csv.getHeader(), map(row, ObsObjField::new)));
    }

    public void rollback() {
        dataList.forEach(ObsObject::rollback);
    }
    public void commit() {
        dataList.forEach(ObsObject::commit);
    }

    public List<String> getHeader() {
        return header;
    }

    public Path getCsvPath() {
        return csvPath;
    }

    public List<ObsObject> getDataList() {
        return dataList;
    }

    public String toStrCsv() {
        JkCsv csv = new JkCsv(header, map(dataList, ObsObject::strFields));
        return JkStreams.joinLines(csv.strLines());
    }

    public boolean isChanged() {
        return JkStreams.count(getDataList(), ObsObject::isChanged) > 0;
    }
}
