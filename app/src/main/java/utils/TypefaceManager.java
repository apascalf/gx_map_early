package utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;

import java.util.HashMap;

public class TypefaceManager {
    private static HashMap<String, Typeface> mCache = new HashMap<>();

    @Nullable
    public static Typeface get(Context context, String name) {
        if (mCache.containsKey(name)) return mCache.get(name);
        try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + name);
            mCache.put(name, typeface);
        } catch (Exception e) {
        }
        return mCache.get(name);
    }
}
