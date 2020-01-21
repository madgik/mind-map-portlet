package gr.uoa.di.madgik.visualization.service;

import static org.gcube.resources.discovery.icclient.ICFactory.clientFor;
import static org.gcube.resources.discovery.icclient.ICFactory.queryFor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gr.uoa.di.madgik.visualization.exceptions.ServiceDiscoveryException;
import org.gcube.common.resources.gcore.GCoreEndpoint;
import org.gcube.common.resources.gcore.GCoreEndpoint.Profile.Endpoint;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.resources.discovery.client.api.DiscoveryClient;
import org.gcube.resources.discovery.client.queries.api.SimpleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceDiscovery {

	private static Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);

	public static Set<String> discoverServiceEndpoints(String scope, ServiceProfile serviceProfile) throws ServiceDiscoveryException {
		Set<String> endpoints = null;

		try {
			ScopeProvider.instance.set(scope);

			endpoints = queryServiceEndpoints(serviceProfile);

			endpoints.forEach(e -> logger.info("Discovered endpoint: " + e));

			if (endpoints.isEmpty()) {
				throw new ServiceDiscoveryException("No Endpoints available.");
			}

			logger.debug("Managed to discover " + endpoints.size() + " " + serviceProfile.getServiceName() + " endpoint(s)");
		} catch (Exception e) {
			logger.error("Problem while discovering endpoints", e);
			throw new ServiceDiscoveryException("Failed to discover any " + serviceProfile.getServiceClass() + "/"
					+ serviceProfile.getServiceName() + " endpoint for scope " + scope, e);
		}

		return endpoints;
	}

	private static Set<String> queryServiceEndpoints(ServiceProfile serviceProfile) throws Exception {
		SimpleQuery query = queryFor(GCoreEndpoint.class);

		query.addCondition("$resource/Profile/ServiceClass/text() eq '" + serviceProfile.getServiceClass() + "'")
				.addCondition("$resource/Profile/ServiceName/text() eq '" + serviceProfile.getServiceName() + "'");

		DiscoveryClient<GCoreEndpoint> client = clientFor(GCoreEndpoint.class);

		List<GCoreEndpoint> eprs = client.submit(query);

		Set<String> clusterHosts = new HashSet<String>();

		if (eprs != null) {
			for (GCoreEndpoint epr : eprs) {
				if (!"ready".equals(epr.profile().deploymentData().status().toLowerCase())) {
					continue;
				}

				for (Endpoint e : epr.profile().endpointMap().values().toArray(new Endpoint[epr.profile().endpointMap().values().size()])) {
					String endpoint = e.uri().toString();

					if (serviceProfile.hasPathContains() && !endpoint.contains(serviceProfile.getPathContains())) {
						continue;
					}

					if (serviceProfile.hasPathEndsWith() && !endpoint.endsWith(serviceProfile.getPathEndsWith())) {
						continue;
					}

					if (serviceProfile.hasPathNotEndsWith() && endpoint.endsWith(serviceProfile.getPathNotEndsWith())) {
						continue;
					}

					if (!endpoint.endsWith("/")) {
						endpoint += "/";
					}

					clusterHosts.add(endpoint);

					logger.debug("Found Service Endpoint: " + endpoint);
				}
			}
		}

		return clusterHosts;
	}
}
