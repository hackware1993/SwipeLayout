package net.lucode.hackware.swipelayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hackware on 2016/8/25.
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SwipeLayout mSwipeLayout;
    private View mHeaderView;
    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeLayout = (SwipeLayout) findViewById(R.id.pull_layout);
        mContentView = findViewById(R.id.content_view);
        mHeaderView = findViewById(R.id.header_view);

        mSwipeLayout.setOnSwipeListener(new SwipeLayout.OnSwipeListener() {

            @Override
            public void onSwipe(float percent) {
                mContentView.setTranslationY(mHeaderView.getMeasuredHeight() * percent);
            }

            @Override
            public void onHeaderOpen() {
                Log.d(TAG, "onHeaderOpen()");
            }

            @Override
            public void onHeaderClose() {
                Log.d(TAG, "onHeaderClose()");
            }
        });

        mHeaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeLayout.closeHeader();
            }
        });

        List<String> mStr = new ArrayList<String>();
        for (int i = 0; i < 100; i++) {
            mStr.add("" + i);
        }

        ListView listView = (ListView) mContentView;
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mStr));

/*        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.openHeader();
                mSwipeLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeLayout.closeHeader();
                    }
                }, 2000);
            }
        }, 3000);*/
    }
}
