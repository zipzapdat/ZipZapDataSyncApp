package com.zipzap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

//import com.dropbox.client2.DropboxAPI;
//import com.dropbox.client2.android.AndroidAuthSession;

public class ImageUtility {

	//public static void saveImage(Bitmap b, String fileName, Context context, DropboxAPI<AndroidAuthSession> mApi)
	public static void saveImage(Bitmap b, String fileName, Context context)
			throws SaveFileException {

		ContextWrapper cw = new ContextWrapper(context);
		// path to /data/data/yourapp/app_data/imageDir
		File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
		// Create imageDir
		File mypath = new File(directory, "design.png");

		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(mypath);
			// Use the compress method on the BitMap object to write image to
			// the OutputStream
			b.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();

		} catch (Throwable t) {
			throw new SaveFileException();
		}

		Log.e("Arun", directory.getAbsolutePath());
		Toast error = Toast.makeText(context, directory.getAbsolutePath()
				.toString(), Toast.LENGTH_LONG);
		error.show();
		
		//////////////////
        DropBoxStorage dStorage = DropBoxStorage.getInstance(context);
        dStorage.upload(mypath);
        
//        GoogleDriveStorage storage = new GoogleDriveStorage(com.zipzap.BackupAppActivity.mGoogleApiClient, com.zipzap.BackupAppActivity.googleBaseAct);
//        storage.upload(mypath);
		//////////////////
		
	}

	public static boolean loadImageFromStorage(ImageView img, Context context) {

		ContextWrapper cw = new ContextWrapper(context);
		// path to /data/data/yourapp/app_data/imageDir
		File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
		// Create imageDir
		File mypath = new File(directory, "design.png");

		try {

			Bitmap b = BitmapFactory.decodeStream(new FileInputStream(mypath));

			img.setImageBitmap(b);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;

	}

}
