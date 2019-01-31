import Reflux from 'reflux';

const AlertRuleActions = Reflux.createActions({
    list: {asyncResult: true},
    listWithData: {asyncResult: true},
    get: {asyncResult: true},
    getData: {asyncResult: true},
    create: {asyncResult: true},
    deleteByName: {asyncResult: true},
    update: {asyncResult: true},
    clone: {asyncResult: true},
    exportAlertRules: {asyncResult: true},
    importAlertRules: {asyncResult: true},
});

export default AlertRuleActions;
