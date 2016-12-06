package org.openmrs.module.facerecognizer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/v1/facerec")
public class FaceRecognizerController {
    @RequestMapping(value = "echo", method = RequestMethod.POST)
    @ResponseBody
    public String echo(@RequestParam("data") String data) {
        return "You sent " + data;
    }
}
