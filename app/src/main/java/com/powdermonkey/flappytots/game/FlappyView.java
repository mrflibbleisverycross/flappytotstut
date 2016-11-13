package com.powdermonkey.flappytots.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.powdermonkey.flappytots.ISprite;
import com.powdermonkey.flappytots.R;
import com.powdermonkey.flappytots.gameold.FPS;
import com.powdermonkey.flappytots.gameold.FlowerPhysics;
import com.powdermonkey.flappytots.gameold.FrameSprite;
import com.powdermonkey.flappytots.gameold.MovingLeft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

/**
 * Game view
 * Created by Peter Davis on 05/10/2016.
 */

public class FlappyView extends SurfaceView implements Runnable {
    private final Paint paint;
    private final SurfaceHolder holder;
    private int surfaceWidth;
    private int surfaceHeight;

    private boolean playing;
    private float framesPerSecond;
    private Thread gameThread;
    private FPS fps;
    private long time, newflower;
    private int score;


    private ArrayList<FlowerPhysics> objects;
    private boolean hit;
    private long nextFrame;
    private Bitmap[] dying = new Bitmap[4];
    private MovingLeft[] floors;
    private FrameSprite floor;
    private boolean ready = false;
    private Foreground foreground;
    private List<DroopFlowerPhysics> flowers = new ArrayList<>();
    private FrameSprite flower;
    private FrameSprite stem;


    public FlappyView(Context context, int res) {
        super(context);
        // Initialize ourHolder and paint objects
        holder = getHolder();
        paint = new Paint();
        final Bitmap floorbm = BitmapFactory.decodeResource(this.getResources(), R.drawable.repeatable_floor_500);
        final Bitmap flowerbm = BitmapFactory.decodeResource(this.getResources(), R.drawable.flower_frames_300);
        final Bitmap stembm = BitmapFactory.decodeResource(this.getResources(), R.drawable.stem);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                surfaceWidth = width;
                surfaceHeight = height;

                flower = new FrameSprite(flowerbm, surfaceWidth / 10, surfaceHeight / 10, 2);
                stem = new FrameSprite(stembm, surfaceWidth / 100, surfaceHeight, 1);
                foreground = new Foreground(floorbm, width, height, 4);
                time = System.currentTimeMillis();
                newflower = time + 1000;
                ready = true;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                ready = false;
            }
        });
    }

    @Override
    public void run() {
        fps = new FPS();
        long count = 0;
        while (playing) {

            // Update the frame
            update();

            draw();

            if(++count%10 == 0) {
                framesPerSecond = fps.fps() * 10;
                count = 0;
            }
        }

    }

    long frame = 0;
    /**
     * Draw the entire screen
     */
    private void draw() {
        // Make sure our drawing surface is valid before using
        if (holder.getSurface().isValid() && ready) {
            // Lock the canvas ready to draw
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.argb(255, 188, 237, 237)); // Draw the background color
            paint.setTextSize(45);
            paint.setDither(true);
            paint.setAntiAlias(true);
            paint.setColor(Color.argb(255, 156, 112, 233));
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawText("SCORE:" + score, 20, 40, paint);

            paint.setAlpha(255);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            foreground.draw(canvas, paint);

            synchronized (flowers) {
                for (DroopFlowerPhysics dfp : flowers) {
                    dfp.draw(canvas, paint);
                }
            }
            // Draw everything to the screen
            holder.unlockCanvasAndPost(canvas);
        }

    }

    /**
     * Runs the game logic updating position and state of all players
     */
    private void update() {
        if (ready) {
            time = System.currentTimeMillis();
            foreground.update(time);

            synchronized (flowers) {
                if (time > newflower) {
                    Point2f px = new Point2f(surfaceWidth + 100, (float) ((Math.random() * surfaceHeight / 2) + (surfaceHeight / 10)));
                    Vector2f vx = new Vector2f(-surfaceWidth / 4, 0);
                    DroopFlowerPhysics f = new DroopFlowerPhysics(flower, stem, px, vx);
                    flowers.add(f);
                    newflower = time + 1000;
                }
            }

            for(Iterator<DroopFlowerPhysics> it = flowers.iterator(); it.hasNext();) {
                DroopFlowerPhysics dfp = it.next();
                dfp.update(time);
                if(dfp.getPoint().x < -dfp.getSize().x / 2) {
                    it.remove();
                }
            }

            frame++;
        }
    }

    /**
     * shutdown game thread.
     */
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }


    /**
     * start the game thread.
     */
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        }

        return true;
    }
}
