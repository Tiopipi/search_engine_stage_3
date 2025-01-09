package org.ulpgc.bd.apps;

public class EnvironmentLoader {
    public static String[] loadPaths() {
        String METADATA_FILE_PATH = System.getenv("METADATA_FILE_PATH");
        String UNIQUE_FILE_PATH = System.getenv("UNIQUE_FILE_PATH");
        String TREE_DIRECTORY = System.getenv("TREE_DIRECTORY");
        String HIERARCHICAL_DIRECTORY = System.getenv("HIERARCHICAL_DIRECTORY");
        String BOOKS_FILE_PATH = System.getenv("BOOKS_FILE_PATH");
        return new String[]{
                METADATA_FILE_PATH,
                UNIQUE_FILE_PATH,
                TREE_DIRECTORY,
                HIERARCHICAL_DIRECTORY,
                BOOKS_FILE_PATH
        };
    }
}
