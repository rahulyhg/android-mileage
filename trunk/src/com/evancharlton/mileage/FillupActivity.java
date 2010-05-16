package com.evancharlton.mileage;

import java.util.ArrayList;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Field;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.FillupField;
import com.evancharlton.mileage.dao.FillupSeries;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.dao.Dao.InvalidFieldException;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.Settings;
import com.evancharlton.mileage.provider.tables.FieldsTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.views.CursorSpinner;
import com.evancharlton.mileage.views.DateButton;
import com.evancharlton.mileage.views.FieldView;

public class FillupActivity extends BaseFormActivity {
	private EditText mOdometer;
	private EditText mVolume;
	private EditText mPrice;
	private DateButton mDate;
	private CursorSpinner mVehicles;
	private CheckBox mPartial;
	private LinearLayout mFieldsContainer;
	private final ArrayList<FieldView> mFields = new ArrayList<FieldView>();
	private final Fillup mFillup = new Fillup(new ContentValues());

	private Bundle mIcicle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.fillup);
		// save the icicle so that we can restore the meta fields later on.
		mIcicle = savedInstanceState;
	}

	@Override
	protected void onResume() {
		super.onResume();

		Cursor fields = managedQuery(Uri.withAppendedPath(FillUpsProvider.BASE_URI, FieldsTable.URI), FieldsTable.getFullProjectionArray(), null,
				null, null);
		LayoutInflater inflater = LayoutInflater.from(this);
		mFieldsContainer.removeAllViews();
		while (fields.moveToNext()) {
			String hint = fields.getString(fields.getColumnIndex(Field.TITLE));
			long id = fields.getLong(fields.getColumnIndex(Field._ID));
			View container = inflater.inflate(R.layout.fillup_field, null);
			FieldView field = (FieldView) container.findViewById(R.id.field);
			field.setFieldId(id);
			field.setId((int) id);
			field.setHint(hint);
			mFieldsContainer.addView(container);
			mFields.add(field);

			if (mIcicle != null) {
				String value = mIcicle.getString(field.getKey());
				if (value != null) {
					field.setText(value);
				}
			}
		}
		if (fields.getCount() == 0) {
			mFieldsContainer.setVisibility(View.GONE);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		for (FieldView fieldView : mFields) {
			outState.putString(fieldView.getKey(), fieldView.getText().toString());
		}
	}

	@Override
	protected boolean postSaveValidation() {
		try {
			for (FieldView fieldView : mFields) {
				FillupField field = new FillupField(new ContentValues());
				field.setFillupId(mFillup.getId());
				field.setTemplateId(fieldView.getFieldId());
				field.setValue(fieldView.getText().toString());
				field.save(this);
			}
			return true;
		} catch (InvalidFieldException exception) {
			Toast.makeText(this, getString(exception.getErrorMessage()), Toast.LENGTH_LONG).show();
		}
		return false;
	}

	@Override
	protected void saved() {
		if (getParent() == null) {
			finish();
		} else {
			onCreate(null);
		}
	}

	@Override
	protected Dao getDao() {
		return mFillup;
	}

	@Override
	protected String[] getProjectionArray() {
		return FillupsTable.PROJECTION;
	}

	@Override
	protected Uri getUri(long id) {
		return ContentUris.withAppendedId(FillupsTable.BASE_URI, id);
	}

	@Override
	protected void initUI() {
		mOdometer = (EditText) findViewById(R.id.odometer);
		mVolume = (EditText) findViewById(R.id.volume);
		mPrice = (EditText) findViewById(R.id.price);
		mDate = (DateButton) findViewById(R.id.date);
		mPartial = (CheckBox) findViewById(R.id.partial);
		mFieldsContainer = (LinearLayout) findViewById(R.id.container);
		mVehicles = (CursorSpinner) findViewById(R.id.vehicle);

		setDataFormats();
	}

	@Override
	protected void populateUI() {
		mOdometer.setText(String.valueOf(mFillup.getOdometer()));
		mDate.setDate(mFillup.getTimestamp());
		mPartial.setChecked(mFillup.isPartial());

		setDataFormats();
	}

	private void setDataFormats() {
		// TODO: magic numbers
		int dataFormat = Integer.parseInt(mPreferences.getString(Settings.DATA_FORMAT, "0"));
		boolean existing = mFillup.isExistingObject();
		switch (dataFormat) {
			case 0:
				// unit price, volume
				mVolume.setHint(R.string.unit_count);
				mPrice.setHint(R.string.price_per_unit);
				if (existing) {
					mVolume.setText(String.valueOf(mFillup.getVolume()));
					mPrice.setText(String.valueOf(mFillup.getUnitPrice()));
				}
				break;
			case 1:
				// total cost, volume
				mVolume.setHint(R.string.unit_count);
				mPrice.setHint(R.string.total_cost);
				if (existing) {
					mVolume.setText(String.valueOf(mFillup.getVolume()));
					mPrice.setText(String.valueOf(mFillup.getTotalCost()));
				}
				break;
			case 2:
				// total cost, unit price
				mVolume.setHint(R.string.total_cost);
				mPrice.setHint(R.string.price_per_unit);
				if (existing) {
					mVolume.setText(String.valueOf(mFillup.getTotalCost()));
					mPrice.setText(String.valueOf(mFillup.getUnitPrice()));
				}
				break;
		}
	}

	@Override
	protected void setFields() {
		int dataFormat = Integer.parseInt(mPreferences.getString(Settings.DATA_FORMAT, "0"));
		switch (dataFormat) {
			case 1:
				// total cost, volume
				try {
					mFillup.setVolume(Double.parseDouble(mVolume.getText().toString()));
				} catch (NumberFormatException e) {
					throw new InvalidFieldException(R.string.error_no_volume_specified);
				}

				try {
					mFillup.setTotalCost(Double.parseDouble(mPrice.getText().toString()));
				} catch (NumberFormatException e) {
					throw new InvalidFieldException(R.string.error_no_total_cost_specified);
				}
				break;
			case 2:
				// total cost, unit price
				try {
					mFillup.setTotalCost(Double.parseDouble(mVolume.getText().toString()));
				} catch (NumberFormatException e) {
					throw new InvalidFieldException(R.string.error_no_total_cost_specified);
				}

				try {
					mFillup.setUnitPrice(Double.parseDouble(mPrice.getText().toString()));
				} catch (NumberFormatException e) {
					throw new InvalidFieldException(R.string.error_no_price_specified);
				}
				break;
			default:
			case 0:
				// unit price, volume
				try {
					mFillup.setVolume(Double.parseDouble(mVolume.getText().toString()));
				} catch (NumberFormatException e) {
					throw new InvalidFieldException(R.string.error_no_volume_specified);
				}

				try {
					mFillup.setUnitPrice(Double.parseDouble(mPrice.getText().toString()));
				} catch (NumberFormatException e) {
					throw new InvalidFieldException(R.string.error_no_price_specified);
				}
				break;
		}

		try {
			String odometerText = mOdometer.getText().toString();
			double odometerValue = 0;
			if (odometerText.startsWith("+")) {
				Fillup previous = mFillup.loadPrevious(this);
				odometerValue = previous.getOdometer() + Double.parseDouble(odometerText.substring(1));
			} else {
				odometerValue = Double.parseDouble(odometerText);
			}
			mFillup.setOdometer(odometerValue);
		} catch (NumberFormatException e) {
			throw new InvalidFieldException(R.string.error_no_odometer_specified);
		}

		mFillup.setPartial(mPartial.isChecked());
		mFillup.setVehicleId(mVehicles.getSelectedItemId());

		// update the economy number
		Uri vehicleUri = ContentUris.withAppendedId(VehiclesTable.BASE_URI, mVehicles.getSelectedItemId());

		Vehicle v = null;
		Cursor vehicleCursor = managedQuery(vehicleUri, VehiclesTable.PROJECTION, null, null, null);
		if (vehicleCursor.getCount() == 1) {
			vehicleCursor.moveToFirst();
			v = new Vehicle(vehicleCursor);
			Fillup previous = null;
			if (mFillup.isExistingObject()) {
				previous = mFillup.loadPrevious(this);
			} else {
				previous = v.loadLatestFillup(this);
			}
			if (previous == null) {
				mFillup.setEconomy(0D);
			} else {
				double economy = Calculator.averageEconomy(v, new FillupSeries(previous, mFillup));
				mFillup.setEconomy(economy);
			}
		}
	}

	@Override
	protected int getCreateString() {
		return R.string.add_fillup;
	}
}
