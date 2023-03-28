package com.example.acneapplication;

import static org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod.NEAREST_NEIGHBOR;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;
import android.util.Size;

import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassifierWithModel {
    private static final String MODEL_NAME = "acne_clf_model.tflite";
    private static final String LABEL_FILE = "labels.txt";

    Context context;
    Model model;
    int modelInputWidth, modelInputHeight, modelInputChannel;
    TensorImage inputImage;
    TensorBuffer outputBuffer;
    private List<String> labels;

    private boolean isInitialized = false;

    public ClassifierWithModel(Context context) {
        this.context = context;
    }

    // 모델 출력 클래스 수를 담을 멤버 변수 선언
    int modelOutputClasses;

    public void init() throws IOException {
        model = Model.createModel(context, MODEL_NAME);

        initModelShape();
        labels = FileUtil.loadLabels(context, LABEL_FILE);
//        labels.remove(0)

        isInitialized = true;
    }

    public boolean isInitialized() { return isInitialized; }

    // 모델의 입출력 크기 계산 함수 정의
    public void initModelShape() {
        Tensor inputTensor = model.getInputTensor(0);
        int[] inputShape = inputTensor.shape();
        modelInputChannel = inputShape[0];
        modelInputWidth = inputShape[1];
        modelInputHeight = inputShape[2];

        inputImage = new TensorImage(inputTensor.dataType());

        // 모델 출력 클래스 수 계산
        Tensor outputTensor = model.getOutputTensor(0);
        outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType());
    }

    public Size getModelInputSize() {
        if(!isInitialized)
            return new Size(0, 0);
        return new Size(modelInputWidth, modelInputHeight);
    }

    // Bitmap을 ARGB_8888로 변환하는 함수
    private Bitmap convertBitmapToARGB8888(Bitmap bitmap) {
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    // TensorImage에 bitmap 이미지 입력 및 이미지 전처리 로직 정의
    private TensorImage loadImage(final Bitmap bitmap, int sensorOrientation) {
        if(bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            inputImage.load(convertBitmapToARGB8888(bitmap));
        } else {
            inputImage.load(bitmap);
        }

        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int numRotation = sensorOrientation / 90;

        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                .add(new ResizeOp(modelInputWidth, modelInputHeight, NEAREST_NEIGHBOR))
                .add(new Rot90Op(numRotation))
                .add(new NormalizeOp(0.0f, 255.0f))
                .build();

        return imageProcessor.process(inputImage);
    }


    public Pair<String, Float> classify(Bitmap image, int sensorOrientation) {
        inputImage = loadImage(image, sensorOrientation);

        Object[] inputs = new Object[]{inputImage.getBuffer()};
        Map<Integer, Object> outputs = new HashMap();
        outputs.put(0, outputBuffer.getBuffer().rewind());

        model.run(inputs, outputs);

        Map<String, Float> output =
                new TensorLabel(labels, outputBuffer).getMapWithFloatValue();

        return argmax(output);
    }

    public Pair<String, Float> classify(Bitmap image) {
        return classify(image, 0);
    }

    // 추론 결과 해석
    private Pair<String, Float> argmax(Map<String, Float> map) {
        String maxKey = "";
        float maxVal = -1;

        for(Map.Entry<String, Float> entry : map.entrySet()) {
            float f = entry.getValue();
            if(f > maxVal) {
                maxKey = entry.getKey();
                maxVal = f;
            }
        }

        return new Pair<>(maxKey, maxVal);
    }

//    // 입력 이미지 크기 변환
//    private Bitmap resizeBitmap(Bitmap bitmap) {
//        return Bitmap.createScaledBitmap(bitmap, modelInputWidth, modelInputHeight, false);
//    }
//
//    // ARGB를 GrayScale로 변환하면서 Bitmap을 ByteBuffer포맷으로 변환
//    private ByteBuffer convertBitmapToGrayByteBuffer(Bitmap bitmap) {
//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bitmap.getByteCount());
//        byteBuffer.order(ByteOrder.nativeOrder());
//
//        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
//        bitmap.getPixels(pixels, 0, bitmap.getWidth(),0, 0, bitmap.getWidth(), bitmap.getHeight());
//
//        for (int pixel : pixels) {
//            int r = pixel >> 16 & 0xFF;
//            int g = pixel >> 8 & 0xFF;
//            int b = pixel & 0xFF;
//
//            float avgPixelValue = (r + g + b) / 3.0f;
//            float normalizedPixelValue = avgPixelValue / 255.0f;
//
//            byteBuffer.putFloat(normalizedPixelValue);
//        }
//        return byteBuffer;
//    }

    // interpreter 자원 정리
    public void finish() {
        if(model != null)
            model.close();
    }

}
