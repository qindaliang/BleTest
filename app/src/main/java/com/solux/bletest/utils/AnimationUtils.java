package com.solux.bletest.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;

/**
 * Create by qindl
 * on 2018/10/17
 */
public class AnimationUtils {

    public static void moveX(View view,int x){
        float curTranslationX = view.getTranslationX();
        // 获得当前按钮的位置
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", curTranslationX, x);
        animator.setDuration(500);
        animator.start();
    }

    public static void moveY(View view,int y){
        float curTranslationX = view.getTranslationX();
        // 获得当前按钮的位置
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", curTranslationX, y);
        animator.setDuration(500);
        animator.start();
    }

    public static void scale(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 3f, 1f);
        animator.setDuration(5000);
        animator.start();
    }
}
