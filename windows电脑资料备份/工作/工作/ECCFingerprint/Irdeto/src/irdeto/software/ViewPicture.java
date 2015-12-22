package irdeto.software;

import java.util.Vector;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageButton;

//Use a imagebutton to show the picture
public class ViewPicture extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewpic);

		ImageButton view_button = (ImageButton) findViewById(R.id.view_picture);
		try {
			Bitmap bm = getPicFromBytes(Operate_File.pic_decrypt_content, null);
			// bm.
			view_button.setImageBitmap(bm);
			view_button.setClickable(false);
		} catch (Exception e) {
			Show_Hint();
		}
	}

	// create Bitmap from memory byte array
	private Bitmap getPicFromBytes(Vector<byte[]> bitcontent,
			BitmapFactory.Options opts) {
		if (bitcontent != null) {
			int length = 0;
			for (int i = 0; i < bitcontent.size(); i++)
				length += bitcontent.elementAt(i).length;

			byte[] content = new byte[length];

			int lastlength = 0;
			for (int i = 0; i < bitcontent.size(); i++) {
				System.arraycopy(bitcontent.elementAt(i), 0, content,
						lastlength, bitcontent.elementAt(i).length);
				lastlength += bitcontent.elementAt(i).length;
			}
			try {
				if (opts != null) {
					return BitmapFactory.decodeByteArray(content, 0,
							content.length, opts);
				} else {
					return BitmapFactory.decodeByteArray(content, 0,
							content.length);
				}
			} catch (OutOfMemoryError e1) {
				Show_Hint();
			} catch (Exception e) {
				Show_Hint();
			}

		}
		return null;
	}

	private void Show_Hint() {
		new AlertDialog.Builder(ViewPicture.this).setTitle("Failure")
				.setMessage("Too big picture")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// System.exit(0);
					}
				}).show();
	}
}