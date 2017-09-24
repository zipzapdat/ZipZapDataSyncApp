package com.zipzap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;

public class GoogleDriveStorage {
	
    protected static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    protected static final int REQUEST_CODE_CREATOR = 2;
    protected static final int REQUEST_CODE_RESOLUTION = 3;
	
    private static final String TAG = "GoogleDriveWrapper";
    private GoogleApiClient mGoogleApiClient;
    private Activity parentActivity;
    
    public GoogleDriveStorage(GoogleApiClient mGoogleApiClient, Activity googleBaseAct) {
		this.mGoogleApiClient = mGoogleApiClient;
		this.parentActivity = googleBaseAct;
	}

	/**
     * Create a new file and save it to Drive.
     */
    public void upload(final File file) {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
//        final Bitmap image = mBitmapToSave;
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveContentsResult>() {

            @Override
            public void onResult(DriveContentsResult result) {
                // If the operation was not successful, we cannot do anything
                // and must
                // fail.
                if (!result.getStatus().isSuccess()) {
                    Log.i(TAG, "Failed to create new contents.");
                    return;
                }
                // Otherwise, we can write our data to the new contents.
                Log.i(TAG, "New contents created.");
                
                try {
                	
                	// Get an output stream for the contents.
                    OutputStream outputStream = result.getDriveContents().getOutputStream();
                	
	                FileInputStream fis = new FileInputStream(file);
	                byte[] buffer = new byte[4096]; // To hold file contents
	                int bytes_read; 
	                
	                while ((bytes_read = fis.read(buffer)) != -1)	// Read until EOF	                  
	                	outputStream.write(buffer, 0, bytes_read);
	                
	                fis.close();
                
                } catch (IOException e1) {
                    Log.i(TAG, "Unable to write file contents.");
                }
                // Create the initial metadata - MIME type and title.
                // Note that the user will be able to change the title later.
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setMimeType("image/jpeg").setTitle("Android Photo.png").build();
                // Create an intent for the file chooser, and start it.
                IntentSender intentSender = Drive.DriveApi
                        .newCreateFileActivityBuilder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(result.getDriveContents())
                        .build(mGoogleApiClient);
                try {
                	parentActivity.startIntentSenderForResult(
                            intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                } catch (SendIntentException e) {
                    Log.i(TAG, "Failed to launch file chooser.");
                }
            }
        });
    }

}
