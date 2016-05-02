package awaydaytest.twittertest2;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class TwitterActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    private String accessToken = "2373953304-OK0vFHfPYIOY6MH8GihIB0K6X8MhOm392LIfOu4";
    private String accessTokenSecret = "uwJedZrlNGM1ZbwB3vtHCnv31H63OhsKI12ZlYgOI8P4W";
    private String consumerKey = "TMiuuiaL7FxmJrV2piLWTrcnQ";
    private String consumerSecret = "P0lzTUOhoXdiQZvvNuatPBENh6iqjzpMFeskd00KTy24nz6y7S";

    Toolbar toolbar;

    private TwitterListAdapter mAdapter;
    private Handler hdr;
    private PullToRefreshListView pullToRefreshView;

    private Twitter twitter;
    private AccessToken accesstoken;

    private ImageView profileImgView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(TwitterActivity.this);

                alert.setTitle("트윗 작성");

                // Set an EditText view to get user input
                final EditText input = new EditText(TwitterActivity.this);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();

                        new SendTwitTask(value).execute();
                    }
                });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });

                alert.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        hdr = new Handler()
        {
            public void handleMessage(final android.os.Message msg)
            {
                if(msg.what == 0)
                {
                    Snackbar.make(findViewById(R.id.nav_view), "API Call 횟수를 모두 소진하였습니다.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
                else if(msg.what == 1)
                {
                    Snackbar.make(findViewById(R.id.nav_view), "중복된 트윗입니다.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
                else if(msg.what == 2)
                {
                    mAdapter.notifyDataSetChanged();
                    pullToRefreshView.onRefreshComplete();
                }
                else if(msg.what == 3)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TwitterActivity.this);
                    builder.setTitle("@"+((Status) msg.obj).getUser().getScreenName());
                    builder.setMessage(((Status) msg.obj).getText());
                    final long stID = ((Status) msg.obj).getId();
                    final String replyTaskName = "@"+((Status) msg.obj).getUser().getScreenName();
                    builder.setPositiveButton("마음", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            new SetRTORFav(1, stID).execute();
                        }
                    });
                    builder.setNeutralButton("멘션", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(TwitterActivity.this);

                            alert.setTitle("멘션 작성");

                            // Set an EditText view to get user input
                            final EditText input = new EditText(TwitterActivity.this);
                            input.setText(replyTaskName+" ");

                            alert.setView(input);
                            input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if (hasFocus) {
                                        input.setSelection(input.getText().length());
                                    }
                                }
                            });

                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = input.getText().toString();

                                    new ReplyTask(stID, value).execute();
                                }
                            });

                            alert.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    });

                            alert.show();

                        }
                    });
                    builder.setNegativeButton("리트윗", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            new SetRTORFav(0, stID).execute();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            };
        };

        //////////리스트 부분
        // 어댑터 생성
        mAdapter = new TwitterListAdapter(hdr);

        // Xml에서 추가한 ListView 연결
        pullToRefreshView = (PullToRefreshListView) findViewById(R.id.twitListView);
        pullToRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // Do work to refresh the list here.
                new GetDataTask().execute();
            }
        });

        // ListView에 어댑터 연결
        pullToRefreshView.setAdapter(mAdapter);

        // ListView 아이템 터치 시 이벤트 추가
        pullToRefreshView.setOnItemClickListener(onClickListItem);
        pullToRefreshView.setOnItemLongClickListener(onLongClickListItem);
        ////////////// 리스트 부분

        accesstoken = new AccessToken(accessToken, accessTokenSecret);
        twitter = TwitterFactory.getSingleton();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        twitter.setOAuthAccessToken(accesstoken);

        profileImgView = (ImageView) findViewById(R.id.nav_header_profile_img);
        if(profileImgView == null)
        {
            Log.e("imgView","dead?");
        }
        else
        {
            new GetUserDataTask().execute();
        }
        TextView tv1 = (TextView)findViewById(R.id.nav_header_name);
        if(tv1 == null)
        {
            Log.e("TextView","dead?");
        }
        else
        {
            tv1.setText("이야호");
        }
        TextView tv2 = (TextView)findViewById(R.id.nav_header_id);
        if(tv2 == null)
        {
            Log.e("TextView","dead?");
        }
        else
        {
            tv2.setText("댕댕");
        }

        new StartSequence().execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
            super.onBackPressed();
        }
        finish();
    }

    protected void onDestroy()
    {
        //Log.d("onDestroy","finish");
        finish();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.twitter, menu);
        //menu.add(0, 0, Menu.NONE, "멘션 테스트");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_mytimeline)
        {

        }
        else if(id == R.id.nav_mention)
        {
            Intent mention = new Intent(TwitterActivity.this, MentionActivity.class);
            startActivity(mention);
        }
        else if(id == R.id.nav_fav)
        {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getTwitList()
    {
        List<Status> statuses;

        Paging page = new Paging();
        page.count(40);
        page.setPage(1);

        try
        {
            statuses = twitter.getHomeTimeline(page);

            for(Status status : statuses)
            {
                mAdapter.add(status);
            }
        }
        catch (TwitterException e)
        {
            //Log.e("updateTimeline",e.getErrorMessage());
            if(e.getErrorMessage().equals("Rate limit exceeded"))
            {
                hdr.sendMessage(hdr.obtainMessage(0, 0, 0, ""));
            }
        }
    }

    public void updateTimeline()
    {
        mAdapter.removeAllObject();
        getTwitList();
    }

    public void reply(long inReplyToStatusId, String text/*,  double latitude, double longitude, TwitterFactory factory */) throws TwitterException
    {
        StatusUpdate stat= new StatusUpdate(text);

        stat.setInReplyToStatusId(inReplyToStatusId);

        //GeoLocation location= new GeoLocation(latitude, longitude);
        //stat.setLocation(location);

        twitter.updateStatus(stat);
    }

    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            hdr.sendMessage(hdr.obtainMessage(3, mAdapter.getItem(arg2 - 1)));
        }
    };

    private AdapterView.OnItemLongClickListener onLongClickListItem = new AdapterView.OnItemLongClickListener()
    {
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            Log.e("Yaho", "HoYa");
            return false;
        }
    };

    private class GetUserDataTask extends AsyncTask<Void, Void, Bitmap>
    {
        private Bitmap bm;
        @Override
        protected Bitmap doInBackground(Void... params)
        {
            try
            {
                User user = twitter.verifyCredentials();
                Log.i("GetUserDataTask", user.getProfileImageURL());
                URL url = new URL(user.getProfileImageURL());
                URLConnection conn = url.openConnection();
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                //ImageView imgView = (ImageView)findViewById(R.id.imageView);
                //imgView.setImageBitmap(bm);
                Log.i("GetUserDataTask", user.getProfileBannerMobileURL());
            } catch (TwitterException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bm;
        }

        protected void onPostExecute(Bitmap result)
        {
            profileImgView.setImageBitmap(result);
        }
    }

    // 겟 데이터 테스크라는게 아무래도 새로고침 할 때 쓰는 것 같다.
    private class GetDataTask extends AsyncTask<Void, Void, Boolean>
    {
        String API;
        @Override
        protected Boolean doInBackground(Void... params) {
            updateTimeline();
            try {
                RateLimitStatus rls = twitter.getRateLimitStatus("statuses").get("/statuses/home_timeline");
                API = rls.getRemaining() + " / " + rls.getLimit() + ", API Reset: " + (rls.getSecondsUntilReset() % 3600) / 60 + "min" + rls.getSecondsUntilReset() % 3600 % 60 + "sec";
                Log.i("onC", API);

            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            toolbar.setSubtitle(API);
            hdr.sendMessage(hdr.obtainMessage(2, 0, 0, ""));
            //pullToRefreshView.onRefreshComplete();
            super.onPostExecute(aBoolean);
        }
    }

    // 트윗을 던진다.
    private class SendTwitTask extends AsyncTask<Void, Void, Boolean>
    {
        private String value;

        public SendTwitTask(String str)
        {
            value = str;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                twitter.updateStatus(value);
            }
            catch (TwitterException e)
            {
                //Log.e("sendMsg",e.getErrorMessage());
                if(e.getErrorMessage().equals("Status is a duplicate."))
                {
                    hdr.sendMessage(hdr.obtainMessage(1, 0, 0, ""));
                    //Snackbar.make(view, "중복된 트윗입니다.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
            }
            return true;
        }
    }

    // 리트윗 또는 관글을 함.
    private class SetRTORFav extends AsyncTask<Void, Void, Boolean>
    {
        long twitID;
        int mode;
        public SetRTORFav(int order, long ID)
        {
            mode = order;
            twitID = ID;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            if(mode == 0)
            {
                try {
                    twitter.retweetStatus(twitID);
                    Log.i("SetRTORFav","RT");
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }
            else if(mode == 1)
            {
                try {
                    twitter.createFavorite(twitID);
                    Log.i("SetRTORFav", "Fav");
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }
    }

    // 이 비동기 태스크는.... 뭘 할까?
    // 아마 앱 시작시의 동작들 묶음일 것.
    private class StartSequence extends AsyncTask<Void, Void, Boolean>
    {
        String API;
        String name;

        @Override
        protected Boolean doInBackground(Void... params) {
            updateTimeline();
            try {
                RateLimitStatus rls = twitter.getRateLimitStatus("statuses").get("/statuses/home_timeline");
                name = twitter.verifyCredentials().getScreenName();
                API = rls.getRemaining() + " / " + rls.getLimit() + ", API Reset: " + (rls.getSecondsUntilReset() % 3600) / 60 + "min" + rls.getSecondsUntilReset() % 3600 % 60 + "sec";
                Log.i("onC", API);

            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return true;
        }
        protected void onPostExecute(Boolean aBoolean) {
                toolbar.setTitle("@"+name);
                toolbar.setSubtitle(API);
                hdr.sendMessage(hdr.obtainMessage(2, 0, 0, ""));
        }
    }

    // 이 비동기 태스크는 멘션 다는 일을 합니다.
    private class ReplyTask extends AsyncTask<Void, Void, Boolean>
    {
        long id;
        String value;
        public ReplyTask(long inReplyToStatusId, String text)
        {
            id = inReplyToStatusId;
            value = text;
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            try {
                reply(id, value);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
