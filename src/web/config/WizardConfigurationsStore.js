import Reflux from 'reflux';
import URLUtils from 'util/URLUtils';
import UserNotification from 'util/UserNotification';
import fetch from 'logic/rest/FetchProvider';
import WizardConfigurationsActions from './WizardConfigurationsActions';

const WizardConfigurationStore = Reflux.createStore({
    listenables: [WizardConfigurationsActions],
    sourceUrl: '/plugins/com.airbus_cyber_security.graylog/config',
    configurations: undefined,

    init() {
        this.trigger({configurations: this.configurations});
    },

    list() {
        const promise = fetch('GET', URLUtils.qualifyUrl(this.sourceUrl))
            .then(
                response => {
            this.configurations = response;
            this.trigger({configurations: this.configurations});
            return this.configurations;
        },
        error => {
            UserNotification.error(`Fetching wizard configurations failed with status: ${error}`,
                'Could not retrieve configurations');
        });
        WizardConfigurationsActions.list.promise(promise);
    },

    update(config) {
        const request = {field_order: config.field_order,
                        default_values: config.default_values,
                        import_policy: config.import_policy,
        };

        const promise = fetch('PUT', URLUtils.qualifyUrl(this.sourceUrl), request)
            .then(() => {
            UserNotification.success('Wizard configurations successfully updated');
            this.list();
            return true;
        }, (error) => {
            UserNotification.error(`Updating wizard configurations failed with status: ${error.message}`,
                'Could not update configurations');
        });

        WizardConfigurationsActions.update.promise(promise);
    },

});

export default WizardConfigurationStore;
