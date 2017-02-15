package com.jokrapp.android;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jokrapp.R;
import com.jokrapp.android.data.MyJokesDataProvider;
import com.jokrapp.android.data.model.Joke;

import java.util.UUID;

public class MainFragment extends Fragment {

    private ImageButton recordButton;

    private ImageButton playJokeButton;

    private TextView bubbleText;

    private ExtAudioRecorder recorder;

    private MediaPlayer introPlayer;
    private MediaPlayer finalePlayer;
    private MediaPlayer jokePlayer;
    boolean isRecording = false;

    private String currentUuid;
    private ProgressDialog progressDialog;

    private Joke lastRecordedJoke = null;

    private int currentJokeDuration;

    public View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_main_screen, container, false);

        if (Static.background == null) {
            Static.updateBackground(this.getActivity());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (Static.background != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    rootView.setBackground(Static.background);
                }
            }
           /* new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Static.background != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            rootView.setBackground(Static.background);
                        }
                    }
                }
            }, 500);*/
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                rootView.setBackground(Static.background);
            }
        }

        this.recordButton = (ImageButton) rootView.findViewById(R.id.recordButton);
        this.recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });

        this.playJokeButton = (ImageButton) rootView.findViewById(R.id.playJoke);
        this.playJokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isRecording) {
                    showJokePlayer();
                }
            }
        });


        this.bubbleText = (TextView) rootView.findViewById(R.id.bubbleText);

        return rootView;
    }


    private void showJokePlayer() {

        getFragmentManager().beginTransaction().replace(R.id.content_frame, new JokePlayerFragment()).addToBackStack("my_fragment").commit();

    }

    private void stopRecording() {

        playLaugh();

        this.recordButton.setImageResource(R.drawable.record);

        this.bubbleText.setText("haha!"); // random funny text ???

        recorder.stop();
        recorder.reset();    // set state to idle
        recorder.release();

        isRecording = false;

//        String title = new SimpleDateFormat("d-M-yyyy - HH:mm").format(new Date());

        Joke j = new Joke(currentUuid, "");
        j.setCreationTimestamp(System.currentTimeMillis());
        j.setIsPublic(false);
        j.setFramingBegin(1);
        j.setFramingEnd(1);

        MyJokesDataProvider.getInstance().addJoke(j);

        lastRecordedJoke = j;

        uploadJoke();
    }



    public void uploadJoke() {
        EditFragment f = new EditFragment();
        f.setJoke(lastRecordedJoke);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, f).addToBackStack("my_fragment").commit();

    }

    public void playLaugh() {

        if (finalePlayer != null) {
            finalePlayer.reset();
            finalePlayer.release();
        }
        finalePlayer = MediaPlayer.create(super.getContext(), R.raw.laugh1);
        finalePlayer.start();

    }

    private void startRecording() {
        this.recordButton.setImageResource(R.drawable.stop);
        if (recorder != null) {
            recorder.reset();
            recorder.release();
        }
        // recording in PCM 16 bit format, best quality...
        recorder = ExtAudioRecorder.getInstanse(false);

        currentUuid = UUID.randomUUID().toString();
        recorder.setOutputFile(Constants.JOKE_DIR + "/" + currentUuid + ".mp3");

        recorder.prepare();

        isRecording = true;
        recorder.start();
        this.bubbleText.setText("recording...");

    }



}
