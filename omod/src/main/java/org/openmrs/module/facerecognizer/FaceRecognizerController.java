package org.openmrs.module.facerecognizer;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;


@Controller
@RequestMapping(value = "/rest/v1/facerec")
public class FaceRecognizerController {
    @RequestMapping(value = "echo", method = RequestMethod.POST)
    @ResponseBody
    public String echo(@RequestParam(value = "facesData") String[]
                               facesData) {

        int numberOfFaces = facesData.length;

        Loader.load(org.bytedeco.javacpp.opencv_core.class);
        FaceRecognizer lbphFaceRecognizer = createLBPHFaceRecognizer();
        return String.valueOf(numberOfFaces);

    }


}
