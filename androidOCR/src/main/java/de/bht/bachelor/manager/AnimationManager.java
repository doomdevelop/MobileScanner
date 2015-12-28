package de.bht.bachelor.manager;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import de.bht.bachelor.R;

/**
 * Created by and on 20.02.15.
 */
public class AnimationManager {
    private Animation zoomAnimation;
    private volatile boolean zoomOnRun = false;
    private static final String TAG = AnimationManager.class.getSimpleName();

    public AnimationManager(Context context){
        zoomAnimation= AnimationUtils.loadAnimation(context, R.anim.zoom);

    }

    public void addAndStartZoomAnimation(final View view){
        Log.d(TAG, "addAndStartZoomAnimation()... ");
        zoomAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                zoomOnRun = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(zoomOnRun) {
                    view.startAnimation(animation);
                }
                }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(zoomAnimation);
    }
    public void stopZoomAnimation(){
        Log.d(TAG, "stopZoomAnimation()... ");
        zoomOnRun  =false;
        zoomAnimation.cancel();
        zoomAnimation.reset();

    }

}
