package com.pavkoo.sample;

import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pavkoo.androidarcmenu.GFloatingMenu;

public class MainActivity extends AppCompatActivity {
    private static final String menu1 = "奋发图强";
    private static final String menu2 = "自强不息";
    private static final String menu3 = "大鹏展翅";
    private static final String menu4 = "恭喜发财";
    private static final String menu5 = "春暖花开";

    private RelativeLayout rlParent;
    private GFloatingMenu menu;
    private GFloatingMenu.OnItemClickListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rlParent = (RelativeLayout) findViewById(R.id.rlParent);
        menu = (GFloatingMenu) findViewById(R.id.flmenu);
        menu.setParentBlurView(rlParent);
        mListener = new GFloatingMenu.OnItemClickListener() {
            @Override
            public void onItemClick(GFloatingMenu.GFloatingMenuItem view) {
                menu.closeMenu();
                if (view.getItemInfo().text.equals(menu1)) {
                    Toast.makeText(MainActivity.this, "加油，加油！", Toast.LENGTH_SHORT).show();
                } else if (view.getItemInfo().text.equals(menu2)) {
                    Toast.makeText(MainActivity.this, "男人就该这样！", Toast.LENGTH_SHORT).show();
                } else if (view.getItemInfo().text.equals(menu3)) {
                    Toast.makeText(MainActivity.this, "一飞冲天吧！", Toast.LENGTH_SHORT).show();
                } else if (view.getItemInfo().text.equals(menu4)) {
                    Toast.makeText(MainActivity.this, "红包拿来！", Toast.LENGTH_SHORT).show();
                } else if (view.getItemInfo().text.equals(menu5)) {
                    Toast.makeText(MainActivity.this, "你的春天在哪里？", Toast.LENGTH_SHORT).show();
                }
            }
        };

        menu.AddMenuItem(((BitmapDrawable) getResources().getDrawable(R.drawable.exit)).getBitmap(), menu1, mListener);
        menu.AddMenuItem(((BitmapDrawable) getResources().getDrawable(R.drawable.openmenu)).getBitmap(), menu2, mListener);
        menu.AddMenuItem(((BitmapDrawable) getResources().getDrawable(R.drawable.expense)).getBitmap(), menu3, mListener);
        menu.AddMenuItem(((BitmapDrawable) getResources().getDrawable(R.drawable.income)).getBitmap(), menu4, mListener);
        menu.AddMenuItem(((BitmapDrawable) getResources().getDrawable(R.drawable.voice)).getBitmap(), menu5, mListener);
    }
}
