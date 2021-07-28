/*
 * Copyright (c) 2021. Aravind Chowdary
 */

package me.aravi.repost;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.ixuea.android.downloader.DownloadService;
import com.ixuea.android.downloader.callback.DownloadListener;
import com.ixuea.android.downloader.callback.DownloadManager;
import com.ixuea.android.downloader.domain.DownloadInfo;
import com.ixuea.android.downloader.exception.DownloadException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.aravi.repost.adapter.PostsAdapter;
import me.aravi.repost.databinding.ActivityRepostBinding;
import me.aravi.repost.model.Repost;
import me.aravi.repost.utils.HttpHandler;
import me.aravi.repost.utils.NetworkUtil;
import me.aravi.repost.utils.NotificationUtils;


public class RepostActivity extends AppCompatActivity {

    private static final String TAG = RepostActivity.class.getSimpleName();
    private ArrayList<String> display_urls = new ArrayList<>();
    private ArrayList<String> video_urls = new ArrayList<>();
    private List<String> thumbnails = new ArrayList<>();
    private static String URL;

    private ActivityRepostBinding mBinding;
    private PostsAdapter adapter;
    private List<Repost> repostList;
    private DownloadManager downloadManager;
    private NotificationUtils notificationUtils;
    private Dialog dialog;


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void getPermission() {
        if (!(ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRepostBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        getPermission();
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mBinding.recyclerView.setLayoutManager(linearLayoutManager);
        mBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        downloadManager = DownloadService.getDownloadManager(getApplicationContext());
        notificationUtils = new NotificationUtils(this);

        repostList = new ArrayList<>();
        adapter = new PostsAdapter(repostList, this, false, null);
        mBinding.recyclerView.setAdapter(adapter);
        mBinding.fab.setVisibility(View.GONE);

        String url = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(url)) {
            mBinding.defaultLayout.setVisibility(View.GONE);
            int index = url.lastIndexOf("/");
            if (index > 0) {
                url = url.substring(0, index + 1);
            }
            URL = url + "?__a=1";
            if (URL.contains("://www.instagram.com/")) {
                if (NetworkUtil.getConnectivityStatus(getApplicationContext()) != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    new GetData().execute();
                } else {
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RepostActivity.this, "Invalid URL", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        mBinding.defaultLayout.setVisibility(View.VISIBLE);
    }


    public void openInstagram(View view) {
        String url = "https://www.instagram.com";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("com.instagram.android");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(RepostActivity.this, "Instagram not found", Toast.LENGTH_SHORT).show();
        }
    }


    public void downloadPost(View view) {
        if (!repostList.isEmpty()) {
            Toast.makeText(this, "Starting to download...", Toast.LENGTH_SHORT).show();
            for (String url : repostList.get(0).getDisplay_url()) {
                String[] text = url.split("\\?");
                String[] code = text[0].split("/");
                String fileName = code[code.length - 1];

                boolean success = true;
                String directory;
                File storageDir;

                //todo
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/HikeTop";

                } else {
                    directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/HikeTop/";
                }
                storageDir = new File(directory + "/Repost/");

                if (!storageDir.exists()) {
                    success = storageDir.mkdirs();
                }

                if (success) {

                    File imageFile = new File(storageDir, fileName);
                    if (imageFile.exists()) {
                        Toast.makeText(getApplicationContext(), "Looks like you already downloaded this post !!!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final DownloadInfo downloadInfo = new DownloadInfo.Builder().setUrl(url)
                            .setPath(imageFile.getAbsolutePath())
                            .build();
                    downloadInfo.setDownloadListener(new DownloadListener() {
                        @Override
                        public void onStart() {

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
                            notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Download successful", "Downloaded " + fileName, false, pendingIntent);

                        }

                        @Override
                        public void onDownloadFailed(DownloadException e) {
                            notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Download failed", "Couldn't download " + fileName, false, null);
                        }
                    });
                    downloadManager.download(downloadInfo);

                }

            }


            /**
             * ALgorithm for downloading video files in a post
             * It should support almost all kinds of posts
             * except private posts or reels (partially compatible)
             */
            for (String url : repostList.get(0).getVideo_urls()) {

                //StringBuilder fileName= new StringBuilder("RE_" + repostList.get(0).getUsername() + "_" + System.currentTimeMillis());
                String[] text = url.split("\\?");
                String[] code = text[0].split("/");
                String fileName = code[code.length - 1];

                boolean success = true;
                String directory;
                File storageDir;

                //todo
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/HikeTop";

                } else {
                    directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/HikeTop/";
                }
                storageDir = new File(directory + "/Repost/");

                if (!storageDir.exists()) {
                    success = storageDir.mkdirs();
                }

                if (success) {
                    File imageFile = new File(storageDir, fileName);
                    if (imageFile.exists()) {
                        Toast.makeText(getApplicationContext(), "Looks like you already downloaded this post !!!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final DownloadInfo downloadInfo = new DownloadInfo.Builder().setUrl(url)
                            .setPath(imageFile.getAbsolutePath())
                            .build();
                    downloadInfo.setDownloadListener(new DownloadListener() {
                        @Override
                        public void onStart() {
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
                            intent.setDataAndType(uri, "video/*");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                            galleryAddPic(imageFile);
                            Toast.makeText(RepostActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                            notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Download successful", "Downloaded " + fileName, false, pendingIntent);
                        }

                        @Override
                        public void onDownloadFailed(DownloadException e) {
                            Toast.makeText(RepostActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                            notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Download failed", "Couldn't download " + fileName, false, null);
                        }
                    });
                    downloadManager.download(downloadInfo);
                }

            }
        }

    }


    /**
     * @param file
     */
    private void galleryAddPic(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }


    /**
     * @param size
     * @return formated file name
     */
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

    /**
     * The main algorithm that gets all the data from adding ?__a=1 to the post url
     * gets the json response
     * and parses the response
     */
    private class GetData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            loadingDialog();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            display_urls.clear();
            video_urls.clear();
            thumbnails.clear();
            HttpHandler sh = new HttpHandler();
            String jsonStr = sh.makeServiceCall(URL);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    String caption = "", video_url = "null";

                    JSONObject graphql = jsonObj.getJSONObject("graphql");
                    JSONObject shortcode_media = graphql.getJSONObject("shortcode_media");
                    String display_url = shortcode_media.getString("display_url");
                    boolean is_video = shortcode_media.getBoolean("is_video");

                    if (is_video) {
                        video_url = shortcode_media.getString("video_url");
                    }

                    JSONObject owner = shortcode_media.getJSONObject("owner");
                    String username = owner.getString("username");
                    String full_name = owner.getString("full_name");
                    String profile_pic_url = owner.getString("profile_pic_url");

                    JSONObject edge_media_to_caption = shortcode_media.getJSONObject("edge_media_to_caption");
                    JSONArray edges_caption = edge_media_to_caption.getJSONArray("edges");
                    if (edges_caption != null && edges_caption.length() > 0) {
                        caption = edges_caption
                                .getJSONObject(0)
                                .getJSONObject("node")
                                .getString("text");
                    }

                    JSONObject edge_web_media_to_related_media = shortcode_media.getJSONObject("edge_web_media_to_related_media");
                    JSONArray edges = edge_web_media_to_related_media.getJSONArray("edges");

                    JSONArray edges1 = null;
                    try {
                        JSONObject edge_sidecar_to_children = shortcode_media.getJSONObject("edge_sidecar_to_children");
                        edges1 = edge_sidecar_to_children.getJSONArray("edges");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Checks for multiple posts
                    String edge_display_url, edge_video_url = "null";
                    boolean edge_is_video;
                    if (edges != null && edges.length() > 0) {

                        for (int i = 0; i < edges.length(); i++) {

                            JSONObject node = edges.getJSONObject(i).getJSONObject("node");

                            edge_display_url = node.getString("display_url");
                            edge_is_video = node.getBoolean("is_video");

                            if (edge_is_video) {
                                edge_video_url = node.getString("video_url");
                            }

                            runOnUiThread(() -> mBinding.defaultLayout.setVisibility(View.GONE));
                            video_urls.add(edge_video_url);
                            display_urls.add(edge_display_url);
                        }
                        Repost post_model = new Repost(profile_pic_url, video_urls, caption, username, full_name, display_urls, URL, String.valueOf(System.currentTimeMillis()));
                        repostList.add(0, post_model);

                    }
                    //Also checks for multiple posts another node
                    else if (edges1 != null && edges1.length() > 0) {

                        for (int i = 0; i < edges1.length(); i++) {

                            JSONObject node = edges1.getJSONObject(i).getJSONObject("node");

                            edge_display_url = node.getString("display_url");
                            edge_is_video = node.getBoolean("is_video");

                            if (edge_is_video) {
                                edge_video_url = node.getString("video_url");
                            }

                            display_urls.add(edge_display_url);
                            video_urls.add(edge_video_url);

                        }
                        Repost post_model = new Repost(profile_pic_url, video_urls, caption, username, full_name, display_urls, URL, String.valueOf(System.currentTimeMillis()));
                        repostList.add(0, post_model);

                    } else {

                        display_urls.add(display_url);
                        video_urls.add(video_url);
                        Repost post_model = new Repost(profile_pic_url, video_urls, caption, username, full_name, display_urls, URL, String.valueOf(System.currentTimeMillis()));
                        repostList.add(0, post_model);

                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Technical error : " + e.getMessage(),
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
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get post, maybe the account is private.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Completable.fromAction(() -> {
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            mBinding.recyclerView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                            mBinding.defaultLayout.setVisibility(View.GONE);
                            mBinding.fab.setVisibility(View.INVISIBLE);
                            Animation animation = AnimationUtils.loadAnimation(RepostActivity.this, R.anim.scale_up);
                            mBinding.fab.startAnimation(animation);
                            mBinding.fab.show();
                            stopLoading();
                        }

                        @Override
                        public void onError(Throwable e) {
                        }
                    });
        }
    }


    /**
     * Shows a loading dialog with a progress bar at center
     */
    public void loadingDialog() {
        dialog = new Dialog(RepostActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void stopLoading() {
        dialog.dismiss();
    }

    /**
     * @param message
     */
    private void showSnackbar(String message) {
        Snackbar.make(mBinding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }


}
