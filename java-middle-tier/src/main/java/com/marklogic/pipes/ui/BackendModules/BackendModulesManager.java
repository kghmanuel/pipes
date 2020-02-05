package com.marklogic.pipes.ui.BackendModules;

import com.marklogic.appdeployer.AppConfig;
import com.marklogic.appdeployer.AppDeployer;
import com.marklogic.appdeployer.command.modules.DeleteModulesCommand;
import com.marklogic.appdeployer.command.modules.LoadModulesCommand;
import com.marklogic.appdeployer.impl.SimpleAppDeployer;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.admin.ResourceExtensionsManager;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.ManageConfig;
import com.marklogic.mgmt.admin.AdminConfig;
import com.marklogic.mgmt.admin.AdminManager;
import com.marklogic.pipes.ui.BackendModules.operations.Copy;
import com.marklogic.pipes.ui.BackendModules.operations.Delete;
import com.marklogic.pipes.ui.BackendModules.operations.ModuleOperation;
import com.marklogic.pipes.ui.config.ClientConfig;
import com.marklogic.pipes.ui.Application;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;


@Repository
public class BackendModulesManager {
  private static final Logger logger = LoggerFactory.getLogger(BackendModulesManager.class);

  final String resourcesDhfRoot = "/dhf/src/main/ml-modules";
  final String destinationDhfRoot = "/src/main/ml-modules";
  final String customModulesPathPrefix = "/root/custom-modules/pipes";

  ArrayList<String> filePaths = new ArrayList<String>(
    Arrays.asList(
      customModulesPathPrefix+"/core.sjs",
      customModulesPathPrefix+"/entity-services-lib-vpp.sjs",
      customModulesPathPrefix+"/google-libphonenumber.sjs",
      customModulesPathPrefix+"/graphHelper.sjs",
      customModulesPathPrefix+"/litegraph.sjs",
      customModulesPathPrefix+"/moment-with-locales.min.sjs",
      "/services/vppBackendServices.sjs"
    ));

  public void copyAndDeployPipesBackend(ClientConfig config) throws Exception {


    logger.info(
      String.format("Will copy MarkLogic backend modules to following DHF root: %s", config.getMlDhfRoot()));
    try {
      manageMarkLogicBackendModules(new Copy(), config, filePaths);
    }
    catch (Exception e) {
      logger.error(
        String.format("Aborting Pipes start-up - failed to copy modules: "+e.getMessage()),e);
      throw e;
    }

    try {

      logger.info(
        String.format("Now loading Pipes modules to your DHF modules database...")
      );
      deployMlBackendModulesToModulesDatabase(".*/pipes/.*.sjs|.*vppBackendServices.sjs", config);
      logger.info(
        String.format("MarkLogic backend modules have been loaded."));

    }
    catch (com.marklogic.client.MarkLogicIOException e) {
      logger.error(
        String.format("Hm, failed to connect to your database: "+config.getMlHost()+":"+config.getMlStagingPort()+
          ". Are you sure your MarkLogic server is running at that address? Aborting Pipes start."),e);
      System.exit(1);
    }
    catch (Exception e) {
      logger.error(
        String.format("Aborting Pipes start-up - Failed to load modules: "+e.getMessage()),e);
      throw e;
    }

    logger.info(
      String.format("Modules successfully copied and deployed."));

  }

  public void unloadPipesModules(String pattern, ClientConfig config) throws Exception {

    // unload Pipes backend modules
    removeModulesFromDb(pattern, config);

    // unload Pipes rest extension
    removeRestExtension(config);

    // delete Pipes modules from DHF project
    manageMarkLogicBackendModules(new Delete(), config, filePaths);

    // also delete the Pipes folder from DHF project
    deleteDhfPipesDir(config);


    logger.info(
      String.format("Pipes deleted.")
    );


  }


  private void manageMarkLogicBackendModules(ModuleOperation operation, ClientConfig config,  ArrayList<String> filePaths) throws Exception {

    final String CUSTOMSJSNAME="user.sjs";
    final String CUSTOMSJSPATH=config.getCustomModulesRoot()+File.separator+CUSTOMSJSNAME;




    for (final String filePath : filePaths) {
      final InputStream is = Application.class.getResourceAsStream(resourcesDhfRoot + filePath);
      final File dest = new File(config.getMlDhfRoot() + destinationDhfRoot + filePath);

      operation.execute(is, dest);
    }

    Boolean includeCustomUserModule = checkIfIncludeCustomModules(config, CUSTOMSJSPATH);

    // do the same for the custom user module
    if (includeCustomUserModule) {

      //final File source = new File(CUSTOMSJSPATH);
      final InputStream is = new FileInputStream(CUSTOMSJSPATH);
      final File dest = new File(config.getMlDhfRoot() + destinationDhfRoot + customModulesPathPrefix + File.separator +CUSTOMSJSNAME);
      operation.execute(is,dest);
    }

  }

  public Boolean checkIfIncludeCustomModules(ClientConfig config, String CUSTOMSJSPATH) {
    Boolean includeCustomUserModule=false;

    if(config.getCustomModulesRoot()!=null) {


      if( (new File(CUSTOMSJSPATH)).exists() ) {
        includeCustomUserModule = true;
      }
      else {
        logger.error(
          String.format("Looks like your custom module \""+CUSTOMSJSPATH+"\" is missing. Check your application.properties. Pipes aborting."));
        System.exit(1);
      }
    }
    return includeCustomUserModule;
  }

  public void deployMlBackendModulesToModulesDatabase(String patternString, ClientConfig config) {
    Pattern pattern=Pattern.compile(patternString);

    ManageClient client = getManageClient(config);
    AdminManager manager = getAdminManager(config);
    AppConfig appConfig = getAppConfig(config);

    AppDeployer appDeployer = new SimpleAppDeployer(client, manager, new LoadModulesCommand());

    // Setting batch size just to verify that nothing blows up when doing so
    appConfig.setModulesLoaderBatchSize(1);

    // push /pipes/ modules
    appConfig.setModuleFilenamesIncludePattern(pattern);
    // Call it
    appDeployer.deploy(appConfig);

  }

  private AppConfig getAppConfig(ClientConfig config) {
    // AppConfig contains all configuration about the application being deployed
    AppConfig appConfig = new AppConfig(new File(config.getMlDhfRoot()));
    appConfig.setName("data-hub"); // TO-DO hard coded, bad idea.
    appConfig.setRestPort(config.getMlStagingPort());
    appConfig.setHost(config.getMlHost());
    appConfig.setAppServicesPort(config.getMlAppServicesPort());
    appConfig.setModulesDatabaseName(config.getMlModulesDatabase());
    return appConfig;
  }

  private AdminManager getAdminManager(ClientConfig config) {
    // used for restarting ML; defaults to localhost/8001/admin/admin
    return new AdminManager(new AdminConfig(config.getMlHost(), config.getMlAdminPort(), config.getMlUsername(), config.getMlPassword()));
  }

  private ManageClient getManageClient(ClientConfig config) {
    return new ManageClient(new ManageConfig(config.getMlHost(), config.getMlManagePort(), config.getMlUsername(), config.getMlPassword()));
  }



  private void deleteDhfPipesDir(ClientConfig config) throws IOException {
    String folderPath = config.getMlDhfRoot() + destinationDhfRoot + customModulesPathPrefix;
    FileUtils.deleteDirectory(new File(folderPath));

    logger.info(
      String.format("Deleted folder " + folderPath));
  }

  private void removeRestExtension(ClientConfig config) {
    logger.info(
      String.format("Now deleting Pipes REST extension from your DHF modules database...")
    );

    // will use MarkLogic Client API for this
    // so need a DatabaseClient instance
    DatabaseClient dbClient = getDbClient(config);

    ResourceExtensionsManager resourceExtensionsManager = dbClient.newServerConfigManager().newResourceExtensionsManager();
    resourceExtensionsManager.deleteServices("vppBackendServices");


    logger.info(
      String.format("Deleted Pipes REST extension from your DHF modules database...")
    );
  }

  private void removeModulesFromDb(String pattern, ClientConfig config) {
    logger.info(
      String.format("Now deleting Pipes modules from your DHF modules database...")
    );

    ManageClient client = getManageClient(config);
    AdminManager manager = getAdminManager(config);
    AppConfig appConfig = getAppConfig(config);

    // will use the DeleteModulesCommand
    AppDeployer appDeployer = new SimpleAppDeployer(client, manager, new DeleteModulesCommand(pattern));

    // Setting batch size just to verify that nothing blows up when doing so
    appConfig.setModulesLoaderBatchSize(1);
    appDeployer.deploy(appConfig);

    logger.info(
      String.format("MarkLogic backend modules have been deleted."));
  }

  private DatabaseClient getDbClient(ClientConfig config) {
    return DatabaseClientFactory.newClient(
      config.getMlHost(), config.getMlStagingPort(),
      new DatabaseClientFactory.DigestAuthContext(config.getMlUsername(), config.getMlPassword()));
  }

}
