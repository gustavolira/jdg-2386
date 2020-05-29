import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.Assert;
import org.junit.Test;

public class GetBulkTest {

   private static final int TOTAL_ENTRIES = 5;
   private static final String ISPN_10= "10";

   @Test
   public void getBulkTestCompatibility() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
      RemoteCache<String, String> remoteCache = getRemoteCache();

      String fullVersion = RemoteCache.class.getPackage().getImplementationVersion();
      Integer version = Integer.parseInt(fullVersion.substring(0, fullVersion.indexOf(".")));

      System.out.println(version);

//      // RemoteCache should use an older version than ISPN 10
//      Assert.assertFalse(RemoteCache.class.getPackage().getImplementationVersion().startsWith(ISPN_10));
//
      Map<String, String> map = new HashMap<>();
      IntStream.rangeClosed(1,TOTAL_ENTRIES).forEach(i-> map.put("key"+i, "value"+i));

      remoteCache.putAll(map);


      Method getBulkMethod = remoteCache.getClass().getDeclaredMethod("getBulk");

      Map<String, String> bulk = (Map<String, String>) getBulkMethod.invoke(remoteCache);

      IntStream.rangeClosed(1,TOTAL_ENTRIES).forEach(i-> {
         assertTrue(bulk.containsKey("key"+i));
         assertTrue(bulk.containsValue("value"+i));
      });
      Assert.assertEquals(TOTAL_ENTRIES, bulk.size());

      Method getBulkSizeMethod = remoteCache.getClass().getDeclaredMethod("getBulk", int.class);
      Map<String, String> bulk2 = (Map<String, String>) getBulkSizeMethod.invoke(remoteCache, 2);
      Assert.assertEquals(2, bulk2.size());
   }

   private RemoteCache<String, String> getRemoteCache() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("localhost").port(11222);
      RemoteCacheManager remoteCacheManager = new RemoteCacheManager(builder.build());
      return remoteCacheManager.administration().getOrCreateCache("default", new org.infinispan.configuration.cache.ConfigurationBuilder().build());
   }

}

