package com.jokrapp.android.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jokrapp.android.Constants;
import com.jokrapp.android.data.model.Joke;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by pat on 10/27/2015.
 */
public class MyJokesDataProvider {
    private static MyJokesDataProvider ourInstance = new MyJokesDataProvider();

    private ArrayList<Joke> myJokes = null;
    private File jsonFile = new File(Constants.APP_DIR + "/myJokes.json");

    public static MyJokesDataProvider getInstance() {
        return ourInstance;
    }

    private MyJokesDataProvider() {
    }

    public void initialize() {
        loadFromJson();
    }

    private void loadFromJson() {
        // Read from File to String
        myJokes = new ArrayList<Joke>();

        try {
            JsonElement jsonElement = new JsonParser().parse(new FileReader(jsonFile));
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray allJokes = jsonObject.getAsJsonArray("jokes");
            System.out.println(allJokes);
            Iterator it = allJokes.iterator();
            while (it.hasNext()) {
                Joke obj = new Gson().fromJson((JsonElement) it.next(), Joke.class);
                myJokes.add(obj);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No file exists yet.");
        }
    }

    public ArrayList<Joke> getJokes() {
        if (myJokes != null) {
            return myJokes;
        } else {
            loadFromJson();
            return myJokes;
        }
    }

    public void deleteJoke(Joke joke) {
        myJokes.remove(joke);
        persistToJson();
    }

    public void addJoke(Joke joke) {
        myJokes.add(0, joke);
        persistToJson();
    }

    public void updateJoke(Joke joke) {
        update(joke);

        persistToJson();
    }

    private Boolean update(Joke joke) {
        for (int i = 0; i < myJokes.size(); i++) {
            if (myJokes.get(i).equals(joke)) {
                myJokes.set(i, joke);
                return true;
            }
        }
        return false;
    }

    public void sync() {
        if (UserDataProvider.getInstance().getUser() != null) {
            RestClient client = new RestClient();
            RestResponse resp = client.get(Constants.JOKE_OF_USER + "?userId=" + UserDataProvider.getInstance().getUser().get_id());

            if (resp.getCode() != 200) {
                return;
            }

            JsonElement jsonElement = new JsonParser().parse(resp.getBody());
            JsonArray allJokes = jsonElement.getAsJsonArray();

            System.out.println(allJokes.size() + " jokes found to sync.");
            System.out.println(allJokes);
            for (int i = 0; i < allJokes.size(); i++) {
                Joke joke = new Gson().fromJson(allJokes.get(i), Joke.class);
                if (!update(joke)) {
                    // only on server but not locally, therefore add it
                    addJoke(joke);
                }
            }

            persistToJson();
        }
    }

    private void persistToJson() {

        try {
            jsonFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonArray allJokes = new JsonArray();
        Gson gson = new Gson();

        for (Joke j : myJokes) {
            JsonObject jsonObject = (new JsonParser()).parse(gson.toJson(j)).getAsJsonObject();
            allJokes.add(jsonObject);

        }

        PrintWriter out = null;
        try {
            out = new PrintWriter(jsonFile);
            JsonObject obj = new JsonObject();
            obj.add("jokes", allJokes);
            out.print(obj.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }
}
