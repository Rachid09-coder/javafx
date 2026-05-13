package com.edusmart.util;

public class RunDbMigrator {
    /**
     * Small CLI entry to run DB migration outside the JavaFX app.
     */
    public static void main(String[] args) {
        System.out.println("Running DB migrator...");
        DbMigrator.ensureShopTablesExist();
        System.out.println("Done.");
    }
}
