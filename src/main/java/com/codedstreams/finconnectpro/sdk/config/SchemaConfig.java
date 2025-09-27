package com.codedstreams.finconnectpro.sdk.config;

/**
 * Configuration class for XML schema validation and processing settings.
 * <p>
 * This class holds configuration properties for schema validation, XSLT transformations,
 * and XML processing parameters. These settings can be customized through application
 * properties or YAML configuration files.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public class SchemaConfig {

    private String schemaBasePath;
    private String customTransformsPath;
    private boolean validationEnabled = true;
    private int transformationTimeout = 30000;

    /**
     * Gets the base path for XML schema files.
     *
     * @return the schema base path, or null if not set
     */
    public String getSchemaBasePath() {
        return schemaBasePath;
    }

    /**
     * Sets the base path for XML schema files.
     *
     * @param schemaBasePath the schema base path to set
     */
    public void setSchemaBasePath(String schemaBasePath) {
        this.schemaBasePath = schemaBasePath;
    }

    /**
     * Gets the path for custom XSLT transformation files.
     *
     * @return the custom transforms path, or null if not set
     */
    public String getCustomTransformsPath() {
        return customTransformsPath;
    }

    /**
     * Sets the path for custom XSLT transformation files.
     *
     * @param customTransformsPath the custom transforms path to set
     */
    public void setCustomTransformsPath(String customTransformsPath) {
        this.customTransformsPath = customTransformsPath;
    }

    /**
     * Checks if XML schema validation is enabled.
     *
     * @return true if validation is enabled, false otherwise
     */
    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    /**
     * Enables or disables XML schema validation.
     *
     * @param validationEnabled true to enable validation, false to disable
     */
    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    /**
     * Gets the timeout for XSLT transformations in milliseconds.
     *
     * @return the transformation timeout in milliseconds
     */
    public int getTransformationTimeout() {
        return transformationTimeout;
    }

    /**
     * Sets the timeout for XSLT transformations in milliseconds.
     *
     * @param transformationTimeout the transformation timeout to set
     */
    public void setTransformationTimeout(int transformationTimeout) {
        this.transformationTimeout = transformationTimeout;
    }
}
