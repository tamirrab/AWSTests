package com.slavafleer.awstests;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.regions.Regions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_PASSWORD = "password";

    private EditText editTextUserName;
    private EditText editTextPassword;

    private CognitoCachingCredentialsProvider credentialsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "eu-west-1:b2ec2309-fabc-4830-b3ae-e7c899537ad9", // Identity Pool ID
                Regions.EU_WEST_1 // Region
        );

        // Initialize UI Widgets
        editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
    }

    public void buttonSignUp_onClick(View view) {

        final String userName = editTextUserName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (userName.equals("") || password.equals("")) {
            Toast.makeText(this, "All fields must be filled.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize the Cognito Sync client
        CognitoSyncManager syncClient = new CognitoSyncManager(
                getApplicationContext(),
                Regions.EU_WEST_1, // Region
                credentialsProvider);

        // Create a record in a dataset and synchronize with the server
        Dataset dataset = syncClient.openOrCreateDataset(userName);
        dataset.put(KEY_USER_NAME, userName);
        dataset.put(KEY_PASSWORD, password);
        dataset.synchronize(new DefaultSyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List newRecords) {
                Log.i(TAG, "New user was created: " + userName);
            }

            @Override
            public boolean onConflict(Dataset dataset, List<SyncConflict> conflicts) {

                Log.d(TAG, dataset.toString());
                Log.d(TAG, conflicts.toString());

                return super.onConflict(dataset, conflicts);
            }
        });
    }
}
