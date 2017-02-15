package com.jokrapp.android;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jokrapp.R;
import com.jokrapp.android.data.UserDataProvider;
import com.jokrapp.android.drawer.DrawerItemCustomAdapter;
import com.jokrapp.android.drawer.ObjectDrawerItem;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private DrawerItemClickListener clickListener;

    ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private ProgressDialog progressDialog;

    private ImageView profilePic;
    private LinearLayout drawerll;

    private CircularProgressBar circularProgressBar;

    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mNavigationDrawerItemTitles = getResources().getStringArray(R.array.navigation_drawer_items_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        drawerll = (LinearLayout) findViewById(R.id.drawerLayout);
        userName = (TextView) findViewById(R.id.userNameInDrawer);

        circularProgressBar = (CircularProgressBar) findViewById(R.id.circleProgressBar);
        circularProgressBar.setVisibility(View.INVISIBLE);


        if (UserDataProvider.getInstance().getUser() != null) {
            userName.setText(UserDataProvider.getInstance().getUser().getUserName());
        }
        //  View header = getLayoutInflater().inflate(R.layout.header, null);
        // mDrawerList.addHeaderView(header);

        ObjectDrawerItem[] drawerItem = new ObjectDrawerItem[6];

        drawerItem[0] = new ObjectDrawerItem(R.drawable.home, mNavigationDrawerItemTitles[0]);
        drawerItem[1] = new ObjectDrawerItem(R.drawable.myjokes, mNavigationDrawerItemTitles[1]);
        drawerItem[2] = new ObjectDrawerItem(R.drawable.favorites, mNavigationDrawerItemTitles[2]);
        drawerItem[3] = new ObjectDrawerItem(R.drawable.following, mNavigationDrawerItemTitles[3]);
        drawerItem[4] = new ObjectDrawerItem(R.drawable.my, mNavigationDrawerItemTitles[4]);
        drawerItem[5] = new ObjectDrawerItem(R.drawable.settings, mNavigationDrawerItemTitles[5]);

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.drawer_item, drawerItem);
        mDrawerList.setAdapter(adapter);

        clickListener = new DrawerItemClickListener();

        mDrawerList.setOnItemClickListener(clickListener);

        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {


        };

        getSupportActionBar().setDisplayUseLogoEnabled(true);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.jokr);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        profilePic = (ImageView) findViewById(R.id.profilePicture);
        updateProfilePic();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setElevation(0);
        //  getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff3f3f")));

        if (UserDataProvider.getInstance().getUser().getLanguages() == null) {
            clickListener.selectItem(5);
        } else {
            clickListener.selectItem(0);
        }
        setTitle("");
        JokePlayer.getInstance().initialize(this);


    }

    public void updateProfilePic() {
        File f = new File(Constants.APP_DIR + "/profilePic.jpg");
        Picasso.with(this).load(f).skipMemoryCache().error(R.drawable.logo)
                .resize(80, 80).centerCrop().transform(new CropCircleTransformation()).into(profilePic);
    }

    public void updateUserdata() {
        if (UserDataProvider.getInstance().getUser() != null) {
            this.userName.setText(UserDataProvider.getInstance().getUser().getUserName());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    public void switchToMainFrame() {
        clickListener.selectItem(0);
    }

    public void switchToProfileFrame() {
        clickListener.selectItem(3);
    }


    public void switchToMyJokesFrame() {
        clickListener.selectItem(1);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }


        private void selectItem(int position) {

            Fragment fragment = null;

            switch (position) {
                case 0:
                    fragment = new MainFragment();
                    break;
                case 1:
                    fragment = new MyRecordingsFragment();
                    break;
                case 2:
                    fragment = new FavoritesFragment();
                    break;
                case 3:
                    fragment = new FollowingFragment();
                    break;
                case 4:
                    fragment = new ProfileFragment();
                    break;
                case 5:
                    fragment = new SettingsFragment();
                    break;
                default:
                    break;
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("m").commit();

                setTitle("");
             /*   if (position != 0) {
                    setTitle(mNavigationDrawerItemTitles[position]);
                } else {
                    setTitle("");
                } */
                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                mDrawerLayout.closeDrawer(drawerll);

            } else {
                Log.e("MainActivity", "Error in creating fragment");
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            setTitle("");
            getSupportFragmentManager().popBackStack();
        } else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }


    public void showProgressDialog() {

        circularProgressBar.setVisibility(View.VISIBLE);


        circularProgressBar.setProgressWithAnimation(100, 2500);


        //       progressDialog = new ProgressDialog(this, R.style.ProgressBar);
        //     progressDialog.setIndeterminate(true);
        //    progressDialog.setCancelable(false);
        //   progressDialog.getWindow().setGravity(Gravity.CENTER);
        //  progressDialog.show();
    }

    public void hideProgressDialog() {
        //    progressDialog.dismiss();
        circularProgressBar.setVisibility(View.INVISIBLE);

        circularProgressBar.setProgressWithAnimation(0, 2500);

    }


}
