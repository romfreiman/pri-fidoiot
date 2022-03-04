package org.fidoalliance.fdo.protocol;

import java.io.IOException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class StandardHttpClientSupplier implements HttpClientSupplier {

  @Override
  public CloseableHttpClient get() throws IOException {
    return HttpClients.createSystem();
  }
}
