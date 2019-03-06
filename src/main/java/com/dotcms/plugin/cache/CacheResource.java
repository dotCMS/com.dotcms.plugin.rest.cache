package com.dotcms.plugin.cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dotcms.enterprise.cache.provider.CacheProviderAPIImpl;
import com.dotcms.repackage.com.ibm.icu.impl.Assert;
import com.dotcms.repackage.javax.ws.rs.GET;
import com.dotcms.repackage.javax.ws.rs.Path;
import com.dotcms.repackage.javax.ws.rs.PathParam;
import com.dotcms.repackage.javax.ws.rs.core.Context;
import com.dotcms.repackage.javax.ws.rs.core.Response;
import com.dotcms.rest.annotation.NoCache;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.business.cache.provider.CacheProvider;
import com.dotmarketing.util.json.JSONArray;

/**
 * 
 * 
 * Call
 *
 */
@Path("/v1/cache")
public class CacheResource {


  @Context
  private HttpServletRequest httpRequest;

  private Method method = null;
  private CacheProviderAPIImpl providerAPI = null;

  private final List<CacheProvider> getProviders(final String group) {

    if (providerAPI == null) {
      providerAPI = (CacheProviderAPIImpl) APILocator.getCacheProviderAPI();
      try {

        printDeclareMethods (providerAPI.getClass());
        method = providerAPI.getClass().getDeclaredMethod("getProvidersForRegion", new Class[] {String.class});
        method.setAccessible(true);
      } catch (NoSuchMethodException | SecurityException e) {
        try {
          method = providerAPI.getClass().getDeclaredMethod("b", new Class[] {String.class});
          method.setAccessible(true);
        } catch (NoSuchMethodException e1) {
          throw new DotStateException(e);
        }
      }
    }

    try {
      return (List<CacheProvider>) method.invoke(providerAPI, group);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new DotStateException(e);
    }

  }

  private void printDeclareMethods(Class<? extends CacheProviderAPIImpl> aClass) {

    final Method methods[] = aClass.getDeclaredMethods();
    for (int i = 0; i < methods.length; ++i) {

      System.out.println(methods[i]);
    }
  }

  private final CacheProvider getProvider(final String group, final String provider) {

    List<CacheProvider> providers = getProviders(group);

    for (CacheProvider cache : providers) {
      if (cache.getKey().equals(provider)) {
        return cache;
      }

    }
    throw new DotStateException("Unable to find provider: " + provider + " provider for group:" + group);



  }

  @NoCache
  @GET
  @Path("/hello")
  public Response hello(@Context final HttpServletRequest request,
                             @Context final HttpServletResponse response) {

    final Response.ResponseBuilder responseBuilder = Response.ok("Cache Says Hello");
    return responseBuilder.build();
  }

  @NoCache
  @GET
  @Path("/groups")
  public Response showGroups(@Context final HttpServletRequest request,
                                @Context final HttpServletResponse response) {

    System.out.println(APILocator.getCacheProviderAPI().getGroups());
    final Response.ResponseBuilder responseBuilder = Response.ok(APILocator.getCacheProviderAPI().getGroups().toString());
    return responseBuilder.build();
  }



  @NoCache
  @GET
  @Path("/providers/{group}")
  public Response showProviders(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response, @PathParam("group") final String group) {

    final Response.ResponseBuilder responseBuilder = Response.ok(getProviders(group).stream().map(this::providerToString).collect(Collectors.toList()).toString());
    return responseBuilder.build();
  }

  private String providerToString(CacheProvider cacheProvider) {
    return "{ key: "  + cacheProvider.getKey() +
            ", name:" + cacheProvider.getName() +
            ", group"  + cacheProvider.getGroups() +
            ", stats"  + cacheProvider.getStats().toString() +
            "}";
  }


  @NoCache
  @GET
  @Path("/provider/{provider}/{group}")
  public Response showProviders(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response, @PathParam("provider") final String provider, @PathParam("group") final String group) {

    final Response.ResponseBuilder responseBuilder = Response.ok(this.providerToString(getProvider(group, provider)));
    return responseBuilder.build();
  }




  @NoCache
  @GET
  @Path("/provider/{provider}/keys/{group}")
  public Response getKeys(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response, @PathParam("provider") final String provider,  @PathParam("group") final String group) {


    Set<String> keys = new HashSet<>();
    keys.addAll(getProvider(group, provider).getKeys(group));

    JSONArray jo = new JSONArray(keys);
    final Response.ResponseBuilder responseBuilder = Response.ok(jo.toString());
    return responseBuilder.build();
  }

  @NoCache
  @GET
  @Path("/provider/{provider}/object/{group}/{id}")
  public Response showObject(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response,@PathParam("provider") final String provider,  @PathParam("group") final String group,
      @PathParam("id") final String id) {

    Object obj = getProvider(group, provider).get(group, id);

    obj = (obj == null) ? "NOPE" : obj;
    // JSONObject jo = new JSONObject(obj);

    final Response.ResponseBuilder responseBuilder = Response.ok(obj.toString());
    return responseBuilder.build();
  }

  
  @NoCache
  @GET
  @Path("/provider/{provider}/flush/{group}")
  public Response flushGroup(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response, @PathParam("provider") final String provider,  @PathParam("group") final String group) {

    getProvider(group, provider).remove(group);
    final Response.ResponseBuilder responseBuilder = Response.ok("flushed");
    return responseBuilder.build();
  }
  
  
  
  
  
  @NoCache
  @GET
  @Path("/provider/{provider}/flush/{group}/{id}")
  public Response flushObject(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response,@PathParam("provider") final String provider,  @PathParam("group") final String group,
      @PathParam("id") final String id) {
    getProvider(group, provider).remove(group, id);
    final Response.ResponseBuilder responseBuilder = Response.ok("flushed");
    return responseBuilder.build();
  }


}
