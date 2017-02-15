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

import com.jokrapp.R;
import com.jokrapp.android.data.FavoriteJokesDataProvider;
import com.jokrapp.android.data.RestClient;
import com.jokrapp.android.data.RestResponse;
import com.jokrapp.android.data.UserDataProvider;
import com.jokrapp.android.data.model.Joke;

import java.util.ArrayList;


public class FavoritesFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView emptyView;
    private Joke currentSelectedJoke;

    private ProgressDialog progressDialog;

    public void setCurrentSelectedJoke(Joke currentSelectedJoke) {
        this.currentSelectedJoke = currentSelectedJoke;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);

        emptyView = (TextView) rootView.findViewById(R.id.empty_text);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(super.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        ArrayList<Joke> jokes = FavoriteJokesDataProvider.getInstance().getJokes();

        // specify an adapter (see also next example)
        mAdapter = new FavoritesListAdapter(jokes, this, this);

        mRecyclerView.setAdapter(mAdapter);

        if (jokes.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.action_share:
                if (currentSelectedJoke.isUploaded()) {
                    Intent sharingIntent = new FragmentUtil().shareJoke(currentSelectedJoke);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                } else {
                    showToast("Error occured.");
                }
                break;
            case R.id.action_delete:
                FavoriteJokesDataProvider.getInstance().deleteJoke(currentSelectedJoke);

                new DeleteTask().execute(currentSelectedJoke);

                break;

        }

        mAdapter.notifyDataSetChanged();

        if (FavoriteJokesDataProvider.getInstance().getJokes().isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        return true;
    }


    private void showToast(String text) {

        Toast toast = Toast.makeText(super.getContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private class DeleteTask extends AsyncTask<Joke, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Joke... params) {

            Joke j = (Joke) params[0];
            RestResponse rsp = new RestClient().post(Constants.JOKE_DEL_Favorite_URL + "?jokeId=" + j.get_id() + "&userId=" + UserDataProvider.getInstance().getUser().get_id(), "");
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

