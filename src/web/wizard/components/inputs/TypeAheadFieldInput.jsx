/*
 * Copyright (C) 2018 Airbus CyberSecurity (SAS)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

import PropTypes from 'prop-types';
import React from 'react';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import 'typeahead.js';

import { Input } from 'components/bootstrap';
import UniversalSearch from 'logic/search/UniversalSearch';
import ApiRoutes from 'routing/ApiRoutes';
import { qualifyUrl } from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

/**
 * Component that renders an input offering auto-completion for message fields.
 * Fields are loaded from the Graylog server in the background.
 */
class TypeAheadFieldInput extends React.Component {
  static propTypes = {
    /** ID of the input. */
    id: PropTypes.string.isRequired,
    /** Label of the field input */
    label: PropTypes.string,
    /** Specifies if the input should have the input focus or not. */
    autoFocus: PropTypes.bool,
    /**
     * Function that is called when the input changes. The function receives
     * the typeahead event object for the event that triggered the change. For
     * more information on typeahead events, see:
     * https://github.com/twitter/typeahead.js/blob/master/doc/jquery_typeahead.md#custom-events
     */
    onChange: PropTypes.func,
    onBlur: PropTypes.func,
    /** Display an error for the input * */
    error: PropTypes.string,
  };

  static defaultProps = {
    autoFocus: false,
    label: undefined,
    onChange: () => {},
    onBlur: () => {},
    error: undefined,
  };

  componentDidMount() {
    if (this.fieldInput) {
      const { autoFocus, onChange } = this.props;
      const fieldInput = $(this.fieldInput.getInputDOMNode());

      fetch('GET', qualifyUrl(ApiRoutes.SystemApiController.fields().url))
        .then(
          (data) => {
            fieldInput.typeahead(
              {
                hint: true,
                highlight: true,
                minLength: 1,
              },
              {
                name: 'fields',
                displayKey: 'value',
                source: UniversalSearch.substringMatcher(data.fields, 'value', 6),
              },
            );

            if (autoFocus) {
              fieldInput.focus();
              fieldInput.typeahead('close');
            }
          },
        );

      // eslint-disable-next-line react/no-find-dom-node
      const fieldFormGroup = ReactDOM.findDOMNode(this.fieldInput);

      $(fieldFormGroup).on('typeahead:change typeahead:selected', (event) => {
        console.log('TypeAheadFieldInput.onChange')
        onChange(event);
      });
    }
  }

  componentWillUnmount() {
    if (this.fieldInput) {
      const fieldInput = $(this.fieldInput.getInputDOMNode());

      fieldInput.typeahead('destroy');

      // eslint-disable-next-line react/no-find-dom-node
      const fieldFormGroup = ReactDOM.findDOMNode(this.fieldInput);

      $(fieldFormGroup).off('typeahead:change typeahead:selected');
    }
  }

  _getFilteredProps = () => {
    let result = {};
    for (const key in this.props) {
      if (key === 'onChange') {
        continue;
      }
      result[key] = this.props[key]
    }
    return result;
  };

  render() {

    return (
      <Input ref={(fieldInput) => { this.fieldInput = fieldInput; }}
             wrapperClassName="typeahead-wrapper"
             {...this._getFilteredProps()} />
    );
  }
}

export default TypeAheadFieldInput;