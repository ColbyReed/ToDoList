package com.csce4623.ahnelson.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;

import static com.csce4623.ahnelson.todolist.ToDoProvider.mOpenHelper;
import static java.lang.System.currentTimeMillis;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener {

    boolean titleExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        initializeComponents();

        TextView etDatePicker = findViewById(R.id.etDatePicker);
        etDatePicker.setText(DateFormat.getInstance().format(currentTimeMillis()));
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
                    createNoteContents();
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

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor cursor = db.query(ToDoProvider.TABLE_NAME,
                new String[]{ToDoProvider.TODO_TABLE_COL_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int cursorTitle = cursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_TITLE);
            if(cursor.getString(cursorTitle).equals(title)){
                titleExists = true;
            }else{
                titleExists = false;
            }

//            cursor.close();
            db.close();
        }
    }

    public void createNoteContents(){
        //Create a ContentValues object
        ContentValues myCV = new ContentValues();

        EditText tvNoteTitle = findViewById(R.id.tvNoteTitle);
        EditText etNoteContent = findViewById(R.id.etNoteContent);

        String title = tvNoteTitle.getText().toString();
        String content = etNoteContent.getText().toString();

        //Put key_value pairs based on the column names, and the values
        myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, title);
        myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, content);
        //Perform the insert function using the ContentProvider
        getContentResolver().insert(ToDoProvider.CONTENT_URI,myCV);
        //Set the projection for the columns to be returned
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT};
        //Perform a query to get all rows in the DB
        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,null);
    }
}
