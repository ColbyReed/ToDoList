package com.csce4623.ahnelson.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


import static android.content.ContentValues.TAG;
import static com.csce4623.ahnelson.todolist.ToDoProvider.TODO_TABLE_COL_DATE;
import static com.csce4623.ahnelson.todolist.ToDoProvider.TODO_TABLE_COL_ID;
import static com.csce4623.ahnelson.todolist.ToDoProvider.TODO_TABLE_COL_TITLE;
import static com.csce4623.ahnelson.todolist.ToDoProvider.mOpenHelper;

//Create HomeActivity and implement the OnClick listener
public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    public ListView mTaskListView;
    public ListView mCheckedListView;
    public EditText editDate;
    private ArrayAdapter<String> mAdapter;
    private ArrayAdapter<String> mCheckedAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeComponents();

        //Further intitializing
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        mCheckedListView = (ListView) findViewById(R.id.list_checked);

        editDate = (EditText) findViewById(R.id.etDatePicker);

        CheckBox chxBox = (CheckBox) findViewById(R.id.cbxCompleted);

        //Updates view on start
        updateUI();

    }

    //Set the OnClick Listener for buttons
    void initializeComponents(){
        findViewById(R.id.btnNewNote).setOnClickListener(this);
    }

    //Updates view after other activities finish
    @Override
    protected void onStart(){
        super.onStart();
        updateUI();
        Log.i(TAG, "On Start");
    }


    @Override
    public void onClick(View v){
        switch (v.getId()){
            //If new Note, open NoteActivity()
            case R.id.btnNewNote:
                Intent intent = new Intent(HomeActivity.this, NoteActivity.class);
                startActivity(intent);
                updateUI();
                break;
            //This shouldn't happen
            default:
                break;
        }
    }

    //When check boxed is clicked, checks the state and saves it to database
    public void onCheckboxClicked(View v) {

        View parent = (View) v.getParent();

        TextView taskTextView = (TextView) parent.findViewById(R.id.tvListNoteTitle);
        String task = String.valueOf(taskTextView.getText());
        boolean checked = ((CheckBox) v).isChecked();
        ContentValues values = new ContentValues();

        if (checked) {
            values.put(ToDoProvider.TODO_TABLE_COL_COMPLETED, 1);
        }else {
            values.put(ToDoProvider.TODO_TABLE_COL_COMPLETED, 0);
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        db.update(ToDoProvider.TABLE_NAME, values, TODO_TABLE_COL_TITLE + " = ?", new String[]{task});
        db.close();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                updateUI();
            }
        }, 500);

    }


    //Deletes note from list
    public void btnDoneClick(View v){
        deleteTask(v);
    }

    //Open EditActivity to allow user to update information and save the updates to the database
    public void btnEditClick(View view){

        Intent intent = new Intent(HomeActivity.this, EditActivity.class);
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.tvListNoteTitle);
        String task = String.valueOf(taskTextView.getText());

        String taskID = "";
        String taskContent = "";
        long taskDate = 0;

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor cursor = db.query(ToDoProvider.TABLE_NAME,
                new String[]{ToDoProvider.TODO_TABLE_COL_ID, ToDoProvider.TODO_TABLE_COL_TITLE, ToDoProvider.TODO_TABLE_COL_CONTENT, ToDoProvider.TODO_TABLE_COL_DATE},
                ToDoProvider.TODO_TABLE_COL_TITLE + " = ?", new String[]{task}, null, null, null);
        while(cursor.moveToNext()) {
            int id  = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_ID);
            int title = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_TITLE);
            int content = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_CONTENT);
            int date = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_DATE);
//            Log.d(TAG, "Task: " + cursor.getString(id) + " " + cursor.getString(title));
            taskID = cursor.getString(id);
            taskContent = cursor.getString(content);
            taskDate = cursor.getLong(date);
        }

        cursor.close();

        Log.d(TAG, "TaskID: " + taskID + " " + task);

        //Passes data to EditActivity
        intent.putExtra("TASK", task);
        intent.putExtra("TASK_ID", taskID);
        intent.putExtra("TASK_CONTENT", taskContent);
        intent.putExtra("TASK_DATE", taskDate);

        startActivity(intent);
        updateUI();
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.tvListNoteTitle);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(ToDoProvider.TABLE_NAME,
                ToDoProvider.TODO_TABLE_COL_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();
    }

    //Updates the view
    public void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        ArrayList<String> checkedList = new ArrayList<>();
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(ToDoProvider.TABLE_NAME,
                new String[]{ToDoProvider.TODO_TABLE_COL_ID, ToDoProvider.TODO_TABLE_COL_TITLE, ToDoProvider.TODO_TABLE_COL_CONTENT, ToDoProvider.TODO_TABLE_COL_DATE, ToDoProvider.TODO_TABLE_COL_COMPLETED},
                null, null, null, null, null);
        while(cursor.moveToNext()) {
            int id  = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_ID);
            int title = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_TITLE);
            int content = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_CONTENT);
            int date = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_DATE);
            int completed = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_COMPLETED);
//            Log.d(TAG, "Task: " + cursor.getString(id) + " " + cursor.getString(title));
            Log.d(TAG, "Task: " + cursor.getString(id) + " | " + cursor.getString(title) + " | " + cursor.getString(content) + " | " + cursor.getString(completed) + " | " + cursor.getString(date));

            //Checks if activity is completed, and assigns said activity to corresponding listview
            if(cursor.getInt(completed) == 0) {
                taskList.add(cursor.getString(title));
            }else{
                checkedList.add(cursor.getString(title));
            }
        }

        //Assigns to uncompleted list
        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.list_activity,
                    R.id.tvListNoteTitle,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }

        //Assigns to completed list
        if (mCheckedAdapter == null) {
            mCheckedAdapter = new ArrayAdapter<>(this,
                    R.layout.checked_activity,
                    R.id.tvListNoteTitle,
                    checkedList);
            mCheckedListView.setAdapter(mCheckedAdapter);
        } else {
            mCheckedAdapter.clear();
            mCheckedAdapter.addAll(checkedList);
            mCheckedAdapter.notifyDataSetChanged();
        }


        cursor.close();
        db.close();

    }
}
