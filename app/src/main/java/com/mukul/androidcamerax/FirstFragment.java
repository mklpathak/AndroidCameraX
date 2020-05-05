package com.mukul.androidcamerax;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FirstFragment extends Fragment {

    @BindView(R.id.imageCrop)
    CropImageView imageCrop;
    private File imageFile;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {

        View view = inflater.inflate(R.layout.fragment_first, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        imageFile = (File) getActivity().getIntent().getSerializableExtra("Image");


        imageCrop.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
        imageCrop.rotateImage(90);



        return view;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.crop_menu,menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.save:




                break;
        }



        return true;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    private void saveImage(Bitmap bitmap){



        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
