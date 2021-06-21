
package com.valdioveliu.valdio.audioplayer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.valdioveliu.valdio.audioplayer.MainActivity;


public class splash_activity extends Activity {

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(splash_activity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);

    }
}