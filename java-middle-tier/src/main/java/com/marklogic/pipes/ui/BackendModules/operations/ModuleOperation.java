package com.marklogic.pipes.ui.BackendModules.operations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface ModuleOperation {

  public void execute(InputStream is, File dest);
}
