package com.cresan.antivirus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.cresan.androidprotector.R;

/**
 * Created by Magic Frame on 27/01/2016.
 */
public class SplashController extends Activity
{


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash);

        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(3000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{

                    if(EulaController.isEulaAccepted)
                    {
                        Intent intent = new Intent(SplashController.this,AntivirusActivity.class);
                        startActivity(intent);
                    }else
                    {
                        Intent intent = new Intent(SplashController.this,EulaController.class);
                        startActivity(intent);
                    }

                }
            }
        };
        timerThread.start();
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        finish();
    }
}
