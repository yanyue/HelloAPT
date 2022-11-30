package com.example.helloapt;

import com.example.apt_annotation.Executor;
import com.example.apt_annotation.SpeechExecutor;

@Executor(namespace ={"simple", "love"}, name="test")
public class DefaultExecutor implements SpeechExecutor {

    @Override
    public boolean execute(String str) {
        return false;
    }
}
