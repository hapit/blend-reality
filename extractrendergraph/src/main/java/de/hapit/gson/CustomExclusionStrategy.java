package de.hapit.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;


public class CustomExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(GsonSkip.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
