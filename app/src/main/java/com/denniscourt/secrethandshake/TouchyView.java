package com.denniscourt.secrethandshake;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SUPER touchy like it's so delicate please don't ever hurt this view it will cry for like a week
 * Created by dennis on 2/7/15.
 */
public class TouchyView extends View {
    private static final String TAG = TouchyView.class.getSimpleName();
    private static final float ERROR_THRESHOLD = 150;
    private MotionEvent lastTouchDownEvent;

    private List<PointF> savedTouches = new ArrayList<>(10);

    private enum Mode { RECORDING, DETECTING };
    private Mode currentMode = Mode.RECORDING;

    public TouchyView(Context context) {
        super(context);
        init();
    }

    public TouchyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        // set up a touch listener
        setOnTouchListener(new MyTouchListener());
    }

    public void startDetecting() {
        currentMode = Mode.DETECTING;
        setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
    }

    public void startRecording() {
        currentMode = Mode.RECORDING;
        setBackgroundColor(getResources().getColor(android.R.color.white));
    }

    public interface TouchPatternListener {
        public void touchPatternReceived(List<PointF> touches);
    }

    private TouchPatternListener touchPatternListener;

    public void setTouchPatternListener(TouchPatternListener touchPatternListener) {
        this.touchPatternListener = touchPatternListener;
    }

    public class MyTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                // stop collecting information about touches

                List<PointF> touches = new ArrayList<>(event.getPointerCount());
                for (int index = 0; index < lastTouchDownEvent.getPointerCount(); index++) {
                    float x = lastTouchDownEvent.getX(index);
                    float y = lastTouchDownEvent.getY(index);

                    PointF location = new PointF(x, y);
                    touches.add(location);
                }
                float minX = touches.get(0).x;
                float minY = touches.get(0).y;
                // convert all of the touches to a coordinate system relative to the top-left
                // corner of the rectangle containing all of the touch points
                for (int i = 1; i < touches.size(); i++) {
                    if (touches.get(i).x < minX) {
                        minX = touches.get(i).x;
                    }
                    if (touches.get(i).y < minY) {
                        minY = touches.get(i).y;
                    }

                }
                for (PointF point : touches){
                    point.x -= minX;
                    point.y -= minY;
                }

                if (touches.size() > 1) {
                    if (currentMode == Mode.DETECTING) {
                        if (detectPattern(touches)) {
                            setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                        } else {
                            setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                        }
                    } else {
                        savedTouches = touches;
                    }
                }
            } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                // keep this touch in the pipeline for when we get an up event
                lastTouchDownEvent = event;
            }

            Log.d(TAG, String.format("action=%d, count=%d", event.getAction(), event.getPointerCount()));

            return true;
        }
    }

    private boolean detectPattern(List<PointF> touches) {
        //access last saved touch down pattern
        //compare realtive x,y locations
        //if yes .. you know the drill etc
        //is there going to be an object that contains the last savd touch pattern
        // or is it going to just be field variables we access?
        //might be cleaner to make an obj, but who cares.
        // I should be using multiline comment.. who cares.
        /* boom, here we go. ok, once we access the pattern and compare we should then make the
        screen light up green or something for "proof of concept" yes?
        what do you think?
        whatels
        e
         */

        if (touches.size() != savedTouches.size()) {
            return false;
        }

        Set<PointF> unvisitedPoints = new HashSet<>(touches);
        float totalError = 0;
        for (PointF touch : touches) {
            PointF closestSaved = null;
            float closestDistance = Float.MAX_VALUE;

            for (PointF savedTouch : savedTouches) {
                float currentDistance = distanceBetween(touch, savedTouch);
                if (currentDistance < closestDistance) {
                    closestSaved = savedTouch;
                    closestDistance = currentDistance;
                }
            }

            totalError += closestDistance;
            unvisitedPoints.remove(closestSaved);
        }

        Log.d(TAG, "total error: " + totalError);

        return totalError < ERROR_THRESHOLD;
    }

    private float distanceBetween(PointF point1, PointF point2) {
        return (float) Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
    }
}
