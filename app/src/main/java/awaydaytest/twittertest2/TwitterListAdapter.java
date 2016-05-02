package awaydaytest.twittertest2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by AwayDay on 2015-12-28.
 */
public class TwitterListAdapter extends BaseAdapter
{
    private Handler hdr;

    private ArrayList<Status> twitList;

    private SparseArray<WeakReference<View>> viewArray;

    // 생성자
    public TwitterListAdapter(Handler inHdr)
    {
        // 어레이 리스트를 생성한다, 끝.
        twitList = new ArrayList<Status>();
        hdr = inHdr;
        this.viewArray = new SparseArray<WeakReference<View>>();
    }

    // 현재 아이템의 수를 리턴
    @Override
    public int getCount()
    {
        return twitList.size();
    }

    // 현재 아이템의 오브젝트를 리턴, Object를 상황에 맞게 변경하거나 리턴받은 오브젝트를 캐스팅해서 사용
    @Override
    public Object getItem(int position)
    {
        return twitList.get(position);
    }

    // 아이템 position의 ID 값 리턴
    @Override
    public long getItemId(int position) {
        return position;
    }

//    // 출력 될 아이템 관리
//    @Override
//    public View getView(final int position, View convertView, ViewGroup parent)
//    {
//        final int pos = position;
//        final Context context = parent.getContext();
//        CustomHolder holder  = null;
//
//        /*
//        TextView id    = null;
//        TextView name    = null;
//        TextView twit    = null;
//        TextView rt = null;
//        TextView fav = null;
//        ImageView img = null;
//        */
//
//        // 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 converView가 null인 상태로 들어 옴
//        if ( convertView == null )
//        {
//            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = inflater.inflate(R.layout.user_twit_item, parent, false);
//
//            /*
//            // 레이아웃에 등록
//            id = (TextView)convertView.findViewById(R.id.id);
//            name = (TextView)convertView.findViewById(R.id.name);
//            twit = (TextView)convertView.findViewById(R.id.twit);
//            rt = (TextView)convertView.findViewById(R.id.rt_view);
//            fav = (TextView)convertView.findViewById(R.id.fav_view);
//            img = (ImageView)convertView.findViewById(R.id.twit_imageView);
//            */
//
//            /*
//            // 홀더 생성 및 Tag로 등록
//            holder = new CustomHolder();
//            holder.userID = id;
//            holder.userName = name;
//            holder.userTwit = twit;
//            holder.userRT = rt;
//            holder.userFav = fav;
//            holder.userImg = img;
//            convertView.setTag(holder);
//            */
//            holder = new CustomHolder();
//            holder.userID = (TextView)convertView.findViewById(R.id.id);
//            holder.userName = (TextView)convertView.findViewById(R.id.name);
//            holder.userTwit = (TextView)convertView.findViewById(R.id.twit);
//            holder.userRT = (TextView)convertView.findViewById(R.id.rt_view);
//            holder.userFav = (TextView)convertView.findViewById(R.id.fav_view);
//            holder.userImg = (ImageView)convertView.findViewById(R.id.twit_imageView);
//            convertView.setTag(holder);
//        }
//        else
//        {
//            holder = (CustomHolder) convertView.getTag();
//            /*
//            id = holder.userID;
//            name = holder.userName;
//            twit = holder.userTwit;
//            rt = holder.userRT;
//            fav = holder.userFav;
//            img = holder.userImg;
//            */
//        }
//
//        convertView.setTag(holder);
//
//        //final Status st = getItem(position);
//
//        holder.userID.setText("@" + twitList.get(position).getUser().getScreenName());
//        holder.userName.setText(twitList.get(position).getUser().getName());
//        holder.userTwit.setText(twitList.get(position).getText());
//        if(twitList.get(position).isRetweetedByMe())
//        {
//            holder.userRT.setTextColor(0xFF52E252);
//        }
//        else
//        {
//            holder.userRT.setTextColor(0xFF000000);
//        }
//        if(twitList.get(position).isFavorited())
//        {
//            holder.userFav.setTextColor(0xFFCD1039);
//        }
//        else
//        {
//            holder.userFav.setTextColor(0xFF000000);
//        }
//        holder.userRT.setText(twitList.get(position).getRetweetCount() + "RT");
//        holder.userFav.setText(twitList.get(position).getFavoriteCount() + "Fav");
//        if(twitList.get(position).getMediaEntities().length == 0)
//        {
//            holder.userImg.setVisibility(View.GONE);
//        }
//        else
//        {
//            holder.userImg.setVisibility(View.VISIBLE);
//            for(MediaEntity mediaEntity  : twitList.get(position).getMediaEntities())
//            {
//                System.out.println(mediaEntity.getType() + ": " + mediaEntity.getMediaURL());
//                new GetMediaTask(mediaEntity.getMediaURL(), holder.userImg).execute();
//            }
//        }
//
//        /*
//        // Text 등록
//        id.setText("@" + twitList.get(position).getUser().getScreenName());
//        name.setText(twitList.get(position).getUser().getName());
//        twit.setText(twitList.get(position).getText());
//        if(twitList.get(position).isRetweetedByMe())
//        {
//            rt.setTextColor(0xFF52E252);
//        }
//        else
//        {
//            rt.setTextColor(0xFF000000);
//        }
//        if(twitList.get(position).isFavorited())
//        {
//            fav.setTextColor(0xFFCD1039);
//        }
//        else
//        {
//            fav.setTextColor(0xFF000000);
//        }
//        rt.setText(twitList.get(position).getRetweetCount() + "RT");
//        fav.setText(twitList.get(position).getFavoriteCount() + "Fav");
//        if(twitList.get(position).getMediaEntities().length == 0)
//        {
//            img.setVisibility(View.GONE);
//        }
//        else
//        {
//            img.setVisibility(View.VISIBLE);
//            for(MediaEntity mediaEntity  : twitList.get(position).getMediaEntities())
//            {
//                System.out.println(mediaEntity.getType() + ": " + mediaEntity.getMediaURL());
//                new GetMediaTask(mediaEntity.getMediaURL(), img).execute();
//            }
//        }
//        */
//
//
//        // 리스트 아이템을 터치 했을 때 이벤트 발생
//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("TLA", "" + twitList.get(pos).getId());
//                //hdr.sendMessage(hdr.obtainMessage(3, twitList.get(pos).getId()));
//                hdr.sendMessage(hdr.obtainMessage(3, twitList.get(pos)));
//            }
//        });
//
//        // 리스트 아이템을 길게 터치 했을 떄 이벤트 발생
//        convertView.setOnLongClickListener(new View.OnLongClickListener()
//                                           {
//                                               @Override
//                                               public boolean onLongClick(View v) {
//                                                   //Toast.makeText(context, "리스트 롱 클릭 : "+m_List.get(pos), Toast.LENGTH_SHORT).show();
//                                                   return true;
//                                               }
//                                           }
//        );
//
//        return convertView;
//    }

    @Override
    public View getView(int position, View cView, ViewGroup pView) {
        final int pos = position;
        final Context context = pView.getContext();

        if(viewArray != null && viewArray.get(position) != null) {
            cView = viewArray.get(position).get();
            if(cView != null)
                return cView;
        }

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //cView = inflater.inflate(R.layout.user_twit_item, pView, false);
            cView = inflater.inflate(R.layout.user_twit_item, null);

            TextView id = (TextView)cView.findViewById(R.id.id);
            TextView name = (TextView)cView.findViewById(R.id.name);
            TextView twit = (TextView)cView.findViewById(R.id.twit);
            TextView rt = (TextView)cView.findViewById(R.id.rt_view);
            TextView fav = (TextView)cView.findViewById(R.id.fav_view);
            ImageView img = (ImageView)cView.findViewById(R.id.twit_imageView);

            id.setText("@" + twitList.get(position).getUser().getScreenName());
            name.setText(twitList.get(position).getUser().getName());
            twit.setText(twitList.get(position).getText());
            if(twitList.get(position).isRetweetedByMe())
            {
                rt.setTextColor(0xFF52E252);
            }
            else
            {
                rt.setTextColor(0xFF000000);
            }
            if(twitList.get(position).isFavorited())
            {
                fav.setTextColor(0xFFCD1039);
            }
            else
            {
                fav.setTextColor(0xFF000000);
            }
            rt.setText(twitList.get(position).getRetweetCount() + "RT");
            fav.setText(twitList.get(position).getFavoriteCount() + "Fav");
            if(twitList.get(position).getMediaEntities().length == 0)
            {
                img.setVisibility(View.GONE);
            }
            else
            {
                img.setVisibility(View.VISIBLE);
                for(MediaEntity mediaEntity  : twitList.get(position).getMediaEntities())
                {
                    System.out.println(mediaEntity.getType() + ": " + mediaEntity.getMediaURL());
                    new GetMediaTask(mediaEntity.getMediaURL(), img).execute();
                }
            }

        } finally {
            viewArray.put(position, new WeakReference<View>(cView));
        }

        return cView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void update() {
        viewArray.clear();
        notifyDataSetChanged();
    }

    // 외부로 밀려나도 나는 이것을 저장한다.
    private class CustomHolder
    {
        TextView userID;
        TextView userName;
        TextView userTwit;
        TextView userRT;
        TextView userFav;
        ImageView userImg;
    }

    // 외부에서 아이템 추가 요청 시 사용
    public void add(Status st)
    {
        twitList.add(st);
    }

    // 외부에서 아이템 삭제 요청 시 사용
    public void remove(int _position) {
        twitList.remove(_position);
    }

    public void removeAllObject()
    {
        int endPoint = twitList.size();

        for(int i = endPoint; i > 0; i--)
        {
            twitList.remove(i-1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            viewArray.removeAtRange(0,viewArray.size());
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            for(int i = viewArray.size()-1; i >= 0; i--)
            {
                // 이건 과연 될까
                viewArray.removeAt(i);
            }
        }
        else
        {
            // 솔직히 이게 될지 안될지 모르겠다.
            viewArray.clear();
        }
    }

    private class GetMediaTask extends AsyncTask<Void, Void, Bitmap>
    {
        private Bitmap bm;
        URL getMediaUrl;
        ImageView iv;

        public GetMediaTask(String in, ImageView inIv)
        {
            try {
                getMediaUrl = new URL(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            iv = inIv;
        }

        @Override
        protected Bitmap doInBackground(Void... params)
        {
            try
            {
                URL url = getMediaUrl;
                URLConnection conn = url.openConnection();
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                //ImageView imgView = (ImageView)findViewById(R.id.imageView);
                //imgView.setImageBitmap(bm);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bm;
        }

        protected void onPostExecute(Bitmap result)
        {
            iv.setImageBitmap(result);
        }
    }
}
