package org.example.progettosettimanale6.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class LlmClientConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmClientConfig.class);

    @Bean
    RestClient llmClient(RestClient.Builder builder,
                          @Value("${app.llm.base-url}") String baseUrl,
                          @Value("${app.llm.api-key:}") String apiKey,
                          @Value("${app.llm.site-url:}") String siteUrl,
                          @Value("${app.llm.site-name:}") String siteName) {

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("app.llm.api-key non impostata: le chiamate riceveranno 401");
        }

        log.info("client LLM verso {}", baseUrl);

        var costruttore = builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey);

        if (siteUrl != null && !siteUrl.isBlank()) {
            costruttore.defaultHeader("HTTP-Referer", siteUrl);
        }
        if (siteName != null && !siteName.isBlank()) {
            costruttore.defaultHeader("X-Title", siteName);
        }

        return costruttore.build();
    }
}
