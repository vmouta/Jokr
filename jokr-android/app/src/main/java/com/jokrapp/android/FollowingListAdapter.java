package com.jokrapp.android;

import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jokrapp.R;
import com.jokrapp.android.data.RestClient;
import com.jokrapp.android.data.RestResponse;
import com.jokrapp.android.data.model.Joke;
import com.jokrapp.android.data.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FollowingListAdapter extends RecyclerView.Adapter<FollowingListAdapter.ViewHolder> {
    private ArrayList<User> itemsData;

    private PopupMenu.OnMenuItemClickListener menuItemClickListener;

    private FollowingFragment parent;

    public FollowingListAdapter(ArrayList<User> itemsData, FollowingFragment parent, PopupMenu.OnMenuItemClickListener menuItemClickListener) {
        this.itemsData = itemsData;
        this.menuItemClickListener = menuItemClickListener;
        this.parent = parent;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FollowingListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items_following, null);

        // create ViewHolder;
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData

        User user = itemsData.get(position);
        if (user.getUserName() != null && user.getUserName().length() > 0) {
            viewHolder.itemTitle.setText(user.getUserName());
        } else {
            viewHolder.itemTitle.setText("noname");
        }

        viewHolder.menu.setTag(user);
        viewHolder.userIcon.setTag(user);
        viewHolder.userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                User u = (User) v.getTag();

                JokelistFragment fragment = new JokelistFragment();

                fragment.updateJokes(u);

                parent.getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("m").commit();

            }
        });

        String url = Constants.USER_PIC_FETCH + "?userId=" + user.get_id();
        Picasso.with(parent.getContext()).load(url)
                .resize(110, 110).centerCrop().error(R.drawable.logo_gray).transform(new CropCircleTransformation()).into(viewHolder.userIcon);


        viewHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User u = (User) v.getTag();
                parent.setCurrentSelectedUser(u);
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.setOnMenuItemClickListener(menuItemClickListener);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.following_jokeitem, popup.getMenu());
                popup.show();

            }
        });
    }

    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView itemTitle;
        public ImageView userIcon;
        public ImageView menu;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            itemTitle = (TextView) itemLayoutView.findViewById(R.id.item_userName);
            userIcon = (ImageButton) itemLayoutView.findViewById(R.id.button_user);
            menu = (ImageView) itemLayoutView.findViewById(R.id.button_list_menu);
        }
    }


    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return itemsData.size();
    }
}

