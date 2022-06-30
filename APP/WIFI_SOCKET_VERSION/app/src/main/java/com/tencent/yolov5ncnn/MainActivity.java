// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2020 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

package com.tencent.yolov5ncnn;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private Bitmap yourSelectedImage = null;
    private YoloV5Ncnn yolov5ncnn = new YoloV5Ncnn();
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    int getPicLen = 0;
                    Socket socket = new Socket("192.168.43.60", 12345);
                    InputStream inputStream = socket.getInputStream();
                    //OutputStream outputStream = socket.getOutputStream();
                    byte[] picLenBuff = new byte[200];
                    int picLen = inputStream.read(picLenBuff);
                    String picLenString = new String(picLenBuff, 0, picLen);
                    if(isStringInt(picLenString))
                    {
                        getPicLen = Integer.valueOf(picLenString);
                    }
                    else{
                        inputStream.read();
                        inputStream.close();
                        socket.close();
                        continue;
                    }
                    int offset = 0;
                    byte[] bitmapBuff = new byte[getPicLen];
                    while (offset < getPicLen) {
                        int len = inputStream.read(bitmapBuff, offset, getPicLen - offset);
                        offset += len;
                    }
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBuff, 0, offset);

                    yourSelectedImage = bitmap;
                    if (yourSelectedImage == null)
                        return;
                    YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect(yourSelectedImage, true);
                    for (int i = 0; i < objects.length; i++) {
                        if (objects[i].prob > 0.5) {
                            //outputStream.write(("Detect").getBytes());
                            Socket socket2 = new Socket("192.168.43.60", 8001);
                            OutputStream outputStream = socket2.getOutputStream();
                            outputStream.write(("Detect").getBytes());
                            socket2.close();
                        }
                    }
                    inputStream.close();
                    socket.close();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    });


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        boolean ret_init = yolov5ncnn.Init(getAssets());
        if (!ret_init) {
            Log.e("MainActivity", "yolov5ncnn Init failed");
        }

        thread.start();

    }
    private static boolean isStringInt(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }



}
