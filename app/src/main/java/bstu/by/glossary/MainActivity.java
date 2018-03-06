package bstu.by.glossary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import bstu.by.glossary.Dictionary.Dictionary;
import bstu.by.glossary.translation.Translation;

import static bstu.by.glossary.Levenshtein.Levenshtein.*;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Glossary";
    private final String fileName = "dictionary.json";
    private File file;
    private String[] rusWords = new String[] {"Молоко", "Сыр", "Творог", "Соль", "Сахар", "Хлеб", "Вода", "Чай", "Кофе", "Мука"};
    private String[] engWords = new String[] {"Milk", "Cheese", "Curd", "Salt", "Sugar", "Bread", "Water", "Tea", "Coffee", "Flour"};
    private ArrayList<String> mostAppropriateWords;
    private ArrayList<String> lessSuitableWords;
    private Dictionary dictionary, reverseDictionary;
    private Translation translation;

    private EditText et_forTranslate, et_newRusWord, et_newEngWord;
    private TextView tv_translate, tv_translationInfo;
    private Button btn_translate, btn_goodTranslate, btn_badTranslate, btn_addToDictionary;
    private ImageButton btn_changeTranslation;

    private int mostAppropriateWordsCounter = 0;
    private int lessSuitableWordsCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mostAppropriateWords = new ArrayList<>();
        lessSuitableWords = new ArrayList<>();

        dictionary = new Dictionary();
        reverseDictionary = new Dictionary();

        translation = Translation.RU_ENG;

        et_newRusWord = findViewById(R.id.et_word);
        et_newEngWord = findViewById(R.id.et_translation);

        btn_addToDictionary = findViewById(R.id.btn_add_to_dictionary);
        btn_addToDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_newRusWord.length() == 0 || et_newEngWord.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Не все поля заполненны", Toast.LENGTH_SHORT).show();
                    return;
                }

                String rusWord = et_newRusWord.getText().toString();
                String engWord = et_newEngWord.getText().toString();

                dictionary.add(rusWord, engWord);
                reverseDictionary.add(engWord, rusWord);
                saveDictionary(file);
                et_newRusWord.setText("");
                et_newEngWord.setText("");
                Toast.makeText(getApplicationContext(), "Слово добавленно в словарь", Toast.LENGTH_SHORT).show();
            }
        });

        et_forTranslate = findViewById(R.id.et_to_translate);
        tv_translate = findViewById(R.id.tv_translation);
        tv_translationInfo = findViewById(R.id.tv_translationInfo);
        btn_badTranslate = findViewById(R.id.btn_bad_translation);
        btn_badTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mostAppropriateWordsCounter < mostAppropriateWords.size()) {
                    tv_translate.setText("Возможно вы имелли ввиду: " + mostAppropriateWords.get(mostAppropriateWordsCounter) + ", тогда перевод: " + dictionary.get(mostAppropriateWords.get(mostAppropriateWordsCounter)));
                    mostAppropriateWordsCounter++;
                    return;
                }
                if (lessSuitableWordsCounter < lessSuitableWords.size()) {
                    tv_translate.setText("Возможно вы имелли ввиду: " + lessSuitableWords.get(lessSuitableWordsCounter) + ", тогда перевод: " + dictionary.get(lessSuitableWords.get(lessSuitableWordsCounter)));
                    lessSuitableWordsCounter++;
                    return;
                }

                tv_translate.setText("");
                Toast.makeText(getApplicationContext(), "Перевод невозможен", Toast.LENGTH_SHORT).show();
                btn_goodTranslate.setVisibility(View.INVISIBLE);
                btn_badTranslate.setVisibility(View.INVISIBLE);
            }
        });
        btn_goodTranslate = findViewById(R.id.btn_good_translation);
        btn_goodTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Отлично!", Toast.LENGTH_SHORT).show();
                mostAppropriateWordsCounter = 0;
                lessSuitableWordsCounter = 0;
                btn_badTranslate.setVisibility(View.INVISIBLE);
                btn_goodTranslate.setVisibility(View.INVISIBLE);
            }
        });
        btn_translate = findViewById(R.id.btn_translate);
        btn_translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_forTranslate.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Введите слова для перевода!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int distance;
                boolean translated = false;
                String word = et_forTranslate.getText().toString();

                switch (translation) {
                    case RU_ENG:
                        mostAppropriateWords.clear();
                        lessSuitableWords.clear();

                        for (int i = 0; i < dictionary.size(); i++) {
                            distance = getDistance(word, dictionary.returnKey(i));
                            if (distance == 0) {
                                tv_translate.setText(dictionary.get(i));
                                translated = true;
                                et_forTranslate.setText("");
                                mostAppropriateWords.clear();
                                lessSuitableWords.clear();
                                break;
                            }
                            if (distance == 1) {
                                mostAppropriateWords.add(dictionary.returnKey(i));
                                continue;
                            }
                            if (distance == 2) {
                                lessSuitableWords.add(dictionary.returnKey(i));
                                continue;
                            }
                            if (distance > 2)
                                continue;
                        }

                        if (!translated) {
                            et_forTranslate.setText("");

                            if (mostAppropriateWords.size() == 0 && lessSuitableWords.size() == 0) {
                                Toast.makeText(getApplicationContext(), "Перевод невозможен", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (mostAppropriateWords.size() != 0) {
                                tv_translate.setText("Возможно вы имелли ввиду: " + mostAppropriateWords.get(0) + ", тогда перевод: " + dictionary.get(mostAppropriateWords.get(0)));
                                mostAppropriateWordsCounter++;
                                btn_goodTranslate.setVisibility(View.VISIBLE);
                                btn_badTranslate.setVisibility(View.VISIBLE);
                            }
                            if (mostAppropriateWords.size() == 0 && lessSuitableWords.size() != 0) {
                                tv_translate.setText("Возможно вы имелли ввиду: " + lessSuitableWords.get(0) + ", тогда перевод: " + dictionary.get(lessSuitableWords.get(0)));
                                lessSuitableWordsCounter++;
                                btn_goodTranslate.setVisibility(View.VISIBLE);
                                btn_badTranslate.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    case ENG_RU:
                        mostAppropriateWords.clear();
                        lessSuitableWords.clear();

                        for (int i = 0; i < reverseDictionary.size(); i++) {
                            distance = getDistance(word, reverseDictionary.returnKey(i));
                            if (distance == 0) {
                                tv_translate.setText(reverseDictionary.get(i));
                                translated = true;
                                et_forTranslate.setText("");
                                mostAppropriateWords.clear();
                                lessSuitableWords.clear();
                                break;
                            }
                            if (distance == 1) {
                                mostAppropriateWords.add(reverseDictionary.returnKey(i));
                                continue;
                            }
                            if (distance == 2) {
                                lessSuitableWords.add(reverseDictionary.returnKey(i));
                                continue;
                            }
                            if (distance > 2)
                                continue;
                        }

                        if (!translated) {
                            et_forTranslate.setText("");

                            if (mostAppropriateWords.size() == 0 && lessSuitableWords.size() == 0) {
                                Toast.makeText(getApplicationContext(), "Перевод невозможен", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (mostAppropriateWords.size() != 0) {
                                tv_translate.setText("Возможно вы имелли ввиду: " + mostAppropriateWords.get(0) + ", тогда перевод: " + reverseDictionary.get(mostAppropriateWords.get(0)));
                                mostAppropriateWordsCounter++;
                                btn_goodTranslate.setVisibility(View.VISIBLE);
                                btn_badTranslate.setVisibility(View.VISIBLE);
                            }
                            if (mostAppropriateWords.size() == 0 && lessSuitableWords.size() != 0) {
                                tv_translate.setText("Возможно вы имелли ввиду: " + lessSuitableWords.get(0) + ", тогда перевод: " + reverseDictionary.get(lessSuitableWords.get(0)));
                                lessSuitableWordsCounter++;
                                btn_goodTranslate.setVisibility(View.VISIBLE);
                                btn_badTranslate.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                        default: return;
                }
            }
        });

        btn_changeTranslation = findViewById(R.id.btn_changeTranslation);
        btn_changeTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (translation == Translation.RU_ENG) {
                    translation = Translation.ENG_RU;
                    tv_translationInfo.setText("Перевести на русский: ");
                } else {
                    translation = Translation.RU_ENG;
                    tv_translationInfo.setText("Перевести на английский: ");
                }
            }
        });

        file = new File(getFilesDir(), fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
                fillJSON(file);
            } catch (IOException e) {
                Log.e(TAG, "Не удалось создать файл");
            }
        }

        fillDictionary(file);
    }

    private void fillJSON(File f) {
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();

        for (int i = 0; i < rusWords.length; i++) {
            JSONObject item = new JSONObject();
            item.put(rusWords[i], engWords[i]);
            array.add(item);
        }

        object.put("Dictionary", array);

        try (FileWriter writer = new FileWriter(f)) {
            writer.write(object.toJSONString());
        } catch (IOException e) {
            Log.e(TAG, "Ошибка: FileWriter");
        }
    }
    private void fillDictionary(File f) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(new FileReader(f));
            JSONArray array = (JSONArray) object.get("Dictionary");

            for(Object item : array) {
                Set<String> keys = ((JSONObject) item).keySet();
                Iterator<?> i = keys.iterator();
                String key = i.next().toString();
                String value = ((JSONObject) item).get(key).toString();

                dictionary.add(key, value);
                reverseDictionary.add(value, key);
            }
        } catch (IOException e) {
            Log.e(TAG, "Ошибка: FileReader");
        } catch (ParseException e) {
            Log.e(TAG, "Ошибка: Parser");
        }
    }

    private void saveDictionary(File f) {
        JSONParser parser = new JSONParser();
        JSONObject object = null;
        try {
            object = (JSONObject) parser.parse(new FileReader(f));
            JSONArray array = (JSONArray) object.get("Dictionary");

            for(int i = 0; i < dictionary.size(); i++) {
                JSONObject item = new JSONObject();
                item.put(dictionary.returnKey(i), dictionary.get(i));
                array.add(item);
            }

            object.put("Dictionary", array);

            try (FileWriter writer = new FileWriter(f)) {
                writer.write(object.toJSONString());
            }

        } catch (IOException e) {
            Log.e(TAG, "Ошибка: FileWriter");
        } catch (ParseException e) {
            Log.e(TAG, "Ошибка: Parser");
        }
    }
}
