package org.mskcc.cbio.portal.scripts;

import java.io.File;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.mskcc.cbio.portal.util.TransactionalScripts;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A high-level script runner than can be used to run a batch of scripts within a
 * transactional context. It's handy loading a batch of data of different types.
 * This is already deprecated, because in the long run everything should be
 * Runnable and/or work using Spring Batch or similar, but this is a quick and
 * easy way to get a transactional block of data loaded through different
 * import scripts defined elsewhere.
 *
 * The principle is a simple one: we get a set of lines on an input stream and
 * we read from it. The lines are parsed into a class and an array of String
 * arguments, which are then passed to the corresponding class as a call to
 * their static main method. All this happens within a single JVM context,
 * which will enclose a dependency-injected Spring context file. So we actually
 * configure the whole caboodle (nastily) from Spring itself.
 *
 * Typically we'd expect two context files to be used: one for the database
 * (which will be constant for most applications) and a second which which will
 * be generated for a study load. So a Python wrapper can generate the second
 * file, and pass it with a DB file configured for the whole portal to load
 * a study transactionally. This pushes some of the logic into the Python code,
 * but does mean that load errors don't corrupt the whole database.
 *
 * @author stuartw
 *
 */
public class TransactionalScriptRunner extends ConsoleRunnable {
    private TransactionTemplate transactionTemplate;

    private TransactionalScriptRunner(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        (new TransactionalScriptRunner(args)).run();
    }

    public void run() {
        String result = null;

        if (args.length == 0) {
            throw new UsageException(
                "TransactionalScriptRunner",
                "context file",
                "context_file"
            );
        }

        FileSystemXmlApplicationContext context = null;

        // Load the context files. This should set up the DB and core
        try {
            // Convert files to URLs to make them work as absolutes
            // See: Javadocs for FileSystemXmlApplicationContext
            for (int i = 0; i < args.length; i++) {
                args[i] = new File(args[i]).toURI().toURL().toString();
            }

            context = new FileSystemXmlApplicationContext(args);

            // Inject the context into SpringUtil, so we don't need to initialize again.
            // This ensures that the XML files from the command line provide a complete
            // context and we don't get data sources later from anywhere else.
            SpringUtil.initDataSource(context);

            // Set up the transaction template
            transactionTemplate =
                (TransactionTemplate) context.getBean(
                    "scriptTransactionTemplate"
                );
            if (transactionTemplate == null) {
                throw new Exception(
                    "Can't find transaction template in Spring context"
                );
            }

            // Locate the scripts data
            TransactionalScripts scripts = context.getBean(
                TransactionalScripts.class
            );

            // And run the scripts
            runInTransaction(scripts);
        } catch (Exception e) {
            result = e.getLocalizedMessage();
        } finally {
            if (context != null) {
                context.close();
            }
        }

        // If something went wrong, notify
        if (result != null) {
            throw new RuntimeException(result);
        }
    }

    public void runInTransaction(final TransactionalScripts scripts) {
        transactionTemplate.execute(
            new TransactionCallback<Object>() {

                // the code in this method executes in a transactional context
                public Object doInTransaction(TransactionStatus status) {
                    scripts.run();
                    return null;
                }
            }
        );
    }
}
