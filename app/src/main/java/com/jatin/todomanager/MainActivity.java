package com.jatin.todomanager;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import com.jatin.todomanager.db.TaskTable;
import com.jatin.todomanager.model.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Task> tasks = new ArrayList<>();

    private ListView taskList;


    private SwipeRefreshLayout swipeRefresh;

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskList = (ListView) findViewById(R.id.list_task);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);



        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                deleteFromDb();
                Log.d(TAG, "onRefresh: here bhai");
                swipeRefresh.setRefreshing(false);
                updateUI();
            }
        });


        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_add_task:
                LayoutInflater factory = LayoutInflater.from(this);
                final View view = factory.inflate(R.layout.dialog_layout, null);

                final DatePicker dpDate = (DatePicker) view.findViewById(R.id.dp_date);
                final EditText etTask = (EditText) view.findViewById(R.id.et_task);

                dpDate.setMinDate(System.currentTimeMillis() - 1000);

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new task")
                        .setView(view)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SQLiteDatabase myDb = MyDbOpener.openWritableDatabase(MainActivity.this);
                                ContentValues value = new ContentValues();
                                value.put(TaskTable.Columns.TITLE, etTask.getText().toString());
                                value.put(TaskTable.Columns.DATE, dpDate.getYear() + "-" + (((dpDate.getMonth() + 1) > 10) ? "" : "0") + (dpDate.getMonth() + 1) + "-" + ((dpDate.getDayOfMonth() > 10) ? "" : "0") + dpDate.getDayOfMonth());
                                myDb.insert(TaskTable.TABLE_NAME, null, value);
                                myDb.close();
                                updateUI();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                break;
            case R.id.action_edit_task:
                swipeRefresh.setRefreshing(true);
                Log.d(TAG, "onClick: here bhai");
                deleteFromDb();
                updateUI();
                swipeRefresh.setRefreshing(false);
                break;
        }
        return true;
    }

    public class TaskAdapter extends BaseAdapter {

        class Holder {
            public ImageButton btnEdit;
            public CheckBox checkBox;
            public TextView title;
            public TextView date;
        }

        private ArrayList<Task> mTasks;

        public TaskAdapter(ArrayList<Task> mTasks) {
            this.mTasks = mTasks;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater li = getLayoutInflater();
            Holder holder;

            if (view == null) {
                view = li.inflate(R.layout.list_item, null);
                holder = new Holder();
                holder.title = (TextView) view.findViewById(R.id.tv_title);
                holder.date = (TextView) view.findViewById(R.id.tv_date);
                holder.checkBox = (CheckBox) view.findViewById(R.id.check_box);
                holder.btnEdit = (ImageButton) view.findViewById(R.id.btn_edit);
                view.setTag(holder);
                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckBox cb = (CheckBox) view;

                        ViewGroup viewGroup1 = (ViewGroup) view.getParent();
                        TextView tvTitleDone = (TextView) viewGroup1.findViewById(R.id.tv_title);
                        TextView tvDateDone = (TextView) viewGroup1.findViewById(R.id.tv_date);

                        Task task = (Task) cb.getTag();
                        task.setSelected(cb.isChecked());
                        if(task.isSelected()) {

                            tvTitleDone.setPaintFlags(tvTitleDone.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            tvDateDone.setPaintFlags(tvDateDone.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        } else{
                            tvTitleDone.setPaintFlags(tvTitleDone.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            tvDateDone.setPaintFlags(tvDateDone.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        }
                    }
                });
                holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Task task = (Task) v.getTag();
                        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                        final View view = factory.inflate(R.layout.dialog_layout, null);

                        final DatePicker dpDate = (DatePicker) view.findViewById(R.id.dp_date);
                        final EditText etTask = (EditText) view.findViewById(R.id.et_task);

                        String[] dateSet = task.getDate().split("-");

                        dpDate.setMinDate(System.currentTimeMillis() - 1000);
                        etTask.setText(task.getTitle());
                        etTask.setSelection(task.getTitle().length());
                        dpDate.updateDate(Integer.parseInt(dateSet[0]), Integer.parseInt(dateSet[1]) - 1, Integer.parseInt(dateSet[2]));

                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Edit the task")
                                .setView(view)
                                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        SQLiteDatabase myDb = MyDbOpener.openWritableDatabase(MainActivity.this);
                                        ContentValues value = new ContentValues();
                                        value.put(TaskTable.Columns.TITLE, etTask.getText().toString());
                                        value.put(TaskTable.Columns.DATE, dpDate.getYear() + "-" + (((dpDate.getMonth() + 1) > 10) ? "" : "0") + (dpDate.getMonth() + 1) + "-" + ((dpDate.getDayOfMonth() > 10) ? "" : "0") + dpDate.getDayOfMonth());
                                        myDb.update(TaskTable.TABLE_NAME, value, "ID = ? ", new String[]{String.valueOf(task.getTaskID())});
                                        myDb.close();
                                        updateUI();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create();
                        dialog.show();
                    }
                });
            } else {
                holder = (Holder) view.getTag();
            }
            Task thisTask = (Task) getItem(i);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Log.d(TAG, "getView: " + date);
            holder.title.setText(thisTask.getTitle());
            holder.checkBox.setChecked(thisTask.isSelected());
            holder.date.setText(changeDateFormat(thisTask.getDate()));
            if (thisTask.getDate().compareTo(date) < 0) {
                holder.date.setTextColor(Color.RED);
                holder.title.setTextColor(Color.RED);
            } else {
                holder.title.setTextColor(Color.BLACK);
                holder.date.setTextColor(Color.BLACK);
            }
            holder.checkBox.setTag(thisTask);
            holder.btnEdit.setTag(thisTask);
            return view;
        }

        @Override
        public Object getItem(int i) {
            return mTasks.get(i);
        }

        @Override
        public int getCount() {
            return mTasks.size();
        }
    }

    private void updateUI() {
        SQLiteDatabase myDb = MyDbOpener.openWritableDatabase(this);
        String[] projection = {
                TaskTable.Columns.ID,
                TaskTable.Columns.TITLE,
                TaskTable.Columns.DATE
        };
        Cursor c = myDb.query(
                TaskTable.TABLE_NAME,
                projection,
                null, null, null, null, TaskTable.Columns.DATE + " ASC"
        );
        tasks.clear();
        while (c.moveToNext()) {
            tasks.add(new Task(
                    c.getInt(c.getColumnIndex(TaskTable.Columns.ID)),
                    c.getString(c.getColumnIndex(TaskTable.Columns.TITLE)),
                    c.getString(c.getColumnIndex(TaskTable.Columns.DATE))
            ));
        }
        c.close();
        myDb.close();
        TaskAdapter taskAdapter = new TaskAdapter(tasks);
        taskList.setAdapter(taskAdapter);
        taskAdapter.notifyDataSetChanged();
    }

    private void deleteFromDb() {
        SQLiteDatabase myDb = MyDbOpener.openWritableDatabase(MainActivity.this);
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.isSelected()) {
                myDb.delete(TaskTable.TABLE_NAME, TaskTable.Columns.ID + " = ?", new String[]{String.valueOf(task.getTaskID())});
            }
        }
        myDb.close();
    }

    public String changeDateFormat(String time) {
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }
}