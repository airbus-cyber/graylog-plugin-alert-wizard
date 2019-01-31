package com.airbus_cyber_security.graylog;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Arrays;
import java.util.Collection;

/**
 * Implement the Plugin interface here.
 */
public class AlertWizardPlugin implements Plugin {
    @Override
    public PluginMetaData metadata() {
        return new AlertWizardtMetaData();
    }

    @Override
    public Collection<PluginModule> modules () {
        return Arrays.<PluginModule>asList(new AlertWizardModule());
    }
}
