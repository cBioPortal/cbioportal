/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.servlet;

import junit.framework.TestCase;
import org.junit.Test;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;
import org.mskcc.cbio.portal.servlet.MutSigJSON;

import java.util.HashSet;

public class TestMutSigJSON extends TestCase {

    @Test
    public void testMutSigtoMap()
    {
        // make a stupid test
        CanonicalGene canonicalGene = new CanonicalGene(12, "hello", new HashSet<String>());

        // test one of the values of the map.  probably a waste of time
        MutSig mutsig = new MutSig(1, canonicalGene, 1, 502500, 20, 1E-11f, 1E-8f);
        assertTrue(!MutSigJSON.MutSigtoMap(mutsig).isEmpty());
        assertTrue(MutSigJSON.MutSigtoMap(mutsig).get("qval").equals(1E-8f));

        // should now reject because qval is too large
        mutsig = new MutSig(1, canonicalGene, 1, 502500, 20, 12f, 12f);
        assertFalse(MutSigJSON.MutSigtoMap(mutsig).isEmpty());

        // sometimes the qvals comes in a funny format
        mutsig = new MutSig(1, canonicalGene, 1, 502500, 20, 1E-8f, 1E-8f);
        assertTrue(!MutSigJSON.MutSigtoMap((mutsig)).isEmpty());
        assertTrue(MutSigJSON.MutSigtoMap(mutsig).get("qval").equals(1E-8f));

        // untested : functionality of DoGet method
        //
        // this is the most important part!
        // to test this we would have to do some work to simulate an httpServletRequest
        // I've been doing this from the browser
    }
}

