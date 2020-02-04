package com.marklogic.pipes.ui.BackendModules.operations;

import com.marklogic.pipes.ui.BackendModules.BackendModulesManager;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Copy implements ModuleOperation {

  private static final Logger logger = LoggerFactory.getLogger(BackendModulesManager.class);

  @Override
  public void execute(InputStream is, File dest) {
    try {
      FileUtils.copyInputStreamToFile(is, dest);
    } catch (IOException e) {
      logger.error("Tried to copy "+is.toString()+" to "+dest.getAbsolutePath()+" and failed.",e);
      e.printStackTrace();
    }
  }
}
