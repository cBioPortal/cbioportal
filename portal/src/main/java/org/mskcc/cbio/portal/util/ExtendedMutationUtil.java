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

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.cgds.model.ExtendedMutation;

/**
 * Utility class related to ExtendedMutation.
 */
public class ExtendedMutationUtil
{
	public static Integer calculateCosmicCount(ExtendedMutation mutation)
	{
		String cosmicOverlap = mutation.getOncotatorCosmicOverlapping();

		if (cosmicOverlap == null)
		{
			return -1;
		}

		String[] parts = cosmicOverlap.split("\\|");
		Integer total = 0;

		for (String cosmic : parts)
		{
			int beginIdx = cosmic.indexOf('(') + 1;
			int endIdx = cosmic.indexOf(")");

			String count;

			if (beginIdx == 0 || endIdx < 0)
			{
				count = "0";
			}
			else
			{
				count = cosmic.substring(beginIdx, endIdx);
			}

			boolean unknownCosmic = cosmic.startsWith("p.?") || cosmic.startsWith("?");

			// update the total count if the count is a valid integer value
			// and the cosmic value does not start with "?"
			if (count.matches("[0-9]+") &&
				!unknownCosmic)
			{
				total += Integer.parseInt(count);
			}
		}

		return total;
	}
}
