package com.autoio.core_sdk.base;

import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by wuhongyun on 17-7-17.
 * 为了方便扩展使之继承了recyclerview的holder
 */

public class BaseHolder extends RecyclerView.ViewHolder {

    private final Unbinder unbinder;

    /**
     * 此处使用butterKnife进行了view绑定操作
     * @param itemView
     */
    public BaseHolder(View itemView) {
        super(itemView);
        unbinder = ButterKnife.bind(this, itemView);

    }

    public void unBind(){
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    public void setListeners(View.OnClickListener onClickListener,@IdRes int ... ids){
        for (int id :ids){
            try {
                View view = itemView.findViewById(id);
                if (view!=null){
                    if (!view.isClickable()){
                        view.setClickable(true);
                    }
                    view.setOnClickListener(onClickListener);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
