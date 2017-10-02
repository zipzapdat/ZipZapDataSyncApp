package com.zipzap;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.zipzap.adapter.FragmentAdapter;
import com.zipzap.fragment.BackupFragment;


public class BackupActivity extends AppCompatActivity {

	public static BackupFragment frag_backup;

	private ViewPager viewPager;
	private ActionBar actionBar;
	private Toolbar toolbar;

	private SearchView search;
	private FloatingActionButton fab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	private void onCreateProcess() {
		actionBar   = getSupportActionBar();
		viewPager   = (ViewPager) findViewById(R.id.viewpager);
		fab         = (FloatingActionButton) findViewById(R.id.fab);
		if (viewPager != null) {
			setupViewPager(viewPager);
		}

		initToolbar();

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				frag_backup.refresh(true);
			}
		});

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {

				viewPager.setCurrentItem(tab.getPosition());
				// close contextual action mode
				if(frag_backup.getActionMode() !=  null){
					frag_backup.getActionMode().finish();
				}

				if (tab.getPosition() == 0) {
					fab.show();
				}
				search.onActionViewCollapsed();
				supportInvalidateOptionsMenu();
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
			}

		});


	}

	private void initToolbar(){
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(false);
	}

	private void setupViewPager(ViewPager viewPager) {
		FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());

		if (frag_backup == null) {
			frag_backup = new BackupFragment();
		}
		adapter.addFragment(frag_backup, getString(R.string.tab_title_backup));
		viewPager.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		onCreateProcess();
		super.onResume();
	}


}

