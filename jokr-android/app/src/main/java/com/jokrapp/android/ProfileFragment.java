package com.jokrapp.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jokrapp.R;
import com.jokrapp.android.data.PictureHttpFileUpload;
import com.jokrapp.android.data.RestClient;
import com.jokrapp.android.data.RestResponse;
import com.jokrapp.android.data.UserDataProvider;
import com.jokrapp.android.data.model.User;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileFragment extends Fragment {

    private EditText textUsername;
    private ImageButton imageButton;
    private User user;
    private CropImageView imageView;
    private View rootView;
    private MainActivity mainActivity;
    private ImageView userPic;
    private ImageButton uploadButton;

    private EditFragment editFragment;

    private boolean isBeforeUpload = false;

    public boolean isBeforeUpload() {
        return isBeforeUpload;
    }

    public void setIsBeforeUpload(boolean isBeforeUpload) {
        this.isBeforeUpload = isBeforeUpload;
    }

    public EditFragment getEditFragment() {
        return editFragment;
    }

    public void setEditFragment(EditFragment editFragment) {
        this.editFragment = editFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        userPic = (ImageView)rootView.findViewById(R.id.userPic);

        mainActivity = ((MainActivity) getActivity());

        this.textUsername
                = (EditText) rootView.findViewById(R.id.textJokeTitle);

        this.user = UserDataProvider.getInstance().getUser();
        this.textUsername.setText(user.getUserName());


        this.imageButton = (ImageButton) rootView.findViewById(R.id.chooseImageButton);
        this.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicker();
            }
        });
        imageView = (CropImageView) rootView.findViewById(R.id.CropImageView);

        File f = new File(Constants.APP_DIR + "/profilePic.jpg");
        Picasso.with(getContext()).load(f).skipMemoryCache()
                .resize(160, 160).centerCrop().transform(new CropCircleTransformation()).into(userPic);
        this.uploadButton = (ImageButton) rootView.findViewById(R.id.uploadButton);

        this.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textUsername.getText().length() > 0) {
                    user.setUserName(textUsername.getText().toString());
                    new UpdateTask().execute();
                    editFragment.uploadJoke();
                }else{
                    showToast("Please define a Jokr name before uploading.");
                }
            }
        });

        if(!isBeforeUpload) {
            this.uploadButton.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }


    private void showToast(String text) {
        Toast toast = Toast.makeText(mainActivity.getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showImagePicker() {
        Intent intent = new Intent();
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println(requestCode + " " + resultCode);

        //    if (requestCode == 1 && resultCode == 0 && data != null && data.getData() != null) {
        if (requestCode == 1 && data != null && data.getData() != null) {
            Uri uri = data.getData();

            // hide original picture.
            userPic.setVisibility(View.INVISIBLE);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(rootView.getContext().getContentResolver()
                        , uri);

                //   Bitmap d = new BitmapDrawable(rootView.getResources() , uri.getAbsolutePath()).getBitmap();
                int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                imageView.setImageBitmap(scaled);

                imageView.setAspectRatio(5, 5);
                imageView.setFixedAspectRatio(true);
                imageView.setGuidelines(1);
                imageView.setCropShape(CropImageView.CropShape.OVAL);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setImageBitmap(scaled);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        this.user.setUserName(this.textUsername.getText().toString());
        new UpdateTask().execute();

        Bitmap bitmap = imageView.getCroppedImage(25, 25);

        if (bitmap != null) {
            UploadTask task = new UploadTask();
            task.execute(bitmap);
        }
        mainActivity.updateUserdata();
    }


    private class UploadTask extends AsyncTask<Bitmap, Integer, Boolean> {

        private Bitmap bitmap;

        @Override
        protected Boolean doInBackground(Bitmap... params) {

            bitmap = (Bitmap) params[0];
            User u = UserDataProvider.getInstance().getUser();

            try {

                PictureHttpFileUpload hfu = new PictureHttpFileUpload(Constants.USER_PICTURE_UPDATE_URL, u.get_id());

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] byteArray = stream.toByteArray();

                ByteArrayInputStream b = new ByteArrayInputStream(byteArray);

                hfu.Send_Now(b);

            } catch (Throwable e) {
                e.printStackTrace();
                return false;

            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(Constants.APP_DIR + "/profilePic.jpg");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
                mainActivity.updateProfilePic();
                showToast("Profile updated.");
            } else {
                showToast("Could not save your picture. Please try again later.");
            }
        }
    }

}



