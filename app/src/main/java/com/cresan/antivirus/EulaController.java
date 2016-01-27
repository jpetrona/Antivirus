package com.cresan.antivirus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;

import com.cresan.androidprotector.R;
/**
 * Created by Magic Frame on 27/01/2016.
 */
public class EulaController extends Activity
{




    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eula);
        Button _acceptEula = (Button) findViewById(R.id.accept_eula_button);
        Button _declineEula = (Button) findViewById(R.id.decline_eula_button);

        _acceptEula.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AppData appData = AppData.getInstance(EulaController.this);
                appData.setEulaAccepted(true);
                appData.serialize(EulaController.this);
                Intent intent = new Intent(EulaController.this,AntivirusActivity.class);
                startActivity(intent);
            }
        });

        _declineEula.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

    }



}
