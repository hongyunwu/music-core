package com.autoio.core_sdk.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

/**
 * Created by wuhongyun on 17-7-17.
 */

public abstract class BaseActivity<T extends BaseHolder> extends AppCompatActivity {


    protected Handler mHandler = new Handler(Looper.getMainLooper());
    protected T viewHolder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(this, getLayoutID(), null);
        setContentView(view);
        //EventBus.getDefault().register(this);
        viewHolder = getViewHolder(view);
        /*if (savedInstanceState!=null)
            dealBundle(savedInstanceState);*/
        dealIntent();
        initData();


    }

    /**
     * 处理横竖屏切换
     * @param savedInstanceState
     */
    protected void dealBundle(Bundle savedInstanceState) {

    }

    /**
     * 处理横竖屏切换
     * @param outState
     */
    protected void saveBundle(Bundle outState) {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        dealIntent();
    }

    /**
     * 负责处理新增的intent
     */
    protected  void dealIntent(){};

    /**
     * 通过反射调用构造方法来创建ViewHolder对象
     * @param view
     * @return
     */
    private T generateViewHolder(View view){
        T t = null;
        Class clazz = generateT();
        try {
             t = (T) clazz.getConstructors()[0].newInstance(view);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     * 获取子类中viewHolder的实际类型
     * @return
     */
    private Class generateT(){
        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();//BaseDao<Category>
        return (Class) pt.getActualTypeArguments()[0];
    }

    /**
     * 获取layout的id
     * @return
     */
    public abstract @LayoutRes int getLayoutID();

    /**
     * 获取viewHolder
     * @return T extends BaseHolder，此方法暂时废弃，现已使用反射获取
     */
    public abstract T getViewHolder(View contentView);

    /**
     * 一些全局变量可以存放在此,暂时废弃
     * @return 全局变量holder extends BaseValue
     */
    //public abstract V getContentValues();
    /**
     * 初始化数据，可以做一些网络请求等
     */
    public abstract void initData();


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        dealBundle(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveBundle(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewHolder!=null){
            viewHolder.unBind();
        }
        Log.i("MainActivity","ondestroy...");
        //EventBus.getDefault().unregister(this);
    }
}
