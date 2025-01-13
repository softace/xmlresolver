package org.xmlresolver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xmlresolver.spi.SchemeResolver;
import org.xmlresolver.utils.URIUtils;

import javax.xml.transform.Source;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.fail;

public class SchemeResolverTest {
    public static final String catalog1 = "src/test/resources/schemecat.xml";

    @Test
    public void issue210_uri_schemes() {
        XMLResolverConfiguration config = new XMLResolverConfiguration(Collections.emptyList());
        config.setFeature(ResolverFeature.ALWAYS_RESOLVE, true);
        XMLResolver localresolver = new XMLResolver(config);

        try {
            String baseURI = URIUtils.cwd().resolve("test-scheme://path/to/file.xml").toString();
            Source result = localresolver.getURIResolver().resolve("", baseURI);
            Assertions.assertNotNull(result);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void issue210_uri_schemes_catalog() {
        XMLResolverConfiguration config = new XMLResolverConfiguration(Collections.singletonList(catalog1));
        config.setFeature(ResolverFeature.ALWAYS_RESOLVE, false);
        XMLResolver localresolver = new XMLResolver(config);

        try {
            String baseURI = URIUtils.cwd().resolve("https://example.com/sample/1.0/uri.dtd").toString();
            Source result = localresolver.getURIResolver().resolve("", baseURI);
            Assertions.assertNotNull(result);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void issue210_directregistration() {
        XMLResolverConfiguration config = new XMLResolverConfiguration(Collections.singletonList(catalog1));
        XMLResolver localresolver = new XMLResolver(config);

        ((XMLResolverConfiguration) localresolver.getConfiguration()).registerSchemeResolver("test-scheme2", new MySchemeResolver());

        try {
            String baseURI = URIUtils.cwd().resolve("test-scheme2:/whatever").toString();
            Source result = localresolver.getURIResolver().resolve("", baseURI);
            Assertions.assertNotNull(result);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    private static class MySchemeResolver implements SchemeResolver {
        @Override
        public ResourceResponse getResource(ResourceRequest request, URI uri) throws IOException {
            ResourceResponse response = new ResourceResponse(request, uri);
            ResourceConnection conn = new ResourceConnection(uri);
            ByteArrayInputStream bais = new ByteArrayInputStream("<doc>".getBytes(StandardCharsets.UTF_8));
            conn.setStream(bais);
            response.setConnection(conn);
            return response;
        }
    }

}