package com.github.maxwell.nc.nativestrencrypt;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.callback.Consumer;
import com.github.maxwell.nc.reactivelib.callback.Function;
import com.github.maxwell.nc.reactivelib.scheduler.Schedulers;

import java.io.InputStream;


public class MainActivity extends Activity {

    private ImageView ivImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivImg = (ImageView) findViewById(R.id.iv_img);
    }

    public void onShow(View view) {
        showImg();
    }

    public void onReset(View view) {
        ivImg.setImageBitmap(null);
    }

    /**
     * 显示图片
     */
    public void showImg() {
        Publisher.just("test.png")
                .map(new Function<String, Bitmap>() {
                    @Override
                    public Bitmap apply(String s) throws Exception {
                        InputStream inputStream = getResources().getAssets().open(s);
                        Bitmap image = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        return image;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.uiThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) {
                        ivImg.setImageBitmap(bitmap);
                    }
                });
    }

}
