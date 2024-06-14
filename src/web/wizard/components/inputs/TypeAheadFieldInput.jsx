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

import React from 'react';
import { useEffect, useState } from 'react';

import { withTheme } from 'styled-components';
import { themePropTypes } from 'theme';
import isEqual from 'lodash/isEqual';

import PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import Immutable from 'immutable';
import $ from 'jquery';
import 'typeahead.js';

import { Input } from 'components/bootstrap';
import UniversalSearch from 'logic/search/UniversalSearch';
import ApiRoutes from 'routing/ApiRoutes';
import { qualifyUrl } from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

// TODO should try to remove this component.
//      this is taken and modified from the graylog TypeAheadFieldInput which has a bug when switching the theme
//      see https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/118
class TypeAheadFieldInput extends React.Component {
  static propTypes = {
    id: PropTypes.string.isRequired,
    label: PropTypes.string,
    autoFocus: PropTypes.bool,
    onChange: PropTypes.func,
    onBlur: PropTypes.func,
    error: PropTypes.string,
    theme: themePropTypes.isRequired,
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
            this.source = UniversalSearch.substringMatcher(data.fields, 'value', 6)
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
        if (onChange) {
          onChange(event);
        }
      });
    }
  }

  componentDidUpdate(prevProps) {
      if (!isEqual(this.props.theme, prevProps.theme)) {
          const fieldInput = $(this.fieldInput.getInputDOMNode());

          fieldInput.typeahead('destroy');
          fieldInput.typeahead(
              {
                  hint: true,
                  highlight: true,
                  minLength: 1,
              },
              {
                  name: 'fields',
                  displayKey: 'value',
                  source: this.source,
              },
          );
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
    let props = Immutable.fromJS(this.props);
    props = props.delete('onChange');

    return props.toJS();
  };

  render() {
    const { id, label, error, onBlur } = this.props;

    return (
      <Input id={id}
             ref={(fieldInput) => { this.fieldInput = fieldInput; }}
             label={label}
             onBlur={onBlur}
             error={error}
             wrapperClassName="typeahead-wrapper"
             {...this._getFilteredProps()} />
    );
  }
}

const TypeAheadFieldInputWithTheme = withTheme(TypeAheadFieldInput);

// TODO this is a hack, because it seems the callback provided to the TypeAheadFieldInput is called in a context
//      where it does not have access the the current value of other states
//      so, instead of directly calling the onChange callback, we set the event value
//      and then the effect will call the onChange callback in a context where it works...
//   => try to pinpoint the bug and report to Graylog/or try to use some other widget (react-bootstrap-typeahead)/or try to implement own
const TypeAheadFieldInputWrapper = ({id, type, required, name, defaultValue, onChange, autoFocus}) => {
    const [event, setEvent] = useState();

    const _onChangeWrapper = () => {
        if (event === undefined) {
            return;
        }
        onChange(event);
    }

    useEffect(_onChangeWrapper, [event]);

    return (
        <TypeAheadFieldInputWithTheme id={id} type={type} required={required} name={name} defaultValue={defaultValue} onChange={setEvent} autoFocus={autoFocus} />
    )
}

export default TypeAheadFieldInputWrapper;