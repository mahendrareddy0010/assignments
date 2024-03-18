package mrApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mr.KeyValue;

public class WordCount implements MapReduce {
    static final Random random = new Random();

    @Override
    public List<KeyValue> map(String content) {
        String[] words = content.split("[\\s\\p{Punct}]+");
        List<KeyValue> mrKeyValues = new ArrayList<>();

        for (String word : words) {
            if (word != "" && word.matches("[a-zA-Z]+")) {
                mrKeyValues.add(new KeyValue(word, "1"));
            }
        }

        try {
            Thread.sleep(random.nextInt(15) * 1000);
        } catch (InterruptedException e) {
            System.out.println("Unable to sleep for sometime");
        }

        return mrKeyValues;
    }

    @Override
    public String reduce(String key, List<String> values) {
        
        return Integer.toString(values.size());
    }

}
