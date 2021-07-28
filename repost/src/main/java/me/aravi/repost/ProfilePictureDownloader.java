/*
 * Copyright (c) 2020. Aravind Chowdary
 */

package me.aravi.repost;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ixuea.android.downloader.DownloadService;
import com.ixuea.android.downloader.callback.DownloadListener;
import com.ixuea.android.downloader.callback.DownloadManager;
import com.ixuea.android.downloader.domain.DownloadInfo;
import com.ixuea.android.downloader.exception.DownloadException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import me.aravi.repost.databinding.ActivityProfilePictureDownloaderBinding;
import me.aravi.repost.utils.HttpHandler;
import me.aravi.repost.utils.NetworkUtil;
import me.aravi.repost.utils.NotificationUtils;

public class ProfilePictureDownloader extends AppCompatActivity {
    private static final String TAG = ProfilePictureDownloader.class.getSimpleName();
    private ActivityProfilePictureDownloaderBinding mBinding;
    private static String URL;
    private Map<String, Object> map = new HashMap<>();
    private String jsonStr;
    private DownloadManager downloadManager;
    private NotificationUtils notificationUtils;


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityProfilePictureDownloaderBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        notificationUtils = new NotificationUtils(getApplicationContext());

        downloadManager = DownloadService.getDownloadManager(getApplicationContext());
        mBinding.fab.setOnClickListener(v -> {
            if (!map.isEmpty()) {
                String fileName = map.get("username") + ".png";
                boolean success = true;
                String directory;
                File storageDir;
                directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/HikeTop/";
                storageDir = new File(directory + "/DP/");
                if (!storageDir.exists()) {
                    success = storageDir.mkdirs();
                }
                if (success) {
                    File imageFile = new File(storageDir, fileName);
                    if (imageFile.exists()) {
                        Toast.makeText(getApplicationContext(), "Looks like you already downloaded it.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final DownloadInfo downloadInfo = new DownloadInfo.Builder().setUrl(map.get("profile_pic_url_hd").toString())
                            .setPath(imageFile.getAbsolutePath())
                            .build();
                    downloadInfo.setDownloadListener(new DownloadListener() {
                        @Override
                        public void onStart() {
                            Toast.makeText(getApplicationContext(), "Starting to download...", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onWaited() {
                        }

                        @Override
                        public void onPaused() {
                        }

                        @Override
                        public void onDownloading(long progress, long size) {
                            notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Downloading " + fileName, "Downloaded " + formatFileSize(progress) + "/" + formatFileSize(size), true, null);
                        }

                        @Override
                        public void onRemoved() {
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                            notificationManager.cancel(downloadInfo.hashCode());
                        }

                        @Override
                        public void onDownloadSuccess() {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", imageFile);
                            intent.setDataAndType(uri, "image/*");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                            galleryAddPic(imageFile);
                            Toast.makeText(ProfilePictureDownloader.this, "Download Completed", Toast.LENGTH_SHORT).show();
                            notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Download successful", "Downloaded " + fileName, false, pendingIntent);
                        }

                        @Override
                        public void onDownloadFailed(DownloadException e) {
                            Toast.makeText(ProfilePictureDownloader.this, "Download Failed", Toast.LENGTH_SHORT).show();
                            notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Download failed", "Couldn't download " + fileName, false, null);
                        }
                    });
                    downloadManager.download(downloadInfo);
                }

            } else {
                Toast.makeText(ProfilePictureDownloader.this, "Error saving profile picture", Toast.LENGTH_SHORT).show();
            }

        });

    }

    private void galleryAddPic(File f) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    public void getProfilePicture(View view) {

        String username_txt = mBinding.username.getText().toString();
        if (TextUtils.isEmpty(username_txt) || username_txt.length() < 2) {
            Toast.makeText(this, "Enter a valid username", Toast.LENGTH_SHORT).show();
            return;
        }

        URL = "https://www.instagram.com/" + username_txt + "/?__a=1";
        if (NetworkUtil.getConnectivityStatus(getApplicationContext()) != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
            new GetData().execute();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public static String formatFileSize(long size) {
        String sFileSize = "";
        if (size > 0) {
            double dFileSize = (double) size;

            double kiloByte = dFileSize / 1024;
            if (kiloByte < 1 && kiloByte > 0) {
                return size + "Byte";
            }
            double megaByte = kiloByte / 1024;
            if (megaByte < 1) {
                sFileSize = String.format("%.2f", kiloByte);
                return sFileSize + "K";
            }

            double gigaByte = megaByte / 1024;
            if (gigaByte < 1) {
                sFileSize = String.format("%.2f", megaByte);
                return sFileSize + "M";
            }

            double teraByte = gigaByte / 1024;
            if (teraByte < 1) {
                sFileSize = String.format("%.2f", gigaByte);
                return sFileSize + "G";
            }

            sFileSize = String.format("%.2f", teraByte);
            return sFileSize + "T";
        }
        return "0K";
    }

    private class GetData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (mBinding.fab.isShown()) {
                Animation animation = AnimationUtils.loadAnimation(ProfilePictureDownloader.this, R.anim.scale_down);
                mBinding.fab.startAnimation(animation);
                mBinding.fab.hide();
            }

            Glide.with(ProfilePictureDownloader.this)
                    .load(ContextCompat.getDrawable(ProfilePictureDownloader.this, R.drawable.ic_outline_account_circle_24))
                    .into(mBinding.profilePic);

            mBinding.pbar.setVisibility(View.VISIBLE);
            mBinding.button.setEnabled(false);
            mBinding.username.setEnabled(false);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            map.clear();
            HttpHandler sh = new HttpHandler();
            jsonStr = sh.makeServiceCall(URL);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    JSONObject graphql = jsonObj.getJSONObject("graphql");
                    JSONObject user = graphql.getJSONObject("user");
                    JSONObject edge_followed_by = user.getJSONObject("edge_followed_by");
                    int followers_count = edge_followed_by.getInt("count");
                    JSONObject edge_follow = user.getJSONObject("edge_follow");
                    int following_count = edge_follow.getInt("count");
                    String full_name = user.getString("full_name");
                    String profile_pic_url_hd = user.getString("profile_pic_url_hd");
                    String username = user.getString("username");

                    map.put("profile_pic_url_hd", profile_pic_url_hd);
                    map.put("username", username);
                    map.put("full_name", full_name);
                    map.put("followers_count", followers_count);
                    map.put("following_count", following_count);

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Some technical error occurred",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(jsonStr)) {

                            Toast.makeText(getApplicationContext(),
                                    "No user found",
                                    Toast.LENGTH_LONG)
                                    .show();

                        } else {

                            Toast.makeText(getApplicationContext(),
                                    "Some technical error occurred",
                                    Toast.LENGTH_LONG)
                                    .show();

                        }
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mBinding.button.setEnabled(true);
            mBinding.username.setEnabled(true);
            if (!map.isEmpty()) {
                Animation animation = AnimationUtils.loadAnimation(ProfilePictureDownloader.this, R.anim.scale_up);
                mBinding.fab.startAnimation(animation);
                mBinding.fab.show();

                Glide.with(ProfilePictureDownloader.this)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_outline_account_circle_24))
                        .load(map.get("profile_pic_url_hd"))
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                mBinding.pbar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                mBinding.pbar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(mBinding.profilePic);
            } else {
                mBinding.pbar.setVisibility(View.GONE);
            }
        }
    }


}