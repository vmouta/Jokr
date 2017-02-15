package com.jokrapp.android;

import android.os.Environment;

/**
 * Created by pat on 10/27/2015.
 */
public class Constants {
    public static String APP_DIR = Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/jokr";


    public static int SPLASH_TIME = 0;
    public static boolean DEMO_FETCH = true;

    public static String SERVER = "http://jokr.eu-gb.mybluemix.net";

    public static String JOKE_DIR = APP_DIR + "/jokes";

    public static String USER_URL = SERVER + "/api/user";
    public static String USER_PICTURE_UPDATE_URL = SERVER + "/addUserImage";
    public static String USER_BLOCK_URL = SERVER + "/api/user/blockUser";

    public static String JOKE_LIKE_URL = SERVER + "/api/joke/like";
    public static String JOKE_DISLIKE_URL = SERVER + "/api/joke/dislike";

    public static String JOKE_ADD_Favorite_URL = SERVER + "/api/user/addFavorite";
    public static String JOKE_DEL_Favorite_URL = SERVER + "/api/user/removeFavorite";
    public static String JOKE_GET_FAVORITES = SERVER + "/api/user/getAllFavoritesOfUser";

    public static String JOKE_FETCH_URL = SERVER + "/api/fetch";
    public static String JOKE_UPLOAD_URL = SERVER + "/receiver";
    public static String JOKE_UPDATE_URL = SERVER + "/api/joke/update";
    public static String JOKE_DELETE_URL = SERVER + "/api/joke/delete";

    public static String JOKE_OF_USER = SERVER + "/api/joke/getAllJokesOfUser";

    public static String JOKE_BINARY_FETCH = SERVER + "/fetchJoke";
    public static String USER_PIC_FETCH = SERVER + "/fetchUserPic";

    public static String[] languages = {"sde", "de", "en", "fr", "it", "es", "pt"};

    public static String USER_FOLLOW = SERVER + "/api/user/follow";
    public static String USER_UNFOLLOW = SERVER + "/api/user/unfollow";
    public static String USER_FOLLOWING = SERVER + "/api/user/getFollowers";

}
