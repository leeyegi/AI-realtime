package com.example.yegilee.ai_realtime;

import android.content.Context;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

//tensoeflow lite를 이용하여 freeze한 모델 불러와 prediction 수행 클래스
public class TensorFlowClassifier {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private TensorFlowInferenceInterface inferenceInterface;
    private static final String MODEL_FILE = "file:///android_asset/optimized_rnnraw_40_e300.pb";
//    private static final String MODEL_FILE = "file:///android_asset/frozen_har.pb";
    private static final String INPUT_NODE = "in_";
    private static final String HIDDEN_NODE = "hidden_";

    private static final String[] OUTPUT_NODES = {"out_"};
    private static final String OUTPUT_NODE = "out_";
    private static final long[] INPUT_SIZE = {1,40,6};
    private static final long[] HIDDEN_SIZE = {1,512};

    private static final int OUTPUT_SIZE = 16;

    //모델을 불러옴
    public TensorFlowClassifier(final Context context) {
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
    }

    //prediction수행 메소드
    //feed_dict하는 것은 모두 shape를 맞춰야함
    public float[] predictProbabilities(float[] data) {
        float[] result = new float[OUTPUT_SIZE];
        float[] hidden_data=new float[512];
        Log.e("aa",inferenceInterface.getStatString());

        inferenceInterface.feed(INPUT_NODE, data, INPUT_SIZE);
        inferenceInterface.feed(HIDDEN_NODE, hidden_data, HIDDEN_SIZE);

        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE, result);

        return result;
    }
}
