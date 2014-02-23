/*******************************************************************************
 * Copyright 2014, barter.li
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package li.barter;

import java.io.IOException;
import java.util.Collection;

import li.barter.utils.AppConstants;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.jwetherell.quick_response_code.DecoderActivityHandler;
import com.jwetherell.quick_response_code.IDecoderActivity;
import com.jwetherell.quick_response_code.ViewfinderView;
import com.jwetherell.quick_response_code.camera.CameraManager;
import com.jwetherell.quick_response_code.result.ResultHandler;
import com.jwetherell.quick_response_code.result.ResultHandlerFactory;

/**
 * @author vinaysshenoy Activity to scan an ISBN barcode
 */
public class ScanIsbnActivity extends AbstractBarterLiActivity implements
		IDecoderActivity, SurfaceHolder.Callback {

	private static final String TAG = "ScanIsbnActivity";

	private DecoderActivityHandler mDecoderActivityHandler;
	private ViewfinderView mViewFinderView;
	private CameraManager mCameraManager;
	private boolean mHasSurface;
	private Collection<BarcodeFormat> mDecodeFormats;
	private String mCharacterSet;

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_book);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mDecoderActivityHandler = null;
		mHasSurface = false;
		mViewFinderView = null;
		mDecodeFormats = null;
		mCharacterSet = null;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_scan_book, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startScanningForBarcode();

	}

	@Override
	protected void onPause() {
		super.onPause();
		stopScanningForBarcode();
	}

	/**
	 * Initialize the camera and start scanning for a barcode
	 */
	private void startScanningForBarcode() {
		// CameraManager must be initialized here, not in onCreate().
		if (mCameraManager == null)
			mCameraManager = new CameraManager(getApplication());

		if (mViewFinderView == null) {
			mViewFinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
			mViewFinderView.setCameraManager(mCameraManager);
		}

		showScanner();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (mHasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}

	}

	/**
	 * Stop scanning for a barcode, and release the camera
	 */
	private void stopScanningForBarcode() {
		if (mDecoderActivityHandler != null) {
			mDecoderActivityHandler.quitSynchronously();
			mDecoderActivityHandler = null;
		}

		mCameraManager.closeDriver();

		if (!mHasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_add_manually) {
			startActivity(new Intent(this, AddOrEditBookActivity.class));
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_FOCUS
				|| keyCode == KeyEvent.KEYCODE_CAMERA) {
			// Handle these events so they don't launch the Camera app
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null)
			Log.e(TAG,
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		if (!mHasSurface) {
			mHasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// Ignore
	}

	@Override
	public ViewfinderView getViewfinder() {
		return mViewFinderView;
	}

	@Override
	public Handler getHandler() {
		return mDecoderActivityHandler;
	}

	@Override
	public CameraManager getCameraManager() {
		return mCameraManager;
	}

	private void showScanner() {
		mViewFinderView.setVisibility(View.VISIBLE);
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			mCameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (mDecoderActivityHandler == null)
				mDecoderActivityHandler = new DecoderActivityHandler(this,
						mDecodeFormats, mCharacterSet, mCameraManager);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
		}
	}

	@Override
	public void handleDecode(Result rawResult, Bitmap barcode) {
		stopScanningForBarcode();
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(
				this, rawResult);
		handleDecodeInternally(rawResult, resultHandler, barcode);
	}

	private void handleDecodeInternally(Result rawResult,
			ResultHandler resultHandler, Bitmap barcode) {

		final String isbnNumber = resultHandler.getDisplayContents().toString(); // Barcode
																					// data
		final String barcodeSymbology = rawResult.getBarcodeFormat().toString(); // Barcode
																					// symbology
		final String type = resultHandler.getType().toString(); // Whether
																// barcode is of
																// type URI or
																// ISBN

		final Intent addBookIntent = new Intent(this,
				AddOrEditBookActivity.class);
		addBookIntent.putExtra(AppConstants.BOOK_ID, isbnNumber);
		startActivity(addBookIntent);

	}

}
