package com.valdioveliu.valdio.audioplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Xml;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;


import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 3 ;
    private Menu menu;
    private Menu menu2;
    ArrayList<Audio> web_songs = new ArrayList<Audio>();
    boolean done = false;
    DbInterface dao = new Db_DAO(this);
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.valdioveliu.valdio.audioplayer.PlayNewAudio";

    private MediaPlayerService player;
    boolean serviceBound = false;
    ArrayList<Audio> audioList;
    ImageView collapsingImageView;

    int imageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        collapsingImageView = (ImageView) findViewById(R.id.collapsingImageView);

        loadCollapsingImage(imageIndex);

        loadAudio();
        initRecyclerView();

        // Initialize the Mobile Ads SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                //Toast.makeText(this, " sucesfull ", Toast.LENGTH_SHORT).show();
            }
        });

        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                load();
                done=true;
            }
        });

        thread.start();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
                //play the first audio in the ArrayList
//                playAudio(2);
                if (imageIndex == 4) {
                    imageIndex = 0;
                    loadCollapsingImage(imageIndex);
                } else {
                    loadCollapsingImage(++imageIndex);
                }
            }
        });

    }


    private void initRecyclerView() {
        if (audioList.size() > 0) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
            RecyclerView_Adapter adapter = new RecyclerView_Adapter(audioList, getApplication());
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

    private void loadCollapsingImage(int i) {
        TypedArray array = getResources().obtainTypedArray(R.array.images);
        collapsingImageView.setImageDrawable(array.getDrawable(i));
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }

    //Binding this Client to the AudioPlayer Service
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
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            if(audioList.get(audioIndex).get_flag()==true)
            {
                menu.getItem(0).setIcon(R.drawable.ic_favorite_filled);
            }
            else {
                menu.getItem(0).setIcon(R.drawable.favorite_icon);
            }
           // fav_flag=true;

        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);
            if(audioList.get(audioIndex).get_flag()==true)
            {
                menu.getItem(0).setIcon(R.drawable.ic_favorite_filled);
            }
            else {
                menu.getItem(0).setIcon(R.drawable.favorite_icon);
            }
            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }


    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        audioList = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                // Save to audioList
                audioList.add(new Audio(data, title, album, artist));
            }
        }

        cursor.close();
        load_db();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
        	 unbindService(serviceConnection);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound=false;
        }
    }
    @Override
    protected void onPause() {

        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);

        this.menu2=menu;
        getMenuInflater().inflate(R.menu.menu_scrolling,menu);


        return super.onCreateOptionsMenu(menu);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.web_settings:
                Toast.makeText(this, "Search", LENGTH_SHORT).show();
                while(done==false)
                {

                }
                Intent intent = new Intent(this,web_list.class);
                intent.putExtra("list",web_songs);
                if(player != null) {
                player.stopSelf();
}

                startActivity(intent);
                return true;

            case R.id.menu_favorites:
                    if (serviceBound) {

                        StorageUtil storage = new StorageUtil(getApplicationContext());
                        int currentPosition= storage.loadAudioIndex();

                        boolean fav_flag = audioList.get(currentPosition).get_flag();

                        if (!fav_flag) {
                            Toast.makeText(this, "Added to Favorites", LENGTH_SHORT).show();
                            item.setIcon(R.drawable.ic_favorite_filled);

                            audioList.get(currentPosition).set_flag(true);

                            Audio favList = new Audio(audioList.get(currentPosition).getData(),audioList.get(currentPosition).getTitle(),audioList.get(currentPosition).getAlbum(),audioList.get(currentPosition).getArtist());

                                    favList.set_dao(dao);
                                    favList.save();

                        }
                        else {
                            item.setIcon(R.drawable.favorite_icon);
                            audioList.get(currentPosition).set_flag(false);
                                String n = audioList.get(currentPosition).getData();
                                DB_Helper dbHelper = new DB_Helper(this);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                String sq = "DELETE FROM " + "TABLE_SONGS" + " WHERE "+"data"+"='"+n+"'";
                                db.execSQL(sq);
                            Toast.makeText(this, "REMOVED FROM FAVORITES", LENGTH_SHORT).show();
                        }
                    }
                    return true;

            case R.id.action_settings:

                Toast.makeText(this, "Search", LENGTH_SHORT).show();
                ArrayList<Audio> db_list = new ArrayList<>();
                db_list=Audio.load(dao);
                Intent i = new Intent(this,Fav_List.class);
                i.putExtra("list",db_list);
                if(player != null) {
                    player.stopSelf();
                }

                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    void load_db()
    {
        ArrayList<Audio> db_list = new ArrayList<>();
        db_list=Audio.load(dao);

        for(Audio a:db_list)
        {
            for(Audio b: audioList)
            {
                if(a.getData().equals((b.getData())))
                {
                    b.set_flag(true);
                    break;
                }

            }
        }
    }



    private void load(){

        String line = "";
        //  TextView view = (TextView) findViewById(R.id.text);

        try{

            boolean a=isInternetOn(this);
            URL url = new URL("https://drive.google.com/uc?export=download&id=1wveQbSAex0xcESHk6H1XwpwC_DU-h0HC");
            //URL url = new URL("http://10.0.2.2/android/pictures.xml");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();

            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );


            while( (line = reader.readLine()) != null ){
                content.append(line);
            }

            line = content.toString();
            parse(line);
        } catch(Exception ex) {
            line = ex.getMessage();
            ex.printStackTrace();
        }

    }

    private void parse(String xml){

        String category = "";

        try{
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(xml));

            int event = parser.getEventType();
            while(event != XmlPullParser.END_DOCUMENT){

                if(event == XmlPullParser.START_TAG &&
                        parser.getName().equals("category") ) {

                    category = parser.getAttributeValue(null,"name");
                }

                if(event == XmlPullParser.START_TAG &&
                        parser.getName().equals("im") ){

                    String name = parser.getAttributeValue(null,"name");
                    String album = parser.getAttributeValue(null,"album");
                    String artist = parser.getAttributeValue(null,"artist");
                    String data = parser.getAttributeValue(null,"data");
                    web_songs.add(new Audio(data,name,album,artist));
                }

                event = parser.next();
            }
        } catch(Exception ex){ }
    }

    public static boolean isInternetOn(Context context) {

        if (isMobileOrWifiConnectivityAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (Exception e) {

            }
        } else {

        }
        return false;
    }


    public static boolean isMobileOrWifiConnectivityAvailable(Context ctx) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;


        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected()) {
                        haveConnectedWifi = true;
                    }
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected()) {
                        haveConnectedMobile = true;
                    }
            }
        } catch (Exception e) {

        }
        return haveConnectedWifi || haveConnectedMobile;
    }

}
