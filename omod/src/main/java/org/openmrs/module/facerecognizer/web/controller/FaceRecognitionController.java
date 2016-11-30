package org.openmrs.module.facerecognizer.web.controller;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.openmrs.module.facerecognizer.FaceRecognizerWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.springframework.http.ResponseEntity.ok;

@Controller
public class FaceRecognitionController
{
    private final FaceRecognizerWrapper recognizerWrapper = new FaceRecognizerWrapper("LearnedData.yml");

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> faces(@RequestParam(value = "facesData[]") String[] facesData) throws IOException {
        int numberOfFaces = facesData.length;
        MatVector faces = new MatVector(numberOfFaces);

        for (int faceIndex = 0; faceIndex < numberOfFaces; faceIndex++) {
            BufferedImage bufferedImage = getBufferedImage(facesData[faceIndex]);
            Mat matImage = createFaceMatrix(bufferedImage);
            imwrite("image" + faceIndex + ".png", matImage);
            faces.put(faceIndex, matImage);
        }


        return ok("thank you");
    }

    private BufferedImage getBufferedImage(String imageDataUrl) throws IOException {
        String encodingPrefix = "base64,";
        int contentStartIndex = imageDataUrl.indexOf(encodingPrefix) + encodingPrefix.length();
        byte[] imageData = DatatypeConverter.parseBase64Binary(imageDataUrl.substring(contentStartIndex));
        InputStream inputStream = new ByteArrayInputStream(imageData);
        return ImageIO.read(inputStream);

    }

    private Mat createFaceMatrix(BufferedImage bufferedImage) {
        OpenCVFrameConverter.ToMat cv = new OpenCVFrameConverter.ToMat();
        Java2DFrameConverter frameConverter = new Java2DFrameConverter();
        Frame frame = frameConverter.getFrame(bufferedImage, 1.0, true);
        IplImage iplImage = cv.convertToIplImage(frame);
        int width = iplImage.width();
        int height = iplImage.height();
        IplImage grayIplImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
        cvCvtColor(iplImage, grayIplImage, CV_BGR2GRAY);
        return new Mat(grayIplImage);
    }

    @RequestMapping(value= "/train", method = RequestMethod.GET)
    public void train(MatVector faces, Mat labels){
//        FaceDetectorWrapper detector = new FaceDetectorWrapper();
//        MatVector faces = detector.detect(image);
        recognizerWrapper.train(faces, labels);
        recognizerWrapper.close();
    }

    @RequestMapping(value = "/predict", method = RequestMethod.GET)
    public int[] predict(MatVector faces) {
        int[] predicted = recognizerWrapper.predict(faces);
        return predicted;
    }
}
