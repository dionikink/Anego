package com.etiennelawlor.imagegallery.library.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.etiennelawlor.imagegallery.library.R;
import com.etiennelawlor.imagegallery.library.adapters.FullScreenImageGalleryAdapter;
import com.etiennelawlor.imagegallery.library.enums.PaletteColorType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FullScreenImageGalleryActivity extends AppCompatActivity {

    // region Member Variables
    private List<String> mImages;
    private int mPosition;
    private PaletteColorType mPaletteColorType;

    private Toolbar mToolbar;
    private ViewPager mViewPager;

    // endregion

    // region Listeners
    private final ViewPager.OnPageChangeListener mViewPagerOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(position);

                setActionBarTitle(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
    // endregion

    // region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_screen_image_gallery);

        bindViews();

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mImages = extras.getStringArrayList("images");
                mPaletteColorType = (PaletteColorType) extras.get("palette_color_type");
                mPosition = extras.getInt("position");
            }
        }

        setUpViewPager();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;
        int id = item.getItemId();

        if (id == R.id.delete) {
            File image = new File(mImages.get(mViewPager.getCurrentItem()));
            image.delete();
            onBackPressed();
            result = true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            result = true;
        } else {
            result = super.onOptionsItemSelected(item);
        }

        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListeners();
    }
    // endregion


    // region Helper Methods
    private void bindViews() {
        mViewPager = (ViewPager) findViewById(R.id.vp);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void setUpViewPager() {
        ArrayList<String> images = new ArrayList<>();
        images.addAll(mImages);

        FullScreenImageGalleryAdapter fullScreenImageGalleryAdapter = new FullScreenImageGalleryAdapter(images, mPaletteColorType);
        mViewPager.setAdapter(fullScreenImageGalleryAdapter);
        mViewPager.addOnPageChangeListener(mViewPagerOnPageChangeListener);
        mViewPager.setCurrentItem(mPosition);

        setActionBarTitle(mPosition);
    }

    private void setActionBarTitle(int position) {
        if (mViewPager != null && mImages.size() > 1) {
            int totalPages = mViewPager.getAdapter().getCount();

            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(String.format("%d of %d", (position + 1), totalPages));
            }
        }
    }

    private void removeListeners() {
        mViewPager.removeOnPageChangeListener(mViewPagerOnPageChangeListener);
    }
    // endregion
}
