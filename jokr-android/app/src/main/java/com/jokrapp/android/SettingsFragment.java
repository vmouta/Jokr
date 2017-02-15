package com.jokrapp.android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jokrapp.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jokrapp.android.data.RestClient;
import com.jokrapp.android.data.RestResponse;
import com.jokrapp.android.data.UserDataProvider;
import com.jokrapp.android.data.model.User;

public class SettingsFragment extends Fragment {

    private EditText textUsername;

    private CheckBox sde;
    private CheckBox de;
    private CheckBox en;
    private CheckBox fr;
    private CheckBox it;
    private CheckBox es;
    private CheckBox pt;

    private ImageButton imageButton;


    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        this.imageButton = (ImageButton) rootView.findViewById(R.id.saveButton);

        this.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                ((MainActivity)getActivity()).switchToMainFrame();
            }
        });

        sde = (CheckBox) rootView.findViewById(R.id.sde);
        de = (CheckBox) rootView.findViewById(R.id.de);
        en = (CheckBox) rootView.findViewById(R.id.en);
        fr = (CheckBox) rootView.findViewById(R.id.fr);
        es = (CheckBox) rootView.findViewById(R.id.es);
        pt = (CheckBox) rootView.findViewById(R.id.pt);
        it = (CheckBox) rootView.findViewById(R.id.it);

        user = UserDataProvider.getInstance().getUser();

        String lang = user.getLanguages();

        if (lang != null) {
            if (lang.indexOf(" sde ") != -1) {
                sde.setChecked(true);
            }
            if (lang.indexOf(" de ") != -1) {
                de.setChecked(true);
            }
            if (lang.indexOf(" en ") != -1) {
                en.setChecked(true);
            }
            if (lang.indexOf(" fr ") != -1) {
                fr.setChecked(true);
            }
            if (lang.indexOf(" es ") != -1) {
                es.setChecked(true);
            }
            if (lang.indexOf(" pt ") != -1) {
                pt.setChecked(true);
            }
            if (lang.indexOf(" it ") != -1) {
                it.setChecked(true);
            }
        }else{
            de.setChecked(true);
            sde.setChecked(true);
        }
        return rootView;
    }


    private void showToast(String text) {
        Toast toast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }


    private class UpdateTask extends AsyncTask<Object, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {

            JsonObject jsonObject = (new JsonParser()).parse(new Gson().toJson(user)).getAsJsonObject();
            RestResponse resp = new RestClient().post(Constants.USER_URL, jsonObject.toString());

            if (resp.getCode() == 200) {
                UserDataProvider.getInstance().saveUser(user);
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // do nothing
            if (!result) {
                showToast("Profile was not updated. Please try again later.");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        save();
    }

    private void save(){
        String langs = "";

        if (sde.isChecked()) {
            langs += " sde ";
        }
        if (de.isChecked()) {
            langs += " de ";
        }
        if (en.isChecked()) {
            langs += " en ";
        }
        if (fr.isChecked()) {
            langs += " fr ";
        }
        if (es.isChecked()) {
            langs += " es ";
        }
        if (pt.isChecked()) {
            langs += " pt ";
        }

        if (it.isChecked()) {
            langs += " it ";
        }

        this.user.setLanguages(langs);
        new UpdateTask().execute();

    }

}



