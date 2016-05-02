package awaydaytest.twittertest2;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.List;

import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class MentionActivity extends AppCompatActivity
{

    private TwitterListAdapter mAdapter;
    private Handler hdr;
    private PullToRefreshListView pullToRefreshView;

    private Twitter twitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mention);

        setTitle("Mention");

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
                }
                else if(msg.what == 3)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MentionActivity.this);
                    builder.setTitle("@"+((Status) msg.obj).getUser().getScreenName());
                    builder.setMessage(((Status) msg.obj).getText());
                    final long stID = ((Status) msg.obj).getId();
                    final String replyTaskName = "@"+((Status) msg.obj).getUser().getScreenName();
                    builder.setPositiveButton("마음", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //new SetRTORFav(1, stID).execute();
                        }
                    });
                    builder.setNeutralButton("멘션", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(MentionActivity.this);

                            alert.setTitle("멘션 작성");

                            // Set an EditText view to get user input
                            final EditText input = new EditText(MentionActivity.this);
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

                                    //new ReplyTask(stID, value).execute();
                                }
                            });

                            alert.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // Canceled.
                                        }
                                    });

                            alert.show();

                        }
                    });
                    builder.setNegativeButton("리트윗", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //new SetRTORFav(0, stID).execute();
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
        pullToRefreshView = (PullToRefreshListView) findViewById(R.id.mentionListView);
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

        twitter = TwitterFactory.getSingleton();
    }

    public void getMentionList()
    {
        List<Status> statuses;

        Paging page = new Paging();
        page.count(40);
        page.setPage(1);

        try
        {
            statuses = twitter.getMentionsTimeline(page);

            for(Status status : statuses)
            {
                mAdapter.add(status);
                //hdr.sendMessage(hdr.obtainMessage(2, 0, 0, ""));
                //mAdapter.notifyDataSetChanged();
                //System.out.println(status.getUser().getScreenName());
                //System.out.println(status.getUser().getName());
                //System.out.println(status.getText());
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
        getMentionList();
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

    private class GetDataTask extends AsyncTask<Void, Void, Boolean>
    {
        //String API;
        @Override
        protected Boolean doInBackground(Void... params) {
            updateTimeline();
            /*
            try {
                //RateLimitStatus rls = twitter.getRateLimitStatus("statuses").get("/statuses/home_timeline");
                //API = rls.getRemaining() + " / " + rls.getLimit() + ", API Reset: " + (rls.getSecondsUntilReset() % 3600) / 60 + "min" + rls.getSecondsUntilReset() % 3600 % 60 + "sec";
                //Log.i("onC", API);

            } catch (TwitterException e) {
                e.printStackTrace();
            }
            */
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            hdr.sendMessage(hdr.obtainMessage(2, 0, 0, ""));
            pullToRefreshView.onRefreshComplete();
            super.onPostExecute(aBoolean);
        }
    }
}
