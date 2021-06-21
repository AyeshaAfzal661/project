package com.valdioveliu.valdio.audioplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.valdioveliu.valdio.audioplayer.MainActivity.Broadcast_PLAY_NEW_AUDIO;

public class Fav_List extends AppCompatActivity {
        private MediaPlayerService player;
        boolean serviceBound = false;
        RecyclerView_Adapter adapter;
        RecyclerView recyclerView;
        ImageView collapsingImageView;
        private ArrayList<Audio> fav_list = new ArrayList<Audio>();
       TextView t;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_fav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
          t = findViewById(R.id.title);
          t.setText("My Favorites");
            collapsingImageView = (ImageView) findViewById(R.id.collapsingImageView);
            TypedArray array = getResources().obtainTypedArray(R.array.images);
            collapsingImageView.setImageDrawable(array.getDrawable(1));
        loadAudio();
        initRecyclerView();


    }
        void loadAudio()
        {
            Intent intent = getIntent();
            fav_list = (ArrayList<Audio>) intent.getSerializableExtra("list");

        }

        void initRecyclerView()
        {
            if (fav_list.size() > 0) {
                recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
                adapter = new RecyclerView_Adapter(fav_list, getApplication());
                recyclerView.setAdapter(adapter);

                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.addOnItemTouchListener(new CustomTouchListener(this, new onItemClickListener() {
                    @Override
                    public void onClick(View view, int index) {
                        playAudio(index);
                    }
                }));

            }
        }
        private ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
                player = binder.getService();
                serviceBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceBound = false;
            }
        };
        private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext()); ///////////p
            storage.storeAudio(fav_list);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);


        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }


        @Override
        protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);

            //service is active
            //   player.stopSelf();
        }
    }
}
