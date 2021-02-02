package org.osgi.test.assertj.servicereference;

public class ServiceReferenceAssertTest extends AbstractServiceReferenceAssertTest<ServiceReferenceAssert> {
	public ServiceReferenceAssertTest() {
		super(ServiceReferenceAssert::assertThat);
	}
}
