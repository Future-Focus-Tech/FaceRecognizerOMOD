package org.openmrs.module.facerecognizer;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
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
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;


@Controller
@RequestMapping(value = "/rest/v1/facerec")
public class FaceRecognizerController {

    @RequestMapping(value = "train", method = RequestMethod.POST)
    @ResponseBody
    public String train(@RequestParam(value = "facesData") String[] facesData,
                        @RequestParam(value = "mappedValues") int[] mappedValues)
            throws IOException {
        int numberOfFaces = facesData.length;
        int numberOfMappedValues = mappedValues.length;
        if (numberOfFaces != numberOfMappedValues)
            return "Invalid parameters";

        FaceRecognizerWrapper wrapper = new FaceRecognizerWrapper
                ("/opt/openmrs/facerec-files/learnedData.yml");

        MatVector faces = new MatVector(numberOfFaces);

        for (int faceIndex = 0; faceIndex < numberOfFaces; faceIndex++) {
            BufferedImage bufferedImage = getBufferedImage(facesData[faceIndex]);
            Mat matImage = createFaceMatrix(bufferedImage);
            faces.put(faceIndex, matImage);
        }

        wrapper.train(faces, new Mat(mappedValues));
        wrapper.writeToFile();
        return "Trained";
    }

    @RequestMapping(value = "predict", method = RequestMethod.POST)
    @ResponseBody
    public String predict(@RequestParam(value = "facesData") String[] facesData)
            throws IOException {
        FaceRecognizerWrapper wrapper = new FaceRecognizerWrapper
                ("/opt/openmrs/facerec-files/learnedData.yml");
        int numberOfFaces = facesData.length;
        MatVector faces = new MatVector(numberOfFaces);

        for (int faceIndex = 0; faceIndex < numberOfFaces; faceIndex++) {
            BufferedImage bufferedImage = getBufferedImage(facesData[faceIndex]);
            Mat matImage = createFaceMatrix(bufferedImage);
            faces.put(faceIndex, matImage);
        }
        int[] predictedValues = wrapper.predict(faces);
        return java.util.Arrays.toString(predictedValues);
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
        opencv_core.IplImage iplImage = cv.convertToIplImage(frame);
        int width = iplImage.width();
        int height = iplImage.height();
        IplImage grayIplImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
        cvCvtColor(iplImage, grayIplImage, CV_BGR2GRAY);
        return new Mat(grayIplImage);
    }

}
