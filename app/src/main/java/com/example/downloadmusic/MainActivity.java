package com.example.downloadmusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.Console;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ConditionVariable mCondition;
    private int id ;
    final static int REQUEST_PERMISSION = 99 ;
    private MediaPlayer mediaPlayer;
    private boolean downloaded = false;

    private ImageView pp,stop ;
    private EditText link;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},REQUEST_PERMISSION);
        }
        // referencing views

        link = findViewById(R.id.link);

        pp = findViewById(R.id.pp);
        stop = findViewById(R.id.stop);

        mediaPlayer  = new MediaPlayer();

        pp.setVisibility(View.GONE);
        stop.setVisibility(View.GONE);

        Button download = findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!link.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,link.getText().toString(),Toast.LENGTH_SHORT).show();
                    Uri uri = Uri.parse(link.getText().toString());
                    pp.setVisibility(View.GONE);
                    stop.setVisibility(View.GONE);
                    new DownloadThread().execute(uri);
                }else{
                    Toast.makeText(MainActivity.this,"Enter a Link",Toast.LENGTH_SHORT).show();
                }
            }
        });

        pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri myUri1 = Uri.parse("file://musicdownloaded/"+"downloadedMusic"+id+".mp3");
                if (downloaded){
                    if(mediaPlayer.isPlaying()) mediaPlayer.pause();
                    else{
                        if(stop.getVisibility() == View.GONE){
                            mediaPlayer.reset();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            try {
                                mediaPlayer.setDataSource(getApplicationContext(), myUri1);
                                mediaPlayer.prepare();
                                mediaPlayer.seekTo(0);
                                mediaPlayer.start();
                                stop.setVisibility(View.VISIBLE);

                            } catch (IOException e) {
                            }
                        }else{
                            mediaPlayer.start();
                        }
                    }

                }

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();

                pp.setVisibility(View.VISIBLE);
                stop.setVisibility(View.GONE);
            }
        });
    }

    class DownloadThread extends AsyncTask<Uri, Integer, Integer> {

        @Override
        protected Integer doInBackground(Uri... uris) {
            DownloadData(uris[0]);
            return 0;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        @Override
        protected void onPostExecute(Integer s) {
            link.setText("");

            stop.setVisibility(View.GONE);
            pp.setVisibility(View.VISIBLE);
        }

        private void DownloadData (Uri uri) {

            DownloadManager downloadmanager = (DownloadManager)
                    getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("downloaded music");
            request.setDescription("Downloading");


            id++;
            request.setDestinationInExternalPublicDir("/musicdownloaded/",
                    "downloadedMusic"+id+".mp3"  );

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            final long downloadId=downloadmanager.enqueue(request);

            mCondition = new ConditionVariable(false);
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

            Log.i("here","it is here");
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (downloadId == reference) {
                        mCondition.open();
                        downloaded = true;
                    }
                }
            };
            Log.i("here", String.valueOf(downloaded));
            getApplicationContext().registerReceiver(receiver, filter);
            mCondition.block();

        }
    }
}