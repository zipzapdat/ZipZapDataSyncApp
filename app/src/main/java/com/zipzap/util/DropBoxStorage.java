package com.zipzap.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

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


import com.zipzap.ZipZapProvider;


public class DropBoxStorage {
	
	private static DropBoxStorage _instance = null;
	
	private Context context;
	private String serverPath;
	
	public static DropBoxStorage getInstance(Context context) {
		if (_instance == null)
			_instance = new DropBoxStorage(context, "/Data/");
		return _instance;
	}

	private DropBoxStorage(Context context, String path) {
		this.context = context;
		this.serverPath = path;
	}

	public void upload(File mypath) {
//		UploadPicture upload = new UploadPicture(mApi, serverPath, mypath);
//        upload.execute();
	}


	private void downloadFile(FileMetadata file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Downloading");
        dialog.show();

        new DownloadFileTask(FilesActivity.this, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();

                if (result != null) {
                    viewFileInExternalApp(result);
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to download file.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file);

    }

    private void uploadFile(String fileUri) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading");
        dialog.show();

        new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                dialog.dismiss();

                String message = result.getName() + " size " + result.getSize() + " modified " +
                        DateFormat.getDateTimeInstance().format(result.getClientModified());
                Toast.makeText(FilesActivity.this, message, Toast.LENGTH_SHORT)
                        .show();

                // Reload the folder
                loadData();
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to upload file.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(fileUri, mPath);
    }
	

	public void showToast(String msg) {
		Toast error = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		error.show();
	}


	public void download(String path, ContentResolver contentResolver) {
//		DownloadFile download = new DownloadFile(mApi, path);
//      download.execute(contentResolver);
	}
	
	
	public Boolean syncZipZapDB() {
//        try {
//
//        	String basePath = "/data/data/com.zipzap/";
//
//
//            // Get the metadata for a directory
//        	Entry entries = mApi.metadata("/Data/", 100, null, true, null);
//
//        	for (Entry e : entries.contents) {
//        	    if (!e.isDeleted) {
//        	        System.out.println("Files Name : "+e.fileName());
//        	        try {
//
//						FileOutputStream fos = new FileOutputStream(basePath+e.fileName());
//						mApi.getFile(basePath+e.fileName(), null, fos, null);
//
//					} catch (FileNotFoundException e1) {
//						e1.printStackTrace();
//					}
//
//        	    }
//        	}
//
//        	return true;
//
//        } catch (DropboxUnlinkedException e) {
//            // The AuthSession wasn't properly authenticated or user unlinked.
//        } catch (DropboxPartialFileException e) {
//            // We canceled the operation
////            mErrorMsg = "Download canceled";
//        } catch (DropboxServerException e) {
//            // Server-side exception.  These are examples of what could happen,
//            // but we don't do anything special with them here.
//            if (e.error == DropboxServerException._304_NOT_MODIFIED) {
//                // won't happen since we don't pass in revision with metadata
//            } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
//                // Unauthorized, so we should unlink them.  You may want to
//                // automatically log the user out in this case.
//            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
//                // Not allowed to access this
//            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
//                // path not found (or if it was the thumbnail, can't be
//                // thumbnailed)
//            } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
//                // too many entries to return
//            } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
//                // can't be thumbnailed
//            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
//                // user is over quota
//            } else {
//                // Something else
//            }
//            // This gets the Dropbox error, translated into the user's language
////            mErrorMsg = e.body.userError;
////            if (mErrorMsg == null) {
////                mErrorMsg = e.body.error;
////            }
//        } catch (DropboxIOException e) {
//            // Happens all the time, probably want to retry automatically.
////            mErrorMsg = "Network error.  Try again.";
//        } catch (DropboxParseException e) {
//            // Probably due to Dropbox server restarting, should retry
////            mErrorMsg = "Dropbox error.  Try again.";
//        } catch (DropboxException e) {
//            // Unknown error
////            mErrorMsg = "Unknown error.  Try again.";
//        }
		return false;
		
	}
    
}
