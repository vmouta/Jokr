package com.jokrapp.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jokrapp.R;
import com.jokrapp.android.data.FavoriteJokesDataProvider;
import com.jokrapp.android.data.FollowingDataProvider;
import com.jokrapp.android.data.MyJokesDataProvider;
import com.jokrapp.android.data.RestClient;
import com.jokrapp.android.data.RestResponse;
import com.jokrapp.android.data.UserDataProvider;
import com.jokrapp.android.data.model.User;
import com.squareup.picasso.Picasso;

import java.io.File;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        super.getSupportActionBar().hide();


     /*   OkHttpDownloader d = new OkHttpDownloader(getCacheDir(), 10000000);

        // up to 10 MB on disk caching.
        Picasso picasso = new Picasso.Builder(this).downloader(d).build();
        Picasso.setSingletonInstance(picasso); */
        Picasso picasso = new Picasso.Builder(this).loggingEnabled(false).build();
        Picasso.setSingletonInstance(picasso);

        Static.updateBackground(this);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        System.out.println("Jokr App directory: " + Constants.APP_DIR);
        File appDir = new File(Constants.APP_DIR);
        File jokeDir = new File(Constants.JOKE_DIR);

        if (!appDir.exists())

        {
            appDir.mkdirs();
        }

        if (!jokeDir.exists())

        {
            jokeDir.mkdirs();
        }

        MyJokesDataProvider.getInstance().

                initialize();

        FavoriteJokesDataProvider.getInstance().

                initialize();

        UserDataProvider.getInstance().

                initialize();

        FollowingDataProvider.getInstance().

                initialize();

        try

        {
            // first time user creation
            if (UserDataProvider.getInstance().getUser() == null) {
                String android_id = Settings.Secure.getString(super.getBaseContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                RestClient rc = new RestClient();

                User user = new User();
                user.setDeviceOS("a");
                user.setDeviceId(android_id);

                JsonObject jsonObject = (new JsonParser()).parse(new Gson().toJson(user)).getAsJsonObject();

                RestResponse resp = rc.post(Constants.USER_URL, jsonObject.toString());

                if (resp.getCode() == 200 && resp.getBody() != null) {
                    user = new Gson().fromJson(new JsonParser().parse(resp.getBody()), User.class);
                    System.out.println("User created with id: " + user.get_id());
                    UserDataProvider.getInstance().saveUser(user);
                } else {
                    System.out.println("Error while creating user: " + resp.getCode() + ", " + resp.getBody());
                }
            }

        } catch (
                Throwable e
                )

        {
            e.printStackTrace();
        }

        new

                UpdateListsTask()

                .

                        execute();

        new

                Handler()

                .

                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startMainActivity();
                            }
                        }

                                , Constants.SPLASH_TIME);

    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private class UpdateListsTask extends AsyncTask<Object, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            MyJokesDataProvider.getInstance().sync();
            FavoriteJokesDataProvider.getInstance().sync();
            FollowingDataProvider.getInstance().sync();
            return true;
        }


    }

}
