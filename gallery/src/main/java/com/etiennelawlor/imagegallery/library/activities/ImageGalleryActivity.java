package com.etiennelawlor.imagegallery.library.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.etiennelawlor.imagegallery.library.R;
import com.etiennelawlor.imagegallery.library.adapters.ImageGalleryAdapter;
import com.etiennelawlor.imagegallery.library.enums.PaletteColorType;
import com.etiennelawlor.imagegallery.library.util.ImageGalleryUtils;
import com.etiennelawlor.imagegallery.library.view.GridSpacesItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageGalleryActivity extends AppCompatActivity implements ImageGalleryAdapter.OnImageClickListener {

    // region Member Variables
    private ArrayList<String> mImages;
    private PaletteColorType mPaletteColorType;

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    // endregion

    // region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_gallery);

        bindViews();

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mImages = (ArrayList) getImages();
                mPaletteColorType = (PaletteColorType) extras.get("palette_color_type");
            }
        }

        setUpRecyclerView();
    }
    // endregion

    @Override
    protected void onResume() {
        super.onResume();
        mImages = (ArrayList) getImages();
        setUpRecyclerView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUpRecyclerView();
    }

    // region ImageGalleryAdapter.OnImageClickListener Methods
    @Override
    public void onImageClick(int position) {
        Intent intent = new Intent(ImageGalleryActivity.this, FullScreenImageGalleryActivity.class);

        intent.putStringArrayListExtra("images", mImages);
        intent.putExtra("position", position);
        if (mPaletteColorType != null) {
            intent.putExtra("palette_color_type", mPaletteColorType);
        }

        startActivity(intent);
    }
    // endregion

    // region Helper Methods
    private void bindViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void setUpRecyclerView() {
        int numOfColumns;
        if (ImageGalleryUtils.isInLandscapeMode(this)) {
            numOfColumns = 4;
        } else {
            numOfColumns = 3;
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(ImageGalleryActivity.this, numOfColumns));
        mRecyclerView.addItemDecoration(new GridSpacesItemDecoration(ImageGalleryUtils.dp2px(this, 2), numOfColumns));
        ImageGalleryAdapter imageGalleryAdapter = new ImageGalleryAdapter(mImages);
        imageGalleryAdapter.setOnImageClickListener(this);

        mRecyclerView.setAdapter(imageGalleryAdapter);
    }
    // endregion

    public List<String> getImages() {
        List<String> images = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory().toString() + "/Anego/Screenshots";
        File folder = new File(path);
        File[] files = folder.listFiles();

        for(File file : files) {
            images.add(file.getAbsolutePath());
        }

        return images;
    }
}
