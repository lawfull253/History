package sub.kyung.history;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    private static final int CREATE_MODE = 100;
    private static final int EDIT_MODE = 101;
    private static final int GOOGLE_SIGN_IN = 1000;

    private static final String serverURL = "http://192.168.35.152:8088/historyserver/";

    private LinearLayout emptyLayout;
    private LinearLayout notEmptyLayout;
    private TextView menuTextView;
    private TextView dateTextView;
    private TextView keywordTextView;
    private TextView usernameTextView;
    private TextView accountTextView;
    private EditText searchEditText;
    private ImageButton searchButton;
    private ImageButton createButton;
    private ImageButton deleteButton;
    private ImageButton modifyButton;
    private ImageButton openButton;
    private Button googleSignInButton;
    private Button googleSignOutButton;

    private CalendarView cv;
    private Database db;
    private History history;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private NavigationView navigationView;

    private String googleEmail;
    private FirebaseUser firebaseUser;
    private boolean searchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initialized();
        assignOnClickListener();
        setCardLayout(Calendar.getInstance().getTime());
        setGoogleSignIn();

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            googleSignIn();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

                googleEmail = account.getEmail();
                accountTextView.setText(googleEmail);
                googleSignInButton.setVisibility(View.GONE);
                googleSignOutButton.setVisibility(VISIBLE);
                navigationView.refreshDrawableState();
            }
        }

        if(resultCode == RESULT_OK) {
            cv.updateCalendar();
            setCardLayout(history.getHistoryDate());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchMode == true){
            searchMode = false;
            searchEditText.setVisibility(View.GONE);
            searchEditText.setText("");
            cv.updateCalendar();
        } else {
            super.onBackPressed();
        }


    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(firebaseUser == null){
            Toast.makeText(MainActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (id == R.id.nav_upload) {
            appToServer();
        } else if (id == R.id.nav_download) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

            alertDialog
                    .setMessage("기록을 덮어씁니다.")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            serverToApp();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialog.create();
            alert.show();
        } else if (id == R.id.nav_delete_all){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

            alertDialog
                    .setMessage("모든 기록들이 사라집니다.")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.deleteHistoryAll();
                            cv.updateCalendar();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialog.create();
            alert.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_search){
            if(searchMode == true){
                String key = searchEditText.getText().toString();

                if(key.length() > 0){
                    searchKeyword(key);
                    searchEditText.setVisibility(View.GONE);
                    searchMode = false;
                }
            } else {
                searchMode = true;
                searchEditText.setVisibility(View.VISIBLE);
            }

            return true;
        }

        return false;
    }

    private void initialized(){
        this.emptyLayout = findViewById(R.id.empty_monthly_layout);
        this.notEmptyLayout = findViewById(R.id.not_empty_monthly_layout);
        //this.menuTextView = findViewById(R.id.monthly_menu_textview);
        this.dateTextView = findViewById(R.id.monthly_card_date_textview);
        this.keywordTextView = findViewById(R.id.monthly_keyword_textview);
        accountTextView = navigationView.getHeaderView(0).findViewById(R.id.email_textview);
        searchEditText = findViewById(R.id.search_edittext);
       // this.searchButton = findViewById(R.id.monthly_search_button);
        this.createButton = findViewById(R.id.monthly_create_button);
        this.deleteButton = findViewById(R.id.monthly_delete_button);
        this.modifyButton = findViewById(R.id.monthly_modify_button);
        this.openButton = findViewById(R.id.monthly_open_button);
        this.googleSignInButton = navigationView.getHeaderView(0).findViewById(R.id.google_sign_in_button);
        this.googleSignOutButton = navigationView.getHeaderView(0).findViewById(R.id.google_sign_out_button);

        cv = findViewById(R.id.layout_monthly);
        db = new Database(this);

        cv.updateCalendar();
        cv.setEventHandler(eventHandler);
    }

    private void setGoogleSignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
    }

    private void assignOnClickListener(){

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), HistoryEditActivity.class);
                intent.putExtra("mode", CREATE_MODE);
                intent.putExtra("date", history.getStringDate());

                startActivityForResult(intent, CREATE_MODE);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                alertDialog
                        .setMessage("삭제 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.deleteHistory(history.getHistoryDate());
                                cv.updateCalendar();
                                setCardLayout(history.getHistoryDate());
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog alert = alertDialog.create();
                        alert.show();
            }
        });

        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), HistoryEditActivity.class);
                intent.putExtra("mode", EDIT_MODE);
                intent.putExtra("date", history.getStringDate());
                intent.putExtra("keyword", history.getHistoryKeyword());
                intent.putExtra("content", history.getHistoryContent());

                startActivityForResult(intent, EDIT_MODE);
            }
        });

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), HistoryViewerActivity.class);
                intent.putExtra("date", history.getStringDate());
                intent.putExtra("keyword", history.getHistoryKeyword());
                intent.putExtra("content", history.getHistoryContent());

                startActivity(intent);
            }
        });

        googleSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignOut();
            }
        });

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });
    }

    CalendarView.EventHandler eventHandler = new CalendarView.EventHandler() {
        @Override
        public void onDayClick(Date date) {
            setCardLayout(date);
        }

        @Override
        public void onClickMonthButton() {
            cv.updateCalendar();
        }

        @Override
        public void onClickDateDisplay() {
            cv.updateCalendar();
        }
    };

    private void setCardLayout(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");

        dateTextView.setText(sdf.format(date));
        history = db.selectHistory(date);

        if(history.getHistoryIdx() < 0){
            emptyLayout.setVisibility(VISIBLE);
            notEmptyLayout.setVisibility(View.GONE);
            openButton.setVisibility(View.INVISIBLE);
            openButton.setEnabled(false);
            keywordTextView.setText("아무것도 없어요");
        } else{
            emptyLayout.setVisibility(View.GONE);
            notEmptyLayout.setVisibility(VISIBLE);
            openButton.setVisibility(VISIBLE);
            openButton.setEnabled(true);
            keywordTextView.setText(history.getHistoryKeyword());
        }
    }

    private void searchKeyword(String key){
        HashSet<Date> searchHashSet = db.selectHistoryDaysFromKey(key, cv.getCurrentDate());
        cv.updateCalendar(searchHashSet);
        searchEditText.setText("");
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            firebaseUser = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "인증 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void googleSignIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    private void googleSignOut(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        alertDialog
                .setMessage("로그아웃 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        accountTextView.setText("로그인이 필요합니다.");
                        googleSignInButton.setVisibility(VISIBLE);
                        googleSignOutButton.setVisibility(View.GONE);

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                        }
                                    }
                                });

                        FirebaseAuth.getInstance().signOut();
                        firebaseUser = null;


                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void appToServer(){
        List<History> historyList = db.selectHistoryAll();

        Gson gson = new Gson();
        String historyJson = gson.toJson(historyList);
        try {
            historyJson = URLEncoder.encode(historyJson, "UTF-8");
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }

        String url = serverURL + "insertHistory.json?email="+googleEmail+"&historyJson="+historyJson;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                Type type = new TypeToken<String>() {
                }.getType();

                String message = gson.fromJson(response, type);
                if (message.equals("complete")) {
                    Toast.makeText(MainActivity.this, "업로드 완료", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "업로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(request);

    }

    private void serverToApp(){
        String url = serverURL + "getHistory.json?email="+googleEmail;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<History>>() {
                }.getType();

                List<History> historyList = gson.fromJson(response, type);

                for (int i = 0; i < historyList.size(); i++) {
                    db.insertHistory(historyList.get(i));
                }

                Toast.makeText(MainActivity.this, "다운로드 완료", Toast.LENGTH_SHORT).show();
                cv.updateCalendar();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "다운로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(request);

    }
}
