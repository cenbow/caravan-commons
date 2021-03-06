/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.caravan.commons.httpclient.impl;

import java.lang.reflect.Field;

import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Utility functions to get internal configuration settings from an HttpClient instance.
 * Assumes the instance is an instance of org.apache.http.impl.client.InternalHttpClient.
 */
public final class HttpClientTestUtils {

  private HttpClientTestUtils() {
    // static methods only
  }

  public static RequestConfig getDefaultRequestConfig(HttpClient httpClient) {
    return (RequestConfig)getField(httpClient, "defaultConfig");
  }

  public static int getConnectTimeout(HttpClient httpClient) {
    return getDefaultRequestConfig(httpClient).getConnectTimeout();
  }

  public static PoolingHttpClientConnectionManager getConnectionManager(HttpClient httpClient) {
    return (PoolingHttpClientConnectionManager)getField(httpClient, "connManager");
  }

  public static CredentialsProvider getCredentialsProvider(HttpClient httpClient) {
    return (CredentialsProvider)getField(httpClient, "credentialsProvider");
  }

  public static HttpHost getProxyHost(HttpClient httpClient) {
    HttpRoutePlanner routePlanner = (HttpRoutePlanner)getField(httpClient, "routePlanner");
    if (routePlanner instanceof DefaultProxyRoutePlanner) {
      return (HttpHost)getField(routePlanner, "proxy");
    }
    else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static Registry<ConnectionSocketFactory> getSchemeRegistry(HttpClient httpClient) {
    PoolingHttpClientConnectionManager connManager = getConnectionManager(httpClient);
    Object connectionOperator = getField(connManager, "connectionOperator");
    return (Registry<ConnectionSocketFactory>)getField(connectionOperator, "socketFactoryRegistry");
  }

  private static Object getField(Object object, String fieldName) {
    return getField(object, object.getClass(), fieldName);
  }

  private static Object getField(Object object, Class clazz, String fieldName) {
    Field field;
    try {
      field = clazz.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(object);
    }
    catch (NoSuchFieldException ex) {
      Class superClazz = clazz.getSuperclass();
      if (superClazz != null) {
        return getField(object, superClazz, fieldName);
      }
      else {
        throw new RuntimeException("Unable to get field value for '" + fieldName + "'.", ex);
      }
    }
    catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
      throw new RuntimeException("Unable to get field value for '" + fieldName + "'.", ex);
    }
  }

}
