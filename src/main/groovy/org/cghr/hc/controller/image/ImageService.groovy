package org.cghr.hc.controller.image

import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.ServletContext

/**
 * Created by ravitej on 15/5/14.
 */
@RestController
@RequestMapping("/image")
class ImageService {

    @Autowired
    ServletContext servletContext
    @Autowired
    HashMap fileStoreFactory

    @RequestMapping(value = "/{memberId}/{filestore}/{category}", produces = MediaType.IMAGE_PNG_VALUE)
    byte[] getImage(
            @PathVariable("memberId") String memberId,
            @PathVariable("filestore") String filestore,
            @PathVariable("category") String category) throws IOException {

        InputStream inputStream = servletContext.getResourceAsStream(getImagePath(memberId, filestore, category))
        IOUtils.toByteArray(inputStream)
    }

    String getImagePath(String memberId, String filestore, String category) {

        fileStoreFactory."$filestore"."$category"

    }

}
