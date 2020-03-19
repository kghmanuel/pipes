/*
Copyright ©2020 MarkLogic Corporation.
*/

package com.marklogic.pipes.ui.customStep;

  public class CustomStep {
    private String name;
    private String path;
    private String collection;

    private String database;

    public String getCollection() {
      return collection;
    }

    public void setCollection(String collection) {
      this.collection = collection;
    }

    public String getDatabase() {
      return database;
    }

    public void setDatabase(String database) {
      this.database = database;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public CustomStep(String name, String path, String database, String collection){
      this.name=name;
      this.path=path;
      this.database=database;
      this.collection=collection;
    }
  }

