package mapReduce.mrApplication;

import java.util.List;

import mapReduce.mr.KeyValue;

public interface MapReduce {
    public List<KeyValue> map(String content);
    public String reduce(String key, List<String> values);
}
