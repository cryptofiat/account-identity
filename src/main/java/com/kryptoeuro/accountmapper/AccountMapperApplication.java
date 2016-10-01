package com.kryptoeuro.accountmapper;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AccountMapperApplication {
	@Value("${tomcat.ajp.port}")
	private int ajpPort;

	@Value("${tomcat.ajp.remoteauthentication}")
	private String remoteAuthentication;

	@Value("${tomcat.ajp.enabled}")
	private boolean tomcatAjpEnabled;

	public static void main(String[] args) {
		SpringApplication.run(AccountMapperApplication.class, args);
	}

    @RequestMapping(value = "/", produces = "text/plain")
    public String index() {
        return "OK";
    }

	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
		if (tomcatAjpEnabled)  {
			Connector ajpConnector = new Connector("AJP/1.3");
			ajpConnector.setProtocol("AJP/1.3");
			ajpConnector.setPort(ajpPort);
			ajpConnector.setSecure(false);
			ajpConnector.setAllowTrace(false);
			ajpConnector.setScheme("http");
			tomcat.addAdditionalTomcatConnectors(ajpConnector);
		}
		return tomcat;
	}
}
