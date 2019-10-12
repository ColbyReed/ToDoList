package com.csce4623.ahnelson.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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

import static com.csce4623.ahnelson.todolist.ToDoProvider.mOpenHelper;
import static java.lang.System.currentTimeMillis;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener {

    boolean titleExists = false;
    String date_time = "";
    int mYear;
    int mMonth;
    int mDay;

    int mHour;
    int mMinute;

    long milTime;

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
        findViewById(R.id.btnCalender).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            //If new Note, call createNewNote()
            case R.id.btnSave:
                checkTitle();
                if(!titleExists) {
                    createNoteContents();
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

    private void calender(){
// Get Current Date
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

                        //*************Call Time Picker Here ********************
                        clock();
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    //Launches time picker dialog for time data
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

                        //Sets formatted time data to textview
                        etDatePicker.setText(dateFormat);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();


    }

    //Checks if title already exists
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

    //Sends note contents to server, and attempts to send broadcast receiver data info for notifications
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
        myCV.put(ToDoProvider.TODO_TABLE_COL_DATE, milTime);
        //Perform the insert function using the ContentProvider
        getContentResolver().insert(ToDoProvider.CONTENT_URI,myCV);
        //Set the projection for the columns to be returned
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT,
                ToDoProvider.TODO_TABLE_COL_DATE};
        //Perform a query to get all rows in the DB
        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,null);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, milTime, broadcast);

        notificationIntent.setAction("com.csce4623.ahnelson.NOTIFICATION_INTENT");
        sendBroadcast(notificationIntent);
    }

}
