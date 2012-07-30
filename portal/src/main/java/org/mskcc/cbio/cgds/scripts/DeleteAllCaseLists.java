package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cbio.cgds.dao.DaoCaseList;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

/**
 * Command Line Tool to Delete All Case Lists.
 */
public class DeleteAllCaseLists {

    public static void main(String[] args) throws Exception {
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        DaoCaseList daoCaseList = new DaoCaseList();
        daoCaseList.deleteAllRecords();
        System.out.println ("\nAll Existing Case Lists Deleted.");
        ConsoleUtil.showWarnings(pMonitor);
    }
}