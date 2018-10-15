package com.solux.bletest.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.solux.bletest.R;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class WaitDialog extends DialogFragment {

    @BindView(R.id.tv_tip)
    TextView tvTip;
    Unbinder unbinder;
    private Context mContext;
    private String msg;
    private AlertDialog mDialog;
    private View mView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        mView = inflater.inflate(R.layout.dialog_wait, null);
        unbinder = ButterKnife.bind(this, mView);
        builder.setView(mView);
        tvTip.setText(msg);
        mDialog = builder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        return mDialog;
    }

    public void setView(@IdRes int id){
        this.mView = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(id, null);
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void show(FragmentManager fragmentManager) {
        this.show(fragmentManager, WaitDialog.class.getSimpleName());
    }

    public void hide() {
        mDialog.hide();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * 0.5), ViewGroup.LayoutParams.WRAP_CONTENT);
            Objects.requireNonNull(getDialog().getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
