import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import Reflux from 'reflux';
import {Col, Row, Button} from 'react-bootstrap';
import {Input} from 'components/bootstrap';
import {Select, Spinner} from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import StoreProvider from 'injection/StoreProvider';
import {FormattedMessage} from 'react-intl';
import ActionsProvider from 'injection/ActionsProvider';
import FieldRule from '../FieldRule';

const StreamsStore = StoreProvider.getStore('Streams');
const IndexSetsStore = StoreProvider.getStore('IndexSets');
const IndexSetsActions = ActionsProvider.getActions('IndexSets');

const STREAM = {
    matching_type: '',
    field_rule: [{field: '', type: '', value: ''}],
};

const FieldsCondition = createReactClass({
    displayName: 'FieldsCondition',

    mixins: [Reflux.connect(IndexSetsStore)],

    propTypes: {
        stream: PropTypes.object,
        onSaveStream: PropTypes.func,
        message: PropTypes.object,
        matchData: PropTypes.object,
    },   
    contextTypes: {
        intl: React.PropTypes.object.isRequired
    },
    getDefaultProps() {
        return {
            stream: STREAM,
        };
    },
    componentDidMount() {
        IndexSetsActions.list(false);
    },
    componentWillMount(){
        const messages = {
                add: this.context.intl.formatMessage({id: "wizard.add", defaultMessage: "Add"}),
                tryFieldsCondition: this.context.intl.formatMessage({id: "wizard.tryFieldsCondition", defaultMessage: "Try the fields condition"}),
            };
        this.setState({messages:messages});
    },
    componentWillReceiveProps(nextProps) {
        if(!_.isEqual(nextProps.matchData, this.props.matchData)){
            this.setState({matchData: nextProps.matchData});
        }
        if(!_.isEqual(nextProps.stream, this.props.stream) && nextProps.stream !== null){
            this.setState({stream: nextProps.stream});
            this.setState({matchData: undefined});
        }
    },
    getInitialState() {
        return {
            stream: ObjectUtils.clone(this.props.stream ? this.props.stream : STREAM),
            indexSets: undefined,
        };
    },
    _update() {
        this.props.onUpdate(this.state.stream);
    },
    _availableMatchingType() {
        return [
            {value: 'AND', label: <FormattedMessage id= "wizard.all" defaultMessage= "all" />},
            {value: 'OR', label: <FormattedMessage id= "wizard.atLeastOne" defaultMessage= "at least one" />},
        ];
    },
    _onMatchingTypeSelect(id) {
        this._updateStreamField('matching_type', id)
    },
    
    _updateStreamField(field, value) {
        const update = ObjectUtils.clone(this.state.stream);
        update[field] = value;
        this.setState({stream: update});
        this.props.onSaveStream(field, value);
    },
    
    _onUpdateFieldRuleSubmit(index, rule) {
        let update = ObjectUtils.clone(this.state.stream);
        update.field_rule[index] = rule;
        this.setState({stream: update});
        this.props.onSaveStream('field_rule', update['field_rule']);
    },

    _onDeleteFieldRuleSubmit(index) {
        let update = ObjectUtils.clone(this.state.stream);
        update['field_rule'].splice(index, 1);
        this.setState({stream: update});
        this.props.onSaveStream('field_rule', update['field_rule']);
    },
    _isRuleValid(rule){
        if (!(rule.field !== '' &&
                (rule.type === 5 || rule.type === -5) ||
                rule.field !== '' && rule.value !== '' &&
                (rule.type === 1 || rule.type === -1 ||
                    rule.type === 2 || rule.type === -2 ||
                    rule.type === 3 || rule.type === -3 ||
                    rule.type === 4 || rule.type === -4 ||
                    rule.type === 6 || rule.type === -6))) {
                return false;
            }
        return true;
    },
    _isFieldRulesValid() {
        for (let i = 0; i < this.state.stream.field_rule.length; i++) {
            if (!this._isRuleValid(this.state.stream.field_rule[i])){
                return false;
            }
        }
        return true;
    },
    _addFieldRule() {
        const rule = {field: '', type: '', value: ''};
        const update = ObjectUtils.clone(this.state.stream);
        update['field_rule'].push(rule);
        this.setState({stream: update});
        this.props.onSaveStream('field_rule', update['field_rule']);
    },
    _checkFieldsCondition(){
        if (this.props.message !== undefined) {
            /* Create temporary stream */
            let tempRules = [];
            for (let index = 0; index < this.state.stream.field_rule.length; index++) {
                if(this._isRuleValid(this.state.stream.field_rule[index])){
                    let inverted = false;
                    let type = this.state.stream.field_rule[index].type;
                    if(type < 0){
                        inverted = true;
                        type = -this.state.stream.field_rule[index].type;
                    }
                    let rule= { type: type,
                                value: this.state.stream.field_rule[index].value,
                                field: this.state.stream.field_rule[index].field,
                                inverted: inverted
                    }
                    tempRules.push(rule);
                }   
            }
            
            let tempStream = {  title: "WizardTempStream",
                            description: "Temporary stream to test wizard rules",
                            matching_type: this.state.stream.matching_type,
                            rules:tempRules,
                            index_set_id: this.state.indexSets[0].id
                        };
            
            StreamsStore.save(tempStream, stream => {
                this.setState({tempStreamID: stream.stream_id}); 
                /* Get the rules */
                StreamsStore.get(stream.stream_id, stream => {
                    
                    let newRules = [];
                    for (let index = 0; index < stream.rules.length; index++) {
                        let rule= { value: stream.rules[index].value,
                                field: stream.rules[index].field,
                                id: stream.rules[index].id,
                                type: stream.rules[index].inverted ? -stream.rules[index].type : stream.rules[index].type
                        }
                        newRules.push(rule);
                    }
                    const update = ObjectUtils.clone(this.state.stream);
                    update['field_rule'] = newRules;
                    this.setState({stream: update}); 
                    
                    /*Check the rules*/
                    StreamsStore.testMatch(stream.id, {message: this.props.message.fields}, (resultData) => {
                        this.setState({matchData: resultData});
                        
                        /* Remove temporary stream */
                        StreamsStore.remove(this.state.tempStreamID); 
                    });
                });
                
            } );

        } else {
            this.setState({matchData: undefined});
        }
    },
    _getMatchDataColor(){
        return (this.state.matchData.matches ? '#dff0d8' : '#f2dede');
    },
    _isLoading() {
        return (!this.state.stream);
    },
    
    render() {
        if (this._isLoading()) {
            return (
                <div style={{marginLeft: 10}}>
                    <Spinner/>
                </div>
            );
        }
        const color = (this.state.matchData ? this._getMatchDataColor() : '#FFFFFF');
        let listFieldRule = this.state.stream.field_rule.map((rule) =>
            <div><FieldRule rule={rule} onUpdate={this._onUpdateFieldRuleSubmit} onDelete={this._onDeleteFieldRuleSubmit}
                           index={this.state.stream['field_rule'].indexOf(rule)}
                           matchData={this.state.matchData}/>
                <br/>
            </div>
        );
        
        return (
        <Row>
            <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                <label className="pull-right" ><FormattedMessage id= "wizard.fieldsCondition" defaultMessage= "Fields Condition" /></label>
            </Col>
            <Col md={10}>
                <label><FormattedMessage id= "wizard.messagesMatch" defaultMessage= "Messages must match" /></label>
                <Input ref="matching_type" id="matching_type" name="matching_type" required>
                    <Select style={{backgroundColor: color}}
                        autosize={false}
                        required
                        value={this.state.stream.matching_type}
                        options={this._availableMatchingType()}
                        matchProp="value"
                        onChange={this._onMatchingTypeSelect}
                        placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                    />
                </Input>
                <label>&nbsp; </label>
                <label><FormattedMessage id= "wizard.followingRules" defaultMessage= "of the following rules:" /></label>
                {' '}
                <Button disabled={!this.props.message} onClick={this._checkFieldsCondition} bsStyle="info" title={this.state.messages.tryFieldsCondition} style={{marginLeft: '10em'}}>
                    <FormattedMessage id ="wizard.try" defaultMessage="Try" />
                </Button>
                <br/><br/>
                {listFieldRule}  
                <Button onClick={this._addFieldRule} bsStyle="info" title={this.state.messages.add}><i className="fa fa-plus-circle" style={{ fontSize: '18px' }} /></Button>
            </Col>
        </Row>
        );
        
    },
});

export default FieldsCondition;
