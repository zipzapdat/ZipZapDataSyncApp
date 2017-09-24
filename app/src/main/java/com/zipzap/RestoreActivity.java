package com.zipzap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

public class RestoreActivity extends Activity implements OnClickListener, OnItemSelectedListener {

	private Spinner mSpinnerRestoreLocal;
	private CheckBox mCbGoogleDrive;
	private CheckBox mCbDropBox;
	private CheckBox mCbOneDrive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restore);
		mSpinnerRestoreLocal = (Spinner) findViewById(R.id.rv_spinner_restore_local);
		mCbGoogleDrive = (CheckBox) findViewById(R.id.rv_cb_google);
		mCbDropBox = (CheckBox) findViewById(R.id.rv_cb_dropbox);
		mCbOneDrive = (CheckBox) findViewById(R.id.rv_cb_onedrive);
		mSpinnerRestoreLocal.setOnItemSelectedListener(this);
		mCbGoogleDrive.setOnClickListener(this);
		mCbDropBox.setOnClickListener(this);
		mCbOneDrive.setOnClickListener(this);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

//		switch (parent.getItemAtPosition(pos).toString()) {
//		case "From sdcard0":
//			Toast.makeText(parent.getContext(), "From sdcard0", Toast.LENGTH_SHORT).show();
//			break;
//			
//		case "From sdcard1":
//			Toast.makeText(parent.getContext(), "From sdcard1", Toast.LENGTH_SHORT).show();
//			break;
//
//		case "From hardDisk":
//			Toast.makeText(parent.getContext(), "From hardDisk", Toast.LENGTH_SHORT).show();
//			break;
//
//		default:
//			break;
//		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.rv_cb_google:
			googleRestore(view);
			break;
		case R.id.rv_cb_dropbox:
			dropBoxRestore(view);
			break;

		case R.id.rv_cb_onedrive:
			onedriveStore(view);
			break;

		default:
			break;
		}

	}

	private void onedriveStore(View view) {
		CheckBox checkBox = (CheckBox) view;
		if (checkBox.isChecked()) {
			Toast.makeText(RestoreActivity.this, "one drive checked", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(RestoreActivity.this, "one drive unchecked", Toast.LENGTH_SHORT).show();
		}
	}

	private void dropBoxRestore(View view) {
		CheckBox checkBox = (CheckBox) view;
		if (checkBox.isChecked()) {
			Toast.makeText(RestoreActivity.this, "drop box checked", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(RestoreActivity.this, "drop box unchecked", Toast.LENGTH_SHORT).show();
		}
	}

	private void googleRestore(View view) {
		CheckBox checkBox = (CheckBox) view;
		if (checkBox.isChecked()) {
			Toast.makeText(RestoreActivity.this, "google checked", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(RestoreActivity.this, "google unchecked", Toast.LENGTH_SHORT).show();
		}
	}

}
