package com.csce4623.ahnelson.todolist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

import static com.csce4623.ahnelson.todolist.ToDoProvider.TODO_TABLE_COL_ID;
import static com.csce4623.ahnelson.todolist.ToDoProvider.mOpenHelper;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    boolean titleExists = false;
    String date_time = "";
    int mYear;
    int mMonth;
    int mDay;

    int mHour;
    int mMinute;

    long milTime;
    private static final String TAG = HomeActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        initializeComponents();
        String taskTitle = getIntent().getStringExtra("TASK");
        String taskID = getIntent().getStringExtra("TASK_ID");
        String taskContent = getIntent().getStringExtra("TASK_CONTENT");
        Long taskDate = getIntent().getLongExtra("TASK_DATE", 0);
        EditText tvNoteTitle = findViewById(R.id.tvNoteTitle);
        tvNoteTitle.setText(taskTitle);
        EditText etNoteContent = findViewById(R.id.etNoteContent);
        etNoteContent.setText(taskContent);
        TextView etDatePicker = findViewById(R.id.etDatePicker);
        etDatePicker.setText(DateFormat.getInstance().format(taskDate));
        Log.d(TAG, "TaskID: " + taskID + " " + taskTitle);
    }

    //Set the OnClick Listener for buttons
    void initializeComponents(){
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.btnCalender).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            //If new Note, call createNewNote()
            case R.id.btnSave:
                checkTitle();
                if(!titleExists) {
                    updateNoteContents();
                    finish();
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(), "This title already exists!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                }
                break;
            case R.id.btnCalender:
                calender();
                break;
            //This shouldn't happen
            default:
                break;
        }
    }

    //Opens calender dialog
    private void calender(){
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;

                        //Launch Time Picker dialog for specific time
                        clock();
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void clock(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        final TextView etDatePicker = findViewById(R.id.etDatePicker);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(mYear, mMonth, mDay, mHour, mMinute, 0);
                        milTime = calendar.getTimeInMillis();

                        String dateFormat = DateFormat.getInstance().format(milTime);
                        //Sets textview to formatted date and time
                        etDatePicker.setText(dateFormat);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    //Checks if title already exists
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

    //Sends updated note content to database
    public void updateNoteContents(){
        EditText tvNoteTitle = findViewById(R.id.tvNoteTitle);
        EditText etNoteContent = findViewById(R.id.etNoteContent);

        String taskID = getIntent().getStringExtra("TASK_ID");
        String title = tvNoteTitle.getText().toString();
        String content = etNoteContent.getText().toString();
        Long date = milTime;


        ContentValues values = new ContentValues();
        values.put(ToDoProvider.TODO_TABLE_COL_TITLE, title);
        values.put(ToDoProvider.TODO_TABLE_COL_CONTENT, content);
        values.put(ToDoProvider.TODO_TABLE_COL_DATE, date);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        int cursor = db.update(ToDoProvider.TABLE_NAME, values, TODO_TABLE_COL_ID + " = ?", new String[]{taskID});
        db.close();
    }
}
