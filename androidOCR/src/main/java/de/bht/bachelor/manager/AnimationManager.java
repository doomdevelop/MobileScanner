package de.bht.bachelor.manager;

import android.content.Context;
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

    public AnimationManager(Context context){
        zoomAnimation= AnimationUtils.loadAnimation(context, R.anim.zoom);

    }

    public void addAndStartAnimation(final View view ){
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
        zoomOnRun  =false;
        zoomAnimation.cancel();

    }

}
