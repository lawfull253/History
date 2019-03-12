package sub.kyung.history;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class CalendarView extends LinearLayout {
    private static final int DAYS_COUNT = 42;
    private static final String DATE_FORMAT = "yyyy년 MM월";

    private ImageButton prevMonthButton;
    private ImageButton nextMonthButton;
    private TextView dateDisplay;
    private GridView grid;

    private Database db;

    private Calendar calendar = Calendar.getInstance();
    private EventHandler eventHandler = null;

    private Date curDate;

    public CalendarView(Context context) {
        super(context);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialized(context, attrs);
        assignClickListeners();
        updateCalendar();
    }

    private void initialized(Context context, AttributeSet attrs){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_monthly, this);

        prevMonthButton = findViewById(R.id.monthly_prev_month_button);
        nextMonthButton = findViewById(R.id.monthly_next_month_button);
        dateDisplay = findViewById(R.id.monthly_date_display);
        grid = findViewById(R.id.monthly_calendar_gridview);

        db = new Database(getContext());
    }

    private void assignClickListeners(){
        prevMonthButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, -1);
                eventHandler.onClickMonthButton();
            }
        });

        nextMonthButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, 1);
                eventHandler.onClickMonthButton();
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                eventHandler.onDayClick((Date)parent.getItemAtPosition(position));
            }
        });

        dateDisplay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                eventHandler.onClickDateDisplay();
            }
        });
    }

    public void updateCalendar(){
        updateCalendar(null);
    }

    public void updateCalendar(HashSet<Date> events){
        ArrayList<Date> cells = new ArrayList<>();

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Calendar clone = (Calendar)calendar.clone();
        curDate = clone.getTime();

        int monthBeginningCell = clone.get(Calendar.DAY_OF_WEEK) - 1;
        clone.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        while(cells.size() < DAYS_COUNT){
            cells.add(clone.getTime());
            clone.add(Calendar.DAY_OF_MONTH, 1);
        }

        grid.setAdapter(new CalendarAdapter(getContext(), cells, events));

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        dateDisplay.setText(sdf.format(calendar.getTime()));
    }

    private class CalendarAdapter extends ArrayAdapter<Date> {
        private HashSet<Date> eventDays;
        private LayoutInflater inflater;

        public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> eventDays) {
            super(context, R.layout.layout_monthly_day, days);
            this.eventDays = eventDays;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //이벤트 표시해야할 날짜를 받는 캘린더
            Calendar eventCalendar = Calendar.getInstance();
            //달력에 나타날 날짜를 받는 캘린더
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(getItem(position));

            int year = dateCalendar.get(Calendar.YEAR);
            int month = dateCalendar.get(Calendar.MONTH);
            int day = dateCalendar.get(Calendar.DAY_OF_MONTH);

            Date today = new Date();

            if(convertView == null){
                convertView = inflater.inflate(R.layout.layout_monthly_day, parent, false);
            }
            convertView.setBackgroundResource(0);

            // 특정일 이벤트 표시
//            if(eventDays != null){
//                for(Date eventDate : eventDays){
//                    eventCalendar.setTime(eventDate);
//                    int eventYear = eventCalendar.get(Calendar.YEAR);
//                    int eventMonth = eventCalendar.get(Calendar.MONTH);
//                    int eventDay = eventCalendar.get(Calendar.DAY_OF_MONTH);
//
//                    if(eventYear == year && eventMonth == month && eventDay == day){
//                        //convertView.setBackgroundResource(R.drawable.reminder);
//                        break;
//                    }
//                }
//            }

            //   Log.e("test1", "date :: " + dateCalendar.getTime() + "histories : " + db.selectHistories(dateCalendar.getTime()).getHistoriesIdx());
            if(db.selectHistory(dateCalendar.getTime()).getHistoryIdx() > 0){
                convertView.setBackgroundResource(R.drawable.reminder);
            }

            TextView dayView = (TextView) convertView;
            int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

            dayView.setTypeface(null, Typeface.NORMAL);
            if(dayOfWeek == 1){
                dayView.setTextColor(Color.RED);
            } else if(dayOfWeek == 7){
                dayView.setTextColor(Color.BLUE);
            } else {
                dayView.setTextColor(Color.BLACK);
            }

            dateCalendar.setTime(today);

            if(month != calendar.get(Calendar.MONTH) || year != calendar.get(Calendar.YEAR)){
                dayView.setTextColor(Color.GRAY);
            }
            else if(day == dateCalendar.get(Calendar.DAY_OF_MONTH) && month == dateCalendar.get(Calendar.MONTH) && year == dateCalendar.get(Calendar.YEAR)){
                dayView.setTypeface(null, Typeface.BOLD);
                dayView.setTextColor(Color.CYAN);
            }

            dateCalendar.setTime(getItem(position));
            int curDay = dateCalendar.get(Calendar.DAY_OF_MONTH);


            if(eventDays != null){
                for(Date eventDate : eventDays){
                    eventCalendar.setTime(eventDate);
                    int eventYear = eventCalendar.get(Calendar.YEAR);
                    int eventMonth = eventCalendar.get(Calendar.MONTH);
                    int eventDay = eventCalendar.get(Calendar.DAY_OF_MONTH);

                    if(eventYear == year && eventMonth == month && eventDay == day){
                        dayView.setTextColor(Color.YELLOW);
                        break;
                    }
                }
            }

            dayView.setText(Integer.toString(curDay));

            return convertView;
        }
    }

    public Date getCurrentDate(){
        return this.curDate;
    }

    public void setEventHandler(EventHandler eventHandler){
        this.eventHandler = eventHandler;

    }
    public interface EventHandler{
        void onDayClick(Date date);
        void onClickMonthButton();
        void onClickDateDisplay();
    }
}
