/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.authentication.oauth2.ecm.internal;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.everit.authentication.oauth2.OAuth2UserIdResolver;
import org.everit.authentication.oauth2.ecm.OAuth2Constants;
import org.everit.authentication.oauth2.ri.AccessTokenResponse;
import org.everit.authentication.oauth2.ri.OAuth2Communicator;
import org.everit.authentication.oauth2.ri.OAuth2ConfigurationDTO;
import org.everit.authentication.oauth2.ri.OAuth2Services;
import org.everit.authentication.oauth2.ri.internal.OAuth2CommunicatorImpl;
import org.everit.authentication.oauth2.ri.internal.OAuth2ResourceIdResolverImpl;
import org.everit.osgi.ecm.annotation.Activate;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.ConfigurationPolicy;
import org.everit.osgi.ecm.annotation.Service;
import org.everit.osgi.ecm.annotation.ServiceRef;
import org.everit.osgi.ecm.annotation.attribute.StringAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttributes;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.everit.osgi.props.PropertyManager;
import org.everit.osgi.querydsl.support.QuerydslSupport;
import org.everit.osgi.resource.ResourceService;
import org.everit.osgi.resource.resolver.ResourceIdResolver;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.osgi.framework.Constants;

import aQute.bnd.annotation.headers.ProvideCapability;

/**
 * Create and Provide OAuth2 services.
 */
@Component(configurationPolicy = ConfigurationPolicy.FACTORY)
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@StringAttributes({
    @StringAttribute(attributeId = Constants.SERVICE_DESCRIPTION,
        defaultValue = OAuth2Constants.DEFAULT_SERVICE_DESCRIPTION)
})
@Service(value = { OAuth2Communicator.class, ResourceIdResolver.class, OAuth2UserIdResolver.class,
    OAuth2Services.class })
public class OAuth2Component implements OAuth2Services {

  private String authorizationEndpoint;

  private String clientId;

  private String clientSecret;

  private OAuth2Communicator oAuth2Communicator;

  private OAuth2UserIdResolver oauth2UserIdResolverWrapped;

  private PropertyManager propertyManager;

  private String providerName;

  private QuerydslSupport querydslSupport;

  private String redirectEndpoint;

  private ResourceIdResolver resourceIdResolver;

  private ResourceService resourceService;

  private String scope;

  private String tokenEndpoint;

  private TransactionHelper transactionHelper;

  /**
   * Component activator method.
   */
  @Activate
  public void activate() {
    resourceIdResolver = new OAuth2ResourceIdResolverImpl(
        propertyManager, querydslSupport, resourceService, transactionHelper, providerName);

    OAuth2ConfigurationDTO oauth2Configuration = new OAuth2ConfigurationDTO()
        .authorizationEndpoint(authorizationEndpoint)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .providerName(providerName)
        .redirectEndpoint(redirectEndpoint)
        .scope(scope)
        .tokenEndpoint(tokenEndpoint);
    oAuth2Communicator = new OAuth2CommunicatorImpl(oauth2Configuration);
  }

  @Override
  public AccessTokenResponse getAccessToken(final HttpServletRequest req) {
    return oAuth2Communicator.getAccessToken(req);
  }

  @Override
  public String getAuthorizationUriWithParams() {
    return oAuth2Communicator.getAuthorizationUriWithParams();
  }

  @Override
  public Optional<Long> getResourceId(final String uniqueIdentifier) {
    return resourceIdResolver.getResourceId(uniqueIdentifier);
  }

  @Override
  public String getUniqueUserId(final String tokenType, final String accessToken,
      final Long accessTokenExpiresIn, final String refreshToken, final String scope) {
    return oauth2UserIdResolverWrapped.getUniqueUserId(tokenType, accessToken, accessTokenExpiresIn,
        refreshToken, scope);
  }

  @StringAttribute(attributeId = OAuth2Constants.PROP_AUTHORITZATION_ENDPOINT,
      defaultValue = OAuth2Constants.DEFAULT_AUTHORIZATION_ENDPOINT)
  public void setAuthorizationEndpoint(final String authorizationEndpoint) {
    this.authorizationEndpoint = authorizationEndpoint;
  }

  @StringAttribute(attributeId = OAuth2Constants.PROP_CLIENT_ID,
      defaultValue = OAuth2Constants.DEFAULT_CLIENT_ID)
  public void setClientId(final String clientId) {
    this.clientId = clientId;
  }

  @StringAttribute(attributeId = OAuth2Constants.PROP_CLIENT_SECRET,
      defaultValue = OAuth2Constants.DEFAULT_CLIENT_SECRET)
  public void setClientSecret(final String clientSecret) {
    this.clientSecret = clientSecret;
  }

  @ServiceRef(attributeId = OAuth2Constants.SERVICE_OAUTH2_USER_ID_RESOLVER_WRAPPED,
      defaultValue = "")
  public void setOauth2UserIdResolverWrapped(final OAuth2UserIdResolver oauth2UserIdResolver) {
    oauth2UserIdResolverWrapped = oauth2UserIdResolver;
  }

  @ServiceRef(attributeId = OAuth2Constants.SERVICE_PROPERTY_MANAGER,
      defaultValue = "")
  public void setPropertyManager(final PropertyManager propertyManager) {
    this.propertyManager = propertyManager;
  }

  @StringAttribute(attributeId = OAuth2Constants.PROP_PROVIDER_NAME,
      defaultValue = OAuth2Constants.DEFAULT_PROVIDER_NAME)
  public void setProviderName(final String providerName) {
    this.providerName = providerName;
  }

  @ServiceRef(attributeId = OAuth2Constants.SERVICE_QUERYDSL_SUPPORT,
      defaultValue = "")
  public void setQuerydslSupport(final QuerydslSupport querydslSupport) {
    this.querydslSupport = querydslSupport;
  }

  @StringAttribute(attributeId = OAuth2Constants.PROP_REDIRECT_ENDPOINT,
      defaultValue = OAuth2Constants.DEFAULT_REDIRECT_ENDPOINT)
  public void setRedirectEndpoint(final String redirectEndpoint) {
    this.redirectEndpoint = redirectEndpoint;
  }

  @ServiceRef(attributeId = OAuth2Constants.SERVICE_RESOURCE_SERVICE,
      defaultValue = "")
  public void setResourceService(final ResourceService resourceService) {
    this.resourceService = resourceService;
  }

  @StringAttribute(attributeId = OAuth2Constants.PROP_SCOPE,
      defaultValue = OAuth2Constants.DEFAULT_SCOPE)
  public void setScope(final String scope) {
    this.scope = scope;
  }

  @StringAttribute(attributeId = OAuth2Constants.PROP_TOKEN_ENDPOINT,
      defaultValue = OAuth2Constants.DEFAULT_TOKEN_ENDPOINT)
  public void setTokenEndpoint(final String tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
  }

  @ServiceRef(attributeId = OAuth2Constants.SERVICE_TRANSACTION_HELPER,
      defaultValue = "")
  public void setTransactionHelper(final TransactionHelper transactionHelper) {
    this.transactionHelper = transactionHelper;
  }

}