package com.zipzap;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dropbox.core.android.Auth;

import com.zipzap.util.DropBoxStorage;

public class MainActivity extends DropboxActivity implements OnClickListener {
	private LinearLayout mLlBackup;
	private LinearLayout mLlRestore;
	private ImageView mIvSync;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mLlBackup = (LinearLayout) findViewById(R.id.home_backup_layout);
		mLlRestore = (LinearLayout) findViewById(R.id.home_restore_layout);
		mIvSync = (ImageView) findViewById(R.id.home_sync_image);

		mLlBackup.setOnClickListener(this);
		mLlRestore.setOnClickListener(this);
		mIvSync.setOnClickListener(this);



        Auth.startOAuth2Authentication(MainActivity.this, getString(R.string.app_key));

		
	}
	
	@Override
	protected void onResume() {

		super.onResume();

		if (hasToken()) {
			File file = new File(ZipZapProvider.BASE_FILE_PATH
								+ ZipZapProvider.BASE_FILE_NAME + "1");//ZipZapProvider.APP_KEY);
			
			if(!file.exists())
				writeDefaultFile();
        }

	}

	@Override
	protected void loadData() {

	}

	private void writeDefaultFile() {
		
		String filePath = ZipZapProvider.BASE_FILE_PATH
						+ ZipZapProvider.BASE_FILE_NAME + "1"; // ZipZapProvider.APP_KEY;

		 DropBoxStorage dStorage = DropBoxStorage.getInstance(this);
		 dStorage.download(filePath, this.getContentResolver());
		
		filePath = ZipZapProvider.BASE_FILE_PATH
				+ ZipZapProvider.BASE_FILE_NAME + "2"; // ZipZapProvider.APP_KEY;

		 dStorage.download(filePath, this.getContentResolver());
		
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.home_backup_layout:
			Intent backupIntent = new Intent(MainActivity.this, BackupActivity.class);
			startActivity(backupIntent);
			Toast.makeText(MainActivity.this,"Todo:: Backup", Toast.LENGTH_SHORT).show();
			break;
			
		case R.id.home_restore_layout:
			Intent restoreIntent = new Intent(MainActivity.this, RestoreActivity.class);
			startActivity(restoreIntent);
			Toast.makeText(MainActivity.this,"Todo:: Restore", Toast.LENGTH_SHORT).show();			
			break;

		case R.id.home_sync_image:
			syncDataAndApp();
			break;

		default:
			break;
		}

	}

	private void syncDataAndApp() {
		// Intent backupIntent = new Intent(MainActivity.this, BackupActivity.class);
		// startActivity(backupIntent);
	}

}