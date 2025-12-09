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
package com.airbus_cyber_security.graylog.wizard.fields;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Empty implementation of Subject
 */
// TODO: Remove this when Graylog will allow search without Subject
public class EmptySubject implements Subject {
    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public PrincipalCollection getPrincipals() {
        return null;
    }

    @Override
    public boolean isPermitted(String permission) {
        return true;
    }

    @Override
    public boolean isPermitted(Permission permission) {
        return true;
    }

    @Override
    public boolean[] isPermitted(String... permissions) {
        return new boolean[0];
    }

    @Override
    public boolean[] isPermitted(List<Permission> permissions) {
        return new boolean[0];
    }

    @Override
    public boolean isPermittedAll(String... permissions) {
        return false;
    }

    @Override
    public boolean isPermittedAll(Collection<Permission> permissions) {
        return false;
    }

    @Override
    public void checkPermission(String permission) throws AuthorizationException {

    }

    @Override
    public void checkPermission(Permission permission) throws AuthorizationException {

    }

    @Override
    public void checkPermissions(String... permissions) throws AuthorizationException {

    }

    @Override
    public void checkPermissions(Collection<Permission> permissions) throws AuthorizationException {

    }

    @Override
    public boolean hasRole(String roleIdentifier) {
        return false;
    }

    @Override
    public boolean[] hasRoles(List<String> roleIdentifiers) {
        return new boolean[0];
    }

    @Override
    public boolean hasAllRoles(Collection<String> roleIdentifiers) {
        return false;
    }

    @Override
    public void checkRole(String roleIdentifier) throws AuthorizationException {

    }

    @Override
    public void checkRoles(Collection<String> roleIdentifiers) throws AuthorizationException {

    }

    @Override
    public void checkRoles(String... roleIdentifiers) throws AuthorizationException {

    }

    @Override
    public void login(AuthenticationToken token) throws AuthenticationException {

    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public boolean isRemembered() {
        return false;
    }

    @Override
    public Session getSession() {
        return null;
    }

    @Override
    public Session getSession(boolean create) {
        return null;
    }

    @Override
    public void logout() {

    }

    @Override
    public <V> V execute(Callable<V> callable) throws ExecutionException {
        return null;
    }

    @Override
    public void execute(Runnable runnable) {

    }

    @Override
    public <V> Callable<V> associateWith(Callable<V> callable) {
        return null;
    }

    @Override
    public Runnable associateWith(Runnable runnable) {
        return null;
    }

    @Override
    public void runAs(PrincipalCollection principals) throws NullPointerException, IllegalStateException {

    }

    @Override
    public boolean isRunAs() {
        return false;
    }

    @Override
    public PrincipalCollection getPreviousPrincipals() {
        return null;
    }

    @Override
    public PrincipalCollection releaseRunAs() {
        return null;
    }
}
