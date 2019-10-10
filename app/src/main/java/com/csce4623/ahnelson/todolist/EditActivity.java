package com.csce4623.ahnelson.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static com.csce4623.ahnelson.todolist.ToDoProvider.TODO_TABLE_COL_ID;
import static com.csce4623.ahnelson.todolist.ToDoProvider.mOpenHelper;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    boolean titleExists = false;
    private static final String TAG = HomeActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        initializeComponents();
        String taskTitle = getIntent().getStringExtra("TASK");
        String taskID = getIntent().getStringExtra("TASK_ID");
        String taskContent = getIntent().getStringExtra("TASK_CONTENT");
        EditText tvNoteTitle = findViewById(R.id.tvNoteTitle);
        tvNoteTitle.setText(taskTitle);
        EditText etNoteContent = findViewById(R.id.etNoteContent);
        etNoteContent.setText(taskContent);
        Log.d(TAG, "TaskID: " + taskID + " " + taskTitle);
    }

    //Set the OnClick Listener for buttons
    void initializeComponents(){
        findViewById(R.id.btnSave).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            //If new Note, call createNewNote()
            case R.id.btnSave:
                checkTitle();
                if(!titleExists) {
                    updateNoteContents();
//                HomeActivity hm = new HomeActivity();
//                hm.updateUI();
                    finish();
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(), "This title already exists!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                }
                break;
            //This shouldn't happen
            default:
                break;
        }
    }

    public void checkTitle() {
        EditText tvNoteTitle = findViewById(R.id.tvNoteTitle);
        String title = tvNoteTitle.getText().toString();
        String taskID = getIntent().getStringExtra("TASK_ID");

        String stringTitle = "";
        String stringID = "";

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor cursor = db.query(ToDoProvider.TABLE_NAME,
                new String[]{ToDoProvider.TODO_TABLE_COL_TITLE, ToDoProvider.TODO_TABLE_COL_ID},
                null, null, null, null, null);
        while (cursor.moveToNext() && !titleExists) {
            int cursorTitle = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_TITLE);
            int cursorID = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_ID);
            stringTitle = cursor.getString(cursorTitle);
            stringID = cursor.getString(cursorID);
            if(stringTitle.equals(title) && !stringID.equals(taskID)){
                titleExists = true;
            }else{
                titleExists = false;
            }

//            cursor.close();
            db.close();
        }
    }

    public void updateNoteContents(){
        EditText tvNoteTitle = findViewById(R.id.tvNoteTitle);
        EditText etNoteContent = findViewById(R.id.etNoteContent);

        String taskID = getIntent().getStringExtra("TASK_ID");
        String title = tvNoteTitle.getText().toString();
        String content = etNoteContent.getText().toString();


        ContentValues values = new ContentValues();
        values.put(ToDoProvider.TODO_TABLE_COL_TITLE, title);
        values.put(ToDoProvider.TODO_TABLE_COL_CONTENT, content);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        int cursor = db.update(ToDoProvider.TABLE_NAME, values, TODO_TABLE_COL_ID + " = ?", new String[]{taskID});
        db.close();
    }
}
