package eu.mvanco.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import eu.mvanco.lila.DailyChallengeReceiver;
import eu.mvanco.lila.R;
import eu.mvanco.lila.location.MainActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;


public class PhotoIntentActivity extends Activity {

	private static final int ACTION_TAKE_PHOTO_B = 1;
	private static final int ACTION_GET_LOCATION = 2;
	public static final String START_CAMERA = "startCamera";

	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private static final String HAS_PHOTO_STORAGE_KEY = "hasphotostoragekey";
	private static final String LOCATION_KEY = "location_key";
	private static final String LATITUDE_KEY = "latitude_key";
	private static final String LONGITUDE_KEY = "longitude_key";
	private static final String DESCRIPTION_KEY = "descripion_key";
	private ImageView mImageView;
	private Bitmap mImageBitmap;
	
	private EditText commentView;

	private String mCurrentPhotoPath;

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	
	private String receivedLatitude = "";
	private String receivedLongitude = "";
	private String address = "";
	
	
	public static final String COMMIT_FILE = "commit";

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	
	private Handler handler = null;
	boolean hasPhoto = false;

	
	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}

	
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {
		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		
		return f;
	}
	
	public static int calculateInSampleSize(int width, int height,
            int reqWidth, int reqHeight) {
    // Raw height and width of image

    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        // Calculate ratios of height and width to requested height and width
        final int heightRatio = Math.round((float) height / (float) reqHeight);
        final int widthRatio = Math.round((float) width / (float) reqWidth);

        // Choose the smallest ratio as inSampleSize value, this will guarantee
        // a final image with both dimensions larger than or equal to the
        // requested height and width.
        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
    }

    return inSampleSize;
}

	private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = photoH/targetH;	
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = calculateInSampleSize(3264, 2448, targetW, 350);
		//TODO: change to photoW photoH....
		
		
		//TODO: dont do back to introduction screen aftere ok button
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		
		/* Associate the Bitmap to the ImageView */
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(View.VISIBLE);
		mImageView.invalidate();
	}

	private void galleryAddPic() {
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentPhotoPath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    this.sendBroadcast(mediaScanIntent);
	}

	private void dispatchTakePictureIntent(int actionCode) {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		File f = null;
		
		try {
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("photoPath", mCurrentPhotoPath).commit();
		
			
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		} catch (IOException e) {
			e.printStackTrace();
			f = null;
			mCurrentPhotoPath = null;
		}

		startActivityForResult(takePictureIntent, actionCode);
	}


	private void handleBigCameraPhoto() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mCurrentPhotoPath = prefs.getString("photoPath", "");
		if (!mCurrentPhotoPath.isEmpty()) {
			setPic();
		}

	}


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_camera);
		//TODO: add showing of location as well, saving it and thoughts after OK button

		mImageView = (ImageView) findViewById(R.id.imageView1);
		mImageBitmap = null;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		}
		else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
		
		handler = new Handler();
		
		
		Button buttonOK = (Button) findViewById(R.id.button_ok);
		commentView = (EditText) findViewById(R.id.comment_view);
		buttonOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String path = PhotoIntentActivity.this.getFilesDir().getAbsolutePath();
				FileOutputStream fos = null;
				try {
					fos = openFileOutput(COMMIT_FILE, Context.MODE_APPEND);
					path = fos.getFD().toString();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				StringBuilder sb = new StringBuilder();
				sb.append(mCurrentPhotoPath);
				sb.append("{");
				sb.append(commentView.getText().toString());
				sb.append("{");
				
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String currentDateandTime = sdf.format(cal.getTime());
				sb.append(currentDateandTime);
				sb.append('{');
				
				sb.append(receivedLatitude);
				sb.append("{");
				sb.append(receivedLongitude);
				sb.append("{");
				sb.append(address);
				sb.append(";");
				

				
				try {
					fos.write(sb.toString().getBytes());
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				finish();
				
			}
			
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if ( !hasPhoto				
				&& isIntentAvailable(this, MediaStore.ACTION_IMAGE_CAPTURE) ) {
			
			handler.postDelayed(new Runnable() {
				  @Override
				  public void run() {
					  dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
				  }
				}, 10);
		} 

	}
	
	@Override
	public void onPause() {
		super.onPause();
		
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTION_TAKE_PHOTO_B: {
			if (resultCode == RESULT_OK) {
				hasPhoto = true;
				Intent in = new Intent(this, DailyChallengeReceiver.class);
				in.putExtra(DailyChallengeReceiver.FINISH_DAILY_CHALLENGE, true);
				this.sendBroadcast(in);
				if (mImageBitmap == null)
					handleBigCameraPhoto();
				
				
				Intent inte = new Intent(PhotoIntentActivity.this, MainActivity.class);
				//inte.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				PhotoIntentActivity.this.startActivityForResult(inte, ACTION_GET_LOCATION);
				/*
		        Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					  @Override
					  public void run() {

					  }
					}, 10); */
				

			}
			break;
		} // ACTION_TAKE_PHOTO_B
		case ACTION_GET_LOCATION: {
			address = data.getStringExtra(MainActivity.LOCATION);
			receivedLatitude = data.getStringExtra(MainActivity.LATITUDE);
			receivedLongitude = data.getStringExtra(MainActivity.LONGITUDE);
			
			if (address.isEmpty()) {
				//Toast.makeText(this, "You don't have GPS support...", Toast.LENGTH_LONG).show();
				address = "x";
			}
			else {
				//Toast.makeText(this, "Your current address is " + address, Toast.LENGTH_LONG).show();
				receivedLatitude = data.getStringExtra(MainActivity.LATITUDE);
				receivedLongitude = data.getStringExtra(MainActivity.LONGITUDE);
			}
			
			Toast.makeText(this, "GPS service has finished.", Toast.LENGTH_LONG).show();
			
			break;
		}
		} // switch
	}

	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mImageBitmap == null)
			handleBigCameraPhoto();

		
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		outState.putBoolean(HAS_PHOTO_STORAGE_KEY, hasPhoto);
		
		
		
		outState.putString(LOCATION_KEY, address);
		outState.putString(LONGITUDE_KEY, receivedLongitude);
		outState.putString(LATITUDE_KEY, receivedLatitude);
		
		
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
		

		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(
				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);
		
		hasPhoto = savedInstanceState.getBoolean(HAS_PHOTO_STORAGE_KEY);
		address = savedInstanceState.getString(LOCATION_KEY);
		receivedLongitude = savedInstanceState.getString(LONGITUDE_KEY);
		receivedLatitude = savedInstanceState.getString(LATITUDE_KEY);
	}

	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}


}