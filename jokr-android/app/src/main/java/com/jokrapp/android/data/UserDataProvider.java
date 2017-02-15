package com.jokrapp.android.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jokrapp.android.Constants;
import com.jokrapp.android.data.model.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by pat on 10/27/2015.
 */
public class UserDataProvider {
    private static UserDataProvider ourInstance = new UserDataProvider();

    private User user;
    private File jsonFile =  new File(Constants.APP_DIR + "/user.json");


    public static UserDataProvider getInstance() {
        return ourInstance;
    }

    private UserDataProvider() {
    }

    public void initialize() {
        loadFromJson();
    }

    private void loadFromJson() {
        try {
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(jsonFile));
            user = new Gson().fromJson(jsonObject, User.class);
            System.out.println(user);
        } catch (FileNotFoundException e) {
            System.out.println("user.json doesn't exist yet. Please go ahead and create a new user from scratch.");
        }
    }

    public void saveUser(User user) {
        this.user = user;
        persistToJson();
    }

    public User getUser() {
        if(user != null) {
            return user;
        }else{
            loadFromJson();
            return user;
        }
    }

    private void persistToJson() {

        try {
            jsonFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();

        JsonObject jsonObject = (new JsonParser()).parse(gson.toJson(user)).getAsJsonObject();

        PrintWriter out = null;
        try {
            out = new PrintWriter(jsonFile);
            out.print(jsonObject.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }
}
