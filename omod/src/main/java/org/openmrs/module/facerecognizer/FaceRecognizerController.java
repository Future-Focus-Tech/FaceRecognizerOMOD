package org.openmrs.module.facerecognizer;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
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
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;


@Controller
@RequestMapping(value = "/rest/v1/facerec")
public class FaceRecognizerController {
    @RequestMapping(value = "echo", method = RequestMethod.POST)
    @ResponseBody
    public String echo(@RequestParam(value = "facesData") String[]
                               facesData) throws IOException {

        int numberOfFaces = facesData.length;

        Loader.load(org.bytedeco.javacpp.opencv_core.class);
        FaceRecognizer lbphFaceRecognizer = createLBPHFaceRecognizer();
        FaceRecognizerWrapper wrapper = new FaceRecognizerWrapper
                ("/opt/openmrs/facerec-files/learnedData.yml");

        MatVector faces = new MatVector(numberOfFaces);

        for (int faceIndex = 0; faceIndex < numberOfFaces; faceIndex++) {
            BufferedImage bufferedImage = getBufferedImage(facesData[faceIndex]);
            opencv_core.Mat matImage = createFaceMatrix(bufferedImage);
            imwrite("/opt/openmrs/facerec-files/image" + faceIndex + ".png",
                    matImage);
            faces.put(faceIndex, matImage);
        }

        return String.valueOf(numberOfFaces);

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
