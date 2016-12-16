package org.openmrs.module.facerecognizer;

import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;

import java.io.File;

import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

public class FaceRecognizerWrapper {
    private final String trainedFile;
    private final FaceRecognizer lbphFaceRecognizer;

    public FaceRecognizerWrapper(String trainedFile) {
        this.trainedFile = trainedFile;
        File file = new File(trainedFile);
        boolean isFileExists = file.exists() && file.isFile();
        lbphFaceRecognizer = createLBPHFaceRecognizer();
        if (isFileExists) {
            lbphFaceRecognizer.load(trainedFile);
        } else {
            lbphFaceRecognizer.save(trainedFile);
        }
    }

    public void train(MatVector faces, Mat labels) {
        lbphFaceRecognizer.update(faces, labels);
    }

    public int[] predict(MatVector images) {
        int noOfImages = (int) images.size();
        int[] predicted;
        predicted = new int[noOfImages];
        for (int index = 0; index < noOfImages; ++index) {
            predicted[index] = lbphFaceRecognizer.predict(images.get(index));
        }
        return predicted;
    }

    public void writeToFile() {
        lbphFaceRecognizer.save(trainedFile);
    }
}