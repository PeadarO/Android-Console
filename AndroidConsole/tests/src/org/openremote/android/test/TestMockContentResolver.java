package org.openremote.android.test;

import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.test.mock.MockContentResolver;

public class TestMockContentResolver extends MockContentResolver {

   private Context context;
   private ContentProvider provider;

   public TestMockContentResolver() {
      super();
   }

   /**
    * @return the ctx
    */
   public Context getContext() {
      return context;
   }

   /**
    * @param ctx
    *           the ctx to set
    */
   public void setContext(Context context) {
      this.context = context;
   }

   /**
    * @return the provider
    */
   public ContentProvider getProvider() {
      if (null == provider) {
         provider = new TestMockContentProvider(); // use your application's provider class implementation here
         ProviderInfo pi = new ProviderInfo();
         pi.authority = "org.openremote.android.test.provider.authority"; // use the provider authority name defined for
                                                                         // your application, either in the manifest or
                                                                         // in a static 'persistence' class
         pi.enabled = true;
         pi.isSyncable = false;
         pi.packageName = TestMockContentProvider.class.getPackage().getName(); // again - customize this for your
         pi.packageName = "";                                                                     // application
         provider.attachInfo(this.context, pi);
         super.addProvider("org.openremote.android.test.provider.authority", provider);
      }
      return provider;
   }

   /**
    * @param provider
    *           the provider to set
    */
   public void setProvider(ContentProvider provider) {
      this.provider = provider;
   }

}
