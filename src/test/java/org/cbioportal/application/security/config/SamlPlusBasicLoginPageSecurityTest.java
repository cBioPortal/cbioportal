/*
 * Copyright (c) 2026 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.application.security.config;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "authenticate=saml_plus_basic")
class SamlPlusBasicLoginPageSecurityTest extends AbstractSamlLoginPageSecurityTest {}
