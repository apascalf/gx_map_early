package utils;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class RobotoTextView extends AppCompatTextView {

    public RobotoTextView(Context context) {
        super(context);
        setTypeface(TypefaceManager.get(context, "Roboto-Regular.ttf"));
    }

    public RobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(TypefaceManager.get(context, "Roboto-Regular.ttf"));

    }

    public RobotoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(TypefaceManager.get(context, "Roboto-Regular.ttf"));
    }
}
