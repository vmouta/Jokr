package com.jokrapp.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jokrapp.R;
import com.jokrapp.android.data.HttpFileUpload;
import com.jokrapp.android.data.MyJokesDataProvider;
import com.jokrapp.android.data.RestClient;
import com.jokrapp.android.data.RestResponse;
import com.jokrapp.android.data.UserDataProvider;
import com.jokrapp.android.data.model.Joke;
import com.jokrapp.android.data.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EditFragment extends Fragment {

    private ImageButton uploadButton;
    private ImageButton playbackButton;
    private ImageButton shareButton;

    private EditText textTitle;
    private EditText textTags;
    private Spinner languageSpinner;
    private Spinner endingSpinner;
    private Spinner beginSpinner;

    private MediaPlayer introPlayer;
    private MediaPlayer finalePlayer;
    private MediaPlayer jokePlayer;

    private ProgressDialog progressDialog;

    private int currentJokeDuration;

    private Joke joke;

    public Joke getJoke() {
        return joke;
    }

    public void setJoke(Joke joke) {
        this.joke = joke;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_edit, container, false);

        this.uploadButton = (ImageButton) rootView.findViewById(R.id.uploadButton);
        this.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadJoke();
            }
        });
        this.playbackButton = (ImageButton) rootView.findViewById(R.id.playbackButton);
        this.playbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playbackJoke();
            }
        });
        this.shareButton = (ImageButton) rootView.findViewById(R.id.shareButton);
        this.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareJoke();
            }
        });

        languageSpinner = (Spinner) rootView.findViewById(R.id.spinnerLanguage);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        endingSpinner = (Spinner) rootView.findViewById(R.id.endingSpinner);
        adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.endings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endingSpinner.setAdapter(adapter);


        beginSpinner = (Spinner) rootView.findViewById(R.id.beginSpinner);
        adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.begins, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        beginSpinner.setAdapter(adapter);


        if (joke.isUploaded()) {
            endingSpinner.setEnabled(false);
            beginSpinner.setEnabled(false);
        }

        this.textTitle = (EditText) rootView.findViewById(R.id.textTitle);
        this.textTags = (EditText) rootView.findViewById(R.id.textTags);

        this.textTitle.setText(getJoke().getTitle());
        this.textTags.setText(getJoke().getTags());

        // set language
        for (int i = 0; i < Constants.languages.length; i++) {
            if (Constants.languages[i].equals(getJoke().getLanguage())) {
                this.languageSpinner.setSelection(i);
            }
        }

        this.endingSpinner.setSelection(joke.getFramingEnd());
        this.beginSpinner.setSelection(joke.getFramingBegin());

        setUploadButtonEnabled(!joke.isPublic());

        return rootView;
    }


    public void setUploadButtonEnabled(boolean enabled) {
        this.uploadButton.setEnabled(enabled);
        if (enabled)
            this.uploadButton.setImageResource(R.drawable.upload);
        else
            this.uploadButton.setImageResource(R.drawable.upload_d);
    }


    public void shareJoke() {
        if (!getJoke().isUploaded()) {
            updateJoke();
            UploadTask task = new UploadTask();
            task.execute(getJoke());
        }
        Intent sharingIntent = new FragmentUtil().shareJoke(getJoke());
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void playbackJoke() {

        getJoke().setFramingEnd(this.endingSpinner.getSelectedItemPosition());
        getJoke().setFramingBegin(this.beginSpinner.getSelectedItemPosition());
        JokePlayer.getInstance().playJoke(getJoke());
    }

    public void uploadJoke() {

        if(!joke.isUploaded()) {
            // update the joke object with the form values
            updateJoke();

            String username = UserDataProvider.getInstance().getUser().getUserName();

            if (username == null || username.length() == 0) {
                ProfileFragment f = new ProfileFragment();
                f.setEditFragment(this);
                f.setIsBeforeUpload(true);
                super.getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, f).addToBackStack("my_fragment").commit();

            } else {

                progressDialog = new ProgressDialog(super.getContext());
                progressDialog.setMessage("uploading...");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();

                getJoke().setIsPublic(true);

                UploadTask task = new UploadTask();
                task.execute(getJoke());

            }
        }else{
            getJoke().setIsPublic(true);
            setUploadButtonEnabled(false);
            showToast("Your joke is now public.");
        }
    }


    private void showToast(String text) {

        Toast toast = Toast.makeText(super.getContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }


    public class UploadTask extends AsyncTask<Joke, Integer, Boolean> {

        private Joke j = null;

        @Override
        protected Boolean doInBackground(Joke... params) {

            j = (Joke) params[0];
            User u = UserDataProvider.getInstance().getUser();

            try {
                // Set your file recordedFilePath here
                FileInputStream fstrm = null;
                try {
                    fstrm = new FileInputStream(Constants.JOKE_DIR + "/" + j.get_id() + ".mp3");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return true;
                }

                // Set your server page url (and the file title/description)
                HttpFileUpload hfu = new HttpFileUpload(Constants.JOKE_UPLOAD_URL, j.get_id(), u.get_id(), j.getTitle(), j.getLanguage(), j.getTags(), j.getFramingBegin(), j.getFramingEnd(), j.getCreationTimestamp(), j.isPublic());

                boolean result = hfu.Send_Now(fstrm);

                if (result) {
                    j.setIsUploaded(true);
                    MyJokesDataProvider.getInstance().updateJoke(j);
                } else {
                    return false;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                return false;

            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (result) {
                setUploadButtonEnabled(false);
                // delete local file
                new File(Constants.JOKE_DIR + "/" + j.get_id() + ".mp3").delete();

                joke = null;

                showToast("Uploaded");
                MyJokesDataProvider.getInstance().sync();

                switchToMyJokes();

            } else {
                showToast("Could not upload your joke. Please try again later.");
            }
        }
    }

    public void switchToMyJokes() {
        ((MainActivity) super.getActivity()).switchToMyJokesFrame();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getJoke() != null) {
            updateJoke();

            if (getJoke().isUploaded()) {
                UpdateTask task = new UpdateTask();
                task.execute(getJoke());
            }
        }

    }


    private void updateJoke() {
        getJoke().setTitle(this.textTitle.getText().toString());
        getJoke().setTags(this.textTags.getText().toString());
        getJoke().setFramingEnd(this.endingSpinner.getSelectedItemPosition());
        getJoke().setFramingBegin(this.beginSpinner.getSelectedItemPosition());
        getJoke().setLanguage(Constants.languages[this.languageSpinner.getSelectedItemPosition()]);
        MyJokesDataProvider.getInstance().updateJoke(getJoke());

    }

    private class UpdateTask extends AsyncTask<Joke, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Joke... params) {

            Joke j = (Joke) params[0];

            RestClient client = new RestClient();
            JsonObject jsonObject = (new JsonParser()).parse(new Gson().toJson(j)).getAsJsonObject();

            System.out.println("Updating joke: " + jsonObject);
            RestResponse rsp = client.post(Constants.JOKE_UPDATE_URL, jsonObject.toString());
            if (rsp.getCode() == 200)
                return true;
            else
                return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // do nothing
        }
    }
}
