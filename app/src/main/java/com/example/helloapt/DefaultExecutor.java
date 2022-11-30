package com.example.helloapt;

import com.example.apt_annotation.Executor;

@Executor(namespace ={"simple", "love"}, name="cat")
public class DefaultExecutor extends SimpleExecutor {

    @Override
    public boolean execute(String str) {
        return false;
    }
}
