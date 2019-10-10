package com.csce4623.ahnelson.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        initializeComponents();
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
                createNoteContents();
//                HomeActivity hm = new HomeActivity();
//                hm.updateUI();
                finish();
                break;
            //This shouldn't happen
            default:
                break;
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
