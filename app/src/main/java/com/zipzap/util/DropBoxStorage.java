package com.zipzap.util;

import android.app.ProgressDialog;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;


import com.dropbox.core.v2.files.WriteMode;
import com.zipzap.DropboxClientFactory;
import com.zipzap.UriHelpers;
import com.zipzap.ZipZapProvider;


public class DropBoxStorage {

    private static final String TAG = DropBoxStorage.class.getName();
	
	private static DropBoxStorage _instance = null;

	private String serverPath;
	
	public static DropBoxStorage getInstance() {
		if (_instance == null)
			_instance = new DropBoxStorage("/Data/");
		return _instance;
	}

	private DropBoxStorage(String path) {
		this.serverPath = path;
	}

	public void upload(Context context, String path) {
//		UploadPicture upload = new UploadPicture(mApi, serverPath, mypath);
//      upload.execute();
	}

    public void download(Context context, String path) {
//		DownloadFile download = new DownloadFile(mApi, path);
//      download.execute(contentResolver);
    }

	private void downloadFile(final Context context, FileMetadata file) {

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Downloading");
        dialog.show();

        new DownloadFileTask(context, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();

                if (result != null) {
//                    viewFileInExternalApp(result);
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to download file.", e);
                Toast.makeText(context,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file);

    }

    private void uploadFile(final Context context, String fileUri) {

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading");
        dialog.show();

        String mPath = "";

        new UploadFileTask(context, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                dialog.dismiss();

                String message = result.getName() + " size " + result.getSize() + " modified " +
                        DateFormat.getDateTimeInstance().format(result.getClientModified());
                Toast.makeText(context, message, Toast.LENGTH_SHORT)
                        .show();

            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to upload file.", e);
                Toast.makeText(context,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(fileUri, mPath);
    }



	
	public Boolean syncZipZapDB() {

		return false;
		
	}
    
}



/**
 * Task to download a file from Dropbox and put it in the Downloads folder
 */
class DownloadFileTask extends AsyncTask<FileMetadata, Void, File> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

    public DownloadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

    @Override
    protected File doInBackground(FileMetadata... params) {
        FileMetadata metadata = params[0];
        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, metadata.getName());

            // Make sure the Downloads directory exists.
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    mException = new RuntimeException("Unable to create directory: " + path);
                }
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }

            // Download the file.
            OutputStream outputStream = new FileOutputStream(file);
            mDbxClient.files().download(metadata.getPathLower(), metadata.getRev())
                    .download(outputStream);


            // Tell android about the file
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            mContext.sendBroadcast(intent);

            return file;
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}


/**
 * Async task to upload a file to a directory
 */
class UploadFileTask extends AsyncTask<String, Void, FileMetadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(FileMetadata result);
        void onError(Exception e);
    }

    public UploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
        }
    }

    @Override
    protected FileMetadata doInBackground(String... params) {
        String localUri = params[0];
        File localFile = UriHelpers.getFileForUri(mContext, Uri.parse(localUri));

        if (localFile != null) {
            String remoteFolderPath = params[1];

            // Note - this is not ensuring the name is a valid dropbox file name
            String remoteFileName = localFile.getName();

            try {
                InputStream inputStream = new FileInputStream(localFile);
                return mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
            } catch (DbxException | IOException e) {
                mException = e;
            }

        }

        return null;
    }
}

