package com.zipzap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.zipzap.util.DropBoxStorage;

public class BackupActivity extends Activity 
	implements OnClickListener {

	protected static final String TAG = "BackupActivity";

	private CheckBox mCbGoogleDrive;
	private CheckBox mCbDropBox;
	private CheckBox mCbOneDrive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_backup);
		mCbGoogleDrive = (CheckBox) findViewById(R.id.bv_cb_google);
		mCbDropBox = (CheckBox) findViewById(R.id.bv_cb_dropbox);
		mCbOneDrive = (CheckBox) findViewById(R.id.bv_cb_onedrive);
		mCbGoogleDrive.setOnClickListener(this);
		mCbDropBox.setOnClickListener(this);
		mCbOneDrive.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.bv_cb_google:
			googleBackup(view);
			break;
		case R.id.bv_cb_dropbox:
			dropBoxBackup(view);
			break;

		case R.id.bv_cb_onedrive:
			onedriveBackup(view);
			break;

		default:
			break;
		}

	}

	private void onedriveBackup(View view) {
		CheckBox checkBox = (CheckBox) view;
		if (checkBox.isChecked()) {
			Toast.makeText(BackupActivity.this, "one drive checked", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(BackupActivity.this, "one drive unchecked", Toast.LENGTH_SHORT).show();
		}
	}

	private void dropBoxBackup(View view) {
		CheckBox checkBox = (CheckBox) view;
		if (checkBox.isChecked()) {
			Toast.makeText(BackupActivity.this, "drop box checked", Toast.LENGTH_SHORT).show();
			
			//initiate the config if needed
			DropBoxStorage dStorage = DropBoxStorage.getInstance(this);
			dStorage.getSession();
			
		} else {
			Toast.makeText(BackupActivity.this, "drop box unchecked", Toast.LENGTH_SHORT).show();
		}
	}

	private void googleBackup(View view) {
		CheckBox checkBox = (CheckBox) view;
		if (checkBox.isChecked()) {
			Toast.makeText(BackupActivity.this, "google checked", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(BackupActivity.this, "google unchecked", Toast.LENGTH_SHORT).show();
		}
	}

}
