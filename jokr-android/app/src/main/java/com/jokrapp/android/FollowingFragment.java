package com.jokrapp.android;

import android.app.ProgressDialog;
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

import com.jokrapp.R;
import com.jokrapp.android.data.FollowingDataProvider;
import com.jokrapp.android.data.RestClient;
import com.jokrapp.android.data.RestResponse;
import com.jokrapp.android.data.UserDataProvider;
import com.jokrapp.android.data.model.User;

import java.util.ArrayList;


public class FollowingFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView emptyView;


    private ProgressDialog progressDialog;

    private User currentSelectedUser;

    public User getCurrentSelectedUser() {
        return currentSelectedUser;
    }

    public void setCurrentSelectedUser(User currentSelectedUser) {
        this.currentSelectedUser = currentSelectedUser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_following, container, false);

        emptyView = (TextView) rootView.findViewById(R.id.empty_text);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(super.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        ArrayList<User> users = FollowingDataProvider.getInstance().getUsers();

        // specify an adapter (see also next example)
        mAdapter = new FollowingListAdapter(users, this, this);

        mRecyclerView.setAdapter(mAdapter);

        if (users.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.action_unfollow:

                new UnfollowTask().execute(currentSelectedUser);

                break;

        }

        mAdapter.notifyDataSetChanged();

        if (FollowingDataProvider.getInstance().getUsers().isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        return true;
    }


    private class UnfollowTask extends AsyncTask<User, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(User... params) {

            User j = params[0];
            RestResponse rsp = new RestClient().post(Constants.USER_UNFOLLOW + "?followUserId=" + j.get_id() + "&userId=" + UserDataProvider.getInstance().getUser().get_id(), "");
            FollowingDataProvider.getInstance().deleteUser(j);

            if (rsp.getCode() == 200) {
                return true;
            } else
                return false;

        }

        @Override
        protected void onPostExecute(Boolean result) {//do nothing
        }
    }


}

