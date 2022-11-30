package com.example.helloapt;

import com.example.apt_annotation.SpeechExecutor;

public class SimpleExecutor implements SpeechExecutor {

    @Override
    public boolean execute(String str) {
        return false;
    }
}
