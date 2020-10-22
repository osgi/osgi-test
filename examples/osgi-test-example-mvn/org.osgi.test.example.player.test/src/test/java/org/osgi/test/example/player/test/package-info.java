// We require a Player service to test
@Requirement(namespace = ServiceNamespace.SERVICE_NAMESPACE, filter = "("
	+ ServiceNamespace.CAPABILITY_OBJECTCLASS_ATTRIBUTE + "=org.osgi.test.example.api.Player)")
// We provide the Ball service the Player impl will need
@Capability(namespace = ServiceNamespace.SERVICE_NAMESPACE, attribute = ServiceNamespace.CAPABILITY_OBJECTCLASS_ATTRIBUTE
	+ "=org.osgi.test.example.api.Ball")
package org.osgi.test.example.player.test;

import org.osgi.annotation.bundle.Capability;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.namespace.service.ServiceNamespace;
