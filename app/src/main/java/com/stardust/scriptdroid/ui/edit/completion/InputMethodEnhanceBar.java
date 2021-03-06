package com.stardust.scriptdroid.ui.edit.completion;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.workground.WrapContentLinearLayoutManager;

import com.google.gson.Gson;
import com.stardust.pio.UncheckedIOException;
import com.stardust.scriptdroid.App;
import com.stardust.scriptdroid.R;
import com.stardust.scriptdroid.autojs.AutoJs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Stardust on 2017/2/17.
 */

public class InputMethodEnhanceBar extends RecyclerView implements CodeCompletion.OnCodeCompletionChangeListener {


    public InputMethodEnhanceBar(Context context) {
        super(context);
        init();
    }

    public InputMethodEnhanceBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InputMethodEnhanceBar(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public interface EditTextBridge {
        void appendText(CharSequence text);

        void backspace(int count);

        com.jecelyin.editor.v2.core.widget.TextView getEditText();
    }

    EditTextBridge mEditTextBridge;
    private CodeCompletion mCodeCompletion = new CodeCompletion(this);
    private List<CodeCompletion.CodeCompletionItem> mCodeCompletionList = new ArrayList<>();
    private final OnClickListener mOnCodeCompletionItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = getChildViewHolder(v).getAdapterPosition();
            if (position >= 0 && position < mCodeCompletionList.size())
                mEditTextBridge.appendText(mCodeCompletionList.get(position).getAppendText());
        }
    };

    private final OnLongClickListener mOnCodeCompletionItemLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int position = getChildViewHolder(v).getAdapterPosition();
            if (position < 0 || position >= mCodeCompletionList.size())
                return false;
            ((ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("", mCodeCompletionList.get(position).getDisplayText()));
            Toast.makeText(getContext(), R.string.text_copied, Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    private void init() {
        setAdapter(new CodeCompletionAdapter());
        setLayoutManager(new WrapContentLinearLayoutManager(getContext(), HORIZONTAL, false));
        mCodeCompletion.setFunctions(readFunctions(getContext(), "js/functions.json"));
    }

    public void setEditTextBridge(EditTextBridge editTextBridge) {
        mEditTextBridge = editTextBridge;
        mCodeCompletion.setEditText(mEditTextBridge.getEditText());
    }

    @Override
    public void OnCodeCompletionChange(Collection<CodeCompletion.CodeCompletionItem>... lists) {
        mCodeCompletionList.clear();
        for (Collection<CodeCompletion.CodeCompletionItem> list : lists) {
            mCodeCompletionList.addAll(list);
        }
        getAdapter().notifyDataSetChanged();

    }


    private class CodeCompletionAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.input_method_enhance_bar_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(mCodeCompletionList.get(position).getDisplayText());
        }

        @Override
        public int getItemCount() {
            return mCodeCompletionList.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(mOnCodeCompletionItemClickListener);
            itemView.setOnLongClickListener(mOnCodeCompletionItemLongClickListener);
        }
    }

    private static String[] readFunctions(Context context, String path) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(new InputStreamReader(context.getAssets().open(path)), String[].class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
