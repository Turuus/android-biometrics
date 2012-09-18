
package nhatnq.biometrics;

import java.io.File;

import nhatnq.biometrics.face.FaceRecognizer;
import nhatnq.biometrics.util.AppConst;
import nhatnq.biometrics.util.AppUtil;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenRecognizing extends Activity {
	private static final String TAG = ScreenRecognizing.class.getCanonicalName();
	private static final int REQ_CAMERA_CAPTURE = 7;
    private String extraImagePath;
	private Spinner mThresholdSpn;
	private TextView mTvStatus;
	private ImageView mIvFace;
	public static float threshold;
	public static boolean result = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_recognizing);
		
		mThresholdSpn = (Spinner) findViewById(R.id.spinner_threshold);
		mIvFace = (ImageView) findViewById(R.id.ivFace);
		mTvStatus = (TextView) findViewById(R.id.tvStatus);
		
		Button bt;
		bt = (Button) findViewById(R.id.btnCapture);
		bt.setOnClickListener(ClickButtonHandler);
		bt = (Button) findViewById(R.id.btnRecognize);
		bt.setOnClickListener(ClickButtonHandler);
		
		if(savedInstanceState != null){
        	extraImagePath = savedInstanceState.getString("extra_image_path");
        }
	}
	
	protected void onResume() {
		super.onResume();
		
		if(extraImagePath != null){
			if(extraImagePath.endsWith(".pgm")) {
				Toast.makeText(getApplicationContext(), "Pick .pgm image", Toast.LENGTH_LONG).show();
				return;
			}
    		AppUtil.loadBitmapFromPath(extraImagePath, mIvFace, 320, 240);
    	}
	};
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(extraImagePath != null){
			Log.e(TAG, "onSaveInstanceState:extra->"+extraImagePath);
			outState.putString("extra_image_path", extraImagePath);
		}
	}
	
	View.OnClickListener ClickButtonHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnCapture:
				callCameraImageCapturer();
				break;
			case R.id.btnRecognize:
				recognizeObject();
				break;
			}
		}
	};
	
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST, Menu.NONE, "Pick Gallery");
		menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST+1, Menu.NONE, "View gray-scale");
		return true;
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case Menu.FIRST:
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(Intent.createChooser(intent, "Pick via"), 77);
			break;
		case Menu.FIRST + 1:
			intent = new Intent(ScreenRecognizing.this, ScreenGrayscaleDisplay.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 77 && resultCode == RESULT_OK){
			String path = data.getData().getPath();
			extraImagePath = path;
		}
	}
	
	private void callCameraImageCapturer() {
		Log.e(TAG, "SDCard state = "+Environment.getExternalStorageState());
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)){
			Toast.makeText(this, "Please insert SDCard into your phone", Toast.LENGTH_SHORT).show();
			return;
		}
		
		File imageFile = new File(AppConst.FACE_FOLDER, AppUtil.generateFaceNameAtThisTime());

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
		extraImagePath = imageFile.getAbsolutePath();
		
		startActivityForResult(intent, REQ_CAMERA_CAPTURE);
	}

	private void recognizeObject(){
		int pos = mThresholdSpn.getSelectedItemPosition();
		String[] thresholds = getResources().getStringArray(R.array.pref_confidence_threshold);
		threshold = Float.parseFloat(thresholds[pos]);
		
		FaceRecognizer recog = new FaceRecognizer(ScreenRecognizing.this);
		recog.recognizeObjectByFace(extraImagePath);
	}
	
}