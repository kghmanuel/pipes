/*
Copyright ©2020 MarkLogic Corporation.
*/

package com.marklogic.pipes.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.stereotype.*;

/**
 * InstallPipesBackend
 */
@Component
public class InstallPipesBackend implements ApplicationRunner {

  @Autowired
  ClientConfig clientConfig;

  @Override
  public void run(ApplicationArguments args) throws Exception {

    boolean deployBackend = args.containsOption("deployBackend");

    // TO-DO: check for existence of the folder

    if (deployBackend) {
      LoggerFactory.getLogger(getClass()).info(
        String.format("Will copy MarkLogic backend modules to following DHF root: %s", clientConfig.getMlDhfRoot()));

      final String resourcesDhfRoot = "/dhf/src/main/ml-modules";
      final String destinationDhfRoot = "/src/main/ml-modules";
      final String customModulesPathPrefix = "/root/custom-modules/pipes/";

      ArrayList<String> filePaths = new ArrayList<String>(
        Arrays.asList(
          customModulesPathPrefix+"core.sjs",
          customModulesPathPrefix+"entity-services-lib-vpp.sjs",
          customModulesPathPrefix+"google-libphonenumber.sjs",
          customModulesPathPrefix+"graphHelper.sjs",
          customModulesPathPrefix+"litegraph.sjs",
          customModulesPathPrefix+"moment-with-locales.min.sjs",
          customModulesPathPrefix+"user.sjs",
          "/services/vppBackendServices.sjs"
        ));


      for (final String filePath : filePaths) {
        final InputStream is = Proxy.class.getResourceAsStream(resourcesDhfRoot + filePath);
        final File dest = new File(clientConfig.getMlDhfRoot() + destinationDhfRoot + filePath);

        try {
          FileUtils.copyInputStreamToFile(is, dest);
        } catch (final IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          System.out.println(e.toString());
        }
      }

      LoggerFactory.getLogger(getClass()).info(
        String.format("MarkLogic backend modules copied to your DHF project. Make sure to reload the modules from your DHF project!"));
    }



  }


}
