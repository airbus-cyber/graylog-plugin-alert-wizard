import URLUtils from 'util/URLUtils';
import UserNotification from 'util/UserNotification';
import fetch from 'logic/rest/FetchProvider';
import RestUtils from 'wizard/stores/RestUtils';

const SOURCE_URL = URLUtils.qualifyUrl('/events/definitions');

function enable(eventDefinitionIdentifier) {
    return fetch('PUT', `${SOURCE_URL}/${eventDefinitionIdentifier}/schedule`)
        .then(response => response)
        .catch(error => {
            UserNotification.error(`Enabling Event Definition "${eventDefinitionIdentifier}" failed with status: ${error}`,
                'Could not enable Event Definition');
        });
}

function disable(eventDefinitionIdentifier) {
    return fetch('PUT', `${SOURCE_URL}/${eventDefinitionIdentifier}/unschedule`)
        .then(response => response)
        .catch(error => {
            UserNotification.error(`Disabling Event Definition "${eventDefinitionIdentifier}" failed with status: ${error}`,
                'Could not disable Event Definition');
        });
}

export default {
    enable,
    disable
};