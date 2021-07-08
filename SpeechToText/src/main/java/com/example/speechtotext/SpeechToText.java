package com.example.speechtotext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SpeechToText extends GodotPlugin {
    // tag for logging this specific module
    private static final String TAG = "godot-android";

    private SpeechRecognizer speechRecognizer;
    private Intent intent;

    // holds the words spoken by the user
    // will hold the value "error" if there was some error with STT
    private String words;
    public SpeechToText(Godot godot) {
        super(godot);

        // SpeechRecognizer class requires to be run on Main thread
        // When loading plugins in godot, they are loading inside their separate thread
        // To force them to run on the main thread, we use the "runOnUiThread" method
        // this method requires a runnable object, which contains the actual code to to be executed
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                // this specifies the language to Android's STT Module
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA");

                speechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle params) {
                    }
                    @Override
                    public void onBeginningOfSpeech() {
                    }
                    @Override
                    public void onRmsChanged(float rmsdB) {
                    }
                    @Override
                    public void onBufferReceived(byte[] buffer) {
                    }
                    @Override
                    public void onEndOfSpeech() {
                    }
                    @Override
                    public void onError(int error) {
                        // this function will either be called if there is some internal error with Android's speech to text
                        // or user has said nothing while the "listen" function is being called
                        words = "error";
                    }
                    @Override
                    public void onResults(Bundle results) {
                        // this function will be executed when Android's STT returns the detected string
                        // the results bundle contains an array consisted of all detected strings
                        // strings are sorted based on accuracy, so index 0 will have the string with the highest accuracy
                        // high accuracy == best result string
                        ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        words = data.get(0);
                    }
                    @Override
                    public void onPartialResults(Bundle partialResults) {
                    }
                    @Override
                    public void onEvent(int eventType, Bundle params) {
                    }
                });
                Log.d(TAG, "Speech Recognizer initialized");
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "SpeechToText";
    }

    @NonNull
    @Override
    public List<String> getPluginMethods() {
        return Arrays.asList("listen", "stop", "getWords");
    }

    public void stop() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speechRecognizer.stopListening();
            }
        });
    }

    public void listen() {
        words = "";
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Listening startedd");
                speechRecognizer.startListening(intent);
            }
        });
    }

    public String getWords() {
        return words;
    }
}
