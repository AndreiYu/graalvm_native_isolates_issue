package org.acme.getting.started;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/calculate")
public class CalculatorResource {

	@Inject
	CalculateService service;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String calculate(@QueryParam("n") int n) {
		return String.valueOf(service.calculate(n));
	}
}