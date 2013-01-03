package com.reuters.core.ngram_deduplication.ddservice;

import org.apache.log4j.Logger;

import javax.ws.rs.*;
import java.util.*;

/**
 * User: Oleksiy Pylypenko
 * Date: 12/14/11
 * Time: 2:09 PM
 */
@Path("/")
public class DeDupWebService {
    private static final Logger log = Logger.getLogger(DeDupWebService.class);

    @POST
    @Path("/parse-text")
    @Produces("text/plain")
    public String parse(@FormParam("text") String text) {
        DeDupAlgo algo = DeDupAlgo.INSTANCE;
        int id = algo.addDocument(text);
        List<DocumentMatch> list = new ArrayList<DocumentMatch>();
        double unique = algo.calcUniqueness(id, list, 10, 0.33);
        algo.index(id);
        System.out.println("doc=" + id + " list=" + list + "unique=" + unique + " " + Arrays.toString(algo.parseDocument(text)));
        return "unique=" + unique;
    }

    @GET
    @Path("/stop")
    @Produces("text/plain")
    public String stop(@FormParam("code") String code) {
        if (code.equals("SYEa8x2k5ZQ")) {
            DeDupWebMain.MAIN_INSTANCE.stop();
            return "DONE";
        } else {
            return "BAD_CODE";
        }
    }

}
