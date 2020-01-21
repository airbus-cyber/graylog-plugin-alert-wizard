import Reflux from 'reflux';

const AlertListActions = Reflux.createActions({
    list: {asyncResult: true},
    get: {asyncResult: true},
    create: {asyncResult: true},
    deleteByName: {asyncResult: true},
    update: {asyncResult: true},
    clone: {asyncResult: true},
    exportAlertLists: {asyncResult: true},
    importAlertLists: {asyncResult: true},
});

export default AlertListActions;