/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.camera;

import com.google.common.collect.Lists;

import org.ros.RosCore;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import org.ros.NodeRunner;
import org.ros.rosjava.android.views.RosCameraPreviewView;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends Activity {

  private final NodeRunner nodeRunner;

  private RosCameraPreviewView preview;
  private int cameraId;

  public MainActivity() {
    nodeRunner = NodeRunner.createDefault();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    preview = new RosCameraPreviewView(this);
    setContentView(preview);
    try {
      RosCore rosCore = new RosCore(11311);
      nodeRunner.run(rosCore, Lists.newArrayList("RosCore", "__master:=foo"));
      rosCore.awaitStart();
      // TODO(damonkohler): The master needs to be set via some sort of
      // NodeConfiguration builder.
      nodeRunner.run(preview,
          Lists.newArrayList("Camera", "__ip:=192.168.1.112", "__master:=http://192.168.1.112:11311/"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    cameraId = 0;
    preview.setCamera(Camera.open(cameraId));
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP) {
      int numberOfCameras = Camera.getNumberOfCameras();
      final Toast toast;
      if (numberOfCameras > 1) {
        cameraId = (cameraId + 1) % numberOfCameras;
        preview.releaseCamera();
        preview.setCamera(Camera.open(cameraId));
        toast = Toast.makeText(this, "Switching cameras.", Toast.LENGTH_SHORT);
      } else {
        toast = Toast.makeText(this, "No alternative cameras to switch to.", Toast.LENGTH_SHORT);
      }
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          toast.show();
        }
      });
    }
    return true;
  }

  @Override
  protected void onPause() {
    super.onPause();
    preview.releaseCamera();
  }

}