package propets.filters.lfconverter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import propets.configuration.lfconverter.ConverterConfiguration;

@Service
@Order(10)
public class XTokenFilter implements Filter {
	
	@Autowired
	ConverterConfiguration configuration;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();
		String method = request.getMethod();
		String xToken = request.getHeader("X-token");
		String url = configuration.getUrlCheckToken();
		if (!checkPointCut(path, method)) {
			if (xToken == null) {
				response.sendError(401);
				return;
			}
			RestTemplate restTemplate = new RestTemplate();
			URI urlCheckTokenServ = null;
			try {
				urlCheckTokenServ = new URI(url);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			HttpHeaders headers = new HttpHeaders();
			headers.add("X-token", xToken);
			RequestEntity<String> requestCheckToken = new RequestEntity<>(headers, HttpMethod.GET, urlCheckTokenServ);
			ResponseEntity<String> responseCheckToken;
			try {
				responseCheckToken = restTemplate.exchange(requestCheckToken, String.class);
			} catch (RestClientException e) {
				response.sendError(409);
				return;
			}
			if (responseCheckToken.getStatusCode().equals(HttpStatus.CONFLICT)) {
				response.sendError(409);
				return;
			}
			response.addHeader("X-token", responseCheckToken.getHeaders().getFirst("X-token"));
		}
		chain.doFilter(request, response);

	}

	private boolean checkPointCut(String path, String method) {
		boolean check = path.endsWith("/posts/allid") && method.equalsIgnoreCase("Post");
		return check;
	}

}
