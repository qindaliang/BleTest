package com.solux.bletest.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.solux.bletest.R;
import com.solux.bletest.data.MsgReciveData;
import com.solux.bletest.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Create by qindl
 * on 2018/10/17
 */
public class ReciverFragment extends Fragment {
    @BindView(R.id.tv_recive)
    TextView tvRecive;
    Unbinder unbinder;
    @BindView(R.id.btn_clearr)
    Button btnClearr;
    private Activity mActivity;
    private StringBuilder mBuilder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity(); mBuilder = new StringBuilder();
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reciver, null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MsgReciveData event) {
        String msg = event.msg;
        if ("".equals(msg) || msg == null) {
            mBuilder.delete(0, mBuilder.capacity());
            tvRecive.setText("未接收到任何数据！");
        } else {
            String string = mBuilder.append(msg).append("\n\n").toString();
            tvRecive.setText(string);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.btn_clearr)
    public void onViewClicked() {
        mBuilder.delete(0,mBuilder.capacity());
        tvRecive.setText("未接收到任何数据！");
    }
}
