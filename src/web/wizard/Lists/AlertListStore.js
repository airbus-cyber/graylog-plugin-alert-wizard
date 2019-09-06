import Reflux from 'reflux';
import URLUtils from 'util/URLUtils';
import UserNotification from 'util/UserNotification';
import fetch from 'logic/rest/FetchProvider';
import AlertListActions from './AlertListActions'

const AlertListStore = Reflux.createStore({
    listenables: [AlertListActions],
    sourceUrl: '/plugins/com.airbus_cyber_security.graylog/lists',
    lists: undefined,

    init() {
      this.trigger({lists: this.lists});
    },

    list() {
        const promise = fetch('GET', URLUtils.qualifyUrl(this.sourceUrl))
            .then(
                response => {
                    this.lists = response.lists;
                    this.trigger({lists: this.lists});
                    return this.lists;
                },
                error => {
                    UserNotification.error(`Fetching alert lists failed with status: ${error}`,
                        'Could not retrieve alerts lists');
                });
        AlertListActions.list.promise(promise);
    },
});