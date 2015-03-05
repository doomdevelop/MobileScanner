package de.bht.bachelor.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.bht.bachelor.R;
import de.bht.bachelor.activities.OcrResultActivity;

/**
 * Created by and on 25.01.15.
 */
public class OcrResultImagePageFragment extends android.support.v4.app.Fragment {
    public static final String EXTRA_OCR_RESULT_IMAGE = "extra_ocr_result_image";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ocr_result_image_page_fragment, container, false);
    }

    public static OcrResultImagePageFragment newInstance(Bitmap bitmap) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_OCR_RESULT_IMAGE, bitmap);
        OcrResultImagePageFragment ocrResultImagePageFragment = new OcrResultImagePageFragment();
        ocrResultImagePageFragment.setArguments(bundle);
        return ocrResultImagePageFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bitmap bitmap = null;
        if (getArguments() != null && getArguments().containsKey(EXTRA_OCR_RESULT_IMAGE)) {
            bitmap = getArguments().getParcelable(EXTRA_OCR_RESULT_IMAGE);
        }
        initLayout(bitmap);

    }

    private void initLayout(final Bitmap bitmap) {
        ImageView imageView = (ImageView) getView().findViewById(R.id.ocr_result_image);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((OcrResultActivity) getActivity()).showFullScreenDialog(bitmap,0);
                }
            });
        } else {
            Log.e(getTag(), "BITMAP is NULL");
        }


    }
}
