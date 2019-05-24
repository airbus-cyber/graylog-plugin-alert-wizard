import Reflux from 'reflux';

const WizardConfigurationsActions = Reflux.createActions({
    list: { asyncResult: true },
    update: { asyncResult: true },
});

export default WizardConfigurationsActions;
