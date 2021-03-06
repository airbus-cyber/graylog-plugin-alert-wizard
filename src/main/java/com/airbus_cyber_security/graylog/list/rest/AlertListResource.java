/*
 * graylog-plugin-alert-wizard Source Code
 * Copyright (C) 2018-2020 - Airbus CyberSecurity (SAS) - All rights reserved
 *
 * This file is part of the graylog-plugin-alert-wizard GPL Source Code.
 *
 * graylog-plugin-alert-wizard Source Code is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.airbus_cyber_security.graylog.list.rest;

import com.airbus_cyber_security.graylog.audit.AlertWizardAuditEventTypes;
import com.airbus_cyber_security.graylog.config.rest.AlertWizardConfig;
import com.airbus_cyber_security.graylog.config.rest.ImportPolicyType;
import com.airbus_cyber_security.graylog.list.AlertList;
import com.airbus_cyber_security.graylog.list.AlertListImpl;
import com.airbus_cyber_security.graylog.list.AlertListService;
import com.airbus_cyber_security.graylog.list.bundles.AlertListExporter;
import com.airbus_cyber_security.graylog.list.bundles.ExportAlertList;
import com.airbus_cyber_security.graylog.list.bundles.ExportAlertListRequest;
import com.airbus_cyber_security.graylog.list.rest.models.requests.AlertListRequest;
import com.airbus_cyber_security.graylog.list.rest.models.requests.CloneAlertListRequest;
import com.airbus_cyber_security.graylog.list.rest.models.responses.GetAlertList;
import com.airbus_cyber_security.graylog.list.rest.models.responses.GetListAlertList;
import com.airbus_cyber_security.graylog.list.utilities.AlertListUtilsService;
import com.airbus_cyber_security.graylog.permissions.AlertRuleRestPermissions;
import com.codahale.metrics.annotation.Timed;
import com.mongodb.MongoException;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.shared.rest.resources.RestResource;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Api(value = "Wizard/Lists", description = "Management of Wizard lists.")
@Path("/lists")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AlertListResource extends RestResource implements PluginRestResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlertListResource.class);

    private static final String ENCODING = "UTF-8";
    private static final String TITLE = "title";

    private final AlertListService alertListService;
    private final ClusterConfigService clusterConfigService;
    private final AlertListExporter alertListExporter;
    private final AlertListUtilsService alertListUtilsService;


    @Inject
    public AlertListResource(AlertListService alertListService,
                             ClusterConfigService clusterConfigService) {
        this.alertListService = alertListService;
        this.clusterConfigService = clusterConfigService;
        this.alertListUtilsService = new AlertListUtilsService(alertListService);
        this.alertListExporter = new AlertListExporter(alertListService);
    }

    @GET
    @Timed
    @ApiOperation(value = "AlertListDisplay all existing lists")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    public GetListAlertList list() {
        final List<AlertList> lists = alertListService.all();
        return GetListAlertList.create(lists);
    }

    @GET
    @Path("/{title}")
    @Timed
    @ApiOperation(value = "Get a list")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "List not found."),
    })
    public GetAlertList get(@ApiParam(name = TITLE, required = true) @PathParam(TITLE) String title)
            throws UnsupportedEncodingException, NotFoundException {
        String listTitle = java.net.URLDecoder.decode(title, ENCODING);

        final AlertList list = alertListService.load(listTitle);
        if(list == null) {
            throw new NotFoundException("List <" + listTitle + "> not found!");
        }
        return GetAlertList.create(list);
    }

    private String checkImportPolicyAndGetTitle(String title){
        String listTitle = title;
        if(alertListService.isPresent(listTitle)) {
            final AlertWizardConfig configGeneral = clusterConfigService.get(AlertWizardConfig.class);
            ImportPolicyType importPolicy = configGeneral.accessImportPolicy();
            if(importPolicy != null && importPolicy.equals(ImportPolicyType.RENAME)) {
                String newListTitle;
                int i = 1;
                do{
                    newListTitle = listTitle+"("+i+")";
                    i++;
                }while (alertListService.isPresent(newListTitle));
                listTitle = newListTitle;
            }else if(importPolicy != null && importPolicy.equals(ImportPolicyType.REPLACE)) {
                try {
                    this.delete(listTitle);
                } catch (MongoException | UnsupportedEncodingException e) {
                    LOG.error("Failed to replace list");
                    throw new BadRequestException("Failed to replace alert list.");
                }
            }else{
                LOG.error("Failed to create alert list : list title already exist");
                throw new BadRequestException("Failed to create list : list title already exist.");
            }
        }
        return listTitle;
    }

    @POST
    @Timed
    @ApiOperation(value = "Create a list")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    public Response create(@ApiParam(name = "JSON body", required = true) @Valid @NotNull AlertListRequest request)
            throws ValidationException, BadRequestException{

        alertListUtilsService.checkIsValidRequest(request);
        String listTitle = checkImportPolicyAndGetTitle(request.getTitle());

        alertListService.create(AlertListImpl.create(
                listTitle,
                DateTime.now(),
                getCurrentUser().getName(),
                DateTime.now(),
                request.getDescription(),
                0,
                request.getLists()));

        return Response.accepted().build();
    }

    @PUT
    @Path("/{title}")
    @Timed
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_UPDATE)
    @ApiOperation(value = "Update a list")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_UPDATE)
    public Response update(@ApiParam(name = TITLE, required = true)
                           @PathParam(TITLE) String title,
                           @ApiParam(name = "JSON body", required = true) @Valid @NotNull AlertListRequest request
    ) throws UnsupportedEncodingException, NotFoundException, ValidationException, ConfigurationException {

        alertListUtilsService.checkIsValidRequest(request);

        AlertList oldAlert = alertListService.load(title);
        String listTitle = request.getTitle();

        alertListService.update(java.net.URLDecoder.decode(title, ENCODING),
                AlertListImpl.create(
                        listTitle,
                        oldAlert.getCreatedAt(),
                        getCurrentUser().getName(),
                        DateTime.now(),
                        request.getDescription(),
                        request.getUsage(),
                        request.getLists()));

        return Response.accepted().build();
    }

    @POST
    @Path("/{title}/Clone")
    @Timed
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiOperation(value = "Clone a list")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    public Response clone(@ApiParam(name = TITLE, required = true)
                          @PathParam(TITLE) String title,
                          @ApiParam(name = "JSON body", required = true) @Valid @NotNull CloneAlertListRequest request
    ) throws NotFoundException, ValidationException {

        AlertList sourcelist = alertListService.load(title);

        alertListService.create(AlertListImpl.create(
                request.getTitle(),
                DateTime.now(),
                getCurrentUser().getName(),
                DateTime.now(),
                request.getDescription(),
                0,
                sourcelist.getLists()));

        return Response.accepted().build();
    }

    @DELETE
    @Path("/{title}")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_DELETE)
    @ApiOperation(value = "Delete a list")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "List not found."),
            @ApiResponse(code = 400, message = "Invalid ObjectId.")
    })
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_DELETE)
    public void delete(@ApiParam(name = TITLE, required = true)
                       @PathParam(TITLE) String title
    ) throws MongoException, UnsupportedEncodingException {
        String listTitle = java.net.URLDecoder.decode(title, ENCODING);

        try{
            AlertList alertList = alertListService.load(listTitle);
            if(alertList.getUsage() <= 0){
                alertListService.destroy(listTitle);
            }else{
                throw new javax.ws.rs.BadRequestException("List " + listTitle + " used in alert rules");
            }
        }catch(NotFoundException e){
            throw new javax.ws.rs.NotFoundException("Cannot find list " + listTitle );
        }

    }

    @POST
    @Path("/export")
    @Timed
    @ApiOperation(value = "Export lists")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_READ)
    public List<ExportAlertList> getExportAlertList(@ApiParam(name = "JSON body", required = true) @Valid @NotNull ExportAlertListRequest request) {
        LOG.debug("List titles : " + request.getTitles());
        return alertListExporter.export(request.getTitles());
    }

    public void importAlertList(ExportAlertList alertList)
            throws ValidationException, BadRequestException{
        String listTitle = checkImportPolicyAndGetTitle(alertList.getTitle());

        alertListService.create(AlertListImpl.create(
                listTitle,
                DateTime.now(),
                getCurrentUser().getName(),
                DateTime.now(),
                alertList.getDescription(),
                0,
                alertList.getLists()));
    }

    @PUT
    @Path("/import")
    @Timed
    @ApiOperation(value = "Import a list")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    public Response importAlertLists (@ApiParam(name = "JSON body", required = true) @Valid @NotNull List<ExportAlertList> request) {
        Response responses = Response.accepted().build();

        for (ExportAlertList alertList : request) {
            if(!alertListService.isValidImportRequest(alertList)){
                LOG.error("Invalid list:" + alertList.getTitle() );
            }else {
                try {
                    importAlertList(alertList);
                } catch (Exception e) {
                    LOG.error("Cannot create list "+ alertList.getTitle() + ": ", e.getMessage());
                    responses = Response.serverError().build();
                }
            }
        }

        return responses;
    }
}
