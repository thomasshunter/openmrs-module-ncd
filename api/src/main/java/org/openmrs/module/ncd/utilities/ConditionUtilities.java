/**
 * 
 */
package org.openmrs.module.ncd.utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.Condition;

/**
 * @author jlbrown
 *
 */
public class ConditionUtilities {
	public static Set<Condition> getConditionSet(String code, String codeSystem) {
		Set<Condition> retVal = new HashSet<Condition>(0);
        List<CodeCondition> loincCodeRows = NCDUtilities.getService().findByCodeAndSystem(code, codeSystem);
        if (loincCodeRows != null) {
            for (CodeCondition loincCodeRow : loincCodeRows) {
            	Condition condition = loincCodeRow.getCondition();
            	retVal.add(condition);            	
            }
        }
        return retVal;
	}
}
