/*
 *  Generika Android
 *  Copyright (C) 2018 ywesee GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oddb.generika.ui.reader;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.vision.CameraSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;


public class GraphicOverlay<T extends GraphicOverlay.Graphic> extends View {
  private final Object mLock = new Object();
  private int mPreviewWidth;
  private float mWidthScaleFactor = 1.0f;
  private int mPreviewHeight;
  private float mHeightScaleFactor = 1.0f;
  private int mFacing = CameraSource.CAMERA_FACING_BACK;
  private Set<T> mGraphics = new HashSet<>();

  public static abstract class Graphic {
    private GraphicOverlay mOverlay;

    public Graphic(GraphicOverlay overlay) {
      mOverlay = overlay;
    }

    public abstract void draw(Canvas canvas);

    public float scaleX(float horizontal) {
      return horizontal * mOverlay.mWidthScaleFactor;
    }

    public float scaleY(float vertical) {
      return vertical * mOverlay.mHeightScaleFactor;
    }

    public float translateX(float x) {
      if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
        return mOverlay.getWidth() - scaleX(x);
      } else {
        return scaleX(x);
      }
    }

    public float translateY(float y) {
      return scaleY(y);
    }

    public void postInvalidate() {
      mOverlay.postInvalidate();
    }
  }

  public GraphicOverlay(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void clear() {
    synchronized (mLock) {
      mGraphics.clear();
    }
    postInvalidate();
  }

  public void add(T graphic) {
    synchronized (mLock) {
      mGraphics.add(graphic);
    }
    postInvalidate();
  }

  public void remove(T graphic) {
    synchronized (mLock) {
      mGraphics.remove(graphic);
    }
    postInvalidate();
  }

  public List<T> getGraphics() {
    synchronized (mLock) {
      return new Vector(mGraphics);
    }
  }

  public float getWidthScaleFactor() {
    return mWidthScaleFactor;
  }

  public float getHeightScaleFactor() {
    return mHeightScaleFactor;
  }

  public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
    synchronized (mLock) {
      mPreviewWidth = previewWidth;
      mPreviewHeight = previewHeight;
      mFacing = facing;
    }
    postInvalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    synchronized (mLock) {
      if ((mPreviewWidth != 0) && (mPreviewHeight != 0)) {
        mWidthScaleFactor =
          (float) canvas.getWidth() / (float) mPreviewWidth;
        mHeightScaleFactor =
          (float) canvas.getHeight() / (float) mPreviewHeight;
      }

      for (Graphic graphic : mGraphics) {
        graphic.draw(canvas);
      }
    }
  }
}
