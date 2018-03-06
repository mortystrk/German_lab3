package bstu.by.glossary.Dictionary;


import java.util.ArrayList;

public final class Dictionary {

    private ArrayList<String> keys;
    private ArrayList<String> values;

    public Dictionary() {
        keys = new ArrayList<>();
        values = new ArrayList<>();
    }

    public void add(String key, String value) {
        keys.add(key);
        values.add(value);
    }

    public String get(int index) {
        return values.get(index);
    }

    public String get(String key) {
        int index = -1;

        for (int i = 0; i < keys.size(); i++) {
            if (key.equals(keys.get(i)))
                index = i;
        }

        if (index == -1) {
            return null;
        } else {
            return values.get(index);
        }
    }

    public String returnKey(int index) {
        return keys.get(index);
    }

    public String returnKey(String value) {
        for (int i = 0; i < keys.size(); i++) {
            if (values.get(i).equals(value))
                return keys.get(i);
        }

        return null;
    }

    public int size() {
        return keys.size();
    }
}
