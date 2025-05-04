package com.dam.kairos.utils;

import androidx.annotation.NonNull;

public class ImageStylePrompt {
    private final String name;
    private final String promptStart;
    private final String promptEnd;

    public ImageStylePrompt(String name, String promptStart, String promptEnd) {
        this.name = name;
        this.promptStart = promptStart;
        this.promptEnd = promptEnd;
    }

    public String getName() {
        return name;
    }

    public String getFullPrompt(String diaryText) {
        return promptStart + diaryText + (promptEnd != null ? promptEnd : "");
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}

