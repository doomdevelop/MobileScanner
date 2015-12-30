package de.bht.bachelor.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.bht.bachelor.R;
import de.bht.bachelor.activities.OcrResultActivity;
import de.bht.bachelor.beans.OcrResult;
import de.bht.bachelor.graphic.transform.ImageProcessing;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by and on 25.01.15.
 */
public class OcrResultImagePageFragment extends android.support.v4.app.Fragment {
    private static final String TAG = OcrResultImagePageFragment.class.getSimpleName();
    public static final String EXTRA_OCR_RESULT_IMAGE = "extra_ocr_result_image";
    private Observable<Bitmap> observable;
    private Subscriber<Bitmap> bitmapSubscriber;
    private Handler backgroundHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final OcrResult ocrResult = ((OcrResultActivity) getActivity()).getOcrResult();

        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        observable =sampleObservable(ocrResult)
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                .observeOn(AndroidSchedulers.mainThread());
        Log.d(TAG, "---------- observable created --------------");
    }

    static class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
        }
    }

    private Observable<Bitmap> sampleObservable(final OcrResult ocrResult) {
        return Observable.defer(new Func0<Observable<Bitmap>>() {
            @Override
            public Observable<Bitmap> call() {
                Log.d(TAG, "sampleObservable create bitmap on thread: " + Thread.currentThread().getName());
                return Observable.just(createBitmap(ocrResult));
            }
        });
    }
    private Bitmap createBitmap(final OcrResult ocrResult) {


        Bitmap bmp = null;
        Log.d(TAG, "start create bitmap on thread: " + Thread.currentThread().getName());
        byte[] data = ocrResult.getImageData();
        if (ocrResult.isLandscape()) {
            int[] rgb = new int[data.length];
            ImageProcessing.decodeYUV420RGB(rgb, data, ocrResult.getW(), ocrResult.getH());
            bmp = Bitmap.createBitmap(rgb, ocrResult.getW(), ocrResult.getH(), Bitmap.Config.ARGB_8888);
        } else {
            bmp = ImageProcessing.convertAndRotate(data, ocrResult.getRotateValue(), ocrResult.getW(), ocrResult.getH());
        }
        return bmp;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ocr_result_image_page_fragment, container, false);
    }

    public static OcrResultImagePageFragment newInstance() {
        OcrResultImagePageFragment ocrResultImagePageFragment = new OcrResultImagePageFragment();
        return ocrResultImagePageFragment;
    }


    @Override
    public void onResume() {
        super.onResume();
        bitmapSubscriber = new Subscriber<Bitmap>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Bitmap bitmap) {
                Log.d(TAG, "set bitmap in UI on thread: " + Thread.currentThread().getName());
                initLayout(bitmap);
            }
        };
        Log.d(TAG, "-------------- subscribe --------------------");
        observable.subscribe(bitmapSubscriber);

    }

    @Override
    public void onPause() {
        super.onPause();
        bitmapSubscriber.unsubscribe();
    }

    private void initLayout(final Bitmap bitmap) {
        ImageView imageView = (ImageView) getView().findViewById(R.id.ocr_result_image);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((OcrResultActivity) getActivity()).showFullScreenDialog(bitmap, 0);
                }
            });
        } else {
            Log.e(getTag(), "BITMAP is NULL");
        }
    }
}
