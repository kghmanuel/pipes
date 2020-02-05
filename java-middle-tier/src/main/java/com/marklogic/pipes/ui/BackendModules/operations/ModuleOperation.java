package com.marklogic.pipes.ui.BackendModules.operations;

import com.marklogic.pipes.ui.BackendModules.BackendModulesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface ModuleOperation {

  static final Logger logger = LoggerFactory.getLogger(BackendModulesManager.class);

  public void execute(InputStream is, File dest);
}
