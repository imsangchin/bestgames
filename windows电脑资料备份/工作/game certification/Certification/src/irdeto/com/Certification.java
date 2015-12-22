package irdeto.com;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Certification extends Activity {
	/** Called when the activity is first created. */
	public native String HandleFunction(byte[] iemi, byte[] pdata, int len,
			byte[] key, byte[] keyiv);

	private TextView OutText;
	private Button ConfirmButton;
	private EditText InputEdit;
	private String Input_str;
private String Cerfication_Key = "A58F1B5B514DAEEAFB60B0B017C3C6F8";
	private String Keyiv = "89BF9BCBDD7F57C44F18C068415D455D";
	// private String imsi;
	private String imei;
	TelephonyManager telephonyManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);

		OutText = (TextView) findViewById(R.id.TextView03);
		ConfirmButton = (Button) findViewById(R.id.Button01);
		InputEdit = (EditText) findViewById(R.id.EditText01);
		imei = telephonyManager.getDeviceId();
		ConfirmButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Input_str = InputEdit.getText().toString();
				byte[] iv = new byte[16];
				for (int i = 0; i < 16; i++) {
					iv[i] = 0x00;
				}

				try {
					if (imei == null)
						imei = "000000000000000";
					if (Input_str == null || iv == null) {
						OutText.setText("ERROR!");
						return;
					}
					// = Cerfication_Key.getBytes();
					byte[] bin_array = HexString_to_byte(Cerfication_Key);
					String result = HandleFunction(imei.getBytes(),
							Input_str.getBytes(), Input_str.length(),
							bin_array, iv);
					OutText.setText(result);

				} catch (Exception ex) {
					OutText.setText("ERROR!");
				}

			}
		});
	}

	// 16 format to byte
	private byte[] HexString_to_byte(String hexstring) {
		byte[] array = new byte[hexstring.length() / 2];

		for (int i = 0; i < hexstring.length() / 2; i++) {
			String temp = hexstring.substring(2 * i, 2 * (i + 1));
			// = Integer.getInteger(hexstring.substring(2*i,2*(i+1)));
			int x = Integer.parseInt(temp, 16);
			array[i] = (byte) (x & 0xff);
		}
		// array[length/2] = '\0';
		return array;
	}

	static {
		System.loadLibrary("Certification");
	}

}
