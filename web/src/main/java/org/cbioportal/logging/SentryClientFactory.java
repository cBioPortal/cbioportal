package org.cbioportal.logging;

import io.sentry.*;
import io.sentry.dsn.Dsn;
import io.sentry.event.helper.ContextBuilderHelper;
import io.sentry.event.helper.ForwardedAddressResolver;
import io.sentry.event.helper.HttpEventBuilderHelper;

public class SentryClientFactory extends DefaultSentryClientFactory {
    @Override
    public SentryClient createSentryClient(Dsn dsn) {
        SentryClient sentryClient = new SentryClient(createConnection(dsn), getContextManager(dsn));

        /*
         Create and use the ForwardedAddressResolver, which will use the
         X-FORWARDED-FOR header for the remote address if it exists.
          */
        ForwardedAddressResolver forwardedAddressResolver = new ForwardedAddressResolver();
        
        sentryClient.addBuilderHelper(new HttpEventBuilderHelper(forwardedAddressResolver));

        sentryClient.addBuilderHelper(new ContextBuilderHelper(sentryClient));
        return configureSentryClient(sentryClient, dsn);
    }
}