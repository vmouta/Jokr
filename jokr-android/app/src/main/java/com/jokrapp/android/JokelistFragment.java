package com.jokrapp.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jokrapp.R;
import com.jokrapp.android.data.FavoriteJokesDataProvider;
import com.jokrapp.android.data.RestClient;
import com.jokrapp.android.data.RestResponse;
import com.jokrapp.android.data.UserDataProvider;
import com.jokrapp.android.data.model.Joke;
import com.jokrapp.android.data.model.User;

import java.util.ArrayList;


public class JokelistFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView emptyView;

    private ArrayList<Joke> jokes = null;

    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_jokelist, container, false);

        emptyView = (TextView) rootView.findViewById(R.id.empty_text);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(super.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (jokes != null) {
            // specify an adapter (see also next example)
            mAdapter = new JokelistListAdapter(jokes, this);

            mRecyclerView.setAdapter(mAdapter);
        }

        if (jokes.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        return rootView;
    }

    public void updateJokes(User u) {
        jokes = new ArrayList<Joke>();
        RestClient client = new RestClient();
        RestResponse resp = client.get(Constants.JOKE_OF_USER + "?userId=" + u.get_id());

        if (resp.getCode() != 200) {
            return;
        }

        JsonElement jsonElement = new JsonParser().parse(resp.getBody());
        JsonArray allJokes = jsonElement.getAsJsonArray();

        for (int i = 0; i < allJokes.size(); i++) {
            Joke joke = new Gson().fromJson(allJokes.get(i), Joke.class);
            jokes.add(0, joke);
        }
    }

}

