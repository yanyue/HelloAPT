package com.example.helloapt;

import com.example.apt_annotation.Executor;
import com.example.apt_annotation.SpeechExecutor;

@Executor(name = "weather")
public class WeatherExecutor implements SpeechExecutor {

    @Override
    public boolean execute(String str) {
        return false;
    }
}
