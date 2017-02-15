package com.jokrapp.android;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;

import com.jokrapp.R;
import com.jokrapp.android.data.model.Joke;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by pat on 11/15/2015.
 */
public class JokePlayer {

    private MediaPlayer jokePlayer;
    private MediaPlayer introPlayer;
    private MediaPlayer finalePlayer;

    private MainActivity mainActivity;

    private static JokePlayer ourInstance = new JokePlayer();

    public static JokePlayer getInstance() {
        return ourInstance;
    }

    private static long lastJokeStartTimestamp; // TS of last joke started

    private static long currentJokeDuration;

    private boolean isPreparing = false;

    private JokePlayer() {
    }

    public void initialize(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void stopPlaying() {

        if (jokePlayer != null) {
            if (jokePlayer.isPlaying() && !isPreparing)
                    jokePlayer.stop();
            jokePlayer.release();
        }
    }

    public boolean isPrepairing(){
        return isPreparing;
    }
    public void playJoke(Joke joke, boolean cache) {

        playJoke(joke);

        if (cache) {
            File f = new File(Constants.JOKE_DIR + "/" + joke.get_id() + ".mp3");
            if (!f.exists()) {
                new CacheFileTask().execute(joke);
            }
        }
    }

    public void playJoke(Joke joke) {
        if(!isPreparing) {
            stopPlaying();

            File f = new File(Constants.JOKE_DIR + "/" + joke.get_id() + ".mp3");

            lastJokeStartTimestamp = System.currentTimeMillis();

            if (f.exists() && joke.isUploaded() == false) {
                final Joke currentJoke = joke;
                final long thisJokeStartTimestamp = lastJokeStartTimestamp;

                // if file is locally stored and not uploaded yet
                try {
                    jokePlayer = new MediaPlayer();
                    isPreparing = true;
                    jokePlayer.setDataSource(f.getAbsolutePath());
                    jokePlayer.prepare();
                    isPreparing = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    isPreparing = false;
                }

                if (joke.getFramingBegin() > 0) {
                    startPlayingIntro();


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (thisJokeStartTimestamp == lastJokeStartTimestamp) {
                                if (jokePlayer != null) {
                                    jokePlayer.reset();
                                    jokePlayer.release();
                                }
                                jokePlayer = new MediaPlayer();
                                File f = new File(Constants.JOKE_DIR + "/" + currentJoke.get_id() + ".mp3");

                                try {
                                    isPreparing = true;
                                    jokePlayer.setDataSource(f.getAbsolutePath());
                                    jokePlayer.prepare();
                                    isPreparing = false;
                                    currentJokeDuration = jokePlayer.getDuration();

                                    if (currentJoke.getFramingEnd() > 0) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (thisJokeStartTimestamp == lastJokeStartTimestamp) {
                                                    playLaugh();
                                                }
                                            }
                                        }, currentJokeDuration);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    isPreparing = false;
                                }

                                jokePlayer.start();
                            }
                        }
                    }, 2000);

                } else {


                    if (jokePlayer != null) {
                        jokePlayer.reset();
                        jokePlayer.release();
                    }
                    jokePlayer = new MediaPlayer();

                    try {
                        isPreparing = true;
                        jokePlayer.setDataSource(f.getAbsolutePath());
                        jokePlayer.prepare();
                        isPreparing = false;
                        currentJokeDuration = jokePlayer.getDuration();

                        if (currentJoke.getFramingEnd() > 0) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (thisJokeStartTimestamp == lastJokeStartTimestamp) {
                                        playLaugh();
                                    }
                                }
                            }, currentJokeDuration);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        isPreparing = false;
                    }

                    jokePlayer.start();

                }
            } else if (f.exists() && joke.isUploaded() == true) {

                // just play as is...
                try {
                    jokePlayer = new MediaPlayer();
                    isPreparing = true;
                    jokePlayer.setDataSource(f.getAbsolutePath());
                    jokePlayer.prepare();
                    isPreparing = false;
                    jokePlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    isPreparing = false;
                }


            } else {

                // online but not cached

                mainActivity.showProgressDialog();

                new PrepaireJokeTask().execute(joke);
/*
            if (joke.getFramingBegin() > 0) {
                startPlayingIntro();
            }
  */
            }
        }
    }


    public void startPlayingIntro() {

        introPlayer = MediaPlayer.create(mainActivity.getApplicationContext(), R.raw.clapping1);
        introPlayer.start();

    }


    public void playLaugh() {

        finalePlayer = MediaPlayer.create(mainActivity.getApplicationContext(), R.raw.laugh1);
        finalePlayer.start();


    }

    private class PrepaireJokeTask extends AsyncTask<Joke, Integer, Boolean> {

        private Joke joke;

        @Override
        protected Boolean doInBackground(Joke... params) {
            joke = params[0];

            String url = Constants.JOKE_BINARY_FETCH + "?jokeId=" + joke.get_id();
            System.out.println("Prepairing joke from url: " + url);
            jokePlayer = new MediaPlayer();
            jokePlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                isPreparing = true;
                jokePlayer.setDataSource(url);
                jokePlayer.prepare();
                isPreparing = false;
                System.out.println("Duration of track: " + jokePlayer.getDuration());

            } catch (IOException e) {
                e.printStackTrace();
                isPreparing = false;
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mainActivity.hideProgressDialog();

       /*     if (joke.getFramingBegin() > 0) {
                jokePlayer.seekTo(3000); // start playing from 3rd second
            }*/

            jokePlayer.start();
/*
            if (joke.getFramingBegin() > 0) {
                introPlayer.stop();
                introPlayer.release();
            }
*/
        }
    }


    private class CacheFileTask extends AsyncTask<Joke, Integer, Boolean> {

        private Joke joke;

        @Override
        protected Boolean doInBackground(Joke... params) {
            joke = params[0];

            try {
                // so that there is no immediate act
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String url = Constants.JOKE_BINARY_FETCH + "?jokeId=" + joke.get_id();

            String fileName = new File(Constants.JOKE_DIR + "/" + joke.get_id() + ".mp3").getAbsolutePath();

            try {

                java.io.BufferedInputStream in = new java.io.BufferedInputStream(new java.net.URL(url).openStream());
                java.io.FileOutputStream fos = new java.io.FileOutputStream(fileName);
                java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
                byte[] data = new byte[1024];
                int x = 0;
                while ((x = in.read(data, 0, 1024)) >= 0) {
                    bout.write(data, 0, x);
                }
                fos.flush();
                bout.flush();
                fos.close();
                bout.close();
                in.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Cached joke: " + joke);
            return true;
        }

    }


}
