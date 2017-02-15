package com.jokrapp.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jokrapp.R;
import com.jokrapp.android.data.FavoriteJokesDataProvider;
import com.jokrapp.android.data.FollowingDataProvider;
import com.jokrapp.android.data.RestClient;
import com.jokrapp.android.data.RestResponse;
import com.jokrapp.android.data.UserDataProvider;
import com.jokrapp.android.data.model.Joke;
import com.jokrapp.android.data.model.User;
import com.squareup.picasso.Picasso;

public class JokePlayerFragment extends Fragment {

    private ImageButton playJokeButton;
    private ImageButton likeButton;
    private ImageButton dislikeButton;
    private ImageButton favoriteButton;
    private ImageButton blockButton;
    private ImageButton shareButton;
    private ImageButton followButton;

    private ImageView userPic;
    private ImageView flag;

    private TextView likesText;
    private TextView userNameText;
    private TextView tagsText;

    private int jokePlayIndex = -1;
    private JsonArray allJokes;


    public static String msgNoJokes = "There are no more jokes available. Please feel free to contribute and try again later.";

    public View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_joke_player, container, false);

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
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                rootView.setBackground(Static.background);
            }
        }

        this.userPic = (ImageView) rootView.findViewById(R.id.userPic);
        this.flag = (ImageView) rootView.findViewById(R.id.flag);

        this.playJokeButton = (ImageButton) rootView.findViewById(R.id.playJoke);
        this.playJokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextJoke();

            }
        });

        this.likeButton = (ImageButton) rootView.findViewById(R.id.likeJoke);

        this.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeJoke();
            }
        });

        this.dislikeButton = (ImageButton) rootView.findViewById(R.id.dislikeJoke);

        this.dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dislikeJoke();
            }
        });

        this.favoriteButton = (ImageButton) rootView.findViewById(R.id.favoriteJoke);

        this.favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addJokeToFavorites();
            }
        });

        this.followButton = (ImageButton) rootView.findViewById(R.id.follow);

        this.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                follow();
            }
        });


        this.blockButton = (ImageButton) rootView.findViewById(R.id.blockUser);

        this.blockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockUser();
            }
        });

        this.shareButton = (ImageButton) rootView.findViewById(R.id.shareJoke);
        this.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Joke joke = new Gson().fromJson(allJokes.get(jokePlayIndex), Joke.class);
                Intent sharingIntent = new FragmentUtil().shareJoke(joke);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
        this.likesText = (TextView) rootView.findViewById(R.id.numberOflikes);
        this.tagsText = (TextView) rootView.findViewById(R.id.tags);
        this.userNameText = (TextView) rootView.findViewById(R.id.userName);

        this.likesText.setText("");
        this.tagsText.setText("");
        this.userNameText.setText("");

        this.likeButton.setVisibility(View.INVISIBLE);
        this.dislikeButton.setVisibility(View.INVISIBLE);
        this.favoriteButton.setVisibility(View.INVISIBLE);
        this.blockButton.setVisibility(View.INVISIBLE);
        this.shareButton.setVisibility(View.INVISIBLE);
        this.followButton.setVisibility(View.INVISIBLE);
        this.flag.setVisibility(View.INVISIBLE);

        setLikeEnabled(false);
        setDisLikeEnabled(false);
        setFavoriteEnabled(false);
        setBlocknabled(false);

        playNextJoke();
        return rootView;

    }

    private void blockUser() {
        // current joke
        Joke joke = new Gson().fromJson(allJokes.get(jokePlayIndex), Joke.class);

        new BlockJokeTask().execute(joke);

    }

    private void follow() {
        // current joke
        Joke joke = new Gson().fromJson(allJokes.get(jokePlayIndex), Joke.class);

        new FollowTask().execute(joke);


    }

    private void playNextJoke() {

        if(!JokePlayer.getInstance().isPrepairing()) {

            // always fetching a couple of jokes for better performance... max 10 jokes are sent by server
            if (jokePlayIndex == -1 || allJokes != null && jokePlayIndex == allJokes.size() - 1) {

                ((MainActivity) super.getActivity()).showProgressDialog();

                FetchJokeTask task = new FetchJokeTask();
                task.execute();

                jokePlayIndex = 0;

                return;
            } else {
                jokePlayIndex++;
            }

            playNextJokeInArray();
        }
    }

    private void setPoints(int points) {
        if (points < 0)
            this.likesText.setTextColor(getResources().getColor(R.color.negative));
        if (points >= 0)
            this.likesText.setTextColor(getResources().getColor(R.color.positive));

        this.likesText.setText(points + " points");
    }

    private void playNextJokeInArray() {

        if (allJokes == null) {
            showToast(msgNoJokes);
            return;

        }
        // get next joke
        Joke joke = new Gson().fromJson(allJokes.get(jokePlayIndex), Joke.class);

        this.userNameText.setText(joke.getUserName());
        this.setPoints(joke.getLikes());

        this.tagsText.setText(joke.getTags());

        setLikeEnabled(true);
        setDisLikeEnabled(true);
        setFavoriteEnabled(true);
        setBlocknabled(true);

        String url = Constants.USER_PIC_FETCH + "?userId=" + joke.getUserId();
        Picasso.with(getContext()).load(url)
                .resize(160, 160).centerCrop().error(R.drawable.logo_gray).transform(new CropCircleTransformation()).into(userPic);

        this.likeButton.setVisibility(View.VISIBLE);
        this.dislikeButton.setVisibility(View.VISIBLE);
        this.favoriteButton.setVisibility(View.VISIBLE);
        this.blockButton.setVisibility(View.VISIBLE);
        this.shareButton.setVisibility(View.VISIBLE);
        this.flag.setVisibility(View.VISIBLE);
        this.followButton.setVisibility(View.VISIBLE);

        this.flag.setImageResource(getResources().getIdentifier(joke.getLanguage(), "drawable", getContext().getApplicationInfo().packageName));

        if (FollowingDataProvider.getInstance().contains(joke.getUserId())) {
            setFollowEnabled(false);
            setBlocknabled(false);
        } else {
            setFollowEnabled(true);
            setBlocknabled(true);
        }

        JokePlayer.getInstance().playJoke(joke);

    }


    private void setLikeEnabled(boolean enabled) {
        this.likeButton.setEnabled(enabled);
        if (enabled)
            this.likeButton.setImageResource(R.drawable.like);
        else
            this.likeButton.setImageResource(R.drawable.like_d);
    }

    private void setBlocknabled(boolean enabled) {
        this.blockButton.setEnabled(enabled);
        if (enabled)
            this.blockButton.setImageResource(R.drawable.block);
        else
            this.blockButton.setImageResource(R.drawable.block_d);
    }

    private void setDisLikeEnabled(boolean enabled) {
        this.dislikeButton.setEnabled(enabled);
        if (enabled)
            this.dislikeButton.setImageResource(R.drawable.dislike);
        else
            this.dislikeButton.setImageResource(R.drawable.dislike_d);
    }


    private void setFavoriteEnabled(boolean enabled) {
        this.favoriteButton.setEnabled(enabled);
        if (enabled)
            this.favoriteButton.setImageResource(R.drawable.favorite);
        else
            this.favoriteButton.setImageResource(R.drawable.favorite_d);
    }


    private void setFollowEnabled(boolean enabled) {
        this.followButton.setEnabled(enabled);
        if (enabled)
            this.followButton.setImageResource(R.drawable.add);
        else
            this.followButton.setImageResource(R.drawable.add_d);
    }

    private void showToast(String text) {

        Toast toast = Toast.makeText(super.getContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void likeJoke() {

        // current joke
        Joke joke = new Gson().fromJson(allJokes.get(jokePlayIndex), Joke.class);

        LikeJokeTask task = new LikeJokeTask();
        task.execute(joke);

        setPoints(joke.getLikes() + 1);

        setLikeEnabled(false);
        setDisLikeEnabled(false);

    }

    public void dislikeJoke() {

        // current joke
        Joke joke = new Gson().fromJson(allJokes.get(jokePlayIndex), Joke.class);

        DislikeJokeTask task = new DislikeJokeTask();
        task.execute(joke);

        setPoints(joke.getLikes() - 1);
        setLikeEnabled(false);
        setDisLikeEnabled(false);
    }

    public void addJokeToFavorites() {
        Joke joke = new Gson().fromJson(allJokes.get(jokePlayIndex), Joke.class);

        FavoriteJokeTask task = new FavoriteJokeTask();
        task.execute(joke);

    }

    private class LikeJokeTask extends AsyncTask<Joke, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Joke... params) {
            Joke joke = params[0];

            String userId = UserDataProvider.getInstance().getUser().get_id();

            new RestClient().post(Constants.JOKE_LIKE_URL + "?userId=" + userId + "&jokeId=" + joke.get_id(), "");

            return true;
        }
    }

    private class DislikeJokeTask extends AsyncTask<Joke, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Joke... params) {
            Joke joke = params[0];

            String userId = UserDataProvider.getInstance().getUser().get_id();

            new RestClient().post(Constants.JOKE_DISLIKE_URL + "?userId=" + userId + "&jokeId=" + joke.get_id(), "");

            return true;
        }
    }

    private class FavoriteJokeTask extends AsyncTask<Joke, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Joke... params) {
            Joke joke = params[0];

            FavoriteJokesDataProvider.getInstance().addJoke(joke);

            String userId = UserDataProvider.getInstance().getUser().get_id();

            RestResponse r = new RestClient().post(Constants.JOKE_ADD_Favorite_URL + "?userId=" + userId + "&jokeId=" + joke.get_id(), "");

            if (r.getCode() == 200) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                setFavoriteEnabled(false);
                showToast("added to favorites");
            } else {
                showToast("something went wrong...");
            }
        }
    }

    private class FetchJokeTask extends AsyncTask<Object, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {

            System.out.println("fetching jokes...");
            RestClient client = new RestClient();
            RestResponse resp = client.get(Constants.JOKE_FETCH_URL + "?userId=" + UserDataProvider.getInstance().getUser().get_id() + "&demo=" + Constants.DEMO_FETCH);

            if (resp.getCode() != 200) {
                return false;
            }

            JsonElement jsonElement = new JsonParser().parse(resp.getBody());
            allJokes = jsonElement.getAsJsonArray();
            System.out.println(allJokes);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            hideProgressDialog();
            if (!result) {
                showToast(msgNoJokes);
            } else {
                playNextJokeInArray();
            }
        }
    }

    public void hideProgressDialog() {
        ((MainActivity) super.getActivity()).hideProgressDialog();

    }

    private class BlockJokeTask extends AsyncTask<Joke, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Joke... params) {
            Joke joke = params[0];

            String userId = UserDataProvider.getInstance().getUser().get_id();

            RestResponse r = new RestClient().post(Constants.USER_BLOCK_URL + "?userId=" + userId + "&userToBlockId=" + joke.getUserId(), "");

            if (r.getCode() == 200) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                setBlocknabled(false);
                showToast("blocked user");
            } else {
            }
        }
    }

    private class FollowTask extends AsyncTask<Joke, Integer, Boolean> {

        private Joke joke;

        @Override
        protected Boolean doInBackground(Joke... params) {
            joke = params[0];

            String userId = UserDataProvider.getInstance().getUser().get_id();

            RestResponse r = new RestClient().post(Constants.USER_FOLLOW + "?userId=" + userId + "&followUserId=" + joke.getUserId(), "");

            if (r.getCode() == 200) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (joke.getUserName() != null) {
                    showToast("You are now following " + joke.getUserName());
                } else {
                    showToast("You are now following the author of this joke..");
                }
                setFollowEnabled(false);

                User u = new User();
                u.setUserName(joke.getUserName());
                u.set_id(joke.getUserId());

                FollowingDataProvider.getInstance().addUser(u);

            } else {
            }
        }
    }
}
