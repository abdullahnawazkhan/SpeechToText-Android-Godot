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
import androidx.collection.ArraySet;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SpeechToText extends GodotPlugin {
    // tag for logging this specific module
    private static final String TAG = "godot-android";

    private SpeechRecognizer speechRecognizer;
    private Intent intent;
    private String currentLanguage;
    // holds the words spoken by the user
    // will hold the value "error" if there was some error with STT
    private String words;
    private boolean isErrorAlreadyCalledAfterBeginOfSpeech = false;
    /**
     * Save the latest partial result for reporting when offline.
     */
    private Bundle latestPartialResults = null;

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
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage);

                speechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle params) {
                    }
                    /**
                     * Fires on start of STT
                     */
                    @Override
                    public void onBeginningOfSpeech() {
                        latestPartialResults = null;
                        Log.d(TAG, "Speech Has Started");
                    }
                    @Override
                    public void onRmsChanged(float rmsdB) {
                    }
                    @Override
                    public void onBufferReceived(byte[] buffer) {
                    }
                    @Override
                    public void onEndOfSpeech() {
                        Log.d(TAG, "Speech Has Ended");
                    }
                    @Override
                    public void onError(int error) {
                        // this function will either be called if there is some internal error with Android's speech to text
                        // or user has said nothing while the "listen" function is being called
                        Log.d(TAG, "Error Has Occured " + error);
                       // If the error has already been fired attempt to recover some words that where spoken.
                        if(!isErrorAlreadyCalledAfterBeginOfSpeech) {
                            isErrorAlreadyCalledAfterBeginOfSpeech = true;
                            Log.d(TAG, "Checking For Partial Results");
                            if (isOfflineSpeechRecognition(error, latestPartialResults)) {
                                Log.d(TAG, "Appending Partial Results");
                                appendUnstableTextToResult(latestPartialResults);
                                onResults(latestPartialResults);
                            }
                        }
                        emitSignal("error", String.valueOf(error));

                        //words = "error";
                    }

                    /**
                     * Checks if we are offline speech recognition
                     */
                    private boolean isOfflineSpeechRecognition(int error, Bundle latestPartialResults) {
                        return error == SpeechRecognizer.ERROR_NO_MATCH && latestPartialResults != null;
                    }
                    private void appendUnstableTextToResult(Bundle partialResults) {
                        // Partial results are split up by previous spoken text and the current spoken text after
                        // short speak break given as 'UNSTABLE_TEXT'. To provide same usage for online and offline
                        // we report it combined. It gives only one result when doing speech recognition offline.
                        Log.d(TAG, "Appending Partial Results");
                        ArrayList<String> target = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        ArrayList<String> source = partialResults.getStringArrayList("android.speech.extra.UNSTABLE_TEXT");
                        words = (target.get(0) + " " + source.get(0)).trim();
                    }
                    @Override
                    public void onResults(Bundle results) {
                        // this function will be executed when Android's STT returns the detected string
                        // the results bundle contains an array consisted of all detected strings
                        // strings are sorted based on accuracy, so index 0 will have the string with the highest accuracy
                        // high accuracy == best result string
                        ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        words = data.get(0);
                        emitSignal("listening_completed", words);
                        Log.d(TAG, words);
                    }
                    /**
                     * Gets the partial results(if a result was not verified) and returns it.
                     */
                    @Override
                    public void onPartialResults(Bundle partialResults) {
                        latestPartialResults = partialResults;

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
        return Arrays.asList("listen", "stop", "getWords", "setLanguage");
    }

    public void stop() {
        getActivity().runOnUiThread(new Runnable() {
            @Override

            public void run() {
                Log.d(TAG, "Speech has stop listening.");
                speechRecognizer.stopListening();

            }
        });
    }
    /**
     * Checks if the speech processor is able to be ran on the device.
     */
    public void isSTTAvailable() {
        SpeechRecognizer.isRecognitionAvailable(getActivity());
    }
    /**
     * Cancels the speech processor (can fix small bugs with parsing).
     */
    public void cancel() {
        getActivity().runOnUiThread(new Runnable() {
            @Override

            public void run() {
                Log.d(TAG, "Speech has canceled.");
                speechRecognizer.cancel();

            }
        });
    }
    /**
     * Starts the listening process for the google STT engine.
     */
    public void listen() {
        words = "";
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Listening started");

                speechRecognizer.startListening(intent);
            }
        });
    }
    /**
     * Sets the users language.
     * @param language  The Language the user is using.
     */
    public void setLanguage(String language){
        currentLanguage = language;
        Log.d(TAG, "Set Language to: " + language);
    }
    /**
     * Returns the last spoken words of the user.
     * @return String of the last spoken words the user said in the current session.
     */
    public String getWords() {
        Log.d(TAG, "Returning Words: " + words);
        return words;
    }
    /**
     * Sets up all the signals the user has access to.
     * @return The signals that Godot can use.
     */
    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        //return super.getPluginSignals();
        Set<SignalInfo> signals = new ArraySet<>();
        signals.add(new SignalInfo("listening_completed", String.class));
        signals.add(new SignalInfo("error", String.class));
        return signals;
    }
}
