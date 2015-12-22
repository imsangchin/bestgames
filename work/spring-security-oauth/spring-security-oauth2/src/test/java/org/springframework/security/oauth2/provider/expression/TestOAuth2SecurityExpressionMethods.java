/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.oauth2.provider.expression;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.BaseClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.RequestTokenFactory;
import org.springframework.security.oauth2.provider.OAuth2Request;

/**
 * @author Dave Syer
 * 
 */
public class TestOAuth2SecurityExpressionMethods {

	@Test
	public void testOauthClient() throws Exception {
		AuthorizationRequest request = new AuthorizationRequest("foo",
				Collections.singleton("read"));
		request
				.setResourceIdsAndAuthoritiesFromClientDetails(new BaseClientDetails("foo", "", "", "client_credentials", "ROLE_CLIENT"));
		Authentication userAuthentication = null;
		
		OAuth2Request clientAuthentication = RequestTokenFactory.createOAuth2Request(request.getRequestParameters(), request.getClientId(), request.getAuthorities(), request.isApproved(), request.getScope(), request.getResourceIds(),
				request.getRedirectUri(), request.getResponseTypes(), request.getExtensions());
		
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientAuthentication, userAuthentication);
		assertTrue(new OAuth2SecurityExpressionMethods(oAuth2Authentication, true).clientHasAnyRole("ROLE_CLIENT"));
	}

	@Test
	public void testScopes() throws Exception {
		OAuth2Request clientAuthentication = RequestTokenFactory.createOAuth2Request(null, "foo", null, false, Collections.singleton("read"), null, null, null, null);

		Authentication userAuthentication = null;
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientAuthentication, userAuthentication);
		OAuth2SecurityExpressionMethods root = new OAuth2SecurityExpressionMethods(oAuth2Authentication, false);
		assertTrue(root.hasAnyScope("read"));
	}

	@Test
	public void testScopesFalse() throws Exception {
		OAuth2Request clientAuthentication = RequestTokenFactory.createOAuth2Request(null, "foo", null, false, Collections.singleton("read"), null, null, null, null);

		Authentication userAuthentication = null;
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientAuthentication, userAuthentication);
		OAuth2SecurityExpressionMethods root = new OAuth2SecurityExpressionMethods(oAuth2Authentication, false);
		assertFalse(root.hasAnyScope("write"));
	}

	@Test(expected = AccessDeniedException.class)
	public void testScopesWithException() throws Exception {
		OAuth2Request clientAuthentication = RequestTokenFactory.createOAuth2Request(null, "foo", null, false, Collections.singleton("read"), null, null, null, null);

		Authentication userAuthentication = null;
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientAuthentication, userAuthentication);
		assertTrue(new OAuth2SecurityExpressionMethods(oAuth2Authentication, true).hasAnyScope("foo"));
	}

	@Test(expected = AccessDeniedException.class)
	public void testInsufficientScope() throws Exception {
		OAuth2Request clientAuthentication = RequestTokenFactory.createOAuth2Request(null, "foo", null, false, Collections.singleton("read"), null, null, null, null);

		Authentication userAuthentication = null;
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientAuthentication, userAuthentication);
		assertFalse(new OAuth2SecurityExpressionMethods(oAuth2Authentication, true).hasAnyScope("foo"));
		assertFalse(new OAuth2SecurityExpressionMethods(oAuth2Authentication, true).sufficientScope(false));
	}

	@Test
	public void testSufficientScope() throws Exception {
		OAuth2Request clientAuthentication = RequestTokenFactory.createOAuth2Request(null, "foo", null, false, Collections.singleton("read"), null, null, null, null);

		Authentication userAuthentication = null;
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientAuthentication, userAuthentication);
		assertTrue(new OAuth2SecurityExpressionMethods(oAuth2Authentication, true).hasAnyScope("read"));
		assertTrue(new OAuth2SecurityExpressionMethods(oAuth2Authentication, true).sufficientScope(true));
	}

	@Test
	public void testSufficientScopeWithNoPreviousScopeDecision() throws Exception {
		OAuth2Request clientAuthentication = RequestTokenFactory.createOAuth2Request(null, "foo", null, true, Collections.singleton("read"), null, null, null, null);

		Authentication userAuthentication = null;
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientAuthentication, userAuthentication);
		assertTrue(new OAuth2SecurityExpressionMethods(oAuth2Authentication, true).isClient());
		assertFalse(new OAuth2SecurityExpressionMethods(oAuth2Authentication, true).sufficientScope(false));
	}

	@Test
	public void testNonOauthClient() throws Exception {
		Authentication clientAuthentication = new UsernamePasswordAuthenticationToken("foo", "bar");
		assertFalse(new OAuth2SecurityExpressionMethods(clientAuthentication, true).clientHasAnyRole("ROLE_USER"));
	}

	@Test
	public void testClientOnly() throws Exception {
		OAuth2Request clientAuthentication = RequestTokenFactory.createOAuth2Request(null, "foo", null, true, Collections.singleton("read"), null, null, null, null);

		Authentication userAuthentication = new UsernamePasswordAuthenticationToken("foo", "bar",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientAuthentication, userAuthentication);
		assertFalse(new OAuth2SecurityExpressionMethods(oAuth2Authentication, true).isClient());
		assertTrue(new OAuth2SecurityExpressionMethods(new OAuth2Authentication(clientAuthentication, null), true).isClient());
	}

	@Test
	public void testOAuthUser() throws Exception {
		OAuth2Request clientAuthentication = RequestTokenFactory.createOAuth2Request(null, "foo", null, true, Collections.singleton("read"), null, null, null, null);

		Authentication userAuthentication = new UsernamePasswordAuthenticationToken("foo", "bar",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientAuthentication, userAuthentication);
		assertTrue(new OAuth2SecurityExpressionMethods(oAuth2Authentication, true).isUser());
		assertFalse(new OAuth2SecurityExpressionMethods(new OAuth2Authentication(clientAuthentication, null), true)
				.isUser());
	}

}
