import history from 'util/History';
import Routes from 'routing/Routes';

// Note: it seems (I am not 100% sure), we can't just set '/wizard/AlertRules' as a value here
//       thus losing the SSOT with the paths used when registering the plugin
//       this is because graylog function Routes.pluginRoute prefix the path with some prefix that may have been configured for the graylog server
// TODO check this is true
function getWizardRoute() {
    return Routes.pluginRoute('WIZARD_ALERTRULES');
}

function redirectToWizard() {
    history.push(getWizardRoute());
}

const Navigation = {
    getWizardRoute,
    redirectToWizard
};

export default Navigation;