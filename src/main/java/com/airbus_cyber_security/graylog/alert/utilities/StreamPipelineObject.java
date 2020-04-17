package com.airbus_cyber_security.graylog.alert.utilities;

import com.airbus_cyber_security.graylog.alert.FieldRuleImpl;
import org.graylog2.plugin.streams.Stream;

import java.util.List;

public class StreamPipelineObject {
    public Stream getStream() {
        return stream;
    }

    public String getPipelineID() {
        return pipelineID;
    }

    public String getPipelineRuleID() {
        return pipelineRuleID;
    }

    public List<FieldRuleImpl> getListPipelineFieldRule() {
        return listPipelineFieldRule;
    }

    Stream stream;
    String pipelineID;
    String pipelineRuleID;
    List<FieldRuleImpl> listPipelineFieldRule;
    public StreamPipelineObject(Stream stream, String pipelineID, String pipelineRuleID, List<FieldRuleImpl> listPipelineFieldRule)
    {
        this.stream = stream;
        this.pipelineID = pipelineID;
        this.pipelineRuleID = pipelineRuleID;
        this.listPipelineFieldRule = listPipelineFieldRule;
    }
}
