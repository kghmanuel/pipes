package com.marklogic.pipes.ui.BackendModules.operations;

import java.io.File;
import java.io.InputStream;

public class Delete implements ModuleOperation {
  @Override
  public void execute(InputStream is, File dest) {
    dest.delete();

  }
}
