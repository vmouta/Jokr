package com.jokrapp.android.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jokrapp.android.Constants;
import com.jokrapp.android.data.model.User;

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
public class FollowingDataProvider {
    private static FollowingDataProvider ourInstance = new FollowingDataProvider();

    private ArrayList<User> users = null;
    private File jsonFile = new File(Constants.APP_DIR + "/following.json");


    public static FollowingDataProvider getInstance() {
        return ourInstance;
    }

    private FollowingDataProvider() {
    }

    public void initialize() {
        loadFromJson();
    }

    private void loadFromJson() {
        // Read from File to String
        users = new ArrayList<User>();

        try {
            JsonElement jsonElement = new JsonParser().parse(new FileReader(jsonFile));
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray allJokes = jsonObject.getAsJsonArray("users");
            System.out.println(allJokes);
            Iterator it = allJokes.iterator();
            while (it.hasNext()) {
                User obj = new Gson().fromJson((JsonElement) it.next(), User.class);
                users.add(obj);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No file exists yet.");
        }
    }

    public ArrayList<User> getUsers() {
        if (users != null) {
            return users;
        } else {
            loadFromJson();
            return users;
        }
    }

    public void deleteUser(User user) {
        users.remove(user);
        persistToJson();
    }

    public void addUser(User user) {
        users.add(0, user);
        persistToJson();
    }

    public void updateUser(User user) {
        update(user);

        persistToJson();
    }

    public Boolean contains(String userId){
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).get_id().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    private Boolean update(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).equals(user)) {
                users.set(i, user);
                return true;
            }
        }
        return false;
    }

    public void sync() {
        if (UserDataProvider.getInstance().getUser() != null) {
            RestClient client = new RestClient();
            RestResponse resp = client.get(Constants.USER_FOLLOWING + "?userId=" + UserDataProvider.getInstance().getUser().get_id());

            if (resp.getCode() != 200) {
                return;
            }

            JsonElement jsonElement = new JsonParser().parse(resp.getBody());
            JsonArray allJokes = jsonElement.getAsJsonArray();

            for (int i = 0; i < allJokes.size(); i++) {
                User user = new Gson().fromJson(allJokes.get(i), User.class);
                if (!update(user)) {
                    // only on server but not locally, therefore add it
                    addUser(user);
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

            for (User u : users) {
                JsonObject jsonObject = (new JsonParser()).parse(gson.toJson(u)).getAsJsonObject();
            allJokes.add(jsonObject);
        }

        PrintWriter out = null;
        try {
            out = new PrintWriter(jsonFile);
            JsonObject obj = new JsonObject();
            obj.add("users", allJokes);
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
