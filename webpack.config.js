const PluginWebpackConfig = require('graylog-web-plugin').PluginWebpackConfig;
const loadBuildConfig = require('graylog-web-plugin').loadBuildConfig;
const path = require('path');

// Remember to use the same name here and in `getUniqueId()` in the java MetaData class
module.exports = new PluginWebpackConfig(__dirname, 'com.airbus_cyber_security.graylog.AlertWizardPlugin', loadBuildConfig(path.resolve(__dirname, './build.config')), {
  // Here goes your additional webpack configuration.

    performance: {
        maxEntrypointSize: 3074000,
        maxAssetSize: 2048000
    }
});
