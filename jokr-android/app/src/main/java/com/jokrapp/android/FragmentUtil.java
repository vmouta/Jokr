package com.jokrapp.android;

import android.content.Intent;

import com.jokrapp.android.data.model.Joke;

/**
 * Created by pat on 11/29/2015.
 */
public class FragmentUtil {

    public Intent shareJoke(Joke joke) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "http://jokr.lol/?j=" + joke.get_id().replaceAll("[\\-]", "");
        String shareTitle = joke.getTitle();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareTitle);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        return sharingIntent;
    }


}
