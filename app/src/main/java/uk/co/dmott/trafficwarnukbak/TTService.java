package uk.co.dmott.trafficwarnukbak;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

public class TTService extends Service implements TextToSpeech.OnInitListener {
    private String str;
    private TextToSpeech mTts;
    //private boolean mTtsIsReady;
    private static final String TAG="TTSService";


    public TTService() {
    }

    @Override
    public void onCreate() {
        //mTtsIsReady = false;
        mTts = new TextToSpeech(this,
                this // OnInitListener
        );
        mTts.setSpeechRate(0.5f);
        //mTts.setOnUtteranceProgressListener(mErrorListener)
        Log.v(TAG, "oncreate_service");
        str ="Traffic Warn UK Text to Speech";
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        str = intent.getExtras().getString("textToSpeak").replace(":", "");

        //sayTraffic(str);

        return START_NOT_STICKY;
    }



    @Override
    public void onStart(Intent intent, int startId) {


        str = intent.getExtras().getString("textToSpeak").replace(":", "");


        //sayHello(str);

        Log.v(TAG, "onstart_service");

        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {

        if (mTts != null) {
            Log.v(TAG, "Stopping the mTts.");
            //mTtsIsReady = false;
            mTts.stop();
            mTts.shutdown();
        }

        Log.v(TAG, "ondestroy_service");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onInit(int status) {
        Log.v(TAG, "oninit");
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.v(TAG, "Language is not available.");
            } else {

                //mTtsIsReady = true;

                mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                    @Override
                    public void onStart(String utteranceId) {
                        Log.v(TAG, "UtteranceProgressListener: onStart.");
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.v(TAG, "UtteranceProgressListener: onDone.");
                        stopSelf();

                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.v(TAG, "UtteranceProgressListener: onError.");
                    }
                });


                Log.v(TAG, "Calling sayTraffic with string." + str);
                sayTraffic(str);
 //               stopSelf(); causes crash
            }
        } else {
            Log.v(TAG, "Could not initialize TextToSpeech.");
        }
    }

    private void sayTraffic(String str) {

            Log.v(TAG, "sayTraffic Sending string." + str);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"Id1");
            mTts.speak(str,
                    TextToSpeech.QUEUE_FLUSH,
                    params);

    }

    /**
    private class InitializeSpeechEngine extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
            protected void onPostExecute (Void aVoid){
                // Display Toast that the engine is ready for use.
                pDialog.dismiss();
                Toast.makeText(MainActivity.this, "Speech to text engine initialization complete", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onProgressUpdate (Void...params){

            }
        }

    }

*/


}

