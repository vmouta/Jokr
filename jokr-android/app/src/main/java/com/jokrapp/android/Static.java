package com.jokrapp.android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;

import com.jokrapp.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by pat on 12/15/2015.
 */
public class Static {
    public static Drawable background;


    public static void updateBackground(Activity activity){

        new UpdateBackgroundTask().execute(activity);
    }

    private static class UpdateBackgroundTask extends AsyncTask<Activity, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Activity... params) {
            Activity activity = params[0];
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            Bitmap bitmap = null;
            try {
                bitmap = Picasso.with(activity.getApplicationContext())
                        .load(R.drawable.bg)
                        .resize(metrics.widthPixels, metrics.heightPixels)
                        .centerCrop().get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Static.background = new BitmapDrawable(activity.getResources(), bitmap);
            return true;
        }


    }
}
