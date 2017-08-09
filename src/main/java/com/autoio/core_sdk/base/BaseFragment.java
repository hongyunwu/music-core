package com.autoio.core_sdk.base;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wuhongyun on 17-7-17.
 */

public abstract class BaseFragment<T extends BaseHolder> extends Fragment {

    protected T viewHolder;
    private View contentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //保证多次执行onCreateView时布局view引用都相同
        if (contentView==null){
            contentView = inflater.inflate(getLayoutID(),null);
            //EventBus.getDefault().register(this);
            viewHolder = getViewHolder(contentView);

        }

        //EventBus.getDefault().register(this);
        Log.i("BaseFragment",getClass().getSimpleName()+" onCreateView()...");
        initData();
        return contentView;
    }

    /**
     * 获取layoutID
     * @return 资源id
     */
    public abstract  @LayoutRes int getLayoutID();

    /**
     * ViewHolder中装载有该fragment的view
     * @param contentView 父view
     * @return T 根据子类的泛型参数决定类型
     */
    public abstract T getViewHolder(View contentView);

    /**
     * 进行一些初始化操作
     */
    public abstract void initData();
    @Override
    public void onDestroy() {
        super.onDestroy();
        //不要unbindview
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //EventBus.getDefault().unregister(this);
    }


}
