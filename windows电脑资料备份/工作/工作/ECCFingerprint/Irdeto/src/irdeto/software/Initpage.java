package irdeto.software;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

public class Initpage extends Activity {

	private Button register;
	private Button run;
	private ArrayAdapter<String> Spinneradapter;
	private Spinner update;
	private CheckBox check;
	private TextView myView;
	private boolean First_set = false;
	private final String[] update_cycle = { "every 3 hours", "every 6 hours",
			"every 8 hours", "every 12 hours" };

	private String file_path = "/data/data/irdeto.software/Time.txt";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Find_Resource();

		Spinneradapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, update_cycle);
		update.setAdapter(Spinneradapter);
		// update.setClickable(false);
		update.setVisibility(android.view.View.GONE);

		update.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					final int position, long id) {
				// TODO Auto-generated method stub
				if (!First_set) {
					First_set = true;
					return;
				}
				new AlertDialog.Builder(Initpage.this)
						.setTitle("hint")
						.setMessage(
								"Updated " + Spinneradapter.getItem(position))
						.setPositiveButton("OK", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								try {
									DataOutputStream out = new DataOutputStream(
											new FileOutputStream(file_path));
									switch (position) {
									case 0:
										out.writeInt(3);
										break;
									case 1:
										out.writeInt(6);
										break;
									case 2:
										out.writeInt(8);
										break;
									case 3:
										out.writeInt(12);
										break;
									}
									out.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

						}).setNegativeButton("Cancel", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}

						}).show();

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}

		});

		check.setOnCheckedChangeListener(CheckBoxClick);

		run.setOnClickListener(runpage);
		register.setOnClickListener(repage);
	}

	private void Find_Resource() {
		run = (Button) findViewById(R.id.Button01);
		register = (Button) findViewById(R.id.register);
		update = (Spinner) findViewById(R.id.myspinner);
		check = (CheckBox) findViewById(R.id.check);
		myView = (TextView) findViewById(R.id.TextView);
		myView.setTextColor(Color.BLACK);
		check.setTextColor(Color.BLACK);
		run.setTextColor(Color.BLACK);
		register.setTextColor(Color.BLACK);
	}

	private CheckBox.OnCheckedChangeListener CheckBoxClick = new CheckBox.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub

			if (check.isChecked()) {
				update.setVisibility(android.view.View.VISIBLE);
			} else {
				First_set = true;
				update.setVisibility(android.view.View.GONE);
			}

		}

	};

	private View.OnClickListener runpage = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent runintent = new Intent();
			runintent.setClass(Initpage.this, Irdeto.class);
			startActivity(runintent);
			// finish();
		}
	};

	private View.OnClickListener repage = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent runintent = new Intent();
			runintent.setClass(Initpage.this, Init_Phone.class);
			// runintent.setDataAndType(Uri.EMPTY,
			// "vnd.android.cursor.item/vnd.google.note");
			startActivity(runintent);
			finish();
		}
	};
}