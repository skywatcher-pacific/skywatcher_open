package com.skywatcher.PanoramaApp;

import java.util.List;

import com.skywatcher.PanoramaApp.ShootScenario.ShootScenarioException;
import com.skywatcher.PanoramaApp.R;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class QuickSelectActivity extends ListActivity {
	public static final int SCENARIO_ADD = 0x1001; // 新增劇本
	public static final int SCENARIO_EDIT = 0x1002;// 編輯既有劇本
	public static final String SCENARIO = "Scenario";

	private Button button_edit, button_add;

	private ScenariosDBAdapter dbAdapter; // Database操作用

	// ListView所處的模式：普通模式、編輯模式
	public enum Mode {
		NORMAL_MODE, EDIT_MODE
	};

	private Mode mode; // 目前ListView所處的模式

	private List<ShootScenario> scenarioList; // 儲存目前畫面上所有的劇本

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quick_select);

		mode = Mode.NORMAL_MODE;

		findViews();
		setListeners();
		setDBAdapter();
	}

	private void findViews() {
		button_edit = (Button) findViewById(R.id.button_edit);
		button_add = (Button) findViewById(R.id.button_add);
	}

	private void setListeners() {
		button_edit.setOnClickListener(editListener);
		button_add.setOnClickListener(addListener);
	}

	// 設定 DB並開啟，若不存在資料庫則放入5個預設的劇本
	private void setDBAdapter() {
		dbAdapter = new ScenariosDBAdapter(this);

		if (dbAdapter.checkDataBase()) { // 已存在資料庫
			dbAdapter.open();
		} else { // 不存在資料庫
			dbAdapter.open();
			setDefaultScenario(); // 給定預設的5組劇本
		}
		fillData();
	}

	// 設定預設的劇本
	private void setDefaultScenario() {
		try {
			ShootScenario ss = new ShootScenario();
			ss.setMinAz(0);
			ss.setMaxAz(120);
			ss.setMinAlt(0);
			ss.setMaxAlt(0);
			ss.setInterlace(false);
			dbAdapter.insert(ss);

			ss = new ShootScenario();
			ss.setMinAz(0);
			ss.setMaxAz(120);
			ss.setMinAlt(0);
			ss.setMaxAlt(30);
			ss.setInterlace(false);
			dbAdapter.insert(ss);

			ss = new ShootScenario();
			ss.setMinAz(0);
			ss.setMaxAz(180);
			ss.setMinAlt(0);
			ss.setMaxAlt(0);
			ss.setInterlace(false);
			dbAdapter.insert(ss);

			ss = new ShootScenario();
			ss.setMinAz(0);
			ss.setMaxAz(180);
			ss.setMinAlt(0);
			ss.setMaxAlt(30);
			ss.setInterlace(false);
			dbAdapter.insert(ss);

			ss = new ShootScenario();
			ss.setMinAz(-180);
			ss.setMaxAz(180);
			ss.setMinAlt(-50);
			ss.setMaxAlt(90);
			ss.setInterlace(false);
			dbAdapter.insert(ss);

		} catch (ShootScenarioException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	// 若按下Edit button則切換至有刪除按鈕的Adapter，並顯示為Done button
	// 反之則按下Done button則切換至原Adapter，顯示Edit button
	private OnClickListener editListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (mode) {
			case NORMAL_MODE:
				mode = Mode.EDIT_MODE;
				fillData();
				button_add.setVisibility(View.INVISIBLE);
				button_edit.setText(R.string.button_done);
				break;
			case EDIT_MODE:
				mode = Mode.NORMAL_MODE;
				fillData();
				button_add.setVisibility(View.VISIBLE);
				button_edit.setText(R.string.button_edit);
				break;
			}
		}
	};

	private OnClickListener addListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(QuickSelectActivity.this,
					ScenarioSettingActivity.class);
			startActivityForResult(intent, SCENARIO_ADD);
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent(this, ScenarioSettingActivity.class);
		intent.putExtra(SCENARIO, scenarioList.get(position)); // 將整個劇本傳到下一頁
		startActivityForResult(intent, SCENARIO_EDIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fillData();
	}

	// 從資料庫讀取新data, 重新整理 ListView
	private void fillData() {
		scenarioList = dbAdapter.getAll();

		switch (mode) {
		case NORMAL_MODE:
			String[] data = new String[scenarioList.size()];
			for (int i = 0; i < data.length; i++) {
				data[i] = scenarioList.get(i).toString();
			}
			setListAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, data));
			break;
		case EDIT_MODE:
			setListAdapter(new ListAdapterWithButton<ShootScenario>(this,
					scenarioList));
			break;
		}
	}

	// 刪除 List 中劇本的功能
	protected void deleteScenario(Object object) {
		ShootScenario ss = (ShootScenario) object;
		if (dbAdapter.delete(ss.getRowid()) <= 0) {
			Toast.makeText(this, "Delete error, rowId:" + ss.getRowid(),
					Toast.LENGTH_SHORT).show();
			return;
		}
		fillData();
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}

	// 做為有刪除功能的 Adapter
	public class ListAdapterWithButton<T> extends BaseAdapter {
		private LayoutInflater layoutInflater;
		private List<T> list;

		public ListAdapterWithButton(Context context, List<T> list) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			this.layoutInflater = LayoutInflater.from(context);

			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public T getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return ((ShootScenario) list.get(position)).getRowid();
		}

		class ViewHolder {
			TextView label;
			ImageButton button;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			/*
			 * A ViewHolder keeps references to children views to avoid
			 * unneccessary calls to findViewById() on each row.
			 */
			ViewHolder holder;

			/*
			 * When convertView is not null, we can reuse it directly, there is
			 * no need to reinflate it. We only inflate a new View when the
			 * convertView supplied by ListView is null.
			 */
			if (convertView == null) {
				convertView = layoutInflater.inflate(
						R.layout.quick_select_item_delete, null);
				/*
				 * Creates a ViewHolder and store references to the two children
				 * views we want to bind data to.
				 */
				holder = new ViewHolder();
				holder.label = (TextView) convertView.findViewById(R.id.text);
				holder.button = (ImageButton) convertView
						.findViewById(R.id.button_delete);

				convertView.setTag(holder);
			} else {
				/*
				 * Get the ViewHolder back to get fast access to the TextView
				 * and the ImageView.
				 */
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder.
			holder.label.setText(list.get(position).toString());
			holder.button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					deleteScenario(list.get(position));
				}
			});

			return convertView;
		}
	}
}
