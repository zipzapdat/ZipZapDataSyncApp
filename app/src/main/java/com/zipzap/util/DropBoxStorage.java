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

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.zipzap.ZipZapProvider;


public class DropBoxStorage {
	
	private static DropBoxStorage _instance = null;
	
	private Context context;
	private DropboxAPI<AndroidAuthSession> mApi;
	private String serverPath;
	
	public static DropBoxStorage getInstance(Context context) {
		if (_instance == null)
			_instance = new DropBoxStorage(context, "/Data/");
		return _instance;
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// Your app-specific settings. //
	// /////////////////////////////////////////////////////////////////////////

	// Replace this with your app key and secret assigned by Dropbox.
	// Note that this is a really insecure way to do this, and you shouldn't
	// ship code which contains your key & secret in such an obvious way.
	// Obfuscation is good.
	final static protected String APP_KEY = "jo071zwzq0f1mo7";
	final static protected String APP_SECRET = "2hfcowksgbq3uy9";

	// /////////////////////////////////////////////////////////////////////////
	// End app-specific settings. //
	// /////////////////////////////////////////////////////////////////////////

	// You don't need to change these, leave them alone.
	final static protected String ACCOUNT_PREFS_NAME = "prefs";
	final static protected String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static protected String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	protected static final boolean USE_OAUTH1 = false;

	protected boolean mLoggedIn;

	final static protected int NEW_PICTURE = 1;
	protected String mCameraFileName;

	// //////////////////////
	private DropBoxStorage(Context context, String path) {
		this.context = context;
		this.serverPath = path;

		// We create a new AuthSession so that we can use the Dropbox API.
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);

		checkAppKeySetup();

		// Start the remote authentication
		if (USE_OAUTH1) {
			mApi.getSession().startAuthentication(context);
		} else {
			mApi.getSession().startOAuth2Authentication(context);
		}

	}

	public void upload(File mypath) {
		UploadPicture upload = new UploadPicture(mApi, serverPath, mypath);
        upload.execute();
	}
	
	// /////////
	private void checkAppKeySetup() {
		// Check to make sure that we have a valid app key
		if (APP_KEY.startsWith("CHANGE") || APP_SECRET.startsWith("CHANGE")) {
			showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
			return;
		}

		// Check if the app has set up its manifest properly.
		Intent testIntent = new Intent(Intent.ACTION_VIEW);
		String scheme = "db-" + APP_KEY;
		String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
		testIntent.setData(Uri.parse(uri));
		PackageManager pm = context.getPackageManager();
		if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
			showToast("URL scheme in your app's "
					+ "manifest is not set up correctly. You should have a "
					+ "com.dropbox.client2.android.AuthActivity with the "
					+ "scheme: " + scheme);
		}

	}
	
	public void showToast(String msg) {
		Toast error = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		error.show();
	}

	private void logOut() {
		// Remove credentials from the session
		mApi.getSession().unlink();

		// Clear our stored keys
		clearKeys();

	}
	
	private void clearKeys() {
		SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

		AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
		loadAuth(session);
		return session;
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a
	 * local store, rather than storing user name & password, and
	 * re-authenticating each time (which is not to be done, ever).
	 */
	private void loadAuth(AndroidAuthSession session) {
		SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key == null || secret == null || key.length() == 0
				|| secret.length() == 0)
			return;

		if (key.equals("oauth2:")) {
			// If the key is set to "oauth2:", then we can assume the token is
			// for OAuth 2.
			session.setOAuth2AccessToken(secret);
		} else {
			// Still support using old OAuth 1 tokens.
			session.setAccessTokenPair(new AccessTokenPair(key, secret));
		}
	}

	public void download(String path, ContentResolver contentResolver) {
		
		DownloadFile download = new DownloadFile(mApi, path);
        download.execute(contentResolver);
        
	}
	
	public Boolean syncZipZapDB() {
        try {
        	
        	String basePath = "/data/data/com.zipzap/";

        	
            // Get the metadata for a directory
        	Entry entries = mApi.metadata("/Data/", 100, null, true, null);
        	
        	for (Entry e : entries.contents) {
        	    if (!e.isDeleted) {
        	        System.out.println("Files Name : "+e.fileName());
        	        try {
        	        	
						FileOutputStream fos = new FileOutputStream(basePath+e.fileName());
						mApi.getFile(basePath+e.fileName(), null, fos, null);
						
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
                	
        	    }
        	}
        	
        	return true;
        	
        } catch (DropboxUnlinkedException e) {
            // The AuthSession wasn't properly authenticated or user unlinked.
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
//            mErrorMsg = "Download canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._304_NOT_MODIFIED) {
                // won't happen since we don't pass in revision with metadata
            } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
                // too many entries to return
            } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
                // can't be thumbnailed
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
            // This gets the Dropbox error, translated into the user's language
//            mErrorMsg = e.body.userError;
//            if (mErrorMsg == null) {
//                mErrorMsg = e.body.error;
//            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
//            mErrorMsg = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
//            mErrorMsg = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            // Unknown error
//            mErrorMsg = "Unknown error.  Try again.";
        }
		return false;
		
	}

	public AndroidAuthSession getSession() {
		return mApi.getSession();
	}
	
	/**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    public void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        // Store the OAuth 1 access token, if there is one.  This is only necessary if
        // you're still using OAuth 1.
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
            return;
        }
    }
    
}


class UploadPicture extends AsyncTask<Void, Long, Boolean> {

    private DropboxAPI<?> mApi;
    private String mPath;
    private File mFile;

    private UploadRequest mRequest;

    private String mErrorMsg;


    public UploadPicture(DropboxAPI<?> api, String dropboxPath, File file) {
        // We set the context this way so we don't accidentally leak activities
//        mContext = context.getApplicationContext();

        mApi = api;
        mPath = dropboxPath;
        mFile = file;

//        mDialog = new ProgressDialog(context);
//        mDialog.setMax(100);
//        mDialog.setMessage("Uploading " + file.getName());
//        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        mDialog.setProgress(0);
//        mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // This will cancel the putFile operation
//                mRequest.abort();
//            }
//        });
//        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            // By creating a request, we get a handle to the putFile operation,
            // so we can cancel it later if we want to
            FileInputStream fis = new FileInputStream(mFile);
            String path = mPath + mFile.getName();
            mRequest = mApi.putFileOverwriteRequest(path, fis, mFile.length(),
                    new ProgressListener() {
                @Override
                public long progressInterval() {
                    // Update the progress bar every half-second or so
                    return 500;
                }

                @Override
                public void onProgress(long bytes, long total) {
                    publishProgress(bytes);
                }
            });

            if (mRequest != null) {
                mRequest.upload();
                return true;
            }

        } catch (DropboxUnlinkedException e) {
            // This session wasn't authenticated properly or user unlinked
            mErrorMsg = "This app wasn't authenticated properly.";
        } catch (DropboxFileSizeException e) {
            // File size too big to upload via the API
            mErrorMsg = "This file is too big to upload";
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Upload canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
            // This gets the Dropbox error, translated into the user's language
            mErrorMsg = e.body.userError;
            if (mErrorMsg == null) {
                mErrorMsg = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            mErrorMsg = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            mErrorMsg = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            // Unknown error
            mErrorMsg = "Unknown error.  Try again.";
        } catch (FileNotFoundException e) {
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
//        int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
//        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
//        mDialog.dismiss();
        if (result) {
            showToast("Image successfully uploaded");
        } else {
            showToast(mErrorMsg);
        }
    }

    private void showToast(String msg) {
//        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
//        error.show();
    	System.out.println(msg);
    }
}

class DownloadFile extends AsyncTask<ContentResolver, Long, Boolean> {


    private DropboxAPI<?> mApi;
    private String mPath;

    private FileOutputStream mFos;
    private String mErrorMsg;

    public DownloadFile(DropboxAPI<?> api,
            String dropboxPath) {
        // We set the context this way so we don't accidentally leak activities
        mApi = api;
        mPath = dropboxPath;

    }

    @Override
    protected Boolean doInBackground(ContentResolver... cont) {
        try {
        		
    		File mypath = new File(mPath);
    		Boolean newInstance = !mypath.exists();

            try {
            	mFos = new FileOutputStream(mypath);
            } catch (FileNotFoundException e) {
                mErrorMsg = "Couldn't create a local file";
                return false;
            }
    		mApi.getFile("/Data/file1", null, mFos, null);
    		
    		updateDBContent(cont[0], newInstance, mPath);
    		
            return true;

        } catch (DropboxUnlinkedException e) {
            // The AuthSession wasn't properly authenticated or user unlinked.
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Download canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._304_NOT_MODIFIED) {
                // won't happen since we don't pass in revision with metadata
            } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
                // too many entries to return
            } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
                // can't be thumbnailed
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
            // This gets the Dropbox error, translated into the user's language
            mErrorMsg = e.body.userError;
            if (mErrorMsg == null) {
                mErrorMsg = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            mErrorMsg = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            mErrorMsg = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            // Unknown error
            mErrorMsg = "Unknown error.  Try again.";
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
//        int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
//        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
//        mDialog.dismiss();
        if (result) {
            // Set the image now that we have it
        } else {
            // Couldn't download it, so show an error
            showToast(mErrorMsg);
        }
    }
    
	private void updateDBContent(ContentResolver contentResolver, Boolean newInstance, String filePath) {
		
		try {
//			String mypath = "/data/data/com.zipzap/file1";
			File file = new File(filePath);

			// if file doesn't exists, then create it and put some default value
			if (!file.exists()) {
				
				file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write("10,20,30,40,0\n");

				bw.close();
			}
			
			printFileContent();
			System.out.println("newInstance = "+newInstance);
			
			FileInputStream in = new FileInputStream(file);
			int size = (int) file.length();
			byte[] fileData = new byte[size];

			in.read(fileData, 0, size);
			in.close();
			
			ContentValues values = new ContentValues();
			values.put(ZipZapProvider.name, fileData);
			if (newInstance)
				contentResolver.insert(ZipZapProvider.CONTENT_URI, values);
			else {
				String[] args = new String[1];
				if(filePath.contains("file1"))
					args[0] = "1";
				else
					args[0] = "2";
				contentResolver.update(ZipZapProvider.CONTENT_URI, values, "id=?", args);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private void printFileContent() {
        File fin = new File("/data/data/com.zipzap/file1");
        FileInputStream fis;
          try {
              
              fis = new FileInputStream(fin);
              //Construct BufferedReader from InputStreamReader
              BufferedReader br = new BufferedReader(new InputStreamReader(fis));
           
              String line = null;
              System.out.println("printFileContent...............");
//              while ((line = br.readLine()) != null) {
//                  System.out.println(line);
//              }
              br.close();

          } catch (FileNotFoundException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
	}

	private void showToast(String msg) {
//        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
//        error.show();
    	System.out.println(msg);
    }


}